<template>
    <div class="min-h-screen bg-[#f0f4f8] text-[#2c3e50] flex items-center justify-center p-4 relative font-sans">
        <!-- Background Texture -->
        <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-100 pointer-events-none"></div>

        <div class="w-full max-w-5xl h-[720px] bg-white rounded-[32px] shadow-2xl overflow-hidden flex relative ring-1 ring-black/5 z-10">
            
            <!-- Left Side: Brand Visuals (Image) -->
            <div class="hidden lg:flex w-5/12 bg-[#2c3e50] relative overflow-hidden flex-col justify-between p-12 text-white">
                <!-- Background Image -->
                <div class="absolute inset-0 z-0">
                    <img src="https://images.unsplash.com/photo-1556761175-5973dc0f32e7?auto=format&fit=crop&q=80&w=1600" class="w-full h-full object-cover opacity-60 mix-blend-overlay" />
                    <div class="absolute inset-0 bg-gradient-to-br from-[#4a8b6e]/90 to-[#2c3e50]/95 mix-blend-multiply"></div>
                    <!-- Decorative Texture -->
                    <div class="absolute inset-0 opacity-10 bg-[url('https://www.transparenttextures.com/patterns/stardust.png')]"></div>
                </div>

                <!-- Logo Area -->
                <div class="relative z-10 flex items-center gap-2.5">
                    <div class="w-9 h-9 bg-white/10 backdrop-blur-md rounded-xl flex items-center justify-center text-white font-bold text-xl italic shadow-inner border border-white/20">T</div>
                    <span class="text-2xl font-bold tracking-tight text-white/90">TrueUsed.</span>
                </div>

                <!-- Slogan -->
                <div class="relative z-10">
                    <div class="inline-block px-3 py-1 mb-4 rounded-full bg-white/10 backdrop-blur-sm border border-white/10 text-xs font-medium text-emerald-100">
                        ✨ 平台验货流程展示
                    </div>
                    <h1 class="text-4xl font-bold leading-[1.15] mb-6 tracking-tight">
                        让闲置好物<br>再次<span class="text-emerald-300">闪闪发光</span>。
                    </h1>
                    <p class="text-white/60 text-sm leading-relaxed max-w-xs">
                        加入 TrueUsed 社区，体验更安全、更透明的二手交易新模式。
                    </p>
                </div>

                <!-- Footer -->
                <div class="relative z-10 text-[10px] text-white/30 font-medium tracking-wide">
                    © 2024 TRUEUSED INC.
                </div>
            </div>

            <!-- Right Side: Form Area -->
            <div class="w-full lg:w-7/12 bg-white flex flex-col justify-center px-8 sm:px-16 lg:px-24 py-12 relative overflow-hidden">
                
                <!-- Decorative Blobs -->
                <div class="blob-1"></div>
                <div class="blob-2"></div>

                <!-- Mobile Logo -->
                <div class="lg:hidden flex items-center justify-center gap-2 mb-8">
                    <div class="w-8 h-8 bg-[#4a8b6e] rounded-lg flex items-center justify-center text-white font-bold text-xl italic">T</div>
                    <span class="text-2xl font-bold text-[#2c3e50]">TrueUsed.</span>
                </div>

                <!-- Header Text -->
                <div class="mb-8 relative z-10">
                    <h2 class="text-2xl font-bold text-[#2c3e50] mb-2">
                        {{ mode === 'login' ? '欢迎回来' : '开启旅程' }}
                    </h2>
                    <p class="text-sm text-gray-400">
                        {{ mode === 'login' ? '请输入您的账号信息以继续' : '只需几步，轻松创建您的账号' }}
                    </p>
                </div>

                <!-- Tabs -->
                <div class="flex gap-8 mb-8 border-b border-gray-100 relative z-10">
                    <button 
                        @click="switchMode('login')"
                        :class="['pb-3 text-base font-bold transition-all relative', mode === 'login' ? 'text-[#2c3e50]' : 'text-gray-400 hover:text-gray-600']"
                    >
                        登录
                        <div v-if="mode === 'login'" class="absolute -bottom-px left-0 w-full h-0.5 bg-[#4a8b6e] rounded-full shadow-[0_-2px_6px_rgba(74,139,110,0.4)]"></div>
                    </button>
                    <button 
                        @click="switchMode('register')"
                        :class="['pb-3 text-base font-bold transition-all relative', mode === 'register' ? 'text-[#2c3e50]' : 'text-gray-400 hover:text-gray-600']"
                    >
                        注册账号
                        <div v-if="mode === 'register'" class="absolute -bottom-px left-0 w-full h-0.5 bg-[#4a8b6e] rounded-full shadow-[0_-2px_6px_rgba(74,139,110,0.4)]"></div>
                    </button>
                </div>

                <!-- Form Container with Transition -->
                <div class="relative flex-1 z-10 min-h-[380px]"> <!-- Increased min-height for register form -->
                    <transition name="fade" mode="out-in">
                        
                        <!-- LOGIN FORM -->
                        <div v-if="mode === 'login'" key="login" class="space-y-6 absolute inset-0">
                            <form @submit.prevent="handleLogin" class="space-y-5">
                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3.5 flex items-center gap-3 border border-transparent">
                                    <Smartphone class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">账号</label>
                                        <input v-model="loginForm.username" type="text" placeholder="手机号码 / 用户名" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                </div>
                                
                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3.5 flex items-center gap-3 border border-transparent">
                                    <Lock class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">密码</label>
                                        <input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="••••••••" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                    <button type="button" @click="showPassword = !showPassword" class="text-gray-400 hover:text-gray-600">
                                        <Eye v-if="showPassword" class="w-4 h-4" />
                                        <EyeOff v-else class="w-4 h-4" />
                                    </button>
                                </div>

                                <div class="flex justify-between items-center text-xs pt-1">
                                    <label class="flex items-center gap-2 cursor-pointer text-gray-500 hover:text-gray-700 select-none group">
                                        <div class="w-4 h-4 rounded border border-gray-300 bg-white flex items-center justify-center transition-colors group-hover:border-[#4a8b6e]" :class="{'bg-[#4a8b6e] border-[#4a8b6e]': rememberMe}">
                                            <input type="checkbox" v-model="rememberMe" class="hidden" />
                                            <div v-if="rememberMe" class="w-2 h-2 bg-white rounded-sm"></div> 
                                        </div>
                                        <span class="font-medium">记住我</span>
                                    </label>
                                    <a href="#" class="text-[#4a8b6e] hover:underline font-bold transition-colors">忘记密码？</a>
                                </div>

                                <button type="submit" :disabled="submitting" class="w-full bg-gradient-to-r from-[#4a8b6e] to-[#3b755b] hover:shadow-lg hover:shadow-[#4a8b6e]/30 text-white py-4 rounded-2xl font-bold text-sm transition-all active:scale-[0.98] flex items-center justify-center gap-2 mt-4 disabled:opacity-70 disabled:cursor-not-allowed">
                                    {{ submitting ? '登录中...' : '立即登录' }}
                                    <ArrowRight v-if="!submitting" class="w-[18px]" />
                                </button>
                            </form>
                        </div>

                        <!-- REGISTER FORM -->
                        <div v-else key="register" class="space-y-6 absolute inset-0 overflow-y-auto pb-4 custom-scrollbar">
                            <form @submit.prevent="handleRegister" class="space-y-4">
                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3 flex items-center gap-3 border border-transparent">
                                    <Smartphone class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">手机号</label>
                                        <input v-model="registerForm.phone" type="tel" placeholder="请输入手机号码" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                </div>
                                
                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3 flex items-center gap-3 border border-transparent">
                                    <Mail class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">邮箱</label>
                                        <input v-model="registerForm.email" type="email" placeholder="请输入邮箱地址" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                </div>

                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3 flex items-center gap-3 border border-transparent">
                                    <Lock class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">设置密码</label>
                                        <input v-model="registerForm.password" :type="showRegisterPassword ? 'text' : 'password'" placeholder="6-16位字符" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                    <button type="button" @click="showRegisterPassword = !showRegisterPassword" class="text-gray-400 hover:text-gray-600">
                                        <Eye v-if="showRegisterPassword" class="w-4 h-4" />
                                        <EyeOff v-else class="w-4 h-4" />
                                    </button>
                                </div>

                                <div class="input-group bg-gray-50 rounded-2xl px-5 py-3 flex items-center gap-3 border border-transparent">
                                    <Lock class="text-gray-400 w-5 h-5 transition-colors" />
                                    <div class="flex-1">
                                        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">确认密码</label>
                                        <input v-model="registerForm.confirm" :type="showRegisterConfirm ? 'text' : 'password'" placeholder="请再次输入密码" class="bg-transparent border-none outline-none text-sm w-full font-bold placeholder:text-gray-300 text-[#2c3e50] h-5" required />
                                    </div>
                                    <button type="button" @click="showRegisterConfirm = !showRegisterConfirm" class="text-gray-400 hover:text-gray-600">
                                        <Eye v-if="showRegisterConfirm" class="w-4 h-4" />
                                        <EyeOff v-else class="w-4 h-4" />
                                    </button>
                                </div>

                                <div class="flex items-start gap-2 text-xs text-gray-500 pt-1">
                                    <!-- 协议勾选小圆圈 -->
                                    <div 
                                        @click="agreed = !agreed"
                                        :class="[
                                            'w-4 h-4 rounded-full border flex-shrink-0 flex items-center justify-center cursor-pointer transition-all duration-300 select-none mt-0.5',
                                            agreed 
                                                ? 'bg-[#4a8b6e] border-[#4a8b6e] scale-110 shadow-[0_2px_8px_rgba(74,139,110,0.4)]' 
                                                : 'bg-white border-gray-400 hover:border-[#4a8b6e] hover:scale-105 shadow-sm'
                                        ]"
                                    >
                                        <!-- 内部选中的白色微型小圆点，带有淡入淡出和缩放动画 -->
                                        <transition name="pop">
                                            <div v-if="agreed" class="w-1.5 h-1.5 bg-white rounded-full"></div>
                                        </transition>
                                    </div>
                                    <span @click="agreed = !agreed" class="cursor-pointer select-none">
                                        我已阅读并同意 
                                        <a href="#" class="text-[#4a8b6e] hover:underline font-bold" @click.stop="openAgreement('user')">《用户协议》</a> 与 
                                        <a href="#" class="text-[#4a8b6e] hover:underline font-bold" @click.stop="openAgreement('privacy')">《隐私政策》</a>
                                    </span>
                                </div>

                                <button type="submit" :disabled="submitting || !agreed" class="w-full bg-[#2c3e50] hover:bg-[#1a252f] hover:shadow-lg text-white py-4 rounded-2xl font-bold text-sm transition-all active:scale-[0.98] mt-2 disabled:opacity-70 disabled:cursor-not-allowed">
                                    {{ submitting ? '注册中...' : '注册并自动登录' }}
                                </button>
                            </form>
                        </div>
                    </transition>
                </div>

            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { loginApi, registerApi } from '@/api/auth';
import { Smartphone, Lock, ArrowRight, Mail, Eye, EyeOff } from 'lucide-vue-next';
import { showToast, showSuccessToast, showFailToast, showLoadingToast, closeToast } from 'vant';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const mode = ref('login');
const submitting = ref(false);

// Login State
const showPassword = ref(false);
const rememberMe = ref(false);
const loginForm = reactive({
    username: '',
    password: ''
});

// Register State
const showRegisterPassword = ref(false);
const showRegisterConfirm = ref(false);
const agreed = ref(false);
const registerForm = reactive({
    phone: '',
    email: '',
    password: '',
    confirm: ''
});

const switchMode = (newMode) => {
    mode.value = newMode;
};

// Initial setup
onMounted(() => {
    // Check for redirect param or path to switch to register mode if needed
    if (route.path === '/register' || route.query.mode === 'register') {
        mode.value = 'register';
    }

    const rememberedPhone = localStorage.getItem('remember_phone');
    if (rememberedPhone) {
        loginForm.username = rememberedPhone;
        rememberMe.value = true;
    }
});

const handleLogin = async () => {
    if (!loginForm.username || !loginForm.password) {
        showToast('请输入账号和密码');
        return;
    }

    submitting.value = true;
    showLoadingToast({ message: '登录中...', duration: 0, forbidClick: true, loadingType: 'spinner' });

    try {
        const response = await loginApi(loginForm);
        const { token } = response;

        if (token) {
            userStore.setToken(token);
            await userStore.loadMe();
            showSuccessToast('登录成功');

            if (rememberMe.value) {
                localStorage.setItem('remember_phone', loginForm.username);
            } else {
                localStorage.removeItem('remember_phone');
            }

            const redirect = route.query.redirect || '/';
            router.replace(redirect);
        } else {
            showFailToast('登录失败，未获取到令牌');
        }
    } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '登录失败，请检查账号密码';
        showFailToast(msg);
    } finally {
        submitting.value = false;
        closeToast();
    }
};

const handleRegister = async () => {
    // Basic validation
    if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
        showToast('手机号格式不正确');
        return;
    }
    if (!/\S+@\S+\.\S+/.test(registerForm.email)) {
        showToast('邮箱格式不正确');
        return;
    }
    if (registerForm.password.length < 6) {
        showToast('密码长度需至少6位');
        return;
    }
    if (registerForm.password !== registerForm.confirm) {
        showToast('两次输入密码不一致');
        return;
    }
    if (!agreed.value) {
        showToast('请阅读并同意协议');
        return;
    }

    submitting.value = true;
    showLoadingToast({ message: '创建账号...', duration: 0, forbidClick: true, loadingType: 'spinner' });

    try {
        const payload = { 
            username: registerForm.phone, 
            email: registerForm.email, 
            password: registerForm.password 
        };
        await registerApi(payload);
        showSuccessToast('注册成功，正在登录...');
        
        // Auto login after register
        const loginResponse = await loginApi({ 
            username: registerForm.phone, 
            password: registerForm.password 
        });
        
        if (loginResponse.token) {
            userStore.setToken(loginResponse.token);
            await userStore.loadMe();
            localStorage.setItem('remember_phone', registerForm.phone);
            router.replace('/');
        } else {
            // Fallback if auto-login fails
            mode.value = 'login';
            loginForm.username = registerForm.phone;
            loginForm.password = '';
        }
    } catch (e) {
        const msg = e?.response?.data?.message || e?.message || '注册失败，请稍后再试';
        showFailToast(msg);
    } finally {
        submitting.value = false;
        closeToast();
    }
};

const openAgreement = (type) => {
    showToast(type === 'user' ? '用户协议（占位）' : '隐私政策（占位）');
};
</script>

<style scoped>
/* 字体优化 */
body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }

/* 输入框聚焦动画 */
.input-group { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.input-group:focus-within { 
    background-color: #fff; 
    box-shadow: 0 4px 20px rgba(74, 139, 110, 0.15);
    border: 1px solid rgba(74, 139, 110, 0.4);
    transform: translateY(-1px);
}
.input-group:focus-within :deep(svg) { color: #4a8b6e; }

/* 切换动画 */
.fade-enter-active,
.fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(8px); }

/* 背景光晕装饰 */
.blob-1 {
    position: absolute;
    top: -10%;
    right: -10%;
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba(74,139,110,0.08) 0%, rgba(255,255,255,0) 70%);
    border-radius: 50%;
    pointer-events: none;
}
.blob-2 {
    position: absolute;
    bottom: -10%;
    left: -10%;
    width: 250px;
    height: 250px;
    background: radial-gradient(circle, rgba(44,62,80,0.05) 0%, rgba(255,255,255,0) 70%);
    border-radius: 50%;
    pointer-events: none;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 2px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
/* 白色小圆点弹出的动效 */
.pop-enter-active {
    transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); /* 弹性动画 */
}
.pop-leave-active {
    transition: all 0.15s ease-in;
}
.pop-enter-from,
.pop-leave-to {
    opacity: 0;
    transform: scale(0); /* 从 0 放大到 1 */
}
</style>
