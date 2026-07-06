<script setup>
import { getReceivedReviews } from '@/api/reviews';
import {
    MessageSquare
} from 'lucide-vue-next';
import { ImagePreview, showFailToast, showSuccessToast } from 'vant';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// --- State ---
const reviews = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const finished = ref(false);
const isInitialLoading = ref(true);
const page = ref(0);
const size = ref(10);

// --- Actions ---
const loadReviews = async () => {
    try {
        const res = await getReceivedReviews({ page: page.value, size: size.value });
        const newReviews = res.content || [];

        if (page.value === 0) {
            reviews.value = newReviews;
        } else {
            reviews.value.push(...newReviews);
        }

        page.value++;

        if (reviews.value.length >= res.totalElements) {
            finished.value = true;
        }
    } catch (error) {
        showFailToast('加载评价失败');
        finished.value = true;
    } finally {
        loading.value = false;
        isInitialLoading.value = false;
    }
};

const onRefresh = async () => {
    finished.value = false;
    loading.value = true;
    page.value = 0;
    await loadReviews();
    refreshing.value = false;
    showSuccessToast('已刷新');
};

const formatTime = (time) => {
    if (!time) return '';
    return new Date(time).toLocaleDateString();
};

const getRatingText = (rating) => {
    const texts = ['极差', '失望', '一般', '满意', '惊喜'];
    return texts[rating - 1] || '';
};

const previewImage = (images, startPosition) => {
    ImagePreview({
        images,
        startPosition,
        closeable: true,
    });
};
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- Header -->
                <div class="flex items-center justify-between">
                    <div>
                        <h1 class="text-2xl font-bold text-[#2c3e50]">评价管理</h1>
                        <p class="text-xs text-gray-400 mt-1">查看买家对您商品的评价</p>
                    </div>
                </div>

                <!-- Review List -->
                <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
                    <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadReviews">

                        <div v-if="reviews.length === 0 && !loading"
                            class="flex flex-col items-center justify-center py-20 text-gray-400">
                            <MessageSquare :size="48" class="mb-4 opacity-20" />
                            <p>暂无评价数据</p>
                        </div>

                        <div class="space-y-4">
                            <div v-for="review in reviews" :key="review.id"
                                class="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm hover:shadow-md transition-all">

                                <!-- Product Header -->
                                <div class="flex items-center gap-4 mb-4 pb-4 border-b border-gray-50 cursor-pointer hover:bg-gray-50/50 -mx-6 px-6 transition-colors"
                                    @click="router.push(`/product/${review.productId}`)">
                                    <div
                                        class="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden border border-gray-100 flex-shrink-0">
                                        <img :src="review.productImage" class="w-full h-full object-cover" />
                                    </div>
                                    <div class="flex-1 min-w-0">
                                        <div class="font-bold text-sm text-[#2c3e50] truncate">{{ review.productTitle }}
                                        </div>
                                        <div class="text-xs text-gray-400 mt-0.5">{{ formatTime(review.createdAt) }}
                                        </div>
                                    </div>
                                </div>

                                <!-- Rating -->
                                <div class="flex items-center gap-2 mb-3">
                                    <van-rate v-model="review.rating" readonly size="14px" color="#ffd21e"
                                        void-icon="star" void-color="#eee" />
                                    <span class="text-xs font-bold text-gray-500">{{ getRatingText(review.rating)
                                        }}</span>
                                </div>

                                <!-- Content -->
                                <div class="text-sm text-gray-700 leading-relaxed mb-4">
                                    {{ review.content }}
                                </div>

                                <!-- Review Images -->
                                <div v-if="review.images && review.images.length > 0"
                                    class="flex gap-2 mb-4 overflow-x-auto pb-2 scrollbar-hide">
                                    <div v-for="(img, idx) in review.images" :key="idx"
                                        class="w-20 h-20 rounded-lg overflow-hidden border border-gray-100 flex-shrink-0 cursor-zoom-in"
                                        @click="previewImage(review.images, idx)">
                                        <img :src="img" class="w-full h-full object-cover" />
                                    </div>
                                </div>

                                <!-- Reply -->
                                <div v-if="review.sellerReply" class="bg-gray-50 rounded-xl p-4 text-xs">
                                    <div class="font-bold text-gray-500 mb-1">卖家回复：</div>
                                    <div class="text-gray-600">{{ review.sellerReply }}</div>
                                </div>

                            </div>
                        </div>
                    </van-list>
                </van-pull-refresh>

        </main>

    </div>
</template>
