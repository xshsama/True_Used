import request from '@/utils/request'

// 列表查询：支持 q, categoryId, priceMin, priceMax, sort, page, size
export function listProducts(params = {}) {
  return request({
    url: '/products',
    method: 'get',
    params,
  })
}

// 详情
export function getProduct(id) {
  return request({ url: `/products/${id}`, method: 'get' })
}

// 创建
export function createProduct(payload) {
  return request({ url: '/products', method: 'post', data: payload })
}

// 更新
export function updateProduct(id, payload) {
  return request({ url: `/products/${id}`, method: 'put', data: payload })
}

// 删除
export function deleteProduct(id) {
  return request({ url: `/products/${id}`, method: 'delete' })
}

// 获取我的商品
export function getMyProducts(params = {}) {
  return request({
    url: '/products/my',
    method: 'get',
    params,
  })
}

// 上架商品
export function publishProduct(id) {
  return request({ url: `/products/${id}/publish`, method: 'put' })
}

// 下架商品
export function hideProduct(id) {
  return request({ url: `/products/${id}/hide`, method: 'put' })
}

// 擦亮商品
export function polishProduct(id) {
  return request({ url: `/products/${id}/polish`, method: 'put' })
}
