<template>
    <div class="search-page">
        <div class="search-header">
            <button type="button" class="search-back" @click="goBack">
                <div class="i-lucide-chevron-left w-5 h-5"></div>
            </button>
            <div class="search-header-bar">
                <SearchBar v-model="searchValue" placeholder="搜索商品"
                    :show-shortcut="false" @search="onSearch" />
            </div>
        </div>

        <!-- 搜索历史 -->
        <div v-if="!searchValue && searchHistory.length > 0" class="search-history">
            <div class="section-header">
                <span class="section-title">搜索历史</span>
                <van-button size="small" plain @click="clearHistory">清空</van-button>
            </div>
            <div class="history-tags">
                <van-tag v-for="(item, index) in searchHistory" :key="index" @click="applyKeyword(item)">
                    {{ item }}
                </van-tag>
            </div>
        </div>

        <!-- 热门搜索 -->
        <div v-if="!searchValue" class="hot-search">
            <div class="section-title">热门搜索</div>
            <div class="hot-tags">
                <van-tag v-for="(item, index) in hotSearch" :key="index" type="primary" plain
                    @click="applyKeyword(item)">
                    {{ item }}
                </van-tag>
            </div>
        </div>

        <!-- 搜索与筛选结果 -->
        <div class="search-results">
            <!-- 顶部筛选栏（排序/价格区间/类目占位） -->
            <div class="filter-bar">
                <van-dropdown-menu>
                    <van-dropdown-item v-model="sort" :options="sortOptions" @change="onSortChange" />
                </van-dropdown-menu>
                <div class="price-range">
                    <van-field v-model.number="priceMin" type="number" placeholder="最低价" input-align="center" />
                    <span class="sep">-</span>
                    <van-field v-model.number="priceMax" type="number" placeholder="最高价" input-align="center" />
                    <van-button size="small" type="primary" @click="applyFilters">筛选</van-button>
                </div>
            </div>

            <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
                <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
                    <!-- 骨架屏 -->
                    <div v-if="loading && page === 0" class="product-grid">
                        <div v-for="i in 8" :key="i" class="product-skeleton">
                            <van-skeleton animated :row="3" />
                        </div>
                    </div>

                    <!-- 结果列表 (Grid) -->
                    <div v-else-if="resultList.length > 0" class="product-grid">
                        <ProductCard v-for="item in resultList" :key="item.id" :product="item" :show-desc="true"
                            @click="goToProductDetail(item.id)" />
                    </div>

                    <van-empty v-else-if="searched && !loading" image="search" description="没有找到相关商品" />
                </van-list>
            </van-pull-refresh>
        </div>
    </div>
</template>

<script>
import { listProducts } from '@/api/products'
import ProductCard from '@/components/ProductCard.vue'
import SearchBar from '@/components/SearchBar.vue'
import { useUserStore } from '@/stores/user'
import { showFailToast, showSuccessToast } from 'vant'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export default {
    name: 'Search',
    components: { ProductCard, SearchBar },
    setup() {
        const router = useRouter()
        const route = useRoute()
        const userStore = useUserStore()

        const searchValue = ref('')
        const refreshing = ref(false)
        const loading = ref(false)
        const finished = ref(false)
        const searched = ref(false)
        const resultList = ref([])
        const page = ref(0)
        const size = ref(10)
        const sort = ref('created,desc')
        const priceMin = ref()
        const priceMax = ref()
        const categoryId = ref()
        const currentUserId = computed(() => userStore.user?.id)

        const isNotOwnProduct = (product) => {
            return !currentUserId.value || Number(product.seller?.id) !== Number(currentUserId.value)
        }

        const sortOptions = [
            { text: '最新发布', value: 'created,desc' },
            { text: '价格从低到高', value: 'price,asc' },
            { text: '价格从高到低', value: 'price,desc' },
            { text: '收藏最多', value: 'favorites,desc' },
            { text: '浏览最多', value: 'views,desc' },
        ]

        // 搜索历史
        const searchHistory = ref([
            'iPhone 14',
            'MacBook',
            '耐克鞋',
            '二手相机'
        ])

        // 热门搜索
        const hotSearch = ref([
            '手机数码',
            '笔记本电脑',
            '运动鞋',
            '相机镜头',
            '游戏主机',
            '平板电脑'
        ])

        // 拉取列表
        const fetchList = async () => {
            loading.value = true
            try {
                const params = {
                    q: searchValue.value || undefined,
                    categoryId: categoryId.value || undefined,
                    priceMin: priceMin.value || undefined,
                    priceMax: priceMax.value || undefined,
                    sort: sort.value || undefined,
                    page: page.value,
                    size: size.value,
                }
                const res = await listProducts(params)
                // 后端返回 Page<ProductDTO>
                const content = (res?.content || []).filter(isNotOwnProduct)
                if (page.value === 0) resultList.value = []
                resultList.value.push(...content)
                finished.value = res?.last || content.length < size.value
                page.value += 1
            } catch (e) {
                showFailToast('加载失败')
                finished.value = true
            } finally {
                loading.value = false
                refreshing.value = false
            }
        }

        // 搜索
        const onSearch = () => {
            if (!searchValue.value.trim()) return

            // 添加到搜索历史
            const keyword = searchValue.value.trim()
            if (!searchHistory.value.includes(keyword)) {
                searchHistory.value.unshift(keyword)
                if (searchHistory.value.length > 10) {
                    searchHistory.value.pop()
                }
            }

            // 执行搜索
            searched.value = true
            page.value = 0
            resultList.value = []
            finished.value = false
            fetchList()
        }

        // 清空搜索
        const onClear = () => {
            searched.value = false
            resultList.value = []
        }

        // 清空历史
        const clearHistory = () => {
            searchHistory.value = []
            showSuccessToast('已清空搜索历史')
        }

        // 下拉刷新
        const onRefresh = () => {
            page.value = 0
            finished.value = false
            fetchList()
        }

        // 加载更多
        const onLoad = () => {
            if (finished.value) return
            fetchList()
        }

        const onSortChange = () => {
            page.value = 0
            resultList.value = []
            finished.value = false
            fetchList()
        }

        const applyFilters = () => {
            if (priceMin.value && priceMax.value && Number(priceMin.value) > Number(priceMax.value)) {
                showFailToast('最小价格不能大于最大价格')
                return
            }
            page.value = 0
            resultList.value = []
            finished.value = false
            fetchList()
        }

        // 跳转商品详情
        const goToProductDetail = (id) => {
            router.push(`/product/${id}`)
        }

        const applyKeyword = (keyword) => {
            searchValue.value = keyword
            onSearch()
        }

        const goBack = () => {
            router.go(-1)
        }

        watch(searchValue, (value) => {
            if (!String(value || '').trim()) {
                onClear()
            }
        })

        onMounted(() => {
            const { q, sort: sortQuery } = route.query
            if (q) {
                searchValue.value = q
                onSearch()
            } else if (sortQuery) {
                // If come from "More" link which might not have q but has sort
                sort.value = sortQuery
                // If no search value, maybe we want to list all products?
                // The current onSearch requires searchValue. But fetchList handles optional q.
                // Let's trigger fetchList directly if sort is present but no q
                searched.value = true
                fetchList()
            }
        })

        return {
            searchValue,
            refreshing,
            loading,
            finished,
            searched,
            resultList,
            page,
            size,
            sort,
            sortOptions,
            priceMin,
            priceMax,
            categoryId,
            searchHistory,
            hotSearch,
            onSearch,
            onClear,
            clearHistory,
            onRefresh,
            onLoad,
            onSortChange,
            applyFilters,
            goToProductDetail,
            applyKeyword,
            goBack
        }
    }
}
</script>

<style scoped>
.search-page {
    min-height: 100vh;
    background-color: #f7f8fa;
}

.search-header {
    position: sticky;
    top: 0;
    z-index: 20;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border-bottom: 1px solid rgba(226, 232, 240, 0.8);
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(18px);
}

.search-back {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: 9999px;
    border: 1px solid rgba(203, 213, 225, 0.85);
    background: rgba(255, 255, 255, 0.92);
    color: #475569;
    flex-shrink: 0;
}

.search-header-bar {
    width: 100%;
    max-width: 640px;
}

.search-history,
.hot-search {
    background: #fff;
    padding: 16px;
    margin-bottom: 8px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.section-title {
    font-size: 14px;
    font-weight: 500;
    color: #323233;
    margin-bottom: 12px;
}

.history-tags,
.hot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.filter-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 8px 12px;
    background: #fff;
    margin-bottom: 8px;
}

.price-range {
    display: flex;
    align-items: center;
    gap: 6px;
}

.price-range .sep {
    color: #c8c9cc;
}

/* 商品网格 - 响应式 Grid */
.product-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    /* 手机端双列 */
    gap: 12px;
    padding: 12px 16px;
    /* consistent padding */
}

@media (min-width: 768px) {
    .product-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;
    }
}

@media (min-width: 1024px) {
    .product-grid {
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;
        padding: 20px 16px;
    }
}

@media (min-width: 1440px) {
    .product-grid {
        grid-template-columns: repeat(5, 1fr);
        /* 宽屏五列 */
    }
}

.product-skeleton {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 0;
}
</style>
