import request from '@/utils/request'

/**
 * 创建订单
 * @param {number} productId 商品ID
 */
export function createOrder(orderRequest) {
  return request({
    url: '/orders',
    method: 'post',
    data: orderRequest,
  })
}

/**
 * 获取我买到的订单列表
 */
export function getMyOrders() {
  return request({
    url: '/orders/my-orders',
    method: 'get',
  })
}

/**
 * 获取我卖出的订单列表
 */
export function getSoldOrders(params = {}) {
  return request({
    url: '/orders/sold-orders',
    method: 'get',
    params,
  })
}

/**
 * 根据ID获取订单详情
 * @param {number} id 订单ID
 */
export function getOrderById(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get',
  })
}

/**
 * 支付订单
 * @param {number} id 订单ID
 */
export function payOrder(id) {
  return request({
    url: `/orders/${id}/pay`,
    method: 'put',
  })
}

/**
 * 钱包支付订单
 * @param {number} id 订单ID
 * @param {string} password 支付密码
 */
export function payOrderByWallet(id, password) {
  return request({
    url: `/orders/${id}/pay-wallet`,
    method: 'put',
    data: { password },
  })
}

/**
 * 卖家发货
 * @param {number} id 订单ID
 * @param {object} shipData 发货信息 { expressCompany: string, trackingNumber?: string }
 */
export function shipOrder(id, shipData = {}) {
  return request({
    url: `/orders/${id}/ship`,
    method: 'put',
    data: shipData,
  })
}

/**
 * 获取订单物流追踪信息
 * @param {number} id 订单ID
 */
export function getOrderShipping(id) {
  return request({
    url: `/orders/${id}/shipping`,
    method: 'get',
  })
}

/**
 * 获取支持的快递公司列表
 */
export function getExpressCompanies() {
  return request({
    url: '/orders/express-companies',
    method: 'get',
  })
}

/**
 * 买家确认收货
 * @param {number} id 订单ID
 */
export function confirmDelivery(id) {
  return request({
    url: `/orders/${id}/confirm-delivery`,
    method: 'put',
  })
}

/**
 * 取消订单
 * @param {number} id 订单ID
 */
export function cancelOrder(id) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'put',
  })
}

/**
 * 更新订单状态
 * @param {number} id 订单ID
 * @param {string} status 目标状态
 */
export function updateOrderStatus(id, status) {
  return request({
    url: `/orders/${id}/status`,
    method: 'put',
    data: { status },
  })
}

/**
 * 申请退款
 * @param {number} id 订单ID
 * @param {object} data 退款信息 { reason: string, refundType: string, refundAmount: number }
 */
export function requestRefund(id, data) {
  return request({
    url: `/orders/${id}/refund-request`,
    method: 'post',
    data,
  })
}

/**
 * 获取退款详情
 * @param {number} id 订单ID
 */
export function getRefundDetail(id) {
  return request({
    url: `/orders/${id}/refund-detail`,
    method: 'get',
  })
}

/**
 * 同意退款
 * @param {number} id 订单ID
 */
export function approveRefund(id) {
  return request({
    url: `/orders/${id}/refund-approve`,
    method: 'put',
  })
}

/**
 * 拒绝退款
 * @param {number} id 订单ID
 */
export function rejectRefund(id) {
  return request({
    url: `/orders/${id}/refund-reject`,
    method: 'put',
  })
}

/**
 * 完成退款
 * @param {number} id 订单ID
 */
export function completeRefund(id) {
  return request({
    url: `/orders/${id}/refund-complete`,
    method: 'put',
  })
}
