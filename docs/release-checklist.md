# TrueUsed Release Checklist

每次交付前按此清单执行。没有执行的项必须在发布说明里明确标注原因。

## P0 基线

- [ ] 根目录 `ROADMAP.md` 存在，并与 `TrueUsed/ROADMAP.md`、`TrueUsed/TODO.md` 指向一致。
- [ ] 外层 `README.md` 不再出现“API 文档待补充”“部署文档待补充”的过期描述。
- [ ] `docs/api.md` 覆盖当前主要接口与标准响应。
- [ ] `docs/deployment.md` 覆盖本地、Docker、迁移、构建和压测入口。
- [ ] 后端 Java 版本为 21。
- [ ] 后端 Dockerfile 使用 Java 21 构建与运行镜像。

## 本地验证

- [ ] `cd TrueUsed && ./mvnw test`
- [ ] `cd TrueUsed-web && npm run build`
- [ ] `docker compose -f ops/docker/docker-compose.yml up -d --build`
- [ ] `curl http://localhost:8081/actuator/health` 返回 `UP`
- [ ] Prometheus target `trueused-backend` 为 `UP`
- [ ] Grafana 能打开 `TrueUsed JVM Overview`

## 业务冒烟

- [ ] 注册卖家和买家。
- [ ] 卖家发布 `FREE_TRADING` 商品。
- [ ] 买家创建订单。
- [ ] 买家使用钱包支付。
- [ ] 卖家发货。
- [ ] 订单详情展示物流轨迹。
- [ ] 买家确认收货。
- [ ] 买家发起退款申请。
- [ ] 卖家审批退款。

## 安全检查

- [ ] 生产环境未使用 `change-me`、`demo` 或示例密钥。
- [ ] 需要登录的接口匿名访问返回 401。
- [ ] 非资源归属用户访问订单、地址、商品编辑接口返回 403。
- [ ] 管理接口只有管理员角色可访问。
- [ ] 登出后旧 token 无法继续访问受保护接口。

## 数据与迁移

- [ ] Flyway 迁移在空库可执行。
- [ ] Flyway 迁移在已有库可验证。
- [ ] `spring.jpa.hibernate.ddl-auto` 不是生产自动建表模式。
- [ ] 测试数据 Seeder 默认关闭。

## 可观测性

- [ ] Actuator health 可访问。
- [ ] Prometheus 指标可抓取。
- [ ] JVM、HTTP、HikariCP 基础面板正常。
- [ ] k6 业务链路压测达到阈值。

## 发布说明

- [ ] 列出本次变更模块。
- [ ] 列出已执行测试。
- [ ] 列出未执行测试及原因。
- [ ] 列出已知风险。
- [ ] 列出下一步优先级。
