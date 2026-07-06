<script setup>
import { useUserStore } from '@/stores/user';
import {
    Bell,
    Check,
    ChevronRight,
    LogOut,
    Megaphone,
    Moon,
    Sparkles,
    Trash2
} from 'lucide-vue-next';
import { showConfirmDialog, showSuccessToast, showToast } from 'vant';
import { onMounted, ref, watchEffect } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const userStore = useUserStore();

// --- State ---
const settings = ref({
    notifications: true,
    marketing: false,
    personalization: true
});
const cacheSize = ref('计算中...');
const appVersion = ref('1.0.0');
const KEY = 'tu.settings';

// --- Logic ---
const loadSettings = () => {
    try {
        const v = JSON.parse(localStorage.getItem(KEY) || '{}');
        if (typeof v.notifications === 'boolean') settings.value.notifications = v.notifications;
        if (typeof v.marketing === 'boolean') settings.value.marketing = v.marketing;
        if (typeof v.personalization === 'boolean') settings.value.personalization = v.personalization;
    } catch { }
};

const saveSettings = () => {
    localStorage.setItem(KEY, JSON.stringify(settings.value));
};

const calculateCacheSize = () => {
    try {
        let total = 0;
        for (let key in localStorage) {
            if (localStorage.hasOwnProperty(key)) {
                total += localStorage.getItem(key).length * 2; // UTF-16
            }
        }
        if (total < 1024) {
            cacheSize.value = `${total} B`;
        } else if (total < 1024 * 1024) {
            cacheSize.value = `${(total / 1024).toFixed(1)} KB`;
        } else {
            cacheSize.value = `${(total / 1024 / 1024).toFixed(1)} MB`;
        }
    } catch {
        cacheSize.value = '未知';
    }
};

const clearCache = () => {
    if (!cacheSize.value || cacheSize.value === '0 B') return;

    showConfirmDialog({
        title: '清除缓存',
        message: '确定要清除本地缓存吗？这将清除图片缓存和临时数据。',
    })
        .then(() => {
            // Keep critical keys
            const keysToKeep = ['token', 'token_expires_at', 'user', KEY];
            const keysToRemove = [];
            for (let key in localStorage) {
                if (localStorage.hasOwnProperty(key) && !keysToKeep.includes(key)) {
                    keysToRemove.push(key);
                }
            }
            keysToRemove.forEach(key => localStorage.removeItem(key));
            calculateCacheSize();
            showSuccessToast('缓存已清除');
        })
        .catch(() => { });
};

const logout = () => {
    showConfirmDialog({
        title: '退出登录',
        message: '确定要退出登录吗？',
        confirmButtonColor: '#ff5e57',
    })
        .then(async () => {
            await userStore.logout();
            showSuccessToast('已退出');
            router.push('/login');
        })
        .catch(() => { });
};

const toggleTheme = () => {
    showToast('深色模式后续接入');
};

onMounted(() => {
    loadSettings();
    calculateCacheSize();
});

watchEffect(saveSettings);
</script>

<template>
    <div class="min-h-screen bg-[#f7f9fa] font-sans text-[#2c3e50] pb-12">
        <main class="mx-auto max-w-[960px] space-y-6 px-8 py-6">

            <!-- --- Header --- -->
            <div class="mb-2">
                <h1 class="text-2xl font-bold text-[#2c3e50]">系统设置</h1>
            </div>

            <!-- --- Group 1: Notifications & Privacy --- -->
            <section class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-50 bg-gray-50/50">
                    <h2 class="text-xs font-bold text-gray-400 uppercase tracking-wider">通知与隐私</h2>
                </div>

                <div class="divide-y divide-gray-50">
                    <!-- Item -->
                    <div class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors">
                        <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center">
                                <Bell :size="16" />
                            </div>
                            <span class="text-sm font-medium text-gray-700">消息通知</span>
                        </div>
                        <!-- Custom Switch Component -->
                        <div @click="settings.notifications = !settings.notifications"
                            :class="['w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300', settings.notifications ? 'bg-[#4a8b6e]' : 'bg-gray-200']">
                            <div
                                :class="['bg-white w-4 h-4 rounded-full shadow-md transform transition-transform duration-300', settings.notifications ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </div>
                    </div>

                    <!-- Item -->
                    <div class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors">
                        <div class="flex items-center gap-3">
                            <div
                                class="w-8 h-8 rounded-full bg-orange-50 text-orange-500 flex items-center justify-center">
                                <Megaphone :size="16" />
                            </div>
                            <span class="text-sm font-medium text-gray-700">推送营销消息</span>
                        </div>
                        <div @click="settings.marketing = !settings.marketing"
                            :class="['w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300', settings.marketing ? 'bg-[#4a8b6e]' : 'bg-gray-200']">
                            <div
                                :class="['bg-white w-4 h-4 rounded-full shadow-md transform transition-transform duration-300', settings.marketing ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </div>
                    </div>

                    <!-- Item -->
                    <div class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors">
                        <div class="flex items-center gap-3">
                            <div
                                class="w-8 h-8 rounded-full bg-purple-50 text-purple-500 flex items-center justify-center">
                                <Sparkles :size="16" />
                            </div>
                            <span class="text-sm font-medium text-gray-700">个性化推荐</span>
                        </div>
                        <div @click="settings.personalization = !settings.personalization"
                            :class="['w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors duration-300', settings.personalization ? 'bg-[#4a8b6e]' : 'bg-gray-200']">
                            <div
                                :class="['bg-white w-4 h-4 rounded-full shadow-md transform transition-transform duration-300', settings.personalization ? 'translate-x-5' : 'translate-x-0']">
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- --- Group 2: Appearance & Cache --- -->
            <section class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-50 bg-gray-50/50">
                    <h2 class="text-xs font-bold text-gray-400 uppercase tracking-wider">通用</h2>
                </div>

                <div class="divide-y divide-gray-50">
                    <!-- Appearance -->
                    <div @click="toggleTheme"
                        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer group">
                        <div class="flex items-center gap-3">
                            <div
                                class="w-8 h-8 rounded-full bg-gray-100 text-gray-600 flex items-center justify-center">
                                <Moon :size="16" />
                            </div>
                            <span class="text-sm font-medium text-gray-700">深色模式</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-xs text-gray-400 group-hover:text-gray-600 transition-colors">跟随系统</span>
                            <ChevronRight :size="16"
                                class="text-gray-300 group-hover:text-gray-500 transition-colors" />
                        </div>
                    </div>

                    <!-- Clear Cache -->
                    <div @click="clearCache"
                        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer group">
                        <div class="flex items-center gap-3">
                            <div
                                class="w-8 h-8 rounded-full bg-green-50 text-green-600 flex items-center justify-center">
                                <Trash2 :size="16" />
                            </div>
                            <span class="text-sm font-medium text-gray-700">清除缓存</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <span v-if="cacheSize && cacheSize !== '0 B'"
                                class="text-xs text-gray-400 group-hover:text-gray-600 transition-colors">{{ cacheSize
                                }}</span>
                            <span v-else class="text-xs text-[#4a8b6e] font-medium flex items-center gap-1">
                                <Check :size="12" /> 已清理
                            </span>
                            <ChevronRight :size="16"
                                class="text-gray-300 group-hover:text-gray-500 transition-colors" />
                        </div>
                    </div>
                </div>
            </section>

            <!-- --- Group 3: About --- -->
            <section class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-50 bg-gray-50/50">
                    <h2 class="text-xs font-bold text-gray-400 uppercase tracking-wider">关于</h2>
                </div>

                <div class="divide-y divide-gray-50">
                    <!-- Version -->
                    <div class="flex items-center justify-between px-6 py-4">
                        <div class="flex items-center gap-3">
                            <span class="text-sm font-medium text-gray-700">版本号</span>
                        </div>
                        <span class="text-xs font-mono text-gray-400 bg-gray-100 px-2 py-1 rounded">v{{ appVersion
                            }}</span>
                    </div>

                    <!-- User Agreement -->
                    <div
                        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer group">
                        <span class="text-sm font-medium text-gray-700">用户协议</span>
                        <ChevronRight :size="16" class="text-gray-300 group-hover:text-gray-500 transition-colors" />
                    </div>

                    <!-- Privacy Policy -->
                    <div
                        class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer group">
                        <span class="text-sm font-medium text-gray-700">隐私政策</span>
                        <ChevronRight :size="16" class="text-gray-300 group-hover:text-gray-500 transition-colors" />
                    </div>
                </div>
            </section>

            <!-- --- Logout Button --- -->
            <div class="pt-4 pb-8">
                <button @click="logout"
                    class="w-full bg-white border border-gray-200 text-[#ff5e57] hover:bg-[#ff5e57] hover:text-white hover:border-[#ff5e57] py-3.5 rounded-xl font-bold transition-all shadow-sm active:scale-95 flex items-center justify-center gap-2">
                    <LogOut :size="18" /> 退出登录
                </button>
                <p class="text-center text-[10px] text-gray-300 mt-4">TrueUsed Inc. All rights reserved.</p>
            </div>

        </main>

    </div>
</template>
