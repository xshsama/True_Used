# 问题解决日志

本文件用于记录每次任务完成后的问题与解决过程。

## 2026-03-03 初始化日志机制

- 问题描述：
  仓库中缺少统一的“问题解决记录”方式。
- 根因分析：
  没有强制记录规则，也没有统一的 Markdown 日志文件。
- 解决方法：
  新增 `AGENTS.md` 记录规则，并创建本日志文件模板。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/AGENTS.md`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  检查两个文件是否存在且模板字段完整。
- 结果：
  后续任务可按统一格式持续追加记录。
- 后续建议：
  每次任务完成后追加一条日志。

## YYYY-MM-DD 任务标题

- 问题描述：
- 根因分析：
- 解决方法：
- 修改文件：
- 验证方式：
- 结果：
- 后续建议：

## 2026-03-04 日志看起来是空的

- 问题描述：
  用户反馈 Markdown 日志文件看起来是空的。
- 根因分析：
  实际项目目录是 `/Users/xshsama/code/TrueUsed/TrueUsed`，可能查看了外层目录。
- 解决方法：
  确认真实日志路径并核对内容，验证历史记录存在。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  校验日志行数并打印该路径下文件内容。
- 结果：
  确认活动项目中的日志并非空文件。
- 后续建议：
  后续统一在内层项目路径打开并维护日志。

## 2026-03-04 前后端分别提交 Git

- 问题描述：
  需要将前端与后端当前变更各提交一次。
- 根因分析：
  两个仓库都存在大量未提交改动，影响后续版本追踪。
- 解决方法：
  在后端仓库与前端仓库分别执行 `git add -A` 与 `git commit`。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed`（后端仓库，已提交）
  `/Users/xshsama/code/TrueUsed/TrueUsed-web`（前端仓库，已提交）
- 验证方式：
  查看提交输出并记录最新提交哈希。
- 结果：
  后端提交哈希为 `dce7a89`，前端提交哈希为 `ea2e12a`。
- 后续建议：
  如需同步远端，可分别执行 `git push`。

## 2026-03-04 清理冗余文档与无用截图

- 问题描述：
  需要检查并删除老旧冗余的 Markdown 文件和无用图片文件。
- 根因分析：
  根目录存在历史合并后的旧计划文档，以及未被项目引用的临时截图，造成目录噪音。
- 解决方法：
  删除已明确“内容已合并到 ROADMAP”的旧文档，以及 7 张未被引用的调试截图。
- 修改文件：
  删除 `/Users/xshsama/code/TrueUsed/CHAT_IMPLEMENTATION_PLAN.md`
  删除 `/Users/xshsama/code/TrueUsed/CHAT_OPTIMIZATION_PLAN.md`
  删除 `/Users/xshsama/code/TrueUsed/pw_order_after_submit.png`
  删除 `/Users/xshsama/code/TrueUsed/pw_order_product.png`
  删除 `/Users/xshsama/code/TrueUsed/pw_order_settlement_before_submit.png`
  删除 `/Users/xshsama/code/TrueUsed/trueused_login_result.png`
  删除 `/Users/xshsama/code/TrueUsed/trueused_register_result.png`
  删除 `/Users/xshsama/code/TrueUsed/ui_click_order_after_fix.png`
  删除 `/Users/xshsama/code/TrueUsed/ui_click_order_result.png`
  更新 `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  重新扫描根目录文件清单并逐一确认目标文件不存在。
- 结果：
  已完成高置信度冗余文件清理，根目录仅保留仍有参考价值的文档。
- 后续建议：
  后续调试截图建议放入临时目录并定期清理。

## 2026-03-12 加固 JWT 类型校验与服务端登出撤销

- 问题描述：
  认证过滤器仅校验 JWT 是否合法，未强制要求 `typ=access`；登出流程只清理浏览器 Cookie，旧 token 在有效期内仍可能继续使用。
- 根因分析：
  HTTP 与 WebSocket 认证入口都只调用了通用 `validateToken`；服务端没有持久化撤销状态，且前端登出请求默认不会携带 `Authorization` 头。
- 解决方法：
  在 HTTP/WebSocket 认证入口增加 `access token` 类型校验；新增持久化撤销表与 `TokenRevocationService`，对登出和 refresh 轮换时的旧令牌做服务端撤销；前端登出请求显式附带当前 `Authorization` 头，确保 access token 可被服务端注销。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/auth/controller/AuthController.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/auth/service/LoginService.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/security/jwt/JwtAuthenticationFilter.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/security/jwt/JwtTokenProvider.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/config/WebSocketConfig.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/entity/RevokedToken.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/security/repository/RevokedTokenRepository.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/security/service/TokenRevocationService.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/resources/db/migration/V7__revoked_tokens.sql`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/test/java/com/xsh/trueused/security/jwt/JwtAuthenticationFilterTest.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/test/java/com/xsh/trueused/auth/service/LoginServiceTest.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/api/auth.js`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  补充过滤器与登录服务的单元测试；尝试执行 `./mvnw -Dtest=JwtAuthenticationFilterTest,LoginServiceTest test`；同时静态检查差异与关键文件逻辑。
- 结果：
  代码已完成加固与测试补充；当前环境缺少 Java Runtime，导致 Maven 测试未能实际执行，其余静态检查通过。
- 后续建议：
  在具备 JDK 的环境执行迁移与测试；后续可增加过期撤销记录清理任务，并考虑将撤销查询迁移到 Redis 以降低数据库热路径压力。

## 2026-03-17 更新前后端说明文档

- 问题描述：
  前后端 README 与实现记录没有覆盖本轮登录鉴权、首页改版、验货报告页调整和测试数据开关等最近改动，文档与代码现状存在偏差。
- 根因分析：
  本轮开发先完成了代码更新，但对应的仓库说明文件没有同步维护，导致项目亮点、配置说明和页面实现记录仍停留在旧版本。
- 解决方法：
  更新后端 README，补充令牌撤销、刷新轮换、WebSocket 鉴权与测试数据 Seeder 说明；更新前端 README 与 Implementation 文档，补充首页、验货报告页和登出联动的当前实现状态。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/README.md`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/README.md`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/Implementation.md`
- 验证方式：
  人工核对 README/Implementation 中的模块说明、配置项和页面描述是否与当前工作区代码改动一致，再检查 git diff 确认仅修改目标文档。
- 结果：
  前后端说明文档已与当前本地实现基本对齐，便于后续直接提交并同步到 GitHub。
- 后续建议：
  后续每一轮功能合并时同步更新 README 里的“当前已实现模块”和“遗留问题”，避免再次出现文档滞后。

## 2026-03-18 吃透支付流程

- 问题描述：
  需要梳理 TrueUsed 当前从下单、收银台支付、支付宝回调、钱包冻结、发货、确认收货到退款/取消的完整支付链路，并明确前后端代码入口与关键状态流转。
- 根因分析：
  支付相关逻辑分散在结算页、收银台、订单服务、支付服务、钱包服务和定时任务中；同时支付宝与钱包两条链路共用订单状态机，但资金处理方式并不完全一致，单看单个文件很难形成全局认知。
- 解决方法：
  逐一核对 `Settlement.vue`、`Payment.vue`、`PaymentSuccess.vue`、订单与钱包 API、`OrderCommandService`、支付策略、`AlipayController`、`AlipayService`、`WalletService`、状态机与定时任务，确认真实调用链、状态变化、异步回调位置与退款结算分支，并记录当前实现中的关键风险点。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  通过静态代码走查交叉验证前端路由、API 调用、后端控制器、服务层、状态机和定时任务，确认每个支付分支都能在代码中闭环定位。
- 结果：
  已确认系统存在两条主支付链路：支付宝通过 `/api/alipay/pay` 发起、由 `/api/alipay/notify` 异步落单；钱包支付通过 `/api/orders/{id}/pay-wallet` 立即冻结资金并更新订单。订单在确认收货或自动确认后才给卖家入账，取消/退款则退回买家钱包；同时识别出支付宝金额校验与订单查询权限两个高风险薄弱点。
- 后续建议：
  优先补齐支付宝回调中的订单归属与金额核验，明确外部支付与内部钱包台账的一致性策略，并为订单详情查询增加买卖双方权限校验。

## 2026-03-18 详细讲解支付宝支付分支

- 问题描述：
  需要把支付宝分支按前端发起支付、后端生成表单、浏览器跳转支付宝、异步回调更新订单、前端回跳页轮询确认的顺序讲清楚，并明确每段代码的真实职责。
- 根因分析：
  当前实现把“下单”“拉起支付宝”“支付结果确认”拆散在多个页面和服务里，容易误以为 `/api/alipay/pay` 就已经完成了支付落账，或误把回跳页当成最终支付确认点。
- 解决方法：
  重新核对 `Payment.vue`、`payments.js`、`AlipayController`、`AlipayService`、`OrderCommandService`、`CallbackOrderPaymentStrategy`、`PaymentSuccess.vue`、`SecurityConfig` 与支付宝配置，按请求流和状态流拆解职责边界与异步时序。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  通过静态代码走查核对前端按钮动作、后端接口、支付宝回调放行配置、回跳页轮询逻辑和订单状态更新代码是否首尾相连。
- 结果：
  已确认订单在进入收银台前就已创建；`/api/alipay/pay` 只生成并返回自动提交的支付宝表单，不改订单状态；真正的支付确认来自 `/api/alipay/notify` 的签名校验与回调处理，前端 `/payment/success` 页面只负责轮询订单状态并展示结果。
- 后续建议：
  后续若继续完善支付宝链路，建议补上支付前订单归属与金额校验、回调业务字段校验，以及“支付发起日志”和“回调处理日志”的统一追踪字段。

## 2026-05-14 管理端前端接入

- 问题描述：
  当前后端已经存在 `/api/admin/**` 管理接口与 `ADMIN` 权限控制，但前端缺少管理员专属操作界面，导致用户管理和提现审核只能通过接口调试完成。
- 根因分析：
  项目此前重点放在买家端与卖家工作台闭环，后台能力虽已在 Spring Security 和控制器层落地，但前端路由、登录态角色识别和独立后台画布都没有接入。
- 解决方法：
  在 `TrueUsed-web` 内直接新增 `/admin` 独立控制台路由，补充基于 `roles` 的管理员守卫；在用户 store 中统一规范角色字段并暴露 `isAdmin`；新增 `admin.js` API 封装；实现 Linear Alpha 风格的暗色管理页，覆盖平台概览、用户管理、提现审核，并把入口挂到管理员头像菜单中。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/stores/user.js`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/router/index.js`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/components/TopNavbar.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/App.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/api/admin.js`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/views/AdminConsole.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  通过前端构建验证新增页面、路由守卫与 API 模块可正常编译；人工检查 `/admin` 权限入口、管理员菜单入口和暗色后台样式是否与既有主站隔离。
- 结果：
  已完成单项目内的管理员控制台接入，不需要新开前端工程；普通用户无法直接访问 `/admin`，管理员可在同一登录体系下进入后台执行用户状态管理和提现审核。
- 后续建议：
  下一步建议继续补后台的订单仲裁、举报处理、验货任务调度与运营看板接口，避免管理端停留在“只有面板、缺少平台操作闭环”的阶段。

## 2026-07-06 编写需求文档初稿

- 问题描述：
  需要为 TrueUsed 输出一版精简需求文档初稿，并包含不少于 5 条用例、不少于 2 个竞品和不少于 2 个技术候选方案。
- 根因分析：
  仓库已有 README、ROADMAP 和接口说明，但缺少面向产品验收的短版 PRD，竞品与技术取舍也没有集中沉淀。
- 解决方法：
  基于当前商品、交易、验货、物流、售后、后台模块现状，新增需求文档，补充功能范围、核心用例、竞品分析表、技术选型矩阵和验收指标。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/docs/requirements-draft.md`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  人工核对文档是否满足用例数量、竞品数量、候选方案数量和精简表达要求。
- 结果：
  已完成需求文档初稿，范围聚焦二手交易 MVP，未扩写到复杂生产化平台。
- 后续建议：
  后续可在确认真实课程或答辩要求后，再补页面原型、接口清单和测试用例明细。

## 2026-07-06 生成需求文档 Word 版

- 问题描述：
  Markdown 初稿不满足“完整 Word 文档”的交付形式，需要生成可直接打开和提交的 `.docx` 文件。
- 根因分析：
  需求初稿已有内容和表格，但缺少 Word 文件、正式页眉页脚、封面信息和渲染校验。
- 解决方法：
  基于现有需求初稿整理 Word 版，补充标题区、文档信息、页眉页脚和表格样式，并通过 DOCX 渲染生成 PDF/PNG 预览检查版式。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/output/doc/TrueUsed-需求文档初稿.docx`
  `/Users/xshsama/code/TrueUsed/output/render/requirements-draft-final/TrueUsed-需求文档初稿.pdf`
  `/Users/xshsama/code/TrueUsed/output/render/requirements-draft-final/page-1.png`
  `/Users/xshsama/code/TrueUsed/output/render/requirements-draft-final/page-2.png`
  `/Users/xshsama/code/TrueUsed/output/render/requirements-draft-final/page-3.png`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  使用 `render_docx.py` 将 DOCX 渲染为 PDF 和 3 页 PNG，逐页检查标题、表格、中文换行、页眉页脚和分页。
- 结果：
  Word 版需求文档已生成，最终渲染为 3 页，未发现表格裁切、文字重叠或页脚错位。
- 后续建议：
  后续若需要正式答辩版，可在该 Word 基础上补封面学校信息、成员信息和评审要求字段。

## 2026-07-06 RFC/CVE 安全加固与 PR 准备

- 问题描述：
  需要对 TrueUsed 仓库做 RFC 不符合项、CVE/依赖漏洞和高风险安全边界审计，并在确认 upstream 未被他人更新后直接修复、验证并准备 PR。
- 根因分析：
  WebSocket 私聊曾额外发布到可猜测的公开 `/topic/user/{id}`，且无效 STOMP CONNECT 只记录不拒绝；JWT 缺少 RFC 7519 的 issuer/audience/jti 绑定；生产默认 CORS、refresh cookie、错误信息和 SQL 日志不够收敛；前端 lockfile 存在 npm audit 报告的已知漏洞；支付前端直接执行后端返回 HTML；PDF 错误响应和文件名头部构造缺少防御性处理。
- 解决方法：
  拒绝无效 WebSocket CONNECT/SEND/SUBSCRIBE，限制公开 topic 并改用 `/user/queue/messages` 私聊队列；为 JWT 增加并校验 issuer/audience/jti；将 CORS、refresh cookie、错误信息和 SQL 日志改为安全默认和环境变量配置；升级 axios、vite、plugin-vue、unocss 及 lockfile；支付表单改为 DOMParser 惰性解析、HTTPS Alipay host allowlist 和隐藏字段重建提交；PDF 响应用 `ContentDisposition` 并避免返回内部异常详情；补充相关测试与环境示例。
- 修改文件：
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/config/WebSocketConfig.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/chat/controller/MessageController.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed-web/src/stores/message.js`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/security/jwt/JwtTokenProvider.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/test/java/com/xsh/trueused/security/jwt/JwtTokenProviderTest.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/security/config/SecurityConfig.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/auth/service/LoginService.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/java/com/xsh/trueused/inspection/controller/InspectionController.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/pom.xml`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/main/resources/application.properties`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/test/java/com/xsh/trueused/security/config/SecurityConfigTest.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/src/test/java/com/xsh/trueused/auth/service/LoginServiceTest.java`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed-web/src/api/payments.js`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed-web/package.json`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed-web/package-lock.json`
  `/Users/skyhua/Documents/2nd/True_Used/package-lock.json`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/.env.example`
  `/Users/skyhua/Documents/2nd/True_Used/docs/.env.example`
  `/Users/skyhua/Documents/2nd/True_Used/TrueUsed/docs/issue-log.md`
- 验证方式：
  已确认本地 HEAD 与 `origin/main` 均为 `032139d6df3c705bebf8e5c07306ce6eed819641` 后开始修改；完成 Codex Security deep scan 并生成 5 个 finding 报告；执行 `git diff --check`；执行根目录 `npm audit --omit=dev`；执行 `TrueUsed-web` 下 `npm ci`、`npm audit`、`npm run build`；执行 `TrueUsed` 下 `mvn test`；通过 OSV API 查询显式 Maven 依赖；检查本地 Java/Maven 运行时。
- 结果：
  前端 npm audit 与根目录生产依赖 audit 均为 0 vulnerabilities，Vite 构建通过但保留既有 `@apply` 和 `:deep(...)` CSS minify warning；OSV 对显式 Maven 依赖未返回漏洞；首次 `mvn test` 暴露 JDK 24 下 Lombok annotation processor `1.18.32` 的 `TypeTag UNKNOWN` 编译问题，已将 `maven-compiler-plugin` 的 Lombok processor 版本改为 `${lombok.version}`；随后修复 JWT `validateToken` 未复用 `parseAllClaims` 的 issuer/audience 校验遗漏，最终 `mvn test` 通过 50 个测试且 0 failures/errors。
- 后续建议：
  补充 WebSocket 集成测试和支付表单 allowlist 浏览器测试；生产部署时设置真实 `JWT_SECRET`、`SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS`、`SECURITY_REFRESH_COOKIE_SECURE=true` 与正式 Alipay/Cloudinary 配置。

## 2026-07-06 创建管理员账号

- 问题描述：
  需要在当前 TrueUsed 本地环境中开通一个可登录后台的管理员账号。
- 根因分析：
  当前数据库已有 `ROLE_USER` 与 `ROLE_ADMIN` 角色数据，前端也已经接入 `/admin` 后台路由，但现有用户查询结果中没有绑定 `ROLE_ADMIN` 的可用账号。
- 解决方法：
  通过 `/api/auth/register` 创建 `admin` 用户，确保密码使用后端 `BCryptPasswordEncoder` 生成；随后在 MySQL 中绑定 `ROLE_ADMIN`，并将账号昵称更新为“系统管理员”。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
  本地 MySQL `trueused.users`
  本地 MySQL `trueused.user_roles`
- 验证方式：
  登录接口 `/api/auth/login` 返回 `ROLE_USER, ROLE_ADMIN`；携带 JWT 访问 `/api/admin/users` 返回 HTTP 200。
- 结果：
  管理员账号 `admin` 已创建并启用，角色为 `ROLE_ADMIN, ROLE_USER`。
- 后续建议：
  本地联调结束后建议尽快修改默认密码，生产环境应改为一次性初始化脚本或受控运维命令，避免硬编码管理员凭据。

## 2026-07-06 同步远端配置修复提交

- 问题描述：
  本地存在配置修复改动，但远端 `origin/main` 已先推送 7 个提交，直接推送会被拒绝。
- 根因分析：
  本地分支落后远端安全加固与依赖升级提交，同时本地改动覆盖了前端 lockfile 和问题日志，rebase 时产生冲突。
- 解决方法：
  先提交本地改动，再执行 `git fetch origin` 与 `git rebase origin/main`；lockfile 冲突保留远端安全升级版本，日志冲突合并远端记录、本地记录和本次同步记录。
- 修改文件：
  `/Users/xshsama/code/TrueUsed/TrueUsed/.env.example`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/resources/application.properties`
  `/Users/xshsama/code/TrueUsed/TrueUsed/src/main/java/com/xsh/trueused/storage/service/CloudinaryService.java`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/components/ImageUpload.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/views/PostCreate.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed-web/src/views/ReviewCreate.vue`
  `/Users/xshsama/code/TrueUsed/TrueUsed/docs/issue-log.md`
- 验证方式：
  检查 rebase 冲突标记、运行 `git diff --check`，并确认最终分支只比 `origin/main` 多一个配置修复提交。
- 结果：
  本地配置修复提交已重放到最新 `origin/main` 之后，可推送到远端。
- 后续建议：
  后续多人协作前先执行 `git fetch` 并查看 `main...origin/main` 差异，减少 lockfile 与文档冲突。
