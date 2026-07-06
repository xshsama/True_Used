/**
 * 身份验证 Composable
 * 用于在需要登录的操作前进行检查，自动跳转登录页
 */
import { useUserStore } from '@/stores/user'
import { showConfirmDialog } from 'vant'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()

  // 是否已登录
  const isLoggedIn = computed(() => userStore.isLoggedIn)

  // 当前用户信息
  const currentUser = computed(() => userStore.user)

  /**
   * 检查登录状态，未登录则跳转登录页
   * @param {Object} options 配置选项
   * @param {string} options.message 提示消息
   * @param {boolean} options.showDialog 是否显示确认对话框
   * @param {string} options.redirectPath 登录后返回的路径（默认当前路径）
   * @returns {Promise<boolean>} 是否已登录
   */
  const requireLogin = async (options = {}) => {
    const {
      message = '该操作需要登录，是否立即登录？',
      showDialog = true,
      redirectPath = route.fullPath,
    } = options

    // 已登录直接返回 true
    if (userStore.isLoggedIn) {
      return true
    }

    // 显示登录确认对话框
    if (showDialog) {
      try {
        await showConfirmDialog({
          title: '需要登录',
          message,
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          confirmButtonColor: '#764ba2',
        })

        // 用户确认登录，跳转登录页
        router.push({
          name: 'Login',
          query: { redirect: redirectPath },
        })
      } catch {
        // 用户取消
      }
    } else {
      // 不显示对话框，直接跳转
      router.push({
        name: 'Login',
        query: { redirect: redirectPath },
      })
    }

    return false
  }

  /**
   * 执行需要登录的操作
   * @param {Function} action 需要执行的操作
   * @param {Object} options requireLogin 的配置选项
   * @returns {Promise<any>} 操作结果
   */
  const withAuth = async (action, options = {}) => {
    const loggedIn = await requireLogin(options)
    if (loggedIn) {
      return action()
    }
    return null
  }

  /**
   * 退出登录
   */
  const logout = () => {
    userStore.logout()
    router.push('/')
  }

  return {
    isLoggedIn,
    currentUser,
    requireLogin,
    withAuth,
    logout,
  }
}
