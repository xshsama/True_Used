<template>
    <nav class="sticky top-0 z-50 border-b border-white/60 bg-white/88 backdrop-blur-xl shadow-[0_12px_30px_rgba(15,23,42,0.06)]">
        <div class="mx-auto flex h-[84px] w-full max-w-[1560px] items-center gap-4 px-5">
            <div class="flex min-w-0 shrink items-center gap-5">
                <button
                    class="flex shrink-0 items-center gap-3 rounded-full bg-transparent px-0 py-0 text-left transition-transform hover:scale-[1.01]"
                    type="button" @click="goHome">
                    <div
                        class="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-[#0b8a61] to-[#0f5b45] text-lg font-black italic text-white shadow-[0_10px_24px_rgba(0,135,90,0.24)]">
                        T
                    </div>
                    <div>
                        <div class="text-[24px] font-black tracking-tight text-[#1f2d3d]">
                            TrueUsed<span class="text-[#0b8a61]">.</span>
                        </div>
                        <div class="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-400">
                            Desktop Market Console
                        </div>
                    </div>
                </button>

                <div
                    class="flex min-w-0 shrink items-center gap-1 rounded-full border border-slate-200/80 bg-slate-50/90 p-1 shadow-inner">
                    <button v-for="item in navItems" :key="item.to" type="button"
                        class="inline-flex min-w-[74px] items-center justify-center whitespace-nowrap rounded-full px-3.5 py-2.5 text-[14px] font-semibold leading-none transition-all"
                        :class="isActiveLink(item) ? 'bg-white text-[#0b8a61] shadow-sm' : 'text-slate-500 hover:text-slate-900'"
                        @click="navigate(item.to)">
                        {{ item.label }}
                    </button>
                </div>
            </div>

            <div v-if="mode === 'buyer'" class="min-w-0 flex-[0_1_360px]">
                <SearchBar :show-shortcut="false" @search="goSearch" />
            </div>

            <div class="ml-auto flex shrink-0 items-center gap-2">
                <button v-if="mode === 'buyer'" type="button" class="desktop-icon-button" title="消息"
                    aria-label="消息" @click="navigate('/messages')">
                    <span class="i-lucide-message-circle text-[18px]"></span>
                    <span v-if="unreadCount > 0" class="desktop-icon-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
                </button>

                <button v-if="mode === 'buyer'" type="button" class="desktop-icon-button" title="收藏"
                    aria-label="收藏" @click="navigate('/favorites')">
                    <span class="i-lucide-heart text-[18px]"></span>
                </button>

                <button v-if="mode === 'buyer'" type="button"
                    class="inline-flex min-w-[102px] items-center justify-center gap-2 whitespace-nowrap rounded-full border border-emerald-300/70 bg-emerald-50 px-4 py-2.5 text-sm font-semibold text-[#0b8a61] transition-all hover:border-emerald-400 hover:bg-emerald-100"
                    @click="navigate('/my-products')">
                    <span class="i-lucide-layout-dashboard text-[16px]"></span>
                    卖家工作台
                </button>

                <button type="button"
                    class="inline-flex min-w-[118px] items-center justify-center gap-2 whitespace-nowrap rounded-full bg-gradient-to-br from-[#00875A] to-[#064E3B] px-4 py-2.5 text-sm font-bold text-white shadow-[0_12px_24px_rgba(0,135,90,0.24)] transition-all hover:translate-y-[-1px]"
                    @click="navigate('/post/create')">
                    <span class="i-lucide-plus w-4 h-4"></span>
                    发布商品
                </button>

                <div ref="menuRef" class="relative z-50">
                    <button type="button"
                        class="flex min-w-[138px] max-w-[166px] items-center gap-2.5 rounded-full border border-slate-200 bg-white px-3 py-2 shadow-sm transition-all hover:border-emerald-200 hover:shadow-md"
                        @click.stop="toggleMenu">
                        <div class="h-9 w-9 shrink-0 overflow-hidden rounded-full border border-slate-100 bg-slate-100">
                            <img :src="avatarSrc" class="h-full w-full object-cover" />
                        </div>
                        <div class="min-w-0 flex-1 pr-1 text-left">
                            <div class="truncate text-sm font-semibold text-slate-800">{{ userStore.user?.nickname || userStore.user?.username || '访客' }}</div>
                            <div class="truncate whitespace-nowrap text-[11px] text-slate-400">{{ userStore.isLoggedIn ? '账户中心' : '点击登录' }}</div>
                        </div>
                        <span class="i-lucide-chevron-down shrink-0 text-slate-400 transition-transform" :class="{ 'rotate-180': menuOpen }"></span>
                    </button>

                    <div v-if="menuOpen"
                        class="absolute right-0 top-full mt-3 w-56 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_18px_50px_rgba(15,23,42,0.16)]">
                        <template v-if="!userStore.isLoggedIn">
                            <button type="button" class="desktop-menu-item font-semibold" @click="navigate('/login')">登录 / 注册</button>
                        </template>
                        <template v-else>
                            <template v-for="(section, sectionIndex) in menuSections" :key="sectionIndex">
                                <button v-for="item in section" :key="item.label" type="button" class="desktop-menu-item"
                                    :class="{ 'desktop-menu-item--active': isActiveMenuItem(item) }"
                                    @click="navigate(item.to)">
                                    {{ item.label }}
                                </button>
                                <div v-if="sectionIndex < menuSections.length - 1" class="mx-2 my-2 h-px bg-slate-100"></div>
                            </template>
                            <button type="button" class="desktop-menu-item !text-red-500 hover:!bg-red-50" @click="handleLogout">退出登录</button>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </nav>
</template>

<script setup>
import SearchBar from '@/components/SearchBar.vue';
import { useMessageStore } from '@/stores/message';
import { useUserStore } from '@/stores/user';
import { resolveAvatar } from '@/utils/avatar';
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const props = defineProps({
    mode: {
        type: String,
        default: 'buyer'
    }
});

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const messageStore = useMessageStore();
const menuOpen = ref(false);
const menuRef = ref(null);

const unreadCount = computed(() => messageStore.unreadCount);
const avatarSrc = computed(() => resolveAvatar(userStore.user?.avatarUrl, userStore.user?.avatar));
const navItems = computed(() => {
    if (props.mode === 'seller') {
        return [
            { label: '商品管理', to: '/my-products' },
            { label: '数据中心', to: '/seller/data-center' },
            { label: '订单管理', to: '/order-manage' },
            { label: '评价管理', to: '/my-reviews' },
            { label: '店铺设置', to: '/shop-settings' }
        ];
    }
    return [
        { label: '首页', to: '/home' },
        { label: '捡漏榜', to: '/ranking' },
        { label: '验货报告', to: '/inspection-reports' },
        { label: '帮助中心', to: '/help' }
    ];
});

const menuSections = computed(() => {
    if (props.mode === 'seller') {
        const sections = [[
            { label: '个人中心', to: '/profile', match: '/profile' },
            { label: '系统设置', to: { path: '/settings', query: { mode: 'seller' } }, match: '/settings' }
        ]];
        if (userStore.isAdmin) {
            sections.push([
                { label: '管理控制台', to: '/admin', match: '/admin' }
            ]);
        }
        return sections;
    }

    const sections = [
        [
            { label: '个人中心', to: '/profile', match: '/profile' },
            { label: '我的订单', to: '/orders', match: '/orders' },
            { label: '我的收藏', to: '/favorites', match: '/favorites' },
            { label: '浏览记录', to: '/history', match: '/history' },
            { label: '收货地址', to: '/address', match: '/address' }
        ],
        [
            { label: '设置', to: '/settings', match: '/settings' }
        ]
    ];
    if (userStore.isAdmin) {
        sections.push([
            { label: '管理控制台', to: '/admin', match: '/admin' }
        ]);
    }
    return sections;
});

const navigate = (to) => {
    menuOpen.value = false;
    router.push(to);
};

const isActiveLink = (item) => {
    if (item.to === '/home') {
        return route.path === '/' || route.path.startsWith('/home');
    }
    return route.path.startsWith(item.to);
};

const isActiveMenuItem = (item) => {
    const target = item.match || item.to;
    if (typeof target !== 'string') return false;
    return route.path === target || route.path.startsWith(`${target}/`);
};

const goHome = () => {
    navigate('/');
};

const goSearch = (kw) => {
    if (!kw) return;
    navigate({ path: '/search', query: { q: kw } });
};

const toggleMenu = () => {
    menuOpen.value = !menuOpen.value;
};

const handleDocumentClick = (event) => {
    if (menuRef.value && !menuRef.value.contains(event.target)) {
        menuOpen.value = false;
    }
};

const handleLogout = () => {
    menuOpen.value = false;
    userStore.logout();
    router.push('/login');
};

onMounted(() => {
    document.addEventListener('click', handleDocumentClick);
});

onBeforeUnmount(() => {
    document.removeEventListener('click', handleDocumentClick);
});
</script>

<style scoped>
.desktop-icon-button {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 46px;
    height: 46px;
    border-radius: 9999px;
    border: 1px solid rgba(226, 232, 240, 0.9);
    background: rgba(255, 255, 255, 0.92);
    padding: 0;
    color: #475569;
    transition: all 0.2s ease;
}

.desktop-icon-button:hover {
    border-color: rgba(11, 138, 97, 0.25);
    color: #0b8a61;
    box-shadow: 0 10px 18px rgba(15, 23, 42, 0.08);
}

.desktop-icon-badge {
    position: absolute;
    right: -4px;
    top: -6px;
    min-width: 20px;
    border-radius: 9999px;
    border: 2px solid #fff;
    background: #ef4444;
    padding: 2px 6px;
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    text-align: center;
}

.desktop-menu-item {
    display: block;
    width: 100%;
    border: 0;
    border-radius: 12px;
    background: transparent;
    padding: 10px 12px;
    text-align: left;
    font-size: 14px;
    color: #334155;
    transition: all 0.15s ease;
}

.desktop-menu-item:hover {
    background: #f8fafc;
}

.desktop-menu-item--active {
    background: rgba(11, 138, 97, 0.08);
    color: #0b8a61;
    font-weight: 600;
}
</style>
