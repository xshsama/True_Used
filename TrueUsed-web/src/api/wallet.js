import request from '@/utils/request'

export function getMyWallet() {
  return request({
    url: '/wallet',
    method: 'get',
  })
}

export function setPayPassword(data) {
  return request({
    url: '/wallet/set-password',
    method: 'post',
    data,
  })
}

export function topUp(data) {
  return request({
    url: '/wallet/top-up',
    method: 'post',
    data,
  })
}

export function withdraw(data) {
  return request({
    url: '/wallet/withdraw',
    method: 'post',
    data,
  })
}

export function getTransactions(params) {
  return request({
    url: '/wallet/transactions',
    method: 'get',
    params,
  })
}
