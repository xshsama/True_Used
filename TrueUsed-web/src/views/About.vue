<template>
    <div class="page">
        <van-nav-bar title="关于我们" left-arrow @click-left="$router.back()" fixed />
        <div class="container" style="padding-top:56px;">
            <div class="about-card shadow-soft-lg">
                <div class="logo">TU</div>
                <div class="name">TrueUsed</div>
                <div class="desc">一个专注于“安全可信”的二手交易平台</div>
                <div class="ver">版本：1.0.0</div>
            </div>
            <van-cell-group inset>
                <van-cell title="项目主页" value="GitHub" is-link @click="open('https://github.com/xshsama/trueused')" />
                <van-cell title="开源协议" value="MIT" />
                <van-cell title="问题反馈" is-link @click="$router.push({ name: 'Feedback' })" />
            </van-cell-group>
        </div>
    </div>
</template>

<script>
export default {
    name: 'About',
    setup() {
        const open = (url) => window.open(url, '_blank')
        return { open }
    }
}
</script>

<style scoped>
.about-card {
    background: #fff;
    border-radius: 16px;
    padding: 16px;
    text-align: center;
    margin: 10px 0 16px;
}

.logo {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(45deg, #60a5fa, #22d3ee);
    color: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 24px;
    margin-bottom: 8px;
}

.name {
    font-weight: 800;
    font-size: 18px;
}

.desc {
    color: #6b7280;
    margin: 6px 0;
}

.ver {
    color: #64748b;
    font-size: 12px;
}
</style>
