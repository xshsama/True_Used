<script setup>
import { fetchMyStats } from '@/api/auth';
import { getMyConsignments, updateConsignmentLogistics } from '@/api/consignment';
import { deleteProduct, getMyProducts, hideProduct, polishProduct, publishProduct } from '@/api/products';
import SearchBar from '@/components/SearchBar.vue';
import {
    ArrowDown,
    Check,
    Eye,
    Heart,
    MessageCircle,
    MessageSquare,
    MoreHorizontal,
    RefreshCw,
    TrendingUp,
    Truck
} from 'lucide-vue-next';
import { showConfirmDialog, showDialog, showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

// --- State ---
const activeTab = ref('inbound');
const searchQuery = ref('');
const isBatchMode = ref(false);
const selectedItems = ref([]);
const items = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const isInitialLoading = ref(true);
const page = ref(0);
const pageSize = 20;
const rejectedProductIds = ref([]);

// Stats
const stats = ref({
    views: 0,
    unread: 0,
    income: 0
});

const tabs = [
    { name: '入仓期', key: 'inbound' },
    { name: '验货期', key: 'inspecting' },
    { name: '销售期', key: 'sales' },
    { name: '下架', key: 'off' },
];

// --- Helpers ---
const getStatusText = (status) => {
    const map = {
        'CREATED': '待发货',
        'SHIPPED': '运输中',
        'RECEIVED': '已签收',
        'INSPECTING': '验货中',
        'PASSED': '验货通过',
        'REJECTED': '验货驳回',
        'RETURNING': '退回中',
        'RETURNED': '已退回',
        'PENDING': '待处理',
        'ON_SALE': '出售中',
        'LOCKED': '交易中',
        'SOLD': '已售出',
        'OFF_SHELF': '已下架'
    };
    return map[status] || status;
};

// --- API & Data ---
const loadStats = async () => {
    try {
        const res = await fetchMyStats();
        stats.value = {
            views: res.viewsCount || 0,
            unread: res.unreadMessages || 0,
            income: res.totalIncome || 0
        };
    } catch (e) {
        console.error('Failed to load stats', e);
    }
};

const loadData = async () => {
    if (loading.value) return;
    loading.value = true;

    try {
        if (activeTab.value === 'inbound' || activeTab.value === 'inspecting') {
            // Load Consignments
            const res = await getMyConsignments();
            const mapped = res.map(c => ({
                id: c.id,
                title: c.title,
                price: c.expectedPrice,
                originalPrice: null, // Consignments don't usually show original price here
                image: c.product && c.product.images && c.product.images.length > 0 ? c.product.images[0].url : '',
                status: c.status,
                trackingNo: c.trackingNoInbound,
                type: 'consignment',
                views: 0,
                comments: 0,
                likes: 0
            }));

            // Filter locally since API returns all
            if (activeTab.value === 'inbound') {
                items.value = mapped.filter(i => ['CREATED', 'SHIPPED', 'RECEIVED'].includes(i.status));
            } else {
                items.value = mapped.filter(i => i.status === 'INSPECTING');
            }
            finished.value = true;
        } else {
            if (activeTab.value === 'off') {
                await loadRejectedProductIds();
            }

            // Load Products
            const res = await getMyProducts({
                page: page.value,
                size: pageSize,
                sort: 'created_desc'
            });

            const newItems = (res.content || []).map(p => ({
                id: p.id,
                title: p.title,
                price: p.price,
                originalPrice: p.originalPrice || (p.price * 1.2).toFixed(0),
                image: p.images && p.images.length > 0 ? p.images[0].url : '',
                views: p.viewsCount || 0,
                comments: 0,
                likes: p.favoritesCount || 0,
                status: p.status,
                type: 'product'
            }));

            // Filter locally if needed, but usually backend handles pagination. 
            // However, getMyProducts returns all products for the user. 
            // We need to filter by status group.
            // Since backend pagination might return mixed statuses, we might need to filter on client side 
            // OR update backend to support status filtering. 
            // For now, let's assume we filter client side and append.

            let filteredNewItems = [];
            if (activeTab.value === 'sales') {
                filteredNewItems = newItems.filter(i => ['ON_SALE', 'LOCKED'].includes(i.status));
            } else {
                filteredNewItems = newItems.filter(i => ['SOLD', 'OFF_SHELF'].includes(i.status));
            }

            if (page.value === 0) {
                items.value = dedupeById(filteredNewItems);
            } else {
                items.value = dedupeById([...items.value, ...filteredNewItems]);
            }

            finished.value = res.last;
            page.value++;
        }
    } catch (error) {
        console.error(error);
        showFailToast('加载失败');
    } finally {
        loading.value = false;
        isInitialLoading.value = false;
        refreshing.value = false;
    }
};

const onRefresh = async () => {
    page.value = 0;
    finished.value = false;
    items.value = [];
    rejectedProductIds.value = [];
    await loadData();
    await loadStats();
    showSuccessToast('已刷新');
};

const onLoad = () => {
    if (!refreshing.value && !isInitialLoading.value && (activeTab.value === 'sales' || activeTab.value === 'off')) {
        loadData();
    }
};

// --- Computed ---
const filteredItems = computed(() => {
    let result = items.value;
    if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase();
        result = result.filter(p => p.title.toLowerCase().includes(q));
    }
    return result;
});

const dedupeById = (list) => {
    const seen = new Set();
    return list.filter(item => {
        if (seen.has(item.id)) return false;
        seen.add(item.id);
        return true;
    });
};

const loadRejectedProductIds = async () => {
    try {
        const consignments = await getMyConsignments();
        rejectedProductIds.value = (consignments || [])
            .filter(c => c.status === 'REJECTED' && c.product && c.product.id)
            .map(c => c.product.id);
    } catch (e) {
        console.error('Failed to load rejected consignments', e);
        rejectedProductIds.value = [];
    }
};

const isInspectionRejectedProduct = (productId) => {
    return rejectedProductIds.value.includes(productId);
};

const tabCounts = computed(() => {
    // This is tricky because we don't have counts for all tabs without fetching everything.
    // For now, we can only show counts for the loaded data or hide them.
    // Let's hide them or show 0 to avoid confusion, or implement a separate count API.
    return {};
});

// --- Actions ---
const toggleSelect = (id) => {
    if (!isBatchMode.value) return;
    if (selectedItems.value.includes(id)) {
        selectedItems.value = selectedItems.value.filter(i => i !== id);
    } else {
        selectedItems.value.push(id);
    }
};

const toggleStatus = async (item) => {
    if (item.status !== 'ON_SALE' && isInspectionRejectedProduct(item.id)) {
        showFailToast('验货驳回商品不能重新上架');
        return;
    }

    const action = item.status === 'ON_SALE' ? '下架' : '上架';
    showConfirmDialog({ title: `${action}商品`, message: `确定要${action}这个商品吗？` })
        .then(async () => {
            try {
                if (item.status === 'ON_SALE') {
                    await hideProduct(item.id);
                    // Refresh to move to 'off' tab
                    onRefresh();
                } else {
                    await publishProduct(item.id);
                    // Refresh to move to 'sales' tab
                    onRefresh();
                }
                showSuccessToast(`${action}成功`);
            } catch (e) {
                showFailToast('操作失败');
            }
        }).catch(() => { });
};

const removeItem = (item) => {
    showConfirmDialog({ title: '删除商品', message: '删除后无法恢复，确定删除吗？' })
        .then(async () => {
            try {
                await deleteProduct(item.id);
                items.value = items.value.filter(p => p.id !== item.id);
                showSuccessToast('已删除');
            } catch (e) {
                showFailToast('删除失败');
            }
        }).catch(() => { });
};

const handleShip = (item) => {
    showDialog({
        title: '填写物流单号',
        message: '请输入顺丰/快递单号',
        showCancelButton: true,
        confirmButtonText: '发货',
        showInput: true,
    }).then(() => {
        // Placeholder for simple prompt since Vant dialog input is limited in this version
        const trackingNo = prompt("请输入物流单号");
        if (trackingNo) {
            updateConsignmentLogistics(item.id, trackingNo).then(() => {
                showSuccessToast('发货成功');
                onRefresh();
            }).catch(() => {
                showFailToast('发货失败');
            });
        }
    }).catch(() => {
        // Cancelled
    });
};

const handlePolish = async (item) => {
    showConfirmDialog({
        title: '擦亮商品',
        message: '擦亮将消耗一张推广券，确定继续吗？'
    }).then(async () => {
        try {
            await polishProduct(item.id);
            showSuccessToast('擦亮成功，宝贝已排到最前');
            onRefresh();
        } catch (e) {
            // Check for specific error messages from backend
            const msg = e.response?.data?.message || e.response?.data || '';
            if (msg.includes('推广券')) {
                showFailToast('擦亮失败：您没有可用的推广券');
            } else if (msg.includes('每天')) {
                showFailToast('擦亮失败：该商品今天已擦亮过');
            } else {
                showFailToast('擦亮失败，请稍后重试');
            }
        }
    }).catch(() => {
        // Cancelled
    });
};

// --- Lifecycle ---
onMounted(() => {
    if (route.query.tab) {
        activeTab.value = route.query.tab;
    }
    loadData();
    loadStats();
});

watch(activeTab, () => {
    page.value = 0;
    finished.value = false;
    items.value = [];
    loadData();
});

</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- 1. Data Dashboard -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div
                        class="bg-white rounded-2xl p-6 border border-gray-100/50 shadow-sm relative overflow-hidden group">
                        <div class="absolute right-0 top-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
                            <Eye :size="64" class="text-[#4a8b6e]" />
                        </div>
                        <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">昨日曝光量</div>
                        <div class="text-3xl font-bold text-[#2c3e50] font-mono">{{ stats.views }}</div>
                        <div class="mt-4 flex items-center gap-1 text-xs font-medium text-[#4a8b6e]">
                            <TrendingUp :size="12" /> 较上周 +12%
                        </div>
                    </div>

                    <div
                        class="bg-white rounded-2xl p-6 border border-gray-100/50 shadow-sm relative overflow-hidden group">
                        <div class="absolute right-0 top-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
                            <MessageCircle :size="64" class="text-blue-500" />
                        </div>
                        <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-1">未读留言</div>
                        <div class="text-3xl font-bold text-[#2c3e50] font-mono">{{ stats.unread }}</div>
                        <div class="mt-4 flex items-center gap-1 text-xs font-medium text-gray-500">
                            3 个买家正在咨询
                        </div>
                    </div>

                    <div
                        class="bg-gradient-to-r from-[#4a8b6e] to-[#3b755b] rounded-2xl p-6 text-white shadow-lg shadow-[#4a8b6e]/20 relative overflow-hidden">
                        <div class="text-xs font-bold text-white/80 uppercase tracking-wider mb-1">本月预估收入</div>
                        <div class="text-3xl font-bold font-mono">¥ {{ stats.income }}</div>
                        <button
                            class="mt-4 bg-white/20 hover:bg-white/30 text-white text-xs px-3 py-1.5 rounded-lg backdrop-blur-sm transition-colors flex items-center gap-1">
                            去提现 <div class="i-lucide-chevron-right w-3 h-3"></div>
                        </button>
                    </div>
                </div>

                <!-- 2. Toolbar -->
                <div
                    class="bg-white rounded-2xl p-4 shadow-sm border border-gray-100/50 flex flex-col md:flex-row items-center justify-between gap-4">
                    <div class="flex gap-2 w-full md:w-auto overflow-x-auto scrollbar-hide">
                        <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key"
                            :class="['px-4 py-2 rounded-lg text-sm font-bold whitespace-nowrap transition-all flex items-center gap-2', activeTab === tab.key ? 'bg-[#2c3e50] text-white' : 'text-gray-500 hover:bg-gray-50']">
                            {{ tab.name }}
                        </button>
                    </div>

                    <div class="flex gap-3 w-full md:w-auto">
                        <div class="flex-1 md:w-64">
                            <SearchBar v-model="searchQuery" placeholder="搜索商品..." />
                        </div>
                        <button @click="isBatchMode = !isBatchMode"
                            :class="['px-4 py-2 rounded-xl text-sm font-bold transition-all border', isBatchMode ? 'bg-[#4a8b6e]/10 text-[#4a8b6e] border-[#4a8b6e]' : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50']">
                            {{ isBatchMode ? '完成' : '批量管理' }}
                        </button>
                    </div>
                </div>

                <!-- Batch Action Bar -->
                <div v-if="isBatchMode"
                    class="bg-[#2c3e50] text-white px-6 py-3 rounded-xl flex items-center justify-center shadow-lg animate-in slide-in-from-top-2 duration-200">
                    <span class="text-sm font-medium">已选 {{ selectedItems.length }} 件商品</span>
                </div>

                <!-- 3. Product List -->
                <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
                    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
                        <div class="space-y-4">
                            <div v-for="item in filteredItems" :key="item.id"
                                :class="['bg-white rounded-2xl p-5 border shadow-sm transition-all group item-card flex flex-col md:flex-row gap-6 relative', isBatchMode && selectedItems.includes(item.id) ? 'border-[#4a8b6e] bg-[#4a8b6e]/5' : 'border-gray-100 hover:border-[#4a8b6e]/30 hover:shadow-md']"
                                @click="toggleSelect(item.id)">

                                <!-- Batch Checkbox -->
                                <div v-if="isBatchMode" class="absolute top-5 left-5 z-10">
                                    <div
                                        :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center bg-white check-circle', selectedItems.includes(item.id) ? 'border-[#4a8b6e] bg-[#4a8b6e]' : 'border-gray-300']">
                                        <Check v-if="selectedItems.includes(item.id)" :size="12" class="text-white" />
                                    </div>
                                </div>

                                <!-- Image Section -->
                                <div class="flex gap-5 flex-1 pl-0 md:pl-0" :class="isBatchMode ? 'pl-8 md:pl-0' : ''">
                                    <div
                                        class="w-28 h-28 bg-gray-100 rounded-xl overflow-hidden border border-gray-100 relative group-hover:scale-[1.02] transition-transform flex-shrink-0">
                                        <img :src="item.image" class="w-full h-full object-cover" />
                                        <div v-if="item.status === 'INSPECTING'"
                                            class="absolute inset-0 bg-black/60 flex items-center justify-center">
                                            <span class="text-white text-xs font-bold">验货中</span>
                                        </div>
                                    </div>

                                    <div class="flex flex-col justify-between py-1 flex-1">
                                        <div>
                                            <div class="flex justify-between items-start">
                                                <h3
                                                    class="font-bold text-[#2c3e50] text-base mb-1 line-clamp-1 hover:text-[#4a8b6e] transition-colors cursor-pointer">
                                                    {{ item.title }}</h3>
                                                <button class="text-gray-400 hover:text-[#2c3e50] md:hidden">
                                                    <MoreHorizontal :size="20" />
                                                </button>
                                            </div>
                                            <div class="flex items-center gap-2 text-sm mb-3">
                                                <span class="font-bold text-[#ff5e57] text-lg">¥{{ item.price
                                                }}</span>
                                                <span v-if="item.originalPrice"
                                                    class="text-xs text-gray-400 line-through">¥{{
                                                        item.originalPrice }}</span>
                                            </div>
                                        </div>

                                        <!-- Data Tags -->
                                        <div v-if="item.type === 'product'"
                                            class="flex items-center gap-4 text-xs text-gray-500 bg-gray-50 w-fit px-3 py-1.5 rounded-lg">
                                            <span class="flex items-center gap-1.5" title="浏览量">
                                                <Eye :size="14" class="text-gray-400" /> {{ item.views }}
                                            </span>
                                            <div class="w-px h-3 bg-gray-300"></div>
                                            <span
                                                class="flex items-center gap-1.5 hover:text-[#4a8b6e] cursor-pointer transition-colors"
                                                title="留言">
                                                <MessageSquare :size="14" class="text-gray-400" /> {{ item.comments
                                                }}
                                            </span>
                                            <div class="w-px h-3 bg-gray-300"></div>
                                            <span class="flex items-center gap-1.5" title="想要">
                                                <Heart :size="14" class="text-gray-400" /> {{ item.likes }}
                                            </span>
                                        </div>
                                        <div v-else class="flex items-center gap-2">
                                            <span class="text-xs font-bold px-2 py-1 rounded bg-gray-100 text-gray-600">
                                                {{ getStatusText(item.status) }}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                <!-- Action Section (Desktop) -->
                                <div class="hidden md:flex flex-col justify-center gap-2 w-40 border-l border-gray-100 pl-6"
                                    @click.stop>

                                    <!-- Inbound Actions -->
                                    <template v-if="activeTab === 'inbound'">
                                        <button v-if="item.status === 'CREATED'" @click="handleShip(item)"
                                            class="w-full bg-[#4a8b6e] hover:bg-[#3b755b] text-white text-xs font-bold py-2 rounded-lg shadow-sm shadow-[#4a8b6e]/20 transition-all flex items-center justify-center gap-1">
                                            <Truck :size="12" /> 发货
                                        </button>
                                        <div v-if="item.trackingNo" class="text-xs text-gray-400 text-center truncate"
                                            :title="item.trackingNo">
                                            单号: {{ item.trackingNo }}
                                        </div>
                                        <div v-if="item.status === 'RECEIVED'"
                                            class="text-xs text-center text-green-600 font-bold">
                                            已入库
                                        </div>
                                    </template>

                                    <!-- Inspecting Actions -->
                                    <template v-else-if="activeTab === 'inspecting'">
                                        <div class="text-center text-xs text-gray-500">
                                            等待验货报告
                                        </div>
                                    </template>

                                    <!-- Sales Actions -->
                                    <template v-else-if="activeTab === 'sales'">
                                        <button @click="handlePolish(item)"
                                            class="w-full bg-[#4a8b6e] hover:bg-[#3b755b] text-white text-xs font-bold py-2 rounded-lg shadow-sm shadow-[#4a8b6e]/20 transition-all flex items-center justify-center gap-1">
                                            <RefreshCw :size="12" /> 擦亮
                                        </button>
                                        <button
                                            class="w-full border border-gray-200 hover:border-[#ff5e57] hover:text-[#ff5e57] text-gray-600 text-xs font-bold py-2 rounded-lg transition-colors flex items-center justify-center gap-1">
                                            <ArrowDown :size="12" /> 降价
                                        </button>
                                        <div class="flex justify-center gap-4 mt-1">
                                            <span class="text-xs text-gray-400 hover:text-[#2c3e50] cursor-pointer"
                                                @click="router.push({ name: 'PostCreate', query: { id: item.id } })">编辑</span>
                                            <span class="text-xs text-gray-400 hover:text-[#ff5e57] cursor-pointer"
                                                @click="toggleStatus(item)">下架</span>
                                        </div>
                                    </template>

                                    <!-- Off Actions -->
                                    <template v-else-if="activeTab === 'off'">
                                        <button
                                            v-if="item.status !== 'SOLD' && !isInspectionRejectedProduct(item.id)"
                                            @click="toggleStatus(item)"
                                            class="w-full bg-[#2c3e50] hover:bg-[#34495e] text-white text-xs font-bold py-2 rounded-lg shadow-sm transition-all">
                                            上架
                                        </button>
                                        <span v-if="item.status !== 'SOLD' && isInspectionRejectedProduct(item.id)"
                                            class="text-center text-xs text-red-500 py-2 bg-red-50 rounded-lg">
                                            验货驳回不可上架
                                        </span>
                                        <span v-if="item.status !== 'SOLD'"
                                            class="text-xs text-center text-gray-400 hover:text-[#ff5e57] cursor-pointer mt-2"
                                            @click="removeItem(item)">删除</span>
                                        <span v-if="item.status === 'SOLD'"
                                            class="text-center text-xs text-gray-400 py-2">
                                            交易完成
                                        </span>
                                    </template>
                                </div>

                            </div>
                        </div>
                    </van-list>
                </van-pull-refresh>

        </main>

    </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* 选中动画 */
.check-circle {
    transition: all 0.2s;
}

.item-card:hover .check-circle {
    border-color: #4a8b6e;
}
</style>
