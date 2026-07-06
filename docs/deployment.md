# TrueUsed Deployment

本文档定义本地开发、Docker 联调和交付前部署口径。

## 环境要求

本地直接运行：

| 组件 | 版本 |
| --- | --- |
| JDK | 21+ |
| Maven | 使用后端自带 `./mvnw` |
| Node.js | 18+ |
| npm | 9+ |
| MySQL | 8+ |
| Redis | 6+ |

Docker 运行：

| 组件 | 版本 |
| --- | --- |
| Docker | 24+ |
| Docker Compose | v2+ |

## 环境变量

后端参考：

```text
TrueUsed/.env.example
```

前端参考：

```text
TrueUsed-web/.env.example
```

关键变量：

| 变量 | 说明 |
| --- | --- |
| `SERVER_PORT` | 后端端口，默认 `8081` |
| `SPRING_DATASOURCE_URL` | MySQL JDBC 地址 |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名 |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 |
| `SPRING_REDIS_HOST` | Redis host |
| `SPRING_REDIS_PORT` | Redis port |
| `JWT_SECRET` | JWT 签名密钥 |
| `CLOUDINARY_*` | Cloudinary 配置 |
| `ALIPAY_*` | 支付宝沙箱配置 |
| `APP_TEST_DATA_ENABLED` | 是否启动测试数据 Seeder |

生产环境禁止使用 `change-me`、`demo` 或示例密钥。

## 本地开发启动

后端：

```bash
cd TrueUsed
./mvnw spring-boot:run
```

前端：

```bash
cd TrueUsed-web
npm install
npm run dev
```

默认地址：

| 服务 | 地址 |
| --- | --- |
| 前端 | `http://localhost:5173` |
| 后端 | `http://localhost:8081` |
| 健康检查 | `http://localhost:8081/actuator/health` |

## Docker Compose 启动

在仓库根目录执行：

```bash
docker compose -f ops/docker/docker-compose.yml up -d --build
```

访问地址：

| 服务 | 地址 |
| --- | --- |
| 前端 | `http://localhost` |
| 后端 | `http://localhost:8081` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

Grafana 默认账号由环境变量控制：

```text
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

## 数据库迁移

后端启动时 Flyway 自动执行迁移：

```text
TrueUsed/src/main/resources/db/migration
```

手动迁移可使用：

```bash
cd TrueUsed
./mvnw flyway:migrate
```

需要提供：

```text
FLYWAY_URL
FLYWAY_USER
FLYWAY_PASSWORD
```

## 构建

后端：

```bash
cd TrueUsed
./mvnw clean package
```

前端：

```bash
cd TrueUsed-web
npm run build
```

## 压测

启动基础服务后运行 k6 profile：

```bash
docker compose -f ops/docker/docker-compose.yml --profile loadtest run --rm k6
```

默认阈值：

| 指标 | 阈值 |
| --- | --- |
| `http_req_failed` | `< 5%` |
| `http_req_duration p95` | `< 800ms` |
| `http_req_duration p99` | `< 1500ms` |
| `checks` | `> 95%` |

## 停止与清理

停止服务：

```bash
docker compose -f ops/docker/docker-compose.yml down
```

停止并删除数据卷：

```bash
docker compose -f ops/docker/docker-compose.yml down -v
```

删除数据卷会清空 MySQL、Redis、Prometheus 和 Grafana 数据，仅用于重置本地环境。
