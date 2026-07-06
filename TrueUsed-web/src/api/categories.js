import request from '@/utils/request'

export function listCategories() {
  return request({ url: '/categories', method: 'get' })
}

export function listRootCategories() {
  return request({ url: '/categories/root', method: 'get' })
}
