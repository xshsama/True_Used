<template>
    <div class="min-h-screen bg-[#f7f9fa] pb-12 font-sans text-[#2c3e50]">
        <!-- --- Top Navigation --- -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100">
            <div class="max-w-5xl mx-auto px-6 h-[72px] flex items-center justify-between gap-4">
                <div class="flex items-center gap-10">
                    <div class="flex items-center gap-1.5 cursor-pointer" @click="$router.push('/')">
                        <div
                            class="w-9 h-9 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-xl italic shadow-sm">
                            T</div>
                        <span class="text-2xl font-bold text-[#2c3e50] tracking-tight">TrueUsed<span
                                class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="hidden md:flex items-center gap-2 text-lg font-bold text-gray-700">
                        评价订单
                    </div>
                </div>

                <div class="flex items-center gap-4">
                    <button @click="$router.back()" class="text-sm text-gray-500 hover:text-gray-800">
                        返回
                    </button>
                    <div class="w-9 h-9 rounded-full bg-gray-200 overflow-hidden ml-2 border border-gray-100">
                        <!-- User Avatar Placeholder or Real Avatar -->
                        <img :src="resolveAvatar()"
                            class="w-full h-full object-cover" />
                    </div>
                </div>
            </div>
        </nav>

        <main class="max-w-2xl mx-auto px-4 py-8" v-if="order">

            <!-- Product Card (Horizontal) -->
            <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 mb-6 flex gap-4 items-center">
                <img :src="getProductImage(order.product)" class="w-20 h-20 rounded-lg object-cover bg-gray-100" />
                <div class="flex-1">
                    <h2 class="font-bold text-[#2c3e50] text-sm mb-1 line-clamp-2">{{ order.product.title }}</h2>
                    <div class="text-xs text-gray-400">{{ order.product.condition || '二手' }}</div>
                </div>
                <div class="text-right">
                    <div class="font-bold text-lg text-[#2c3e50] font-mono">¥{{ order.price }}</div>
                    <div class="flex items-center justify-end gap-1.5 mt-1">
                        <img :src="resolveAvatar(order.seller.avatarUrl, order.seller.avatar)"
                            class="w-4 h-4 rounded-full bg-gray-200" />
                        <span class="text-xs text-gray-500">{{ order.seller.nickname || order.seller.username }}</span>
                    </div>
                </div>
            </div>

            <!-- Review Form -->
            <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">

                <!-- 1. Rating Header (Compact) -->
                <div class="px-8 py-5 border-b border-gray-50 flex items-center gap-6 bg-gray-50/30">
                    <span class="text-sm font-bold text-gray-700">整体评价</span>
                    <div class="flex items-center gap-1">
                        <Star v-for="i in 5" :key="i" @click="rating = i" @mouseenter="hoverRating = i"
                            @mouseleave="hoverRating = 0" :size="28"
                            :class="['cursor-pointer transition-transform duration-200', i <= (hoverRating || rating) ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200', i === rating ? 'star-enter-active' : '']" />
                    </div>
                    <span class="text-sm font-bold text-[#4a8b6e] transition-opacity duration-300"
                        :class="(hoverRating || rating) > 0 ? 'opacity-100' : 'opacity-0'">
                        {{ ratingText[hoverRating || rating] }}
                    </span>
                </div>

                <div class="p-8">
                    <!-- 2. Text Review (Immersive) -->
                    <div class="mb-6 relative">
                        <textarea v-model="reviewContent"
                            class="w-full bg-gray-50 border-none rounded-xl p-5 text-sm focus:ring-1 focus:ring-[#4a8b6e]/20 outline-none resize-none placeholder:text-gray-400 transition-all h-40"
                            placeholder="宝贝满足你的期待吗？说说它的优点和美中不足的地方吧，您的评价对其他买家很有帮助~" maxlength="500"></textarea>
                        <div class="absolute bottom-4 right-5 text-xs text-gray-400">
                            {{ reviewContent.length }} / 500
                        </div>
                    </div>

                    <!-- 3. Image Upload -->
                    <div class="mb-8">
                        <div class="flex flex-wrap gap-3">
                            <div v-for="(img, idx) in images" :key="idx"
                                class="w-20 h-20 rounded-lg overflow-hidden relative group border border-gray-200">
                                <img :src="img" class="w-full h-full object-cover" />
                                <button @click="removeImage(idx)"
                                    class="absolute top-1 right-1 bg-black/50 text-white rounded-full p-0.5 opacity-0 group-hover:opacity-100 transition-all">
                                    <X :size="10" />
                                </button>
                            </div>

                            <div v-if="images.length < 5" @click="handleAddImage"
                                class="w-20 h-20 rounded-lg border-2 border-dashed border-gray-200 hover:border-[#4a8b6e] hover:bg-[#4a8b6e]/5 flex flex-col items-center justify-center gap-1 cursor-pointer transition-all text-gray-400 hover:text-[#4a8b6e]">
                                <Camera :size="18" />
                                <span class="text-[10px]">晒图</span>
                            </div>
                            <input type="file" ref="fileInput" @change="handleFileChange" multiple accept="image/*"
                                style="display: none" />
                        </div>
                    </div>

                    <!-- 4. Quick Tags -->
                    <div class="mb-8" v-if="rating >= 4">
                        <div class="text-xs font-bold text-gray-400 mb-2 uppercase">大家都在夸</div>
                        <div class="flex flex-wrap gap-2">
                            <button v-for="tag in quickTags" :key="tag" @click="toggleTag(tag)"
                                :class="['px-3 py-1.5 rounded-full text-xs font-bold transition-all border', selectedTags.includes(tag) ? 'bg-[#4a8b6e]/10 text-[#4a8b6e] border-[#4a8b6e]' : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300']">
                                {{ tag }}
                            </button>
                        </div>
                    </div>

                    <!-- 5. Footer Actions -->
                    <div class="flex items-center justify-between pt-6 border-t border-gray-100">
                        <label class="flex items-center gap-2 cursor-pointer group">
                            <div
                                :class="['w-5 h-5 rounded border flex items-center justify-center transition-colors', isAnonymous ? 'bg-[#4a8b6e] border-[#4a8b6e]' : 'bg-white border-gray-300 group-hover:border-gray-400']">
                                <Check v-if="isAnonymous" :size="12" class="text-white" />
                            </div>
                            <span class="text-sm text-gray-600">匿名评价</span>
                            <input type="checkbox" v-model="isAnonymous" class="hidden">
                        </label>

                        <button @click="submitReview" :disabled="!canSubmit || submitting"
                            :class="['px-8 py-3 rounded-xl font-bold text-sm shadow-lg transition-all transform active:scale-95', canSubmit && !submitting ? 'bg-[#4a8b6e] hover:bg-[#3b755b] text-white shadow-[#4a8b6e]/20' : 'bg-gray-200 text-gray-400 cursor-not-allowed shadow-none']">
                            {{ submitting ? '提交中...' : '发布评价' }}
                        </button>
                    </div>
                </div>

            </div>

        </main>
        <div v-else class="flex justify-center items-center h-screen">
            <div class="text-gray-400">加载中...</div>
        </div>

    </div>
</template>

<script setup>
import { getCloudinarySignature } from '@/api/cloudinary';
import { getOrderById } from '@/api/orders';
import { createReview } from '@/api/reviews';
import { resolveAvatar } from '@/utils/avatar';
import axios from 'axios';
import { Camera, Check, Star, X } from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const orderId = route.query.orderId;

const order = ref(null);
const rating = ref(0);
const hoverRating = ref(0);
const reviewContent = ref('');
const images = ref([]);
const isAnonymous = ref(false);
const selectedTags = ref([]);
const submitting = ref(false);
const fileInput = ref(null);
const isUploading = ref(false);

const ratingText = {
    1: '非常差',
    2: '差',
    3: '一般',
    4: '满意',
    5: '超级满意'
};

const quickTags = ['发货超快', '成色如新', '包装严实', '回复及时', '性价比高'];

const canSubmit = computed(() => {
    return rating.value > 0;
});

const getProductImage = (product) => {
    if (product.images && product.images.length > 0) {
        return product.images[0].url;
    }
    return product.image || 'https://via.placeholder.com/200';
};

const handleAddImage = () => {
    fileInput.value.click();
};

const handleFileChange = async (event) => {
    const files = Array.from(event.target.files || []);
    if (!files.length) return;

    const remainingSlots = 5 - images.value.length;
    if (remainingSlots <= 0) return;

    const filesToUpload = files.slice(0, remainingSlots);
    isUploading.value = true;

    try {
        const rawSignature = await getCloudinarySignature();
        const signatureData = rawSignature && rawSignature.data ? rawSignature.data : rawSignature;
        const apiKey = signatureData?.api_key || signatureData?.apiKey;
        const timestamp = signatureData?.timestamp;
        const signature = signatureData?.signature;
        const uploadPreset = signatureData?.upload_preset || signatureData?.uploadPreset;
        const cloudName = signatureData?.cloud_name || signatureData?.cloudName || import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;

        if (!apiKey || !timestamp || !signature || !cloudName) {
            throw new Error('签名数据缺失');
        }
        const uploadUrl = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;

        const uploadPromises = filesToUpload.map((file) => {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('api_key', apiKey);
            formData.append('timestamp', timestamp);
            formData.append('signature', signature);
            if (uploadPreset) {
                formData.append('upload_preset', uploadPreset);
            }
            return axios.post(uploadUrl, formData);
        });

        const results = await Promise.all(uploadPromises);
        const uploadedUrls = results.map(res => res.data.secure_url);
        images.value.push(...uploadedUrls);
    } catch (err) {
        console.error('Upload failed:', err);
        showFailToast('图片上传失败，请稍后重试');
    } finally {
        isUploading.value = false;
        if (fileInput.value) fileInput.value.value = '';
    }
};

const removeImage = (index) => {
    images.value.splice(index, 1);
};

const toggleTag = (tag) => {
    if (selectedTags.value.includes(tag)) {
        selectedTags.value = selectedTags.value.filter(t => t !== tag);
    } else {
        selectedTags.value.push(tag);
    }
};

const submitReview = async () => {
    if (!canSubmit.value) return;
    if (!reviewContent.value.trim()) {
        showFailToast('请输入评价内容');
        return;
    }

    submitting.value = true;
    try {
        // 组合标签内容到评价中（可选）
        let finalContent = reviewContent.value;
        if (selectedTags.value.length > 0) {
            finalContent += '\n\n标签: ' + selectedTags.value.join(', ');
        }

        await createReview({
            orderId: orderId,
            rating: rating.value,
            content: finalContent,
            isAnonymous: isAnonymous.value,
            images: images.value
        });
        showSuccessToast('评价发布成功！');
        setTimeout(() => {
            router.replace(`/order/${orderId}`);
        }, 1500);
    } catch (error) {
        console.error(error);
        showFailToast(error.response?.data?.message || '提交失败');
    } finally {
        submitting.value = false;
    }
};

onMounted(async () => {
    if (!orderId) {
        showFailToast('参数错误');
        router.back();
        return;
    }
    try {
        const res = await getOrderById(orderId);
        order.value = res;
    } catch (error) {
        showFailToast('获取订单信息失败');
    }
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

/* 星星点击动画 */
.star-enter-active {
    animation: bounce 0.3s;
}

@keyframes bounce {

    0%,
    100% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.2);
    }
}
</style>
