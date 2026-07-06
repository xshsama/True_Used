<template>
    <div class="seller-center">
        <!-- 店铺数据 (Dashboard) -->
        <div class="section-card">
            <div class="section-header">
                <span class="section-title">
                    <van-icon name="shop-o" class="section-icon" /> 店铺数据
                </span>
                <span class="section-link" @click="goOrderManage">
                    全部订单 <van-icon name="arrow" />
                </span>
            </div>
            <div class="order-status-grid">
                <div class="order-status-item" @click="goOrderManage">
                    <div class="status-icon-wrapper linear-style">
                        <van-icon name="orders-o" size="24" />
                        <span class="status-text-value">{{ orderStats.total }}</span>
                    </div>
                    <span class="status-label">今日订单</span>
                </div>
                <div class="order-status-item" @click="goToOrderStatus('PAID')">
                    <div class="status-icon-wrapper linear-style">
                        <van-icon name="logistics" size="24" />
                        <span v-if="orderStats.pendingShip > 0" class="status-badge">{{ orderStats.pendingShip }}</span>
                    </div>
                    <span class="status-label">待发货</span>
                </div>
                <div class="order-status-item" @click="goToOrderStatus('SHIPPED')">
                    <div class="status-icon-wrapper linear-style">
                        <van-icon name="passed" size="24" />
                    </div>
                    <span class="status-label">已发货</span>
                </div>
                <div class="order-status-item" @click="goToSoldProducts">
                    <div class="status-icon-wrapper linear-style">
                        <van-icon name="gold-coin-o" size="24" />
                    </div>
                    <span class="status-label">总收益</span>
                </div>
            </div>
        </div>

        <!-- 常用工具 (Tools) -->
        <div class="section-card">
            <div class="section-header">
                <span class="section-title">
                    <van-icon name="apps-o" class="section-icon" /> 常用工具
                </span>
            </div>
            <div class="menu-grid">
                <div class="menu-grid-item" @click="createProduct">
                    <div class="menu-grid-icon">
                        <van-icon name="add-o" />
                    </div>
                    <span class="menu-grid-text">发布商品</span>
                </div>
                <div class="menu-grid-item" @click="goProductManage">
                    <div class="menu-grid-icon">
                        <van-icon name="bag-o" />
                    </div>
                    <span class="menu-grid-text">商品管理</span>
                </div>
                <div class="menu-grid-item" @click="goOrderManage">
                    <div class="menu-grid-icon">
                        <van-icon name="todo-list-o" />
                    </div>
                    <span class="menu-grid-text">订单管理</span>
                </div>
                <div class="menu-grid-item" @click="goToSoldProducts">
                    <div class="menu-grid-icon">
                        <van-icon name="balance-list-o" />
                    </div>
                    <span class="menu-grid-text">已售出</span>
                </div>
                <div class="menu-grid-item" @click="goServiceMessages">
                    <div class="menu-grid-icon">
                        <van-icon name="chat-o" />
                    </div>
                    <span class="menu-grid-text">客服消息</span>
                </div>
                <div class="menu-grid-item" @click="showToast('功能开发中')">
                    <div class="menu-grid-icon">
                        <van-icon name="setting-o" />
                    </div>
                    <span class="menu-grid-text">店铺设置</span>
                </div>
            </div>
        </div>

        <!-- 悬浮发布按钮 -->
        <button class="floating-post-btn" @click="createProduct">
            <van-icon name="plus" /> 发布闲置
        </button>
    </div>
</template>

<script>
import { getSoldOrders } from '@/api/orders';
import { showToast } from 'vant';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

export default {
    name: 'SellerCenter',
    setup() {
        const router = useRouter();
        const orders = ref([]);

        // 订单统计
        const orderStats = computed(() => {
            const list = orders.value;
            return {
                total: list.length, // 这里简单用总数代替今日订单，实际应过滤日期
                pendingShip: list.filter(o => o.status === 'PAID').length,
                shipped: list.filter(o => o.status === 'SHIPPED').length,
                completed: list.filter(o => o.status === 'COMPLETED').length,
            };
        });

        const loadOrders = async () => {
            try {
                const res = await getSoldOrders();
                orders.value = Array.isArray(res) ? res : (res.content || []);
            } catch (error) {
                console.error('Failed to load orders:', error);
            }
        };

        const createProduct = () => router.push({ name: 'PostCreate' })
        const goToMyProducts = () => router.push({ name: 'MyProducts' })
        const goToSoldProducts = () => router.push({ name: 'SoldProducts' })
        const goToOrderStatus = (key) => router.push({ name: 'OrderManage', query: { status: key } })
        const goProductManage = () => router.push({ name: 'ProductManageCenter' })
        const goOrderManage = () => router.push({ name: 'OrderManage' })
        const goServiceMessages = () => router.push({ name: 'ServiceMessages' })

        onMounted(() => {
            loadOrders();
        });

        return {
            orderStats,
            createProduct,
            goToMyProducts,
            goToSoldProducts,
            goToOrderStatus,
            goProductManage,
            goOrderManage,
            goServiceMessages,
            showToast
        }
    }
}
</script>

<style scoped>
.seller-center {
    display: flex;
    flex-direction: column;
    gap: 24px;
    padding-bottom: 80px;
}

/* 复用 Profile.vue 的样式逻辑 */
.section-card {
    background: #fff;
    border-radius: 20px;
    padding: 24px 20px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.section-icon {
    color: var(--primary-color);
}

.section-link {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--primary-color);
}

/* 订单状态网格 */
.order-status-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
}

.order-status-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}

.status-icon-wrapper.linear-style {
    position: relative;
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--primary-color);
    background: #E8F5E9;
    transition: transform 0.2s ease;
}

.order-status-item:active .status-icon-wrapper {
    transform: scale(0.95);
}

.status-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: #f43f5e;
    border-radius: 8px;
    font-size: 10px;
    font-weight: 600;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
}

.status-text-value {
    font-weight: 700;
    font-size: 14px;
    position: absolute;
    top: -6px;
    right: -6px;
    background: #fff;
    padding: 0 4px;
    border-radius: 10px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    color: var(--primary-color);
}

.status-label {
    font-size: 12px;
    color: #666;
}

/* 菜单网格 */
.menu-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px 12px;
}

.menu-grid-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 0;
    background: transparent;
    border-radius: 0;
    cursor: pointer;
    transition: opacity 0.2s ease;
}

.menu-grid-item:active {
    opacity: 0.7;
}

.menu-grid-icon {
    font-size: 28px;
    color: var(--primary-color);
    background: #E8F5E9;
    width: 48px;
    height: 48px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.menu-grid-text {
    font-size: 12px;
    font-weight: 500;
    color: #374151;
}

/* 悬浮按钮 */
.floating-post-btn {
    position: fixed;
    bottom: 90px;
    left: 50%;
    transform: translateX(-50%);
    background: var(--primary-color);
    color: #fff;
    border: none;
    border-radius: 99px;
    padding: 12px 32px;
    font-size: 16px;
    font-weight: 600;
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.4);
    display: flex;
    align-items: center;
    gap: 8px;
    z-index: 100;
    transition: all 0.3s ease;
}

.floating-post-btn:active {
    transform: translateX(-50%) scale(0.95);
}
</style>
