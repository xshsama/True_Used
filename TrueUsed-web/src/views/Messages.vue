<script setup>
import SearchBar from '@/components/SearchBar.vue';
import { getProduct } from '@/api/products';
import { useMessageStore } from '@/stores/message';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import {
    Bell,
    Clock3,
    MessageSquare,
    Search,
    Send,
    ShieldCheck,
    Sparkles,
    Store,
    UserRound
} from 'lucide-vue-next';
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();
const messageStore = useMessageStore();
const userStore = useUserStore();

const inputText = ref('');
const searchKeyword = ref('');
const messagesContainer = ref(null);
const activeChatId = ref(null);
const linkedProduct = ref(null);
const linkedProductLoading = ref(false);

const quickReplies = ['还在吗？', '支持平台验货吗？', '什么时候可以发货？', '成色细节能再拍一下吗？', '价格还有空间吗？'];

const currentUser = computed(() => {
    const user = userStore.user;
    return {
        id: user?.id || 'me',
        avatar: resolveAvatar(user?.avatarUrl, user?.avatar)
    };
});

const chatList = computed(() => {
    return messageStore.conversations.map(conversation => ({
        id: conversation.id,
        name: conversation.otherUserName || '未知用户',
        avatar: resolveAvatar(conversation.otherUserAvatar),
        lastMessage: conversation.lastMessage || '点击开始沟通',
        time: formatTime(conversation.lastMessageTime),
        unread: conversation.unreadCount || 0,
        otherUserId: conversation.otherUserId,
        online: messageStore.onlineUsers.has(conversation.otherUserId)
    }));
});

const filteredChatList = computed(() => {
    const keyword = searchKeyword.value.trim().toLowerCase();

    if (!keyword) {
        return chatList.value;
    }

    return chatList.value.filter(chat => {
        return `${chat.name} ${chat.lastMessage}`.toLowerCase().includes(keyword);
    });
});

const messages = computed(() => {
    return messageStore.messages.map(message => ({
        id: message.id,
        type: message.isSelf ? 'me' : 'other',
        content: message.content,
        time: formatTime(message.timestamp)
    }));
});

const activeChat = computed(() => {
    return chatList.value.find(chat => chat.id === activeChatId.value) || null;
});

const linkedProductSummary = computed(() => {
    if (!linkedProduct.value) return null;

    const trade = normalizeProductTrade(linkedProduct.value);
    const image = linkedProduct.value.images?.[0]?.url || linkedProduct.value.imageUrl || linkedProduct.value.coverUrl || '';
    const price = Number(linkedProduct.value.price || 0);

    return {
        id: linkedProduct.value.id,
        title: linkedProduct.value.title || '未命名商品',
        image: image || 'https://via.placeholder.com/320x240/111827/ffffff?text=TrueUsed',
        priceText: price > 0 ? `¥${price.toLocaleString('zh-CN')}` : '价格待定',
        tradeLabel: trade.tradeModeLabel,
        conditionLabel: trade.primaryConditionLabel,
        statusLabel: trade.saleStatusLabel || '状态待同步',
        riskNote: trade.hasPlatformInspection
            ? (trade.inspectionStatus === 'passed' ? '平台验货已完成，可按报告核对细节' : '平台验货链路未完成，先确认上架状态')
            : '卖家自出商品，重点确认成色、发货和售后边界'
    };
});

const unreadConversationCount = computed(() => {
    return chatList.value.filter(chat => chat.unread > 0).length;
});

const onlineConversationCount = computed(() => {
    return chatList.value.filter(chat => chat.online).length;
});

const workspaceStats = computed(() => {
    return [
        {
            label: '会话总数',
            value: `${chatList.value.length}`,
            note: '当前账号下的全部对话'
        },
        {
            label: '待回复',
            value: `${unreadConversationCount.value}`,
            note: '需要优先处理的未读会话'
        },
        {
            label: '在线联系人',
            value: `${onlineConversationCount.value}`,
            note: '可以即时沟通的卖家/买家'
        }
    ];
});

const conversationSignals = computed(() => {
    if (!activeChat.value) {
        return [
            { label: '当前状态', value: '未选会话', note: '从左侧选择联系人后开始沟通' },
            { label: '风险提示', value: '先看成色', note: '卖家自出和平台验货要分开判断' }
        ];
    }

    if (linkedProductSummary.value) {
        return [
            {
                label: '当前状态',
                value: activeChat.value.online ? '在线沟通' : '等待回复',
                note: activeChat.value.unread ? `还有 ${activeChat.value.unread} 条未读` : '本会话暂无未读'
            },
            {
                label: '交易模式',
                value: linkedProductSummary.value.tradeLabel,
                note: `${linkedProductSummary.value.conditionLabel} · ${linkedProductSummary.value.statusLabel}`
            },
            {
                label: '风险提示',
                value: linkedProductSummary.value.tradeLabel === '平台验货' ? '核对报告' : '确认成色',
                note: linkedProductSummary.value.riskNote
            }
        ];
    }

    return [
        {
            label: '当前状态',
            value: activeChat.value.online ? '在线沟通' : '等待回复',
            note: activeChat.value.unread ? `还有 ${activeChat.value.unread} 条未读` : '本会话暂无未读'
        },
        {
            label: '风险提示',
            value: '确认成色',
            note: '建议先确认卖家自出成色，再决定是否需要平台验货'
        }
    ];
});

function formatTime(timestamp) {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    const diff = Date.now() - date.getTime();

    if (diff < 86400000) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    return date.toLocaleDateString();
}

function scrollToBottom() {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
    });
}

function routeProductId() {
    const raw = route.query.productId;
    const value = Array.isArray(raw) ? raw[0] : raw;
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}

async function loadLinkedProduct() {
    const productId = routeProductId();

    if (!productId) {
        linkedProduct.value = null;
        return;
    }

    try {
        linkedProductLoading.value = true;
        linkedProduct.value = await getProduct(productId);
    } catch (error) {
        console.error('Failed to load linked product', error);
        linkedProduct.value = null;
    } finally {
        linkedProductLoading.value = false;
    }
}

function openLinkedProduct() {
    if (!linkedProductSummary.value?.id) return;
    router.push({ name: 'ProductDetail', params: { id: linkedProductSummary.value.id } });
}

function openUserProfile(userId) {
    const id = Number(userId);
    if (!Number.isInteger(id) || id <= 0) return;
    router.push({ name: 'SellerProfile', params: { id } });
}

async function handleSend() {
    if (!inputText.value.trim() || !activeChat.value?.otherUserId) return;

    await messageStore.sendMessage(activeChat.value.otherUserId, inputText.value.trim());
    inputText.value = '';
    scrollToBottom();
}

function handleQuickReply(text) {
    inputText.value = text;
    handleSend();
}

async function openConversation(chat, syncRoute = true) {
    if (!chat) return;

    activeChatId.value = chat.id;
    messageStore.markConversationAsRead(chat.id);

    if (messageStore.currentConversationId !== chat.id) {
        await messageStore.fetchMessages(chat.id);
    }

    scrollToBottom();

    if (syncRoute && route.path !== `/messages/chat/${chat.id}`) {
        router.replace(`/messages/chat/${chat.id}`);
    }
}

async function syncConversationFromRoute() {
    const routeId = route.params.id ? Number(route.params.id) : null;

    if (!routeId) {
        if (!activeChatId.value && chatList.value.length) {
            await openConversation(chatList.value[0]);
        }
        return;
    }

    const existingChat = chatList.value.find(chat => Number(chat.id) === routeId);

    if (existingChat) {
        await openConversation(existingChat, false);
        return;
    }

    activeChatId.value = routeId;

    if (messageStore.currentConversationId !== routeId) {
        await messageStore.fetchMessages(routeId);
    }

    scrollToBottom();
}

watch(() => route.params.id, () => {
    syncConversationFromRoute();
});

watch(() => route.query.productId, () => {
    loadLinkedProduct();
});

watch(() => chatList.value.length, () => {
    syncConversationFromRoute();
});

watch(() => messageStore.messages.length, () => {
    scrollToBottom();
});

onMounted(async () => {
    messageStore.connect();
    await Promise.all([
        messageStore.fetchConversations(),
        loadLinkedProduct()
    ]);
    await syncConversationFromRoute();
});

onUnmounted(() => {
    messageStore.clearCurrentConversation();
});
</script>

<template>
    <div class="min-h-screen bg-transparent">
        <div class="mx-auto max-w-[1480px] px-8 py-6">
            <section
                class="relative overflow-hidden rounded-[32px] border border-white/70 bg-gradient-to-br from-[#111d21] via-[#172d34] to-[#274852] px-8 py-8 text-white shadow-[0_28px_80px_rgba(15,23,42,0.18)]">
                <div class="absolute right-0 top-0 h-72 w-72 rounded-full bg-white/8 blur-3xl"></div>

                <div class="relative z-10 flex items-end justify-between gap-10">
                    <div>
                        <div class="inline-flex items-center gap-2 rounded-full border border-sky-400/20 bg-sky-300/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-sky-100">
                            <MessageSquare :size="14" />
                            Messaging Desk
                        </div>
                        <h1 class="mt-4 text-4xl font-black tracking-tight">消息中心</h1>
                        <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-100/72">
                            这里不再按移动端聊天框思路堆按钮，而是拆成桌面三栏结构，让会话、正文和交易侧栏同时可见。
                        </p>
                    </div>

                    <div class="grid grid-cols-3 gap-4">
                        <div v-for="card in workspaceStats" :key="card.label"
                            class="rounded-3xl border border-white/10 bg-white/10 px-5 py-4 backdrop-blur">
                            <div class="text-xs uppercase tracking-[0.18em] text-slate-200/60">{{ card.label }}</div>
                            <div class="mt-2 text-3xl font-black">{{ card.value }}</div>
                            <div class="mt-2 text-xs text-slate-100/60">{{ card.note }}</div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-6 grid grid-cols-[340px_minmax(0,1fr)_320px] gap-6">
                <aside
                    class="overflow-hidden rounded-[32px] border border-white/70 bg-white/92 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                    <div class="border-b border-slate-100 px-5 py-5">
                        <div class="flex items-center justify-between">
                            <div>
                                <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">Conversations</div>
                                <h2 class="mt-2 text-2xl font-black text-slate-950">会话列表</h2>
                            </div>
                            <button type="button"
                                class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500 transition-colors hover:border-emerald-200 hover:text-emerald-700">
                                <Bell :size="16" />
                            </button>
                        </div>

                        <div class="mt-5 rounded-[24px] bg-slate-50 px-4 py-4">
                            <SearchBar v-model="searchKeyword" placeholder="搜索联系人或聊天内容"
                                :show-shortcut="false" :show-submit="false" size="compact" />

                            <div class="mt-4 grid grid-cols-2 gap-3">
                                <div class="rounded-2xl bg-white px-4 py-3">
                                    <div class="text-xs uppercase tracking-[0.18em] text-slate-400">未读会话</div>
                                    <div class="mt-2 text-2xl font-black text-slate-950">{{ unreadConversationCount }}</div>
                                </div>
                                <div class="rounded-2xl bg-white px-4 py-3">
                                    <div class="text-xs uppercase tracking-[0.18em] text-slate-400">在线联系</div>
                                    <div class="mt-2 text-2xl font-black text-slate-950">{{ onlineConversationCount }}</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="h-[calc(100vh-360px)] overflow-y-auto px-3 py-3">
                        <button v-for="chat in filteredChatList" :key="chat.id" type="button"
                            class="mb-2 grid w-full grid-cols-[56px_minmax(0,1fr)_48px] items-center gap-3 rounded-[24px] px-3 py-3 text-left transition-all"
                            :class="activeChatId === chat.id
                                ? 'bg-emerald-50 shadow-[0_14px_28px_rgba(16,185,129,0.10)]'
                                : 'hover:bg-slate-50'"
                            @click="openConversation(chat)">
                            <div class="relative cursor-pointer" @click.stop="openUserProfile(chat.otherUserId)">
                                <img :src="chat.avatar"
                                    class="h-14 w-14 rounded-2xl border border-slate-100 object-cover shadow-sm transition-transform hover:scale-[1.03]"
                                    :alt="`${chat.name}的头像`" />
                                <span v-if="chat.online"
                                    class="absolute bottom-0 right-0 h-3.5 w-3.5 rounded-full border-2 border-white bg-emerald-500"></span>
                            </div>

                            <div class="min-w-0">
                                <div class="flex items-center justify-between gap-3">
                                    <div class="truncate text-sm font-bold text-slate-900">{{ chat.name }}</div>
                                    <div class="text-[11px] text-slate-400">{{ chat.time }}</div>
                                </div>
                                <div class="mt-1 truncate text-xs leading-6"
                                    :class="chat.unread ? 'font-semibold text-slate-700' : 'text-slate-500'">
                                    {{ chat.lastMessage }}
                                </div>
                            </div>

                            <div class="flex flex-col items-end gap-2">
                                <span v-if="chat.unread"
                                    class="inline-flex min-w-[22px] items-center justify-center rounded-full bg-rose-500 px-2 py-1 text-[10px] font-bold text-white">
                                    {{ chat.unread > 99 ? '99+' : chat.unread }}
                                </span>
                                <span class="text-[10px] text-slate-300">
                                    {{ chat.online ? '在线' : '离线' }}
                                </span>
                            </div>
                        </button>

                        <div v-if="!filteredChatList.length"
                            class="flex h-full flex-col items-center justify-center text-center text-slate-400">
                            <div class="mb-4 rounded-full bg-slate-50 p-4">
                                <Search :size="22" />
                            </div>
                            <div class="text-sm font-semibold text-slate-700">没有匹配的会话</div>
                            <div class="mt-2 text-xs">换个关键词试试，或者清空搜索。</div>
                        </div>
                    </div>
                </aside>

                <main
                    class="overflow-hidden rounded-[32px] border border-white/70 bg-white/92 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                    <div v-if="activeChat" class="flex h-full min-h-[calc(100vh-240px)] flex-col">
                        <div class="border-b border-slate-100 px-6 py-5">
                            <div class="flex items-center justify-between gap-5">
                                <div class="flex items-center gap-4">
                                    <button type="button" class="relative cursor-pointer"
                                        @click="openUserProfile(activeChat.otherUserId)">
                                        <img :src="activeChat.avatar"
                                            class="h-14 w-14 rounded-2xl border border-slate-100 object-cover shadow-sm transition-transform hover:scale-[1.03]"
                                            :alt="`${activeChat.name}的头像`" />
                                        <span v-if="activeChat.online"
                                            class="absolute bottom-0 right-0 h-3.5 w-3.5 rounded-full border-2 border-white bg-emerald-500"></span>
                                    </button>
                                    <div>
                                        <div class="flex items-center gap-3">
                                            <h2 class="text-2xl font-black text-slate-950">{{ activeChat.name }}</h2>
                                            <span
                                                class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                                                <ShieldCheck :size="14" />
                                                沟通中
                                            </span>
                                        </div>
                                        <div class="mt-2 flex items-center gap-4 text-sm text-slate-400">
                                            <span class="inline-flex items-center gap-1.5">
                                                <Clock3 :size="14" />
                                                最近活跃 {{ activeChat.time || '刚刚' }}
                                            </span>
                                            <span>{{ activeChat.online ? '当前在线' : '当前离线' }}</span>
                                        </div>
                                    </div>
                                </div>

                                <div v-if="linkedProductSummary" class="flex items-center gap-3">
                                    <button type="button" @click="openLinkedProduct"
                                        class="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-600 transition-colors hover:border-emerald-200 hover:text-emerald-700">
                                        <Store :size="16" />
                                        打开商品
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div ref="messagesContainer" class="flex-1 space-y-5 overflow-y-auto bg-slate-50 px-6 py-6">
                            <div
                                class="mx-auto max-w-max rounded-full border border-amber-200 bg-amber-50 px-4 py-2 text-xs font-medium text-amber-700">
                                {{ linkedProductSummary ? `当前沟通商品：${linkedProductSummary.title}` : '建议先确认成色描述，再判断是否需要走平台验货流程。' }}
                            </div>

                            <div v-for="message in messages" :key="message.id"
                                class="flex gap-3"
                                :class="message.type === 'me' ? 'justify-end' : 'justify-start'">
                                <template v-if="message.type !== 'me'">
                                    <button type="button" class="h-10 w-10 shrink-0 rounded-full"
                                        @click="openUserProfile(activeChat.otherUserId)">
                                        <img :src="activeChat.avatar"
                                            class="h-10 w-10 rounded-full border border-white object-cover shadow-sm transition-transform hover:scale-[1.05]"
                                            :alt="`${activeChat.name}的头像`" />
                                    </button>
                                </template>

                                <div class="max-w-[68%]">
                                    <div class="rounded-[24px] px-4 py-3 text-sm leading-7 shadow-sm"
                                        :class="message.type === 'me'
                                            ? 'rounded-tr-md bg-emerald-600 text-white'
                                            : 'rounded-tl-md border border-slate-100 bg-white text-slate-700'">
                                        {{ message.content }}
                                    </div>
                                    <div class="mt-2 px-2 text-[11px] text-slate-400"
                                        :class="message.type === 'me' ? 'text-right' : 'text-left'">
                                        {{ message.time }}
                                    </div>
                                </div>

                                <template v-if="message.type === 'me'">
                                    <img :src="currentUser.avatar"
                                        class="h-10 w-10 rounded-full border border-white object-cover shadow-sm" />
                                </template>
                            </div>

                            <div v-if="!messages.length"
                                class="flex h-full min-h-[260px] flex-col items-center justify-center text-center text-slate-400">
                                <div class="mb-4 rounded-full bg-white p-5 shadow-sm">
                                    <MessageSquare :size="26" />
                                </div>
                                <div class="text-base font-semibold text-slate-700">会话还没有消息</div>
                                <div class="mt-2 text-sm">右下角输入内容后即可开始沟通。</div>
                            </div>
                        </div>

                        <div class="border-t border-slate-100 px-6 py-5">
                            <div class="flex flex-wrap gap-2">
                                <button v-for="text in quickReplies" :key="text" type="button"
                                    class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-500 transition-colors hover:border-emerald-200 hover:text-emerald-700"
                                    @click="handleQuickReply(text)">
                                    {{ text }}
                                </button>
                            </div>

                            <div class="mt-4 flex items-center gap-3">
                                <label class="relative flex-1">
                                    <input v-model="inputText" type="text" placeholder="输入消息，按 Enter 发送"
                                        class="w-full rounded-full border border-slate-200 bg-slate-50 py-3 pl-5 pr-12 text-sm text-slate-700 outline-none transition-colors focus:border-emerald-200 focus:bg-white"
                                        @keydown.enter.prevent="handleSend" />
                                    <UserRound :size="16" class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-300" />
                                </label>

                                <button type="button"
                                    class="inline-flex h-12 w-12 items-center justify-center rounded-full transition-all"
                                    :class="inputText.trim()
                                        ? 'bg-emerald-600 text-white shadow-[0_14px_28px_rgba(16,185,129,0.24)]'
                                        : 'bg-slate-200 text-slate-400'"
                                    :disabled="!inputText.trim()"
                                    @click="handleSend">
                                    <Send :size="18" />
                                </button>
                            </div>
                        </div>
                    </div>

                    <div v-else class="flex min-h-[calc(100vh-240px)] flex-col items-center justify-center text-center text-slate-400">
                        <div class="mb-5 rounded-full bg-slate-50 p-6 shadow-sm">
                            <MessageSquare :size="34" />
                        </div>
                        <div class="text-xl font-semibold text-slate-700">从左侧选择一个联系人</div>
                        <div class="mt-2 max-w-md text-sm leading-7">
                            桌面端会同时展示会话列表、聊天正文和交易侧栏，所以选中一个会话后信息会完整铺开。
                        </div>
                    </div>
                </main>

                <aside
                    class="overflow-hidden rounded-[32px] border border-white/70 bg-white/92 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                    <div class="border-b border-slate-100 px-5 py-5">
                        <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                            <Sparkles :size="14" />
                            Trade Panel
                        </div>
                        <h2 class="mt-3 text-2xl font-black text-slate-950">交易侧栏</h2>
                        <div class="mt-2 text-sm leading-7 text-slate-500">
                            关联商品、验货状态和快捷话术集中展示。
                        </div>
                    </div>

                    <div class="space-y-4 px-5 py-5">
                        <div v-if="linkedProductSummary" class="rounded-[24px] bg-slate-950 p-5 text-white">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/55">关联商品</div>
                            <div class="mt-4 flex gap-3">
                                <img :src="linkedProductSummary.image"
                                    class="h-16 w-16 rounded-2xl border border-white/10 object-cover" />
                                <div class="min-w-0 flex-1">
                                    <div class="line-clamp-2 text-sm font-bold leading-6">{{ linkedProductSummary.title }}</div>
                                    <div class="mt-1 text-2xl font-black">{{ linkedProductSummary.priceText }}</div>
                                    <div class="mt-1 text-xs text-white/58">
                                        {{ linkedProductSummary.tradeLabel }} · {{ linkedProductSummary.statusLabel }}
                                    </div>
                                </div>
                            </div>
                            <button type="button" @click="openLinkedProduct"
                                class="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-white/15">
                                <Store :size="16" />
                                打开商品
                            </button>
                        </div>

                        <div v-else class="rounded-[24px] bg-slate-950 p-5 text-white">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/55">当前联系人</div>
                            <div class="mt-2 text-2xl font-black">
                                {{ activeChat?.name || '未选择会话' }}
                            </div>
                            <div class="mt-2 text-sm text-white/68">
                                {{ linkedProductLoading ? '正在加载商品上下文...' : (activeChat ? `最近消息：${activeChat.lastMessage}` : '左侧选中会话后，这里会展示对话摘要。') }}
                            </div>
                        </div>

                        <div v-for="signal in conversationSignals" :key="signal.label"
                            class="rounded-2xl border border-slate-200 px-4 py-4">
                            <div class="text-xs uppercase tracking-[0.18em] text-slate-400">{{ signal.label }}</div>
                            <div class="mt-2 text-lg font-bold text-slate-950">{{ signal.value }}</div>
                            <div class="mt-2 text-xs leading-6 text-slate-500">{{ signal.note }}</div>
                        </div>

                        <div class="rounded-2xl border border-slate-200 px-4 py-4">
                            <div class="text-xs uppercase tracking-[0.18em] text-slate-400">沟通建议</div>
                            <div class="mt-3 space-y-3 text-sm leading-6 text-slate-600">
                                <div class="flex items-start gap-2">
                                    <ShieldCheck :size="16" class="mt-1 text-emerald-600" />
                                    <span>先确认卖家描述的是“自出成色”还是“平台验货等级”，避免两个概念混用。</span>
                                </div>
                                <div class="flex items-start gap-2">
                                    <Clock3 :size="16" class="mt-1 text-sky-600" />
                                    <span>如果是高客单价商品，建议尽早确认是否支持平台验货，减少后续扯皮。</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </aside>
            </section>
        </div>
    </div>
</template>
