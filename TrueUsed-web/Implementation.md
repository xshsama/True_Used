# 功能页面落实记录（占位页 → 实际页）

本文档记录从占位页面到可用基础页面的实现进度、交互说明与后续接入建议。

更新时间：2026-03-17

## 本轮更新（2026-03-17）

1. Home 首页（`src/views/Home.vue`）

- 调整首屏视觉与文案表达，弱化“官方背书”口号，改为突出成色、拆修、配件等交易判断信息。
- 新增“看最新上架”滚动导流，首页首屏与商品列表衔接更直接。
- 保留桌面端展示优先的结构，继续服务于作品集和答辩演示场景。

1. InspectionReport 验货报告列表（`src/views/InspectionReport.vue`）

- 页面结构收口到主导航壳层内，不再维持一套额外的独立说明侧栏。
- 列表卡片开始优先展示后端返回的 `reportNo`、商品规格、质检员、验货中心等真实字段；缺值时再回退到占位文案。
- 报告通过/驳回状态、关键检测项和跳转入口继续保留。

1. Auth 登出联动（`src/api/auth.js`）

- 登出请求补发本地 access token 到 `Authorization` 头，配合后端服务端撤销逻辑，避免只清 refresh cookie。

1. 文案调整（`src/views/Login.vue`、`src/views/PostCreate.vue`、`src/views/Profile.vue`）

- 登录页改为“平台验货流程展示”表达，避免过强承诺式文案。
- 发布页将“官方验货费”调整为“平台验货服务费”。
- 个人中心统计文案改成“待接真实数据”口径，明确当前仍属占位信息。

## 已完成

1. Orders 订单列表（`src/views/Orders.vue`）

- 功能：Tabs 状态筛选（all/unpaid/toship/toreceive/afterSale）、下拉刷新、分页加载、骨架屏、空态。
- 交互：支持从路由 query.status 初始化选中状态（例如 /orders?status=unpaid）。提供支付/取消/确认收货/查看等按钮（占位交互）。
- 数据：本地 mock。

1. Address 地址管理（`src/views/Address.vue`）

- 功能：列表展示、设为默认、编辑/新增弹窗、删除（含右滑删除）。
- 数据：localStorage 持久化（键：tu.address.list）。

1. Settings 系统设置（`src/views/Settings.vue`）

- 功能：常见开关（消息通知、营销推送、个性化推荐）持久化；深色模式占位；退出登录确认。
- 数据：localStorage 持久化（键：tu.settings）。

1. Help 帮助中心（`src/views/Help.vue`）

- 功能：搜索 + FAQ 折叠展示；无结果空态。

1. Verification 实名认证（`src/views/Verification.vue`）

- 功能：姓名、身份证号校验、身份证正反面上传（大小 5MB 限制）、提交后进入“审核中”；支持展示“已通过”占位。
- 数据：localStorage 持久化状态（键：tu.verification）。

1. ServiceCenter 客服中心（`src/views/ServiceCenter.vue`）

- 功能：客服入口宫格（客服消息/客服电话/在线客服），FAQ 分类折叠；跳转客服消息页。

1. Feedback 意见反馈（`src/views/Feedback.vue`）

- 功能：问题类型选择、描述、联系方式、图片上传（<=5MB，最多 4 张）；提交提示并返回。

1. About 关于（`src/views/About.vue`）

- 功能：应用信息、版本、项目主页、协议、反馈入口。

1. MyProducts 我的发布（`src/views/MyProducts.vue`）

- 功能：列表展示、上/下架、编辑、删除，占位交互；下拉刷新/分页加载。

1. SoldProducts 已售出（`src/views/SoldProducts.vue`）

- 功能：已售记录展示、售后中标识、查看占位；分页加载。

1. ProductManageCenter 商品管理中心（`src/views/ProductManageCenter.vue`）

- 功能：仪表卡片，快捷入口到 发布商品/我的发布/订单管理。

1. OrderManage 卖家订单管理（`src/views/OrderManage.vue`）

- 功能：Tabs 筛选（全部/待发货/售后）、分页加载、发货和售后处理占位。

1. Settlement 资金结算（`src/views/Settlement.vue`）

- 功能：余额展示、资金明细/结算记录 Tab、分页加载；提现弹窗校验与余额更新；支持 /settlement?action=withdraw 直达弹窗。

1. ServiceMessages 客服消息（`src/views/ServiceMessages.vue`）

- 功能：客服会话列表、未读角标、点击跳转消息页。

## 组件注册补充

- 在 `src/main.js` 注册了 Switch、SwipeCell、Collapse、CollapseItem 以支撑新页面 UI。

## 路由连通

- `src/views/Profile.vue` 和 `src/components/SellerCenter.vue` 的入口已切换为实际 `router.push`，与路由表一致。

## 后续接入建议（API 阶段）

- 将本地 mock 改为接口：统一在 `src/api` 目录下新增对应模块。
- 订单与卖家订单列表：支持分页与状态过滤的 API；按钮操作（取消、发货、确认收货）接入接口。
- 地址管理：提供增删改查接口，默认地址唯一性由后端校验。
- 实名认证：上传走对象存储，后端走 OCR/风控；状态轮询或回调。
- 资金结算：对接钱包/账务服务；提现走工单或第三方支付；增加失败重试提示。
- 客服消息：接入会话与消息列表接口，未读计数走轮询或 WebSocket。

## 运行与验证

- 开发运行：`npm run dev` 后在桌面浏览器中逐页验证交互、刷新与分页。
- 路由直达：
  - 订单状态直达：/orders?status=toreceive
  - 结算提现直达：/settlement?action=withdraw

## 备注

- 当前仓库已不是纯“无后端占位逻辑”状态，认证、部分验货数据和主交易链路已与后端接口联动。
- 仍有部分页面与统计数据使用 mock 或占位文案，提交 README 或演示材料时需要区分“已接接口”和“待接真实数据”两类能力。
