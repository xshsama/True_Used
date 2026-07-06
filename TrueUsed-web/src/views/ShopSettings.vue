<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto grid max-w-[1480px] grid-cols-[minmax(0,1fr)_420px] gap-8 px-8 py-6">
            <div class="space-y-6">
                <div class="flex justify-between items-center">
                    <h1 class="text-2xl font-bold text-[#2c3e50]">店铺装修与设置</h1>
                    <button @click="saveChanges" :disabled="loading"
                        class="bg-[#4a8b6e] hover:bg-[#3b755b] text-white px-6 py-2 rounded-full font-bold text-sm shadow-lg shadow-[#4a8b6e]/20 transition-all flex items-center gap-2 active:scale-95 disabled:opacity-50">
                        <Save :size="16" /> {{ loading ? '保存中...' : '保存更改' }}
                    </button>
                </div>

                <!-- 视觉形象 -->
                <section class="bg-white rounded-2xl p-6 border border-gray-100/50 shadow-sm space-y-6">
                    <h2 class="font-bold text-lg border-b border-gray-50 pb-3 mb-4">视觉形象</h2>

                    <!-- 店铺招牌 -->
                    <div>
                        <label class="text-sm font-bold text-gray-700 mb-2 block">店铺招牌 (背景图)</label>
                        <ImageUpload v-model="coverImageList" :max-images="1"
                            class="relative w-full h-32 rounded-xl overflow-hidden group cursor-pointer border-2 border-dashed border-gray-200 hover:border-[#4a8b6e] transition-colors"
                            :height="128" text="更换背景" />
                        <p class="text-xs text-gray-400 mt-2">建议尺寸 1200x400px</p>
                    </div>

                    <!-- 头像 & 名称 -->
                    <div class="flex gap-6 items-start">
                        <div class="flex-shrink-0 text-center">
                            <label class="text-sm font-bold text-gray-700 mb-2 block">店铺头像</label>
                            <div class="w-20 h-20">
                                <ImageUpload v-model="avatarList" :max-images="1" :round="true" :size="80" />
                            </div>
                        </div>

                        <div class="flex-1 space-y-4">
                            <div>
                                <label class="text-sm font-bold text-gray-700 mb-1 block">常驻地区</label>
                                <div class="flex items-center bg-gray-50 rounded-xl px-4 py-2.5 text-sm">
                                    <MapPin :size="14" class="text-[#4a8b6e] mr-2" />
                                    <input type="text" v-model="form.location"
                                        class="bg-transparent border-none w-full outline-none"
                                        placeholder="例如：上海 · 徐汇区" />
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 简介 -->
                    <div>
                        <div class="flex justify-between items-center mb-1">
                            <label class="text-sm font-bold text-gray-700">店铺简介</label>
                            <span class="text-xs text-gray-400">{{ (form.bio || '').length }}/300</span>
                        </div>
                        <textarea v-model="form.bio" rows="3" maxlength="300"
                            class="w-full bg-gray-50 border-none rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-[#4a8b6e]/20 transition-all resize-none"></textarea>
                    </div>
                </section>

                <!-- 经营设置 -->
                <section class="bg-white rounded-2xl p-6 border border-gray-100/50 shadow-sm space-y-6">
                    <h2 class="font-bold text-lg border-b border-gray-50 pb-3 mb-4">经营设置</h2>

                    <!-- 自动回复 -->
                    <div class="flex items-center justify-between">
                        <div>
                            <div class="font-bold text-sm text-[#2c3e50]">自动回复</div>
                            <div class="text-xs text-gray-400">买家首次私信时自动发送</div>
                        </div>
                        <div @click="form.autoReplyEnabled = !form.autoReplyEnabled"
                            :class="['w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300', form.autoReplyEnabled ? 'bg-[#4a8b6e]' : 'bg-gray-200']">
                            <div
                                :class="['bg-white w-4 h-4 rounded-full shadow-md transform transition-transform duration-300', form.autoReplyEnabled ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </div>
                    </div>

                    <div v-if="form.autoReplyEnabled" class="bg-[#4a8b6e]/5 p-3 rounded-xl border border-[#4a8b6e]/10">
                        <textarea v-model="form.autoReplyText"
                            class="w-full bg-transparent border-none text-sm text-[#2c3e50] focus:ring-0 resize-none placeholder-[#4a8b6e]/50"
                            rows="2" placeholder="输入欢迎语..."></textarea>
                    </div>

                    <!-- 休假模式 -->
                    <div class="flex items-center justify-between">
                        <div>
                            <div class="font-bold text-sm text-[#2c3e50]">休假模式</div>
                            <div class="text-xs text-gray-400">开启后商品将不可被购买</div>
                        </div>
                        <div @click="form.vacationMode = !form.vacationMode"
                            :class="['w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300', form.vacationMode ? 'bg-[#ff5e57]' : 'bg-gray-200']">
                            <div
                                :class="['bg-white w-4 h-4 rounded-full shadow-md transform transition-transform duration-300', form.vacationMode ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </div>
                    </div>
                </section>
            </div>

            <!-- 右侧：实时预览 -->
            <div class="space-y-4">
                <div class="sticky top-[120px]">
                    <div class="flex items-center justify-between mb-4 px-2">
                        <span class="text-sm font-bold text-gray-500">实时预览</span>
                        <span class="text-xs bg-gray-200 text-gray-500 px-2 py-0.5 rounded">Desktop Web</span>
                    </div>

                    <!-- Desktop Preview Container -->
                    <div
                        class="bg-[#f7f9fa] rounded-2xl border border-gray-200 shadow-sm overflow-hidden relative min-h-[600px]">
                        <!-- Browser Top Bar -->
                        <div class="bg-gray-100 border-b border-gray-200 px-4 py-2 flex gap-1.5">
                            <div class="w-2.5 h-2.5 rounded-full bg-red-400"></div>
                            <div class="w-2.5 h-2.5 rounded-full bg-yellow-400"></div>
                            <div class="w-2.5 h-2.5 rounded-full bg-green-400"></div>
                        </div>

                        <!-- Cover Image -->
                        <div class="h-40 relative overflow-hidden bg-gray-800 group">
                            <img :src="form.coverImage || 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?auto=format&fit=crop&q=80&w=800'"
                                class="w-full h-full object-cover opacity-90 group-hover:scale-105 transition-transform duration-700" />
                            <div
                                class="absolute inset-0 bg-gradient-to-t from-[#f7f9fa] via-transparent to-transparent">
                            </div>
                        </div>

                        <!-- Content -->
                        <div class="px-6 relative -mt-16 pb-8">
                            <!-- Profile Card -->
                            <div
                                class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 text-center relative overflow-hidden">
                                <div
                                    class="h-1.5 w-full absolute top-0 left-0 bg-gradient-to-r from-[#4a8b6e] to-[#2c3e50]">
                                </div>

                                <!-- Avatar -->
                                <div class="relative w-24 h-24 mx-auto mb-4">
                                    <img :src="resolveAvatar(form.avatarUrl)"
                                        class="w-full h-full rounded-full object-cover border-4 border-white shadow-md" />
                                </div>

                                <!-- Name -->
                                <h1 class="text-xl font-bold text-[#2c3e50] mb-1">
                                    {{ form.nickname || form.username || '店主' }}
                                </h1>
                                <p class="text-xs text-gray-400 mb-4">加入时间：2023-12-01</p>

                                <!-- Stats -->
                                <div class="flex justify-center gap-4 border-t border-b border-gray-50 py-3 mb-4">
                                    <div>
                                        <div class="text-lg font-bold text-[#2c3e50]">12</div>
                                        <div class="text-xs text-gray-400">在售</div>
                                    </div>
                                    <div>
                                        <div class="text-lg font-bold text-[#2c3e50]">85</div>
                                        <div class="text-xs text-gray-400">已售</div>
                                    </div>
                                    <div>
                                        <div class="text-lg font-bold text-[#2c3e50]">128</div>
                                        <div class="text-xs text-gray-400">关注</div>
                                    </div>
                                </div>

                                <!-- Buttons (Visual only) -->
                                <div class="flex gap-2 opacity-50 pointer-events-none scale-90">
                                    <button
                                        class="flex-1 bg-[#2c3e50] text-white py-2 rounded-xl text-xs font-bold">关注</button>
                                    <button
                                        class="flex-1 border border-gray-200 text-gray-600 py-2 rounded-xl text-xs font-bold">私信</button>
                                </div>
                            </div>

                            <!-- Info Card -->
                            <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 space-y-4 mt-4">
                                <div class="flex items-start gap-3 text-xs text-gray-600">
                                    <MapPin :size="14" class="text-gray-400 mt-0.5" />
                                    <span>{{ form.location || '未设置地区' }}</span>
                                </div>
                                <div class="flex items-start gap-3 text-xs text-gray-600">
                                    <Clock :size="14" class="text-gray-400 mt-0.5" />
                                    <span>平均回复：<span class="text-[#4a8b6e] font-bold">10分钟内</span></span>
                                </div>

                                <div class="border-t border-gray-50 pt-4 mt-2">
                                    <h3 class="text-[10px] font-bold text-gray-400 uppercase mb-2">店铺简介</h3>
                                    <p class="text-xs text-gray-600 leading-relaxed break-words">
                                        {{ form.bio || '暂无简介' }}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup>
import ImageUpload from '@/components/ImageUpload.vue';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { Clock, MapPin, Save } from 'lucide-vue-next';
import { showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';

const userStore = useUserStore();
const loading = ref(false);

const form = ref({
    nickname: '',
    avatarUrl: '',
    coverImage: '',
    location: '',
    bio: '',
    autoReplyEnabled: false,
    autoReplyText: '',
    vacationMode: false
});

const coverImageList = computed({
    get: () => form.value.coverImage ? [form.value.coverImage] : [],
    set: (val) => {
        form.value.coverImage = (val && val.length > 0) ? val[0] : '';
    }
});

const avatarList = computed({
    get: () => form.value.avatarUrl ? [form.value.avatarUrl] : [],
    set: (val) => {
        form.value.avatarUrl = (val && val.length > 0) ? val[0] : '';
    }
});

onMounted(async () => {
    await userStore.loadMe();
    const user = userStore.user;
    if (user) {
        form.value = { ...user };
    }
});

const saveChanges = async () => {
    loading.value = true;
    try {
        await userStore.saveMe(form.value);
        showSuccessToast('保存成功');
    } catch (error) {
        showFailToast('保存失败');
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
</style>
