import request from '@/utils/request'

export function getAddresses() {
  return request({
    url: '/addresses',
    method: 'get',
  })
}

export function createAddress(data) {
  return request({
    url: '/addresses',
    method: 'post',
    data,
    silent: true,
  })
}

export function getAddressById(id) {
  return request({
    url: `/addresses/${id}`,
    method: 'get',
  })
}

export function updateAddress(id, data) {
  return request({
    url: `/addresses/${id}`,
    method: 'put',
    data,
    silent: true,
  })
}

export function deleteAddress(id) {
  return request({
    url: `/addresses/${id}`,
    method: 'delete',
  })
}
