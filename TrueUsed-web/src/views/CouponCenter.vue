<template>
    <div class="min-h-screen bg-[#f7f9fa] pb-12 font-sans text-[#2c3e50]">

        <!-- Navbar -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100">
            <div class="max-w-3xl mx-auto px-4 h-[60px] flex items-center justify-between">
                <div class="flex items-center gap-3 cursor-pointer" @click="router.back()">
                    <div class="i-lucide-arrow-left text-xl text-gray-600"></div>
                    <span class="font-bold text-lg">领券中心</span>
                </div>
            </div>
        </nav>

        <main class="max-w-3xl mx-auto px-4 py-6 space-y-6">

            <!-- Available Coupons -->
            <section>
                <div class="flex items-center gap-2 mb-4">
                    <div class="i-lucide-ticket text-[#e74c3c]"></div>
                    <h2 class="font-bold text-lg">可领取优惠券</h2>
                </div>

                <div v-if="loading" class="py-10 text-center text-gray-400">加载中...</div>

                <div v-else-if="availableCoupons.length === 0" class="py-10 text-center bg-white rounded-xl">
                    <div class="i-lucide-inbox text-4xl text-gray-300 mb-2 mx-auto"></div>
                    <p class="text-gray-400 text-sm">暂无可用优惠券</p>
                </div>

                <div v-else class="space-y-3">
                    <div v-for="coupon in availableCoupons" :key="coupon.id"
                        class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 flex relative">

                        <!-- Left: Amount -->
                        <div
                            class="w-28 bg-gradient-to-br from-[#ff7e5f] to-[#feb47b] text-white flex flex-col items-center justify-center p-2">
                            <div class="text-xs opacity-80">¥</div>
                            <div class="text-3xl font-bold leading-none">{{ coupon.discountAmount }}</div>
                            <div class="text-[10px] mt-1 opacity-90">满{{ coupon.minSpend }}可用</div>
                        </div>

                        <!-- Middle: Info -->
                        <div class="flex-1 p-4 flex flex-col justify-center">
                            <h3 class="font-bold text-[#2c3e50]">{{ coupon.title }}</h3>
                            <p class="text-xs text-gray-400 mt-1">{{ coupon.description }}</p>
                            <div class="text-[10px] text-gray-400 mt-2 bg-gray-50 inline-block px-2 py-0.5 rounded">
                                有效期 {{ coupon.validDays }} 天
                            </div>
                        </div>

                        <!-- Right: Action -->
                        <div class="flex items-center pr-4">
                            <button v-if="isClaimed(coupon.id)"
                                class="px-4 py-1.5 rounded-full text-xs font-bold bg-gray-100 text-gray-400 cursor-not-allowed"
                                disabled>
                                已领取
                            </button>
                            <button v-else @click="handleClaim(coupon.id)"
                                class="px-4 py-1.5 rounded-full text-xs font-bold bg-[#e74c3c] text-white hover:bg-[#c0392b] transition-colors shadow-md shadow-red-200"
                                :disabled="claiming === coupon.id">
                                {{ claiming === coupon.id ? '领取中...' : '立即领取' }}
                            </button>
                        </div>

                        <!-- Decorative Circles -->
                        <div class="absolute left-[6.5rem] -top-2 w-4 h-4 bg-[#f7f9fa] rounded-full"></div>
                        <div class="absolute left-[6.5rem] -bottom-2 w-4 h-4 bg-[#f7f9fa] rounded-full"></div>
                    </div>
                </div>
            </section>

            <!-- My Coupons -->
            <section class="pt-6 border-t border-gray-200">
                <div class="flex items-center gap-2 mb-4">
                    <div class="i-lucide-wallet text-[#4a8b6e]"></div>
                    <h2 class="font-bold text-lg">我的卡包</h2>
                </div>

                <div v-if="myCoupons.length === 0" class="py-10 text-center bg-white rounded-xl">
                    <p class="text-gray-400 text-sm">还没有领取优惠券哦</p>
                </div>

                <div v-else class="space-y-3">
                    <div v-for="uc in myCoupons" :key="uc.id"
                        class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 flex relative opacity-90">

                        <!-- Left: Amount -->
                        <div
                            class="w-28 bg-gradient-to-br from-[#4a8b6e] to-[#6abf9c] text-white flex flex-col items-center justify-center p-2">
                            <div class="text-xs opacity-80">¥</div>
                            <div class="text-3xl font-bold leading-none">{{ uc.coupon.discountAmount }}</div>
                            <div class="text-[10px] mt-1 opacity-90">满{{ uc.coupon.minSpend }}可用</div>
                        </div>

                        <!-- Middle: Info -->
                        <div class="flex-1 p-4 flex flex-col justify-center">
                            <h3 class="font-bold text-[#2c3e50]">{{ uc.coupon.title }}</h3>
                            <p class="text-xs text-gray-400 mt-1">{{ uc.coupon.description }}</p>
                            <div class="text-[10px] text-gray-400 mt-2">
                                有效期至: {{ new Date(uc.validUntil).toLocaleDateString() }}
                            </div>
                        </div>

                        <!-- Right: Status -->
                        <div class="flex items-center pr-4">
                            <span v-if="uc.isUsed"
                                class="text-xs font-bold text-gray-400 border border-gray-200 px-2 py-1 rounded bg-gray-50">
                                已使用
                            </span>
                            <button v-else @click="handleUse(uc.id)"
                                class="px-3 py-1 rounded text-xs font-bold border border-[#4a8b6e] text-[#4a8b6e] hover:bg-[#4a8b6e] hover:text-white transition-colors"
                                :disabled="using === uc.id">
                                {{ using === uc.id ? '使用中...' : '立即使用' }}
                            </button>
                        </div>

                        <!-- Decorative Circles -->
                        <div class="absolute left-[6.5rem] -top-2 w-4 h-4 bg-[#f7f9fa] rounded-full"></div>
                        <div class="absolute left-[6.5rem] -bottom-2 w-4 h-4 bg-[#f7f9fa] rounded-full"></div>
                    </div>
                </div>
            </section>

        </main>
    </div>
</template>

<script setup>
import { claimCoupon, getAvailableCoupons, getMyCoupons, useCoupon } from '@/api/coupon'
import { showFailToast, showSuccessToast } from 'vant'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const claiming = ref(null)
const using = ref(null)
const availableCoupons = ref([])
const myCoupons = ref([])

const fetchData = async () => {
    loading.value = true
    try {
        const [availRes, myRes] = await Promise.all([
            getAvailableCoupons(),
            getMyCoupons()
        ])
        availableCoupons.value = availRes || []
        myCoupons.value = myRes || []
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const isClaimed = (couponId) => {
    return myCoupons.value.some(uc => uc.coupon.id === couponId)
}

const handleClaim = async (couponId) => {
    claiming.value = couponId
    try {
        await claimCoupon(couponId)
        showSuccessToast('领取成功')
        await fetchData() // Refresh lists
    } catch (e) {
        showFailToast(e.message || '领取失败')
    } finally {
        claiming.value = null
    }
}

const handleUse = async (userCouponId) => {
    using.value = userCouponId
    try {
        await useCoupon(userCouponId)
        showSuccessToast('使用成功')
        await fetchData() // Refresh lists
    } catch (e) {
        showFailToast(e.message || '使用失败')
    } finally {
        using.value = null
    }
}

onMounted(() => {
    fetchData()
})
</script>
