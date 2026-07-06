<script setup>
import { listProducts } from '@/api/products';
import { normalizeProductTrade } from '@/utils/productTrade';
import {
    ArrowRight,
    BadgePercent,
    Clock3,
    Eye,
    Flame,
    ShieldCheck,
    Sparkles,
    TrendingDown
} from 'lucide-vue-next';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const loading = ref(false);
const activeTab = ref('综合热度');
const rankingItems = ref([]);

const rankingModes = [
    {
        label: '综合热度',
        sort: 'views_desc',
        eyebrow: 'Momentum Board',
        title: '把热度、关注与浏览堆到一张桌面榜单里',
        description: '更适合快速挑出当下讨论度最高、成交意愿最强的商品。'
    },
    {
        label: '降价幅度',
        sort: 'price_asc',
        eyebrow: 'Price Drop Watch',
        title: '先看谁在让价，再决定值不值得聊',
        description: '当前后端没有专门的降价排序，这里用低价优先做近似，前端会强化降价感知。'
    },
    {
        label: '最新发布',
        sort: 'created_desc',
        eyebrow: 'Fresh Listings',
        title: '把最新上架的商品先拉进视野',
        description: '适合守新品、抢首发意向单，也更接近卖家一手发布节奏。'
    }
];

const activeMode = computed(() => {
    return rankingModes.find(mode => mode.label === activeTab.value) || rankingModes[0];
});

const topItems = computed(() => rankingItems.value.slice(0, 3));
const restItems = computed(() => rankingItems.value.slice(3));

const bestDropItem = computed(() => {
    if (!rankingItems.value.length) return null;
    return rankingItems.value.reduce((best, current) => {
        if (!best) return current;
        return current.drop > best.drop ? current : best;
    }, null);
});

const summaryCards = computed(() => {
    const totalHeat = rankingItems.value.reduce((sum, item) => sum + item.heat, 0);
    const officialCount = rankingItems.value.filter(item => item.isOfficialTrade).length;
    const bestDrop = bestDropItem.value;

    return [
        {
            label: '上榜商品',
            value: `${rankingItems.value.length}`,
            subline: '当前榜单可直接比较的候选'
        },
        {
            label: '平台验货',
            value: `${officialCount}`,
            subline: '带平台背书的商品数量'
        },
        {
            label: '累计热度',
            value: totalHeat.toLocaleString(),
            subline: '关注与浏览的桌面化聚合值'
        },
        {
            label: '最大立省',
            value: bestDrop ? `¥${bestDrop.drop}` : '¥0',
            subline: bestDrop ? bestDrop.title : '暂无明显价差'
        }
    ];
});

const boardInsights = computed(() => {
    const champion = topItems.value[0];
    const dropLeader = bestDropItem.value;

    return [
        champion
            ? `${champion.title} 当前位居榜首，${activeTab.value === '最新发布' ? `发布时间为 ${champion.time}` : `热度 ${champion.heat}`}`
            : '榜首商品加载完成后会显示核心观察。',
        dropLeader && dropLeader.drop > 0
            ? `当前最大让利来自 ${dropLeader.title}，预计可比原价直降 ¥${dropLeader.drop}`
            : '当前榜单没有明显的价格回落，建议优先比较成色与验货模式。',
        rankingItems.value.filter(item => item.isOfficialTrade).length > 0
            ? '榜单里同时混合了平台验货和卖家自出两种模式，适合对比信任成本。'
            : '目前榜单以卖家自出为主，适合先建立价格感知再筛验货需求。'
    ];
});

function formatTime(time) {
    if (!time) return '刚刚上架';

    const date = new Date(time);
    const diff = Date.now() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);

    if (minutes < 1) return '刚刚上架';
    if (minutes < 60) return `${minutes} 分钟前`;
    if (hours < 24) return `${hours} 小时前`;
    return date.toLocaleDateString();
}

function mapRankingItem(product, index) {
    const price = Number(product.price || 0);
    const originalPrice = Number(product.originalPrice || Math.round(price * 1.12) || 0);
    const drop = Math.max(0, Math.round(originalPrice - price));
    const heat = product.wantCount || product.favoritesCount || product.viewsCount || 0;
    const trade = normalizeProductTrade(product);

    return {
        id: product.id,
        rank: index + 1,
        title: product.title || '未命名商品',
        price,
        originalPrice,
        drop,
        heat,
        image: product.image || (product.images && product.images[0] ? (typeof product.images[0] === 'string' ? product.images[0] : product.images[0].url) : 'https://via.placeholder.com/640x640?text=TrueUsed'),
        seller: product.seller ? (product.seller.nickname || product.seller.username || '未知卖家') : '未知卖家',
        sellerCity: product.locationText || product.seller?.city || '全国',
        views: product.viewsCount || 0,
        time: formatTime(product.createdAt),
        tradeMode: trade.tradeMode,
        tradeModeLabel: trade.tradeModeLabel,
        sellerClaimConditionLabel: trade.sellerClaimConditionLabel,
        platformInspectionGradeLabel: trade.platformInspectionGradeLabel,
        conditionLabel: trade.primaryConditionLabel,
        secondaryConditionLabel: trade.secondaryConditionLabel,
        inspectionGrade: trade.platformInspectionGradeCode,
        isOfficialTrade: trade.hasPlatformInspection,
        tradeLabel: trade.tradeModeLabel,
        savingsRate: originalPrice > 0 ? Math.round((drop / originalPrice) * 100) : 0
    };
}

async function loadData() {
    loading.value = true;

    try {
        const res = await listProducts({
            page: 0,
            size: 20,
            sort: activeMode.value.sort,
            status: 'ON_SALE'
        });

        rankingItems.value = (res.content || []).map((product, index) => mapRankingItem(product, index));
    } catch (error) {
        console.error('Failed to load ranking', error);
        rankingItems.value = [];
    } finally {
        loading.value = false;
    }
}

watch(activeTab, () => {
    loadData();
});

onMounted(() => {
    loadData();
});
</script>

<template>
    <div class="min-h-screen bg-transparent pb-10">
        <div class="mx-auto max-w-[1480px] px-8 py-6">
            <section
                class="relative overflow-hidden rounded-[32px] border border-white/60 bg-gradient-to-br from-[#101f1d] via-[#16342f] to-[#255447] px-10 py-10 text-white shadow-[0_28px_80px_rgba(10,20,20,0.24)]">
                <div class="absolute inset-y-0 right-0 w-[440px] bg-[radial-gradient(circle_at_center,rgba(255,255,255,0.12),transparent_58%)]"></div>

                <div class="relative z-10 grid grid-cols-[minmax(0,1.35fr)_360px] gap-8">
                    <div>
                        <div class="mb-4 inline-flex items-center gap-2 rounded-full border border-emerald-400/20 bg-emerald-400/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-emerald-200">
                            <Flame :size="14" />
                            {{ activeMode.eyebrow }}
                        </div>

                        <h1 class="max-w-3xl text-5xl font-black leading-[1.02] tracking-tight">
                            每日捡漏榜
                        </h1>
                        <p class="mt-4 max-w-2xl text-base leading-7 text-emerald-50/78">
                            {{ activeMode.title }}
                            <span class="block text-sm text-emerald-100/60">
                                {{ activeMode.description }}
                            </span>
                        </p>

                        <div class="mt-8 flex gap-3">
                            <button v-for="mode in rankingModes" :key="mode.label" type="button"
                                class="rounded-full border px-5 py-3 text-sm font-semibold transition-all"
                                :class="activeTab === mode.label
                                    ? 'border-white bg-white text-slate-900 shadow-lg'
                                    : 'border-white/10 bg-white/8 text-white hover:border-white/30 hover:bg-white/14'"
                                @click="activeTab = mode.label">
                                {{ mode.label }}
                            </button>
                        </div>
                    </div>

                    <div class="space-y-4">
                        <div class="rounded-[28px] border border-white/10 bg-white/10 p-5 backdrop-blur-md">
                            <div class="text-xs uppercase tracking-[0.18em] text-emerald-100/60">桌面观察位</div>
                            <div class="mt-3 text-3xl font-black">
                                {{ topItems[0]?.title || '等待榜单加载' }}
                            </div>
                            <div class="mt-2 text-sm text-emerald-50/70">
                                {{ topItems[0] ? `${topItems[0].tradeLabel} · ${topItems[0].conditionLabel}` : '加载完成后会显示当前榜首的交易模式与成色标签。' }}
                            </div>
                        </div>

                        <div class="grid grid-cols-2 gap-4">
                            <div class="rounded-3xl border border-white/10 bg-black/10 p-4">
                                <div class="text-xs uppercase tracking-[0.18em] text-emerald-100/60">榜单模式</div>
                                <div class="mt-2 text-lg font-bold">{{ activeTab }}</div>
                            </div>
                            <div class="rounded-3xl border border-white/10 bg-black/10 p-4">
                                <div class="text-xs uppercase tracking-[0.18em] text-emerald-100/60">交易结构</div>
                                <div class="mt-2 text-lg font-bold">双模式混排</div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mt-6 grid grid-cols-4 gap-5">
                <div v-for="card in summaryCards" :key="card.label"
                    class="rounded-[28px] border border-white/70 bg-white/88 p-5 shadow-[0_18px_38px_rgba(15,23,42,0.05)] backdrop-blur">
                    <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">{{ card.label }}</div>
                    <div class="mt-3 text-3xl font-black tracking-tight text-slate-950">{{ card.value }}</div>
                    <div class="mt-2 text-sm text-slate-500">{{ card.subline }}</div>
                </div>
            </section>

            <section class="mt-6 grid grid-cols-[minmax(0,1fr)_320px] gap-6">
                <div class="space-y-6">
                    <div
                        class="rounded-[32px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                        <div class="mb-5 flex items-center justify-between">
                            <div>
                                <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">Top Picks</div>
                                <h2 class="mt-2 text-2xl font-black text-slate-950">桌面榜单 Podium</h2>
                            </div>
                            <div class="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                                按 {{ activeTab }} 排序
                            </div>
                        </div>

                        <div v-if="loading && !rankingItems.length"
                            class="flex min-h-[420px] items-center justify-center rounded-[28px] bg-slate-50">
                            <van-loading vertical color="#0b8a61">榜单加载中...</van-loading>
                        </div>

                        <div v-else-if="topItems.length" class="grid grid-cols-[minmax(0,1.5fr)_360px] gap-5">
                            <button type="button"
                                class="group relative overflow-hidden rounded-[30px] border border-slate-200 bg-slate-950 text-left text-white shadow-[0_20px_50px_rgba(15,23,42,0.18)]"
                                @click="router.push(`/product/${topItems[0].id}`)">
                                <img :src="topItems[0].image"
                                    class="absolute inset-0 h-full w-full object-cover opacity-45 transition-transform duration-500 group-hover:scale-105" />
                                <div class="absolute inset-0 bg-gradient-to-t from-black via-black/75 to-black/20"></div>
                                <div class="relative z-10 flex min-h-[430px] flex-col justify-between p-7">
                                    <div class="flex items-start justify-between">
                                        <div class="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-400 text-slate-900 shadow-lg">
                                            <Flame :size="24" />
                                        </div>
                                        <div
                                            class="rounded-full border border-white/10 bg-white/12 px-3 py-1 text-xs font-semibold text-white/90">
                                            榜首推荐
                                        </div>
                                    </div>

                                    <div>
                                        <div class="mb-3 flex flex-wrap gap-2">
                                            <span class="rounded-full bg-white/12 px-3 py-1 text-xs font-semibold">{{ topItems[0].tradeLabel }}</span>
                                            <span class="rounded-full bg-white/12 px-3 py-1 text-xs font-semibold">{{ topItems[0].conditionLabel }}</span>
                                            <span v-if="topItems[0].secondaryConditionLabel"
                                                class="rounded-full bg-white/12 px-3 py-1 text-xs font-semibold">
                                                {{ topItems[0].secondaryConditionLabel }}
                                            </span>
                                            <span v-if="topItems[0].drop > 0"
                                                class="rounded-full bg-red-500/85 px-3 py-1 text-xs font-semibold">直降 ¥{{ topItems[0].drop }}</span>
                                        </div>
                                        <h3 class="max-w-xl text-3xl font-black leading-tight">{{ topItems[0].title }}</h3>
                                        <div class="mt-4 flex items-end gap-3">
                                            <div class="text-4xl font-black text-rose-300">¥{{ topItems[0].price }}</div>
                                            <div class="pb-1 text-sm text-white/55 line-through">¥{{ topItems[0].originalPrice }}</div>
                                        </div>
                                        <div class="mt-5 flex items-center gap-5 text-sm text-white/70">
                                            <span class="inline-flex items-center gap-1.5">
                                                <Eye :size="15" />
                                                {{ topItems[0].views }} 浏览
                                            </span>
                                            <span class="inline-flex items-center gap-1.5">
                                                <Clock3 :size="15" />
                                                {{ topItems[0].time }}
                                            </span>
                                            <span>{{ topItems[0].seller }}</span>
                                        </div>
                                    </div>
                                </div>
                            </button>

                            <div class="grid gap-5">
                                <button v-for="item in topItems.slice(1)" :key="item.id" type="button"
                                    class="group rounded-[28px] border border-slate-200 bg-white p-5 text-left shadow-sm transition-all hover:-translate-y-1 hover:border-emerald-200 hover:shadow-[0_18px_38px_rgba(15,23,42,0.08)]"
                                    @click="router.push(`/product/${item.id}`)">
                                    <div class="flex gap-4">
                                        <div class="relative h-32 w-32 overflow-hidden rounded-2xl bg-slate-100">
                                            <img :src="item.image"
                                                class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
                                            <div
                                                class="absolute left-3 top-3 inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-950 text-sm font-black text-white">
                                                {{ item.rank }}
                                            </div>
                                        </div>

                                        <div class="flex min-w-0 flex-1 flex-col">
                                            <div class="flex items-start justify-between gap-3">
                                                <h3 class="line-clamp-2 text-lg font-bold leading-7 text-slate-900">{{ item.title }}</h3>
                                                <span
                                                    class="rounded-full px-3 py-1 text-xs font-semibold"
                                                    :class="item.isOfficialTrade ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                                                    {{ item.tradeLabel }}
                                                </span>
                                            </div>

                                            <div class="mt-3 flex flex-wrap gap-2 text-xs text-slate-500">
                                                <span class="rounded-full bg-slate-100 px-3 py-1">{{ item.conditionLabel }}</span>
                                                <span v-if="item.secondaryConditionLabel" class="rounded-full bg-slate-100 px-3 py-1">
                                                    {{ item.secondaryConditionLabel }}
                                                </span>
                                                <span v-if="item.savingsRate > 0" class="rounded-full bg-rose-50 px-3 py-1 text-rose-600">
                                                    节省 {{ item.savingsRate }}%
                                                </span>
                                            </div>

                                            <div class="mt-auto flex items-end justify-between pt-5">
                                                <div>
                                                    <div class="text-2xl font-black text-slate-950">¥{{ item.price }}</div>
                                                    <div class="text-sm text-slate-400 line-through">¥{{ item.originalPrice }}</div>
                                                </div>
                                                <div class="text-right text-xs text-slate-500">
                                                    <div>{{ item.heat }} 热度</div>
                                                    <div class="mt-1">{{ item.time }}</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </button>
                            </div>
                        </div>

                        <div v-else class="flex min-h-[260px] items-center justify-center rounded-[28px] bg-slate-50 text-slate-400">
                            暂无上榜商品
                        </div>
                    </div>

                    <div
                        class="rounded-[32px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                        <div class="mb-5 flex items-center justify-between">
                            <div>
                                <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">Full List</div>
                                <h2 class="mt-2 text-2xl font-black text-slate-950">完整榜单</h2>
                            </div>
                            <div class="text-sm text-slate-400">桌面视图按行快速对比价格、模式与热度</div>
                        </div>

                        <div v-if="restItems.length" class="space-y-3">
                            <button v-for="item in restItems" :key="item.id" type="button"
                                class="group grid w-full grid-cols-[64px_minmax(0,1.3fr)_160px_140px_130px_120px] items-center gap-4 rounded-[24px] border border-slate-200 bg-slate-50/70 px-5 py-4 text-left transition-all hover:border-emerald-200 hover:bg-white hover:shadow-sm"
                                @click="router.push(`/product/${item.id}`)">
                                <div
                                    class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-lg font-black text-slate-400 shadow-sm">
                                    {{ item.rank }}
                                </div>

                                <div class="flex items-center gap-4">
                                    <div class="h-20 w-20 overflow-hidden rounded-2xl bg-slate-200">
                                        <img :src="item.image"
                                            class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />
                                    </div>
                                    <div class="min-w-0">
                                        <div class="line-clamp-1 text-base font-bold text-slate-900">{{ item.title }}</div>
                                        <div class="mt-2 flex flex-wrap gap-2 text-xs text-slate-500">
                                            <span class="rounded-full bg-white px-3 py-1">{{ item.conditionLabel }}</span>
                                            <span v-if="item.secondaryConditionLabel" class="rounded-full bg-white px-3 py-1">
                                                {{ item.secondaryConditionLabel }}
                                            </span>
                                            <span class="rounded-full bg-white px-3 py-1">{{ item.seller }}</span>
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <div
                                        class="inline-flex rounded-full px-3 py-1 text-xs font-semibold"
                                        :class="item.isOfficialTrade ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'">
                                        {{ item.tradeLabel }}
                                    </div>
                                    <div class="mt-2 text-xs text-slate-400">{{ item.sellerCity }}</div>
                                </div>

                                <div>
                                    <div class="text-xl font-black text-slate-950">¥{{ item.price }}</div>
                                    <div class="text-xs text-slate-400 line-through">¥{{ item.originalPrice }}</div>
                                </div>

                                <div class="space-y-1 text-sm text-slate-500">
                                    <div class="inline-flex items-center gap-1.5">
                                        <Eye :size="14" />
                                        {{ item.views }}
                                    </div>
                                    <div class="font-semibold text-slate-900">{{ item.heat }} 热度</div>
                                </div>

                                <div class="flex items-center justify-end gap-2 text-sm font-semibold text-emerald-700">
                                    去看看
                                    <ArrowRight :size="16" class="transition-transform group-hover:translate-x-1" />
                                </div>
                            </button>
                        </div>

                        <div v-else-if="!loading"
                            class="flex min-h-[180px] items-center justify-center rounded-[28px] bg-slate-50 text-slate-400">
                            当前榜单只有头部商品，稍后再来查看更多
                        </div>
                    </div>
                </div>

                <aside class="space-y-6">
                    <div
                        class="sticky top-28 rounded-[30px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                        <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                            <Sparkles :size="14" />
                            Board Insight
                        </div>
                        <h3 class="mt-3 text-2xl font-black text-slate-950">榜单观察</h3>

                        <div class="mt-5 space-y-3">
                            <div v-for="item in boardInsights" :key="item"
                                class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm leading-6 text-slate-600">
                                {{ item }}
                            </div>
                        </div>

                        <div class="mt-6 rounded-[24px] bg-slate-950 p-5 text-white">
                            <div class="flex items-center gap-2 text-xs uppercase tracking-[0.18em] text-white/55">
                                <ShieldCheck :size="14" />
                                Trust Split
                            </div>
                            <div class="mt-3 text-lg font-bold">
                                卖家自出与平台验货并行展示
                            </div>
                            <div class="mt-2 text-sm leading-6 text-white/68">
                                榜单页不再把两种模式混成一个“成色标签”，用户可以先比价，再判断是否需要平台验货背书。
                            </div>
                        </div>

                        <div class="mt-6 space-y-3">
                            <div class="flex items-center gap-3 rounded-2xl border border-slate-200 px-4 py-3">
                                <BadgePercent :size="16" class="text-rose-500" />
                                <div>
                                    <div class="text-sm font-semibold text-slate-900">优先看立省额</div>
                                    <div class="text-xs text-slate-500">适合先筛出明显低于心理价位的商品</div>
                                </div>
                            </div>
                            <div class="flex items-center gap-3 rounded-2xl border border-slate-200 px-4 py-3">
                                <TrendingDown :size="16" class="text-emerald-600" />
                                <div>
                                    <div class="text-sm font-semibold text-slate-900">再看交易模式</div>
                                    <div class="text-xs text-slate-500">平台验货更适合高客单价，卖家自出更适合抢成交速度</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </aside>
            </section>
        </div>
    </div>
</template>
