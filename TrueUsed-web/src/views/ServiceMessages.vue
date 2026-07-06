<script setup>
import {
    ArrowLeft,
    BookOpen,
    ClipboardList,
    Headset,
    SendHorizonal,
    ShieldCheck
} from 'lucide-vue-next';
import { computed, ref } from 'vue';
import { showToast } from 'vant';
import { useRouter } from 'vue-router';

const router = useRouter();

const threads = ref([
    {
        id: 'svc-001',
        title: '平台验货单待出库说明',
        type: '交易咨询',
        unread: 2,
        status: '处理中',
        relatedLabel: '订单 #102401',
        shortcut: { label: '查看订单', action: () => router.push('/orders') },
        messages: [
            { from: 'service', time: '今天 10:12', content: '你这笔订单属于平台验货履约，付款后会先进入待平台出库。' },
            { from: 'user', time: '今天 10:14', content: '为什么不是直接显示已发货？' },
            { from: 'service', time: '今天 10:16', content: '因为当前项目把平台验货单和卖家自出单拆成了两条履约路径，平台仓出库后才会进入物流时间线。' }
        ]
    },
    {
        id: 'svc-002',
        title: '确认收货按钮灰掉',
        type: '物流咨询',
        unread: 0,
        status: '已回复',
        relatedLabel: '订单 #102389',
        shortcut: { label: '帮助中心', action: () => router.push('/help') },
        messages: [
            { from: 'user', time: '昨天 21:03', content: '物流已经在路上了，为什么还不能确认收货？' },
            { from: 'service', time: '昨天 21:09', content: '系统会等 mock 物流推进到“派送中”或“已签收”后再开放确认收货，这是为了让交易链路更完整。' }
        ]
    },
    {
        id: 'svc-003',
        title: '退货退款如何完成',
        type: '售后咨询',
        unread: 1,
        status: '待回访',
        relatedLabel: '售后单 #rf-201',
        shortcut: { label: '查看售后', action: () => router.push('/orders?status=afterSale') },
        messages: [
            { from: 'service', time: '今天 09:20', content: '当前版本没有逆向物流，所以退货退款会在卖家同意后由售后详情页手动完成一次 mock 闭环。' },
            { from: 'user', time: '今天 09:24', content: '那买家还能重新申请吗？' },
            { from: 'service', time: '今天 09:28', content: '如果卖家拒绝，买家可以重新发起新的退款申请。' }
        ]
    }
]);

const activeThreadId = ref(threads.value[0]?.id || '');
const draft = ref('');

const activeThread = computed(() => threads.value.find((item) => item.id === activeThreadId.value) || null);

const selectThread = (id) => {
    activeThreadId.value = id;
    const target = threads.value.find((item) => item.id === id);
    if (target) {
        target.unread = 0;
    }
};

const sendDraft = () => {
    if (!draft.value.trim()) {
        showToast('请输入消息内容');
        return;
    }
    showToast('当前客服会话为 mock，暂不发送真实消息');
    draft.value = '';
};
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#24333f] pb-12">
        <nav class="sticky top-0 z-50 border-b border-gray-100 bg-white/95 backdrop-blur-sm">
            <div class="mx-auto flex h-[76px] max-w-[1320px] items-center justify-between px-8">
                <div class="flex items-center gap-4">
                    <button class="flex h-11 w-11 items-center justify-center rounded-2xl border border-gray-200 text-gray-500 transition-colors hover:border-[#0b8a61] hover:text-[#0b8a61]" @click="router.back()">
                        <ArrowLeft :size="18" />
                    </button>
                    <div>
                        <div class="text-xs font-bold uppercase tracking-[0.18em] text-[#0b8a61]">Support Inbox</div>
                        <div class="mt-1 text-2xl font-bold text-[#24333f]">客服消息</div>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <button class="rounded-full border border-[#0b8a61] px-4 py-1.5 text-sm font-bold text-[#0b8a61] transition-colors hover:bg-[#0b8a61]/5" @click="router.push('/help')">
                        帮助中心
                    </button>
                    <button class="rounded-full bg-[#24333f] px-4 py-1.5 text-sm font-bold text-white transition-colors hover:bg-[#1c2730]" @click="router.push('/service')">
                        返回客服中心
                    </button>
                </div>
            </div>
        </nav>

        <main class="mx-auto max-w-[1320px] space-y-6 px-8 py-8">
            <section class="rounded-[30px] bg-[#24333f] px-8 py-8 text-white shadow-[0_24px_80px_rgba(36,51,63,0.22)]">
                <div class="flex items-start justify-between gap-8">
                    <div>
                        <div class="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-white/70">
                            <Headset :size="14" />
                            Mock Support Threads
                        </div>
                        <h1 class="mt-4 max-w-3xl text-4xl font-bold leading-[1.15]">客服消息页现在不再是空白列表，而是一套可以承接交易与售后排查的 mock 工单面板。</h1>
                        <p class="mt-4 max-w-3xl text-sm leading-7 text-white/70">左侧是客服会话列表，右侧是当前会话内容。消息仍然是 mock，但已经能表达平台验货、物流签收和售后退款这三条核心排查路径。</p>
                    </div>

                    <div class="grid grid-cols-3 gap-4">
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Threads</div>
                            <div class="mt-3 text-3xl font-bold">{{ threads.length }}</div>
                            <div class="mt-1 text-xs text-white/55">当前会话</div>
                        </div>
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Unread</div>
                            <div class="mt-3 text-3xl font-bold">{{ threads.reduce((sum, item) => sum + item.unread, 0) }}</div>
                            <div class="mt-1 text-xs text-white/55">未读消息</div>
                        </div>
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Mode</div>
                            <div class="mt-3 text-3xl font-bold">Desk</div>
                            <div class="mt-1 text-xs text-white/55">桌面布局</div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-[340px_minmax(0,1fr)] gap-6">
                <aside class="rounded-3xl border border-gray-100/70 bg-white p-4 shadow-sm">
                    <div class="flex items-center justify-between px-3 py-2">
                        <div>
                            <h2 class="text-lg font-bold text-[#24333f]">会话列表</h2>
                            <p class="mt-1 text-xs text-gray-400">点击左侧切换不同工单</p>
                        </div>
                        <div class="rounded-full bg-[#f4f7f5] px-3 py-1 text-xs font-bold text-gray-500">{{ threads.length }} 个会话</div>
                    </div>

                    <div class="mt-3 space-y-3">
                        <button
                            v-for="thread in threads"
                            :key="thread.id"
                            :class="[
                                'w-full rounded-3xl border px-4 py-4 text-left transition-all',
                                activeThreadId === thread.id
                                    ? 'border-[#bdd9cb] bg-[#f3faf6] shadow-sm'
                                    : 'border-gray-100 bg-[#fbfcfc] hover:border-[#d3e3da]'
                            ]"
                            @click="selectThread(thread.id)">
                            <div class="flex items-start justify-between gap-4">
                                <div>
                                    <div class="text-sm font-bold text-[#24333f]">{{ thread.title }}</div>
                                    <div class="mt-1 text-xs text-gray-500">{{ thread.type }}</div>
                                </div>
                                <span v-if="thread.unread > 0" class="rounded-full bg-[#ff5e57] px-2 py-0.5 text-[11px] font-bold text-white">
                                    {{ thread.unread }}
                                </span>
                            </div>
                            <div class="mt-3 flex items-center justify-between text-xs text-gray-400">
                                <span>{{ thread.relatedLabel }}</span>
                                <span>{{ thread.status }}</span>
                            </div>
                        </button>
                    </div>
                </aside>

                <div v-if="activeThread" class="overflow-hidden rounded-3xl border border-gray-100/70 bg-white shadow-sm">
                    <div class="flex items-center justify-between border-b border-gray-100 px-6 py-5">
                        <div>
                            <h2 class="text-2xl font-bold text-[#24333f]">{{ activeThread.title }}</h2>
                            <div class="mt-2 flex items-center gap-3 text-xs text-gray-400">
                                <span>{{ activeThread.type }}</span>
                                <span>{{ activeThread.relatedLabel }}</span>
                                <span>{{ activeThread.status }}</span>
                            </div>
                        </div>
                        <button class="rounded-full border border-[#0b8a61]/20 bg-[#edf7f2] px-4 py-2 text-sm font-bold text-[#0b8a61] transition-colors hover:bg-[#e2f3ea]" @click="activeThread.shortcut.action()">
                            {{ activeThread.shortcut.label }}
                        </button>
                    </div>

                    <div class="grid grid-cols-[minmax(0,1fr)_280px] gap-0">
                        <div class="border-r border-gray-100 bg-[#fbfcfc] px-6 py-6">
                            <div class="space-y-4">
                                <div
                                    v-for="(message, index) in activeThread.messages"
                                    :key="`${activeThread.id}-${index}`"
                                    :class="[
                                        'max-w-[82%] rounded-3xl px-5 py-4 text-sm leading-7 shadow-sm',
                                        message.from === 'service'
                                            ? 'bg-white text-gray-600'
                                            : 'ml-auto bg-[#24333f] text-white'
                                    ]">
                                    <div class="flex items-center justify-between gap-4 text-[11px] uppercase tracking-[0.14em]"
                                        :class="message.from === 'service' ? 'text-gray-400' : 'text-white/50'">
                                        <span>{{ message.from === 'service' ? '客服' : '用户' }}</span>
                                        <span>{{ message.time }}</span>
                                    </div>
                                    <div class="mt-2">{{ message.content }}</div>
                                </div>
                            </div>

                            <div class="mt-6 rounded-3xl border border-gray-100 bg-white p-4">
                                <div class="text-xs font-bold uppercase tracking-[0.16em] text-gray-400">Reply Draft</div>
                                <textarea v-model="draft" rows="4" class="mt-3 w-full resize-none rounded-2xl border border-gray-200 bg-[#fbfcfc] px-4 py-3 text-sm leading-7 text-[#24333f] outline-none transition-colors focus:border-[#0b8a61]" placeholder="当前为 mock 客服消息面板，可以体验桌面端排版，但不会发送真实消息"></textarea>
                                <div class="mt-3 flex justify-end">
                                    <button class="inline-flex items-center gap-2 rounded-full bg-[#24333f] px-5 py-2 text-sm font-bold text-white transition-colors hover:bg-[#1c2730]" @click="sendDraft">
                                        <SendHorizonal :size="15" />
                                        发送消息
                                    </button>
                                </div>
                            </div>
                        </div>

                        <aside class="space-y-4 px-5 py-6">
                            <div class="rounded-3xl border border-gray-100 bg-[#f8faf9] p-5">
                                <h3 class="text-sm font-bold uppercase tracking-[0.16em] text-gray-400">会话摘要</h3>
                                <div class="mt-4 space-y-3 text-sm text-gray-500">
                                    <div class="flex items-center justify-between"><span>会话类型</span><span class="font-bold text-[#24333f]">{{ activeThread.type }}</span></div>
                                    <div class="flex items-center justify-between"><span>关联对象</span><span class="font-bold text-[#24333f]">{{ activeThread.relatedLabel }}</span></div>
                                    <div class="flex items-center justify-between"><span>当前状态</span><span class="font-bold text-[#24333f]">{{ activeThread.status }}</span></div>
                                </div>
                            </div>

                            <button class="flex w-full items-center justify-between rounded-3xl border border-gray-100 bg-white px-4 py-4 text-left transition-colors hover:border-[#bdd9cb] hover:bg-[#f3faf6]" @click="router.push('/help')">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><BookOpen :size="16" class="text-[#0b8a61]" /> 查看帮助中心</span>
                                <span class="text-xs text-gray-400">跳转</span>
                            </button>
                            <button class="flex w-full items-center justify-between rounded-3xl border border-gray-100 bg-white px-4 py-4 text-left transition-colors hover:border-[#bdd9cb] hover:bg-[#f3faf6]" @click="router.push('/orders')">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><ClipboardList :size="16" class="text-[#0b8a61]" /> 打开订单页</span>
                                <span class="text-xs text-gray-400">跳转</span>
                            </button>
                            <button class="flex w-full items-center justify-between rounded-3xl border border-gray-100 bg-white px-4 py-4 text-left transition-colors hover:border-[#bdd9cb] hover:bg-[#f3faf6]" @click="router.push('/service')">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><ShieldCheck :size="16" class="text-[#0b8a61]" /> 返回客服中心</span>
                                <span class="text-xs text-gray-400">跳转</span>
                            </button>
                        </aside>
                    </div>
                </div>
            </section>
        </main>
    </div>
</template>
