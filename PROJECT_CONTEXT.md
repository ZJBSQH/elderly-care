# 老人用药管理系统 - 后端项目上下文文档

> **生成时间**: 2026-04-25  
> **最后更新**: 2026-05-13  
> **用途**: 快速恢复开发上下文，记录关键配置和已完成工作


要求:
不确定就问,别猜;
没要求的不写;
只改被要求部分;

---

## 📋 一、项目概述

### 1.1 项目名称
老人用药管理系统（Elderly Care Medicine Management System）

### 1.2 技术栈
**后端**:
- Spring Boot 3.x
- MyBatis-Plus
- Spring Security + JWT
- MySQL 数据库
- Redis (缓存)
- 运行端口: localhost:8080

### 1.3 项目定位
为老年人设计的用药管理APP后端系统，支持：
- 👴 **老人端**: 查看用药提醒、录入健康数据、紧急求助
- 👨‍👩‍👧 **家属端**: 绑定老人、接收通知、查看健康数据
- 👨‍💼 **管理员**: 系统管理、资讯发布

---

## 🗂️ 二、项目结构

```
backend/
├── src/main/java/com/elderlycare/
│   ├── controller/
│   │   ├── user/AuthController.java          # 认证接口
│   │   ├── funtion/
│   │   │   ├── AIAssistantController.java    # AI助手功能
│   │   │   ├── HealthController.java         # 健康数据
│   │   │   ├── MedicineController.java       # 用药管理
│   │   │   ├── NewsController.java           # 资讯管理(用户端)
│   │   │   └── RemindController.java         # 提醒管理
│   │   └── admin/                            # 管理员接口
│   │       ├── AdminAnnouncementController.java
│   │       ├── AdminNewsController.java      # 资讯管理(管理端)
│   │       ├── AdminStatsController.java
│   │       ├── AdminSystemController.java
│   │       └── AdminUserController.java
│   ├── service/impl/
│   │   ├── UserServiceImpl.java
│   │   ├── MedicineServiceImpl.java
│   │   ├── HealthServiceImpl.java
│   │   ├── NewsServiceImpl.java              # 资讯服务实现
│   │   ├── AdminNewsServiceImpl.java         # 管理端资讯服务
│   │   └── ...
│   ├── mapper/                               # MyBatis Mapper
│   ├── pojo/
│   │   ├── entity/                           # 实体类
│   │   ├── dto/                              # 数据传输对象
│   │   └── vo/                               # 视图对象
│   ├── common/
│   │   ├── config/
│   │   │   ├── AIConfig.java                 # AI配置
│   │   │   ├── SecurityConfig.java          # ⚠️ 安全配置（含临时放行）
│   │   │   └── WebSocketConfig.java
│   │   ├── exception/
│   │   │   ├── BusinessException.java
│   │   │   ├── ErrorCode.java
│   │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── scheduler/
│   │   │   └── RemindTaskScheduler.java     # 定时任务调度
│   │   ├── util/
│   │   │   ├── JwtUtil.java
│   │   │   ├── QRCodeUtil.java
│   │   │   └── SecurityUtil.java
│   │   ├── websocket/
│   │   │   └── NotifyWebSocket.java
│   │   └── Result.java                      # 统一响应格式
│   ├── filter/
│   │   ├── AdminAuthFilter.java
│   │   └── JwtAuthenticationFilter.java     # JWT过滤器
│   └── BackendApplication.java
└── resources/
    ├── application.yml                       # 配置文件
    ├── com.elderlycare.mapper/*.xml          # MyBatis XML
    └── templates/
```

---

## 🔑 三、关键配置

### 3.1 数据库配置
**文件**: `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/elderly_care?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

**数据库文件**: `elderly_care.sql`（包含测试数据）

### 3.2 安全配置（⚠️ 重要）
**文件**: `src/main/java/com/elderlycare/common/config/SecurityConfig.java`

**当前状态**: 临时放行了以下接口（生产环境需移除）:
```java
.requestMatchers("/remind/task/**").permitAll()
.requestMatchers("/health/**").permitAll()
.requestMatchers("/remind/notification/**").permitAll()
```

**原因**: 前端Token传递曾有问题，现已修复，但保留临时放行以便测试。

### 3.3 JWT配置
- Token有效期: 7天
- 请求头: `Authorization: Bearer {token}`
- 登录接口返回: `{code: 200, data: {token: '...', user: {...}}}`

---

## ✅ 四、已完成功能

### 4.1 认证模块 ✅
- [x] 用户登录（手机号+密码）
- [x] Token保存与自动携带
- [x] 401/403错误处理（自动跳转登录）
- [x] 退出登录（清除Token）

**关键文件**:
- `controller/user/AuthController.java`
- `service/UserService.java`
- `filter/JwtAuthenticationFilter.java`

### 4.2 用药管理模块 ✅
**控制器**: `controller/funtion/MedicineController.java`

**功能**:
- [x] 今日用药任务查询
- [x] 标记药品服用状态
- [x] 用药记录查询
- [x] 药品信息管理

**API**:
- `GET /remind/task/today?elderId={id}` - 今日任务
- `POST /record/take/{taskId}` - 标记服用
- `GET /medicine/list?elderId={id}` - 药品列表

### 4.3 健康数据模块 ✅
**控制器**: `controller/funtion/HealthController.java`

**功能**:
- [x] 最新健康数据查询
- [x] 历史记录查询
- [x] 健康趋势分析
- [x] 健康预警功能

**API**:
- `GET /health/latest?elderId={id}` - 最新数据
- `GET /health/history?elderId={id}&startDate=...&endDate=...` - 历史记录
- `GET /health/trend?elderId={id}&startDate=...&endDate=...` - 趋势数据

### 4.4 资讯管理模块 ✅
#### 用户端 (`controller/funtion/NewsController.java`)
**功能**:
- [x] 文章浏览：列表、详情、分类查询、推荐文章、热门文章
- [x] 搜索功能：关键词搜索、分类筛选、分页查询
- [x] 互动功能：点赞/取消点赞、收藏/取消收藏
- [x] 状态检查：检查是否已点赞、检查是否已收藏
- [x] 自动精选机制：点赞数≥50且收藏数≥10时自动标记为精选

**核心业务逻辑**:
- 阅读计数：每次查看详情时自动增加阅读量
- 实时状态：在列表中显示用户的点赞和收藏状态
- 批量查询优化：减少数据库查询次数

**API**:
- `GET /health-knowledge/list?pageNum=1&pageSize=10` - 资讯列表
- `GET /health-knowledge/article/{id}` - 文章详情
- `GET /health-knowledge/articles?category=xxx` - 分类文章
- `GET /health-knowledge/recommended` - 推荐文章
- `GET /health-knowledge/popular?limit=10` - 热门文章
- `GET /health-knowledge/search?keyword=xxx` - 搜索文章
- `POST /health-knowledge/collect` - 收藏文章
- `DELETE /health-knowledge/collect` - 取消收藏
- `GET /health-knowledge/collects` - 用户收藏列表
- `GET /health-knowledge/collect/check?newsId={id}` - 检查是否已收藏
- `POST /health-knowledge/like` - 点赞文章
- `DELETE /health-knowledge/like` - 取消点赞
- `GET /health-knowledge/like/check?newsId={id}` - 检查是否已点赞

#### 管理端 (`controller/admin/AdminNewsController.java`)
**功能**:
- [x] 内容管理：发布、编辑、删除资讯
- [x] 状态管理：上架/下架、推荐/取消推荐
- [x] 数据查询：分页列表、详情查看、分类/状态筛选

**API**:
- `POST /admin/news` - 发布资讯
- `PUT /admin/news` - 更新资讯
- `DELETE /admin/news/{id}` - 删除资讯
- `GET /admin/news?page=1&size=10` - 资讯列表（分页）
- `GET /admin/news/{id}` - 获取资讯详情
- `PUT /admin/news/{id}/status` - 上架/下架资讯
- `PUT /admin/news/{id}/recommended` - 推荐/取消推荐资讯

### 4.5 提醒管理模块 ✅
**控制器**: `controller/funtion/RemindController.java`

**功能**:
- [x] 提醒任务管理
- [x] 通知推送
- [x] 定时任务调度

**API**:
- `GET /remind/task/today?elderId={id}` - 今日任务
- `GET /remind/notification/user/my?userId={id}` - 我的通知

### 4.6 AI助手模块 ✅
**控制器**: `controller/funtion/AIAssistantController.java`

**功能**:
- [x] AI对话功能
- [x] 药物信息查询
- [x] 健康建议生成

---

## ⚠️ 五、已知问题与解决方案

### 5.1 GET请求参数丢失 ❌ → ✅ 已修复
**问题**: 前端没有将GET请求的params拼接到URL上

**错误表现**:
```
MissingServletRequestParameterException: Required request parameter 'elderId' is not present
```

**解决方案**: 在前端请求工具中添加参数拼接逻辑

### 5.2 TabBar页面跳转错误 ❌ → ✅ 已修复
**问题**: 前端使用 `uni.navigateTo` 跳转到TabBar页面

**错误表现**:
```
navigateTo:fail can not navigateTo a tabbar page
```

**解决方案**: TabBar页面必须使用 `uni.switchTab`

### 5.3 前后端数据格式不匹配 ❌ → ✅ 已修复
**问题**: 后端返回 `{code: 200, message: '...', data: {...}}`，前端期望直接数据

**错误表现**:
```
Token: 不存在
TypeError: tasks.map is not a function
```

**解决方案**: 在前端请求工具的success回调中自动提取data字段



---

## 🎯 六、待完成功能

### 6.1 高优先级
- [ ] **完善健康数据录入功能**
  - API: `POST /health/record`
  - 字段: elderId, bloodPressure, bloodSugar, heartRate, weight
  
- [ ] **用药统计图表功能**
  - 本周服药率统计
  - 漏服提醒功能

### 6.2 中优先级
- [ ] **个人资料编辑功能**
  - API: `PUT /auth/profile`
  
- [ ] **紧急联系人设置功能**
  - API: `PUT /elder/emergency-contact`
  
- [ ] **健康趋势图表功能**
  - API: `GET /health/trend?elderId={id}&startDate=...&endDate=...`

### 6.3 低优先级
- [ ] WebSocket实时通知功能优化
- [ ] AI助手对话功能增强
- [ ] 用药计划智能推荐功能
- [ ] 家属端完整功能实现

---

## 🔧 七、开发环境

### 7.1 后端启动
```bash
cd C:\Users\zheng\Desktop\软件设计\app\backend
mvn spring-boot:run
```

**检查点**:
- ✅ 端口: 8080
- ✅ 数据库连接成功
- ✅ Security配置正确

### 7.2 测试账号
```
手机号: 13800138000
密码: 123456
用户类型: 老人 (userType=0)
elderId: 1
```

---

## 📝 八、API接口清单

### 8.1 认证接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| POST | `/auth/login` | 登录 | ❌ |
| POST | `/auth/register` | 注册 | ❌ |
| PUT | `/auth/profile` | 更新资料 | ✅ |
| PUT | `/auth/password/change` | 修改密码 | ✅ |

### 8.2 用药接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| GET | `/remind/task/today?elderId={id}` | 今日任务 | ✅ |
| POST | `/record/take/{taskId}` | 标记服用 | ✅ |
| GET | `/medicine/list?elderId={id}` | 药品列表 | ✅ |

### 8.3 健康接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| GET | `/health/latest?elderId={id}` | 最新数据 | ✅ |
| GET | `/health/today?elderId={id}` | 今日数据 | ✅ |
| GET | `/health/history?elderId={id}&startDate=...&endDate=...` | 历史记录 | ✅ |
| POST | `/health/record` | 录入数据 | ✅ |
| GET | `/health/trend?elderId={id}&startDate=...&endDate=...` | 趋势数据 | ✅ |

### 8.4 资讯接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| GET | `/health-knowledge/list?pageNum=1&pageSize=10` | 资讯列表 | ✅ |
| GET | `/health-knowledge/article/{id}` | 资讯详情 | ✅ |
| GET | `/health-knowledge/articles?category=xxx` | 分类文章 | ✅ |
| GET | `/health-knowledge/recommended` | 推荐文章 | ✅ |
| GET | `/health-knowledge/popular?limit=10` | 热门文章 | ✅ |
| GET | `/health-knowledge/search?keyword=xxx` | 搜索文章 | ✅ |
| POST | `/health-knowledge/collect` | 收藏文章 | ✅ |
| DELETE | `/health-knowledge/collect` | 取消收藏 | ✅ |
| GET | `/health-knowledge/collects` | 用户收藏列表 | ✅ |
| GET | `/health-knowledge/collect/check?newsId={id}` | 检查是否已收藏 | ✅ |
| POST | `/health-knowledge/like` | 点赞文章 | ✅ |
| DELETE | `/health-knowledge/like` | 取消点赞 | ✅ |
| GET | `/health-knowledge/like/check?newsId={id}` | 检查是否已点赞 | ✅ |

### 8.5 管理端资讯接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| POST | `/admin/news` | 发布资讯 | ✅ |
| PUT | `/admin/news` | 更新资讯 | ✅ |
| DELETE | `/admin/news/{id}` | 删除资讯 | ✅ |
| GET | `/admin/news?page=1&size=10` | 资讯列表（分页） | ✅ |
| GET | `/admin/news/{id}` | 获取资讯详情 | ✅ |
| PUT | `/admin/news/{id}/status` | 上架/下架资讯 | ✅ |
| PUT | `/admin/news/{id}/recommended` | 推荐/取消推荐资讯 | ✅ |

### 8.6 通知接口
| 方法 | 路径 | 说明 | 需要Token |
|------|------|------|-----------|
| GET | `/remind/notification/user/my?userId={id}` | 我的通知 | ✅ |

---

## 💡 九、开发规范

### 9.1 后端代码规范
1. **统一响应格式**: 使用 `Result.success(data)` 或 `Result.error(code, message)`
2. **异常处理**: 服务层抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一捕获
3. **权限控制**: 通过 `SecurityConfig` 配置接口访问权限
4. **日志记录**: 关键操作使用 `log.info()` 或 `log.error()`

### 9.2 数据库规范
1. **表名前缀**: 无（直接使用业务名称）
2. **主键**: 自增ID（Integer类型）
3. **时间字段**: `LocalDateTime` 类型
4. **外键关联**: 通过 `elder_id`、`user_id` 等字段关联

---

## 🚀 十、快速开始指南

### 10.1 首次启动
1. **导入数据库**: 
   ```bash
   mysql -u root -p < elderly_care.sql
   ```

2. **修改后端配置**:
   - 编辑 `application.yml`，设置数据库密码
   
3. **启动后端**:
   ```bash
   mvn spring-boot:run
   ```

4. **启动前端**:
   - 在HBuilderX中运行到浏览器

5. **登录测试**:
   - 手机号: 13800138000
   - 密码: 123456

### 10.2 日常开发
1. 修改后端代码 → 自动热重载
2. 查看后端日志 → 控制台输出

---

## 📞 十一、常见问题FAQ

### Q1: 登录后提示403错误？
**A**: 检查Token是否正确保存，查看JWT过滤器配置

### Q2: GET请求参数传不过去？
**A**: 确认使用了正确的参数传递方式

### Q3: 后端启动失败？
**A**: 检查端口8080是否被占用，数据库是否启动

---

## 📌 十二、重要提醒

1. ⚠️ **SecurityConfig中的临时放行配置**需要在生产环境移除
2. ⚠️ **后端返回的数据格式是 `{code, message, data}`**
3. ⚠️ **所有业务异常都应通过GlobalExceptionHandler统一处理**

---

## 📅 十三、版本历史

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2026-04-25 | v1.0 | 初始版本，完成基础架构和核心功能 |
| 2026-05-13 | v1.1 | 完善资讯功能，整合前后端接口 |

---

**文档结束**

> 💡 **使用建议**: 下次会话时，将此文件内容发送给AI助手，可以快速恢复上下文，继续开发工作。
