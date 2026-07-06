# TrueUsed Observability

## 目标

先建立最小可运行的监控闭环：

```text
Spring Boot Actuator -> Prometheus -> Grafana
```

当前阶段只看指标，不做链路追踪和 JVM 调参。

## 启动

在仓库根目录执行：

```bash
docker compose -f ops/docker/docker-compose.yml up -d --build
```

访问地址：

```text
Backend Health: http://localhost:8081/actuator/health
Backend Metrics: http://localhost:8081/actuator/prometheus
Prometheus:     http://localhost:9090
Grafana:        http://localhost:3000
```

Grafana 默认账号：

```text
admin / admin
```

Grafana 默认界面语言已设置为简体中文：

```text
GF_USERS_DEFAULT_LANGUAGE=zh-Hans
```

如果已有账号仍显示英文，说明该账号的个人偏好覆盖了服务器默认语言。进入右上角用户菜单，将个人 Language 改为中文即可。

如需修改，设置根目录 `.env`：

```text
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your-password
```

## 验证 Prometheus

进入 Prometheus 页面，打开 `Status -> Targets`，确认 `trueused-backend` 为 `UP`。

可在查询框输入：

```promql
up{job="trueused-backend"}
```

返回 `1` 表示采集正常。

进入 `Alerts` 页面可以查看预置业务告警规则。当前规则覆盖：

```text
1. 后端采集不可用
2. 订单命令平均耗时持续高于 2s
3. 订单命令错误在 10 分钟内超过 5 次
4. 最老待付款订单超过 30 分钟
5. 最老待发货订单超过 24 小时
6. 支付回调错误在 10 分钟内超过 3 次
```

## Grafana Dashboard

Grafana 已自动配置 Prometheus 数据源、JVM Dashboard 和业务 Dashboard：

```text
Dashboards -> TrueUsed -> TrueUsed JVM Overview
Dashboards -> TrueUsed -> TrueUsed Business Overview
```

当前看板覆盖：

```text
Backend target UP / DOWN
Process uptime
HTTP RPS
HTTP request latency avg / max
JVM heap / nonheap memory
JVM GC pause max / avg
JVM live threads
HikariCP active / idle connections
```

当前阶段重点不是看“漂亮图”，而是建立判断习惯：

```text
1. Backend Target 长期为 UP
2. Heap 使用量不要只涨不降
3. GC Pause 不要持续抬高
4. HTTP Latency 出现尖刺时，对照 HikariCP pending / active
5. HikariCP pending 长期大于 0，通常说明数据库连接池或 SQL 成为瓶颈
```

业务面板重点看：

```text
1. 下单 / 支付吞吐是否持续
2. 待付款、待发货是否堆积
3. 近 24h GMV 是否断崖式下降
4. 最老待处理订单年龄是否持续变大
5. 支付与发货通道是否出现单边失速
```

注意：没有真实业务流量时，HTTP 面板主要显示 `/actuator/*` 请求，这是正常的。

## 业务指标

后端通过 Micrometer 暴露订单链路指标，Prometheus 会将 `.` 转换为 `_`：

| 代码指标名 | Prometheus 指标名 | 类型 | 关键标签 |
| --- | --- | --- | --- |
| `trueused.order.created` | `trueused_order_created_total` | Counter | `trade_model`, `result`, `reason` |
| `trueused.order.created.amount` | `trueused_order_created_amount_sum/count` | Summary | `trade_model` |
| `trueused.order.payment` | `trueused_order_payment_total` | Counter | `channel`, `result` |
| `trueused.order.payment.amount` | `trueused_order_payment_amount_sum/count` | Summary | `channel` |
| `trueused.payment.callback` | `trueused_payment_callback_total` | Counter | `result` |
| `trueused.order.shipment` | `trueused_order_shipment_total` | Counter | `fulfillment`, `result` |
| `trueused.order.transition` | `trueused_order_transition_total` | Counter | `operation`, `from_status`, `to_status`, `result`, `reason` |
| `trueused.order.scheduled.job` | `trueused_order_scheduled_job_total` | Counter | `job`, `result` |
| `trueused.order.scheduled.job.affected` | `trueused_order_scheduled_job_affected_sum/count` | Summary | `job`, `result` |
| `trueused.orders.total` | `trueused_orders_total` | Gauge | `status` |
| `trueused.products.total` | `trueused_products_total` | Gauge | `status` |
| `trueused.orders.pending.payment.oldest.age` | `trueused_orders_pending_payment_oldest_age_seconds` | Gauge | - |
| `trueused.orders.pending.shipment.oldest.age` | `trueused_orders_pending_shipment_oldest_age_seconds` | Gauge | - |

核心查询：

```promql
sum(rate(trueused_order_created_total{result="success"}[5m]))
sum(rate(trueused_order_payment_total{result="processed"}[5m]))
sum(increase(trueused_order_payment_amount_sum[24h]))
sum by (status) (trueused_orders_total)
sum by (operation, result) (rate(trueused_order_transition_total[5m]))
sum by (job, result) (increase(trueused_order_scheduled_job_total[1h]))
max(trueused_orders_pending_payment_oldest_age_seconds)
max(trueused_orders_pending_shipment_oldest_age_seconds)
```

## 当前阶段判断标准

先不要急着调优。第一阶段只确认：

```text
1. 后端能暴露 Prometheus 指标
2. Prometheus 能抓到 backend:8081
3. Grafana 能读取 Prometheus 数据源
4. 能看到 JVM / HTTP / HikariCP 基础指标
```
