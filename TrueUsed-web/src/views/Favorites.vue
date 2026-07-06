<script setup>
import { listMyFavorites, removeFavorite } from '@/api/favorites';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import {
    CheckCheck,
    Eye,
    Heart,
    LayoutGrid,
    ShieldCheck,
    Sparkles,
    Trash2,
    TriangleAlert
} from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const activeTab = ref('全部');
const isEditMode = ref(false);
const favorites = ref([]);
const selectedIds = ref([]);
const loading = ref(false);
const actionLoading = ref(false);

const tabOptions = ['全部', '降价', '失效'];

const totalCount = computed(() => favorites.value.length);
const priceDropCount = computed(() => favorites.value.filter(item => item.status === 'price_drop').length);
const invalidCount = computed(() => favorites.value.filter(item => item.status === 'invalid').length);
const officialCount = computed(() => favorites.value.filter(item => item.isOfficialTrade).length);
const totalSaved = computed(() => favorites.value.reduce((sum, item) => sum + item.priceDrop, 0));

const filteredItems = computed(() => {
    if (activeTab.value === '降价') {
        return favorites.value.filter(item => item.status === 'price_drop');
    }

    if (activeTab.value === '失效') {
        return favorites.value.filter(item => item.status === 'invalid');
    }

    return favorites.value;
});

const visibleSelectedCount = computed(() => {
    return filteredItems.value.filter(item => selectedIds.value.includes(item.id)).length;
});

const selectedVisibleIds = computed(() => {
    return filteredItems.value.filter(item => selectedIds.value.includes(item.id)).map(item => item.id);
});

const allVisibleSelected = computed(() => {
    return filteredItems.value.length > 0 && visibleSelectedCount.value === filteredItems.value.length;
});

const summaryCards = computed(() => {
    return [
        {
            label: '收藏总数',
            value: `${totalCount.value}`,
            note: '桌面工作台当前关注的商品'
        },
        {
            label: '降价提醒',
            value: `${priceDropCount.value}`,
            note: `累计可比原价少花 ¥${totalSaved.value}`
        },
        {
            label: '平台验货',
            value: `${officialCount.value}`,
            note: '平台背书商品占比更适合高客单价'
        },
        {
            label: '失效条目',
            value: `${invalidCount.value}`,
            note: '建议定期清理，避免干扰筛选'
        }
    ];
});

function mapStatus(product) {
    if (product.status === 'SOLD' || product.status === 'OFF_SHELF') {
        return 'invalid';
    }

    if (Number(product.originalPrice || 0) > Number(product.price || 0)) {
        return 'price_drop';
    }

    return 'normal';
}

function mapFavoriteItem(item) {
    const product = item.product;
    const originalPrice = Number(product.originalPrice || 0);
    const price = Number(product.price || 0);
    const priceDrop = Math.max(0, originalPrice - price);
    const trade = normalizeProductTrade(product);

    return {
        id: product.id,
        favoriteId: item.id,
        title: product.title || '未命名商品',
        price,
        originalPrice,
        priceDrop,
        image: product.image || (product.images && product.images[0] ? (typeof product.images[0] === 'string' ? product.images[0] : product.images[0].url) : 'https://via.placeholder.com/640x480?text=TrueUsed'),
        seller: {
            name: product.seller ? (product.seller.nickname || product.seller.username || '匿名卖家') : '匿名卖家',
            avatar: product.seller ? product.seller.avatarUrl || product.seller.avatar : '',
            city: product.locationText || product.seller?.city || '全国'
        },
        views: product.viewsCount || 0,
        status: mapStatus(product),
        tradeMode: trade.tradeMode,
        tradeModeLabel: trade.tradeModeLabel,
        sellerClaimConditionLabel: trade.sellerClaimConditionLabel,
        platformInspectionGradeLabel: trade.platformInspectionGradeLabel,
        primaryConditionLabel: trade.primaryConditionLabel,
        secondaryConditionLabel: trade.secondaryConditionLabel,
        isOfficialTrade: trade.hasPlatformInspection,
        tradeLabel: trade.tradeModeLabel,
        conditionLabel: trade.primaryConditionLabel,
        tags: [trade.secondaryConditionLabel, product.categoryName].filter(Boolean)
    };
}

async function fetchFavorites() {
    loading.value = true;

    try {
        const res = await listMyFavorites({ page: 0, size: 100 });
        favorites.value = (res.content || []).filter(item => item.product).map(item => mapFavoriteItem(item));
        selectedIds.value = selectedIds.value.filter(id => favorites.value.some(item => item.id === id));
    } catch (error) {
        console.error(error);
        showFailToast('加载收藏失败');
    } finally {
        loading.value = false;
    }
}

function toggleEditMode() {
    isEditMode.value = !isEditMode.value;

    if (!isEditMode.value) {
        selectedIds.value = [];
    }
}

function toggleSelection(id) {
    if (!isEditMode.value) return;

    if (selectedIds.value.includes(id)) {
        selectedIds.value = selectedIds.value.filter(itemId => itemId !== id);
        return;
    }

    selectedIds.value = [...selectedIds.value, id];
}

function toggleSelectAllVisible() {
    if (!filteredItems.value.length) return;

    if (allVisibleSelected.value) {
        selectedIds.value = selectedIds.value.filter(id => !filteredItems.value.some(item => item.id === id));
        return;
    }

    const merged = new Set(selectedIds.value);
    filteredItems.value.forEach(item => merged.add(item.id));
    selectedIds.value = Array.from(merged);
}

async function removeByIds(ids) {
    if (!ids.length) return;

    actionLoading.value = true;

    try {
        const results = await Promise.allSettled(ids.map(id => removeFavorite(id)));
        const successIds = ids.filter((id, index) => results[index].status === 'fulfilled');
        const failedCount = results.length - successIds.length;

        if (successIds.length) {
            favorites.value = favorites.value.filter(item => !successIds.includes(item.id));
            selectedIds.value = selectedIds.value.filter(id => !successIds.includes(id));
        }

        if (failedCount > 0 && successIds.length > 0) {
            showFailToast(`已取消 ${successIds.length} 件，${failedCount} 件失败`);
        } else if (failedCount > 0) {
            showFailToast('批量操作失败');
        } else {
            showSuccessToast(`已取消 ${successIds.length} 件收藏`);
        }
    } catch (error) {
        console.error(error);
        showFailToast('操作失败');
    } finally {
        actionLoading.value = false;
    }
}

async function handleRemove(item) {
    await removeByIds([item.id]);
}

async function removeInvalidItems() {
    const invalidIds = favorites.value.filter(item => item.status === 'invalid').map(item => item.id);

    if (!invalidIds.length) {
        showFailToast('当前没有失效收藏');
        return;
    }

    await removeByIds(invalidIds);
}

function openItem(item) {
    if (isEditMode.value) {
        toggleSelection(item.id);
        return;
    }

    router.push(`/product/${item.id}`);
}

onMounted(() => {
    fetchFavorites();
});
</script>

<template>
    <div class="min-h-screen bg-transparent">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">
                <section
                    class="relative overflow-hidden rounded-[32px] border border-white/70 bg-gradient-to-br from-[#10201a] via-[#18362d] to-[#255948] px-8 py-8 text-white shadow-[0_28px_80px_rgba(15,23,42,0.18)]">
                    <div class="absolute right-0 top-0 h-72 w-72 rounded-full bg-white/8 blur-3xl"></div>

                    <div class="relative z-10 flex items-end justify-between gap-8">
                        <div>
                            <div class="inline-flex items-center gap-2 rounded-full border border-emerald-400/20 bg-emerald-300/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-emerald-200">
                                <Heart :size="14" />
                                Favorites Desk
                            </div>
                            <h1 class="mt-4 text-4xl font-black tracking-tight">我的收藏</h1>
                            <p class="mt-3 max-w-2xl text-sm leading-7 text-emerald-50/72">
                                这页现在按桌面工作台来组织，不再只是一排卡片。你可以先看降价和失效状态，再决定批量清理还是继续跟进。
                            </p>
                        </div>

                        <div class="grid grid-cols-2 gap-4">
                            <div class="rounded-3xl border border-white/10 bg-white/10 px-5 py-4 backdrop-blur">
                                <div class="text-xs uppercase tracking-[0.18em] text-emerald-100/60">当前视图</div>
                                <div class="mt-2 text-lg font-bold">{{ activeTab }}</div>
                            </div>
                            <div class="rounded-3xl border border-white/10 bg-white/10 px-5 py-4 backdrop-blur">
                                <div class="text-xs uppercase tracking-[0.18em] text-emerald-100/60">选中条目</div>
                                <div class="mt-2 text-lg font-bold">{{ visibleSelectedCount }}</div>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="grid grid-cols-4 gap-5">
                    <div v-for="card in summaryCards" :key="card.label"
                        class="rounded-[28px] border border-white/70 bg-white/88 p-5 shadow-[0_18px_38px_rgba(15,23,42,0.05)] backdrop-blur">
                        <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">{{ card.label }}</div>
                        <div class="mt-3 text-3xl font-black tracking-tight text-slate-950">{{ card.value }}</div>
                        <div class="mt-2 text-sm text-slate-500">{{ card.note }}</div>
                    </div>
                </section>

                <section class="grid grid-cols-[minmax(0,1fr)_320px] gap-6">
                    <div class="space-y-6">
                        <div
                            class="rounded-[32px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                            <div class="flex items-center justify-between gap-4">
                                <div class="flex items-center gap-3">
                                    <h2 class="text-2xl font-black text-slate-950">收藏清单</h2>
                                    <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
                                        {{ filteredItems.length }} 项
                                    </span>
                                </div>

                                <div class="flex items-center gap-3">
                                    <div class="flex rounded-full border border-slate-200 bg-slate-50 p-1.5">
                                        <button v-for="tab in tabOptions" :key="tab" type="button"
                                            class="rounded-full px-4 py-2 text-sm font-semibold transition-colors"
                                            :class="activeTab === tab ? 'bg-slate-900 text-white' : 'text-slate-500 hover:text-slate-900'"
                                            @click="activeTab = tab">
                                            {{ tab }}
                                        </button>
                                    </div>

                                    <button type="button"
                                        class="inline-flex items-center gap-2 rounded-full border px-4 py-2.5 text-sm font-semibold transition-colors"
                                        :class="isEditMode
                                            ? 'border-rose-200 bg-rose-50 text-rose-600'
                                            : 'border-slate-200 bg-white text-slate-600 hover:border-emerald-200 hover:text-emerald-700'"
                                        @click="toggleEditMode">
                                        <LayoutGrid :size="16" />
                                        {{ isEditMode ? '退出管理' : '批量管理' }}
                                    </button>
                                </div>
                            </div>

                            <div class="mt-6 flex items-center justify-between rounded-[24px] bg-slate-50 px-5 py-4">
                                <div class="text-sm text-slate-500">
                                    <span class="font-semibold text-slate-900">{{ activeTab }}</span>
                                    视图下，平台验货与卖家自出会分开展示，便于快速判断是否值得继续跟进。
                                </div>

                                <button v-if="isEditMode" type="button"
                                    class="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-600 transition-colors hover:border-emerald-200 hover:text-emerald-700"
                                    @click="toggleSelectAllVisible">
                                    <CheckCheck :size="16" />
                                    {{ allVisibleSelected ? '取消全选' : '全选当前结果' }}
                                </button>
                            </div>

                            <div v-if="loading"
                                class="mt-6 flex min-h-[320px] items-center justify-center rounded-[28px] bg-slate-50">
                                <van-loading vertical color="#0b8a61">收藏加载中...</van-loading>
                            </div>

                            <div v-else-if="filteredItems.length"
                                class="mt-6 grid grid-cols-3 gap-5">
                                <button v-for="item in filteredItems" :key="item.id" type="button"
                                    class="group relative overflow-hidden rounded-[28px] border bg-white text-left shadow-sm transition-all"
                                    :class="[
                                        isEditMode && selectedIds.includes(item.id)
                                            ? 'border-emerald-300 shadow-[0_18px_34px_rgba(16,185,129,0.16)]'
                                            : 'border-slate-200 hover:-translate-y-1 hover:border-emerald-200 hover:shadow-[0_18px_34px_rgba(15,23,42,0.08)]',
                                        item.status === 'invalid' ? 'opacity-80' : ''
                                    ]"
                                    @click="openItem(item)">
                                    <div class="relative aspect-[4/3] overflow-hidden bg-slate-100">
                                        <img :src="item.image"
                                            class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" />

                                        <div class="absolute left-3 top-3 flex flex-wrap gap-2">
                                            <span
                                                class="rounded-full px-3 py-1 text-xs font-semibold backdrop-blur"
                                                :class="item.isOfficialTrade ? 'bg-emerald-500/90 text-white' : 'bg-amber-500/90 text-white'">
                                                {{ item.tradeLabel }}
                                            </span>
                                            <span v-if="item.status === 'invalid'"
                                                class="rounded-full bg-slate-900/80 px-3 py-1 text-xs font-semibold text-white">
                                                已失效
                                            </span>
                                        </div>

                                        <div v-if="item.priceDrop > 0"
                                            class="absolute bottom-3 left-3 rounded-full bg-rose-500/92 px-3 py-1 text-xs font-semibold text-white shadow-lg">
                                            直降 ¥{{ item.priceDrop }}
                                        </div>

                                        <div v-if="isEditMode"
                                            class="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-full border border-white/60 bg-white/90 shadow-md">
                                            <div class="h-4 w-4 rounded-full transition-colors"
                                                :class="selectedIds.includes(item.id) ? 'bg-emerald-500' : 'bg-slate-200'"></div>
                                        </div>
                                    </div>

                                    <div class="p-5">
                                        <div class="flex items-start justify-between gap-3">
                                            <h3 class="line-clamp-2 text-base font-bold leading-7 text-slate-950">{{ item.title }}</h3>
                                            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-500">
                                                {{ item.conditionLabel }}
                                            </span>
                                        </div>

                                        <div class="mt-3 flex flex-wrap gap-2">
                                            <span v-for="tag in item.tags" :key="tag"
                                                class="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs text-slate-500">
                                                {{ tag }}
                                            </span>
                                        </div>

                                        <div class="mt-5 flex items-end gap-3">
                                            <div class="text-3xl font-black tracking-tight text-slate-950">¥{{ item.price }}</div>
                                            <div v-if="item.originalPrice" class="pb-1 text-sm text-slate-400 line-through">
                                                ¥{{ item.originalPrice }}
                                            </div>
                                        </div>

                                        <div class="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
                                            <div class="flex items-center gap-3">
                                                <img :src="resolveAvatar(item.seller.avatar)"
                                                    class="h-9 w-9 rounded-full border border-slate-100 object-cover" />
                                                <div>
                                                    <div class="text-sm font-semibold text-slate-900">{{ item.seller.name }}</div>
                                                    <div class="text-xs text-slate-400">{{ item.seller.city }}</div>
                                                </div>
                                            </div>

                                            <div class="flex items-center gap-2">
                                                <div class="inline-flex items-center gap-1 text-xs text-slate-400">
                                                    <Eye :size="14" />
                                                    {{ item.views }}
                                                </div>
                                                <button type="button"
                                                    class="inline-flex items-center gap-1 rounded-full border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-500 transition-colors hover:border-rose-200 hover:text-rose-600"
                                                    @click.stop="handleRemove(item)">
                                                    <Trash2 :size="14" />
                                                    取消收藏
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </button>
                            </div>

                            <div v-else
                                class="mt-6 flex min-h-[320px] flex-col items-center justify-center rounded-[28px] bg-slate-50 text-center text-slate-400">
                                <div class="mb-4 rounded-full bg-white p-5 shadow-sm">
                                    <Heart :size="28" />
                                </div>
                                <div class="text-lg font-semibold text-slate-700">当前筛选没有收藏商品</div>
                                <div class="mt-2 text-sm">可以回到首页继续挑选，或者切换到其他状态视图。</div>
                            </div>
                        </div>
                    </div>

                    <aside class="space-y-6">
                        <div
                            class="sticky top-28 rounded-[30px] border border-white/70 bg-white/92 p-6 shadow-[0_24px_54px_rgba(15,23,42,0.06)] backdrop-blur">
                            <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                                <Sparkles :size="14" />
                                Desk Actions
                            </div>
                            <h3 class="mt-3 text-2xl font-black text-slate-950">批量操作</h3>

                            <div class="mt-5 rounded-[24px] bg-slate-950 p-5 text-white">
                                <div class="text-xs uppercase tracking-[0.18em] text-white/55">当前选中</div>
                                <div class="mt-2 text-4xl font-black">{{ visibleSelectedCount }}</div>
                                <div class="mt-2 text-sm text-white/68">
                                    批量模式会直接围绕当前筛选结果工作，不需要在桌面端来回切换页面。
                                </div>
                            </div>

                            <div class="mt-5 space-y-3">
                                <button type="button"
                                    class="flex w-full items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 text-left text-sm font-semibold text-slate-700 transition-colors hover:border-emerald-200 hover:text-emerald-700 disabled:cursor-not-allowed disabled:opacity-45"
                                    :disabled="!visibleSelectedCount || actionLoading"
                                    @click="removeByIds(selectedVisibleIds)">
                                    <span class="inline-flex items-center gap-2">
                                        <CheckCheck :size="16" />
                                        取消选中收藏
                                    </span>
                                    <span>{{ visibleSelectedCount }}</span>
                                </button>

                                <button type="button"
                                    class="flex w-full items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 text-left text-sm font-semibold text-slate-700 transition-colors hover:border-rose-200 hover:text-rose-600 disabled:cursor-not-allowed disabled:opacity-45"
                                    :disabled="!invalidCount || actionLoading"
                                    @click="removeInvalidItems">
                                    <span class="inline-flex items-center gap-2">
                                        <Trash2 :size="16" />
                                        清理失效条目
                                    </span>
                                    <span>{{ invalidCount }}</span>
                                </button>
                            </div>

                            <div class="mt-6 space-y-3">
                                <div class="flex items-start gap-3 rounded-2xl border border-slate-200 px-4 py-3">
                                    <ShieldCheck :size="18" class="mt-0.5 text-emerald-600" />
                                    <div>
                                        <div class="text-sm font-semibold text-slate-900">验货优先级</div>
                                        <div class="mt-1 text-xs leading-6 text-slate-500">
                                            高客单价收藏建议优先保留平台验货商品，减少沟通成本和成色争议。
                                        </div>
                                    </div>
                                </div>

                                <div class="flex items-start gap-3 rounded-2xl border border-slate-200 px-4 py-3">
                                    <TriangleAlert :size="18" class="mt-0.5 text-amber-500" />
                                    <div>
                                        <div class="text-sm font-semibold text-slate-900">失效清理</div>
                                        <div class="mt-1 text-xs leading-6 text-slate-500">
                                            失效条目会拖慢桌面筛选效率，建议直接批量移除，只保留真正要跟进的商品。
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </aside>
                </section>
        </main>
    </div>
</template>
