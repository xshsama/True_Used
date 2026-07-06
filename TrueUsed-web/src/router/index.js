import { useUserStore } from '@/stores/user'
import { createRouter, createWebHistory } from 'vue-router'

function hasAdminRole(user) {
  return Array.isArray(user?.roles) && user.roles.includes('ROLE_ADMIN')
}

const routes = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/admin',
    name: 'AdminConsole',
    component: () => import('@/views/AdminConsole.vue'),
    meta: { requiresAuth: true, requiresAdmin: true, hideNavbar: true, adminCanvas: true },
  },
  {
    path: '/coupon-center',
    name: 'CouponCenter',
    component: () => import('@/views/CouponCenter.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/inspection-reports',
    name: 'InspectionReport',
    component: () => import('@/views/InspectionReport.vue'),
  },
  {
    path: '/inspection-report/:id',
    name: 'InspectionDetail',
    component: () => import('@/views/InspectionDetail.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('@/views/Ranking.vue'),
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/Favorites.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/following',
    name: 'Following',
    component: () => import('@/views/Following.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/post/create',
    name: 'PostCreate',
    component: () => import('@/views/PostCreate.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/messages/chat/:id',
    name: 'MessageChat',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Login.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/seller/:id',
    name: 'SellerProfile',
    component: () => import('@/views/SellerProfile.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { hideNavbar: true },
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/Address.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/verification',
    name: 'Verification',
    component: () => import('@/views/Verification.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/address-edit/:id?',
    name: 'AddressEdit',
    component: () => import('@/views/AddressEdit.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/service',
    name: 'ServiceCenter',
    component: () => import('@/views/ServiceCenter.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  { path: '/help', name: 'Help', component: () => import('@/views/Help.vue') },
  { path: '/feedback', name: 'Feedback', component: () => import('@/views/Feedback.vue') },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/shop-settings',
    name: 'ShopSettings',
    component: () => import('@/views/ShopSettings.vue'),
    meta: { requiresAuth: true, navbarMode: 'seller' },
  },
  { path: '/about', name: 'About', component: () => import('@/views/About.vue') },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: () => import('@/views/MyProducts.vue'),
    meta: { requiresAuth: true, navbarMode: 'seller' },
  },

  {
    path: '/sold-products',
    name: 'SoldProducts',
    component: () => import('@/views/SoldProducts.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/order-manage',
    name: 'OrderManage',
    component: () => import('@/views/OrderManage.vue'),
    meta: { requiresAuth: true, navbarMode: 'seller' },
  },
  {
    path: '/seller/data-center',
    name: 'SellerDataCenter',
    component: () => import('@/views/SellerDataCenter.vue'),
    meta: { requiresAuth: true, navbarMode: 'seller' },
  },
  {
    path: '/settlement',
    name: 'Settlement',
    component: () => import('@/views/Settlement.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/service-messages',
    name: 'ServiceMessages',
    component: () => import('@/views/ServiceMessages.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/payment/success',
    name: 'PaymentSuccess',
    component: () => import('@/views/PaymentSuccess.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/payment/:id',
    name: 'Payment',
    component: () => import('@/views/Payment.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/order/:id/refund-apply',
    name: 'RefundApply',
    component: () => import('@/views/RefundApply.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/order/:id/refund-detail',
    name: 'RefundDetail',
    component: () => import('@/views/RefundDetail.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/review/create',
    name: 'ReviewCreate',
    component: () => import('@/views/ReviewCreate.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/my-reviews',
    name: 'MyReviews',
    component: () => import('@/views/MyReviews.vue'),
    meta: { requiresAuth: true, navbarMode: 'seller' },
  },
  {
    path: '/history',
    name: 'BrowsingHistory',
    component: () => import('@/views/BrowsingHistory.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'SystemNotifications',
    component: () => import('@/views/SystemNotifications.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
  {
    path: '/cashier',
    name: 'Cashier',
    component: () => import('@/views/Cashier.vue'),
    meta: { requiresAuth: true, hideNavbar: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach(async (to) => {
  // 根据路由meta设置body class
  if (to.meta.hideNavbar) {
    document.body.classList.add('hide-navbar')
  } else {
    document.body.classList.remove('hide-navbar')
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // pinia 可能尚未初始化，这里兜底读取 localStorage
    const store = useUserStore()
    const hasToken = store?.token || localStorage.getItem('token')
    if (!hasToken) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }

    if (to.meta.requiresAdmin) {
      if (!store.user && hasToken) {
        try {
          await store.loadMe()
        } catch {
          await store.logout()
          return { name: 'Login', query: { redirect: to.fullPath } }
        }
      }

      if (!hasAdminRole(store.user)) {
        return { name: 'Home' }
      }
    }
  }

  return true
})

export default router
