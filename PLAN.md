# 项目实施计划

## 已完成阶段 (Phase 0-8)

### Phase 0: 父 POM 改造
- [x] 删除原单体 `src/` 目录
- [x] 重写父 POM 为多模块聚合（`<packaging>pom</packaging>`，14 个子模块）
- [x] `<dependencyManagement>` 引入 Spring Cloud / Spring Cloud Alibaba / Spring AI Alibaba BOM
- [x] 创建所有模块目录结构

### Phase 1: common-core（零外部依赖）
- [x] `Result.java`、`BusinessException.java`、`ErrorCode.java`
- [x] `GlobalExceptionHandler.java`、`BeanUtil.java`

### Phase 2: common-redis / common-security / common-mybatis（并行完成）
- [x] **common-redis**: `RedisConfig.java`、`RedisUtil.java`
- [x] **common-security**: `JwtUtil.java`、`JwtAuthenticationFilter.java`、`SecurityUtil.java`、`BaseSecurityConfig.java`
- [x] **common-mybatis**: `MybatisPlusConfig.java`、`MyMetaObjectHandler.java`

### Phase 3: elderly-gateway
- [x] Spring Cloud Gateway（WebFlux，不含 spring-boot-starter-web）
- [x] `AuthGlobalFilter.java` 全局 JWT 解析 + 下游头传递
- [x] `application.yml` 路由规则 + CORS 全局配置

### Phase 4: elderly-auth
- [x] 从原型迁移 User 实体 + 认证 Controller/Service/DTO/VO
- [x] register, login, sendSmsCode, resetPassword, changePassword, updateProfile
- [x] getUserByPhone / getUserById（供其他服务 Feign 调用）

### Phase 5: elderly-user
- [x] Elder / Family 实体 + Mapper
- [x] QRCodeUtil 二维码生成/解析
- [x] ElderController（QR 码/创建/查询）、FamilyController（绑定管理）
- [x] AuthFeignClient 调用 auth 服务

### Phase 6: elderly-medicine + elderly-health（并行完成）
- [x] **health**: Health 实体 + CRUD + 健康阈值告警
- [x] **medicine**: Medicine + Record 实体 + CRUD

### Phase 7: elderly-remind + elderly-news（并行完成）
- [x] **remind**: Remind/RemindTask/Notification 实体 + WebSocket + 定时任务调度
- [x] **news**: News/NewsLike/NewsCollect 实体 + Redis 缓存

### Phase 8: elderly-ai + elderly-admin
- [x] **ai**: AI 助手（SSE 流式，WebFlux），Feign 聚合 medicine/remind 数据
- [x] **admin**: SystemConfig + Disease 管理

### 代码审查与修复（2026-05-23）
- [x] 15 个问题全部修复：
  - Feign 客户端返回类型统一为 `Result<T>`，参数注解与 Controller 匹配
  - 5 个服务安全配置收紧，只暴露必要公开端点
  - auth + gateway 补充 spring-boot-maven-plugin
  - AuthService 返回类型统一
  - AuthErrorCode 重新编号避免冲突
- [x] 最终审查 5 项检查全部通过，全量 BUILD SUCCESS

---

## 待进行 (Phase 9+)

### Phase 9: 集成验证

1. **启动基础设施**
   ```bash
   docker-compose up -d
   ```
   - MySQL 8.0 (13306)、Redis 7.0 (6379)、Nacos 2.3.2 (8848)

2. **启动所有微服务**
   - 依次启动 7 个业务服务 + gateway
   - 验证 Nacos 控制台 `localhost:8848/nacos` 可见全部服务注册

3. **端到端测试**
   - [ ] 注册新用户 → 登录获取 JWT
   - [ ] 创建老人档案 + 家属绑定 + 二维码解析
   - [ ] 创建健康数据（血压/血糖/心率）
   - [ ] 创建用药计划 + 用药记录
   - [ ] 定时任务触发 → WebSocket 推送提醒
   - [ ] 资讯浏览/收藏/点赞
   - [ ] AI 助手 SSE 流式对话
   - [ ] 管理后台配置/疾病字典

4. **Gateway 路由验证**
   - [ ] `/auth/**` → elderly-auth
   - [ ] `/elder/**`, `/family/**` → elderly-user
   - [ ] `/health/**` → elderly-health
   - [ ] `/medicine/**`, `/record/**` → elderly-medicine
   - [ ] `/remind/**`, `/ws/**` → elderly-remind
   - [ ] `/health-knowledge/**` → elderly-news
   - [ ] `/ai/**` → elderly-ai
   - [ ] `/admin/**` → elderly-admin

### 后续优化项

- [ ] Sentinel 限流熔断（当前镜像拉取失败）
- [ ] 单元测试 + 集成测试
- [ ] 接口文档（Knife4j / SpringDoc）
- [ ] 日志收集（ELK）
- [ ] 监控（Prometheus + Grafana）
- [ ] CI/CD 流水线配置
