import request from '@/utils/request'

export function fetchAdminUsers() {
  return request.get('/admin/users')
}

export function disableAdminUser(id) {
  return request.patch(`/admin/users/${id}/disable`)
}

export function enableAdminUser(id) {
  return request.patch(`/admin/users/${id}/enable`)
}

export function fetchPendingWithdrawals(params = {}) {
  return request.get('/admin/wallet/withdrawals/pending', {
    params: {
      page: 0,
      size: 50,
      ...params,
    },
  })
}

export function approveWithdrawal(transactionId) {
  return request.post(`/admin/wallet/withdrawals/${transactionId}/approve`)
}

export function rejectWithdrawal(transactionId, reason) {
  return request.post(`/admin/wallet/withdrawals/${transactionId}/reject`, {
    reason,
  })
}
