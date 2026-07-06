<script setup>
import { deleteAddress as apiDeleteAddress, createAddress, getAddresses, updateAddress } from '@/api/address';
import { areaList } from '@vant/area-data';
import {
    Check,
    ChevronRight,
    Edit3,
    Plus,
    Trash2,
    X
} from 'lucide-vue-next';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// --- State ---
const addresses = ref([]);
const isEditorOpen = ref(false);
const isEditing = ref(false);
const showAreaPicker = ref(false);

const form = ref({
    id: null,
    recipientName: '',
    phone: '',
    region: '',
    province: '',
    city: '',
    district: '',
    detailedAddress: '',
    tag: '',
    isDefault: false
});

// --- Data Fetching ---
const loadAddresses = async () => {
    try {
        const res = await getAddresses();
        addresses.value = res.map(addr => ({
            ...addr,
            // Ensure region string is constructed if not present
            region: addr.region || `${addr.province} ${addr.city} ${addr.district}`
        }));
    } catch (error) {
        console.error('Failed to load addresses:', error);
        showFailToast('加载地址失败');
    }
};

// --- Actions ---
const openEditor = (addr = null) => {
    if (addr) {
        isEditing.value = true;
        form.value = {
            ...addr,
            region: addr.region || `${addr.province} ${addr.city} ${addr.district}`
        };
    } else {
        isEditing.value = false;
        form.value = {
            id: null,
            recipientName: '',
            phone: '',
            region: '',
            province: '',
            city: '',
            district: '',
            detailedAddress: '',
            tag: '',
            isDefault: false
        };
    }
    isEditorOpen.value = true;
};

const closeEditor = () => {
    isEditorOpen.value = false;
};

const onAreaConfirm = ({ selectedOptions }) => {
    form.value.province = selectedOptions[0]?.text || '';
    form.value.city = selectedOptions[1]?.text || '';
    form.value.district = selectedOptions[2]?.text || '';
    form.value.region = `${form.value.province} ${form.value.city} ${form.value.district}`;
    showAreaPicker.value = false;
};

const saveAddress = async () => {
    // Basic validation
    if (!form.value.recipientName || !form.value.phone || !form.value.region || !form.value.detailedAddress) {
        showFailToast('请填写完整信息');
        return;
    }

    try {
        const payload = {
            recipientName: form.value.recipientName,
            phone: form.value.phone,
            province: form.value.province,
            city: form.value.city,
            district: form.value.district,
            detailedAddress: form.value.detailedAddress,
            tag: form.value.tag,
            isDefault: form.value.isDefault
        };

        if (isEditing.value) {
            await updateAddress(form.value.id, payload);
            showSuccessToast('更新成功');
        } else {
            await createAddress(payload);
            showSuccessToast('添加成功');
        }
        await loadAddresses();
        closeEditor();
    } catch (error) {
        console.error(error);
        let msg = '保存失败';
        // 尝试从响应中提取具体的错误信息
        if (error.response?.data) {
            const data = error.response.data;
            // 如果 data 是对象且有 message 字段
            if (data && typeof data === 'object') {
                msg = data.message || JSON.stringify(data);
            } else {
                // 如果 data 是字符串
                msg = String(data);
            }
        } else if (error.message) {
            msg = error.message;
        }
        showFailToast(msg);
    }
};

const setDefault = async (id) => {
    const addr = addresses.value.find(a => a.id === id);
    if (!addr || addr.isDefault) return;

    try {
        // We need to send the full object for update usually, or a specific endpoint
        // Assuming updateAddress handles full object update
        await updateAddress(id, { ...addr, isDefault: true });
        showSuccessToast('设置成功');
        await loadAddresses();
    } catch (error) {
        showFailToast('设置失败');
    }
};

const deleteAddress = (id) => {
    showConfirmDialog({
        title: '删除地址',
        message: '确定要删除这个地址吗？',
    })
        .then(async () => {
            try {
                await apiDeleteAddress(id);
                showSuccessToast('删除成功');
                await loadAddresses();
            } catch (error) {
                showFailToast('删除失败');
            }
        })
        .catch(() => { });
};

const formatPhone = (phone) => {
    if (!phone) return '';
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

onMounted(() => {
    loadAddresses();
});
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12 relative overflow-x-hidden">
        <main class="mx-auto max-w-[1480px] space-y-6 px-8 py-6">

                <!-- --- Header --- -->
                <div class="flex items-center justify-between">
                    <div>
                        <h1 class="text-2xl font-bold text-[#2c3e50]">收货地址</h1>
                        <p class="text-xs text-gray-400 mt-1">管理您的收货地址，下单更便捷</p>
                    </div>
                    <button @click="openEditor()"
                        class="bg-[#4a8b6e] hover:bg-[#3b755b] text-white px-5 py-2.5 rounded-full font-bold shadow-lg shadow-[#4a8b6e]/20 transition-all flex items-center gap-2 active:scale-95">
                        <Plus :size="18" /> 新增地址
                    </button>
                </div>

                <!-- --- Address List --- -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

                    <div v-for="addr in addresses" :key="addr.id" :class="[
                        'bg-white rounded-2xl p-5 border transition-all cursor-pointer group relative overflow-hidden',
                        addr.isDefault
                            ? 'border-[#4a8b6e] shadow-md shadow-[#4a8b6e]/5 ring-1 ring-[#4a8b6e]/20'
                            : 'border-gray-100 shadow-sm hover:border-[#4a8b6e]/30 hover:shadow-md'
                    ]">
                        <!-- Background Decoration for Default -->
                        <div v-if="addr.isDefault" class="absolute top-0 right-0">
                            <div
                                class="bg-[#4a8b6e] text-white text-[10px] font-bold px-3 py-1 rounded-bl-xl shadow-sm">
                                默认
                            </div>
                        </div>

                        <div class="flex flex-col h-full justify-between">
                            <div>
                                <div class="flex items-center gap-2 mb-1">
                                    <span class="text-lg font-bold text-[#2c3e50]">{{ addr.recipientName }}</span>
                                    <span v-if="addr.tag"
                                        class="text-[10px] px-1.5 py-0.5 rounded bg-gray-100 text-gray-500 border border-gray-200">{{
                                            addr.tag }}</span>
                                </div>
                                <div class="text-sm font-medium text-gray-500 mb-3 flex items-center gap-1">
                                    {{ formatPhone(addr.phone) }}
                                </div>

                                <div class="text-sm text-gray-600 leading-relaxed pr-8">
                                    <span class="text-gray-400 mr-1">{{ addr.region }}</span>
                                    {{ addr.detailedAddress }}
                                </div>
                            </div>

                            <div
                                class="mt-5 pt-4 border-t border-gray-50 flex items-center justify-between opacity-0 group-hover:opacity-100 transition-opacity">
                                <div class="flex items-center gap-1 cursor-pointer" @click.stop="setDefault(addr.id)">
                                    <div
                                        :class="['w-4 h-4 rounded-full border flex items-center justify-center', addr.isDefault ? 'border-[#4a8b6e] bg-[#4a8b6e]' : 'border-gray-300']">
                                        <Check v-if="addr.isDefault" :size="10" class="text-white" />
                                    </div>
                                    <span
                                        :class="['text-xs', addr.isDefault ? 'text-[#4a8b6e] font-bold' : 'text-gray-400 hover:text-gray-600']">
                                        {{ addr.isDefault ? '默认地址' : '设为默认' }}
                                    </span>
                                </div>

                                <div class="flex gap-3">
                                    <button @click.stop="openEditor(addr)"
                                        class="text-gray-400 hover:text-[#4a8b6e] transition-colors flex items-center gap-1 text-xs font-medium">
                                        <Edit3 :size="14" /> 编辑
                                    </button>
                                    <button @click.stop="deleteAddress(addr.id)"
                                        class="text-gray-400 hover:text-[#ff5e57] transition-colors flex items-center gap-1 text-xs font-medium">
                                        <Trash2 :size="14" /> 删除
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Add New Placeholder -->
                    <button @click="openEditor()"
                        class="rounded-2xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center p-8 gap-3 text-gray-400 hover:border-[#4a8b6e] hover:text-[#4a8b6e] hover:bg-[#4a8b6e]/5 transition-all group min-h-[180px]">
                        <div
                            class="w-12 h-12 rounded-full bg-gray-50 group-hover:bg-[#4a8b6e]/10 flex items-center justify-center transition-colors">
                            <Plus :size="24" />
                        </div>
                        <span class="font-bold text-sm">添加新地址</span>
                    </button>

                </div>
        </main>

        <!-- --- Edit Drawer (Overlay) --- -->
        <div v-if="isEditorOpen" class="fixed inset-0 z-50 flex justify-end">
            <!-- Backdrop -->
            <div class="absolute inset-0 bg-black/20 backdrop-blur-sm transition-opacity" @click="closeEditor"></div>

            <!-- Drawer Content -->
            <div
                class="relative w-full max-w-md bg-white h-full shadow-2xl p-6 flex flex-col transform transition-transform duration-300 animate-slide-in">

                <div class="flex items-center justify-between mb-8">
                    <h2 class="text-xl font-bold text-[#2c3e50]">{{ isEditing ? '编辑地址' : '新增地址' }}</h2>
                    <button @click="closeEditor"
                        class="p-2 hover:bg-gray-100 rounded-full text-gray-500 transition-colors">
                        <X :size="20" />
                    </button>
                </div>

                <div class="flex-1 space-y-6 overflow-y-auto">
                    <!-- Name & Phone -->
                    <div class="grid grid-cols-2 gap-4">
                        <div class="space-y-1.5">
                            <label class="text-xs font-bold text-gray-500 uppercase">联系人</label>
                            <input type="text" v-model="form.recipientName"
                                class="w-full bg-gray-50 border-none rounded-xl px-4 py-3 text-sm font-medium focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none"
                                placeholder="名字">
                        </div>
                        <div class="space-y-1.5">
                            <label class="text-xs font-bold text-gray-500 uppercase">手机号</label>
                            <input type="text" v-model="form.phone"
                                class="w-full bg-gray-50 border-none rounded-xl px-4 py-3 text-sm font-medium focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none"
                                placeholder="11位手机号">
                        </div>
                    </div>

                    <!-- Region -->
                    <div class="space-y-1.5">
                        <label class="text-xs font-bold text-gray-500 uppercase">所在地区</label>
                        <div @click="showAreaPicker = true"
                            class="flex items-center justify-between bg-gray-50 rounded-xl px-4 py-3 cursor-pointer hover:bg-gray-100 transition-colors">
                            <span class="text-sm font-medium text-gray-800">{{ form.region || '选择省 / 市 / 区' }}</span>
                            <ChevronRight :size="16" class="text-gray-400" />
                        </div>
                    </div>

                    <!-- Detail Address -->
                    <div class="space-y-1.5">
                        <label class="text-xs font-bold text-gray-500 uppercase">详细地址</label>
                        <textarea v-model="form.detailedAddress" rows="3"
                            class="w-full bg-gray-50 border-none rounded-xl px-4 py-3 text-sm font-medium focus:ring-2 focus:ring-[#4a8b6e]/20 outline-none resize-none"
                            placeholder="街道、楼牌号等"></textarea>
                    </div>

                    <!-- Tags -->
                    <div class="space-y-2">
                        <label class="text-xs font-bold text-gray-500 uppercase">标签</label>
                        <div class="flex gap-2">
                            <button v-for="tag in ['家', '公司', '学校']" :key="tag" @click="form.tag = tag"
                                :class="['px-4 py-1.5 rounded-full text-xs font-bold transition-all border', form.tag === tag ? 'bg-[#4a8b6e] text-white border-[#4a8b6e]' : 'bg-white text-gray-500 border-gray-200 hover:border-gray-300']">
                                {{ tag }}
                            </button>
                        </div>
                    </div>

                    <!-- Default Switch -->
                    <div class="flex items-center justify-between pt-4 border-t border-gray-50">
                        <div>
                            <div class="font-bold text-sm text-[#2c3e50]">设为默认地址</div>
                            <div class="text-xs text-gray-400">每次下单时会优先推荐使用</div>
                        </div>
                        <button @click="form.isDefault = !form.isDefault"
                            :class="['w-12 h-7 rounded-full p-1 transition-colors duration-300', form.isDefault ? 'bg-[#4a8b6e]' : 'bg-gray-200']">
                            <div
                                :class="['w-5 h-5 bg-white rounded-full shadow-sm transform transition-transform duration-300', form.isDefault ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </button>
                    </div>

                </div>

                <div class="pt-6 border-t border-gray-50">
                    <button @click="saveAddress"
                        class="w-full bg-[#4a8b6e] hover:bg-[#3b755b] text-white py-3.5 rounded-xl font-bold shadow-lg shadow-[#4a8b6e]/20 transition-all active:scale-95">
                        保存地址
                    </button>
                </div>

            </div>
        </div>

        <!-- Area Picker Popup -->
        <van-popup v-model:show="showAreaPicker" position="bottom" round>
            <van-area :area-list="areaList" @confirm="onAreaConfirm" @cancel="showAreaPicker = false" />
        </van-popup>

    </div>
</template>

<style scoped>
@keyframes slideIn {
    from {
        transform: translateX(100%);
    }

    to {
        transform: translateX(0);
    }
}

.animate-slide-in {
    animation: slideIn 0.3s ease-out forwards;
}
</style>
