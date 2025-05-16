# kafka-console

该工程是一个 Kafka 运维平台，主要对 Kafka 2.8.2 功能进行基本的运维操作。平台分为前后端两部分，分别提供后台服务接口和前端管理界面。

## 项目结构

```
kafka-console/
├── kafka-console-backend/   # 后端服务
│   ├── src/                 # 后端源码
│   │   ├── main/            # 主代码目录
│   │   └── test/            # 测试代码目录
│   ├── Dockerfile           # 后端 Docker 配置
│   ├── Makefile             # 构建和编译脚本
│   ├── pom.xml              # Maven 配置文件
│   └── logs/                # 日志文件目录
├── kafka-console-frontend/  # 前端服务
│   ├── src/                 # 前端源码
│   ├── public/              # 静态资源
│   ├── package.json         # 前端依赖配置
│   └── vue.config.js        # Vue 配置文件
└── README.md                # 项目说明文件
```

## 功能模块

### 后端服务（kafka-console-backend）
- **Topic 管理**：
  - 创建、删除、导入、导出 Topic。
  - 获取 Topic 列表及详情。
  - 更新 Topic 属性配置。
- **消息管理**：
  - 按时间或位移查询消息。
  - 发送消息到指定 Topic。
- **ACL 管理**：
  - 创建、更新或删除 ACL 用户。
  - 获取 ACL 用户列表。
- **Broker 管理**：
  - 查看 Broker 配置。
  - 更新 Broker 配置。
  - 下载 TLS 文件。
- **消费组管理**：
  - 重置消费组的位移。
  - 检查消费组状态。

### 前端服务（kafka-console-frontend）
- 提供基于 Vue.js 的用户界面，用于操作 Kafka 集群。
- 支持实时查看 Topic、消息、ACL 用户和 Broker 的状态。
- 提供直观的表格和图表展示 Kafka 集群信息。

## 快速开始

### 后端服务

1. **构建后端服务**
   ```bash
   cd kafka-console-backend
   make buildx
   ```

2. **运行后端服务**
   使用 Docker 或直接运行 Spring Boot 应用。

3. **访问 Swagger 文档**
   后端服务启动后，可通过 `/swagger-ui.html` 访问 API 文档。

### 前端服务

1. **安装依赖**
   ```bash
   cd kafka-console-frontend
   npm install
   ```

2. **运行开发环境**
   ```bash
   npm run serve
   ```

3. **构建生产环境**
   ```bash
   npm run build
   ```

## 技术栈

- **后端**：
  - Spring Boot
  - Kafka Client
  - Swagger
- **前端**：
  - Vue.js
  - Axios
  - Element UI

## 贡献

欢迎提交 Issue 或 Pull Request 来改进该项目。

## 许可证

本项目采用 MIT 许可证。
```