# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## User preferences

- 所有回复,代码注释,说明文档必须使用中文
- 代码必须遵循本文件中的规范，
  有使用中间件知识如redis,jwt等
 则将用到的方法和注解记录在`docs/KNOWLEDGE_BASE.md` 中并分类

 #要求越苏:
 1,语法无错误,可直接编译运行,杜绝荣誉废弃代码;
 2,逻辑简洁不冗余,不过都设计,适配10000行以内的代码,或者说适配小项目代码
 3,只修改问题部分,不大面积推翻重写原有可用代码;
 4,给出代码是同时附带简短修改说明,标记改动点;
 5,控制代码篇幅,精简写法,减少无效token消耗

## 代码检查规范

- 在检查代码时，如果发现错误，**先不要修改代码**。
- 必须指明错误所在的：
    - 文件路径（如 `src/main/java/.../LikeService.java`）
    - 类名
    - 方法名
    - 代码行范围（如 `第45-47行`）
- 对错误行用 `[ERROR]` 前缀标注，并说明错误原因和修改建议。
- 所有检查结果统一追加到 `docs/ERROR_CODE.md` 中，格式如下：

### 错误报告格式
- **文件**: xxx
- **类**: xxx
- **方法**: xxx
- **行号**: xxx
- **错误原因**: xxx
- **修改建议**: xxx


 ## 前端技术栈
- **框架**: UniApp (Vue3组合式API)
- **UI组件库**:
  - 移动端:  uni-ui，专注适老化设计
  - 后台管理: Element Plus
- **图表库**: ECharts (通过 echarts-for-uniapp 或原生引入)
- **状态管理**: Pinia
- **网络请求**: uni.request 统一封装，与后端 Result<T> 对接
- **样式**: SCSS + 响应式布局


## Build & run

```bash
# Build
./mvnw compile

# Run (port 8080)
./mvnw spring-boot:run

# Run all tests (currently only a contextLoads smoke test)
./mvnw test

# Package
./mvnw package -DskipTests
```

Database: MySQL 8 at `localhost:3306/elderly_care`. Import `elderly_care.sql` before first run.

## Architecture

Spring Boot 3.5.11, Java 17, MyBatis-Plus 3.5.9. Stateless JWT auth with Spring Security.

### Package structure

| Package | Purpose |
|---|---|
| `controller/user/` | Public auth endpoints (`/auth/**`) |
| `controller/funtion/` | Core business endpoints — medicine, health, remind, record |
| `controller/admin/` | Admin management endpoints |
| `controller/test/` | Test-only controllers (e.g., push triggers) |
| `service/` | Service interfaces |
| `service/impl/` | Service implementations (business logic lives here) |
| `mapper/` | MyBatis-Plus mappers |
| `pojo/entity/` | DB entity classes |
| `pojo/dto/` | Request DTOs with `@Valid` constraints |
| `pojo/vo/` | Response view objects |
| `common/config/` | SecurityConfig, WebSocketConfig, AIConfig |
| `common/exception/` | BusinessException + GlobalExceptionHandler (`@RestControllerAdvice`) |
| `common/util/` | JwtUtil, SecurityUtil, QRCodeUtil |
| `common/websocket/` | `@ServerEndpoint` WebSocket (`NotifyWebSocket`) |
| `common/scheduler/` | Quartz scheduled tasks |
| `filter/` | JwtAuthenticationFilter, AdminAuthFilter |
| `ai/` | AI medicine notifier and context builder (DashScope) |

### Request flow

`Filter (JWT) → Controller → Service → Mapper → DB`

- Controllers are thin — they call services and return `Result<T>`.
- Services throw `BusinessException` for errors; `GlobalExceptionHandler` converts all exceptions to `Result.error()`.
- `SecurityUtil` (injectable component) reads the current user from `SecurityContextHolder` via phone lookup.

### Auth

- `/auth/**` is public; everything else requires a valid JWT.
- Token format: `Authorization: Bearer <token>`, valid 24h.
- JWT payload carries `userId` and `phone`.
- `JwtAuthenticationFilter` sets `SecurityContextHolder` but never blocks — expired/missing tokens just leave the context unauthenticated, and Spring Security's `.anyRequest().authenticated()` returns 403.

### AI integration

Spring AI Alibaba DashScope (`qwen-turbo` model). API key via env var `AI_DASHSCOPE_API_KEY`. Chat client bean defined in `AIConfig`. Used by `MedicineNotifier` and `MedicineContextBuilder` for medication reminders.

### WebSocket

`NotifyWebSocket` uses `@ServerEndpoint("/ws/notification/{userId}")`. The `ServerEndpointExporter` bean in `WebSocketConfig` enables it. The JWT filter skips `/ws/**` paths.

### Scheduler

`RemindTaskScheduler` uses Quartz (`@EnableScheduling` on `BackendApplication`) for periodic medication reminder tasks.

### Key conventions

- Unified response: `Result.success(data)` or `Result.error(code, message)`. Success code is `200`.
- Error codes in `ErrorCode` enum — use `BusinessException(ErrorCode.XXX)` when possible.
- Use `@Valid` on request bodies for validation; errors are caught by `GlobalExceptionHandler`.
- MyBatis-Plus XML mappers in `resources/com/elderlycare/mapper/*.xml`.
- Wrapper: use `./mvnw` (Maven wrapper), not system `mvn`.