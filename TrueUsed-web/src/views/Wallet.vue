<template>
    <div class="min-h-screen pb-12 bg-[#f7f9fa] font-sans text-[#2c3e50]">

        <!-- --- Top Navigation --- -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100">
            <div class="max-w-4xl mx-auto px-6 h-[72px] flex items-center justify-between gap-4">
                <div class="flex items-center gap-10">
                    <div class="flex items-center gap-1.5 cursor-pointer" @click="$router.push('/')">
                        <div
                            class="w-9 h-9 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-xl italic shadow-sm">
                            T</div>
                        <span class="text-2xl font-bold text-[#2c3e50] tracking-tight">TrueUsed<span
                                class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="hidden md:flex items-center gap-2 text-lg font-bold text-gray-700">
                        我的钱包
                    </div>
                </div>

                <div class="flex items-center gap-4">
                    <div class="text-xs text-gray-400 flex items-center gap-1">
                        <i data-lucide="shield-check" width="14" class="text-[#4a8b6e]"></i> 资金由银行存管
                    </div>
                    <div class="w-9 h-9 rounded-full bg-gray-200 overflow-hidden ml-2 border border-gray-100">
                        <img :src="resolveAvatar(userStore.userInfo?.avatarUrl, userStore.user?.avatarUrl, userStore.user?.avatar)"
                            class="w-full h-full object-cover" />
                    </div>
                </div>
            </div>
        </nav>

        <main class="max-w-4xl mx-auto px-6 py-8 space-y-8">

            <!-- 1. Asset Card (The "Black Card") -->
            <section
                class="bg-[#2c3e50] rounded-3xl p-8 text-white shadow-xl relative overflow-hidden h-64 flex flex-col justify-between">
                <!-- Decoration -->
                <div
                    class="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3 pointer-events-none">
                </div>
                <div class="card-shine absolute inset-0 pointer-events-none"></div>

                <!-- Top Row -->
                <div class="relative z-10 flex justify-between items-start">
                    <div>
                        <div class="text-white/60 text-sm mb-1 flex items-center gap-2">
                            总资产 (元)
                            <i v-if="!showBalance" @click="showBalance = true" data-lucide="eye-off" width="14"
                                class="cursor-pointer hover:text-white"></i>
                            <i v-else @click="showBalance = false" data-lucide="eye" width="14"
                                class="cursor-pointer hover:text-white"></i>
                        </div>
                        <div class="text-4xl font-bold font-mono tracking-tight">
                            {{ showBalance ? formatCurrency(wallet.balance + wallet.frozenAmount) : '****' }}
                        </div>
                    </div>
                    <div class="flex gap-3">
                        <button @click="handleWithdraw"
                            class="bg-white/10 hover:bg-white/20 text-white px-6 py-2 rounded-xl text-sm font-bold transition-all flex items-center gap-2 active:scale-95">
                            <i data-lucide="arrow-down-left" width="16"></i> 提现
                        </button>
                        <button @click="handleTopUp"
                            class="bg-[#4a8b6e] hover:bg-[#3b755b] text-white px-6 py-2 rounded-xl text-sm font-bold shadow-lg shadow-[#4a8b6e]/30 transition-all flex items-center gap-2 active:scale-95">
                            <i data-lucide="arrow-up-right" width="16"></i> 充值
                        </button>
                    </div>
                </div>

                <!-- Bottom Row -->
                <div class="relative z-10 flex gap-20 border-t border-white/10 pt-6">
                    <div>
                        <div class="text-white/60 text-xs mb-1">可用余额</div>
                        <div class="text-xl font-bold font-mono">{{ showBalance ? formatCurrency(wallet.balance) :
                            '****' }}</div>
                    </div>
                    <div>
                        <div class="text-white/60 text-xs mb-1">本月收益</div>
                        <div class="text-xl font-bold font-mono text-[#4a8b6e]">+{{ showBalance ? '0.00' : '****' }}
                        </div>
                    </div>
                </div>
            </section>

            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

                <!-- Left: Transactions List (2/3) -->
                <div class="lg:col-span-2 space-y-6">
                    <div class="flex items-center justify-between">
                        <h2 class="text-lg font-bold text-[#2c3e50]">收支明细</h2>
                        <div class="flex bg-white rounded-lg p-1 shadow-sm border border-gray-100">
                            <button v-for="tab in tabs" :key="tab" @click="activeTab = tab"
                                :class="['px-4 py-1.5 rounded-md text-xs font-bold transition-all', activeTab === tab ? 'bg-[#2c3e50] text-white' : 'text-gray-500 hover:text-gray-800']">
                                {{ tab }}
                            </button>
                        </div>
                    </div>

                    <div class="bg-white rounded-2xl border border-gray-100/50 shadow-sm overflow-hidden">
                        <div v-for="(item, index) in filteredTransactions" :key="item.id"
                            class="p-5 flex items-center justify-between hover:bg-gray-50 transition-colors border-b border-gray-50 last:border-0">
                            <div class="flex items-center gap-4">
                                <div
                                    :class="['w-10 h-10 rounded-full flex items-center justify-center', getIconBg(item.type)]">
                                    <i :data-lucide="getIcon(item.type)" width="18"
                                        :class="getIconColor(item.type)"></i>
                                </div>
                                <div>
                                    <div class="text-sm font-bold text-[#2c3e50] mb-0.5">{{ item.remark || item.type }}
                                    </div>
                                    <div class="text-xs text-gray-400">{{ formatDate(item.createdAt) }} · {{ item.status
                                        }}</div>
                                    <div v-if="item.bizNo || item.orderId" class="text-[10px] text-gray-300 mt-0.5">
                                        {{ formatSource(item) }}
                                    </div>
                                </div>
                            </div>
                            <div class="text-right">
                                <div
                                    :class="['text-base font-bold font-mono', isPositive(item) ? 'text-[#ff5e57]' : 'text-[#2c3e50]']">
                                    {{ isPositive(item) ? '+' : '' }}{{ formatCurrency(item.amount) }}
                                </div>
                                <!-- <div class="text-[10px] text-gray-400 mt-0.5">余额 {{ item.balance }}</div> -->
                            </div>
                        </div>

                        <!-- Empty State -->
                        <div v-if="filteredTransactions.length === 0" class="py-12 text-center">
                            <i data-lucide="file-text" width="32" class="text-gray-300 mx-auto mb-2"></i>
                            <p class="text-xs text-gray-400">暂无相关记录</p>
                        </div>
                    </div>
                </div>

                <!-- Right: Services & Ads (1/3) -->
                <div class="space-y-6">

                    <!-- Quick Services -->
                    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100/50">
                        <h3 class="text-sm font-bold text-[#2c3e50] mb-4">常用功能</h3>
                        <div class="grid grid-cols-2 gap-3">
                            <button @click="handleSetPassword"
                                class="flex flex-col items-center gap-2 p-3 rounded-xl border border-gray-50 hover:border-[#4a8b6e]/30 hover:bg-[#4a8b6e]/5 transition-all group">
                                <div
                                    class="bg-gray-100 p-2 rounded-full text-gray-500 group-hover:bg-white group-hover:text-[#4a8b6e] transition-colors">
                                    <i data-lucide="lock" width="18"></i>
                                </div>
                                <span class="text-xs font-medium text-gray-600">支付密码</span>
                            </button>
                            <button
                                class="flex flex-col items-center gap-2 p-3 rounded-xl border border-gray-50 hover:border-[#4a8b6e]/30 hover:bg-[#4a8b6e]/5 transition-all group">
                                <div
                                    class="bg-gray-100 p-2 rounded-full text-gray-500 group-hover:bg-white group-hover:text-[#4a8b6e] transition-colors">
                                    <i data-lucide="file-text" width="18"></i>
                                </div>
                                <span class="text-xs font-medium text-gray-600">账单下载</span>
                            </button>
                        </div>
                    </div>

                    <!-- Security Banner -->
                    <div
                        class="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl p-5 border border-blue-100 flex items-start gap-3">
                        <i data-lucide="shield-check" width="20" class="text-blue-600 mt-0.5 flex-shrink-0"></i>
                        <div>
                            <h4 class="text-xs font-bold text-blue-800 mb-1">资金安全保障中</h4>
                            <p class="text-[10px] text-blue-600/80 leading-relaxed">
                                您的资金由平安银行存管，交易全链路加密。未完成的交易资金将暂时冻结，确保买卖双方权益。
                            </p>
                        </div>
                    </div>

                </div>

            </div>

        </main>

        <!-- Top Up Modal -->
        <div v-if="showTopUpModal" class="wallet-modal-mask">
            <div class="wallet-modal-card">
                <div class="wallet-modal-head">
                    <h3>充值余额</h3>
                    <p>实时到账，支持幂等保护</p>
                </div>
                <div class="wallet-modal-body">
                    <label class="wallet-field-label">充值金额</label>
                    <div class="wallet-input-wrap">
                        <span>¥</span>
                        <input v-model="topUpAmount" type="number" placeholder="0.00">
                    </div>
                    <div class="wallet-quick-grid">
                        <button v-for="amount in quickTopUpAmounts" :key="amount" type="button"
                            @click="topUpAmount = String(amount)">¥{{ amount }}</button>
                    </div>
                </div>
                <div class="wallet-modal-actions">
                    <button type="button" class="btn-ghost" @click="showTopUpModal = false">取消</button>
                    <button type="button" class="btn-primary" @click="confirmTopUp" :disabled="submittingTopUp">
                        {{ submittingTopUp ? '处理中...' : '确认充值' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- Withdraw Modal -->
        <div v-if="showWithdrawModal" class="wallet-modal-mask">
            <div class="wallet-modal-card">
                <div class="wallet-modal-head">
                    <h3>申请提现</h3>
                    <p>将进入处理中，系统自动审核</p>
                </div>
                <div class="wallet-modal-body">
                    <label class="wallet-field-label">提现金额</label>
                    <div class="wallet-input-wrap">
                        <span>¥</span>
                        <input v-model="withdrawAmount" type="number" placeholder="0.00">
                    </div>
                    <div class="wallet-quick-grid">
                        <button v-for="amount in quickWithdrawAmounts" :key="amount" type="button"
                            @click="withdrawAmount = String(amount)">¥{{ amount }}</button>
                    </div>
                    <label class="wallet-field-label">支付密码</label>
                    <div class="wallet-input-wrap">
                        <span class="text-[13px]">•••</span>
                        <input v-model="withdrawPassword" type="password" placeholder="请输入支付密码">
                    </div>
                </div>
                <div class="wallet-modal-actions">
                    <button type="button" class="btn-ghost" @click="showWithdrawModal = false">取消</button>
                    <button type="button" class="btn-dark" @click="confirmWithdraw" :disabled="submittingWithdraw">
                        {{ submittingWithdraw ? '处理中...' : '确认提现' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- Set Password Modal -->
        <div v-if="showPasswordModal" class="wallet-modal-mask">
            <div class="wallet-modal-card">
                <div class="wallet-modal-head">
                    <h3>设置支付密码</h3>
                    <p>用于余额支付与提现校验，请妥善保管</p>
                </div>
                <div class="wallet-modal-body">
                    <label class="wallet-field-label">支付密码</label>
                    <div class="wallet-input-wrap">
                        <span class="text-[13px]">•••</span>
                        <input v-model="password" type="password" placeholder="请输入6位数字密码" maxlength="6">
                    </div>
                    <label class="wallet-field-label mt-3">确认密码</label>
                    <div class="wallet-input-wrap">
                        <span class="text-[13px]">•••</span>
                        <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" maxlength="6">
                    </div>
                </div>
                <div class="wallet-modal-actions">
                    <button type="button" class="btn-ghost" @click="showPasswordModal = false">取消</button>
                    <button type="button" class="btn-primary" @click="confirmSetPassword">确认设置</button>
                </div>
            </div>
        </div>

    </div>
</template>

<script setup>
import { getMyWallet, getTransactions, setPayPassword, topUp, withdraw } from '@/api/wallet'
import { useUserStore } from '@/stores/user'
import { resolveAvatar } from '@/utils/avatar'
import { createIcons, icons } from 'lucide'
import { showFailToast, showSuccessToast } from 'vant'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const showBalance = ref(true)
const activeTab = ref('全部')
const tabs = ['全部', '收入', '支出', '提现']
const wallet = ref({
    balance: 0,
    frozenAmount: 0,
    hasPayPassword: false
})
const transactions = ref([])
const showTopUpModal = ref(false)
const topUpAmount = ref('')
const quickTopUpAmounts = [50, 100, 200, 500]
const submittingTopUp = ref(false)
const showWithdrawModal = ref(false)
const withdrawAmount = ref('')
const quickWithdrawAmounts = [50, 100, 200, 500]
const withdrawPassword = ref('')
const submittingWithdraw = ref(false)
const showPasswordModal = ref(false)
const password = ref('')
const confirmPassword = ref('')
let walletPollingTimer = null

const handleVisibilityChange = () => {
    if (document.visibilityState === 'visible') {
        fetchWallet()
        fetchTransactions()
    }
}

const fetchWallet = async () => {
    try {
        const res = await getMyWallet()
        wallet.value = res
    } catch (error) {
        console.error('Failed to fetch wallet:', error)
    }
}

const fetchTransactions = async () => {
    try {
        const res = await getTransactions({ page: 0, size: 20 })
        if (Array.isArray(res)) {
            transactions.value = res
        } else if (Array.isArray(res?.content)) {
            transactions.value = res.content
        } else {
            transactions.value = []
        }
        refreshIcons()
    } catch (error) {
        console.error('Failed to fetch transactions:', error)
    }
}

const handleTopUp = () => {
    showTopUpModal.value = true
}

const handleWithdraw = () => {
    showWithdrawModal.value = true
}

const genBizNo = (prefix) => `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`

const resolveWalletErrorMessage = (error, fallback = '操作失败') => {
    const raw = error?.response?.data?.message || ''
    if (!error?.response) {
        const networkHint = error?.message || error?.code
        return networkHint
            ? `网络异常：${networkHint}（请检查后端服务和 /api 代理）`
            : '网络异常，请检查后端服务是否可用'
    }
    if (!raw) return fallback
    if (raw.includes('Invalid payment password')) return '支付密码错误'
    if (raw.includes('Insufficient balance')) return '钱包余额不足'
    if (raw.includes('Withdraw amount must be positive')) return '请输入正确的提现金额'
    return raw
}

const confirmTopUp = async () => {
    if (!topUpAmount.value || topUpAmount.value <= 0) {
        showFailToast('请输入正确的充值金额')
        return
    }
    if (submittingTopUp.value) return
    try {
        submittingTopUp.value = true
        await topUp({ amount: topUpAmount.value, bizNo: genBizNo('TOPUP') })
        showTopUpModal.value = false
        topUpAmount.value = ''
        showSuccessToast('充值成功')
        fetchWallet()
        fetchTransactions()
    } catch (error) {
        showFailToast(error?.response?.data?.message || '充值失败')
    } finally {
        submittingTopUp.value = false
    }
}

const confirmWithdraw = async () => {
    const amount = Number(withdrawAmount.value)
    if (!Number.isFinite(amount) || amount <= 0) {
        showFailToast('请输入正确的提现金额')
        return
    }
    if (!wallet.value?.hasPayPassword) {
        showFailToast('请先设置支付密码')
        showPasswordModal.value = true
        return
    }
    if (amount > Number(wallet.value?.balance || 0)) {
        showFailToast('钱包余额不足')
        return
    }
    if (!withdrawPassword.value) {
        showFailToast('请输入支付密码')
        return
    }
    if (!/^\d{6}$/.test(withdrawPassword.value)) {
        showFailToast('请输入6位数字支付密码')
        return
    }
    if (submittingWithdraw.value) return
    try {
        submittingWithdraw.value = true
        await withdraw({
            amount,
            password: withdrawPassword.value,
            bizNo: genBizNo('WD')
        })
        showWithdrawModal.value = false
        withdrawAmount.value = ''
        withdrawPassword.value = ''
        showSuccessToast('提现成功')
        fetchWallet()
        fetchTransactions()
    } catch (error) {
        showFailToast(resolveWalletErrorMessage(error, '提现失败'))
    } finally {
        submittingWithdraw.value = false
    }
}

const handleSetPassword = () => {
    showPasswordModal.value = true
}

const confirmSetPassword = async () => {
    if (!password.value || password.value.length !== 6) {
        alert('请输入6位数字密码')
        return
    }
    if (password.value !== confirmPassword.value) {
        showFailToast('两次输入的密码不一致')
        return
    }
    try {
        await setPayPassword({ password: password.value })
        showPasswordModal.value = false
        password.value = ''
        confirmPassword.value = ''
        fetchWallet()
        showSuccessToast('支付密码设置成功')
    } catch (error) {
        showFailToast(error?.response?.data?.message || '设置失败')
    }
}

const filteredTransactions = computed(() => {
    if (activeTab.value === '全部') return transactions.value
    if (activeTab.value === '收入') return transactions.value.filter(t => t.type === 'INCOME' || t.type === 'TOP_UP' || t.type === 'REFUND')
    if (activeTab.value === '支出') return transactions.value.filter(t => t.type === 'PAYMENT')
    if (activeTab.value === '提现') return transactions.value.filter(t => t.type === 'WITHDRAWAL')
    return transactions.value
})

const isPositive = (item) => {
    return ['INCOME', 'TOP_UP', 'REFUND'].includes(item.type)
}

const getIcon = (type) => {
    switch (type) {
        case 'INCOME':
        case 'TOP_UP':
        case 'REFUND':
            return 'arrow-down-left'
        case 'PAYMENT':
        case 'WITHDRAWAL':
            return 'arrow-up-right'
        default: return 'circle-dollar-sign'
    }
}

const getIconBg = (type) => {
    switch (type) {
        case 'INCOME':
        case 'TOP_UP':
        case 'REFUND':
            return 'bg-red-50'
        case 'PAYMENT':
        case 'WITHDRAWAL':
            return 'bg-gray-100'
        default: return 'bg-gray-50'
    }
}

const getIconColor = (type) => {
    switch (type) {
        case 'INCOME':
        case 'TOP_UP':
        case 'REFUND':
            return 'text-[#ff5e57]'
        case 'PAYMENT':
        case 'WITHDRAWAL':
            return 'text-[#2c3e50]'
        default: return 'text-gray-500'
    }
}

const formatCurrency = (value) => {
    return Number(value).toFixed(2)
}

const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleString()
}

const normalizeBizNo = (raw) => {
    if (!raw) return ''
    const bizNo = String(raw)
    return bizNo
        .replace(/^WITHDRAW:WITHDRAW_/i, 'WITHDRAW:')
        .replace(/^WITHDRAW:WD_/i, 'WITHDRAW:')
}

const formatSource = (item) => {
    const normalized = normalizeBizNo(item?.bizNo || '')
    if (normalized.startsWith('WITHDRAW:')) {
        return `提现单号：${normalized.replace(/^WITHDRAW:/, '')}`
    }
    if (normalized.startsWith('ORDER:')) {
        return `订单号：${normalized.replace(/^ORDER:/, '')}`
    }
    if (normalized) {
        return `来源：${normalized}`
    }
    if (item?.orderId) {
        return `订单号：${item.orderId}`
    }
    return ''
}

const refreshIcons = () => {
    nextTick(() => {
        createIcons({ icons })
    })
}

// Watch for changes in filteredTransactions to refresh icons
watch(filteredTransactions, () => {
    refreshIcons()
})

onMounted(() => {
    fetchWallet()
    fetchTransactions()
    refreshIcons()
    document.addEventListener('visibilitychange', handleVisibilityChange)
    walletPollingTimer = window.setInterval(() => {
        fetchWallet()
        fetchTransactions()
    }, 10000)
})

onUnmounted(() => {
    if (walletPollingTimer) {
        window.clearInterval(walletPollingTimer)
        walletPollingTimer = null
    }
    document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

/* 银行卡光泽效果 */
.card-shine {
    background: linear-gradient(105deg, transparent 20%, rgba(255, 255, 255, 0.1) 25%, transparent 30%);
    background-size: 200% 100%;
    animation: shine 3s infinite linear;
}

@keyframes shine {
    0% {
        background-position: 100% 0;
    }

    100% {
        background-position: -100% 0;
    }
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
    width: min(540px, calc(100vw - 32px));
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
    font-size: 22px;
    font-weight: 700;
    color: #2c3e50;
}

.wallet-input-wrap input::placeholder {
    color: #b0b9c7;
    font-weight: 600;
}

.wallet-quick-grid {
    margin: 12px 0 16px;
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
}

.wallet-quick-grid button {
    height: 36px;
    border-radius: 10px;
    border: 1px solid #dde5ef;
    background: #f8fafc;
    color: #3d4f66;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    transition: all .2s ease;
}

.wallet-quick-grid button:hover {
    border-color: #4a8b6e;
    color: #4a8b6e;
    background: #f1faf6;
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

.btn-dark {
    background: #2c3e50;
    color: #fff;
}

.btn-dark:hover {
    background: #1f2d3b;
}
</style>
