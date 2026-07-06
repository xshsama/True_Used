import request from '@/utils/request'

export function getAvailableCoupons() {
  return request({
    url: '/coupons',
    method: 'get',
  })
}

export function getMyCoupons() {
  return request({
    url: '/coupons/my',
    method: 'get',
  })
}

export function claimCoupon(id) {
  return request({
    url: `/coupons/${id}/claim`,
    method: 'post',
  })
}

export function useCoupon(userCouponId) {
  return request({
    url: `/coupons/my/${userCouponId}/use`,
    method: 'post',
  })
}
