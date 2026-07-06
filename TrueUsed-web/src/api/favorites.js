import request from '@/utils/request'

// 添加收藏
export function addFavorite(productId) {
  return request({ url: `/favorites/products/${productId}`, method: 'post' })
}

// 取消收藏
export function removeFavorite(productId) {
  return request({ url: `/favorites/products/${productId}`, method: 'delete' })
}

// 我的收藏分页
export function listMyFavorites(params = {}) {
  return request({ url: '/favorites', method: 'get', params })
}
