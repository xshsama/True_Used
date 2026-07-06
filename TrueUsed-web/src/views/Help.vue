<script setup>
import SearchBar from '@/components/SearchBar.vue';
import {
    BookOpen,
    CircleHelp,
    FileCheck2,
    RefreshCcw,
    ShieldCheck,
    Truck,
    Wallet
} from 'lucide-vue-next';
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const keyword = ref('');
const activeCategory = ref('all');
const expandedIds = ref(['trade-flow']);

const categories = [
    { id: 'all', label: '全部' },
    { id: 'trade', label: '交易流程' },
    { id: 'inspection', label: '验货履约' },
    { id: 'shipping', label: '物流签收' },
    { id: 'refund', label: '售后退款' },
    { id: 'account', label: '账号与支付' }
];

const articles = [
    {
        id: 'trade-flow',
        category: 'trade',
        title: '平台现在有哪两种成交模式？',
        answer: '当前分为“卖家自出”和“平台验货”两种模式。卖家自出是买家付款后等待卖家录入快递信息；平台验货是买家付款后进入待平台出库，再由平台仓生成一条 mock 物流并继续推进运输节点。'
    },
    {
        id: 'inspection-report',
        category: 'inspection',
        title: '平台验货单的验货报告在哪里看？',
        answer: '平台验货订单的验货报告已经和订单绑定。你可以在订单详情页直接点击“查看验货报告”，不需要再单独去个人验货记录里翻。'
    },
    {
        id: 'inspection-diff',
        category: 'inspection',
        title: '平台验货和卖家自出的区别是什么？',
        answer: '核心区别在履约责任。卖家自出由卖家发货，平台验货由平台仓统一出库；订单状态、物流文案和后续售后路径也会随模式变化。'
    },
    {
        id: 'shipping-progress',
        category: 'shipping',
        title: '为什么物流看起来像模拟节点？',
        answer: '因为当前项目采用 mock 物流。系统会在发货后按时间生成“待揽收、已揽收、运输中、派送中、已签收”等节点，用来验证前端和订单状态的闭环。'
    },
    {
        id: 'confirm-receipt',
        category: 'shipping',
        title: '为什么有时候不能立刻确认收货？',
        answer: '现在确认收货已经和物流节点绑定。只有物流推进到“派送中”或“已签收”时，买家才可以确认收货，这样流程更像真实电商链路。'
    },
    {
        id: 'refund-start',
        category: 'refund',
        title: '如何发起售后退款？',
        answer: '在订单详情或订单列表中点击“申请退款/申请售后”即可。提交后订单会变成“售后中”，卖家可以在售后详情页做同意或拒绝处理。'
    },
    {
        id: 'refund-return',
        category: 'refund',
        title: '退货退款为什么还是手动完成？',
        answer: '因为你当前明确要求先不做逆向物流，所以“退货退款”保留为 mock 手动闭环：卖家同意后，可以在售后详情页手动完成一次模拟回收确认。'
    },
    {
        id: 'refund-reapply',
        category: 'refund',
        title: '退款被拒绝后还能重新申请吗？',
        answer: '可以。现在如果卖家拒绝，订单会恢复到原先可继续交易的状态，买家也可以重新发起一次新的售后申请。'
    },
    {
        id: 'payment-method',
        category: 'account',
        title: '支持哪些支付方式？',
        answer: '当前项目里主要有普通支付和钱包支付两种路径。它们最终都会写入订单支付时间，并根据商品模式流转到不同的发货节点。'
    },
    {
        id: 'contact-support',
        category: 'account',
        title: '如果流程卡住了怎么办？',
        answer: '先进入客服中心查看常见问题，再看客服消息页里的工单记录。如果仍无法判断，可以从订单详情进入客服中心，按订单和售后场景继续排查。'
    }
];

const featuredCards = [
    {
        title: '交易模式说明',
        desc: '快速理解卖家自出与平台验货的差别',
        icon: ShieldCheck,
        action: () => {
            activeCategory.value = 'trade';
            expandedIds.value = ['trade-flow'];
        }
    },
    {
        title: '物流确认规则',
        desc: '确认收货为什么要等到派送阶段',
        icon: Truck,
        action: () => {
            activeCategory.value = 'shipping';
            expandedIds.value = ['confirm-receipt'];
        }
    },
    {
        title: '售后退款说明',
        desc: '查看退款申请、拒绝重提和 mock 完成逻辑',
        icon: RefreshCcw,
        action: () => {
            activeCategory.value = 'refund';
            expandedIds.value = ['refund-start'];
        }
    }
];

const filteredArticles = computed(() => {
    const query = keyword.value.trim().toLowerCase();
    return articles.filter((item) => {
        const matchCategory = activeCategory.value === 'all' || item.category === activeCategory.value;
        const matchKeyword = !query
            || item.title.toLowerCase().includes(query)
            || item.answer.toLowerCase().includes(query);
        return matchCategory && matchKeyword;
    });
});

const stats = computed(() => ({
    total: articles.length,
    visible: filteredArticles.value.length,
    refund: articles.filter((item) => item.category === 'refund').length
}));

const quickLinks = [
    { label: '我的订单', desc: '回到买家订单页查看当前进度', action: () => router.push('/orders') },
    { label: '客服中心', desc: '进入客服中心看常见处理入口', action: () => router.push('/service') },
    { label: '意见反馈', desc: '补充真实使用中的问题和建议', action: () => router.push('/feedback') }
];

const toggleArticle = (id) => {
    if (expandedIds.value.includes(id)) {
        expandedIds.value = expandedIds.value.filter((item) => item !== id);
        return;
    }
    expandedIds.value = [...expandedIds.value, id];
};

const isExpanded = (id) => expandedIds.value.includes(id);
</script>

<template>
    <div class="mx-auto max-w-[1320px] space-y-6 px-8 py-6">
        <section class="grid grid-cols-[minmax(0,1fr)_320px] gap-6 rounded-[30px] border border-white/60 bg-[linear-gradient(135deg,#f4faf7_0%,#ffffff_42%,#f6f8fb_100%)] p-8 shadow-[0_24px_60px_rgba(15,23,42,0.06)]">
            <div>
                <div class="inline-flex items-center gap-2 rounded-full bg-[#e8f5ee] px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-[#0b8a61]">
                    <CircleHelp :size="14" />
                    Help Center
                </div>
                <h1 class="mt-4 max-w-3xl text-4xl font-bold leading-[1.15] text-[#1f2f3a]">帮助中心现在不再只是 FAQ 占位，而是把交易、验货、物流和售后规则都串起来。</h1>
                <p class="mt-4 max-w-3xl text-sm leading-7 text-gray-500">这里优先解释你这个项目里已经落地的前后端流程，包括平台验货订单怎么出库、为什么确认收货受物流节点限制、退款被拒绝后如何重新发起。</p>

                <div class="mt-6 max-w-[760px]">
                    <SearchBar v-model="keyword" placeholder="搜索问题，例如 平台验货、确认收货、退款被拒绝" :show-shortcut="false" :show-submit="false" />
                </div>

                <div class="mt-6 flex flex-wrap gap-2">
                    <button
                        v-for="category in categories"
                        :key="category.id"
                        :class="[
                            'rounded-full px-4 py-2 text-sm font-bold transition-colors',
                            activeCategory === category.id
                                ? 'bg-[#24333f] text-white shadow-sm'
                                : 'bg-white text-gray-500 hover:bg-[#eef5f1] hover:text-[#0b8a61]'
                        ]"
                        @click="activeCategory = category.id">
                        {{ category.label }}
                    </button>
                </div>
            </div>

            <div class="grid gap-4">
                <div class="rounded-3xl bg-[#24333f] p-5 text-white shadow-[0_20px_50px_rgba(36,51,63,0.18)]">
                    <p class="text-xs uppercase tracking-[0.18em] text-white/55">Knowledge Base</p>
                    <div class="mt-4 grid grid-cols-3 gap-3">
                        <div class="rounded-2xl bg-white/8 px-4 py-4">
                            <div class="text-2xl font-bold">{{ stats.total }}</div>
                            <div class="mt-1 text-xs text-white/60">总问题</div>
                        </div>
                        <div class="rounded-2xl bg-white/8 px-4 py-4">
                            <div class="text-2xl font-bold">{{ stats.visible }}</div>
                            <div class="mt-1 text-xs text-white/60">当前结果</div>
                        </div>
                        <div class="rounded-2xl bg-white/8 px-4 py-4">
                            <div class="text-2xl font-bold">{{ stats.refund }}</div>
                            <div class="mt-1 text-xs text-white/60">售后专题</div>
                        </div>
                    </div>
                </div>

                <div class="rounded-3xl border border-[#e3ebe7] bg-white p-5">
                    <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Quick Links</p>
                    <div class="mt-4 space-y-3">
                        <button
                            v-for="item in quickLinks"
                            :key="item.label"
                            class="flex w-full items-start justify-between rounded-2xl border border-gray-100 bg-[#f8faf9] px-4 py-4 text-left transition-colors hover:border-[#b9d8ca] hover:bg-[#f3faf6]"
                            @click="item.action()">
                            <div>
                                <div class="text-sm font-bold text-[#24333f]">{{ item.label }}</div>
                                <div class="mt-1 text-xs leading-6 text-gray-500">{{ item.desc }}</div>
                            </div>
                            <BookOpen :size="16" class="mt-1 text-[#0b8a61]" />
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <section class="grid grid-cols-3 gap-5">
            <button
                v-for="card in featuredCards"
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

        <section class="grid grid-cols-[minmax(0,1fr)_320px] gap-6">
            <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                <div class="flex items-center justify-between border-b border-gray-100 pb-4">
                    <div>
                        <h2 class="text-2xl font-bold text-[#24333f]">知识库条目</h2>
                        <p class="mt-1 text-sm text-gray-500">围绕当前项目已经实现的流程进行解释，不再写泛泛的电商文案。</p>
                    </div>
                    <div class="rounded-full bg-[#f4f7f5] px-4 py-2 text-xs font-bold text-gray-500">
                        {{ filteredArticles.length }} 条结果
                    </div>
                </div>

                <div v-if="filteredArticles.length > 0" class="mt-5 space-y-4">
                    <article
                        v-for="article in filteredArticles"
                        :key="article.id"
                        class="rounded-3xl border border-gray-100 bg-[#fbfcfc] px-5 py-5 transition-colors hover:border-[#d0e1d7]">
                        <button class="flex w-full items-start justify-between gap-6 text-left" @click="toggleArticle(article.id)">
                            <div>
                                <div class="text-xs font-bold uppercase tracking-[0.16em] text-[#0b8a61]">{{ categories.find((item) => item.id === article.category)?.label }}</div>
                                <h3 class="mt-2 text-lg font-bold leading-8 text-[#24333f]">{{ article.title }}</h3>
                            </div>
                            <div class="rounded-full border border-gray-200 px-3 py-1 text-xs font-bold text-gray-400">
                                {{ isExpanded(article.id) ? '收起' : '展开' }}
                            </div>
                        </button>

                        <transition name="faq-fade">
                            <p v-if="isExpanded(article.id)" class="mt-4 border-t border-gray-100 pt-4 text-sm leading-8 text-gray-600">
                                {{ article.answer }}
                            </p>
                        </transition>
                    </article>
                </div>

                <div v-else class="mt-8 rounded-3xl border border-dashed border-gray-200 bg-[#f8faf9] px-6 py-12 text-center">
                    <div class="text-lg font-bold text-[#24333f]">没有找到匹配的问题</div>
                    <p class="mt-2 text-sm text-gray-500">可以试试搜索“平台验货”“物流签收”“退款被拒绝”这类关键词。</p>
                </div>
            </div>

            <aside class="space-y-6">
                <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                    <h2 class="text-lg font-bold text-[#24333f]">推荐先看</h2>
                    <div class="mt-4 space-y-3">
                        <div class="rounded-2xl bg-[#f4faf7] px-4 py-4">
                            <div class="flex items-center gap-3 text-[#0b8a61]">
                                <ShieldCheck :size="16" />
                                <span class="text-sm font-bold">平台验货订单</span>
                            </div>
                            <p class="mt-2 text-xs leading-6 text-gray-500">付款后先进入待平台出库，再自动生成 mock 物流。</p>
                        </div>
                        <div class="rounded-2xl bg-[#fff8ef] px-4 py-4">
                            <div class="flex items-center gap-3 text-[#c07619]">
                                <RefreshCcw :size="16" />
                                <span class="text-sm font-bold">退货退款说明</span>
                            </div>
                            <p class="mt-2 text-xs leading-6 text-gray-500">当前版本不做逆向物流，由卖家在售后页手动完成 mock 闭环。</p>
                        </div>
                    </div>
                </div>

                <div class="rounded-3xl border border-gray-100/70 bg-white p-6 shadow-sm">
                    <h2 class="text-lg font-bold text-[#24333f]">常见主题</h2>
                    <div class="mt-4 grid gap-3 text-sm">
                        <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-3 text-left text-gray-600 transition-colors hover:bg-[#eef5f1]" @click="activeCategory = 'inspection'">
                            <span class="flex items-center gap-2"><FileCheck2 :size="15" class="text-[#0b8a61]" /> 验货报告怎么看</span>
                            <span class="text-xs text-gray-400">专题</span>
                        </button>
                        <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-3 text-left text-gray-600 transition-colors hover:bg-[#eef5f1]" @click="activeCategory = 'shipping'">
                            <span class="flex items-center gap-2"><Truck :size="15" class="text-[#0b8a61]" /> 为什么不能马上收货</span>
                            <span class="text-xs text-gray-400">专题</span>
                        </button>
                        <button class="flex items-center justify-between rounded-2xl bg-[#f7f9fa] px-4 py-3 text-left text-gray-600 transition-colors hover:bg-[#eef5f1]" @click="activeCategory = 'account'">
                            <span class="flex items-center gap-2"><Wallet :size="15" class="text-[#0b8a61]" /> 支付方式与钱包</span>
                            <span class="text-xs text-gray-400">专题</span>
                        </button>
                    </div>
                </div>
            </aside>
        </section>
    </div>
</template>

<style scoped>
.faq-fade-enter-active,
.faq-fade-leave-active {
    transition: all 0.22s ease;
}

.faq-fade-enter-from,
.faq-fade-leave-to {
    opacity: 0;
    transform: translateY(-6px);
}
</style>
