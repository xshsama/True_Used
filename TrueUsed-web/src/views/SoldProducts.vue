<template>
    <div class="page">
        <van-nav-bar title="已售出" left-arrow @click-left="$router.back()" fixed />
        <div class="container" style="padding-top:56px;">
            <!-- 销售统计 -->
            <div class="sold-stats">
                <div class="stats-header">
                    <span class="stats-title">销售统计</span>
                    <span class="stats-period">近30天</span>
                </div>
                <div class="stats-row">
                    <div class="stats-item">
                        <div class="stats-value">{{ soldCount }}</div>
                        <div class="stats-label">已售出</div>
                    </div>
                    <div class="stats-item">
                        <div class="stats-value income">￥{{ totalIncome }}</div>
                        <div class="stats-label">总收入</div>
                    </div>
                </div>
            </div>

            <!-- 已售出列表 -->
            <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
                <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了">
                    <template v-if="loading && orders.length === 0">
                        <div v-for="i in 3" :key="i" class="skeleton-card">
                            <van-skeleton avatar :row="2" />
                        </div>
                    </template>
                    <template v-else-if="orders.length === 0">
                        <van-empty description="您还没有卖出任何商品">
                            <van-button type="primary" round size="small" @click="$router.push('/post/create')">
                                去发布商品
                            </van-button>
                        </van-empty>
                    </template>
                    <template v-else>
                        <div v-for="order in orders" :key="order.id" class="sold-card" @click="viewOrderDetail(order)">
                            <div class="sold-card-header">
                                <div class="buyer-info">
                                    <van-image :src="order.buyer?.avatarUrl || defaultAvatar" width="24" height="24"
                                        round fit="cover" />
                                    <span class="buyer-name">{{ order.buyer?.username || '买家' }}</span>
                                </div>
                                <div class="order-status" :class="'status-' + order.status.toLowerCase()">
                                    {{ statusText(order.status) }}
                                </div>
                            </div>
                            <div class="sold-card-body">
                                <van-image :src="order.product?.images?.url" width="70" height="70" radius="10"
                                    fit="cover" />
                                <div class="product-info">
                                    <div class="product-title">{{ order.product?.title }}</div>
                                    <div class="product-meta">
                                        <span class="sold-time">{{ formatTime(order.createdAt) }}</span>
                                    </div>
                                </div>
                                <div class="product-price">
                                    <span class="price-label">成交价</span>
                                    <span class="price-value">￥{{ order.price }}</span>
                                </div>
                            </div>
                            <div class="sold-card-footer" v-if="order.status === 'PAID'">
                                <van-button type="primary" size="small" round @click.stop="handleShip(order)">
                                    <van-icon name="logistics" />
                                    确认发货
                                </van-button>
                            </div>
                        </div>
                    </template>
                </van-list>
            </van-pull-refresh>
        </div>

        <!-- 快捷入口 -->
        <div class="quick-entry" @click="$router.push({ name: 'OrderManage' })">
            <van-icon name="orders-o" />
            <span>订单管理</span>
            <van-icon name="arrow" />
        </div>
    </div>
</template>

<script>
import { getSoldOrders, shipOrder } from '@/api/orders';
import { resolveAvatar } from '@/utils/avatar';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

export default {
    name: 'SoldProducts',
    setup() {
        const router = useRouter();
        const loading = ref(true);
        const refreshing = ref(false);
        const finished = ref(false);
        const orders = ref([]);
        const defaultAvatar = resolveAvatar();

        const statusMap = {
            PENDING: '待付款',
            PAID: '待发货',
            SHIPPED: '待收货',
            COMPLETED: '已完成',
            CANCELLED: '已取消',
        };

        const statusText = (status) => statusMap[status] || status;

        const soldCount = computed(() => {
            return orders.value.filter(o => ['PAID', 'SHIPPED', 'COMPLETED'].includes(o.status)).length;
        });

        const totalIncome = computed(() => {
            return orders.value
                .filter(o => ['PAID', 'SHIPPED', 'COMPLETED'].includes(o.status))
                .reduce((sum, o) => sum + (o.price || 0), 0)
                .toFixed(2);
        });

        const formatTime = (time) => {
            if (!time) return '';
            const date = new Date(time);
            const now = new Date();
            const diff = now - date;
            const days = Math.floor(diff / 86400000);

            if (days < 1) return '今天';
            if (days === 1) return '昨天';
            if (days < 7) return `${days}天前`;
            return date.toLocaleDateString();
        };

        const loadOrders = async () => {
            try {
                loading.value = true;
                const res = await getSoldOrders();
                orders.value = Array.isArray(res) ? res : (res.content || []);
                finished.value = true;
            } catch (error) {
                showFailToast('加载订单失败');
            } finally {
                loading.value = false;
            }
        };

        const onRefresh = async () => {
            await loadOrders();
            refreshing.value = false;
            showSuccessToast('已刷新');
        };

        const viewOrderDetail = (order) => {
            router.push({ name: 'OrderDetail', params: { id: order.id } });
        };

        const handleShip = async (order) => {
            try {
                await showConfirmDialog({
                    title: '确认发货',
                    message: `确定已将商品"${order.product?.title}"发出？`,
                });
                await shipOrder(order.id);
                showSuccessToast('发货成功');
                order.status = 'SHIPPED';
            } catch (error) {
                if (error !== 'cancel') {
                    showFailToast('发货失败');
                }
            }
        };

        onMounted(() => {
            loadOrders();
        });

        return {
            loading,
            refreshing,
            finished,
            orders,
            defaultAvatar,
            soldCount,
            totalIncome,
            statusText,
            formatTime,
            onRefresh,
            viewOrderDetail,
            handleShip,
        };
    },
};
</script>

<style scoped>
/* 销售统计 */
.sold-stats {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    margin: 12px;
    padding: 16px;
    border-radius: 16px;
    color: #fff;
}

.stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.stats-title {
    font-size: 14px;
    font-weight: 500;
}

.stats-period {
    font-size: 12px;
    opacity: 0.8;
}

.stats-row {
    display: flex;
    gap: 24px;
}

.stats-item {
    flex: 1;
}

.stats-value {
    font-size: 24px;
    font-weight: 700;
}

.stats-value.income {
    color: #fff;
}

.stats-label {
    font-size: 12px;
    opacity: 0.85;
    margin-top: 4px;
}

/* 已售出卡片 */
.sold-card {
    background: #fff;
    margin: 12px;
    border-radius: 14px;
    padding: 14px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.sold-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.buyer-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.buyer-name {
    font-size: 13px;
    color: #666;
}

.order-status {
    font-size: 11px;
    font-weight: 500;
    padding: 3px 8px;
    border-radius: 12px;
}

.status-pending {
    background: #fffbeb;
    color: #b45309;
}

.status-paid {
    background: #fef3c7;
    color: #d97706;
}

.status-shipped {
    background: #d1fae5;
    color: #059669;
}

.status-completed {
    background: #f3f4f6;
    color: #6b7280;
}

.status-cancelled {
    background: #fee2e2;
    color: #dc2626;
}

.sold-card-body {
    display: flex;
    gap: 12px;
    align-items: center;
}

.product-info {
    flex: 1;
    min-width: 0;
}

.product-title {
    font-size: 14px;
    font-weight: 500;
    color: #1f2937;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.product-meta {
    margin-top: 6px;
}

.sold-time {
    font-size: 12px;
    color: #9ca3af;
}

.product-price {
    text-align: right;
}

.price-label {
    display: block;
    font-size: 11px;
    color: #9ca3af;
}

.price-value {
    font-size: 16px;
    font-weight: 700;
    color: #ee0a24;
}

.sold-card-footer {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f3f4f6;
    display: flex;
    justify-content: flex-end;
}

/* 骨架屏 */
.skeleton-card {
    background: #fff;
    margin: 12px;
    padding: 14px;
    border-radius: 14px;
}

/* 快捷入口 */
.quick-entry {
    position: fixed;
    bottom: 20px;
    right: 16px;
    background: #fff;
    padding: 10px 16px;
    border-radius: 24px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #333;
    cursor: pointer;
    z-index: 99;
}

.quick-entry:active {
    transform: scale(0.95);
}

.quick-entry .van-icon:first-child {
    font-size: 18px;
    color: #667eea;
}
</style>
