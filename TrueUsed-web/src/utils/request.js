import router from '@/router'
import { useUserStore } from '@/stores/user'
import { isMockEnabled, mockAdapter } from '@/mock/client'
import axios from 'axios'
import { showToast } from 'vant'

// 创建 axios 实例，统一走 /api，交由 Vite 代理到后端
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 单独的刷新实例，携带 Cookie（存放 refresh_token）
const refreshClient = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
})

// 请求拦截：附加 Authorization 头
request.interceptors.request.use(
  (config) => {
    // 不给认证相关接口附带 Authorization，避免旧 token 干扰登录/刷新流程
    if (typeof config.url === 'string' && config.url.startsWith('/auth/')) {
      return config
    }
    const token = localStorage.getItem('token')
    // 可选：基于后端返回的 expiresInMs 做简单过期判断
    const expAt = parseInt(localStorage.getItem('token_expires_at') || '0', 10)
    if (expAt && Date.now() > expAt) {
      // 过期则清理并避免带上旧 token
      localStorage.removeItem('token')
      localStorage.removeItem('token_expires_at')
    } else if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    if (isMockEnabled()) {
      config.adapter = mockAdapter
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截：统一处理 401
request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 统一解包后端标准响应 { code, message, data, timestamp }
    if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
      if (body.code === 0) return body.data
      // 非 0 但返回 2xx，按错误处理
      const err = new Error(body.message || '请求失败')
      err.response = { data: body, status: 200 }
      throw err
    }
    return body
  },
  async (error) => {
    const original = error?.config
    const status = error?.response?.status
    if (status === 401 && !original?._retry) {
      original._retry = true
      try {
        const res = await refreshClient.post('/auth/refresh')
        // 解包可能已由拦截器完成；若没解包，兼容两种结构
        const payload = res?.data?.token ? res.data : res?.data?.data
        if (payload?.token) {
          const store = useUserStore()
          store.setToken(payload.token)
          if (
            typeof payload.expiresInMs === 'number' &&
            payload.expiresInMs > 0
          ) {
            localStorage.setItem(
              'token_expires_at',
              String(Date.now() + payload.expiresInMs),
            )
          }
          // 重新设置头并重试原请求
          original.headers = original.headers || {}
          original.headers.Authorization = `Bearer ${payload.token}`
          return request(original)
        }
      } catch {}
      const redirect = router.currentRoute.value.fullPath
      if (router.currentRoute.value.name !== 'Login') {
        router.replace({ name: 'Login', query: { redirect } })
      }
    }
    // 非 401 的错误，统一弹出提示（可通过 config.silent 抑制）
    try {
      if (!original?.silent) {
        const respData = error?.response?.data
        const msg =
          (respData && typeof respData === 'object' && respData.message) ||
          error?.response?.data?.error ||
          error?.message ||
          '请求失败，请稍后重试'
        showToast(String(msg))
      }
    } catch {}
    return Promise.reject(error)
  },
)

export default request
