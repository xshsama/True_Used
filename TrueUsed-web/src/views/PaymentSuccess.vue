<script setup>
import { getOrderById } from '@/api/orders';
import { normalizeProductTrade } from '@/utils/productTrade';
import { Check, Loader2, XCircle } from 'lucide-vue-next';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const orderId = ref(null);
const order = ref(null);
const loading = ref(true);
const status = ref('checking'); // checking, success, pending, failed
const errorMsg = ref('');
let pollInterval = null;
let pollCount = 0;
const MAX_POLL_COUNT = 10; // Poll for 20 seconds (2s interval)
const trade = computed(() => normalizeProductTrade(order.value?.product || {}));
const successHint = computed(() => {
    if (trade.value.hasPlatformInspection) {
        return '平台仓将尽快安排出库，验货报告可在订单详情中查看。';
    }
    return '卖家将尽快为您发货。';
});

const checkOrderStatus = async () => {
    try {
        if (!orderId.value) return;

        order.value = await getOrderById(orderId.value);

        if (['PAID', 'PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED'].includes(order.value.status)) {
            status.value = 'success';
            loading.value = false;
            stopPolling();
        } else if (order.value.status === 'PENDING_PAYMENT') {
            // Still pending, keep polling
            status.value = 'pending';
            pollCount++;
            if (pollCount >= MAX_POLL_COUNT) {
                stopPolling();
                loading.value = false;
                // Keep status as pending but stop loading to show "processing" message
            }
        } else {
            // Cancelled or other status
            status.value = 'failed';
            errorMsg.value = `订单状态异常: ${order.value.status}`;
            loading.value = false;
            stopPolling();
        }
    } catch (error) {
        console.error('Failed to check order status', error);
        // Don't stop polling on network error immediately, maybe retry
        pollCount++;
        if (pollCount >= MAX_POLL_COUNT) {
            status.value = 'failed';
            errorMsg.value = '无法获取订单状态，请稍后在订单列表中查看';
            loading.value = false;
            stopPolling();
        }
    }
};

const startPolling = () => {
    checkOrderStatus(); // Check immediately
    pollInterval = setInterval(checkOrderStatus, 2000);
};

const stopPolling = () => {
    if (pollInterval) {
        clearInterval(pollInterval);
        pollInterval = null;
    }
};

onMounted(() => {
    // Alipay returns out_trade_no in query params
    const outTradeNo = route.query.out_trade_no;
    if (outTradeNo) {
        orderId.value = outTradeNo;
        startPolling();
    } else {
        status.value = 'failed';
        errorMsg.value = '参数缺失';
        loading.value = false;
    }
});

onUnmounted(() => {
    stopPolling();
});

const goToOrderDetail = () => {
    if (orderId.value) {
        router.replace(`/order/${orderId.value}`);
    } else {
        router.replace('/');
    }
};

const retryCheck = () => {
    loading.value = true;
    pollCount = 0;
    status.value = 'checking';
    startPolling();
};
</script>

<template>
    <div class="min-h-screen flex items-center justify-center bg-[#f7f9fa] p-4">
        <div class="bg-white rounded-2xl p-8 max-w-sm w-full text-center shadow-lg">

            <!-- Loading / Checking -->
            <div v-if="loading || (status === 'pending' && loading)" class="py-8">
                <Loader2 class="animate-spin text-[#4a8b6e] w-12 h-12 mx-auto mb-4" />
                <h2 class="text-xl font-bold text-gray-800 mb-2">正在确认支付结果...</h2>
                <p class="text-gray-500 text-sm">请稍候，我们正在同步支付宝的支付状态</p>
            </div>

            <!-- Success -->
            <div v-else-if="status === 'success'">
                <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <Check class="text-green-500 w-10 h-10" />
                </div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">支付成功!</h2>
                <p class="text-gray-500 mb-8">订单 {{ orderId }} 支付完成。<br>{{ successHint }}</p>
                <button @click="goToOrderDetail"
                    class="w-full bg-[#4a8b6e] text-white font-bold py-3 rounded-xl hover:bg-[#3b755b] transition-colors">
                    查看订单详情
                </button>
            </div>

            <!-- Pending (Timeout) -->
            <div v-else-if="status === 'pending'">
                <div class="w-20 h-20 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <Loader2 class="text-orange-500 w-10 h-10" />
                </div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">支付处理中</h2>
                <p class="text-gray-500 mb-8">支付结果尚未同步，请稍后在订单详情中查看状态。<br>如果已扣款，请勿重复支付。</p>
                <div class="space-y-3">
                    <button @click="retryCheck"
                        class="w-full bg-white border-2 border-[#4a8b6e] text-[#4a8b6e] font-bold py-3 rounded-xl hover:bg-[#4a8b6e]/5 transition-colors">
                        刷新状态
                    </button>
                    <button @click="goToOrderDetail"
                        class="w-full bg-gray-100 text-gray-700 font-bold py-3 rounded-xl hover:bg-gray-200 transition-colors">
                        查看订单详情
                    </button>
                </div>
            </div>

            <!-- Failed -->
            <div v-else>
                <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <XCircle class="text-red-500 w-10 h-10" />
                </div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">状态获取失败</h2>
                <p class="text-gray-500 mb-8">{{ errorMsg }}</p>
                <button @click="goToOrderDetail"
                    class="w-full bg-gray-100 text-gray-700 font-bold py-3 rounded-xl hover:bg-gray-200 transition-colors">
                    返回订单详情
                </button>
            </div>

        </div>
    </div>
</template>
