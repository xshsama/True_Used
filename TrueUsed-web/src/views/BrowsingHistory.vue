<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- Header Actions -->
                <div
                    class="flex items-center justify-between bg-white p-4 rounded-2xl border border-gray-100/50 shadow-sm">
                    <h1 class="text-lg font-bold text-[#2c3e50] flex items-center gap-2">
                        浏览记录
                        <span class="text-xs font-normal text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">{{
                            totalCount }} 条</span>
                    </h1>
                    <div class="flex gap-4">
                        <button
                            class="text-xs text-gray-500 hover:text-[#4a8b6e] flex items-center gap-1 transition-colors">
                            <PauseCircle :size="14" /> 暂停记录
                        </button>
                        <div class="w-px h-4 bg-gray-200"></div>
                        <button @click="clearHistory"
                            class="text-xs text-gray-500 hover:text-[#ff5e57] flex items-center gap-1 transition-colors">
                            <Trash2 :size="14" /> 清空
                        </button>
                    </div>
                </div>

                <!-- History Timeline -->
                <div class="space-y-8" v-if="totalCount > 0">

                    <!-- Group: Today -->
                    <section v-if="todayItems.length > 0">
                        <div class="flex items-center gap-2 mb-4">
                            <div class="w-2 h-2 rounded-full bg-[#4a8b6e]"></div>
                            <h2 class="font-bold text-base text-[#2c3e50]">今天</h2>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div v-for="item in todayItems" :key="item.id" @click="goToProduct(item.productId)"
                                :class="['bg-white rounded-xl border border-gray-100 overflow-hidden hover:shadow-md transition-all group relative cursor-pointer', item.status === 'sold' ? 'opacity-75' : 'hover:border-[#4a8b6e]/30']">

                                <!-- Delete Button (Visible on Hover) -->
                                <button @click.stop="deleteItem(item.id)"
                                    class="absolute top-2 right-2 z-10 bg-black/50 hover:bg-[#ff5e57] text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-all backdrop-blur-md">
                                    <X :size="12" />
                                </button>

                                <div
                                    :class="['aspect-square bg-gray-100 relative', item.status === 'sold' ? 'grayscale' : '']">
                                    <img :src="item.image"
                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />

                                    <!-- Sold Out Overlay -->
                                    <div v-if="item.status === 'sold'"
                                        class="absolute inset-0 bg-black/10 flex items-center justify-center">
                                        <span
                                            class="bg-black/60 text-white text-xs px-3 py-1 rounded-full font-bold backdrop-blur-md">已卖出</span>
                                    </div>

                                    <!-- Price Drop Badge -->
                                    <div v-if="item.priceDrop > 0 && item.status !== 'sold'"
                                        class="absolute bottom-2 left-2 bg-[#ff5e57] text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm flex items-center gap-1">
                                        <ArrowDown :size="10" /> 降 ¥{{ item.priceDrop }}
                                    </div>
                                </div>

                                <div class="p-3">
                                    <div class="flex justify-between items-start mb-1">
                                        <span
                                            :class="['font-bold', item.status === 'sold' ? 'text-gray-400' : 'text-[#ff5e57]']">¥{{
                                                item.price }}</span>
                                        <span class="text-[10px] text-gray-400">{{ item.time }}</span>
                                    </div>
                                    <h3
                                        :class="['text-sm line-clamp-2 mb-2 transition-colors', item.status === 'sold' ? 'text-gray-400' : 'text-[#2c3e50] group-hover:text-[#4a8b6e]']">
                                        {{ item.title }}
                                    </h3>

                                    <div class="flex items-center gap-2 pt-2 border-t border-gray-50">
                                        <img :src="item.sellerAvatar" class="w-5 h-5 rounded-full bg-gray-200">
                                        <span class="text-xs text-gray-500 truncate flex-1">{{ item.sellerName }}</span>
                                        <div v-if="item.isCollected" class="text-[#ff5e57]">
                                            <Heart :size="12" fill="currentColor" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Group: Yesterday -->
                    <section v-if="yesterdayItems.length > 0">
                        <div class="flex items-center gap-2 mb-4">
                            <div class="w-2 h-2 rounded-full bg-gray-300"></div>
                            <h2 class="font-bold text-base text-gray-500">昨天</h2>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div v-for="item in yesterdayItems" :key="item.id" @click="goToProduct(item.productId)"
                                :class="['bg-white rounded-xl border border-gray-100 overflow-hidden hover:shadow-md transition-all group relative cursor-pointer', item.status === 'sold' ? 'opacity-75' : 'hover:border-[#4a8b6e]/30']">

                                <button @click.stop="deleteItem(item.id)"
                                    class="absolute top-2 right-2 z-10 bg-black/50 hover:bg-[#ff5e57] text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-all backdrop-blur-md">
                                    <X :size="12" />
                                </button>

                                <div
                                    :class="['aspect-square bg-gray-100 relative', item.status === 'sold' ? 'grayscale' : '']">
                                    <img :src="item.image"
                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />

                                    <div v-if="item.status === 'sold'"
                                        class="absolute inset-0 bg-black/10 flex items-center justify-center">
                                        <span
                                            class="bg-black/60 text-white text-xs px-3 py-1 rounded-full font-bold backdrop-blur-md">已卖出</span>
                                    </div>

                                    <div v-if="item.priceDrop > 0 && item.status !== 'sold'"
                                        class="absolute bottom-2 left-2 bg-[#ff5e57] text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm flex items-center gap-1">
                                        <ArrowDown :size="10" /> 降 ¥{{ item.priceDrop }}
                                    </div>
                                </div>

                                <div class="p-3">
                                    <div class="flex justify-between items-start mb-1">
                                        <span
                                            :class="['font-bold', item.status === 'sold' ? 'text-gray-400' : 'text-[#ff5e57]']">¥{{
                                                item.price }}</span>
                                        <span class="text-[10px] text-gray-400">{{ item.time }}</span>
                                    </div>
                                    <h3
                                        :class="['text-sm line-clamp-2 mb-2 transition-colors', item.status === 'sold' ? 'text-gray-400' : 'text-[#2c3e50] group-hover:text-[#4a8b6e]']">
                                        {{ item.title }}
                                    </h3>

                                    <div class="flex items-center gap-2 pt-2 border-t border-gray-50">
                                        <img :src="item.sellerAvatar" class="w-5 h-5 rounded-full bg-gray-200">
                                        <span class="text-xs text-gray-500 truncate flex-1">{{ item.sellerName }}</span>
                                        <div v-if="item.isCollected" class="text-[#ff5e57]">
                                            <Heart :size="12" fill="currentColor" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Group: Earlier -->
                    <section v-if="earlierItems.length > 0">
                        <div class="flex items-center gap-2 mb-4">
                            <div class="w-2 h-2 rounded-full bg-gray-200"></div>
                            <h2 class="font-bold text-base text-gray-400">更早</h2>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div v-for="item in earlierItems" :key="item.id" @click="goToProduct(item.productId)"
                                :class="['bg-white rounded-xl border border-gray-100 overflow-hidden hover:shadow-md transition-all group relative cursor-pointer', item.status === 'sold' ? 'opacity-75' : 'hover:border-[#4a8b6e]/30']">

                                <button @click.stop="deleteItem(item.id)"
                                    class="absolute top-2 right-2 z-10 bg-black/50 hover:bg-[#ff5e57] text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-all backdrop-blur-md">
                                    <X :size="12" />
                                </button>

                                <div
                                    :class="['aspect-square bg-gray-100 relative', item.status === 'sold' ? 'grayscale' : '']">
                                    <img :src="item.image"
                                        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />

                                    <div v-if="item.status === 'sold'"
                                        class="absolute inset-0 bg-black/10 flex items-center justify-center">
                                        <span
                                            class="bg-black/60 text-white text-xs px-3 py-1 rounded-full font-bold backdrop-blur-md">已卖出</span>
                                    </div>

                                    <div v-if="item.priceDrop > 0 && item.status !== 'sold'"
                                        class="absolute bottom-2 left-2 bg-[#ff5e57] text-white text-[10px] font-bold px-2 py-1 rounded shadow-sm flex items-center gap-1">
                                        <ArrowDown :size="10" /> 降 ¥{{ item.priceDrop }}
                                    </div>
                                </div>

                                <div class="p-3">
                                    <div class="flex justify-between items-start mb-1">
                                        <span
                                            :class="['font-bold', item.status === 'sold' ? 'text-gray-400' : 'text-[#ff5e57]']">¥{{
                                                item.price }}</span>
                                        <span class="text-[10px] text-gray-400">{{ item.time }}</span>
                                    </div>
                                    <h3
                                        :class="['text-sm line-clamp-2 mb-2 transition-colors', item.status === 'sold' ? 'text-gray-400' : 'text-[#2c3e50] group-hover:text-[#4a8b6e]']">
                                        {{ item.title }}
                                    </h3>

                                    <div class="flex items-center gap-2 pt-2 border-t border-gray-50">
                                        <img :src="item.sellerAvatar" class="w-5 h-5 rounded-full bg-gray-200">
                                        <span class="text-xs text-gray-500 truncate flex-1">{{ item.sellerName }}</span>
                                        <div v-if="item.isCollected" class="text-[#ff5e57]">
                                            <Heart :size="12" fill="currentColor" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                </div>

                <!-- Empty State -->
                <div v-if="totalCount === 0" class="py-20 text-center">
                    <div
                        class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                        <History :size="40" />
                    </div>
                    <h3 class="font-bold text-gray-600 mb-1">暂无浏览记录</h3>
                    <p class="text-gray-400 text-sm mb-6">去首页逛逛，发现更多好物</p>
                    <button @click="router.push('/')"
                        class="bg-[#4a8b6e] text-white px-6 py-2 rounded-full text-sm font-bold shadow-lg hover:bg-[#3b755b] transition-colors">
                        去首页
                    </button>
                </div>

        </main>

    </div>
</template>

<script setup>
import { getBrowsingHistory } from '@/api/history';
import { useUserStore } from '@/stores/user';
import {
    ArrowDown,
    Heart,
    History,
    PauseCircle,
    Trash2,
    X
} from 'lucide-vue-next';
import { showConfirmDialog, showToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const userStore = useUserStore();

const historyData = ref([]);
const loading = ref(false);
const currentUserId = computed(() => userStore.user?.id);

const ensureCurrentUser = async () => {
    if (currentUserId.value) return;
    try {
        await userStore.loadMe();
    } catch (error) {
        console.error('Failed to load current user:', error);
    }
};

const fetchHistory = async () => {
    loading.value = true;
    try {
        const res = await getBrowsingHistory({ page: 0, size: 50 });
        // Process data
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        historyData.value = (res.content || [])
            .filter(item => {
                const product = item.product || {};
                return product.status === 'ON_SALE' && Number(product.seller?.id) !== Number(currentUserId.value);
            })
            .map(item => {
                const viewedDate = new Date(item.viewedAt);
                const viewedDateZero = new Date(viewedDate);
                viewedDateZero.setHours(0, 0, 0, 0);

                let group = 'earlier';
                if (viewedDateZero.getTime() === today.getTime()) {
                    group = 'today';
                } else if (viewedDateZero.getTime() === yesterday.getTime()) {
                    group = 'yesterday';
                }

                const product = item.product;
                const priceDrop = (product.originalPrice && product.originalPrice > product.price)
                    ? (product.originalPrice - product.price).toFixed(0)
                    : 0;

                return {
                    id: item.id, // history id
                    productId: product.id,
                    group,
                    title: product.title,
                    price: product.price,
                    priceDrop,
                    time: viewedDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
                    image: product.images && product.images.length > 0 ? product.images[0].url : '',
                    sellerName: product.seller ? product.seller.nickname : 'Unknown',
                    sellerAvatar: product.seller ? product.seller.avatarUrl : '',
                    isCollected: false, // TODO: check if collected
                    status: product.status === 'SOLD' ? 'sold' : 'active'
                };
            });
    } catch (error) {
        console.error('Failed to fetch history:', error);
        showToast('获取浏览记录失败');
    } finally {
        loading.value = false;
    }
};

onMounted(async () => {
    await ensureCurrentUser();
    fetchHistory();
});

const todayItems = computed(() => historyData.value.filter(i => i.group === 'today'));
const yesterdayItems = computed(() => historyData.value.filter(i => i.group === 'yesterday'));
const earlierItems = computed(() => historyData.value.filter(i => i.group === 'earlier'));

const totalCount = computed(() => historyData.value.length);

const deleteItem = (id) => {
    // TODO: Call API to delete
    historyData.value = historyData.value.filter(i => i.id !== id);
};

const clearHistory = () => {
    showConfirmDialog({
        title: '提示',
        message: '确定要清空所有浏览记录吗？'
    }).then(() => {
        // TODO: Call API to clear
        historyData.value = [];
    }).catch(() => {
        // cancel
    });
};

const goToProduct = (productId) => {
    router.push(`/product/${productId}`);
};

</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>
