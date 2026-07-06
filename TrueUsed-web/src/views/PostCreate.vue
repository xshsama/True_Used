<script setup>
import { getCloudinarySignature } from '@/api/cloudinary';
import { createConsignment } from '@/api/consignment';
import { createProduct } from '@/api/products';
import { useAuth } from '@/composables/useAuth';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import axios from 'axios';
import {
    ArrowRight,
    Camera,
    CheckCircle2,
    HelpCircle,
    MapPin,
    Package,
    PackageCheck,
    Truck,
    UserCheck,
    Users,
    X
} from 'lucide-vue-next';
import { closeToast, showFailToast, showLoadingToast, showSuccessToast, showToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const { currentUser } = useAuth();
const userStore = useUserStore();

// --- 状态管理 ---
const saleMode = ref('consignment'); // 'consignment' or 'direct'

// Form Data structure matching the example, plus internal needs
const form = ref({
    title: '',
    description: '',
    price: '',
    images: [],
    freight: 'seller', // 'seller' (包邮) or 'buyer' (到付)
    location: '上海市',
    categoryId: null,
    categoryName: '',
    condition: '99新', // Default condition as it's removed from UI
    originalPrice: '',
    isMeetup: false
});

const conditionOptions = ['全新', '99新', '95新', '9成新', '8成新', '战损版'];

// --- 图片上传 ---
const fileInput = ref(null);
const isUploading = ref(false);

const handleAddImage = () => {
    if (form.value.images.length < 9) {
        fileInput.value.click();
    }
};

const removeImage = (index) => {
    form.value.images.splice(index, 1);
};

const handleFileChange = async (event) => {
    const files = Array.from(event.target.files || []);
    if (!files.length) return;

    // If the first image is the default placeholder, remove it when user uploads real images
    if (form.value.images.length === 1 && form.value.images[0].includes('unsplash.com')) {
        form.value.images = [];
    }

    const remainingSlots = 9 - form.value.images.length;
    const filesToUpload = files.slice(0, remainingSlots);

    if (!filesToUpload.length) return;

    isUploading.value = true;
    showLoadingToast({ message: '上传中...', forbidClick: true });

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
        cloudinaryCloudName.value = cloudName;
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
        const uploadedKeys = results.map(res => res.data.public_id);
        form.value.images.push(...uploadedKeys);
        closeToast();
    } catch (err) {
        console.error('Upload failed:', err);
        closeToast();
        showFailToast('图片上传失败');
    } finally {
        isUploading.value = false;
        if (fileInput.value) fileInput.value.value = '';
    }
};

// --- 分类选择 ---
const categorySelectRef = ref(null);

const onCategoryChange = (payload) => {
    form.value.categoryId = payload?.value ?? payload;
    form.value.categoryName = payload?.label || '';
};

// --- 地区选择 ---
const showLocationPicker = ref(false);
const locationColumns = [
    { text: '北京市', value: 'beijing' },
    { text: '上海市', value: 'shanghai' },
    { text: '广州市', value: 'guangzhou' },
    { text: '深圳市', value: 'shenzhen' },
    { text: '杭州市', value: 'hangzhou' },
    { text: '其他', value: 'other' }
];

const onLocationConfirm = ({ selectedOptions }) => {
    form.value.location = selectedOptions[0].text;
    showLocationPicker.value = false;
};

// --- 提交逻辑 ---
const isSubmitting = ref(false);

const mapCondition = (text) => {
    const map = {
        '全新': 'NEW',
        '99新': 'LIKE_NEW',
        '95新': 'LIKE_NEW',
        '9成新': 'GOOD',
        '8成新': 'FAIR',
        '战损版': 'POOR',
    };
    return map[text] || 'GOOD';
};

const onSubmit = async () => {
    if (isSubmitting.value) return;

    if (form.value.images.length === 0) {
        showToast('请至少上传一张图片');
        return;
    }
    if (!form.value.title) {
        showToast('请输入标题');
        return;
    }
    if (!form.value.description) {
        showToast('请输入描述');
        return;
    }
    if (!form.value.price) {
        showToast('请输入价格');
        return;
    }
    if (!form.value.categoryId) {
        showToast('请补充商品分类信息');
        categorySelectRef.value?.open?.();
        return;
    }

    isSubmitting.value = true;
    showLoadingToast({ message: '发布中...', duration: 0, forbidClick: true });

    try {
        if (saleMode.value === 'consignment') {
            const payload = {
                title: form.value.title,
                description: form.value.description,
                expectedPrice: Number(form.value.price),
                originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : undefined,
                categoryId: form.value.categoryId,
                sellerClaimCondition: mapCondition(form.value.condition),
                shippingMethod: 'express', // Default
                trackingNoInbound: '', // Initial submission might not have tracking
                imageKeys: form.value.images
            };
            await createConsignment(payload);
        } else {
            let finalTradeTypes = ['EXPRESS']; // Default
            if (form.value.isMeetup) {
                finalTradeTypes.push('MEETUP');
            }

            // Construct payload
            const payload = {
                title: form.value.title,
                description: form.value.description,
                price: Number(form.value.price),
                originalPrice: form.value.originalPrice ? Number(form.value.originalPrice) : undefined,
                currency: 'CNY',
                sellerClaimCondition: mapCondition(form.value.condition),
                categoryId: form.value.categoryId,
                locationText: form.value.location,
                imageKeys: form.value.images,
                shippingPayer: form.value.freight === 'seller' ? 'SELLER' : 'BUYER',
                tradeTypes: finalTradeTypes.join(','),
                tradeModel: 'FREE_TRADING'
            };

            await createProduct(payload);
        }

        closeToast();
        showSuccessToast('发布成功');

        router.replace('/my-products');

    } catch (e) {
        closeToast();
        showFailToast('发布失败');
        console.error(e);
    } finally {
        isSubmitting.value = false;
    }
};

const onSaveDraft = () => {
    showToast('草稿功能开发中');
};

// --- Computed ---
const estimatedIncome = computed(() => {
    const p = parseFloat(form.value.price) || 0;
    if (saleMode.value === 'consignment') {
        return (p * 0.97).toFixed(2);
    } else {
        return (p * 0.99).toFixed(2);
    }
});

const serviceFee = computed(() => {
    const p = parseFloat(form.value.price) || 0;
    if (saleMode.value === 'consignment') {
        return (p * 0.03).toFixed(2);
    } else {
        return (p * 0.01).toFixed(2);
    }
});

const cloudinaryCloudName = ref(import.meta.env.VITE_CLOUDINARY_CLOUD_NAME);

onMounted(() => {
    if (userStore.isLoggedIn && !userStore.user) {
        userStore.loadMe().catch(() => { });
    }
});
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-28">

        <!-- --- Top Navigation --- -->
        <nav class="bg-white sticky top-0 z-50 border-b border-gray-100">
            <div class="max-w-5xl mx-auto px-6 h-[72px] flex items-center justify-between gap-4">
                <div class="flex items-center gap-10">
                    <div class="flex items-center gap-1.5 cursor-pointer" @click="router.push('/')">
                        <div
                            class="w-9 h-9 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-xl italic shadow-sm">
                            T</div>
                        <span class="text-2xl font-bold text-[#2c3e50] tracking-tight">TrueUsed<span
                                class="text-[#4a8b6e]">.</span></span>
                    </div>
                    <div class="hidden md:flex items-center gap-2 text-lg font-bold text-gray-700">
                        发布闲置
                    </div>
                </div>

                <div class="flex items-center gap-4">
                    <button @click="onSaveDraft"
                        class="text-gray-500 hover:text-[#4a8b6e] font-medium text-sm px-4 py-2 rounded-full hover:bg-gray-50 transition-colors">
                        存草稿
                    </button>
                    <div class="w-9 h-9 rounded-full bg-gray-200 overflow-hidden ml-2 border border-gray-100">
                        <img :src="resolveAvatar(currentUser?.avatarUrl, currentUser?.avatar)"
                            class="w-full h-full object-cover" />
                    </div>
                </div>
            </div>
        </nav>

        <!-- --- Main Content --- -->
        <main class="max-w-3xl mx-auto px-4 py-8 space-y-6">

            <!-- 1. Sale Mode Selection -->
            <section class="space-y-3">
                <h2 class="font-bold text-lg text-[#2c3e50] px-1">选择售卖模式</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

                    <!-- Mode A: Consignment -->
                    <div @click="saleMode = 'consignment'" :class="[
                        'p-5 rounded-2xl border-2 cursor-pointer transition-all relative overflow-hidden',
                        saleMode === 'consignment' ? 'mode-card-selected shadow-md' : 'mode-card-default shadow-sm'
                    ]">
                        <div v-if="saleMode === 'consignment'"
                            class="absolute top-0 right-0 bg-[#4a8b6e] text-white text-[10px] px-2 py-1 rounded-bl-lg font-bold">
                            已选择</div>
                        <div class="flex items-start gap-4">
                            <div
                                class="icon-box w-10 h-10 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center flex-shrink-0 transition-colors">
                                <PackageCheck :size="20" />
                            </div>
                            <div>
                                <h3 class="font-bold text-base mb-1">官方寄售 (推荐)</h3>
                                <p class="text-xs opacity-80 leading-relaxed mb-2">寄给平台验货，省心极速卖。</p>
                                <div class="flex flex-wrap gap-2">
                                    <span
                                        class="text-[10px] border border-current px-1.5 rounded opacity-70">流量扶持</span>
                                    <span
                                        class="text-[10px] border border-current px-1.5 rounded opacity-70">坏账包赔</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Mode B: Direct Sell -->
                    <div @click="saleMode = 'direct'" :class="[
                        'p-5 rounded-2xl border-2 cursor-pointer transition-all relative overflow-hidden',
                        saleMode === 'direct' ? 'mode-card-selected shadow-md' : 'mode-card-default shadow-sm'
                    ]">
                        <div v-if="saleMode === 'direct'"
                            class="absolute top-0 right-0 bg-[#4a8b6e] text-white text-[10px] px-2 py-1 rounded-bl-lg font-bold">
                            已选择</div>
                        <div class="flex items-start gap-4">
                            <div
                                class="icon-box w-10 h-10 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center flex-shrink-0 transition-colors">
                                <UserCheck :size="20" />
                            </div>
                            <div>
                                <h3 class="font-bold text-base mb-1">自主售卖</h3>
                                <p class="text-xs opacity-80 leading-relaxed mb-2">自己在平台发布，直接发货给买家。</p>
                                <div class="flex flex-wrap gap-2">
                                    <span
                                        class="text-[10px] border border-current px-1.5 rounded opacity-70">自由定价</span>
                                    <span
                                        class="text-[10px] border border-current px-1.5 rounded opacity-70">支持面交</span>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </section>

            <!-- 2. Product Info (Common) -->
            <section class="bg-white rounded-2xl p-8 shadow-sm border border-gray-100/50 space-y-6">
                <div class="flex justify-between items-center mb-2">
                    <h2 class="font-bold text-lg text-[#2c3e50]">商品信息</h2>
                </div>

                <div class="space-y-4">
                    <div class="category-select-control">
                        <CategorySelect ref="categorySelectRef" v-model="form.categoryId" label="商品分类"
                            placeholder="选择分类" @change="onCategoryChange" />
                    </div>

                    <input type="text" v-model="form.title" placeholder="品牌型号，宝贝特点"
                        class="w-full bg-gray-50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none font-bold placeholder:font-normal" />

                    <div class="grid grid-cols-4 sm:grid-cols-5 gap-4">
                        <div v-for="(img, idx) in form.images" :key="idx"
                            class="aspect-square rounded-xl overflow-hidden relative group border border-gray-200">
                            <img :src="`https://res.cloudinary.com/${cloudinaryCloudName}/image/upload/${img}`"
                                class="w-full h-full object-cover" />
                            <button @click="removeImage(idx)"
                                class="absolute top-1 right-1 bg-black/50 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-all">
                                <X :size="12" />
                            </button>
                        </div>
                        <div v-if="form.images.length < 9" @click="handleAddImage"
                            class="aspect-square rounded-xl border-2 border-dashed border-gray-200 hover:border-[#4a8b6e] hover:bg-[#4a8b6e]/5 flex flex-col items-center justify-center gap-2 cursor-pointer transition-all text-gray-400 hover:text-[#4a8b6e]">
                            <Camera :size="20" />
                            <span class="text-xs font-medium">添加</span>
                        </div>
                    </div>

                    <textarea v-model="form.description" placeholder="描述一下宝贝的使用情况..."
                        class="w-full h-24 bg-gray-50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none resize-none text-sm"></textarea>

                    <div class="space-y-3">
                        <div class="flex items-center justify-between">
                            <label class="text-sm font-bold text-gray-600">卖家自报成色</label>
                            <span class="text-xs text-gray-400">
                                {{ saleMode === 'consignment' ? '平台验货后会生成最终等级' : '买家看到的是你的自报成色' }}
                            </span>
                        </div>
                        <div class="flex flex-wrap gap-2">
                            <button v-for="option in conditionOptions" :key="option" type="button"
                                @click="form.condition = option"
                                :class="[
                                    'px-3 py-2 rounded-full text-sm border transition-colors',
                                    form.condition === option
                                        ? 'bg-[#4a8b6e] border-[#4a8b6e] text-white'
                                        : 'bg-white border-gray-200 text-gray-600 hover:border-[#4a8b6e]/40 hover:text-[#4a8b6e]'
                                ]">
                                {{ option }}
                            </button>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 3. Price & Fees (Dynamic based on Mode) -->
            <section class="bg-white rounded-2xl p-8 shadow-sm border border-gray-100/50 space-y-6">
                <h2 class="font-bold text-lg text-[#2c3e50]">价格设置</h2>

                <div class="space-y-2">
                    <div class="flex justify-between">
                        <label class="text-sm font-bold text-gray-600">期望售价</label>
                    </div>
                    <div class="relative">
                        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-lg">¥</span>
                        <input type="number" v-model="form.price" placeholder="0.00"
                            class="w-full pl-10 pr-4 py-3 bg-gray-50 rounded-xl border-none font-bold text-2xl text-[#2c3e50] focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none" />
                    </div>
                </div>

                <div class="space-y-2">
                    <div class="flex justify-between">
                        <label class="text-sm font-bold text-gray-600">入手原价 (选填)</label>
                    </div>
                    <div class="relative">
                        <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-lg">¥</span>
                        <input type="number" v-model="form.originalPrice" placeholder="0.00"
                            class="w-full pl-10 pr-4 py-3 bg-gray-50 rounded-xl border-none font-bold text-xl text-gray-600 focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none" />
                    </div>
                </div>

                <!-- Consignment Fee Mode -->
                <transition name="fade" mode="out-in">
                    <div v-if="saleMode === 'consignment'"
                        class="bg-[#4a8b6e]/5 rounded-xl p-4 text-sm space-y-2 border border-[#4a8b6e]/10">
                        <div class="flex justify-between text-gray-500">
                            <span>技术服务费 (3%)</span>
                            <span>- ¥{{ serviceFee }}</span>
                        </div>
                        <div class="flex justify-between text-gray-500">
                            <span class="flex items-center gap-1">平台验货服务费
                                <HelpCircle :size="12" />
                            </span>
                            <div class="flex flex-col items-end">
                                <span class="text-[#4a8b6e] font-bold">限时免除</span>
                                <span class="text-xs text-gray-300 line-through">¥ 29.00</span>
                            </div>
                        </div>
                        <div class="border-t border-[#4a8b6e]/10 pt-2 flex justify-between items-center">
                            <span class="font-bold text-gray-700">寄售预计到手</span>
                            <span class="font-bold text-xl text-[#ff5e57]">¥{{ estimatedIncome }}</span>
                        </div>
                    </div>

                    <!-- Direct Sell Mode -->
                    <div v-else class="bg-gray-50 rounded-xl p-4 text-sm space-y-2 border border-gray-100">
                        <div class="flex justify-between text-gray-500">
                            <span>技术服务费 (1% 仅成交后收取)</span>
                            <span>- ¥{{ serviceFee }}</span>
                        </div>
                        <div class="border-t border-gray-200 pt-2 flex justify-between items-center">
                            <span class="font-bold text-gray-700">自售预计到手</span>
                            <span class="font-bold text-xl text-[#ff5e57]">¥{{ estimatedIncome }}</span>
                        </div>
                    </div>
                </transition>
            </section>

            <!-- 4. Logistics (Dynamic based on Mode) -->
            <transition name="fade" mode="out-in">

                <!-- Mode A: Consignment Logistics -->
                <section v-if="saleMode === 'consignment'"
                    class="bg-white rounded-2xl p-8 shadow-sm border border-gray-100/50 space-y-4">
                    <h2 class="font-bold text-lg text-[#2c3e50] flex items-center gap-2">
                        入仓方式
                        <span class="text-xs font-normal bg-[#4a8b6e]/10 text-[#4a8b6e] px-2 py-0.5 rounded">发往
                            TrueUsed 上海仓</span>
                    </h2>
                    <div class="grid grid-cols-2 gap-4">
                        <div
                            class="p-4 rounded-xl border-2 border-[#4a8b6e] bg-[#4a8b6e]/5 text-[#4a8b6e] cursor-pointer flex flex-col gap-2">
                            <div class="flex items-center gap-2">
                                <Truck :size="18" />
                                <span class="font-bold text-sm">预约顺丰上门</span>
                            </div>
                            <p class="text-xs opacity-80">运费到付，验货通过后由买家承担</p>
                        </div>
                        <div
                            class="p-4 rounded-xl border-2 border-gray-100 text-gray-500 hover:border-gray-200 cursor-pointer flex flex-col gap-2">
                            <div class="flex items-center gap-2">
                                <Package :size="18" />
                                <span class="font-bold text-sm">自行寄送</span>
                            </div>
                            <p class="text-xs opacity-80">提交后获得仓库地址及入仓码</p>
                        </div>
                    </div>
                </section>

                <!-- Mode B: Direct Sell Logistics -->
                <section v-else class="bg-white rounded-2xl p-8 shadow-sm border border-gray-100/50 space-y-4">
                    <h2 class="font-bold text-lg text-[#2c3e50]">交易与发货</h2>
                    <div class="space-y-3">
                        <div class="trade-control-row">
                            <div class="trade-control-title">
                                <div class="trade-control-icon">
                                    <Truck :size="17" />
                                </div>
                                <div>
                                    <span class="block text-sm font-bold text-gray-700">快递发货</span>
                                    <span class="text-xs text-gray-400">选择运费承担方</span>
                                </div>
                            </div>
                            <div class="freight-selector" role="radiogroup" aria-label="快递发货运费">
                                <label :class="['freight-option', { 'freight-option-active': form.freight === 'seller' }]">
                                    <input type="radio" name="freight-payer" value="seller" v-model="form.freight"
                                        class="visually-hidden" />
                                    <span class="freight-option-mark"></span>
                                    <span class="freight-option-copy">
                                        <strong>包邮</strong>
                                        <small>卖家承担</small>
                                    </span>
                                </label>
                                <label :class="['freight-option', { 'freight-option-active': form.freight === 'buyer' }]">
                                    <input type="radio" name="freight-payer" value="buyer" v-model="form.freight"
                                        class="visually-hidden" />
                                    <span class="freight-option-mark"></span>
                                    <span class="freight-option-copy">
                                        <strong>到付</strong>
                                        <small>买家承担</small>
                                    </span>
                                </label>
                            </div>
                        </div>
                        <label :class="['trade-control-row meetup-toggle-card', { 'meetup-toggle-card-active': form.isMeetup }]">
                            <input type="checkbox" id="meetup-toggle" v-model="form.isMeetup" class="visually-hidden" />
                            <div class="trade-control-title">
                                <div class="trade-control-icon">
                                    <Users :size="17" />
                                </div>
                                <div>
                                    <span class="block text-sm font-bold text-gray-700">支持面交</span>
                                    <span class="text-xs text-gray-400">同城买家可当面交易</span>
                                </div>
                            </div>
                            <span class="meetup-switch" aria-hidden="true">
                                <span class="meetup-switch-state">{{ form.isMeetup ? '已开启' : '未开启' }}</span>
                                <span class="meetup-switch-track">
                                    <span class="meetup-switch-thumb"></span>
                                </span>
                            </span>
                        </label>
                        <div class="pt-4 border-t border-gray-100">
                            <div class="flex items-center gap-2 text-sm text-gray-500">
                                <MapPin :size="16" class="text-[#4a8b6e]" />
                                <span>发货地：</span>
                                <span class="font-bold text-gray-800" @click="showLocationPicker = true">{{
                                    form.location
                                }}</span>
                            </div>
                        </div>
                    </div>
                </section>

            </transition>

        </main>

        <!-- --- Sticky Footer --- -->
        <div
            class="fixed bottom-0 w-full bg-white border-t border-gray-100 p-4 pb-8 z-40 shadow-[-10px_0_30px_rgba(0,0,0,0.05)]">
            <div class="max-w-3xl mx-auto flex items-center justify-between gap-4">
                <div class="flex flex-col">
                    <div class="flex items-center gap-2 text-xs text-gray-400 mb-0.5">
                        <CheckCircle2 :size="12" class="text-[#4a8b6e]" />
                        <span v-if="saleMode === 'consignment'">同意《寄售服务协议》</span>
                        <span v-else>同意《平台交易规则》</span>
                    </div>
                </div>
                <button @click="onSubmit" :disabled="isSubmitting" :class="[
                    'text-white px-8 py-3 rounded-full font-bold text-base shadow-lg transition-all transform active:scale-95 flex-1 md:flex-none md:w-64 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed',
                    saleMode === 'consignment' ? 'bg-[#4a8b6e] hover:bg-[#3b755b] shadow-[#4a8b6e]/20' : 'bg-[#2c3e50] hover:bg-[#1a252f] shadow-gray-300'
                ]">
                    <span v-if="saleMode === 'consignment'">{{ isSubmitting ? '提交中...' : '提交并获取入仓码' }}</span>
                    <span v-else>{{ isSubmitting ? '发布中...' : '立即发布' }}</span>
                    <ArrowRight :size="16" />
                </button>
            </div>
        </div>

        <!-- Hidden File Input -->
        <input type="file" ref="fileInput" @change="handleFileChange" multiple accept="image/*" class="hidden" />

        <van-popup v-model:show="showLocationPicker" position="bottom">
            <van-picker :columns="locationColumns" @confirm="onLocationConfirm" @cancel="showLocationPicker = false" />
        </van-popup>

    </div>
</template>

<style scoped>
.hidden {
    display: none;
}

.visually-hidden {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
}

/* 模式选择卡片选中态 */
.mode-card-selected {
    border-color: #4a8b6e;
    background-color: #f0fdf4;
    /* bg-emerald-50 */
    color: #064e3b;
    /* text-emerald-900 */
}

.mode-card-selected .icon-box {
    background-color: #4a8b6e;
    color: white;
}

.mode-card-default {
    border-color: #f3f4f6;
    /* border-gray-100 */
    background-color: white;
    color: #4b5563;
    /* text-gray-600 */
}

.mode-card-default:hover {
    border-color: #d1fae5;
    /* hover:border-emerald-100 */
}

.category-select-control :deep(.van-cell) {
    align-items: center;
    min-height: 48px;
    padding: 12px 16px;
    border-radius: 12px;
    background: #f9fafb;
}

.category-select-control :deep(.van-field__label) {
    color: #4b5563;
    font-weight: 700;
}

.category-select-control :deep(.van-field__control) {
    color: #2c3e50;
    font-weight: 700;
    text-align: right;
}

.category-select-control :deep(.van-field__control::placeholder) {
    color: #9ca3af;
    font-weight: 500;
}

.trade-control-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    min-height: 76px;
    padding: 12px 14px;
    border: 1px solid #e5e7eb;
    border-radius: 14px;
    background: #f9fafb;
    transition: border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.trade-control-row:hover {
    border-color: rgba(74, 139, 110, 0.35);
    background: #fbfdfc;
}

.trade-control-title {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.trade-control-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    border: 1px solid #eef0f3;
    border-radius: 12px;
    background: #ffffff;
    color: #2c3e50;
    box-shadow: 0 6px 16px rgba(15, 23, 42, 0.04);
    transition: border-color 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.freight-selector {
    display: grid;
    grid-template-columns: repeat(2, minmax(112px, 1fr));
    gap: 8px;
    flex: 0 0 min(320px, 50%);
}

.freight-option {
    display: flex;
    align-items: center;
    gap: 9px;
    min-height: 48px;
    padding: 9px 12px;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    background: #ffffff;
    color: #64748b;
    cursor: pointer;
    user-select: none;
    transition: border-color 0.18s ease, background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.freight-option:hover {
    border-color: rgba(74, 139, 110, 0.45);
    color: #2c3e50;
}

.freight-option:focus-within,
.meetup-toggle-card:focus-within {
    outline: 3px solid rgba(74, 139, 110, 0.16);
    outline-offset: 2px;
}

.freight-option-active {
    border-color: #4a8b6e;
    background: linear-gradient(180deg, rgba(74, 139, 110, 0.1), rgba(74, 139, 110, 0.04));
    color: #2c3e50;
    box-shadow: 0 8px 20px rgba(74, 139, 110, 0.12);
}

.freight-option-mark {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    border: 2px solid #cbd5e1;
    border-radius: 999px;
    background: #ffffff;
    box-shadow: inset 0 0 0 4px #ffffff;
    transition: border-color 0.18s ease, background 0.18s ease;
}

.freight-option-active .freight-option-mark {
    border-color: #4a8b6e;
    background: #4a8b6e;
}

.freight-option-copy {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 4px;
    line-height: 1.05;
}

.freight-option-copy strong {
    font-size: 13px;
    font-weight: 800;
}

.freight-option-copy small {
    color: #9ca3af;
    font-size: 11px;
    font-weight: 700;
}

.freight-option-active .freight-option-copy small {
    color: rgba(74, 139, 110, 0.86);
}

.meetup-toggle-card {
    cursor: pointer;
    user-select: none;
}

.meetup-toggle-card-active {
    border-color: rgba(74, 139, 110, 0.65);
    background: #f4fbf7;
    box-shadow: 0 8px 20px rgba(74, 139, 110, 0.08);
}

.meetup-toggle-card-active .trade-control-icon {
    border-color: rgba(74, 139, 110, 0.28);
    background: rgba(74, 139, 110, 0.1);
    color: #4a8b6e;
}

.meetup-switch {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    color: #94a3b8;
    font-size: 12px;
    font-weight: 800;
}

.meetup-toggle-card-active .meetup-switch {
    color: #4a8b6e;
}

.meetup-switch-track {
    display: flex;
    align-items: center;
    width: 54px;
    height: 30px;
    padding: 3px;
    border-radius: 999px;
    background: #cfd4dc;
    box-shadow: inset 0 1px 3px rgba(44, 62, 80, 0.2);
    transition: background 0.18s ease;
}

.meetup-switch-thumb {
    width: 24px;
    height: 24px;
    border-radius: 999px;
    background: #ffffff;
    box-shadow: 0 3px 8px rgba(15, 23, 42, 0.2);
    transform: translateX(0);
    transition: transform 0.18s ease;
}

.meetup-toggle-card-active .meetup-switch-track {
    background: #4a8b6e;
}

.meetup-toggle-card-active .meetup-switch-thumb {
    transform: translateX(24px);
}

@media (max-width: 640px) {
    .trade-control-row {
        align-items: stretch;
        flex-direction: column;
    }

    .freight-selector {
        width: 100%;
        flex: none;
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .meetup-switch {
        width: 100%;
        justify-content: space-between;
    }
}

/* 切换动画 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(10px);
}
</style>
