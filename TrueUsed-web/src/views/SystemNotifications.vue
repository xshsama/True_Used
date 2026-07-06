<template>
    <div class="system-notifications-page">
        <van-nav-bar title="系统通知" left-arrow fixed placeholder @click-left="$router.back()">
            <template #right>
                <span class="mark-read-btn" @click="handleMarkAllRead">全部已读</span>
            </template>
        </van-nav-bar>

        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <van-list v-model:loading="notificationStore.loading" :finished="notificationStore.finished"
                finished-text="没有更多了" @load="onLoad">
                <div v-if="notificationStore.notifications.length > 0" class="notification-list">
                    <div v-for="item in notificationStore.notifications" :key="item.id" class="notification-card"
                        :class="{ unread: !item.read }" @click="handleRead(item)">
                        <div class="notification-icon">
                            <van-icon :name="getIcon(item.type)" :color="getIconColor(item.type)" size="24" />
                        </div>
                        <div class="notification-content-wrapper">
                            <div class="card-header">
                                <span class="title">{{ item.title }}</span>
                                <span class="time">{{ formatTime(item.createdAt) }}</span>
                            </div>
                            <div class="card-content">{{ item.content }}</div>
                        </div>
                        <div class="unread-dot" v-if="!item.read"></div>
                    </div>
                </div>
                <van-empty v-else-if="!notificationStore.loading" description="暂无通知" />
            </van-list>
        </van-pull-refresh>
    </div>
</template>

<script setup>
import { useNotificationStore } from '@/stores/notification'
import { showToast } from 'vant'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const notificationStore = useNotificationStore()
const refreshing = ref(false)

const onLoad = () => {
    notificationStore.fetchNotifications()
}

const onRefresh = async () => {
    await notificationStore.fetchNotifications(true)
    refreshing.value = false
}

const handleRead = (item) => {
    if (!item.read) {
        notificationStore.markRead(item.id)
    }

    // Navigation logic
    if (item.type && item.type.startsWith('ORDER_')) {
        router.push(`/order/${item.relatedId}`)
    } else if (item.type && item.type.startsWith('REFUND_')) {
        router.push(`/order/${item.relatedId}/refund-detail`)
    }
}

const handleMarkAllRead = async () => {
    await notificationStore.markAllRead()
    showToast('已全部标记为已读')
}

const formatTime = (isoString) => {
    if (!isoString) return ''
    const date = new Date(isoString)
    const now = new Date()
    const diff = now - date

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
    return date.toLocaleDateString()
}

const getIcon = (type) => {
    if (type && type.includes('ORDER')) return 'orders-o'
    if (type && type.includes('REFUND')) return 'refund-o'
    return 'bell'
}

const getIconColor = (type) => {
    if (type && type.includes('ORDER')) return '#1989fa'
    if (type && type.includes('REFUND')) return '#ee0a24'
    return '#ff976a'
}

onMounted(() => {
    notificationStore.fetchNotifications(true)
})
</script>

<style scoped>
.system-notifications-page {
    background: #f7f8fa;
    min-height: 100vh;
}

.mark-read-btn {
    color: #1989fa;
    font-size: 14px;
    cursor: pointer;
}

.notification-list {
    padding: 0;
}

.notification-card {
    background: #fff;
    padding: 16px;
    margin-bottom: 1px;
    display: flex;
    position: relative;
    transition: background 0.2s;
}

.notification-card:active {
    background: #f5f5f5;
}

.notification-icon {
    margin-right: 12px;
    padding-top: 2px;
}

.notification-content-wrapper {
    flex: 1;
    min-width: 0;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}

.title {
    font-size: 15px;
    font-weight: 500;
    color: #323233;
}

.time {
    font-size: 12px;
    color: #969799;
}

.card-content {
    font-size: 13px;
    color: #646566;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.unread-dot {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 8px;
    height: 8px;
    background: #ee0a24;
    border-radius: 50%;
}
</style>
