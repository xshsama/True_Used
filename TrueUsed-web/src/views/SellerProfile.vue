<template>
    <div class="min-h-screen bg-[#f7f9fa] pb-12 font-sans text-[#2c3e50]">
        <!-- --- Top Navigation --- -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100/50 backdrop-blur-md bg-white/90">
            <div class="max-w-7xl mx-auto px-6 h-[72px] flex items-center justify-between gap-4">
                <div class="flex items-center gap-10">
                    <div class="flex items-center gap-1.5 cursor-pointer" @click="$router.push('/')">
                        <div
                            class="w-9 h-9 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-xl italic shadow-sm">
                            T</div>
                        <span class="text-2xl font-bold text-[#2c3e50] tracking-tight">TrueUsed<span
                                class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="hidden md:flex items-center gap-8 text-[15px] font-medium text-gray-500">
                        <a href="#" @click.prevent="$router.push('/home')"
                            class="hover:text-[#4a8b6e] transition-colors">首页</a>
                        <a href="#" @click.prevent="$router.push('/ranking')"
                            class="hover:text-[#4a8b6e] transition-colors">捡漏榜</a>
                    </div>
                </div>

                <div class="flex items-center gap-4">
                    <button
                        class="bg-gray-100 hover:bg-gray-200 text-[#2c3e50] px-4 py-2 rounded-full font-bold text-sm transition-colors"
                        @click="$router.back()">
                        返回
                    </button>
                </div>
            </div>
        </nav>

        <!-- --- Hero Cover --- -->
        <header class="relative h-64 bg-gray-800 overflow-hidden group">
            <img :src="seller.coverImage || 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?auto=format&fit=crop&q=80&w=1600'"
                class="w-full h-full object-cover opacity-60 group-hover:scale-105 transition-transform duration-700" />
            <div class="absolute inset-0 bg-gradient-to-t from-[#f7f9fa] via-transparent to-transparent"></div>
        </header>

        <main class="max-w-7xl mx-auto px-6 -mt-24 relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-8">

            <!-- Left Sidebar: Seller Profile -->
            <aside class="lg:col-span-3 space-y-6">
                <!-- Profile Card -->
                <div
                    class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 text-center relative overflow-hidden">
                    <div class="verified-badge h-2 w-full absolute top-0 left-0"></div>

                    <div class="relative w-24 h-24 mx-auto mb-4">
                        <img :src="seller.avatar"
                            class="w-full h-full rounded-full object-cover border-4 border-white shadow-md" />
                        <div class="absolute bottom-0 right-0 bg-[#4a8b6e] text-white p-1.5 rounded-full border-2 border-white"
                            title="已实名认证">
                            <ShieldCheck :size="12" />
                        </div>
                    </div>

                    <h1 class="text-xl font-bold text-[#2c3e50] mb-1 flex items-center justify-center gap-1">
                        {{ seller.name }}
                        <Award v-if="seller.isPro" :size="16" class="text-yellow-500 fill-yellow-500" />
                    </h1>
                    <p class="text-xs text-gray-400 mb-6">加入时间：{{ seller.joinDate }}</p>

                    <div class="flex justify-center gap-8 border-t border-b border-gray-50 py-4 mb-6">
                        <div>
                            <div class="text-lg font-bold text-[#2c3e50]">{{ seller.stats.selling }}</div>
                            <div class="text-xs text-gray-400">在售</div>
                        </div>
                        <div>
                            <div class="text-lg font-bold text-[#2c3e50]">{{ seller.stats.sold }}</div>
                            <div class="text-xs text-gray-400">已售</div>
                        </div>
                        <div>
                            <div class="text-lg font-bold text-[#2c3e50]">{{ seller.stats.followers }}</div>
                            <div class="text-xs text-gray-400">关注</div>
                        </div>
                    </div>

                    <div class="flex gap-2">
                        <button @click="handleFollow"
                            class="flex-1 bg-[#2c3e50] hover:bg-[#1a252f] text-white py-2 rounded-xl text-sm font-bold shadow-md transition-colors flex items-center justify-center gap-2">
                            <Plus :size="16" /> 关注
                        </button>
                        <button
                            class="flex-1 border border-gray-200 hover:border-[#4a8b6e] hover:text-[#4a8b6e] text-gray-600 py-2 rounded-xl text-sm font-bold transition-colors">
                            私信
                        </button>
                    </div>
                </div>

                <!-- Bio & Info -->
                <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 space-y-4">
                    <div class="flex items-start gap-3 text-sm text-gray-600">
                        <MapPin :size="16" class="text-gray-400 mt-0.5" />
                        <span>{{ seller.location }}</span>
                    </div>
                    <div class="flex items-start gap-3 text-sm text-gray-600">
                        <Clock :size="16" class="text-gray-400 mt-0.5" />
                        <span>平均回复：<span class="text-[#4a8b6e] font-bold">10分钟内</span></span>
                    </div>
                    <div class="flex items-start gap-3 text-sm text-gray-600">
                        <ThumbsUp :size="16" class="text-gray-400 mt-0.5" />
                        <span>好评率：<span class="text-[#4a8b6e] font-bold">{{ seller.rating }}%</span></span>
                    </div>

                    <div class="border-t border-gray-50 pt-4 mt-2">
                        <h3 class="text-xs font-bold text-gray-400 uppercase mb-2">店铺简介</h3>
                        <p class="text-sm text-gray-600 leading-relaxed">
                            {{ seller.bio }}
                        </p>
                    </div>
                </div>
            </aside>

            <!-- Right Content: Goods & Reviews -->
            <div class="lg:col-span-9 space-y-6">

                <!-- Tabs -->
                <div
                    class="bg-white rounded-2xl p-2 shadow-sm border border-gray-100 flex items-center justify-between">
                    <div class="flex gap-1">
                        <button v-for="tab in tabs" :key="tab.value" @click="activeTab = tab.value"
                            :class="['px-6 py-2.5 rounded-xl text-sm font-bold transition-all', activeTab === tab.value ? 'bg-[#f7f9fa] text-[#2c3e50]' : 'text-gray-500 hover:text-gray-700']">
                            {{ tab.label }}
                            <span v-if="tab.count" class="ml-1 text-xs opacity-60 bg-gray-200 px-1.5 rounded-full">{{
                                tab.count }}</span>
                        </button>
                    </div>

                    <!-- Filter (Only show on Goods tab) -->
                    <div v-if="activeTab === 'goods'" class="flex items-center gap-2 pr-4 text-xs">
                        <button class="text-[#4a8b6e] font-bold">综合排序</button>
                        <span class="text-gray-300">|</span>
                        <button class="text-gray-500 hover:text-[#2c3e50]">最新</button>
                        <span class="text-gray-300">|</span>
                        <button class="text-gray-500 hover:text-[#2c3e50]">价格</button>
                    </div>
                </div>

                <!-- Content Area -->
                <transition name="fade" mode="out-in">

                    <!-- TAB 1: Goods List -->
                    <div v-if="activeTab === 'goods'" key="goods" class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div v-for="item in goods" :key="item.id" @click="$router.push(`/product/${item.id}`)"
                            class="bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-md hover:border-[#4a8b6e]/30 transition-all group cursor-pointer flex flex-col h-full">
                            <div class="aspect-[4/3] bg-gray-100 relative overflow-hidden">
                                <img :src="item.image"
                                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                                <div v-if="item.isNew"
                                    class="absolute top-2 left-2 bg-[#4a8b6e] text-white text-[10px] px-2 py-0.5 rounded shadow-sm">
                                    新上架</div>
                            </div>
                            <div class="p-4 flex flex-col flex-1 justify-between">
                                <div>
                                    <h3
                                        class="font-bold text-[#2c3e50] line-clamp-2 mb-2 group-hover:text-[#4a8b6e] transition-colors">
                                        {{ item.title }}</h3>
                                    <div class="flex flex-wrap gap-2 mb-3">
                                        <span
                                            class="text-[10px] bg-gray-50 text-gray-500 px-1.5 py-0.5 rounded border border-gray-100">{{
                                                item.condition }}</span>
                                        <span v-if="item.verified"
                                            class="text-[10px] bg-[#4a8b6e]/5 text-[#4a8b6e] px-1.5 py-0.5 rounded border border-[#4a8b6e]/20 flex items-center gap-0.5">
                                            <ShieldCheck :size="10" /> 官方验
                                        </span>
                                    </div>
                                </div>
                                <div class="flex items-center justify-between mt-2">
                                    <span class="text-xl font-bold text-[#ff5e57]">¥{{ item.price }}</span>
                                    <span class="text-xs text-gray-400">{{ item.wants }}人想要</span>
                                </div>
                            </div>
                        </div>
                        <div v-if="goods.length === 0" class="col-span-full py-20 text-center">
                            <div
                                class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                                <PackageOpen :size="32" />
                            </div>
                            <p class="text-gray-400 text-sm">暂无在售商品</p>
                        </div>
                    </div>


                    <!-- TAB 2: Reviews -->
                    <div v-else-if="activeTab === 'reviews'" key="reviews" class="space-y-4">
                        <div v-for="review in reviews" :key="review.id"
                            class="bg-white rounded-2xl p-6 border border-gray-100">
                            <div class="flex items-start gap-4">
                                <img :src="review.avatar" class="w-10 h-10 rounded-full bg-gray-100 object-cover" />
                                <div class="flex-1">
                                    <div class="flex justify-between items-start mb-2">
                                        <div>
                                            <div class="font-bold text-sm text-[#2c3e50] mb-1">{{ review.buyer }}</div>
                                            <div class="flex items-center gap-1">
                                                <Star v-for="i in 5" :key="i" :size="12"
                                                    :class="i <= review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-200 fill-gray-200'" />
                                            </div>
                                        </div>
                                        <div class="text-xs text-gray-400">{{ review.date }}</div>
                                    </div>
                                    <p class="text-sm text-gray-600 mb-3">{{ review.content }}</p>

                                    <!-- Review Images -->
                                    <div v-if="review.images && review.images.length > 0"
                                        class="flex gap-2 mb-3 overflow-x-auto pb-1 scrollbar-hide">
                                        <div v-for="(img, imgIdx) in review.images" :key="imgIdx"
                                            class="w-20 h-20 rounded-lg overflow-hidden cursor-pointer border border-gray-100 flex-shrink-0"
                                            @click.stop="previewReviewImage(review.images, imgIdx)">
                                            <img :src="img"
                                                class="w-full h-full object-cover hover:opacity-90 transition-opacity" />
                                        </div>
                                    </div>

                                    <!-- Product Info -->
                                    <div v-if="review.productTitle"
                                        class="bg-gray-50 rounded-lg p-3 flex items-center gap-3 cursor-pointer hover:bg-gray-100 transition-colors"
                                        @click="review.productId && $router.push(`/product/${review.productId}`)">
                                        <img v-if="review.productImage" :src="review.productImage"
                                            class="w-10 h-10 rounded bg-white object-cover" />
                                        <div class="flex-1 min-w-0">
                                            <div class="text-xs font-bold text-[#2c3e50] truncate">{{
                                                review.productTitle }}</div>
                                            <div class="text-xs text-[#ff5e57] font-bold mt-0.5">¥{{ review.price }}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="reviews.length === 0" class="py-10 text-center">
                            <p class="text-gray-400 text-sm">暂无买家评论</p>
                        </div>
                    </div>

                    <!-- TAB 4: Sold (Optional) -->
                    <div v-else-if="activeTab === 'sold'" key="sold"
                        class="py-20 text-center bg-white rounded-2xl border border-gray-100">
                        <div
                            class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                            <PackageOpen :size="32" />
                        </div>
                        <p class="text-gray-400 text-sm">暂无已售记录</p>
                    </div>

                </transition>

            </div>

        </main>

    </div>
</template>

<script setup>
import { createSellerComment, getSellerComments, getSellerReviews } from '@/api/reviews';
import request from '@/utils/request';
import { resolveAvatar } from '@/utils/avatar';
import {
    Award,
    Clock,
    MapPin,
    PackageOpen,
    Plus,
    ShieldCheck,
    ThumbsUp
} from 'lucide-vue-next';
import { ImagePreview, showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const sellerId = route.params.id;

const activeTab = ref('goods');
const loading = ref(false);

const seller = ref({
    name: '加载中...',
    avatar: '',
    coverImage: '',
    isPro: false,
    joinDate: '',
    location: '未知',
    rating: 100,
    bio: '',
    stats: {
        selling: 0,
        sold: 0,
        followers: 0
    }
});

const goods = ref([]);
const reviews = ref([]);
const comments = ref([]);
const soldGoods = ref([]);
const newComment = ref('');
const submittingComment = ref(false);

const tabs = computed(() => [
    { label: '在售宝贝', value: 'goods', count: seller.value.stats.selling },
    { label: '买家评论', value: 'reviews', count: reviews.value.length },
    { label: '已售记录', value: 'sold', count: seller.value.stats.sold },
]);

const loadSellerInfo = async () => {
    loading.value = true;
    try {
        // 1. 获取卖家基本信息
        const profileRes = await request.get(`/users/${sellerId}/public-profile`);
        seller.value = {
            name: profileRes.nickname || profileRes.username || '未知用户',
            avatar: resolveAvatar(profileRes.avatarUrl, profileRes.avatar),
            coverImage: profileRes.coverImage || '',
            isPro: profileRes.creditScore > 700, // 示例逻辑
            joinDate: profileRes.createdAt ? new Date(profileRes.createdAt).toLocaleDateString() : '未知',
            location: profileRes.location || '未知', // 假设API有city字段，如果没有则显示未知
            rating: 98, // 示例数据，API可能需要提供好评率
            bio: profileRes.bio || '这位卖家很懒，什么都没写~',
            stats: {
                selling: profileRes.sellingCount || 0,
                sold: profileRes.soldCount || 0,
                followers: 0 // API暂无
            }
        };

        // 2. 获取在售商品
        const productsRes = await request.get('/products', {
            params: { sellerId: sellerId, status: 'ON_SALE', page: 0, size: 20 }
        });
        goods.value = (productsRes.content || []).map(p => ({
            id: p.id,
            title: p.title,
            price: p.price,
            condition: p.condition || '9成新',
            image: (p.images && p.images.length > 0) ? p.images[0].url : '',
            wants: p.viewCount || 0,
            verified: p.status === 'VERIFIED', // 示例
            isNew: isNewProduct(p.createdAt)
        }));

        // 3. 获取评价列表
        const reviewsRes = await getSellerReviews(sellerId, { page: 0, size: 20 });
        reviews.value = (reviewsRes.content || []).map(r => ({
            id: r.id,
            buyer: r.isAnonymous ? '匿名用户' : (r.buyerName || '买家'),
            avatar: r.isAnonymous ? resolveAvatar() : resolveAvatar(r.buyerAvatar),
            date: new Date(r.createdAt).toLocaleDateString(),
            rating: r.rating,
            content: r.content,
            productId: r.productId,
            productTitle: r.productTitle || '未知商品',
            price: r.price || 0,
            productImage: r.productImage || '',
            images: r.images || []
        }));

        // 4. 获取留言列表
        await loadComments();

    } catch (e) {
        console.error(e);
        showFailToast('加载卖家信息失败');
    } finally {
        loading.value = false;
    }
};

const loadComments = async () => {
    const commentsRes = await getSellerComments(sellerId, { page: 0, size: 20 });
    comments.value = (commentsRes.content || []).map(c => ({
        id: c.id,
        user: c.user?.nickname || c.user?.username || '用户',
        avatar: resolveAvatar(c.user?.avatarUrl, c.user?.avatar),
        date: new Date(c.createdAt).toLocaleDateString(),
        content: c.content,
        replies: c.replies || []
    }));
};

const handlePostComment = async () => {
    if (!newComment.value.trim()) return;
    submittingComment.value = true;
    try {
        await createSellerComment({
            targetUserId: sellerId,
            content: newComment.value
        });
        showSuccessToast('留言成功');
        newComment.value = '';
        await loadComments();
    } catch (e) {
        showFailToast('留言失败');
    } finally {
        submittingComment.value = false;
    }
};

const isNewProduct = (dateStr) => {
    if (!dateStr) return false;
    const date = new Date(dateStr);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 3;
};

const handleFollow = () => {
    showSuccessToast('关注成功');
};

const previewReviewImage = (images, index) => {
    ImagePreview({
        images: images,
        startPosition: index,
        closeable: true,
    });
};

onMounted(() => {
    loadSellerInfo();
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

.verified-badge {
    background: linear-gradient(135deg, #4a8b6e 0%, #2c3e50 100%);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
