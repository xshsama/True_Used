<script setup>
import {
    ArrowLeft,
    CircleHelp,
    ClipboardList,
    Headset,
    MessageSquareMore,
    PhoneCall,
    ShieldCheck,
    Sparkles
} from 'lucide-vue-next';
import { showToast } from 'vant';
import { useRouter } from 'vue-router';

const router = useRouter();

const serviceCards = [
    {
        title: '客服消息',
        desc: '进入 mock 客服会话列表，查看交易、验货和售后工单。',
        icon: MessageSquareMore,
        action: () => router.push({ name: 'ServiceMessages' })
    },
    {
        title: '帮助中心',
        desc: '查看交易模式、平台验货、物流签收和售后逻辑说明。',
        icon: CircleHelp,
        action: () => router.push({ name: 'Help' })
    },
    {
        title: '意见反馈',
        desc: '把体验问题、功能建议和 mock 流程缺口直接记录下来。',
        icon: Sparkles,
        action: () => router.push({ name: 'Feedback' })
    }
];

const supportRules = [
    {
        title: '交易路径优先按订单模式处理',
        desc: '卖家自出看卖家发货，平台验货看平台仓出库，不再混用文案。'
    },
    {
        title: '确认收货要受物流节点限制',
        desc: '只有 mock 物流进入派送中或已签收，买家才可以确认收货。'
    },
    {
        title: '售后状态以订单和退款详情同步展示',
        desc: '退款申请、拒绝重提、卖家同意和 mock 完成退款都能在售后详情里看到。'
    }
];

const commonCases = [
    {
        title: '买家付款后迟迟没有发货',
        detail: '先看订单模式。如果是平台验货单，订单会先进入待平台出库；如果是卖家自出，则需要卖家手动录入快递信息。',
        action: () => router.push('/orders?status=toship')
    },
    {
        title: '平台验货报告和订单没有关联',
        detail: '现在已经改成按订单查看验货报告，直接进入订单详情即可，不再从“我的验货记录”单独跳转。',
        action: () => router.push({ name: 'Help' })
    },
    {
        title: '退货退款还没有逆向物流',
        detail: '当前版本保留为 mock 手动完成，卖家在售后详情页处理通过后可以补一次手动完成。',
        action: () => router.push('/orders?status=afterSale')
    }
];

const callService = () => showToast('客服电话：400-000-0000（mock）');
const connectOnline = () => showToast('已为你接入在线客服占位会话');
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
                        <div class="text-xs font-bold uppercase tracking-[0.18em] text-[#0b8a61]">Support</div>
                        <div class="mt-1 text-2xl font-bold text-[#24333f]">客服中心</div>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <button class="rounded-full border border-[#0b8a61] px-4 py-1.5 text-sm font-bold text-[#0b8a61] transition-colors hover:bg-[#0b8a61]/5" @click="router.push('/help')">
                        查看帮助中心
                    </button>
                    <button class="rounded-full bg-[#24333f] px-4 py-1.5 text-sm font-bold text-white transition-colors hover:bg-[#1c2730]" @click="connectOnline">
                        在线客服
                    </button>
                </div>
            </div>
        </nav>

        <main class="mx-auto max-w-[1320px] space-y-6 px-8 py-8">
            <section class="grid grid-cols-[minmax(0,1fr)_340px] gap-6 rounded-[30px] bg-[#24333f] px-8 py-8 text-white shadow-[0_24px_80px_rgba(36,51,63,0.22)]">
                <div>
                    <div class="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-white/70">
                        <Headset :size="14" />
                        Customer Operations
                    </div>
                    <h1 class="mt-4 max-w-3xl text-4xl font-bold leading-[1.15]">这里不只是客服入口，而是把交易、物流、验货和售后的排查路径也一起补齐。</h1>
                    <p class="mt-4 max-w-3xl text-sm leading-7 text-white/70">如果用户卡在“待平台出库”“无法确认收货”或“退货退款怎么完成”等环节，可以先按下面的规则自助判断，再决定是否进入客服消息。</p>

                    <div class="mt-7 grid grid-cols-3 gap-4">
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Response</div>
                            <div class="mt-3 text-3xl font-bold">15m</div>
                            <div class="mt-1 text-xs text-white/55">首响目标</div>
                        </div>
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Cases</div>
                            <div class="mt-3 text-3xl font-bold">3</div>
                            <div class="mt-1 text-xs text-white/55">重点处理路径</div>
                        </div>
                        <div class="rounded-3xl bg-white/8 px-5 py-5">
                            <div class="text-xs uppercase tracking-[0.18em] text-white/45">Mode</div>
                            <div class="mt-3 text-3xl font-bold">Mock</div>
                            <div class="mt-1 text-xs text-white/55">客服会话状态</div>
                        </div>
                    </div>
                </div>

                <div class="space-y-4 rounded-[28px] border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                    <div class="rounded-3xl border border-white/10 bg-white/8 px-5 py-5">
                        <div class="flex items-center gap-3 text-white">
                            <PhoneCall :size="18" class="text-[#86d8b5]" />
                            <div class="text-lg font-bold">电话支持</div>
                        </div>
                        <p class="mt-3 text-sm leading-7 text-white/70">工作时间内可以使用 mock 客服电话流程，当前主要用于说明页面和工单入口联动。</p>
                        <button class="mt-4 rounded-full bg-white px-4 py-2 text-sm font-bold text-[#24333f] transition-colors hover:bg-[#edf7f2]" @click="callService">
                            拨打客服电话
                        </button>
                    </div>

                    <div class="rounded-3xl border border-[#86d8b5]/25 bg-[#edf7f2] px-5 py-5 text-[#24333f]">
                        <div class="flex items-center gap-3 text-[#0b8a61]">
                            <ShieldCheck :size="18" />
                            <div class="text-lg font-bold">推荐先做自助排查</div>
                        </div>
                        <p class="mt-3 text-sm leading-7 text-gray-600">很多问题已经能在订单详情、售后详情或帮助中心里直接看出原因，尤其是平台验货单与卖家自出单的流程差异。</p>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-3 gap-5">
                <button
                    v-for="card in serviceCards"
                    :key="card.title"
                    class="group rounded-3xl border border-gray-100/70 bg-white p-6 text-left shadow-sm transition-all hover:-translate-y-1 hover:border-[#c9ddd3] hover:shadow-[0_18px_40px_rgba(15,23,42,0.08)]"
                    @click="card.action()">
                    <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#edf7f2] text-[#0b8a61] transition-transform group-hover:scale-105">
                        <component :is="card.icon" :size="22" />
                    </div>
                    <h2 class="mt-4 text-lg font-bold text-[#24333f]">{{ card.title }}</h2>
                    <p class="mt-2 text-sm leading-7 text-gray-500">{{ card.desc }}</p>
                </button>
            </section>

            <section class="grid grid-cols-[minmax(0,1fr)_360px] gap-6">
                <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                    <div class="flex items-center justify-between border-b border-gray-100 pb-4">
                        <div>
                            <h2 class="text-2xl font-bold text-[#24333f]">客服处理规则</h2>
                            <p class="mt-1 text-sm text-gray-500">尽量让客服中心说的是项目里真实已经实现的逻辑，而不是泛泛的售前术语。</p>
                        </div>
                        <button class="rounded-full border border-gray-200 px-4 py-2 text-sm font-bold text-gray-500 transition-colors hover:border-[#0b8a61] hover:text-[#0b8a61]" @click="router.push('/help')">
                            去看详细说明
                        </button>
                    </div>

                    <div class="mt-5 grid gap-4">
                        <article
                            v-for="rule in supportRules"
                            :key="rule.title"
                            class="rounded-3xl border border-gray-100 bg-[#fbfcfc] px-5 py-5">
                            <h3 class="text-lg font-bold text-[#24333f]">{{ rule.title }}</h3>
                            <p class="mt-2 text-sm leading-7 text-gray-600">{{ rule.desc }}</p>
                        </article>
                    </div>

                    <div class="mt-8 border-t border-gray-100 pt-5">
                        <h3 class="text-lg font-bold text-[#24333f]">典型场景</h3>
                        <div class="mt-4 space-y-4">
                            <article
                                v-for="item in commonCases"
                                :key="item.title"
                                class="rounded-3xl border border-gray-100 bg-[#f8faf9] px-5 py-5">
                                <div class="flex items-start justify-between gap-6">
                                    <div>
                                        <h4 class="text-base font-bold text-[#24333f]">{{ item.title }}</h4>
                                        <p class="mt-2 text-sm leading-7 text-gray-600">{{ item.detail }}</p>
                                    </div>
                                    <button class="rounded-full border border-[#0b8a61]/20 bg-white px-4 py-2 text-xs font-bold text-[#0b8a61] transition-colors hover:bg-[#edf7f2]" @click="item.action()">
                                        查看入口
                                    </button>
                                </div>
                            </article>
                        </div>
                    </div>
                </div>

                <aside class="space-y-6">
                    <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                        <h2 class="text-lg font-bold text-[#24333f]">客服工具箱</h2>
                        <div class="mt-4 grid gap-3 text-sm">
                            <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-4 text-left transition-colors hover:bg-[#edf7f2]" @click="router.push('/orders')">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><ClipboardList :size="16" class="text-[#0b8a61]" /> 我的订单</span>
                                <span class="text-xs text-gray-400">入口</span>
                            </button>
                            <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-4 text-left transition-colors hover:bg-[#edf7f2]" @click="router.push('/feedback')">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><Sparkles :size="16" class="text-[#0b8a61]" /> 提交反馈</span>
                                <span class="text-xs text-gray-400">入口</span>
                            </button>
                            <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-4 text-left transition-colors hover:bg-[#edf7f2]" @click="router.push({ name: 'ServiceMessages' })">
                                <span class="flex items-center gap-3 font-bold text-[#24333f]"><MessageSquareMore :size="16" class="text-[#0b8a61]" /> 客服会话</span>
                                <span class="text-xs text-gray-400">入口</span>
                            </button>
                        </div>
                    </div>

                    <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                        <h2 class="text-lg font-bold text-[#24333f]">当前说明</h2>
                        <p class="mt-4 text-sm leading-7 text-gray-600">客服消息、电话和在线客服都还是 mock，但页面结构已经能承接交易和售后入口，不再是空白占位。</p>
                    </div>
                </aside>
            </section>
        </main>
    </div>
</template>
