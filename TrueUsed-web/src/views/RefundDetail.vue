<script setup>
import { approveRefund, completeRefund, getOrderById, getRefundDetail, rejectRefund } from '@/api/orders';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import { Check, ClipboardList, RefreshCcw, ShieldCheck, X } from 'lucide-vue-next';
import { showConfirmDialog, showSuccessToast, showToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const orderId = route.params.id;
const order = ref(null);
const refundRequest = ref(null);
const loading = ref(true);

const REFUND_STATUS_LABELS = {
    PENDING: '待卖家处理',
    APPROVED: '已同意退款',
    REJECTED: '已拒绝',
    COMPLETED: '退款完成'
};

const REFUND_TYPE_LABELS = {
    REFUND_ONLY: '仅退款',
    RETURN_REFUND: '退货退款'
};

const currentUserId = computed(() => userStore.user?.id ?? null);
const isSeller = computed(() => currentUserId.value && order.value?.seller?.id === currentUserId.value);
const isBuyer = computed(() => currentUserId.value && order.value?.buyer?.id === currentUserId.value);
const trade = computed(() => normalizeProductTrade(order.value?.product || {}));
const refundStatusText = computed(() => REFUND_STATUS_LABELS[refundRequest.value?.status] || '售后处理中');
const refundTypeText = computed(() => REFUND_TYPE_LABELS[refundRequest.value?.refundType] || '退款');

const statusDescription = computed(() => {
    if (!refundRequest.value) return '';
    if (refundRequest.value.status === 'PENDING') {
        return isSeller.value ? '你可以在这里同意或拒绝这笔售后申请。' : '申请已提交，等待卖家处理。';
    }
    if (refundRequest.value.status === 'APPROVED') {
        return refundRequest.value.refundType === 'RETURN_REFUND'
            ? '退货物流仍为 mock，卖家确认回收后可手动完成退款。'
            : '卖家已同意退款，系统已进入退款完成阶段。';
    }
    if (refundRequest.value.status === 'REJECTED') {
        return '本次售后已被拒绝，你可以返回订单重新发起申请。';
    }
    if (refundRequest.value.status === 'COMPLETED') {
        return '退款流程已完成，资金已退回买家钱包。';
    }
    return '';
});

const canApproveOrReject = computed(() => isSeller.value && refundRequest.value?.status === 'PENDING');
const canCompleteRefund = computed(() => {
    return isSeller.value
        && refundRequest.value?.status === 'APPROVED'
        && refundRequest.value?.refundType === 'RETURN_REFUND'
        && order.value?.status === 'REFUNDING';
});
const canReapply = computed(() => isBuyer.value && refundRequest.value?.status === 'REJECTED');

const steps = computed(() => [
    { label: '买家申请', done: !!refundRequest.value },
    { label: '卖家处理', done: ['APPROVED', 'REJECTED', 'COMPLETED'].includes(refundRequest.value?.status) },
    { label: '退款完成', done: refundRequest.value?.status === 'COMPLETED' }
]);

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

const getErrorMessage = (error, fallback) => error?.response?.data?.message || error?.message || fallback;

const loadData = async () => {
    try {
        loading.value = true;
        const [orderRes, refundRes] = await Promise.all([
            getOrderById(orderId),
            getRefundDetail(orderId)
        ]);
        order.value = orderRes;
        refundRequest.value = refundRes;
    } catch (error) {
        showToast(getErrorMessage(error, '获取售后信息失败'));
    } finally {
        loading.value = false;
    }
};

const handleApprove = () => {
    showConfirmDialog({ title: '同意退款', message: '确认同意这笔退款申请吗？' })
        .then(async () => {
            try {
                await approveRefund(orderId);
                showSuccessToast('已同意退款');
                await loadData();
            } catch (error) {
                showToast(getErrorMessage(error, '操作失败'));
            }
        })
        .catch(() => { });
};

const handleReject = () => {
    showConfirmDialog({ title: '拒绝退款', message: '确认拒绝这笔退款申请吗？' })
        .then(async () => {
            try {
                await rejectRefund(orderId);
                showSuccessToast('已拒绝退款');
                await loadData();
            } catch (error) {
                showToast(getErrorMessage(error, '操作失败'));
            }
        })
        .catch(() => { });
};

const handleComplete = () => {
    showConfirmDialog({ title: '完成退款', message: '确认以 mock 方式完成本次退货退款吗？' })
        .then(async () => {
            try {
                await completeRefund(orderId);
                showSuccessToast('退款已完成');
                await loadData();
            } catch (error) {
                showToast(getErrorMessage(error, '完成退款失败'));
            }
        })
        .catch(() => { });
};

onMounted(() => {
    loadData();
});
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <nav class="sticky top-0 z-50 border-b border-gray-100 bg-white/95 backdrop-blur-sm">
            <div class="mx-auto flex h-[76px] max-w-[1320px] items-center justify-between px-8">
                <div class="flex items-center gap-10">
                    <div class="flex cursor-pointer items-center gap-2" @click="router.push('/')">
                        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#4a8b6e] text-xl font-bold italic text-white shadow-sm">T</div>
                        <span class="text-2xl font-bold tracking-tight text-[#2c3e50]">TrueUsed<span class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="flex items-center gap-8 text-[15px] font-medium text-gray-500">
                        <a class="cursor-pointer transition-colors hover:text-[#4a8b6e]" @click="router.push('/orders')">我的订单</a>
                        <span class="text-gray-300">/</span>
                        <span class="font-bold text-[#4a8b6e]">售后详情</span>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <button class="rounded-full border border-[#4a8b6e] px-4 py-1.5 text-sm font-bold text-[#4a8b6e] transition-colors hover:bg-[#4a8b6e]/5" @click="router.push(`/order/${orderId}`)">
                        返回订单
                    </button>
                    <div class="h-10 w-10 cursor-pointer overflow-hidden rounded-full border border-gray-100 bg-gray-200" @click="router.push('/profile')">
                        <img :src="resolveAvatar(userStore.user?.avatarUrl, userStore.user?.avatar)" class="h-full w-full object-cover">
                    </div>
                </div>
            </div>
        </nav>

        <main v-if="!loading && order && refundRequest" class="mx-auto max-w-[1320px] space-y-6 px-8 py-8">
            <section class="overflow-hidden rounded-[28px] bg-[#24333f] px-8 py-8 text-white shadow-[0_24px_80px_rgba(36,51,63,0.22)]">
                <div class="grid grid-cols-[minmax(0,1fr)_520px] gap-8">
                    <div>
                        <div class="flex items-center gap-3">
                            <ClipboardList :size="28" class="text-[#7dd3a8]" />
                            <h1 class="text-3xl font-bold">{{ refundStatusText }}</h1>
                        </div>
                        <p class="mt-3 max-w-2xl text-sm leading-6 text-gray-300">{{ statusDescription }}</p>

                        <div class="mt-5 flex flex-wrap gap-2">
                            <span class="rounded-full bg-[#4a8b6e]/15 px-3 py-1 text-xs font-bold text-[#8bd2b3]">{{ refundTypeText }}</span>
                            <span class="rounded-full bg-white/10 px-3 py-1 text-xs font-bold text-white">{{ trade.tradeModeLabel }}</span>
                            <span class="rounded-full bg-white/10 px-3 py-1 text-xs font-bold text-white">订单状态：{{ order.status }}</span>
                        </div>

                        <div class="mt-6 flex flex-wrap items-center gap-3">
                            <button v-if="canApproveOrReject" class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white transition-colors hover:bg-white/20" @click="handleReject">
                                拒绝退款
                            </button>
                            <button v-if="canApproveOrReject" class="rounded-full bg-[#4a8b6e] px-6 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#4a8b6e]/20 transition-colors hover:bg-[#3b755b]" @click="handleApprove">
                                同意退款
                            </button>
                            <button v-if="canCompleteRefund" class="rounded-full bg-[#4a8b6e] px-6 py-2.5 text-sm font-bold text-white shadow-lg shadow-[#4a8b6e]/20 transition-colors hover:bg-[#3b755b]" @click="handleComplete">
                                完成退款
                            </button>
                            <button v-if="canReapply" class="rounded-full border border-white/15 bg-white/10 px-6 py-2.5 text-sm font-bold text-white transition-colors hover:bg-white/20" @click="router.push(`/order/${orderId}/refund-apply`)">
                                重新申请
                            </button>
                        </div>
                    </div>

                    <div class="rounded-3xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm">
                        <div class="relative flex items-center justify-between">
                            <div class="absolute left-0 right-0 top-4 h-1 rounded-full bg-white/10"></div>
                            <div class="absolute left-0 top-4 h-1 rounded-full bg-[#4a8b6e] transition-all duration-700" :style="{ width: refundRequest.status === 'COMPLETED' ? '100%' : ['APPROVED', 'REJECTED'].includes(refundRequest.status) ? '50%' : '0%' }"></div>

                            <div v-for="(step, index) in steps" :key="step.label" class="relative z-10 flex w-[110px] flex-col items-center gap-3 text-center">
                                <div :class="[
                                    'flex h-10 w-10 items-center justify-center rounded-full border-4',
                                    step.done ? 'border-[#24333f] bg-[#4a8b6e] text-white' : 'border-[#24333f] bg-white/10 text-gray-400'
                                ]">
                                    <Check v-if="step.done" :size="16" />
                                    <span v-else class="text-xs font-bold">{{ index + 1 }}</span>
                                </div>
                                <span :class="step.done ? 'text-sm font-bold text-white' : 'text-xs text-gray-400'">{{ step.label }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div class="grid grid-cols-[minmax(0,1fr)_360px] gap-6">
                <section class="space-y-6">
                    <div class="overflow-hidden rounded-3xl border border-gray-100/60 bg-white shadow-sm">
                        <div class="border-b border-gray-100 px-6 py-4">
                            <h2 class="text-lg font-bold text-[#2c3e50]">售后信息</h2>
                        </div>
                        <div class="grid grid-cols-2 gap-6 p-6 text-sm">
                            <div class="rounded-2xl border border-gray-100 bg-gray-50 p-5">
                                <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Refund Amount</p>
                                <p class="mt-3 text-3xl font-bold text-[#ff5e57]">¥{{ refundRequest.refundAmount }}</p>
                            </div>
                            <div class="rounded-2xl border border-gray-100 bg-gray-50 p-5">
                                <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Last Update</p>
                                <p class="mt-3 text-base font-bold text-[#2c3e50]">{{ formatDate(refundRequest.updatedAt) }}</p>
                            </div>
                            <div class="col-span-2 rounded-2xl border border-gray-100 bg-white p-5">
                                <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Reason</p>
                                <p class="mt-3 text-sm leading-7 text-gray-600">{{ refundRequest.reason || '暂无退款原因' }}</p>
                            </div>
                            <div class="col-span-2 rounded-2xl border border-dashed border-[#ffb020]/35 bg-[#fff9ef] p-5 text-sm leading-7 text-[#8b6a1e]">
                                <div class="flex items-start gap-3">
                                    <RefreshCcw :size="18" class="mt-1 flex-shrink-0" />
                                    <div>
                                        <template v-if="refundRequest.refundType === 'RETURN_REFUND' && refundRequest.status === 'APPROVED'">
                                            当前版本未接入逆向物流。卖家确认 mock 回收后，可直接点击“完成退款”把售后闭环走完。
                                        </template>
                                        <template v-else>
                                            本页展示的是订单绑定的 mock 售后数据，后续订单状态与退款结果会同步更新。
                                        </template>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-gray-100/60 bg-white shadow-sm">
                        <div class="border-b border-gray-100 px-6 py-4">
                            <h2 class="text-lg font-bold text-[#2c3e50]">关联订单</h2>
                        </div>
                        <div class="grid grid-cols-[132px_minmax(0,1fr)] gap-6 p-6">
                            <div class="overflow-hidden rounded-2xl border border-gray-100 bg-gray-100">
                                <img :src="productImage" class="h-[132px] w-[132px] object-cover">
                            </div>
                            <div>
                                <h3 class="text-xl font-bold leading-8 text-[#2c3e50]">{{ order.product?.title }}</h3>
                                <div class="mt-3 flex flex-wrap gap-2">
                                    <span class="rounded-full bg-[#4a8b6e]/10 px-2.5 py-1 text-xs font-bold text-[#4a8b6e]">{{ trade.tradeModeLabel }}</span>
                                    <span class="rounded-full bg-gray-50 px-2.5 py-1 text-xs font-bold text-gray-500">订单号 {{ order.id }}</span>
                                </div>
                                <p class="mt-4 text-sm leading-7 text-gray-500">{{ order.product?.description || '暂无商品描述' }}</p>
                                <div class="mt-5 flex items-center gap-3">
                                    <button class="rounded-full border border-gray-200 px-5 py-2 text-sm font-bold text-gray-500 transition-colors hover:border-gray-300" @click="router.push(`/order/${orderId}`)">
                                        查看订单详情
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="space-y-6">
                    <div class="rounded-3xl border border-gray-100/60 bg-white p-6 shadow-sm">
                        <div class="flex items-start gap-3">
                            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#f4faf7] text-[#4a8b6e]">
                                <ShieldCheck :size="20" />
                            </div>
                            <div>
                                <h2 class="text-base font-bold text-[#2c3e50]">售后快照</h2>
                                <div class="mt-4 space-y-3 text-sm text-gray-500">
                                    <div class="flex justify-between gap-4">
                                        <span>申请时间</span>
                                        <span class="text-right text-[#2c3e50]">{{ formatDate(refundRequest.createdAt) }}</span>
                                    </div>
                                    <div class="flex justify-between gap-4">
                                        <span>售后类型</span>
                                        <span class="text-right text-[#2c3e50]">{{ refundTypeText }}</span>
                                    </div>
                                    <div class="flex justify-between gap-4">
                                        <span>当前状态</span>
                                        <span class="text-right text-[#2c3e50]">{{ refundStatusText }}</span>
                                    </div>
                                    <div class="flex justify-between gap-4">
                                        <span>订单状态</span>
                                        <span class="text-right text-[#2c3e50]">{{ order.status }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="rounded-3xl bg-gray-50 p-6 text-sm leading-7 text-gray-500">
                        <h2 class="text-base font-bold text-[#2c3e50]">当前角色说明</h2>
                        <p class="mt-3" v-if="isSeller">你当前以卖家身份查看，可处理待审批售后，并在“退货退款”场景中手动完成 mock 退款。</p>
                        <p class="mt-3" v-else-if="isBuyer">你当前以买家身份查看，可关注卖家处理结果；若被拒绝，可以重新申请一次。</p>
                        <p class="mt-3" v-else>当前账号不是这笔订单的直接参与方。</p>
                    </div>
                </section>
            </div>
        </main>
    </div>
</template>
