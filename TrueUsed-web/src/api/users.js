import request from '@/utils/request'

export function followUser(userId) {
  return request({
    url: `/users/${userId}/follow`,
    method: 'post',
  })
}

export function unfollowUser(userId) {
  return request({
    url: `/users/${userId}/follow`,
    method: 'delete',
  })
}

export function listMyFollowing(params = {}) {
  return request({
    url: '/users/me/following',
    method: 'get',
    params,
  })
}
