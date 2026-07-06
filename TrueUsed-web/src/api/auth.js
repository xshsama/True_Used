import request from '@/utils/request'

export function loginApi(data) {
  // 后端登录路径为 /auth/login，这里通过 Vite 代理前缀 /api 调用
  // 显式开启 withCredentials 以确保跨域环境下浏览器接受 Set-Cookie(refresh_token)
  return request.post('/auth/login', data, { withCredentials: true })
}

export function registerApi(data) {
  return request.post('/auth/register', data)
}

export function logoutApi() {
  // 调用后端登出以清除 HttpOnly 刷新 Cookie；跨域时需要携带凭据
  const token = localStorage.getItem('token')
  return request.post('/auth/logout', null, {
    withCredentials: true,
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
}

// 用户信息相关接口
export function fetchMe() {
  return request.get('/users/me')
}

export function updateMe(data) {
  return request.put('/users/me', data, { silent: true })
}

export function fetchMyStats() {
  return request.get('/users/me/stats')
}
