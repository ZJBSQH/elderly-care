# 🏥 老年护理云平台 (Elderly Care Cloud Platform)

> **Spring Cloud 微服务全栈项目** | Java 17 · Spring Boot 3.5 · uni-app 跨平台前端

一个面向老年人的智慧健康管理平台，涵盖用药提醒、健康监测、AI 健康助手、资讯推送和家属远程关怀等功能。采用微服务架构，前后端分离，支持 H5、微信小程序、App 多端部署。

---

## 📋 目录

- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [模块说明](#-模块说明)
- [功能亮点](#-功能亮点)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [API 接口](#-api-接口)
- [数据库设计](#-数据库设计)
- [安全机制](#-安全机制)
- [开发规范](#-开发规范)

---

## 🛠 技术栈

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.5.11 |
| 微服务 | Spring Cloud | 2025.0.2 |
| 微服务治理 | Spring Cloud Alibaba | 2025.0.0.0 |
| 网关 | Spring Cloud Gateway | - |
| 认证授权 | Spring Security + JWT | - |
| ORM | MyBatis-Plus | 3.5+ |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.0 |
| 注册中心 | Nacos | 3.2.1 |
| 定时任务 | Spring @Scheduled | - |
| 实时推送 | WebSocket | - |
| AI 流式响应 | Spring WebFlux + SSE | - |
| 服务调用 | OpenFeign + LoadBalancer | - |
| 构建工具 | Maven | - |
| 容器化 | Docker Compose | - |

### 前端 (elder_care/)

| 类别 | 技术 |
|------|------|
| 框架 | uni-app (Vue 3) |
| 状态管理 | Pinia |
| 编译目标 | H5 / 微信小程序 / Android / iOS |
| HTTP 客户端 | uni.request (封装拦截器) |
| 实时通信 | WebSocket |

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (uni-app)                         │
│            H5 / 微信小程序 / Android / iOS                │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP / WebSocket
                      ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Cloud Gateway (:8080)                 │
│          JWT 解析 → X-User-Id / X-User-Type              │
└──────┬────────┬────────┬────────┬────────┬────────┬──────┘
       │        │        │        │        │        │
       ▼        ▼        ▼        ▼        ▼        ▼
┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
│  Auth    │  User    │  Health  │ Medicine │  Remind  │   News   │  Admin   │
│ :8081    │ :8082    │ :8083    │ :8084    │ :8085    │ :8086    │ :8088    │
└────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┴────┬─────┘
     │          │          │          │          │          │          │
     ▼          ▼          ▼          ▼          ▼          ▼          ▼
┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
│  db_auth │ db_user  │db_health │db_medi.. │db_remind │ db_news  │db_admin  │
└──────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘
                              ▲
                              │ AI (:8087)
                              │ SSE 流式响应
                              │ Feign 聚合调用
```

### 公共模块

```
elderly-common/
├── common-core       # Result 统一响应 / 全局异常处理 / BeanUtil
├── common-security   # JWT 工具 / Security 过滤器 / SecurityUtil
├── common-redis      # Redis 配置 / 缓存工具类
└── common-mybatis    # MyBatis-Plus 配置 / 自动填充
```

---

## 📦 模块说明

### 业务微服务 (7 个)

| 服务 | 端口 | 数据库 | 核心功能 |
|------|------|--------|----------|
| **elderly-gateway** | 8080 | - | API 网关、JWT 全局鉴权、路由转发、请求头注入 |
| **elderly-auth** | 8081 | db_elderly_auth | 注册/登录/修改密码、BCrypt 加密、JWT 签发、三种角色(老人/家属/管理员) |
| **elderly-user** | 8082 | db_elderly_user | 老人档案(病史/过敏/紧急联系人)、家属绑定、二维码生成、跨服务权限验证 |
| **elderly-health** | 8083 | db_elderly_health | 健康数据录入(血压/血糖/心率/体重)、趋势图表、异常预警标识 |
| **elderly-medicine** | 8084 | db_elderly_medicine | 用药计划管理、服药记录打卡、用药统计分析 |
| **elderly-remind** | 8085 | db_elderly_remind | 提醒设置、定时任务调度、WebSocket 实时推送通知 |
| **elderly-news** | 8086 | db_elderly_news | 健康知识管理、收藏/点赞、Redis 缓存加速 |
| **elderly-admin** | 8088 | db_elderly_admin | 系统配置、疾病字典、用户管理、内容发布 |

### AI 服务 (1 个)

| 服务 | 端口 | 数据库 | 核心功能 |
|------|------|--------|----------|
| **elderly-ai** | 8087 | - | AI 健康助手(WebFlux SSE 流式输出)、RAG 增强检索生成、Feign 聚合用药/提醒数据 |

---

## ✨ 功能亮点

### 1. 三种角色权限体系
- **老人端**：查看健康数据、用药提醒、AI 健康咨询、健康资讯
- **家属端**：绑定老人、远程查看健康/用药、接收异常预警
- **管理员端**：系统配置、用户管理、内容发布、数据字典维护

### 2. AI 智能健康助手
- 基于 **SSE (Server-Sent Events)** 的流式对话，逐字输出体验
- **RAG 增强检索生成**：结合知识库提供精准健康建议
- Feign 聚合多服务数据，AI 可获取用户用药、提醒等上下文

### 3. 用药管理与智能提醒
- 用药计划创建与管理
- 每日服药打卡记录
- `@Scheduled` 定时任务 + **WebSocket** 实时推送服药提醒
- 用药统计分析

### 4. 家属远程关怀
- 家属扫码绑定老人
- 远程查看健康数据趋势
- 查看用药执行情况
- 接收异常健康预警

### 5. 微服务治理
- **Nacos** 服务注册与发现
- **Spring Cloud Gateway** 统一网关路由
- **OpenFeign** 声明式服务调用
- **Spring Security + JWT** 无状态认证
- 各服务独立数据库，软引用关联(无跨库外键)

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 16+ (前端开发)

### 1. 克隆项目

```bash
git clone https://github.com/ZJBSQH/elderly-care.git
cd elderly-care
```

### 2. 启动基础设施

```bash
# 启动 MySQL、Redis、Nacos
docker-compose up -d

# 数据库表结构会在容器启动时自动导入 (init-sql/)
# Nacos 控制台: http://localhost:8080/
```

### 3. 启动后端微服务

```bash
# 编译整个项目
./mvnw clean install -DskipTests

# 按以下顺序启动各服务（或使用 IDE 一键启动）
# 1. elderly-gateway    (8080)
# 2. elderly-auth       (8081)
# 3. elderly-user       (8082)
# 4. elderly-health     (8083)
# 5. elderly-medicine   (8084)
# 6. elderly-remind     (8085)
# 7. elderly-news       (8086)
# 8. elderly-ai         (8087)
# 9. elderly-admin      (8088)
```

### 4. 启动前端 (可选)

```bash
cd elder_care
npm install
npm run dev:h5        # H5 开发模式
# npm run dev:mp-weixin  # 微信小程序
```

### 5. 测试接口

```bash
# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456","role":0}'

# 登录 (获取 Token)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456"}'
```

---

## 📁 项目结构

```
elderly_care_cloud/
├── elderly-common/              # 公共模块
│   ├── common-core/             # 核心工具类、统一响应、异常处理
│   ├── common-security/         # JWT、Spring Security 配置
│   ├── common-redis/            # Redis 配置与工具
│   └── common-mybatis/          # MyBatis-Plus 配置
├── elderly-gateway/             # API 网关 (8080)
├── elderly-auth/                # 认证服务 (8081)
├── elderly-user/                # 用户服务 (8082)
├── elderly-health/              # 健康服务 (8083)
├── elderly-medicine/            # 用药服务 (8084)
├── elderly-remind/              # 提醒服务 (8085)
├── elderly-news/                # 资讯服务 (8086)
├── elderly-ai/                  # AI 服务 (8087)
├── elderly-admin/               # 管理服务 (8088)
├── elder_care/                  # uni-app 跨平台前端
│   ├── api/                     # 接口层
│   ├── pages/                   # 页面
│   ├── store/                   # Pinia 状态管理
│   ├── utils/                   # 工具函数
│   └── composables/             # 组合式函数
├── init-sql/                    # 数据库初始化脚本 (11 个 SQL 文件)
├── docs/                        # 文档
├── docker-compose.yml           # 容器编排
├── pom.xml                      # 父 POM
└── README.md
```

---

## 🔌 API 接口

### 认证服务 (auth)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 用户注册 |
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/sms/send` | 发送短信验证码 |
| GET | `/auth/user/{id}` | 查询用户信息 |

### 健康服务 (health)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/health` | 添加健康记录 |
| GET | `/health/list` | 健康记录列表 |
| GET | `/health/trend` | 健康趋势数据 |
| GET | `/health/latest` | 最新健康数据 |

### 用药服务 (medicine)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/medicine` | 创建用药计划 |
| GET | `/medicine/list` | 用药计划列表 |
| POST | `/medicine/record` | 服药打卡 |
| GET | `/medicine/statistics` | 用药统计 |

### AI 服务 (ai)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat` | AI 对话 (SSE 流式) |
| POST | `/ai/rag/chat` | RAG 增强对话 |

### 提醒服务 (remind)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/remind/setting` | 创建提醒设置 |
| GET | `/remind/notifications` | 通知列表 |
| WS | `/remind/ws` | WebSocket 实时推送 |

---

## 🗄 数据库设计

每个微服务独立数据库，共 7 个库：

| 数据库 | 核心表 | 说明 |
|--------|--------|------|
| db_elderly_auth | user | 用户账号、密码(BCrypt)、角色(0-老人/1-家属/2-管理员) |
| db_elderly_user | elder, family | 老人健康档案、家属绑定关系 |
| db_elderly_health | health | 血压/血糖/心率/体重/预警标记 |
| db_elderly_medicine | medicine, record | 用药计划、服药历史记录 |
| db_elderly_remind | remind, remind_task, notification | 提醒设置、定时任务、推送通知 |
| db_elderly_news | news, news_collect, news_like | 健康资讯、收藏、点赞 |
| db_elderly_admin | system_config, disease | 系统配置、疾病字典 |

> **设计原则**：跨服务使用 ID 软引用，不使用跨库外键，确保服务独立部署和扩展。

---

## 🔒 安全机制

1. **JWT 无状态认证**：登录后签发 Token，每次请求携带 Bearer Token
2. **Gateway 全局鉴权**：`AuthGlobalFilter` 解析 JWT 并注入 `X-User-Id`、`X-User-Type` 请求头
3. **密码加密**：BCrypt 加盐哈希存储
4. **微服务安全**：每个服务通过 `BaseSecurityConfig` 实现独立安全配置，自定义公开端点
5. **Feign Token 传递**：`FeignTokenRelayConfig` 自动在服务间调用时传递认证信息
6. **前端拦截器**：自动注入 Token，401/403 清除 Token 并重定向登录页
7. **统一错误码**：各服务独立错误码范围，便于定位问题

---

## 📐 开发规范

- **注释语言**：全部使用中文
- **Lombok**：`@Data` / `@NoArgsConstructor` / `@AllArgsConstructor` 替代手写 getter/setter
- **分层架构**：Controller → Service(接口) → ServiceImpl → Mapper → DB
- **类型后缀**（阿里巴巴规范）：`*DTO` / `*DO` / `*BO` / `*VO` / `*Query`
- **日志**：`@Slf4j` 统一日志，禁止 `System.out.println`
- **参数校验**：`@Valid` + Jakarta 注解
- **事务**：`@Transactional` 仅用于 Service 层方法，避免在循环中使用
- **SQL 安全**：禁止手动拼接 SQL 字符串
- **设计原则**：SOLID、DRY、KISS、YAGNI、OWASP Top 10

---

## 📊 项目统计

- **Maven 模块**：14 个（4 公共 + 1 网关 + 7 业务 + 1 AI + 1 父 POM）
- **Java 源文件**：137+
- **SQL 初始化脚本**：11 个
- **前端页面**：20+ 个
- **API 接口**：50+ 个

---

## 👤 作者

**郑** — Java 后端开发

---

## 📄 License

本项目仅用于学习与展示，请勿用于生产环境。
