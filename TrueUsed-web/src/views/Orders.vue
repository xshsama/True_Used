<script setup>
import { cancelOrder, confirmDelivery, getMyOrders, getOrderShipping } from '@/api/orders';
import SearchBar from '@/components/SearchBar.vue';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import {
    ChevronRight,
    Clock,
    Package,
    Truck
} from 'lucide-vue-next';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

// --- State ---
const activeTab = ref('全部');
const searchQuery = ref('');
const orders = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const nowTs = ref(Date.now());
let countdownTimer = null;
let shippingRefreshTimer = null;

const tabs = ['全部', '待付款', '待发货', '待收货', '售后/退款'];
const pendingShipStatuses = ['PAID', 'PENDING_SHIPMENT'];
const receiptReadyShippingStatuses = ['DELIVERING', 'DELIVERED'];

// --- Status Mapping ---
const statusMap = {
    '全部': 'all',
    '待付款': 'PENDING_PAYMENT',
    '待发货': 'pendingShipment',
    '待收货': 'SHIPPED',
    '售后/退款': 'afterSale'
};

const getTrade = (order) => normalizeProductTrade(order?.product || {});

const getActionErrorMessage = (error, fallback) => {
    return error?.response?.data?.message || error?.message || fallback;
};

const getShippingStatus = (order) => order?.shippingInfo?.shippingStatus || '';

const canConfirmReceipt = (order) => (
    order?.status === 'SHIPPED' &&
    receiptReadyShippingStatuses.includes(getShippingStatus(order))
);

const getConfirmDisabledReason = (order) => {
    if (order?.status !== 'SHIPPED') return '';
    const status = getShippingStatus(order);
    if (!status) return '物流信息同步中，请稍后再试';
    if (status === 'PENDING') return '包裹尚未揽收，暂不可确认收货';
    if (status === 'PICKED') return '包裹已揽收，暂未进入派送阶段';
    if (status === 'IN_TRANSIT') return '包裹运输中，暂不可确认收货';
    return '';
};

const applyShippingInfo = (order, shippingInfo) => {
    if (!order || !shippingInfo) return;
    order.shippingInfo = shippingInfo;
    order.trackingNumber = order.trackingNumber || shippingInfo.trackingNumber;
    order.expressCompany = order.expressCompany || shippingInfo.expressCompany;
    order.expressCode = order.expressCode || shippingInfo.expressCode;
    order.shippedAt = order.shippedAt || shippingInfo.shippedAt;
    order.estimatedDeliveryTime = shippingInfo.estimatedDeliveryTime || order.estimatedDeliveryTime;
};

const refreshOrderShipping = async (order) => {
    if (order?.status !== 'SHIPPED') return;
    try {
        const shippingInfo = await getOrderShipping(order.id);
        applyShippingInfo(order, shippingInfo);
    } catch (error) {
        console.error('Failed to fetch order shipping:', error);
    }
};

const refreshReceivableShipping = async () => {
    const receivableOrders = orders.value.filter(order => order.status === 'SHIPPED');
    await Promise.all(receivableOrders.map(refreshOrderShipping));
};

const getStatusText = (order) => {
    if (!order) return '';

    const trade = getTrade(order);
    if (order.status === 'PENDING_PAYMENT') return '待付款';
    if (pendingShipStatuses.includes(order.status)) {
        return trade.hasPlatformInspection ? '待平台出库' : '待发货';
    }
    if (order.status === 'SHIPPED') return '待收货';
    if (order.status === 'COMPLETED') return '已完成';
    if (order.status === 'CANCELLED') return '已取消';
    if (order.status === 'REFUNDING') return '售后中';
    if (order.status === 'REFUNDED') return '已退款';
    return order.status;
};

const getStatusColor = (status) => {
    switch (status) {
        case 'PENDING_PAYMENT': return 'text-[#ff5e57]';
        case 'SHIPPED': return 'text-[#4a8b6e]';
        case 'COMPLETED': return 'text-gray-500';
        case 'CANCELLED': return 'text-gray-400';
        case 'PAID': return 'text-[#1989fa]';
        case 'PENDING_SHIPMENT': return 'text-[#1989fa]';
        default: return 'text-gray-500';
    }
};

// --- Data Fetching ---
const loadOrders = async () => {
    loading.value = true;
    try {
        const res = await getMyOrders();
        // Ensure res is an array
        orders.value = Array.isArray(res) ? res : (res.content || []);
        await refreshReceivableShipping();
        finished.value = true;
    } catch (error) {
        console.error(error);
        showFailToast('加载订单失败');
    } finally {
        loading.value = false;
        refreshing.value = false;
    }
};

const onRefresh = () => {
    finished.value = false;
    loadOrders();
};

// --- Computed ---
const filteredOrders = computed(() => {
    let result = orders.value;

    // Tab Filtering
    if (activeTab.value !== '全部') {
        const targetStatus = statusMap[activeTab.value];
        if (targetStatus === 'afterSale') {
            const refundStatuses = ['REFUNDING', 'REFUNDED'];
            result = result.filter(o => refundStatuses.includes(o.status));
        } else if (targetStatus === 'pendingShipment') {
            result = result.filter(o => pendingShipStatuses.includes(o.status));
        } else {
            result = result.filter(o => o.status === targetStatus);
        }
    }

    // Search Filtering
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(o =>
            (o.product?.title && o.product.title.toLowerCase().includes(query)) ||
            (o.id && String(o.id).includes(query))
        );
    }

    return result;
});

// --- Actions ---
const pay = (order) => {
    router.push({ name: 'Payment', params: { id: order.id } });
};

const cancel = (order) => {
    showConfirmDialog({ title: '取消订单', message: '确定取消这个订单吗？' })
        .then(async () => {
            await cancelOrder(order.id);
            showSuccessToast('已取消');
            // Refresh or update local state
            order.status = 'CANCELLED';
        })
        .catch(() => { });
};

const confirm = (order) => {
    if (!canConfirmReceipt(order)) {
        showFailToast(getConfirmDisabledReason(order) || '当前物流状态暂不可确认收货');
        return;
    }
    showConfirmDialog({ title: '确认收货', message: '确认收到货物？' })
        .then(async () => {
            try {
                await confirmDelivery(order.id);
                showSuccessToast('已确认收货');
                order.status = 'COMPLETED';
            } catch (error) {
                showFailToast(getActionErrorMessage(error, '当前物流状态暂不可确认收货'));
            }
        })
        .catch(() => { });
};

const viewDetail = (order) => {
    router.push({ name: 'OrderDetail', params: { id: order.id } });
};

const viewLogistics = (order) => {
    router.push({ name: 'OrderDetail', params: { id: order.id }, query: { focus: 'logistics' } });
};

const viewRefundDetail = (order) => {
    router.push({ name: 'RefundDetail', params: { id: order.id } });
};

const applyRefund = (order) => {
    router.push({ name: 'RefundApply', params: { id: order.id } });
};

const review = (order) => {
    router.push({ path: '/review/create', query: { orderId: order.id } });
};

// --- Helpers ---
const getProductImage = (product) => {
    if (!product) return '';
    if (Array.isArray(product.images) && product.images.length > 0) {
        return product.images[0].url || product.images[0];
    }
    return product.images?.url || product.image || '';
};

const getRemainingTime = (createdAt) => {
    if (!createdAt) return '00:00';
    const created = new Date(createdAt).getTime();
    if (Number.isNaN(created)) return '00:00';
    const expireAt = created + 15 * 60 * 1000;
    const remainMs = Math.max(0, expireAt - nowTs.value);
    const minutes = Math.floor(remainMs / 60000).toString().padStart(2, '0');
    const seconds = Math.floor((remainMs % 60000) / 1000).toString().padStart(2, '0');
    return `${minutes}:${seconds}`;
};

const getLogisticsTitle = (order) => {
    if (pendingShipStatuses.includes(order.status)) {
        return getTrade(order).hasPlatformInspection ? '平台处理中' : '等待发货';
    }
    return '物流追踪';
};

const getLogisticsPreview = (order) => {
    const trade = getTrade(order);
    if (order.status === 'PENDING_SHIPMENT' && trade.hasPlatformInspection) {
        return '平台仓将在支付后自动出库，并生成一条模拟物流时间线';
    }
    if (order.status === 'PAID') {
        return '等待卖家录入快递信息';
    }
    if (order.status === 'SHIPPED') {
        const status = getShippingStatus(order);
        const events = Array.isArray(order.shippingInfo?.trackingEvents) ? order.shippingInfo.trackingEvents : [];
        const latestEvent = events.length > 0 ? events[events.length - 1] : null;
        if (latestEvent?.description) return latestEvent.description;
        if (status === 'PENDING') return '包裹尚未揽收，暂不可确认收货';
        if (status === 'PICKED') return '包裹已揽收，暂未进入派送阶段';
        if (status === 'IN_TRANSIT') return '包裹运输中，暂不可确认收货';
        if (status === 'DELIVERING') return '快件正在派送中，可确认收货';
        if (status === 'DELIVERED') return '快件已签收，可确认收货';
        return trade.hasPlatformInspection
            ? '平台已出库，点击查看完整物流轨迹'
            : '卖家已发货，点击查看完整物流轨迹';
    }
    return '物流节点同步中';
};

// --- Lifecycle ---
onMounted(() => {
    countdownTimer = setInterval(() => {
        nowTs.value = Date.now();
    }, 1000);

    loadOrders();
    shippingRefreshTimer = setInterval(() => {
        refreshReceivableShipping();
    }, 10000);

    // Handle query params for initial tab
    const statusQuery = route.query.status;
    if (statusQuery) {
        const map = {
            'unpaid': '待付款',
            'toship': '待发货',
            'toreceive': '待收货',
            'afterSale': '售后/退款'
        };
        if (map[statusQuery]) {
            activeTab.value = map[statusQuery];
        }
    }
});

onUnmounted(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
    if (shippingRefreshTimer) {
        clearInterval(shippingRefreshTimer);
        shippingRefreshTimer = null;
    }
});

</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- --- Header & Search --- -->
                <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <h1 class="text-2xl font-bold text-[#2c3e50]">我的订单</h1>
                    <div class="relative w-full md:w-80">
                        <SearchBar v-model="searchQuery" placeholder="搜索商品名称 / 订单号" />
                    </div>
                </div>

                <!-- --- Tabs (Sticky) --- -->
                <div class="sticky top-[118px] z-40 bg-[#f7f9fa]/95 backdrop-blur-sm pt-2 pb-4">
                    <div class="flex items-center gap-2 overflow-x-auto scrollbar-hide">
                        <button v-for="tab in tabs" :key="tab" @click="activeTab = tab" :class="[
                            'px-5 py-2 rounded-full text-sm font-bold whitespace-nowrap transition-all',
                            activeTab === tab
                                ? 'bg-[#2c3e50] text-white shadow-lg shadow-gray-200'
                                : 'bg-white text-gray-500 hover:text-[#4a8b6e] hover:bg-white/80'
                        ]">
                            {{ tab }}
                        </button>
                    </div>
                </div>

                <!-- --- Order List --- -->
                <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
                    <div class="space-y-4 min-h-[300px]">

                        <div v-for="order in filteredOrders" :key="order.id"
                            class="bg-white rounded-2xl p-5 border border-gray-100/50 shadow-sm hover:shadow-md transition-shadow group"
                            @click="viewDetail(order)">

                            <!-- Card Header -->
                            <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-50">
                                <div class="flex items-center gap-2">
                                    <!-- Seller Info (Mocked if missing) -->
                                    <img :src="resolveAvatar(order.seller?.avatarUrl, order.seller?.avatar)"
                                        class="w-6 h-6 rounded-full border border-gray-100" />
                                    <span class="font-bold text-sm text-gray-800">{{ order.seller?.username ||
                                        order.seller?.name || '卖家' }}</span>
                                    <span
                                        class="text-[10px] text-[#4a8b6e] bg-[#4a8b6e]/10 px-1.5 py-0.5 rounded font-medium">信用极好</span>
                                    <ChevronRight :size="14" class="text-gray-300" />
                                </div>
                                <div class="flex items-center gap-2">
                                    <span v-if="order.status === 'PENDING_PAYMENT'"
                                        class="text-xs text-[#ff5e57] flex items-center gap-1">
                                        <Clock :size="12" /> 剩余 {{ getRemainingTime(order.createdAt) }}
                                    </span>
                                    <span :class="['text-sm font-bold', getStatusColor(order.status)]">
                                        {{ getStatusText(order) }}
                                    </span>
                                </div>
                            </div>

                            <!-- Card Body -->
                            <div class="flex gap-4 cursor-pointer">
                                <div
                                    class="w-24 h-24 bg-gray-100 rounded-lg flex-shrink-0 overflow-hidden border border-gray-100 relative">
                                    <img :src="getProductImage(order.product)"
                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                                    <div v-if="order.status === 'CANCELLED'"
                                        class="absolute inset-0 bg-white/60 flex items-center justify-center">
                                        <span
                                            class="text-xs font-bold text-gray-500 bg-gray-200 px-2 py-1 rounded">已失效</span>
                                    </div>
                                </div>
                                <div class="flex-1 flex flex-col justify-between py-0.5">
                                    <div>
                                        <h3 class="font-bold text-[#2c3e50] line-clamp-2 leading-snug mb-2">{{
                                            order.product?.title }}</h3>
                                        <div class="flex flex-wrap gap-1.5">
                                            <span
                                                class="text-[10px] px-1.5 py-0.5 rounded font-medium"
                                                :class="getTrade(order).hasPlatformInspection ? 'text-[#4a8b6e] bg-[#4a8b6e]/8' : 'text-orange-600 bg-orange-50'">
                                                {{ getTrade(order).tradeModeLabel }}
                                            </span>
                                            <span v-for="tag in (order.product?.tags || [])" :key="tag"
                                                class="text-[10px] text-gray-500 bg-gray-50 px-1.5 py-0.5 rounded border border-gray-100">
                                                {{ tag }}
                                            </span>
                                        </div>
                                    </div>
                                    <div class="flex items-end justify-between">
                                        <span class="font-mono font-medium text-gray-400 text-xs">x 1</span>
                                        <span class="font-mono font-bold text-lg text-[#2c3e50]">¥{{ order.price
                                            }}</span>
                                    </div>
                                </div>
                            </div>

                            <!-- Logistics Info -->
                            <div v-if="order.status === 'SHIPPED' || pendingShipStatuses.includes(order.status)"
                                class="mt-4 bg-gray-50 rounded-lg p-3 flex items-start gap-3 text-xs text-gray-600">
                                <div class="text-[#4a8b6e] mt-0.5 flex-shrink-0">
                                    <Truck :size="14" />
                                </div>
                                <div class="flex-1">
                                    <p class="font-medium text-[#4a8b6e] mb-0.5">{{ getLogisticsTitle(order) }}</p>
                                    <p class="text-gray-500 line-clamp-1">{{ getLogisticsPreview(order) }}</p>
                                </div>
                                <ChevronRight :size="12" class="text-gray-400 self-center" />
                            </div>

                            <!-- Card Footer -->
                            <div class="mt-4 pt-4 border-t border-gray-50 flex items-center justify-between"
                                @click.stop>
                                <div class="text-xs text-gray-500">
                                    实付 <span class="text-base font-bold text-[#2c3e50] font-mono">¥{{ order.price
                                        }}</span>
                                    <span v-if="order.freight > 0" class="ml-1 text-[10px]">(含运费 ¥{{ order.freight
                                    }})</span>
                                </div>

                                <div class="flex items-center gap-3">
                                    <template v-if="order.status === 'PENDING_PAYMENT'">
                                        <button @click="cancel(order)"
                                            class="px-3 py-1.5 rounded-full border border-gray-200 text-xs text-gray-500 hover:border-gray-300 transition-colors">取消订单</button>
                                        <button @click="pay(order)"
                                            class="px-4 py-1.5 rounded-full bg-[#ff5e57] text-white text-xs font-bold shadow-md shadow-red-100 hover:bg-[#ff4742] transition-colors flex items-center gap-1">
                                            去支付
                                        </button>
                                    </template>

                                    <template v-if="order.status === 'SHIPPED'">
                                        <button @click="viewLogistics(order)"
                                            class="px-3 py-1.5 rounded-full border border-gray-200 text-xs text-gray-500 hover:border-gray-300 transition-colors">查看物流</button>
                                        <button
                                            @click="confirm(order)"
                                            :disabled="!canConfirmReceipt(order)"
                                            :title="getConfirmDisabledReason(order)"
                                            :class="[
                                                'px-4 py-1.5 rounded-full text-xs font-bold transition-colors',
                                                canConfirmReceipt(order)
                                                    ? 'bg-[#4a8b6e] text-white shadow-md shadow-emerald-100 hover:bg-[#3b755b]'
                                                    : 'cursor-not-allowed bg-gray-100 text-gray-400'
                                            ]">
                                            确认收货
                                        </button>
                                    </template>

                                    <template v-if="pendingShipStatuses.includes(order.status)">
                                        <button @click="applyRefund(order)"
                                            class="px-3 py-1.5 rounded-full border border-gray-200 text-xs text-gray-500 hover:border-gray-300 transition-colors">
                                            申请退款
                                        </button>
                                    </template>

                                    <template v-if="order.status === 'COMPLETED'">
                                        <button @click="applyRefund(order)"
                                            class="px-3 py-1.5 rounded-full border border-gray-200 text-xs text-gray-500 hover:border-gray-300 transition-colors">申请售后</button>
                                        <button @click="review(order)"
                                            class="px-3 py-1.5 rounded-full border border-[#4a8b6e] text-[#4a8b6e] text-xs font-medium hover:bg-[#4a8b6e]/5 transition-colors">
                                            评价
                                        </button>
                                    </template>

                                    <template v-if="['REFUNDING', 'REFUNDED'].includes(order.status)">
                                        <button @click="viewRefundDetail(order)"
                                            class="px-3 py-1.5 rounded-full border border-[#4a8b6e]/25 bg-[#4a8b6e]/5 text-xs font-medium text-[#4a8b6e] hover:bg-[#4a8b6e]/10 transition-colors">
                                            {{ order.status === 'REFUNDING' ? '查看售后进度' : '查看退款结果' }}
                                        </button>
                                    </template>

                                    <template v-if="order.status === 'CANCELLED'">
                                        <button
                                            class="px-3 py-1.5 rounded-full border border-gray-200 text-xs text-gray-500 hover:border-gray-300 transition-colors">删除订单</button>
                                    </template>
                                </div>
                            </div>
                        </div>

                        <!-- Empty State -->
                        <div v-if="!loading && filteredOrders.length === 0" class="py-20 text-center">
                            <div
                                class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                                <Package :size="32" />
                            </div>
                            <p class="text-gray-400 text-sm">暂无相关订单</p>
                        </div>

                    </div>
                </van-pull-refresh>

                <p v-if="filteredOrders.length > 0" class="text-center text-xs text-gray-300 pt-4">没有更多订单了</p>
        </main>
    </div>
</template>

<style scoped>
/* 隐藏滚动条但保持功能 */
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
