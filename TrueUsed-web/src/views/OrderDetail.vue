<script setup>
import { cancelOrder, confirmDelivery, getOrderById, getOrderShipping, shipOrder } from '@/api/orders';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import { Check, Copy, FileCheck2, MapPin, PackageSearch, Store, Truck } from 'lucide-vue-next';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const order = ref(null);
const shippingInfo = ref(null);

const SHIPPING_STATUS_LABELS = {
    PENDING: '待揽收',
    PICKED: '已揽收',
    IN_TRANSIT: '运输中',
    DELIVERING: '派送中',
    DELIVERED: '已签收'
};

const currentUserId = computed(() => userStore.user?.id ?? null);
const isCurrentUserBuyer = computed(() => currentUserId.value && order.value?.buyer?.id === currentUserId.value);
const isCurrentUserSeller = computed(() => currentUserId.value && order.value?.seller?.id === currentUserId.value);
const trade = computed(() => normalizeProductTrade(order.value?.product || {}));
const isOfficialTrade = computed(() => trade.value.hasPlatformInspection);
const canApplyRefund = computed(() => order.value && ['PAID', 'PENDING_SHIPMENT', 'SHIPPED'].includes(order.value.status));
const canSellerShip = computed(() => isCurrentUserSeller.value && !isOfficialTrade.value && order.value?.status === 'PAID');
const productTags = computed(() => [
    trade.value.tradeModeLabel,
    trade.value.primaryConditionLabel,
    trade.value.secondaryConditionLabel
].filter(Boolean));

const latestTrackingEvent = computed(() => {
    const events = shippingInfo.value?.trackingEvents || [];
    return events.length ? events[events.length - 1] : null;
});

const logisticsTimeline = computed(() => {
    const events = shippingInfo.value?.trackingEvents || [];
    return [...events].sort((a, b) => new Date(b.time) - new Date(a.time));
});

const logisticsStatusText = computed(() => SHIPPING_STATUS_LABELS[shippingInfo.value?.shippingStatus] || '待更新');

const hasShippingTimeline = computed(() => logisticsTimeline.value.length > 0);
const showPendingPlatformDispatch = computed(() => isOfficialTrade.value && order.value?.status === 'PENDING_SHIPMENT');
const showShippingSection = computed(() => showPendingPlatformDispatch.value || hasShippingTimeline.value || order.value?.status === 'SHIPPED');

const canConfirmReceipt = computed(() => {
    if (!isCurrentUserBuyer.value || order.value?.status !== 'SHIPPED') {
        return false;
    }
    return ['DELIVERING', 'DELIVERED'].includes(shippingInfo.value?.shippingStatus);
});

const confirmDisabledReason = computed(() => {
    if (order.value?.status !== 'SHIPPED' || canConfirmReceipt.value) {
        return '';
    }
    if (!shippingInfo.value) {
        return '物流信息同步中，请稍后再试';
    }
    if (shippingInfo.value.shippingStatus === 'PENDING') {
        return '包裹尚未揽收，暂不可确认收货';
    }
    if (shippingInfo.value.shippingStatus === 'PICKED') {
        return '包裹已揽收，暂未进入派送阶段';
    }
    if (shippingInfo.value.shippingStatus === 'IN_TRANSIT') {
        return '包裹运输中，暂不可确认收货';
    }
    return '暂不可确认收货';
});

const statusText = computed(() => {
    if (!order.value) return '';
    if (order.value.status === 'PENDING_PAYMENT') return '待付款';
    if (order.value.status === 'PAID' || order.value.status === 'PENDING_SHIPMENT') {
        return isOfficialTrade.value ? '待平台出库' : '待发货';
    }
    if (order.value.status === 'SHIPPED') return '已发货';
    if (order.value.status === 'COMPLETED') return '已完成';
    if (order.value.status === 'CANCELLED') return '已取消';
    if (order.value.status === 'REFUNDING') return '售后中';
    if (order.value.status === 'REFUNDED') return '已退款';
    return order.value.status;
});

const statusDesc = computed(() => {
    if (!order.value) return '';
    if (order.value.status === 'PENDING_PAYMENT') {
        return '请尽快完成支付，超时后订单会自动取消';
    }
    if (order.value.status === 'PAID' || order.value.status === 'PENDING_SHIPMENT') {
        return isOfficialTrade.value
            ? '平台仓正在准备出库，生成物流单号后会进入模拟运输流程'
            : '等待卖家录入快递信息';
    }
    if (order.value.status === 'SHIPPED') {
        return latestTrackingEvent.value?.description || '物流轨迹已生成，请根据物流进度确认收货';
    }
    if (order.value.status === 'COMPLETED') {
        return '买家已确认收货，交易完成';
    }
    if (order.value.status === 'CANCELLED') {
        return '订单已取消';
    }
    if (order.value.status === 'REFUNDING') {
        return '退款申请处理中';
    }
    if (order.value.status === 'REFUNDED') {
        return '退款已完成';
    }
    return '';
});

const steps = computed(() => [
    { label: '买家付款', status: 'PENDING_PAYMENT' },
    { label: isOfficialTrade.value ? '平台出库' : '卖家发货', status: 'PAID' },
    { label: '确认收货', status: 'SHIPPED' },
    { label: '交易成功', status: 'COMPLETED' }
]);

const currentStep = computed(() => {
    if (!order.value) return 0;
    const status = order.value.status;
    if (['CANCELLED', 'REFUNDING', 'REFUNDED'].includes(status)) {
        return -1;
    }
    if (status === 'PENDING_PAYMENT') return 1;
    if (status === 'PAID' || status === 'PENDING_SHIPMENT') return 2;
    if (status === 'SHIPPED') return 3;
    if (status === 'COMPLETED') return 4;
    return 0;
});

const stepProgressWidth = computed(() => {
    if (currentStep.value <= 1 || steps.value.length <= 1) {
        return '0%';
    }
    return `${((currentStep.value - 1) / (steps.value.length - 1)) * 100}%`;
});

const productImage = computed(() => {
    const product = order.value?.product;
    if (!product) return '';
    if (Array.isArray(product.images) && product.images.length > 0) {
        return product.images[0].url || product.images[0];
    }
    return product.images?.url || product.image || '';
});

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const getActionErrorMessage = (error, fallback) => {
    return error?.response?.data?.message || error?.message || fallback;
};

const loadOrder = async () => {
    const orderId = route.params.id;
    try {
        loading.value = true;
        shippingInfo.value = null;
        order.value = await getOrderById(orderId);

        if (order.value?.trackingNumber) {
            try {
                shippingInfo.value = await getOrderShipping(orderId);
            } catch (error) {
                console.error('Shipping info error', error);
            }
        }
    } catch (error) {
        showFailToast('加载订单详情失败');
    } finally {
        loading.value = false;
    }
};

const handleCancel = async () => {
    if (!order.value) return;
    showConfirmDialog({ title: '确认取消订单', message: '您确定要取消此订单吗？' })
        .then(async () => {
            try {
                await cancelOrder(order.value.id);
                showSuccessToast('订单已取消');
                await loadOrder();
            } catch (error) {
                showFailToast(getActionErrorMessage(error, '取消订单失败'));
            }
        })
        .catch(() => { });
};

const handleShip = async () => {
    if (!order.value) return;
    showConfirmDialog({ title: '确认发货', message: '确认使用默认模拟物流发货吗？' })
        .then(async () => {
            try {
                await shipOrder(order.value.id);
                showSuccessToast('发货成功');
                await loadOrder();
            } catch (error) {
                showFailToast(getActionErrorMessage(error, '发货失败'));
            }
        })
        .catch(() => { });
};

const handleConfirmReceipt = async () => {
    if (!order.value) return;
    if (!canConfirmReceipt.value) {
        showFailToast(confirmDisabledReason.value || '暂不可确认收货');
        return;
    }
    showConfirmDialog({ title: '确认收货', message: '确认已经收到商品并完成验收吗？' })
        .then(async () => {
            try {
                await confirmDelivery(order.value.id);
                showSuccessToast('已确认收货');
                await loadOrder();
            } catch (error) {
                showFailToast(getActionErrorMessage(error, '确认收货失败'));
            }
        })
        .catch(() => { });
};

const viewRefundDetail = () => {
    if (!order.value) return;
    router.push({ name: 'RefundDetail', params: { id: order.value.id } });
};

const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(String(text ?? ''));
        showSuccessToast('复制成功');
    } catch (error) {
        showFailToast('复制失败');
    }
};

const viewInspectionReport = () => {
    if (!order.value) return;
    router.push({
        name: 'InspectionDetail',
        params: { id: order.value.id },
        query: { type: 'order' }
    });
};

onMounted(() => {
    loadOrder();
});
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <nav class="sticky top-0 z-50 border-b border-gray-100 bg-white/95 backdrop-blur-sm">
            <div class="mx-auto flex h-[76px] max-w-[1320px] items-center justify-between px-8">
                <div class="flex items-center gap-10">
                    <div class="flex cursor-pointer items-center gap-2" @click="router.push('/')">
                        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#4a8b6e] text-xl font-bold italic text-white shadow-sm">
                            T
                        </div>
                        <span class="text-2xl font-bold tracking-tight text-[#2c3e50]">TrueUsed<span class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="flex items-center gap-8 text-[15px] font-medium text-gray-500">
                        <a class="cursor-pointer transition-colors hover:text-[#4a8b6e]" @click="router.push('/')">首页</a>
                        <a class="cursor-pointer transition-colors hover:text-[#4a8b6e]" @click="router.push('/orders')">我的订单</a>
                        <span class="text-gray-300">/</span>
                        <span class="font-bold text-[#4a8b6e]">订单详情</span>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <button
                        class="rounded-full border border-[#4a8b6e] px-4 py-1.5 text-sm font-bold text-[#4a8b6e] transition-colors hover:bg-[#4a8b6e]/5"
                        @click="router.push('/service')">
                        联系客服
                    </button>
                    <div class="h-10 w-10 cursor-pointer overflow-hidden rounded-full border border-gray-100 bg-gray-200" @click="router.push('/profile')">
                        <img :src="resolveAvatar(userStore.user?.avatarUrl, userStore.user?.avatar)" class="h-full w-full object-cover">
                    </div>
                </div>
            </div>
        </nav>

        <main v-if="!loading && order" class="mx-auto max-w-[1320px] space-y-6 px-8 py-8">
            <section class="relative overflow-hidden rounded-[28px] bg-[#24333f] px-8 py-8 text-white shadow-[0_24px_80px_rgba(36,51,63,0.22)]">
                <div class="absolute -right-12 -top-16 h-56 w-56 rounded-full bg-[#4a8b6e]/15 blur-3xl"></div>
                <div class="absolute -bottom-20 left-1/3 h-56 w-56 rounded-full bg-white/5 blur-3xl"></div>

                <div class="relative z-10 grid grid-cols-[minmax(0,1fr)_520px] gap-8">
                    <div>
                        <div class="flex items-center gap-3">
                            <PackageSearch :size="28" class="text-[#7dd3a8]" />
                            <h1 class="text-3xl font-bold">{{ statusText }}</h1>
                        </div>
                        <p class="mt-3 max-w-2xl text-sm leading-6 text-gray-300">{{ statusDesc }}</p>

                        <div class="mt-5 flex flex-wrap gap-2">
                            <span :class="[
                                'rounded-full px-3 py-1 text-xs font-bold',
                                isOfficialTrade ? 'bg-[#4a8b6e]/15 text-[#8bd2b3]' : 'bg-white/10 text-white'
                            ]">
                                {{ trade.tradeModeLabel }}
                            </span>
                            <span class="rounded-full bg-white/10 px-3 py-1 text-xs font-bold text-white">
                                {{ trade.fulfillmentModeLabel }}
                            </span>
                            <span v-if="shippingInfo?.shippingStatus" class="rounded-full bg-white/10 px-3 py-1 text-xs font-bold text-white">
                                物流状态：{{ logisticsStatusText }}
                            </span>
                        </div>

                        <div class="mt-6 flex flex-wrap items-center gap-3">
                            <template v-if="isCurrentUserBuyer">
                                <button
                                    v-if="order.status === 'PENDING_PAYMENT'"
                                    class="rounded-full bg-[#4a8b6e] px-6 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#4a8b6e]/20 transition-colors hover:bg-[#3b755b]"
                                    @click="router.push({ name: 'Payment', params: { id: order.id } })">
                                    去支付
                                </button>
                                <button
                                    v-if="order.status === 'PENDING_PAYMENT'"
                                    class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20"
                                    @click="handleCancel">
                                    取消订单
                                </button>
                                <button
                                    v-if="order.status === 'SHIPPED'"
                                    :disabled="!canConfirmReceipt"
                                    :class="[
                                        'rounded-full px-6 py-2.5 text-sm font-bold transition-colors',
                                        canConfirmReceipt
                                            ? 'bg-[#4a8b6e] text-white shadow-lg shadow-[#4a8b6e]/20 hover:bg-[#3b755b]'
                                            : 'cursor-not-allowed bg-white/10 text-white/45'
                                    ]"
                                    @click="handleConfirmReceipt">
                                    确认收货
                                </button>
                                <button
                                    v-if="canApplyRefund"
                                    class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20"
                                    @click="router.push(`/order/${order.id}/refund-apply`)">
                                    申请退款
                                </button>
                                <button
                                    v-if="isOfficialTrade"
                                    class="flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20"
                                    @click="viewInspectionReport">
                                    <FileCheck2 :size="16" />
                                    查看验货报告
                                </button>
                                <button
                                    v-if="order.status === 'COMPLETED'"
                                    class="rounded-full bg-[#4a8b6e] px-6 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#4a8b6e]/20 transition-colors hover:bg-[#3b755b]"
                                    @click="router.push({ path: '/review/create', query: { orderId: order.id } })">
                                    评价商品
                                </button>
                                <button
                                    v-if="['REFUNDING', 'REFUNDED'].includes(order.status)"
                                    class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20"
                                    @click="viewRefundDetail">
                                    {{ order.status === 'REFUNDING' ? '查看售后进度' : '查看退款结果' }}
                                </button>
                            </template>

                            <template v-if="isCurrentUserSeller && canSellerShip">
                                <button
                                    class="rounded-full bg-[#4a8b6e] px-6 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#4a8b6e]/20 transition-colors hover:bg-[#3b755b]"
                                    @click="handleShip">
                                    确认发货
                                </button>
                            </template>

                            <template v-if="isCurrentUserSeller && ['REFUNDING', 'REFUNDED'].includes(order.status)">
                                <button
                                    class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white backdrop-blur-md transition-colors hover:bg-white/20"
                                    @click="viewRefundDetail">
                                    {{ order.status === 'REFUNDING' ? '处理售后' : '查看退款结果' }}
                                </button>
                            </template>
                        </div>

                        <p v-if="order.status === 'SHIPPED' && !canConfirmReceipt" class="mt-3 text-xs text-gray-400">
                            {{ confirmDisabledReason }}
                        </p>
                    </div>

                    <div v-if="currentStep > 0" class="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                        <div class="relative flex items-center justify-between">
                            <div class="absolute left-0 right-0 top-4 h-1 rounded-full bg-white/10"></div>
                            <div class="absolute left-0 top-4 h-1 rounded-full bg-[#4a8b6e] transition-all duration-700" :style="{ width: stepProgressWidth }"></div>

                            <div v-for="(step, index) in steps" :key="step.label" class="relative z-10 flex w-[104px] flex-col items-center gap-3 text-center">
                                <div :class="[
                                    'flex h-9 w-9 items-center justify-center rounded-full border-4',
                                    (index + 1) < currentStep
                                        ? 'border-[#24333f] bg-[#4a8b6e] text-white'
                                        : (index + 1) === currentStep
                                            ? 'h-11 w-11 border-[#4a8b6e] bg-[#f7f9fa] text-[#4a8b6e] shadow-[0_0_18px_rgba(74,139,110,0.45)]'
                                            : 'border-[#24333f] bg-white/10 text-gray-400'
                                ]">
                                    <Check v-if="(index + 1) < currentStep" :size="16" />
                                    <span v-else class="text-xs font-bold">{{ index + 1 }}</span>
                                </div>
                                <span :class="(index + 1) === currentStep ? 'text-sm font-bold text-white' : 'text-xs text-gray-400'">
                                    {{ step.label }}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div v-else class="flex items-center justify-center rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                        <div class="flex items-center gap-3 rounded-full border border-white/10 bg-white/10 px-5 py-3 text-sm font-bold text-white">
                            <span class="h-2.5 w-2.5 rounded-full bg-[#ff5e57]"></span>
                            当前订单处于取消/退款状态，主流程已终止
                        </div>
                    </div>
                </div>
            </section>

            <div class="grid grid-cols-[minmax(0,1fr)_360px] gap-6">
                <div class="space-y-6">
                    <section v-if="showShippingSection" class="rounded-3xl border border-gray-100/60 bg-white p-6 shadow-sm">
                        <div class="flex items-center justify-between">
                            <h2 class="flex items-center gap-2 text-lg font-bold text-[#2c3e50]">
                                <Truck :size="20" class="text-[#4a8b6e]" />
                                物流进度
                            </h2>
                            <span v-if="shippingInfo?.trackingNumber" class="rounded-full bg-[#f2f7f4] px-3 py-1 text-xs font-bold text-[#4a8b6e]">
                                {{ logisticsStatusText }}
                            </span>
                        </div>

                        <div v-if="showPendingPlatformDispatch" class="mt-5 rounded-2xl border border-dashed border-[#4a8b6e]/35 bg-[#f4faf7] p-5">
                            <div class="flex items-center gap-3">
                                <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#4a8b6e]/12 text-[#4a8b6e]">
                                    <Truck :size="22" />
                                </div>
                                <div>
                                    <p class="text-base font-bold text-[#2c3e50]">平台仓已接管履约</p>
                                    <p class="mt-1 text-sm leading-6 text-gray-600">当前订单会在支付后自动生成一条模拟出库物流，之后物流时间线将按 mock 节点推进，无需卖家手动发货。</p>
                                </div>
                            </div>
                        </div>

                        <template v-else>
                            <div class="mt-5 rounded-2xl border border-[#4a8b6e]/12 bg-[#f4faf7] p-5">
                                <div class="flex items-start justify-between gap-6">
                                    <div>
                                        <div class="flex items-center gap-3">
                                            <p class="text-base font-bold text-[#2c3e50]">
                                                {{ shippingInfo?.expressCompany || order.expressCompany || '物流单号生成中' }}
                                            </p>
                                            <span v-if="shippingInfo?.trackingNumber || order.trackingNumber" class="rounded-full bg-white px-2.5 py-1 font-mono text-xs text-gray-500">
                                                {{ shippingInfo?.trackingNumber || order.trackingNumber }}
                                            </span>
                                        </div>
                                        <p class="mt-2 text-sm font-medium text-[#4a8b6e]">{{ latestTrackingEvent?.description || '物流轨迹已生成，等待后续节点更新' }}</p>
                                        <p class="mt-1 text-xs text-gray-500">{{ latestTrackingEvent?.location || '节点位置待更新' }}</p>
                                    </div>
                                    <div class="text-right text-xs text-gray-400">
                                        <div>预计送达</div>
                                        <div class="mt-1 font-medium text-gray-600">{{ formatDate(shippingInfo?.estimatedDeliveryTime || order.estimatedDeliveryTime) || '待更新' }}</div>
                                    </div>
                                </div>
                            </div>

                            <div v-if="hasShippingTimeline" class="mt-5 space-y-4">
                                <div v-for="(event, index) in logisticsTimeline" :key="`${event.time}-${event.status}-${index}`" class="flex gap-4">
                                    <div class="flex flex-col items-center">
                                        <div :class="[
                                            'h-3.5 w-3.5 rounded-full',
                                            index === 0 ? 'bg-[#4a8b6e] ring-4 ring-[#4a8b6e]/12' : 'bg-gray-300'
                                        ]"></div>
                                        <div v-if="index !== logisticsTimeline.length - 1" class="mt-2 w-px flex-1 bg-gray-200"></div>
                                    </div>
                                    <div class="pb-4">
                                        <div class="flex items-center gap-3">
                                            <span :class="[
                                                'text-sm font-bold',
                                                index === 0 ? 'text-[#2c3e50]' : 'text-gray-500'
                                            ]">
                                                {{ SHIPPING_STATUS_LABELS[event.status] || event.status }}
                                            </span>
                                            <span class="text-xs text-gray-400">{{ formatDate(event.time) }}</span>
                                        </div>
                                        <p :class="[
                                            'mt-1 text-sm leading-6',
                                            index === 0 ? 'text-[#2c3e50]' : 'text-gray-500'
                                        ]">
                                            {{ event.description }}
                                        </p>
                                        <p class="mt-1 text-xs text-gray-400">{{ event.location || '位置待更新' }}</p>
                                    </div>
                                </div>
                            </div>

                            <div v-else class="mt-5 rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">
                                物流已创建，正在同步首个节点。
                            </div>
                        </template>
                    </section>

                    <section class="overflow-hidden rounded-3xl border border-gray-100/60 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-gray-100 px-6 py-4">
                            <h2 class="text-lg font-bold text-[#2c3e50]">商品信息</h2>
                            <button v-if="isOfficialTrade" class="text-sm font-bold text-[#4a8b6e] hover:underline" @click="viewInspectionReport">
                                查看本单验货报告
                            </button>
                        </div>

                        <div class="grid grid-cols-[132px_minmax(0,1fr)_220px] gap-6 p-6">
                            <div class="overflow-hidden rounded-2xl border border-gray-100 bg-gray-100">
                                <img :src="productImage" class="h-[132px] w-[132px] object-cover">
                            </div>

                            <div>
                                <h3 class="text-xl font-bold leading-8 text-[#2c3e50]">{{ order.product?.title }}</h3>
                                <div class="mt-3 flex flex-wrap gap-2">
                                    <span v-for="tag in productTags" :key="tag" :class="[
                                        'rounded-full px-2.5 py-1 text-xs font-bold',
                                        tag === trade.tradeModeLabel
                                            ? (isOfficialTrade ? 'bg-[#4a8b6e]/10 text-[#4a8b6e]' : 'bg-orange-50 text-orange-600')
                                            : 'bg-gray-50 text-gray-500'
                                    ]">
                                        {{ tag }}
                                    </span>
                                </div>
                                <p class="mt-4 line-clamp-3 text-sm leading-7 text-gray-500">{{ order.product?.description || '暂无商品描述' }}</p>

                                <div class="mt-6 rounded-2xl border border-gray-100 bg-gray-50 px-4 py-4">
                                    <div class="flex items-center gap-3">
                                        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-gray-400 shadow-sm">
                                            <Store :size="18" />
                                        </div>
                                        <div>
                                            <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Seller</p>
                                            <p class="mt-1 text-sm font-bold text-[#2c3e50]">{{ order.seller?.username || '卖家' }}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="flex flex-col justify-between rounded-2xl border border-gray-100 bg-[#fcfcfc] p-5">
                                <div>
                                    <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Price</p>
                                    <p class="mt-2 text-3xl font-bold text-[#ff5e57]">¥{{ order.price }}</p>
                                </div>
                                <div class="space-y-3 text-sm text-gray-500">
                                    <div class="rounded-2xl border border-gray-100 bg-white px-4 py-3">
                                        <p class="font-bold text-[#2c3e50]">{{ isOfficialTrade ? '平台验货履约' : '卖家自主发货' }}</p>
                                        <p class="mt-1 text-xs leading-6">{{ statusDesc }}</p>
                                    </div>
                                    <div class="rounded-2xl border border-gray-100 bg-white px-4 py-3">
                                        <p class="font-bold text-[#2c3e50]">收货方式</p>
                                        <p class="mt-1 text-xs">{{ trade.fulfillmentModeLabel }}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>
                </div>

                <div class="space-y-6">
                    <section class="relative overflow-hidden rounded-3xl border border-gray-100/60 bg-white p-6 shadow-sm">
                        <div class="absolute left-0 top-0 h-1 w-full bg-[linear-gradient(90deg,#4a8b6e_0%,#4a8b6e_50%,#ff5e57_50%,#ff5e57_100%)]"></div>
                        <h2 class="mb-4 text-base font-bold text-[#2c3e50]">收货信息</h2>
                        <div v-if="order.address" class="flex items-start gap-3">
                            <div class="mt-1 text-gray-400">
                                <MapPin :size="18" />
                            </div>
                            <div>
                                <div class="flex items-center gap-2">
                                    <span class="font-bold text-[#2c3e50]">{{ order.address.recipientName }}</span>
                                    <span class="text-sm text-gray-500">{{ order.address.phone }}</span>
                                </div>
                                <p class="mt-2 text-sm leading-7 text-gray-600">
                                    {{ order.address.province }} {{ order.address.city }} {{ order.address.district }}<br>
                                    {{ order.address.detailedAddress }}
                                </p>
                            </div>
                        </div>
                    </section>

                    <section class="rounded-3xl border border-gray-100/60 bg-white p-6 shadow-sm">
                        <h2 class="mb-4 text-base font-bold text-[#2c3e50]">订单明细</h2>
                        <div class="space-y-3 text-sm">
                            <div class="flex justify-between text-gray-600">
                                <span>商品总价</span>
                                <span>¥{{ order.price }}</span>
                            </div>
                            <div class="flex justify-between text-gray-600">
                                <span>运费</span>
                                <span>¥0.00</span>
                            </div>
                            <div class="flex justify-between text-[#4a8b6e]">
                                <span>优惠券</span>
                                <span>- ¥0.00</span>
                            </div>
                            <div class="mt-3 flex items-center justify-between border-t border-gray-100 pt-3">
                                <span class="font-bold text-[#2c3e50]">实付款</span>
                                <span class="text-xl font-bold text-[#ff5e57]">¥{{ order.price }}</span>
                            </div>
                        </div>
                    </section>

                    <section class="rounded-3xl bg-gray-50 p-6 text-xs text-gray-500">
                        <h2 class="mb-4 text-sm font-bold text-[#2c3e50]">订单元信息</h2>
                        <div class="space-y-3">
                            <div class="flex items-center justify-between gap-4">
                                <span>订单编号</span>
                                <button class="font-mono text-right transition-colors hover:text-[#2c3e50]" @click="copyToClipboard(order.id)">
                                    {{ order.id }}
                                    <Copy :size="10" class="ml-1 inline" />
                                </button>
                            </div>
                            <div class="flex items-center justify-between gap-4">
                                <span>创建时间</span>
                                <span>{{ formatDate(order.createdAt) }}</span>
                            </div>
                            <div v-if="order.paymentTime" class="flex items-center justify-between gap-4">
                                <span>付款时间</span>
                                <span>{{ formatDate(order.paymentTime) }}</span>
                            </div>
                            <div v-if="order.trackingNumber" class="flex items-center justify-between gap-4">
                                <span>物流单号</span>
                                <span class="font-mono">{{ order.trackingNumber }}</span>
                            </div>
                            <div v-if="order.shippedAt" class="flex items-center justify-between gap-4">
                                <span>发货时间</span>
                                <span>{{ formatDate(order.shippedAt) }}</span>
                            </div>
                            <div v-if="order.deliveredAt" class="flex items-center justify-between gap-4">
                                <span>确认收货时间</span>
                                <span>{{ formatDate(order.deliveredAt) }}</span>
                            </div>
                        </div>
                    </section>
                </div>
            </div>
        </main>

        <div v-else class="flex min-h-screen items-center justify-center">
            <div class="flex animate-pulse flex-col items-center">
                <div class="mb-4 h-12 w-12 rounded-full bg-gray-200"></div>
                <div class="h-4 w-32 rounded bg-gray-200"></div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.line-clamp-3 {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>
