<script setup>
import { getOrderById, requestRefund } from '@/api/orders';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { normalizeProductTrade } from '@/utils/productTrade';
import { AlertCircle, FileText, Package, Wallet } from 'lucide-vue-next';
import { showSuccessToast, showToast } from 'vant';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const orderId = route.params.id;
const order = ref(null);
const submitting = ref(false);

const form = reactive({
    refundType: 'REFUND_ONLY',
    refundAmount: '',
    reason: ''
});

const trade = computed(() => normalizeProductTrade(order.value?.product || {}));
const productImage = computed(() => {
    const product = order.value?.product;
    if (!product) return '';
    if (Array.isArray(product.images) && product.images.length > 0) {
        return product.images[0].url || product.images[0];
    }
    return product.images?.url || product.image || '';
});

const canSubmit = computed(() => {
    return order.value && !['PENDING_PAYMENT', 'CANCELLED', 'REFUNDED', 'REFUNDING'].includes(order.value.status);
});

const amountError = computed(() => {
    if (!form.refundAmount || !order.value) return '';
    const amount = Number(form.refundAmount);
    const max = Number(order.value.price);
    if (!Number.isFinite(amount) || amount <= 0) {
        return '退款金额必须大于 0';
    }
    if (amount > max) {
        return `退款金额不能超过 ¥${max}`;
    }
    return '';
});

const tradeNote = computed(() => {
    return trade.value.hasPlatformInspection
        ? '该订单属于平台验货履约，售后仍以模拟流程推进。'
        : '该订单属于卖家自出，售后处理结果会同步回订单状态。';
});

const loadOrder = async () => {
    try {
        const res = await getOrderById(orderId);
        order.value = res;
        form.refundAmount = String(res.price ?? '');

        if (['REFUNDING', 'REFUNDED'].includes(res.status)) {
            router.replace(`/order/${orderId}/refund-detail`);
            return;
        }
    } catch (error) {
        showToast('获取订单信息失败');
    }
};

const onSubmit = async () => {
    if (!order.value || submitting.value) return;
    if (!canSubmit.value) {
        showToast('当前订单状态暂不可申请售后');
        return;
    }
    if (amountError.value) {
        showToast(amountError.value);
        return;
    }
    if (!form.reason.trim()) {
        showToast('请输入退款原因');
        return;
    }

    submitting.value = true;
    try {
        await requestRefund(orderId, {
            refundType: form.refundType,
            refundAmount: Number(form.refundAmount),
            reason: form.reason.trim()
        });
        showSuccessToast('申请提交成功');
        router.replace(`/order/${orderId}/refund-detail`);
    } catch (error) {
        showToast(error?.response?.data?.message || error?.message || '提交失败');
    } finally {
        submitting.value = false;
    }
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
                        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-[#4a8b6e] text-xl font-bold italic text-white shadow-sm">T</div>
                        <span class="text-2xl font-bold tracking-tight text-[#2c3e50]">TrueUsed<span class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="flex items-center gap-8 text-[15px] font-medium text-gray-500">
                        <a class="cursor-pointer transition-colors hover:text-[#4a8b6e]" @click="router.push('/orders')">我的订单</a>
                        <span class="text-gray-300">/</span>
                        <span class="font-bold text-[#4a8b6e]">申请售后</span>
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

        <main v-if="order" class="mx-auto grid max-w-[1320px] grid-cols-[360px_minmax(0,1fr)] gap-6 px-8 py-8">
            <section class="space-y-6">
                <div class="overflow-hidden rounded-3xl border border-gray-100/60 bg-white shadow-sm">
                    <div class="border-b border-gray-100 px-6 py-4">
                        <h2 class="text-lg font-bold text-[#2c3e50]">订单商品</h2>
                    </div>
                    <div class="p-6">
                        <div class="overflow-hidden rounded-2xl border border-gray-100 bg-gray-100">
                            <img :src="productImage" class="h-[280px] w-full object-cover">
                        </div>
                        <h3 class="mt-5 text-lg font-bold leading-8 text-[#2c3e50]">{{ order.product?.title }}</h3>
                        <div class="mt-3 flex flex-wrap gap-2">
                            <span class="rounded-full bg-[#4a8b6e]/10 px-2.5 py-1 text-xs font-bold text-[#4a8b6e]">{{ trade.tradeModeLabel }}</span>
                            <span class="rounded-full bg-gray-50 px-2.5 py-1 text-xs font-bold text-gray-500">{{ trade.fulfillmentModeLabel }}</span>
                            <span class="rounded-full bg-gray-50 px-2.5 py-1 text-xs font-bold text-gray-500">订单状态：{{ order.status }}</span>
                        </div>
                        <p class="mt-4 text-sm leading-7 text-gray-500">{{ order.product?.description || '暂无商品描述' }}</p>
                    </div>
                </div>

                <div class="rounded-3xl border border-[#4a8b6e]/15 bg-[#f4faf7] p-6">
                    <div class="flex items-start gap-3">
                        <div class="mt-1 flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-[#4a8b6e] shadow-sm">
                            <Package :size="18" />
                        </div>
                        <div>
                            <p class="text-base font-bold text-[#2c3e50]">售后说明</p>
                            <p class="mt-2 text-sm leading-7 text-gray-600">{{ tradeNote }}</p>
                            <p class="mt-2 text-sm leading-7 text-gray-600">如果选择“退货退款”，当前版本不会模拟逆向物流，卖家会在退款详情页手动完成一次 mock 回收确认。</p>
                        </div>
                    </div>
                </div>
            </section>

            <section class="rounded-3xl border border-gray-100/60 bg-white p-8 shadow-sm">
                <div class="flex items-center justify-between border-b border-gray-100 pb-5">
                    <div>
                        <h1 class="text-2xl font-bold text-[#2c3e50]">发起退款申请</h1>
                        <p class="mt-2 text-sm text-gray-500">提交后订单会进入售后中状态，卖家可在售后详情页处理。</p>
                    </div>
                    <div class="rounded-2xl bg-[#f7f9fa] px-5 py-4 text-right">
                        <p class="text-xs uppercase tracking-[0.18em] text-gray-400">Refund Limit</p>
                        <p class="mt-2 text-2xl font-bold text-[#ff5e57]">¥{{ order.price }}</p>
                    </div>
                </div>

                <div class="mt-8 space-y-7">
                    <div>
                        <label class="block text-sm font-bold text-[#2c3e50]">售后类型</label>
                        <div class="mt-3 grid grid-cols-2 gap-4">
                            <button
                                :class="[
                                    'rounded-2xl border px-5 py-4 text-left transition-colors',
                                    form.refundType === 'REFUND_ONLY'
                                        ? 'border-[#4a8b6e] bg-[#f4faf7]'
                                        : 'border-gray-200 bg-white hover:border-[#4a8b6e]/35'
                                ]"
                                @click="form.refundType = 'REFUND_ONLY'">
                                <div class="flex items-center gap-3">
                                    <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-[#4a8b6e] shadow-sm">
                                        <Wallet :size="18" />
                                    </div>
                                    <div>
                                        <p class="font-bold text-[#2c3e50]">仅退款</p>
                                        <p class="mt-1 text-xs leading-6 text-gray-500">适合未发货或无需退回商品的场景</p>
                                    </div>
                                </div>
                            </button>
                            <button
                                :class="[
                                    'rounded-2xl border px-5 py-4 text-left transition-colors',
                                    form.refundType === 'RETURN_REFUND'
                                        ? 'border-[#4a8b6e] bg-[#f4faf7]'
                                        : 'border-gray-200 bg-white hover:border-[#4a8b6e]/35'
                                ]"
                                @click="form.refundType = 'RETURN_REFUND'">
                                <div class="flex items-center gap-3">
                                    <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-[#4a8b6e] shadow-sm">
                                        <FileText :size="18" />
                                    </div>
                                    <div>
                                        <p class="font-bold text-[#2c3e50]">退货退款</p>
                                        <p class="mt-1 text-xs leading-6 text-gray-500">当前以 mock 方式模拟卖家确认回收</p>
                                    </div>
                                </div>
                            </button>
                        </div>
                    </div>

                    <div>
                        <label class="block text-sm font-bold text-[#2c3e50]">退款金额</label>
                        <div class="mt-3 flex items-center gap-3 rounded-2xl border border-gray-200 bg-white px-4 py-4 focus-within:border-[#4a8b6e]">
                            <span class="text-lg font-bold text-[#ff5e57]">¥</span>
                            <input v-model="form.refundAmount" type="number" min="0" step="0.01" class="w-full border-0 bg-transparent text-lg font-bold text-[#2c3e50] outline-none" :placeholder="`最多可退 ${order.price}`">
                        </div>
                        <p class="mt-2 text-xs" :class="amountError ? 'text-[#ff5e57]' : 'text-gray-400'">
                            {{ amountError || `最多可退 ¥${order.price}` }}
                        </p>
                    </div>

                    <div>
                        <label class="block text-sm font-bold text-[#2c3e50]">退款原因</label>
                        <textarea v-model="form.reason" rows="6" class="mt-3 w-full rounded-2xl border border-gray-200 bg-white px-4 py-4 text-sm leading-7 text-[#2c3e50] outline-none transition-colors focus:border-[#4a8b6e]" placeholder="请描述问题，例如与验货报告不符、商品瑕疵超出预期、物流异常等"></textarea>
                    </div>

                    <div class="rounded-2xl border border-dashed border-[#ffb020]/35 bg-[#fff9ef] px-5 py-4 text-sm leading-7 text-[#8b6a1e]">
                        <div class="flex items-start gap-3">
                            <AlertCircle :size="18" class="mt-1 flex-shrink-0" />
                            <div>
                                退款申请提交后会立即把订单切换为“售后中”。如果卖家拒绝，你可以重新发起一次申请；如果卖家同意“退货退款”，当前版本会在售后详情页手动完成 mock 退款闭环。
                            </div>
                        </div>
                    </div>

                    <div class="flex items-center justify-end gap-3 border-t border-gray-100 pt-6">
                        <button class="rounded-full border border-gray-200 px-6 py-2.5 text-sm font-bold text-gray-500 transition-colors hover:border-gray-300" @click="router.push(`/order/${orderId}`)">
                            取消
                        </button>
                        <button
                            :disabled="submitting || !canSubmit"
                            :class="[
                                'rounded-full px-7 py-2.5 text-sm font-bold transition-colors',
                                submitting || !canSubmit
                                    ? 'cursor-not-allowed bg-gray-200 text-gray-400'
                                    : 'bg-[#4a8b6e] text-white shadow-lg shadow-[#4a8b6e]/20 hover:bg-[#3b755b]'
                            ]"
                            @click="onSubmit">
                            {{ submitting ? '提交中...' : '提交申请' }}
                        </button>
                    </div>
                </div>
            </section>
        </main>
    </div>
</template>
