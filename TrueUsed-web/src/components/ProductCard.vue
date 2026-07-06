<template>
    <div class="product-card" @click="onClick">
        <div class="card-image-box">
            <van-image :src="productImage" :alt="product.title || '商品'" fit="cover" class="product-image" lazy-load>
                <template #loading>
                    <div class="image-placeholder"><van-icon name="photo-o" /></div>
                </template>
                <template #error>
                    <div class="image-placeholder"><van-icon name="bag-o" /></div>
                </template>
            </van-image>

            <div class="img-tag top-left" v-if="hasRealShot">
                <van-icon name="photograph" /> 实拍图
            </div>
            <div class="img-tag top-right" :class="isOfficialTrade ? 'img-tag-official' : 'img-tag-direct'">
                {{ isOfficialTrade ? '平台验货' : '卖家自出' }}
            </div>
            <div class="img-tag bottom-right" v-if="timeAgo">
                <van-icon name="clock-o" /> {{ timeAgo }}
            </div>

            <div v-if="statusText && status !== 'selling'" class="status-overlay">
                {{ statusText }}
            </div>
        </div>

        <div class="card-info">
            <h3 class="product-title">{{ product.title || '未命名商品' }}</h3>

            <div class="meta-row">
                <span v-if="conditionLabel" class="condition-badge">{{ conditionLabel }}</span>
                <span v-if="savedAmount > 0" class="save-tag">立省 ¥{{ savedAmount }}</span>
            </div>

            <div class="price-row">
                <div class="main-price">¥<span class="price-num">{{ product.price ?? '-' }}</span></div>
                <div class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</div>
            </div>

            <div class="seller-row" v-if="showSeller">
                <div class="seller-left">
                    <img :src="sellerAvatar" class="seller-avatar" />
                    <div class="seller-text">
                        <div class="seller-name">{{ sellerName }}</div>
                        <div class="seller-sub">{{ sellerLocation }} · {{ sellerCredit }}</div>
                    </div>
                </div>
                <div class="want-count" v-if="wantCount > 0">
                    <van-icon name="eye-o" /> {{ wantCount }}
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import defaultAvatarUrl from '@/assets/icons/user.svg';
import { normalizeProductTrade } from '@/utils/productTrade';

export default {
    name: 'ProductCard',
    props: {
        product: { type: Object, required: true },
        clickable: { type: Boolean, default: true },
        status: { type: String, default: 'selling' },
        showSeller: { type: Boolean, default: true },
    },
    emits: ['click'],
    setup(props, { emit }) {
        const onClick = () => {
            if (props.clickable) emit('click', props.product)
        }
        return { onClick }
    },
    computed: {
        productImage() {
            if (this.product.image) return this.product.image
            if (this.product.images && this.product.images.length > 0) {
                const firstImg = this.product.images[0]
                return typeof firstImg === 'string' ? firstImg : firstImg?.url
            }
            return ''
        },
        hasRealShot() {
            return this.product.tags?.includes('实拍图') || false
        },
        tradeMeta() {
            return normalizeProductTrade(this.product)
        },
        isOfficialTrade() {
            return this.tradeMeta.hasPlatformInspection
        },
        conditionLabel() {
            return this.tradeMeta.primaryConditionLabel || ''
        },
        timeAgo() {
            return this.product.timeAgo || ''
        },
        statusText() {
            const map = {
                ON_SALE: '',
                SOLD: '已售',
                OFF_SHELF: '已下架',
                selling: '',
                sold: '已售',
                offline: '已下架'
            }
            return map[this.status] || ''
        },
        savedAmount() {
            if (this.product.originalPrice && this.product.price) {
                const diff = Math.floor(Number(this.product.originalPrice) - Number(this.product.price))
                return diff > 0 ? diff : 0
            }
            return this.product.saved || 0
        },
        sellerAvatar() {
            return this.product.seller?.avatarUrl || this.product.seller?.avatar || defaultAvatarUrl
        },
        sellerName() {
            return this.product.seller?.nickname || this.product.seller?.username || '匿名卖家'
        },
        sellerLocation() {
            return this.product.locationText || this.product.seller?.city || '未知'
        },
        sellerCredit() {
            if (this.isOfficialTrade) return '平台验货'
            return this.product.seller?.credit || '信用极好'
        },
        wantCount() {
            return this.product.wantCount || this.product.favoritesCount || 0
        }
    }
}
</script>

<style scoped>
.product-card {
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), #ffffff);
    border-radius: 18px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
    border: 1px solid rgba(226, 232, 240, 0.9);
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.product-card:hover {
    transform: translateY(-6px);
    border-color: rgba(11, 138, 97, 0.2);
    box-shadow: 0 18px 34px rgba(15, 23, 42, 0.1);
}

.card-image-box {
    position: relative;
    padding-top: 100%;
    background: #f8fafc;
    overflow: hidden;
}

.product-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #cbd5e1;
    font-size: 24px;
}

.img-tag {
    position: absolute;
    display: flex;
    align-items: center;
    gap: 4px;
    border-radius: 9999px;
    padding: 4px 8px;
    backdrop-filter: blur(8px);
    font-size: 10px;
    font-weight: 700;
}

.img-tag.top-left {
    top: 10px;
    left: 10px;
    background: rgba(15, 23, 42, 0.7);
    color: #fff;
}

.img-tag.top-right {
    top: 10px;
    right: 10px;
}

.img-tag.bottom-right {
    bottom: 10px;
    right: 10px;
    background: rgba(15, 23, 42, 0.7);
    color: #fff;
}

.img-tag-official {
    background: rgba(0, 135, 90, 0.88);
    color: #fff;
}

.img-tag-direct {
    background: rgba(249, 115, 22, 0.88);
    color: #fff;
}

.status-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
}

.card-info {
    padding: 16px;
}

.product-title {
    font-size: 15px;
    font-weight: 700;
    color: #111827;
    margin: 0 0 10px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    height: 42px;
}

.meta-row {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
}

.condition-badge,
.save-tag {
    display: inline-flex;
    align-items: center;
    border-radius: 9999px;
    padding: 4px 8px;
    font-size: 10px;
    font-weight: 700;
}

.condition-badge {
    background: #f8fafc;
    color: #475569;
}

.save-tag {
    background: #FEF2F2;
    color: #EF4444;
}

.price-row {
    display: flex;
    align-items: baseline;
    gap: 6px;
    margin-bottom: 14px;
}

.main-price {
    color: #EF4444;
    font-size: 13px;
    font-weight: 700;
}

.price-num {
    font-size: 24px;
}

.original-price {
    color: #9CA3AF;
    font-size: 12px;
    text-decoration: line-through;
}

.seller-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #F3F4F6;
    padding-top: 10px;
}

.seller-left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    overflow: hidden;
}

.seller-avatar {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    object-fit: cover;
}

.seller-text {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.seller-name {
    font-size: 12px;
    font-weight: 600;
    color: #374151;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.seller-sub {
    font-size: 11px;
    color: #9CA3AF;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.want-count {
    font-size: 11px;
    color: #6B7280;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 4px;
}
</style>
