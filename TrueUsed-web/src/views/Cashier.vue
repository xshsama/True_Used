<template>
  <div class="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden">
      <!-- Header -->
      <div class="bg-[#2c3e50] p-6 text-white text-center">
        <h1 class="text-xl font-bold mb-2">收银台</h1>
        <div class="text-3xl font-mono font-bold">¥ {{ order ? order.price.toFixed(2) : '0.00' }}</div>
        <div class="text-sm text-white/60 mt-1">订单号: {{ orderId }}</div>
      </div>

      <!-- Payment Methods -->
      <div class="p-6 space-y-4">
        <div class="text-sm font-bold text-gray-500 mb-2">选择支付方式</div>
        
        <!-- Alipay -->
        <label class="flex items-center justify-between p-4 border border-gray-200 rounded-xl cursor-pointer hover:border-[#1677ff] transition-colors group">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-[#1677ff]/10 rounded-full flex items-center justify-center text-[#1677ff]">
              <i data-lucide="qr-code" width="20"></i>
            </div>
            <div>
              <div class="font-bold text-gray-800">支付宝支付</div>
              <div class="text-xs text-gray-400">推荐使用</div>
            </div>
          </div>
          <input type="radio" name="payment" value="alipay" v-model="paymentMethod" class="w-5 h-5 text-[#1677ff] focus:ring-[#1677ff]">
        </label>

        <!-- Wallet -->
        <label class="flex items-center justify-between p-4 border border-gray-200 rounded-xl cursor-pointer hover:border-[#4a8b6e] transition-colors group">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-[#4a8b6e]/10 rounded-full flex items-center justify-center text-[#4a8b6e]">
              <i data-lucide="wallet" width="20"></i>
            </div>
            <div>
              <div class="font-bold text-gray-800">钱包余额支付</div>
              <div class="text-xs text-gray-400">余额: ¥ {{ wallet ? wallet.balance.toFixed(2) : '0.00' }}</div>
            </div>
          </div>
          <input type="radio" name="payment" value="wallet" v-model="paymentMethod" class="w-5 h-5 text-[#4a8b6e] focus:ring-[#4a8b6e]">
        </label>
      </div>

      <!-- Action Button -->
      <div class="p-6 pt-0">
        <button 
          @click="handlePay" 
          :disabled="loading"
          class="w-full bg-[#2c3e50] hover:bg-[#34495e] text-white font-bold py-3 rounded-xl transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <i v-if="loading" data-lucide="loader-2" class="animate-spin" width="20"></i>
          <span>{{ loading ? '处理中...' : '确认支付' }}</span>
        </button>
      </div>
    </div>

    <!-- Password Modal -->
    <div v-if="showPasswordModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl p-6 w-full max-w-sm">
        <h3 class="text-lg font-bold mb-4 text-center">请输入支付密码</h3>
        <div class="flex justify-center mb-6">
          <input 
            ref="passwordInput"
            v-model="password" 
            type="password" 
            maxlength="6" 
            class="w-32 text-center text-2xl tracking-[0.5em] border-b-2 border-gray-300 focus:border-[#4a8b6e] focus:outline-none py-2"
            placeholder="******"
          >
        </div>
        <div class="flex justify-end gap-3">
          <button @click="showPasswordModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg">取消</button>
          <button @click="confirmWalletPay" class="px-4 py-2 bg-[#4a8b6e] text-white rounded-lg hover:bg-[#3b755b]">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getOrderById, payOrderByWallet } from '@/api/orders'
import { createPayment } from '@/api/payments'
import { getMyWallet } from '@/api/wallet'
import { createIcons, icons } from 'lucide'
import { nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const orderId = route.query.orderId
const order = ref(null)
const wallet = ref(null)
const paymentMethod = ref('alipay')
const loading = ref(false)
const showPasswordModal = ref(false)
const password = ref('')
const passwordInput = ref(null)

const fetchOrder = async () => {
  try {
    const res = await getOrderById(orderId)
    order.value = res
  } catch (error) {
    console.error('Failed to fetch order:', error)
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

const handlePay = async () => {
  if (paymentMethod.value === 'alipay') {
    loading.value = true
    try {
      await createPayment({
        outTradeNo: orderId.toString(),
        totalAmount: order.value.price.toString(),
        subject: `TrueUsed Order ${orderId}`,
        body: `Payment for order ${orderId}`
      })
      // createPayment handles the redirection
    } catch (error) {
      console.error('Alipay failed:', error)
      loading.value = false
    }
  } else if (paymentMethod.value === 'wallet') {
    if (!wallet.value.hasPayPassword) {
      alert('请先设置支付密码')
      router.push('/wallet')
      return
    }
    if (wallet.value.balance < order.value.price) {
      alert('余额不足')
      return
    }
    showPasswordModal.value = true
    nextTick(() => {
      passwordInput.value?.focus()
    })
  }
}

const confirmWalletPay = async () => {
  if (!password.value || password.value.length !== 6) {
    alert('请输入6位支付密码')
    return
  }
  
  loading.value = true // Show loading on the main button (or modal button)
  // Actually we should probably show loading state on the modal button
  
  try {
    // Simulate delay
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    await payOrderByWallet(orderId, password.value)
    showPasswordModal.value = false
    router.push('/payment/success')
  } catch (error) {
    console.error('Wallet pay failed:', error)
    alert('支付失败: ' + (error.response?.data?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!orderId) {
    router.push('/')
    return
  }
  fetchOrder()
  fetchWallet()
  nextTick(() => {
    createIcons({ icons })
  })
})
</script>
