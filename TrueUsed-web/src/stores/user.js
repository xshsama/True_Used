import { fetchMe, logoutApi, updateMe } from '@/api/auth'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

function normalizeRoles(u) {
  if (!u || typeof u !== 'object') return []
  if (Array.isArray(u.roles)) return u.roles
  if (u.roles instanceof Set) return Array.from(u.roles)
  if (Array.isArray(u.authorities)) {
    return u.authorities
      .map((item) =>
        typeof item === 'string' ? item : item?.authority || item?.name,
      )
      .filter(Boolean)
  }
  return []
}

// 规范化用户对象，统一头像字段为 avatarUrl
function normalizeUser(u) {
  if (!u || typeof u !== 'object') return u
  const avatarUrl = u.avatarUrl || u.avatar || u.avatar_url || null
  return { ...u, avatarUrl, roles: normalizeRoles(u) }
}

export const useUserStore = defineStore('user', () => {
  const persistedUser = (() => {
    try {
      const raw = localStorage.getItem('user')
      return raw ? normalizeUser(JSON.parse(raw)) : null
    } catch {
      return null
    }
  })()

  const user = ref(persistedUser)
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = ref(!!token.value)
  const isAdmin = computed(() =>
    Array.isArray(user.value?.roles)
      ? user.value.roles.includes('ROLE_ADMIN')
      : false,
  )
  let refreshTimer = null

  const setUser = (userData) => {
    const normalized = normalizeUser(userData)
    user.value = normalized
    // 持久化用户基本信息，便于刷新后仍可展示
    try {
      localStorage.setItem('user', JSON.stringify(normalized))
    } catch {
      // 忽略存储异常
    }
    isLoggedIn.value = true
  }

  const loadMe = async () => {
    // request 返回已是 response.data，这里直接作为用户对象
    const data = await fetchMe()
    setUser(data)
    return data
  }

  const saveMe = async (payload) => {
    const data = await updateMe(payload)
    setUser(data)
    return data
  }

  const setToken = (tokenValue) => {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
    isLoggedIn.value = true
    scheduleProactiveRefresh()
  }

  const logout = async () => {
    try {
      // 服务端清除 HttpOnly 刷新 Cookie（忽略失败）
      await logoutApi()
    } catch (e) {
      // ignore network/API errors for logout
    }
    user.value = null
    token.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('token_expires_at')
    localStorage.removeItem('user')
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
  }

  // 主动预刷新：根据 token_expires_at 提前 60 秒刷新
  const scheduleProactiveRefresh = () => {
    try {
      if (refreshTimer) clearTimeout(refreshTimer)
      const expAt = parseInt(
        localStorage.getItem('token_expires_at') || '0',
        10,
      )
      if (!expAt) return
      const now = Date.now()
      const lead = 60 * 1000 // 提前 60s
      const delay = Math.max(0, expAt - now - lead)
      refreshTimer = setTimeout(async () => {
        // 触发一次静默请求，若 401 将由拦截器走 refresh 流程
        try {
          // 选择一个安全、快速的接口
          await fetchMe()
        } catch (e) {
          // 拦截器会处理
        }
      }, delay)
    } catch {}
  }

  // 初始化时也尝试安排一次预刷新
  scheduleProactiveRefresh()

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    setUser,
    setToken,
    loadMe,
    saveMe,
    logout,
    scheduleProactiveRefresh,
  }
})
