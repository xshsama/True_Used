import request from '@/utils/request'

// 创建寄售单
export function createConsignment(payload) {
  return request({ url: '/consignments', method: 'post', data: payload })
}

// 获取我的寄售单
export function getMyConsignments() {
  return request({ url: '/consignments', method: 'get' })
}

// 更新物流信息（发货）
export function updateConsignmentLogistics(id, trackingNo) {
  return request({
    url: `/consignments/${id}/logistics`,
    method: 'put',
    data: { trackingNo },
  })
}
