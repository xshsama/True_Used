<template>
    <div class="min-h-screen bg-transparent pb-12">
        <div class="mx-auto max-w-[1480px] px-8 py-6">
            <section
                class="relative mb-5 overflow-hidden rounded-[32px] bg-[radial-gradient(circle_at_top_left,_rgba(80,220,150,0.18),_transparent_28%),linear-gradient(120deg,#123729_0%,#173f31_42%,#205540_100%)] text-white shadow-[0_26px_72px_rgba(16,66,48,0.2)]">
                <div
                    class="pointer-events-none absolute -left-16 top-0 h-56 w-56 rounded-full bg-emerald-300/10 blur-3xl">
                </div>
                <div
                    class="pointer-events-none absolute right-0 top-0 h-[400px] w-[400px] translate-x-1/4 -translate-y-1/3 rounded-full bg-emerald-400/10 blur-3xl">
                </div>
                <div
                    class="i-lucide-shield-check pointer-events-none absolute -bottom-16 right-0 h-[280px] w-[280px] rotate-12 text-white/[0.04]">
                </div>

                <div class="relative z-10 grid min-h-[272px] grid-cols-[minmax(0,1.15fr)_420px] items-center gap-8 px-10 py-8">
                    <div class="max-w-[640px]">
                        <div
                            class="inline-flex items-center gap-2 rounded-full border border-emerald-400/20 bg-emerald-950/28 px-3 py-1.5 text-xs font-bold uppercase tracking-[0.18em] text-emerald-200 shadow-inner backdrop-blur-md">
                            <div class="i-lucide-badge-check text-sm text-emerald-300"></div>
                            <span>Platform Check · 平台验货</span>
                        </div>

                        <h1 class="mt-5 text-[clamp(3.25rem,4.6vw,4.5rem)] font-black leading-[0.95] tracking-[-0.045em]">
                            怕踩雷？<br />
                            <span class="bg-gradient-to-r from-emerald-200 via-white to-emerald-100 bg-clip-text text-transparent">先看成色</span>，再决定出手。
                        </h1>

                        <p class="mt-4 max-w-[560px] text-[15px] leading-7 text-emerald-50/76">
                            先看卖家描述，再看平台补充的成色、拆修和配件信息。把最影响成交的判断点提前放到首屏，不用翻很深才知道风险。
                        </p>

                        <div class="mt-7 flex items-center gap-3">
                            <button @click="scrollToListings"
                                class="group inline-flex cursor-pointer items-center gap-2 rounded-full border-none bg-white px-8 py-3.5 text-sm font-bold text-[#173b2d] shadow-[0_16px_40px_rgba(255,255,255,0.18)] transition-all hover:-translate-y-0.5 hover:bg-emerald-50">
                                看最新上架
                                <div class="i-lucide-chevron-right text-lg text-emerald-600 transition-transform group-hover:translate-x-1"></div>
                            </button>
                            <button @click="router.push('/ranking')"
                                class="inline-flex items-center gap-2 rounded-full border border-white/14 bg-white/8 px-6 py-3 text-sm font-semibold text-white/86 backdrop-blur-md transition-all hover:border-white/24 hover:bg-white/12">
                                <div class="i-lucide-trophy text-base"></div>
                                去捡漏榜
                            </button>
                        </div>
                    </div>

                    <div class="space-y-3">
                        <div
                            class="hero-card hero-card-primary col-span-2 rounded-[30px] border border-white/12 bg-white/10 p-6 shadow-[0_24px_56px_rgba(6,24,19,0.22)] backdrop-blur-xl transition-transform duration-300 hover:-translate-y-1">
                            <div class="flex items-start justify-between gap-6">
                                <div>
                                    <div
                                        class="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/8 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-emerald-100/86">
                                        <div class="i-lucide-scan-search text-sm"></div>
                                        下单前先看
                                    </div>
                                    <div class="mt-4 text-[28px] font-black leading-[1.15] text-white">
                                        这 3 项，最容易影响成交判断。
                                    </div>
                                    <div class="mt-3 max-w-[320px] text-sm leading-7 text-white/68">
                                        先确认真实成色，再看有没有拆修和缺失配件，能更快判断这单值不值得继续聊。
                                    </div>
                                </div>

                                <div
                                    class="flex h-16 w-16 shrink-0 items-center justify-center rounded-[22px] border border-white/10 bg-emerald-300/10 text-emerald-100/90 shadow-inner">
                                    <div class="i-lucide-shield-check text-3xl"></div>
                                </div>
                            </div>

                            <div class="mt-5 flex flex-wrap gap-2">
                                <span class="hero-chip">成色评级</span>
                                <span class="hero-chip">拆修记录</span>
                                <span class="hero-chip">配件完整度</span>
                            </div>
                        </div>

                        <div class="grid grid-cols-2 gap-3">
                            <div
                                class="hero-card hero-card-secondary rounded-[24px] border border-white/12 bg-white/9 p-4 shadow-[0_18px_36px_rgba(6,24,19,0.18)] backdrop-blur-xl transition-transform duration-300 hover:-translate-y-1">
                                <div class="flex items-center justify-between">
                                    <div
                                        class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white/10 text-emerald-100">
                                        <div class="i-lucide-store text-lg"></div>
                                    </div>
                                    <div class="text-[11px] uppercase tracking-[0.18em] text-white/42">Mode A</div>
                                </div>
                                <div class="mt-5 text-xl font-black text-white">卖家自出</div>
                                <div class="mt-2 text-sm leading-6 text-white/64">
                                    先看卖家填写的成色和备注，适合快速初筛。
                                </div>
                            </div>

                            <div
                                class="hero-card hero-card-tertiary rounded-[24px] border border-white/12 bg-white/9 p-4 shadow-[0_18px_36px_rgba(6,24,19,0.18)] backdrop-blur-xl transition-transform duration-300 hover:-translate-y-1">
                                <div class="flex items-center justify-between">
                                    <div
                                        class="flex h-10 w-10 items-center justify-center rounded-2xl bg-white/10 text-emerald-100">
                                        <div class="i-lucide-badge-alert text-lg"></div>
                                    </div>
                                    <div class="text-[11px] uppercase tracking-[0.18em] text-white/42">Mode B</div>
                                </div>
                                <div class="mt-5 text-xl font-black text-white">平台补充</div>
                                <div class="mt-2 text-sm leading-6 text-white/64">
                                    重点补足拆修、配件和风险判断，降低信息差。
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section class="mb-6 grid grid-cols-3 gap-5">
                <div
                    class="rounded-[28px] border border-white/45 bg-white/28 p-5 shadow-[0_20px_40px_rgba(15,23,42,0.06)] backdrop-blur-xl">
                    <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">在售商品</div>
                    <div class="mt-2 text-3xl font-black text-slate-900">{{ totalProducts }}</div>
                </div>
                <div
                    class="rounded-[28px] border border-white/45 bg-white/28 p-5 shadow-[0_20px_40px_rgba(15,23,42,0.06)] backdrop-blur-xl">
                    <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">当前类目</div>
                    <div class="mt-2 text-lg font-bold text-slate-900">{{ currentCategoryName }}</div>
                </div>
                <div
                    class="rounded-[28px] border border-white/45 bg-white/28 p-5 shadow-[0_20px_40px_rgba(15,23,42,0.06)] backdrop-blur-xl">
                    <div class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">排序策略</div>
                    <div class="mt-2 text-lg font-bold text-slate-900">{{ currentSortLabel }}</div>
                </div>
            </section>

            <div class="mb-6 flex flex-wrap gap-3">
                <div v-for="cat in categories" :key="cat.id"
                    class="flex cursor-pointer items-center gap-1.5 whitespace-nowrap rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:border-gray-300 hover:bg-gray-50"
                    :class="{ '!border-gray-800 !bg-gray-800 !text-white': activeCategory === cat.id }"
                    @click="handleCategoryChange(cat.id)">
                    <div :class="cat.icon" class="text-base"></div>
                    {{ cat.name }}
                </div>
            </div>

            <div ref="listingSection" class="mb-8">
                <div class="mb-5 flex items-end justify-between gap-3">
                    <div class="flex items-baseline gap-2">
                        <h2 class="text-2xl font-extrabold text-gray-900">最新上架</h2>
                        <span class="rounded-full bg-gray-200 px-2 py-0.5 text-xs text-gray-600">{{ totalProducts }}件新商品</span>
                    </div>
                    <div class="flex gap-2 rounded-full border border-slate-200 bg-white/90 p-1.5 shadow-sm">
                        <button v-for="option in sortOptions" :key="option.value" type="button"
                            class="rounded-full px-4 py-2 text-sm font-semibold transition-colors"
                            :class="activeSort === option.value ? 'bg-slate-900 text-white' : 'text-slate-500 hover:text-slate-900'"
                            @click="handleSortChange(option.value)">
                            {{ option.label }}
                        </button>
                    </div>
                </div>

                <TransitionGroup name="product-list" tag="div"
                    class="relative grid min-h-[200px] grid-cols-3 gap-6 xl:grid-cols-4 2xl:grid-cols-5">
                    <div v-if="loading && page === 0" key="loading-spinner"
                        class="absolute inset-0 z-10 col-span-full flex items-center justify-center rounded-xl bg-white/50 backdrop-blur-sm">
                        <van-loading vertical color="#10B981">
                            <template #icon>
                                <div class="i-lucide-loader-2 animate-spin text-3xl text-emerald-600"></div>
                            </template>
                            <span class="mt-2 font-medium text-emerald-600">加载好物中...</span>
                        </van-loading>
                    </div>

                    <ProductCard v-for="product in productList" :key="product.id" :product="product"
                        @click="router.push(`/product/${product.id}`)" />
                </TransitionGroup>

                <div v-if="productList.length > 0 && hasMore" class="mt-10 flex justify-center">
                    <button type="button"
                        class="group inline-flex min-w-[320px] items-center justify-between gap-6 rounded-[26px] border border-slate-200 bg-white px-6 py-4 text-left shadow-[0_18px_36px_rgba(15,23,42,0.06)] transition-all hover:-translate-y-0.5 hover:border-emerald-200 hover:shadow-[0_20px_40px_rgba(15,23,42,0.08)] disabled:cursor-not-allowed disabled:opacity-70"
                        :disabled="loading" @click="fetchProducts">
                        <div>
                            <div class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">
                                More Listings
                            </div>
                            <div class="mt-1 text-base font-bold text-slate-900">
                                {{ loading ? '正在加载更多好物...' : '继续浏览更多商品' }}
                            </div>
                            <div class="mt-1 text-sm text-slate-500">
                                {{ loading ? '请稍候，新的商品卡片正在进入列表。' : `已展示 ${productList.length} 件，再展开下一页结果。` }}
                            </div>
                        </div>

                        <div
                            class="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-slate-950 text-white shadow-[0_12px_24px_rgba(15,23,42,0.18)] transition-transform group-hover:translate-x-1">
                            <div v-if="loading" class="i-lucide-loader-2 animate-spin text-lg"></div>
                            <div v-else class="i-lucide-arrow-right text-lg"></div>
                        </div>
                    </button>
                </div>
                <div v-if="!hasMore && productList.length > 0" class="mt-8 text-center text-sm text-gray-400">没有更多了</div>
            </div>
        </div>

        <div class="fixed bottom-0 right-0 z-50 p-8">
            <div class="flex h-11 w-11 cursor-pointer items-center justify-center rounded-full border border-gray-200 bg-white shadow-lg transition-all hover:border-emerald-600 hover:text-emerald-600"
                @click="scrollToTop">
                <div class="i-lucide-arrow-up-to-line text-lg"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { listRootCategories } from '@/api/categories'
import { listProducts } from '@/api/products'
import ProductCard from '@/components/ProductCard.vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = ref(10)
const totalProducts = ref(0)
const listingSection = ref(null)
const activeSort = ref('created_desc')
const sortOptions = [
    { label: '最新上架', value: 'created_desc' },
    { label: '关注最高', value: 'views_desc' },
    { label: '价格最低', value: 'price_asc' },
    { label: '价格最高', value: 'price_desc' }
]

const activeCategory = ref(0)
const categories = ref([
    { id: 0, name: '全部', icon: 'i-lucide-layout-grid' }
])

const currentCategoryName = computed(() => {
    return categories.value.find(cat => cat.id === activeCategory.value)?.name || '全部'
})

const currentSortLabel = computed(() => {
    return sortOptions.find(option => option.value === activeSort.value)?.label || '最新上架'
})

const handleCategoryChange = (id) => {
    if (activeCategory.value === id) return
    activeCategory.value = id
    page.value = 0
    hasMore.value = true
    fetchProducts()
}

const handleSortChange = (sort) => {
    if (activeSort.value === sort) return
    activeSort.value = sort
    page.value = 0
    hasMore.value = true
    fetchProducts()
}

const fetchCategories = async () => {
    try {
        const res = await listRootCategories()
        const getIcon = (id) => {
            const icons = {
                1: 'i-lucide-smartphone',
                2: 'i-lucide-shirt',
                3: 'i-lucide-armchair',
                4: 'i-lucide-book',
                5: 'i-lucide-dumbbell'
            }
            return icons[id] || 'i-lucide-package'
        }

        const fetched = (res || []).map(c => ({
            id: c.id,
            name: c.name,
            icon: getIcon(c.id)
        }))
        categories.value = [{ id: 0, name: '全部', icon: 'i-lucide-layout-grid' }, ...fetched]
    } catch (e) {
        console.error('Failed to fetch categories', e)
    }
}

const productList = ref([])

const fetchProducts = async () => {
    if (loading.value) return
    loading.value = true
    try {
        const params = {
            page: page.value,
            size: size.value,
            sort: activeSort.value,
            categoryId: activeCategory.value === 0 ? undefined : activeCategory.value
        }
        const res = await listProducts(params)
        if (page.value === 0) productList.value = []
        totalProducts.value = res.totalElements || 0

        const newItems = res.content || []
        productList.value.push(...newItems)
        hasMore.value = !res.last
        page.value++
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

const scrollToListings = () => {
    listingSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
    fetchCategories()
    fetchProducts()
})
</script>

<style scoped>
.hero-card {
    position: relative;
    overflow: hidden;
    animation: hero-rise 0.7s ease-out both;
}

.hero-card::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.12), transparent 55%);
    opacity: 0.45;
    pointer-events: none;
}

.hero-card-primary {
    animation-delay: 0.06s;
}

.hero-card-secondary {
    animation-delay: 0.14s;
}

.hero-card-tertiary {
    animation-delay: 0.22s;
}

.hero-chip {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(255, 255, 255, 0.08);
    border-radius: 9999px;
    padding: 0.4rem 0.85rem;
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    color: rgba(236, 253, 245, 0.92);
}

@keyframes hero-rise {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
