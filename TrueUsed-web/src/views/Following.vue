<script setup>
import { createConversation } from '@/api/chat';
import { listMyFollowing, unfollowUser } from '@/api/users';
import { resolveAvatar } from '@/utils/avatar';
import {
    ArrowLeft,
    Bell,
    MessageCircle,
    Store,
    Trash2,
    UserCheck,
    Users
} from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const sellers = ref([]);
const loading = ref(false);
const actionId = ref(null);
const page = ref(0);
const size = ref(50);

const totalCount = computed(() => sellers.value.length);
const onSaleCount = computed(() => sellers.value.reduce((sum, item) => sum + Number(item.sellingCount || 0), 0));
const totalFollowers = computed(() => sellers.value.reduce((sum, item) => sum + Number(item.followerCount || 0), 0));

const summaryCards = computed(() => [
    { label: '关注卖家', value: `${totalCount.value}`, note: '当前账号关注的卖家' },
    { label: '在售商品', value: `${onSaleCount.value}`, note: '关注卖家正在售卖的商品' },
    { label: '粉丝规模', value: `${totalFollowers.value}`, note: '卖家合计粉丝数' }
]);

function resolveLoadErrorMessage(error) {
    const status = error?.response?.status;
    const message = error?.response?.data?.message || error?.message || '';

    if (!error?.response) {
        return '后端服务未启动或代理不可达';
    }
    if (status === 404) {
        return '关注接口未加载，请重启后端';
    }
    if (status === 500) {
        return message && message !== '服务器内部错误' ? message : '关注数据服务异常，请检查后端日志';
    }
    return message || '加载关注卖家失败';
}

function mapSeller(item) {
    return {
        id: item.id,
        name: item.nickname || item.username || '未知卖家',
        username: item.username || '',
        avatar: resolveAvatar(item.avatarUrl),
        bio: item.bio || '暂无店铺简介',
        location: item.location || '未知地区',
        sellingCount: item.sellingCount || 0,
        soldCount: item.soldCount || 0,
        followerCount: item.followerCount || 0,
        followedAt: item.followedAt ? new Date(item.followedAt).toLocaleDateString() : '未知'
    };
}

async function fetchFollowing() {
    loading.value = true;

    try {
        const res = await listMyFollowing({ page: page.value, size: size.value });
        sellers.value = (res.content || []).map(mapSeller);
    } catch (error) {
        console.error(error);
        showFailToast(resolveLoadErrorMessage(error));
    } finally {
        loading.value = false;
    }
}

function openSeller(id) {
    router.push(`/seller/${id}`);
}

async function messageSeller(seller) {
    try {
        actionId.value = seller.id;
        const res = await createConversation(seller.id);
        if (!res?.id) {
            showFailToast('无法启动会话');
            return;
        }
        router.push(`/messages/chat/${res.id}`);
    } catch (error) {
        console.error(error);
        showFailToast('启动会话失败');
    } finally {
        actionId.value = null;
    }
}

async function removeFollowing(seller) {
    try {
        actionId.value = seller.id;
        await unfollowUser(seller.id);
        sellers.value = sellers.value.filter(item => item.id !== seller.id);
        showSuccessToast('已取消关注');
    } catch (error) {
        console.error(error);
        showFailToast('取消关注失败');
    } finally {
        actionId.value = null;
    }
}

onMounted(fetchFollowing);
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] text-[#24333f]">
        <main class="mx-auto max-w-6xl px-4 py-8">
            <header class="mb-6 flex items-center justify-between gap-4">
                <div class="flex items-center gap-4">
                    <button type="button"
                        class="flex h-11 w-11 items-center justify-center rounded-2xl border border-gray-200 bg-white text-gray-500 transition-colors hover:border-[#2c3e50] hover:text-[#2c3e50]"
                        @click="router.back()">
                        <ArrowLeft :size="18" />
                    </button>
                    <div>
                        <div class="text-xs font-bold uppercase tracking-[0.18em] text-[#4a8b6e]">Following Desk</div>
                        <h1 class="mt-1 text-3xl font-black text-[#24333f]">关注卖家</h1>
                    </div>
                </div>
                <button type="button"
                    class="inline-flex items-center gap-2 rounded-full bg-[#24333f] px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-[#1b2730]"
                    @click="fetchFollowing">
                    <UserCheck :size="16" />
                    刷新
                </button>
            </header>

            <section class="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
                <div v-for="card in summaryCards" :key="card.label"
                    class="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
                    <div class="text-xs font-bold uppercase tracking-[0.16em] text-gray-400">{{ card.label }}</div>
                    <div class="mt-3 text-3xl font-black text-[#24333f]">{{ card.value }}</div>
                    <div class="mt-2 text-sm text-gray-500">{{ card.note }}</div>
                </div>
            </section>

            <section class="overflow-hidden rounded-3xl border border-gray-100 bg-white shadow-sm">
                <div class="overflow-x-auto">
                <div class="grid min-w-[860px] grid-cols-[minmax(0,1.4fr)_120px_120px_150px_220px] border-b border-gray-100 bg-gray-50 px-6 py-3 text-xs font-bold uppercase tracking-[0.14em] text-gray-400">
                    <div>卖家</div>
                    <div>在售</div>
                    <div>粉丝</div>
                    <div>关注时间</div>
                    <div class="text-right">操作</div>
                </div>

                <div v-if="loading" class="py-16 text-center text-sm text-gray-400">正在加载关注卖家...</div>

                <div v-else-if="!sellers.length" class="flex flex-col items-center justify-center py-20 text-center">
                    <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gray-50 text-gray-300">
                        <Users :size="30" />
                    </div>
                    <div class="text-base font-bold text-[#24333f]">暂无关注卖家</div>
                    <div class="mt-2 text-sm text-gray-400">从卖家主页点击关注后，会集中显示在这里。</div>
                    <button type="button"
                        class="mt-6 rounded-full bg-[#24333f] px-5 py-2.5 text-sm font-bold text-white"
                        @click="router.push('/home')">
                        去逛商品
                    </button>
                </div>

                <div v-else>
                    <div v-for="seller in sellers" :key="seller.id"
                        class="grid min-w-[860px] grid-cols-[minmax(0,1.4fr)_120px_120px_150px_220px] items-center border-b border-gray-50 px-6 py-5 last:border-b-0 hover:bg-gray-50/60">
                        <button type="button" class="flex min-w-0 items-center gap-4 text-left"
                            @click="openSeller(seller.id)">
                            <img :src="seller.avatar"
                                class="h-14 w-14 rounded-2xl border border-gray-100 bg-gray-50 object-cover" />
                            <div class="min-w-0">
                                <div class="truncate text-base font-black text-[#24333f]">{{ seller.name }}</div>
                                <div class="mt-1 truncate text-xs text-gray-400">@{{ seller.username || seller.id }} · {{ seller.location }}</div>
                                <div class="mt-1 line-clamp-1 text-sm text-gray-500">{{ seller.bio }}</div>
                            </div>
                        </button>
                        <div class="font-mono text-lg font-black text-[#24333f]">{{ seller.sellingCount }}</div>
                        <div class="font-mono text-lg font-black text-[#24333f]">{{ seller.followerCount }}</div>
                        <div class="text-sm text-gray-500">{{ seller.followedAt }}</div>
                        <div class="flex justify-end gap-2">
                            <button type="button"
                                class="inline-flex items-center gap-1.5 rounded-full border border-gray-200 px-3 py-2 text-xs font-bold text-gray-600 transition-colors hover:border-[#4a8b6e] hover:text-[#4a8b6e]"
                                @click="openSeller(seller.id)">
                                <Store :size="14" />
                                主页
                            </button>
                            <button type="button" :disabled="actionId === seller.id"
                                class="inline-flex items-center gap-1.5 rounded-full border border-gray-200 px-3 py-2 text-xs font-bold text-gray-600 transition-colors hover:border-[#4a8b6e] hover:text-[#4a8b6e] disabled:cursor-not-allowed disabled:opacity-60"
                                @click="messageSeller(seller)">
                                <MessageCircle :size="14" />
                                私信
                            </button>
                            <button type="button" :disabled="actionId === seller.id"
                                class="inline-flex items-center gap-1.5 rounded-full border border-red-100 px-3 py-2 text-xs font-bold text-red-500 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
                                @click="removeFollowing(seller)">
                                <Trash2 :size="14" />
                                取消
                            </button>
                        </div>
                    </div>
                </div>
                </div>
            </section>

            <section class="mt-6 rounded-3xl border border-gray-100 bg-white p-5 shadow-sm">
                <div class="flex items-center gap-3 text-sm font-bold text-[#24333f]">
                    <Bell :size="16" class="text-[#4a8b6e]" />
                    关注卖家后，这里只负责回访入口；商品降价仍保留在“我的收藏”。
                </div>
            </section>
        </main>
    </div>
</template>
