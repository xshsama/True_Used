import request from '@/utils/request'

/**
 * 创建评价
 * @param {object} data { orderId, rating, content, isAnonymous }
 */
export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    data,
  })
}

/**
 * 获取商品评价列表
 * @param {number} productId 商品ID
 * @param {object} params { page, size }
 */
export function getProductReviews(productId, params) {
  return request({
    url: `/reviews/products/${productId}`,
    method: 'get',
    params,
  })
}

/**
 * 获取我的评价 (我写的)
 */
export function getMyReviews() {
  return request({
    url: '/reviews/my',
    method: 'get',
  })
}

/**
 * 获取我收到的评价
 * @param {object} params { page, size }
 */
export function getReceivedReviews(params) {
  return request({
    url: '/reviews/received',
    method: 'get',
    params,
  })
}

/**
 * 卖家回复评价
 * @param {number} reviewId 评价ID
 * @param {string} replyContent 回复内容
 */
export function replyReview(reviewId, replyContent) {
  return request({
    url: `/reviews/${reviewId}/reply`,
    method: 'put',
    data: { replyContent },
  })
}

/**
 * 获取卖家的评价列表
 * @param {number} sellerId 卖家ID
 * @param {object} params { page, size, sort }
 */
export function getSellerReviews(sellerId, params) {
  return request({
    url: `/reviews/sellers/${sellerId}`,
    method: 'get',
    params,
  })
}

/**
 * 获取商品下的留言/咨询
 * @param {number} productId 商品ID
 * @param {object} params { page, size }
 */
export function getProductComments(productId, params) {
  return request({
    url: `/comments/products/${productId}`,
    method: 'get',
    params,
  })
}

/**
 * 发布商品留言/咨询
 * @param {object} data { productId, content, parentId }
 */
export function createProductComment(data) {
  return request({
    url: '/comments',
    method: 'post',
    data,
  })
}

/**
 * 获取卖家的留言/咨询
 * @param {number} sellerId 卖家ID
 * @param {object} params { page, size }
 */
export function getSellerComments(sellerId, params) {
  return request({
    url: `/comments/sellers/${sellerId}`,
    method: 'get',
    params,
  })
}

/**
 * 发布卖家留言/咨询
 * @param {object} data { targetUserId, content, parentId }
 */
export function createSellerComment(data) {
  return request({
    url: '/comments',
    method: 'post',
    data,
  })
}
