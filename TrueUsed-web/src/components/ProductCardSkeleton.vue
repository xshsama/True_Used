<template>
    <div :class="['product-card-wrapper', 'tu-card', type]">
        <div class="media">
            <van-skeleton-image />
        </div>
        <div class="info">
            <van-skeleton-paragraph :row="2" />
            <div class="meta-row-skeleton">
                <van-skeleton-paragraph :row="1" style="width: 40%;" />
                <van-skeleton-paragraph :row="1" style="width: 20%;" />
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ProductCardSkeleton',
    props: {
        type: {
            type: String,
            default: 'vertical', // 'vertical' | 'horizontal'
        },
    },
};
</script>

<style scoped>
.product-card-wrapper {
    border-radius: var(--card-radius);
    display: flex;
    /* 默认竖向 */
    flex-direction: column;
}

.product-card-wrapper.horizontal {
    flex-direction: row;
    padding: 12px;
    background: #fff;
    margin: 10px 0;
    border-radius: 16px;
    align-items: center;
    gap: 12px;
}

/* 竖向模式图片 */
.product-card-wrapper:not(.horizontal) .media {
    aspect-ratio: 4/3;
}

/* 横向模式图片 */
.product-card-wrapper.horizontal .media {
    width: 88px;
    height: 88px;
    flex-shrink: 0;
    aspect-ratio: auto;
}

.media .van-skeleton-image {
    width: 100%;
    height: 100%;
    border-radius: 8px;
}

.info {
    padding: 14px 16px 16px;
    display: flex;
    flex-direction: column;
    flex: 1;
}

.product-card-wrapper.horizontal .info {
    padding: 0;
    justify-content: space-between;
}

.meta-row-skeleton {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
}

@media (max-width: 720px) {
    .product-card-wrapper:not(.horizontal) .info {
        padding: 12px 12px 14px;
    }
}
</style>
