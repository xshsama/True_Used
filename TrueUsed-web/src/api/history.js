import request from '@/utils/request'

export function getBrowsingHistory(params) {
  return request({
    url: '/history',
    method: 'get',
    params,
  })
}
