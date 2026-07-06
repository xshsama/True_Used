<script setup>
import { getOrderById, payOrderByWallet } from '@/api/orders';
import { createPayment } from '@/api/payments';
import { getMyWallet } from '@/api/wallet';
import { normalizeProductTrade } from '@/utils/productTrade';
import { Check, Clock, Loader2, Lock, ShieldCheck, Wallet } from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// --- State ---
const loading = ref(true);
const order = ref(null);
const selectedMethod = ref('alipay');
const wallet = ref(null);
const isProcessing = ref(false);
const showSuccess = ref(false);
const showPasswordModal = ref(false);
const password = ref('');
const countdown = ref('15:00');
let timerInterval = null;

// --- Computed ---
const orderAmount = computed(() => {
    return order.value ? order.value.price.toFixed(2) : '0.00';
});

const orderTitle = computed(() => {
    return order.value ? `订单号：${order.value.id}` : '';
});

const trade = computed(() => normalizeProductTrade(order.value?.product || {}));
const paymentSuccessHint = computed(() => {
    if (trade.value.hasPlatformInspection) {
        return '平台仓将尽快安排出库，验货报告可在订单详情查看。';
    }
    return '卖家将尽快为您发货，请留意消息通知。';
});

// --- Methods ---
const startCountdown = (createdAt) => {
    const createdTime = new Date(createdAt).getTime();
    const maxTime = 15 * 60 * 1000; // 15 minutes

    const updateTimer = () => {
        const now = Date.now();
        const diff = now - createdTime;
        const remaining = maxTime - diff;

        if (remaining <= 0) {
            countdown.value = '00:00';
            clearInterval(timerInterval);
            // Handle timeout (optional: auto-cancel or show expired state)
        } else {
            const m = Math.floor(remaining / 60000).toString().padStart(2, '0');
            const s = Math.floor((remaining % 60000) / 1000).toString().padStart(2, '0');
            countdown.value = `${m}:${s}`;
        }
    };

    updateTimer(); // Initial run
    timerInterval = setInterval(updateTimer, 1000);
};

const loadOrder = async () => {
    try {
        loading.value = true;
        const orderId = route.params.id;
        if (!orderId) {
            // Handle missing ID
            return;
        }
        const res = await getOrderById(orderId);
        order.value = res;

        if (res.status === 'PENDING_PAYMENT') {
            startCountdown(res.createdAt);
        } else if (['PAID', 'PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED'].includes(res.status)) {
            // If already paid, redirect or show success
            showSuccess.value = true;
        }
    } catch (error) {
        console.error('Failed to load order', error);
    } finally {
        loading.value = false;
    }
};

const loadWallet = async () => {
    try {
        const res = await getMyWallet();
        wallet.value = res;
    } catch (error) {
        console.error('Failed to load wallet', error);
    }
};

const handlePayment = async () => {
    if (!order.value) return;

    try {
        if (selectedMethod.value === 'wallet') {
            if (!wallet.value?.hasPayPassword) {
                showFailToast('请先设置支付密码');
                router.push('/wallet');
                return;
            }
            if ((wallet.value?.balance || 0) < order.value.price) {
                showFailToast('钱包余额不足');
                return;
            }
            showPasswordModal.value = true;
            return;
        }

        isProcessing.value = true;
        if (selectedMethod.value === 'alipay') {
            await createPayment({
                outTradeNo: String(order.value.id),
                totalAmount: order.value.price.toFixed(2),
                subject: `TrueUsed Order ${order.value.id}`,
                body: 'TrueUsed Transaction'
            });
        }
    } catch (error) {
        console.error('Payment failed', error);
        showFailToast(error?.response?.data?.message || '支付失败');
    } finally {
        isProcessing.value = false;
    }
};

const confirmWalletPayment = async () => {
    if (!order.value) return;
    if (!password.value) {
        showFailToast('请输入支付密码');
        return;
    }

    isProcessing.value = true;
    try {
        await payOrderByWallet(order.value.id, password.value);
        showPasswordModal.value = false;
        password.value = '';
        showSuccessToast('支付成功');
        showSuccess.value = true;
    } catch (error) {
        console.error('Wallet payment failed', error);
        showFailToast(error?.response?.data?.message || '余额支付失败');
    } finally {
        isProcessing.value = false;
    }
};

const goToOrderDetail = () => {
    if (order.value) {
        router.replace(`/order/${order.value.id}`);
    } else {
        router.push('/');
    }
};

onMounted(() => {
    if (route.query.method === 'wallet') {
        selectedMethod.value = 'wallet';
    } else if (route.query.method === 'alipay') {
        selectedMethod.value = 'alipay';
    }
    loadOrder();
    loadWallet();
});

onUnmounted(() => {
    if (timerInterval) clearInterval(timerInterval);
});
</script>

<template>
    <div class="min-h-screen flex flex-col bg-[#f7f9fa] font-sans text-[#2c3e50]">

        <!-- --- Simple Header (Focus on Payment) --- -->
        <header class="bg-white border-b border-gray-100 py-4">
            <div class="max-w-5xl mx-auto px-4 flex items-center justify-between">
                <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
                    <div class="flex items-center gap-1.5">
                        <div
                            class="w-8 h-8 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-lg italic shadow-sm">
                            T</div>
                        <span class="text-xl font-bold text-[#2c3e50] tracking-tight">TrueUsed<span
                                class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="h-6 w-px bg-gray-200 mx-2"></div>
                    <span class="text-lg text-gray-600 font-medium">收银台</span>
                </div>

                <div
                    class="hidden md:flex items-center gap-2 text-sm text-[#4a8b6e] bg-[#4a8b6e]/10 px-3 py-1.5 rounded-full">
                    <ShieldCheck :size="16" />
                    <span class="font-bold">平台资金托管 · 确认收货后打款</span>
                </div>
            </div>
        </header>

        <!-- --- Main Content --- -->
        <main class="flex-1 flex items-start justify-center pt-12 px-4 pb-12" v-if="!loading && order">

            <div class="w-full max-w-lg space-y-6">

                <!-- 1. Order Summary Card -->
                <div
                    class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center relative overflow-hidden">
                    <!-- Top Stripe -->
                    <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-[#4a8b6e] to-[#3b755b]"></div>

                    <p class="text-gray-500 text-sm mb-2">支付剩余时间</p>
                    <div
                        class="inline-flex items-center gap-2 bg-orange-50 text-[#ff5e57] px-4 py-1 rounded-full text-sm font-bold font-mono mb-6">
                        <Clock :size="14" />
                        {{ countdown }}
                    </div>

                    <div class="flex flex-col items-center justify-center">
                        <span class="text-4xl font-bold text-[#2c3e50] tracking-tight mb-2">
                            <span class="text-2xl align-top mr-1">¥</span>{{ orderAmount }}
                        </span>
                        <p class="text-gray-400 text-sm">{{ orderTitle }}</p>
                    </div>

                    <!-- Divider -->
                    <div class="relative mt-8 mb-2">
                        <div class="absolute inset-0 flex items-center" aria-hidden="true">
                            <div class="w-full border-t border-gray-100"></div>
                        </div>
                        <div class="relative flex justify-center">
                            <span class="bg-white px-2 text-xs text-gray-400">选择支付方式</span>
                        </div>
                    </div>
                </div>

                <!-- 2. Payment Methods -->
                <div class="space-y-3">

                    <!-- Alipay -->
                    <div @click="selectedMethod = 'alipay'"
                        :class="['bg-white rounded-xl p-5 border-2 cursor-pointer transition-all flex items-center justify-between group', selectedMethod === 'alipay' ? 'payment-card-selected' : 'border-transparent hover:border-gray-100']">
                        <div class="flex items-center gap-4">
                            <div
                                class="w-10 h-10 rounded-lg bg-[#1677ff] flex items-center justify-center text-white shrink-0">
                                <span class="font-bold text-sm">支</span>
                            </div>
                            <div>
                                <div class="font-bold text-gray-800 flex items-center gap-2">
                                    支付宝
                                    <span
                                        class="bg-[#1677ff]/10 text-[#1677ff] text-[10px] px-1.5 py-0.5 rounded">推荐</span>
                                </div>
                                <div class="text-xs text-gray-400">数亿用户的选择，安全快捷</div>
                            </div>
                        </div>
                        <div
                            :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center radio-ring', selectedMethod === 'alipay' ? 'border-[#4a8b6e] bg-[#4a8b6e]' : 'border-gray-300']">
                            <Check v-if="selectedMethod === 'alipay'" :size="12" class="text-white" />
                        </div>
                    </div>

                    <!-- Wallet Pay -->
                    <div @click="selectedMethod = 'wallet'"
                        :class="['bg-white rounded-xl p-5 border-2 cursor-pointer transition-all flex items-center justify-between group', selectedMethod === 'wallet' ? 'payment-card-selected' : 'border-transparent hover:border-gray-100']">
                        <div class="flex items-center gap-4">
                            <div
                                class="w-10 h-10 rounded-lg bg-[#2c3e50] flex items-center justify-center text-white shrink-0">
                                <Wallet :size="20" />
                            </div>
                            <div>
                                <div class="font-bold text-gray-800">钱包余额支付</div>
                                <div class="text-xs text-gray-400">
                                    余额：¥{{ wallet ? Number(wallet.balance || 0).toFixed(2) : '0.00' }}
                                </div>
                            </div>
                        </div>
                        <div
                            :class="['w-5 h-5 rounded-full border-2 flex items-center justify-center radio-ring', selectedMethod === 'wallet' ? 'border-[#4a8b6e] bg-[#4a8b6e]' : 'border-gray-300']">
                            <Check v-if="selectedMethod === 'wallet'" :size="12" class="text-white" />
                        </div>
                    </div>

                </div>

                <!-- Pay Button -->
                <button @click="handlePayment" :disabled="isProcessing"
                    class="w-full bg-gradient-to-r from-[#4a8b6e] to-[#3b755b] hover:shadow-lg hover:shadow-[#4a8b6e]/30 text-white font-bold text-lg py-4 rounded-xl transition-all active:scale-[0.98] disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center gap-2 mt-8">
                    <Loader2 v-if="isProcessing" class="animate-spin" :size="20" />
                    <span v-else>立即支付 ¥{{ orderAmount }}</span>
                </button>

                <p class="text-center text-xs text-gray-400 mt-4 flex items-center justify-center gap-1">
                    <Lock :size="12" />
                    SSL加密传输，保障您的支付安全
                </p>

            </div>

        </main>

        <!-- Loading State -->
        <div v-else class="flex-1 flex items-center justify-center">
            <Loader2 class="animate-spin text-[#4a8b6e]" :size="32" />
        </div>

        <!-- --- Wallet Password Modal --- -->
        <div v-if="showPasswordModal" class="wallet-modal-mask">
            <div class="wallet-modal-card">
                <div class="wallet-modal-head">
                    <h3>输入支付密码</h3>
                    <p>本次将使用余额支付 ¥{{ orderAmount }}</p>
                </div>
                <div class="wallet-modal-body">
                    <label class="wallet-field-label">支付密码</label>
                    <div class="wallet-input-wrap">
                        <span class="text-[13px]">•••</span>
                        <input v-model="password" type="password" maxlength="20" placeholder="请输入支付密码">
                    </div>
                </div>
                <div class="wallet-modal-actions">
                    <button type="button" class="btn-ghost" @click="showPasswordModal = false">取消</button>
                    <button type="button" class="btn-primary" :disabled="isProcessing" @click="confirmWalletPayment">
                        {{ isProcessing ? '处理中...' : '确认支付' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- --- Success Modal --- -->
        <div v-if="showSuccess" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/40 backdrop-blur-sm transition-opacity"></div>
            <div
                class="bg-white rounded-2xl p-8 max-w-sm w-full relative z-10 text-center animate-[bounce_0.5s_ease-out]">
                <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <Check class="text-green-500 w-10 h-10" />
                </div>
                <h2 class="text-2xl font-bold text-gray-800 mb-2">支付成功!</h2>
                <p class="text-gray-500 mb-8">{{ paymentSuccessHint }}</p>
                <button @click="goToOrderDetail"
                    class="w-full bg-gray-100 text-gray-700 font-bold py-3 rounded-xl hover:bg-gray-200 transition-colors">
                    查看订单详情
                </button>
            </div>
        </div>

    </div>
</template>

<style scoped>
.radio-ring {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 选中态的动画 */
.payment-card-selected {
    border-color: #4a8b6e;
    background-color: rgba(74, 139, 110, 0.03);
    box-shadow: 0 4px 6px -1px rgba(74, 139, 110, 0.1), 0 2px 4px -1px rgba(74, 139, 110, 0.06);
}

.wallet-modal-mask {
    position: fixed;
    inset: 0;
    z-index: 50;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(11, 18, 32, 0.55);
    backdrop-filter: blur(2px);
}

.wallet-modal-card {
    width: min(520px, calc(100vw - 32px));
    border-radius: 22px;
    background: #fff;
    box-shadow: 0 24px 50px rgba(20, 31, 45, 0.28);
    border: 1px solid #eef2f7;
    overflow: hidden;
}

.wallet-modal-head {
    padding: 20px 24px 14px;
    background: linear-gradient(160deg, #f7fbf9 0%, #ffffff 70%);
    border-bottom: 1px solid #edf2f7;
}

.wallet-modal-head h3 {
    margin: 0;
    font-size: 30px;
    line-height: 1.1;
    color: #2c3e50;
    font-weight: 800;
}

.wallet-modal-head p {
    margin: 8px 0 0;
    font-size: 13px;
    color: #7a8598;
}

.wallet-modal-body {
    padding: 18px 24px 6px;
}

.wallet-field-label {
    display: block;
    margin-bottom: 8px;
    font-size: 13px;
    font-weight: 700;
    color: #617086;
}

.wallet-input-wrap {
    display: flex;
    align-items: center;
    border: 1.5px solid #d8e0ea;
    border-radius: 14px;
    padding: 0 14px;
    height: 56px;
    background: #fbfcff;
    transition: all .2s ease;
}

.wallet-input-wrap:focus-within {
    border-color: #4a8b6e;
    box-shadow: 0 0 0 4px rgba(74, 139, 110, .12);
    background: #fff;
}

.wallet-input-wrap span {
    font-weight: 700;
    color: #2c3e50;
    margin-right: 8px;
}

.wallet-input-wrap input {
    width: 100%;
    border: none;
    outline: none;
    background: transparent;
    font-size: 20px;
    font-weight: 700;
    color: #2c3e50;
}

.wallet-input-wrap input::placeholder {
    color: #b0b9c7;
    font-weight: 600;
}

.wallet-modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 16px 24px 22px;
}

.wallet-modal-actions button {
    min-width: 104px;
    height: 44px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    border: none;
    transition: all .2s ease;
}

.wallet-modal-actions button:disabled {
    opacity: .6;
    cursor: not-allowed;
}

.btn-ghost {
    background: #f4f6fa;
    color: #5f6d82;
}

.btn-ghost:hover {
    background: #eaf0f7;
}

.btn-primary {
    background: #4a8b6e;
    color: #fff;
}

.btn-primary:hover {
    background: #3b755b;
}
</style>
