# TrueUsed 项目

## 项目结构

```
trueused/
├── TrueUsed/              # 后端项目（Java/Maven）
├── TrueUsed-web/          # 前端项目
├── docs/                  # 项目文档
│   ├── resumes/          # 简历相关文件
│   └── .vscode/          # VSCode 配置
├── ops/                   # 运维相关
│   ├── docker/           # Docker 配置
│   ├── grafana/          # 监控配置
│   └── prometheus/       # 监控配置
├── .cloudbase/            # 云开发配置
├── .gitignore            # Git 忽略规则
├── package.json          # Node.js 依赖配置
└── package-lock.json     # 依赖锁定文件
```

## 快速开始

### 后端（TrueUsed）
```bash
cd TrueUsed
./mvnw spring-boot:run
```

### 前端（TrueUsed-web）
```bash
cd TrueUsed-web
npm install
npm run dev
```

### Docker 部署
```bash
cd ops/docker
docker compose up -d --build
```

## 环境配置

1. 复制环境变量示例文件：
   ```bash
   cp TrueUsed/.env.example .env
   ```

2. 根据需要修改 `.env` 文件中的配置

## 文档

- 项目路线图：[ROADMAP.md](./ROADMAP.md)
- API 口径：[docs/api.md](./docs/api.md)
- 部署说明：[docs/deployment.md](./docs/deployment.md)
- 发布检查：[docs/release-checklist.md](./docs/release-checklist.md)
- 监控说明：[docs/observability.md](./docs/observability.md)

## 注意事项

- `node_modules/` 和 `target/` 目录已被添加到 `.gitignore`
- 不要将敏感信息（如 `.env` 文件）提交到版本控制
- 简历等个人文件已移动到 `docs/resumes/`

## 维护

定期清理：
- 构建产物（`target/`, `dist/`, `build/`）
- 依赖目录（`node_modules/`）
- 临时文件（`*.tmp`, `*.log`）
