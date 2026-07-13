const STORAGE_KEY = 'trueused_mock_state_v1'
const CURRENT_USER_KEY = 'trueused_mock_current_user_id'

const clone = (value) => {
  if (typeof structuredClone === 'function') return structuredClone(value)
  return JSON.parse(JSON.stringify(value))
}

const now = () => new Date().toISOString()
const dayMs = 24 * 60 * 60 * 1000
const pickedAfterMs = 10 * 1000
const departedAfterMs = 20 * 1000
const transitAfterMs = 40 * 1000
const deliveringAfterMs = 60 * 1000
const deliveryStationAfterMs = 80 * 1000
const deliveredAfterMs = 120 * 1000
const shippingStatusElapsedMs = {
  PENDING: 0,
  PICKED: pickedAfterMs,
  IN_TRANSIT: transitAfterMs,
  DELIVERING: deliveringAfterMs,
  DELIVERED: deliveredAfterMs,
}
const expressCodes = {
  顺丰速运: 'SF',
  中通快递: 'ZTO',
  圆通速递: 'YTO',
  韵达快递: 'YD',
  申通快递: 'STO',
  极兔速递: 'JT',
  邮政EMS: 'EMS',
  京东物流: 'JD',
}
const placeholder = (text) =>
  `https://via.placeholder.com/900x700/24333f/ffffff?text=${encodeURIComponent(text)}`

const seed = () => ({
  users: [
    {
      id: 1,
      username: 'demo_buyer',
      nickname: '演示买家',
      email: 'buyer@trueused.local',
      avatarUrl: 'https://via.placeholder.com/160/4a8b6e/ffffff?text=B',
      role: 'USER',
    },
    {
      id: 2,
      username: 'demo_seller',
      nickname: '硬核数码卖家',
      email: 'seller@trueused.local',
      avatarUrl: 'https://via.placeholder.com/160/24333f/ffffff?text=S',
      role: 'USER',
    },
    {
      id: 99,
      username: 'admin',
      nickname: '平台管理员',
      email: 'admin@trueused.local',
      avatarUrl: 'https://via.placeholder.com/160/111827/ffffff?text=A',
      role: 'ADMIN',
    },
  ],
  userFollows: [],
  categories: [
    { id: 1, name: '数码', icon: 'i-lucide-smartphone' },
    { id: 2, name: '电脑', icon: 'i-lucide-monitor' },
    { id: 3, name: '摄影', icon: 'i-lucide-camera' },
    { id: 4, name: '穿搭', icon: 'i-lucide-shirt' },
  ],
  products: [
    {
      id: 101,
      title: 'iPhone 15 Pro Max 256G 原色钛金属',
      description: '自用主力机，外观轻微使用痕迹，电池健康 91%。',
      price: 6899,
      originalPrice: 7999,
      currency: 'CNY',
      status: 'ON_SALE',
      condition: 'LIKE_NEW',
      sellerClaimCondition: 'LIKE_NEW',
      inspectionGrade: 'A',
      tradeModel: 'OFFICIAL_INSPECTION',
      inspectionFee: 29,
      seller: { id: 2, username: 'demo_seller', nickname: '硬核数码卖家', avatarUrl: 'https://via.placeholder.com/160/24333f/ffffff?text=S', productCount: 3 },
      category: { id: 1, name: '数码', icon: 'i-lucide-smartphone' },
      locationText: '上海 · 徐汇区',
      viewsCount: 238,
      favoritesCount: 36,
      images: [{ url: placeholder('iPhone 15 Pro Max') }],
      createdAt: new Date(Date.now() - dayMs).toISOString(),
      updatedAt: now(),
    },
    {
      id: 102,
      title: 'MacBook Pro 14 M1 Pro 16G 512G',
      description: '屏幕、键盘、接口功能正常，适合开发和剪辑。',
      price: 7399,
      originalPrice: 10999,
      currency: 'CNY',
      status: 'ON_SALE',
      condition: 'GOOD',
      sellerClaimCondition: 'GOOD',
      inspectionGrade: '',
      tradeModel: 'FREE_TRADING',
      seller: { id: 2, username: 'demo_seller', nickname: '硬核数码卖家', avatarUrl: 'https://via.placeholder.com/160/24333f/ffffff?text=S', productCount: 3 },
      category: { id: 2, name: '电脑', icon: 'i-lucide-monitor' },
      locationText: '杭州 · 西湖区',
      viewsCount: 412,
      favoritesCount: 58,
      images: [{ url: placeholder('MacBook Pro 14') }],
      createdAt: new Date(Date.now() - 2 * dayMs).toISOString(),
      updatedAt: now(),
    },
  ],
  orders: [
    {
      id: 9001,
      buyer: { id: 1, username: 'demo_buyer', nickname: '演示买家', avatarUrl: 'https://via.placeholder.com/160/4a8b6e/ffffff?text=B' },
      seller: { id: 2, username: 'demo_seller', nickname: '硬核数码卖家', avatarUrl: 'https://via.placeholder.com/160/24333f/ffffff?text=S' },
      product: null,
      status: 'SHIPPED',
      price: 6899,
      discountAmount: 0,
      createdAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
      paidAt: new Date(Date.now() - 10 * 60 * 60 * 1000).toISOString(),
      shippingInfo: {
        expressCompany: '顺丰速运',
        expressCode: 'SF',
        trackingNumber: 'MOCK-SF-9001',
        shippingStatus: 'DELIVERING',
        shippedAt: new Date(Date.now() - 70 * 1000).toISOString(),
        estimatedDeliveryTime: new Date(Date.now() + 50 * 1000).toISOString(),
        trackingEvents: [
          { time: new Date(Date.now() - 9 * 60 * 60 * 1000).toISOString(), status: 'PICKED', location: '上海平台仓', description: '平台仓已出库' },
          { time: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), status: 'DELIVERING', location: '上海徐汇', description: '快件正在派送中' },
        ],
      },
    },
    {
      id: 9002,
      buyer: { id: 1, username: 'demo_buyer', nickname: '演示买家', avatarUrl: 'https://via.placeholder.com/160/4a8b6e/ffffff?text=B' },
      seller: { id: 2, username: 'demo_seller', nickname: '硬核数码卖家', avatarUrl: 'https://via.placeholder.com/160/24333f/ffffff?text=S' },
      product: null,
      status: 'REFUNDING',
      price: 7399,
      discountAmount: 100,
      createdAt: new Date(Date.now() - 3 * dayMs).toISOString(),
      paidAt: new Date(Date.now() - 3 * dayMs + 30 * 60 * 1000).toISOString(),
      shippingInfo: {
        expressCompany: '京东快递',
        trackingNumber: 'MOCK-JD-9002',
        shippingStatus: 'DELIVERED',
        trackingEvents: [
          { time: new Date(Date.now() - 2 * dayMs).toISOString(), status: 'DELIVERED', location: '杭州西湖', description: '快件已签收' },
        ],
      },
    },
  ],
  refundRequests: [
    {
      id: 7001,
      orderId: 9002,
      refundType: 'RETURN_REFUND',
      reason: '屏幕边框磕碰比描述明显，希望退货退款。',
      refundAmount: 7299,
      status: 'PENDING',
      createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
      updatedAt: now(),
    },
  ],
  inspections: [
    {
      id: 501,
      orderId: 9001,
      productId: 101,
      inspectionId: 501,
      reportNo: 'TU-RPT-20260514-001',
      reportId: 'TU-RPT-20260514-001',
      status: 'COMPLETED',
      grade: 'A',
      createdAt: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
      productTitle: 'iPhone 15 Pro Max 256G 原色钛金属',
      productImage: placeholder('iPhone 15 Pro Max'),
      categoryName: '数码',
      productSpec: '256G · 原色钛金属',
      inspectorName: 'QC-17',
      inspectionCenterName: '上海',
      resultSummary: '平台验货完成：屏幕、主板、摄像头、电池均通过检测。',
      items: [
        { id: 1, itemName: '屏幕显示', status: 'PASSED', notes: '正常' },
        { id: 2, itemName: '电池健康', status: 'PASSED', notes: '91%' },
        { id: 3, itemName: '拆修痕迹', status: 'PASSED', notes: '无' },
      ],
    },
  ],
  comments: [
    {
      id: 301,
      productId: 101,
      buyerName: '验机党',
      buyerAvatar: 'https://via.placeholder.com/120/4a8b6e/ffffff?text=Y',
      content: '报告里电池和拆修说明很清楚，适合高客单价先看结论。',
      isAnonymous: false,
      createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      images: [],
    },
  ],
  favorites: [{ id: 401, productId: 101, userId: 1 }],
  histories: [
    { id: 601, productId: 101, userId: 1, viewedAt: new Date(Date.now() - 45 * 60 * 1000).toISOString() },
    { id: 602, productId: 102, userId: 1, viewedAt: new Date(Date.now() - dayMs).toISOString() },
  ],
  wallet: {
    balance: 15888,
    frozenAmount: 0,
    hasPassword: true,
    transactions: [
      { id: 801, type: 'TOP_UP', amount: 20000, status: 'SUCCESS', description: 'mock 充值', createdAt: new Date(Date.now() - 7 * dayMs).toISOString() },
      { id: 802, type: 'PAYMENT', amount: -6899, status: 'SUCCESS', description: '支付订单 #9001', createdAt: new Date(Date.now() - 10 * 60 * 60 * 1000).toISOString() },
    ],
  },
  addresses: [
    {
      id: 201,
      recipientName: '演示买家',
      phone: '13800000000',
      province: '上海市',
      city: '上海市',
      district: '徐汇区',
      detailedAddress: 'TrueUsed mock 路 88 号',
      areaCode: '310104',
      isDefault: true,
    },
  ],
  notifications: [
    {
      id: 1001,
      title: '平台验货报告已生成',
      content: '你的 iPhone 15 Pro Max 验货报告已生成，可在订单详情查看。',
      type: 'INSPECTION_READY',
      relatedId: 9001,
      read: false,
      createdAt: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
    },
  ],
  conversations: [
    {
      id: 1101,
      otherUserId: 2,
      otherUserName: '硬核数码卖家',
      otherUserAvatar: 'https://via.placeholder.com/160/24333f/ffffff?text=S',
      lastMessage: '可以看平台验货报告，拆修项都写在里面了。',
      lastMessageTime: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      unreadCount: 1,
    },
  ],
  messages: [
    {
      id: 1201,
      conversationId: 1101,
      senderId: 2,
      receiverId: 1,
      content: '可以看平台验货报告，拆修项都写在里面了。',
      timestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
      self: false,
    },
  ],
  consignments: [
    {
      id: 1301,
      title: 'Switch OLED 白色款寄售',
      expectedPrice: 1399,
      status: 'INSPECTING',
      trackingNoInbound: 'MOCK-IN-1301',
      product: { id: 104, images: [{ url: placeholder('Switch OLED') }] },
    },
  ],
})

function loadState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return hydrate(JSON.parse(raw))
  } catch {}
  const state = hydrate(seed())
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  return state
}

function hydrate(state) {
  if (!Array.isArray(state.userFollows)) {
    state.userFollows = []
  }
  state.orders.forEach((order) => {
    if (!order.product) {
      order.product = getProduct(state, order.id === 9002 ? 102 : 101)
    }
  })
  return state
}

function saveState(state) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function currentUser(state) {
  const id = Number(localStorage.getItem(CURRENT_USER_KEY) || '1')
  return state.users.find((user) => user.id === id) || state.users[0]
}

function pageOf(list, params = {}) {
  const page = Number(params.page ?? 0)
  const size = Number(params.size ?? (list.length || 10))
  const start = page * size
  const content = list.slice(start, start + size)
  return {
    content,
    totalElements: list.length,
    totalPages: size > 0 ? Math.ceil(list.length / size) : 1,
    number: page,
    size,
    first: page === 0,
    last: start + size >= list.length,
    empty: content.length === 0,
  }
}

function json(config, data) {
  return {
    data: { code: 0, message: 'OK', data, timestamp: Date.now(), mock: true },
    status: 200,
    statusText: 'OK',
    headers: { 'x-trueused-mock': '1' },
    config,
    request: {},
  }
}

function httpError(config, status, message) {
  const error = new Error(message)
  error.config = config
  error.response = {
    data: { code: status, message, data: null, timestamp: Date.now(), mock: true },
    status,
    statusText: message,
    headers: { 'x-trueused-mock': '1' },
    config,
    request: {},
  }
  return error
}

function normalizePath(url = '') {
  return new URL(url, 'http://mock.trueused.local').pathname.replace(/^\/api/, '') || '/'
}

function parseData(data) {
  if (!data) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      return {}
    }
  }
  return data
}

function listProducts(state, params = {}, sellerOnly = false) {
  let list = [...state.products]
  if (sellerOnly) list = list.filter((item) => item.seller?.id === 2)
  if (params.q) {
    const q = String(params.q).toLowerCase()
    list = list.filter((item) => item.title.toLowerCase().includes(q) || item.description.toLowerCase().includes(q))
  }
  if (params.categoryId) list = list.filter((item) => Number(item.category?.id) === Number(params.categoryId))
  if (params.sellerId) list = list.filter((item) => Number(item.seller?.id) === Number(params.sellerId))
  if (params.status) list = list.filter((item) => item.status === params.status)
  if (params.priceMin) list = list.filter((item) => Number(item.price) >= Number(params.priceMin))
  if (params.priceMax) list = list.filter((item) => Number(item.price) <= Number(params.priceMax))
  if (params.sort === 'price_asc') list.sort((a, b) => Number(a.price) - Number(b.price))
  else if (params.sort === 'price_desc') list.sort((a, b) => Number(b.price) - Number(a.price))
  else if (params.sort === 'views_desc') list.sort((a, b) => Number(b.viewsCount || 0) - Number(a.viewsCount || 0))
  else list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  return pageOf(list, params)
}

function getProduct(state, id) {
  return state.products.find((item) => Number(item.id) === Number(id)) || state.products[0]
}

function getOrder(state, id) {
  const order = state.orders.find((item) => Number(item.id) === Number(id)) || state.orders[0]
  if (!order.product) order.product = getProduct(state, 101)
  return order
}

function offsetTime(base, ms) {
  return new Date(new Date(base).getTime() + ms).toISOString()
}

function resolveShippingStart(order) {
  const explicitShippingStart =
    order.shippedAt ||
    order.shippingInfo?.shippedAt
  if (explicitShippingStart) return explicitShippingStart

  const status = order.shippingInfo?.shippingStatus
  if (!status) {
    return order.shippingInfo?.trackingEvents?.[0]?.time || now()
  }

  const elapsedMs = shippingStatusElapsedMs[status] ?? 0
  return new Date(Date.now() - elapsedMs - 1000).toISOString()
}

function refreshMockShippingInfo(order) {
  if (!order) return null

  const shippedAt = resolveShippingStart(order)
  const shippedAtMs = new Date(shippedAt).getTime()
  const elapsedMs = Date.now() - shippedAtMs
  const expressCompany = order.shippingInfo?.expressCompany || order.expressCompany || '顺丰速运'
  const trackingNumber = order.shippingInfo?.trackingNumber || order.trackingNumber || `MOCK-${order.id}`
  const expressCode = order.shippingInfo?.expressCode || order.expressCode || expressCodes[expressCompany] || 'OTHER'
  const estimatedDeliveryTime = offsetTime(shippedAt, deliveredAfterMs)
  const sender = order.product?.locationText || 'mock 仓'
  const receiver = order.address?.city || '收货地'

  const timeline = [
    { afterMs: 0, status: 'PENDING', location: sender, description: '卖家已发货，快递员正在揽收中' },
    { afterMs: pickedAfterMs, status: 'PICKED', location: `${sender}营业部`, description: '快递员已揽收，正在发往分拨中心' },
    { afterMs: departedAfterMs, status: 'IN_TRANSIT', location: `${sender}分拨中心`, description: `快件已到达分拨中心，准备发往${receiver}` },
    { afterMs: transitAfterMs, status: 'IN_TRANSIT', location: '郑州转运中心', description: `快件已到达郑州转运中心，正在发往${receiver}` },
    { afterMs: deliveringAfterMs, status: 'DELIVERING', location: `${receiver}分拨中心`, description: '快件已到达目的地分拨中心，正在派送中' },
    { afterMs: deliveryStationAfterMs, status: 'DELIVERING', location: `${receiver}配送站`, description: '快件正在派送中，派送员：李师傅，电话：138****8888' },
    { afterMs: deliveredAfterMs, status: 'DELIVERED', location: receiver, description: `快件已签收，签收人：本人签收。感谢使用${expressCompany}！` },
  ]

  const trackingEvents = timeline
    .filter((event) => elapsedMs >= event.afterMs)
    .map((event) => ({
      time: offsetTime(shippedAt, event.afterMs),
      status: event.status,
      location: event.location,
      description: event.description,
    }))

  const shippingInfo = {
    ...(order.shippingInfo || {}),
    shipmentType: order.product?.tradeModel === 'OFFICIAL_INSPECTION' ? 'PLATFORM_OUTBOUND' : 'SELLER_OUTBOUND',
    expressCompany,
    expressCode,
    trackingNumber,
    shippingStatus: trackingEvents[trackingEvents.length - 1]?.status || 'PENDING',
    shippedAt,
    estimatedDeliveryTime,
    trackingEvents,
  }

  order.shippingInfo = shippingInfo
  order.expressCompany = expressCompany
  order.expressCode = expressCode
  order.trackingNumber = trackingNumber
  order.shippedAt = shippedAt
  order.estimatedDeliveryTime = estimatedDeliveryTime
  return shippingInfo
}

function route(config, state) {
  const method = String(config.method || 'get').toLowerCase()
  const path = normalizePath(config.url)
  const params = config.params || {}
  const data = parseData(config.data)
  const me = currentUser(state)

  if (method === 'post' && path === '/auth/login') {
    const username = data.username || data.email || 'demo_buyer'
    const user = state.users.find((item) => item.username === username || item.email === username) || state.users[0]
    localStorage.setItem(CURRENT_USER_KEY, String(user.id))
    return { token: `mock-token-${user.id}`, expiresInMs: 24 * 60 * 60 * 1000, user }
  }
  if (method === 'post' && path === '/auth/register') {
    const user = {
      id: Math.max(...state.users.map((item) => item.id)) + 1,
      username: data.username || `mock_user_${Date.now()}`,
      nickname: data.nickname || data.username || 'mock_user',
      email: data.email || `mock_${Date.now()}@trueused.local`,
      avatarUrl: 'https://via.placeholder.com/160/4a8b6e/ffffff?text=U',
      role: 'USER',
    }
    state.users.push(user)
    localStorage.setItem(CURRENT_USER_KEY, String(user.id))
    return { token: `mock-token-${user.id}`, expiresInMs: 24 * 60 * 60 * 1000, user }
  }
  if (method === 'post' && path === '/auth/logout') return null
  if (method === 'post' && path === '/auth/refresh') return { token: `mock-token-${me.id}`, expiresInMs: 24 * 60 * 60 * 1000, user: me }

  if (path === '/users/me') {
    if (method === 'put') {
      Object.assign(me, data)
      return me
    }
    return me
  }
  if (path === '/users/me/stats') {
    return {
      onShelfProducts: state.products.filter((item) => item.status === 'ON_SALE').length,
      pendingOrders: state.orders.filter((item) => ['PAID', 'PENDING_SHIPMENT'].includes(item.status)).length,
      violationProducts: 0,
      totalIncome: 28678,
      unreadMessages: state.conversations.reduce((sum, item) => sum + Number(item.unreadCount || 0), 0),
      viewsCount: state.products.reduce((sum, item) => sum + Number(item.viewsCount || 0), 0),
    }
  }
  const publicProfile = path.match(/^\/users\/(\d+)\/public-profile$/)
  if (method === 'get' && publicProfile) {
    const userId = Number(publicProfile[1])
    return {
      ...state.users.find((item) => Number(item.id) === userId) || state.users[1],
      sellingCount: state.products.filter((item) => Number(item.seller?.id) === userId && item.status === 'ON_SALE').length,
      soldCount: 6,
      followerCount: state.userFollows.filter((item) => Number(item.followedId) === userId).length,
      following: state.userFollows.some((item) => Number(item.followerId) === Number(me.id) && Number(item.followedId) === userId),
    }
  }
  const userFollow = path.match(/^\/users\/(\d+)\/follow$/)
  if (userFollow && ['post', 'delete'].includes(method)) {
    const followedId = Number(userFollow[1])
    if (method === 'post' && Number(me.id) !== followedId) {
      const exists = state.userFollows.some((item) => Number(item.followerId) === Number(me.id) && Number(item.followedId) === followedId)
      if (!exists) {
        state.userFollows.push({ id: Date.now(), followerId: me.id, followedId, createdAt: now(), updatedAt: now() })
      }
    }
    if (method === 'delete') {
      state.userFollows = state.userFollows.filter((item) => !(Number(item.followerId) === Number(me.id) && Number(item.followedId) === followedId))
    }
    return {
      userId: followedId,
      followerCount: state.userFollows.filter((item) => Number(item.followedId) === followedId).length,
      following: state.userFollows.some((item) => Number(item.followerId) === Number(me.id) && Number(item.followedId) === followedId),
    }
  }
  if (method === 'get' && path === '/users/me/following') {
    const list = state.userFollows
      .filter((item) => Number(item.followerId) === Number(me.id))
      .map((follow) => {
        const user = state.users.find((item) => Number(item.id) === Number(follow.followedId)) || state.users[1]
        return {
          ...user,
          followedAt: follow.createdAt,
          sellingCount: state.products.filter((item) => Number(item.seller?.id) === Number(user.id) && item.status === 'ON_SALE').length,
          soldCount: 6,
          followerCount: state.userFollows.filter((item) => Number(item.followedId) === Number(user.id)).length,
        }
      })
    return pageOf(list, params)
  }

  if (method === 'get' && path === '/categories') return state.categories
  if (method === 'get' && path === '/categories/root') return state.categories

  if (method === 'get' && path === '/products') return listProducts(state, params)
  if (method === 'get' && path === '/products/my') return listProducts(state, params, true)
  const productMatch = path.match(/^\/products\/(\d+)$/)
  if (productMatch) {
    if (method === 'get') return getProduct(state, productMatch[1])
    if (method === 'put') {
      const product = getProduct(state, productMatch[1])
      Object.assign(product, data, { updatedAt: now() })
      return product
    }
    if (method === 'delete') {
      state.products = state.products.filter((item) => Number(item.id) !== Number(productMatch[1]))
      return null
    }
  }
  const productAction = path.match(/^\/products\/(\d+)\/(publish|hide|polish)$/)
  if (productAction) {
    const product = getProduct(state, productAction[1])
    if (productAction[2] === 'publish') product.status = 'ON_SALE'
    if (productAction[2] === 'hide') product.status = 'OFF_SHELF'
    product.updatedAt = now()
    return product
  }
  if (method === 'post' && path === '/products') {
    const product = {
      id: Date.now(),
      ...state.products[0],
      ...data,
      seller: me,
      category: state.categories.find((item) => Number(item.id) === Number(data.categoryId)) || state.categories[0],
      images: data.images?.length ? data.images : [{ url: placeholder(data.title || 'TrueUsed Product') }],
      status: data.tradeModel === 'OFFICIAL_INSPECTION' ? 'PENDING' : 'ON_SALE',
      viewsCount: 0,
      favoritesCount: 0,
      createdAt: now(),
      updatedAt: now(),
    }
    state.products.unshift(product)
    return product
  }

  if (method === 'get' && path === '/favorites') return pageOf(state.favorites.map((item) => ({ ...item, product: getProduct(state, item.productId) })), params)
  const favoriteMatch = path.match(/^\/favorites\/products\/(\d+)$/)
  if (favoriteMatch && method === 'post') {
    const productId = Number(favoriteMatch[1])
    if (!state.favorites.some((item) => Number(item.productId) === productId)) state.favorites.push({ id: Date.now(), productId, userId: me.id })
    return null
  }
  if (favoriteMatch && method === 'delete') {
    state.favorites = state.favorites.filter((item) => Number(item.productId) !== Number(favoriteMatch[1]))
    return null
  }

  if (method === 'get' && path === '/history') return pageOf(state.histories.map((item) => ({ ...item, product: getProduct(state, item.productId) })), params)

  if (method === 'get' && path === '/orders/my-orders') {
    state.orders.filter((order) => order.status === 'SHIPPED').forEach(refreshMockShippingInfo)
    return state.orders
  }
  if (method === 'get' && path === '/orders/sold-orders') {
    state.orders.filter((order) => order.status === 'SHIPPED').forEach(refreshMockShippingInfo)
    return state.orders
  }
  if (method === 'post' && path === '/orders') {
    const product = getProduct(state, data.productId)
    const order = {
      id: Date.now(),
      buyer: me,
      seller: product.seller,
      product,
      status: 'PENDING_PAYMENT',
      price: product.price,
      discountAmount: 0,
      createdAt: now(),
    }
    state.orders.unshift(order)
    product.status = 'LOCKED'
    return order
  }
  const orderMatch = path.match(/^\/orders\/(\d+)$/)
  if (orderMatch && method === 'get') {
    const order = getOrder(state, orderMatch[1])
    if (order.status === 'SHIPPED') refreshMockShippingInfo(order)
    return order
  }
  const shippingMatch = path.match(/^\/orders\/(\d+)\/shipping$/)
  if (shippingMatch && method === 'get') return refreshMockShippingInfo(getOrder(state, shippingMatch[1])) || {}
  if (method === 'get' && path === '/orders/express-companies') return ['顺丰速运', '京东快递', '中通快递']
  const orderAction = path.match(/^\/orders\/(\d+)\/(pay|pay-wallet|ship|confirm-delivery|cancel|refund-request|refund-detail|refund-approve|refund-reject|refund-complete)$/)
  if (orderAction) {
    const order = getOrder(state, orderAction[1])
    const refund = state.refundRequests.find((item) => Number(item.orderId) === Number(order.id))
    if (method === 'put' && ['pay', 'pay-wallet'].includes(orderAction[2])) {
      order.status = order.product?.tradeModel === 'OFFICIAL_INSPECTION' ? 'PENDING_SHIPMENT' : 'PAID'
      order.paidAt = now()
      return order
    }
    if (method === 'put' && orderAction[2] === 'ship') {
      order.status = 'SHIPPED'
      order.shippedAt = now()
      order.expressCompany = data.expressCompany || '顺丰速运'
      order.expressCode = expressCodes[order.expressCompany] || 'OTHER'
      order.trackingNumber = data.trackingNumber || `MOCK-${order.id}`
      order.shippingInfo = {
        shipmentType: order.product?.tradeModel === 'OFFICIAL_INSPECTION' ? 'PLATFORM_OUTBOUND' : 'SELLER_OUTBOUND',
        expressCompany: data.expressCompany || '顺丰速运',
        expressCode: order.expressCode,
        trackingNumber: data.trackingNumber || `MOCK-${order.id}`,
        shippingStatus: 'PENDING',
        shippedAt: order.shippedAt,
        estimatedDeliveryTime: offsetTime(order.shippedAt, deliveredAfterMs),
        trackingEvents: [{ time: order.shippedAt, status: 'PENDING', location: 'mock 仓', description: '卖家已发货，快递员正在揽收中' }],
      }
      return order
    }
    if (method === 'put' && orderAction[2] === 'confirm-delivery') {
      if (Number(order.buyer?.id) !== Number(me.id)) {
        throw httpError(config, 403, "You are not authorized to confirm this order's delivery")
      }
      if (order.status !== 'SHIPPED') {
        throw httpError(config, 409, 'Order delivery cannot be confirmed')
      }
      const shippingInfo = refreshMockShippingInfo(order)
      if (!['DELIVERING', 'DELIVERED'].includes(shippingInfo?.shippingStatus)) {
        throw httpError(config, 409, 'Package is not ready for delivery confirmation yet')
      }
      order.status = 'COMPLETED'
      order.deliveredAt = now()
      return order
    }
    if (method === 'put' && orderAction[2] === 'cancel') {
      order.status = 'CANCELLED'
      return order
    }
    if (method === 'post' && orderAction[2] === 'refund-request') {
      const next = {
        id: Date.now(),
        orderId: order.id,
        refundType: data.refundType || 'REFUND_ONLY',
        reason: data.reason || 'mock 售后申请',
        refundAmount: Number(data.refundAmount || order.price),
        status: 'PENDING',
        createdAt: now(),
        updatedAt: now(),
      }
      state.refundRequests.unshift(next)
      order.status = 'REFUNDING'
      return next
    }
    if (method === 'get' && orderAction[2] === 'refund-detail') return refund || null
    if (refund && method === 'put' && orderAction[2] === 'refund-approve') refund.status = 'APPROVED'
    if (refund && method === 'put' && orderAction[2] === 'refund-reject') refund.status = 'REJECTED'
    if (refund && method === 'put' && orderAction[2] === 'refund-complete') {
      refund.status = 'COMPLETED'
      order.status = 'REFUNDED'
    }
    if (refund) refund.updatedAt = now()
    return refund || order
  }

  if (method === 'get' && path === '/inspections/my') return state.inspections
  const inspectionFlow = path.match(/^\/inspections\/(\d+)\/flow$/)
  if (inspectionFlow) return state.inspections.find((item) => Number(item.orderId) === Number(inspectionFlow[1])) || state.inspections[0]
  const inspectionReport = path.match(/^\/inspections\/orders\/(\d+)\/report$/)
  if (inspectionReport) return state.inspections.find((item) => Number(item.orderId) === Number(inspectionReport[1])) || null
  if (method === 'post' && path === '/platform/receive_package') return state.inspections[0]

  if (method === 'get' && path === '/reviews/my') return state.comments
  if (method === 'get' && path === '/reviews/received') return pageOf(state.comments, params)
  if (path.match(/^\/reviews\/products\/\d+$/) || path.match(/^\/reviews\/sellers\/\d+$/)) return pageOf(state.comments, params)
  if (path.match(/^\/comments\/products\/\d+$/) || path.match(/^\/comments\/sellers\/\d+$/)) return pageOf(state.comments, params)
  if (method === 'post' && path === '/comments') return { id: Date.now(), buyerName: me.nickname || me.username, buyerAvatar: me.avatarUrl, content: data.content, createdAt: now(), isAnonymous: false }
  if (method === 'post' && path === '/reviews') return { id: Date.now(), ...data, createdAt: now() }

  if (method === 'get' && path === '/wallet') return state.wallet
  if (method === 'post' && path === '/wallet/set-password') return state.wallet
  if (method === 'post' && path === '/wallet/top-up') {
    state.wallet.balance += Number(data.amount || 0)
    return state.wallet
  }
  if (method === 'post' && path === '/wallet/withdraw') {
    state.wallet.balance -= Number(data.amount || 0)
    return state.wallet
  }
  if (method === 'get' && path === '/wallet/transactions') return pageOf(state.wallet.transactions, params)

  if (method === 'get' && path === '/addresses') return state.addresses
  if (method === 'post' && path === '/addresses') {
    const address = { id: Date.now(), ...data }
    state.addresses.push(address)
    return address
  }
  const addressMatch = path.match(/^\/addresses\/(\d+)$/)
  if (addressMatch) {
    const address = state.addresses.find((item) => Number(item.id) === Number(addressMatch[1])) || state.addresses[0]
    if (method === 'get') return address
    if (method === 'put') {
      Object.assign(address, data)
      return address
    }
    if (method === 'delete') {
      state.addresses = state.addresses.filter((item) => Number(item.id) !== Number(addressMatch[1]))
      return null
    }
  }

  if (method === 'get' && path === '/notifications') return pageOf(state.notifications, params)
  if (method === 'get' && path === '/notifications/unread-count') return state.notifications.filter((item) => !item.read).length
  if (method === 'put' && path === '/notifications/read-all') {
    state.notifications.forEach((item) => { item.read = true })
    return null
  }
  const notificationMatch = path.match(/^\/notifications\/(\d+)\/read$/)
  if (notificationMatch && method === 'put') {
    const item = state.notifications.find((row) => Number(row.id) === Number(notificationMatch[1]))
    if (item) item.read = true
    return null
  }

  if (method === 'get' && path === '/conversations') return state.conversations
  if (method === 'get' && path === '/conversations/online') return [1, 2]
  if (method === 'post' && path === '/conversations/start') {
    const otherUserId = Number(params.userId)
    const existing = state.conversations.find((item) => Number(item.otherUserId) === otherUserId)
    if (existing) return existing
    const otherUser = state.users.find((item) => Number(item.id) === otherUserId) || state.users[1]
    const conversation = {
      id: Date.now(),
      otherUserId: otherUser.id,
      otherUserName: otherUser.nickname || otherUser.username,
      otherUserAvatar: otherUser.avatarUrl,
      lastMessage: '',
      lastMessageTime: null,
      unreadCount: 0,
    }
    state.conversations.unshift(conversation)
    return conversation
  }
  const messagesMatch = path.match(/^\/conversations\/(\d+)\/messages$/)
  if (messagesMatch && method === 'get') return state.messages.filter((item) => Number(item.conversationId) === Number(messagesMatch[1]))
  if (method === 'post' && path === '/messages') {
    const message = { id: Date.now(), conversationId: state.conversations[0].id, senderId: me.id, receiverId: data.receiverId, content: data.content, timestamp: now(), self: true }
    state.messages.push(message)
    return message
  }

  if (method === 'get' && path === '/consignments') return state.consignments
  if (method === 'post' && path === '/consignments') {
    const consignment = { id: Date.now(), status: 'CREATED', ...data }
    state.consignments.push(consignment)
    return consignment
  }
  const consignmentMatch = path.match(/^\/consignments\/(\d+)\/logistics$/)
  if (consignmentMatch && method === 'put') {
    const consignment = state.consignments.find((item) => Number(item.id) === Number(consignmentMatch[1]))
    if (consignment) {
      consignment.status = 'SHIPPED'
      consignment.trackingNoInbound = data.trackingNo
    }
    return consignment
  }

  if (method === 'get' && path === '/coupons') return [{ id: 1, code: 'MOCK100', title: '演示满减券', discountAmount: 100, minSpend: 1000, type: 'DISCOUNT', isActive: true }]
  if (method === 'get' && path === '/coupons/my') return []
  if (path.startsWith('/coupons/')) return null

  return method === 'get' ? null : { ok: true }
}

export function isMockEnabled() {
  return String(import.meta.env.VITE_USE_MOCK || '').toLowerCase() === 'true'
}

export async function mockAdapter(config) {
  const state = loadState()
  const result = route(config, state)
  saveState(state)
  return json(config, clone(result))
}
