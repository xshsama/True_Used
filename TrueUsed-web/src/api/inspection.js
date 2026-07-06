import request from '@/utils/request'

export function receivePackage(orderId) {
  return request({
    url: '/platform/receive_package',
    method: 'post',
    params: { orderId },
  })
}

export function getInspectionFlow(orderId) {
  return request({
    url: `/inspections/${orderId}/flow`,
    method: 'get',
  })
}

export function getOrderInspectionReport(orderId) {
  return request({
    url: `/inspections/orders/${orderId}/report`,
    method: 'get',
  })
}

export function getMyInspections() {
  return request({
    url: '/inspections/my',
    method: 'get',
  })
}
