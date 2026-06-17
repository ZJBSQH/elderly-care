
# 开发规范指南

本项目为老年护理云服务（elderly_care_cloud）的基础演示模块。为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目基础信息

- **工作区路径**：`F:\Workspace\JAVA_Workspace\elderly_care_cloud\demo`
- **操作系统**：Windows 11
- **构建工具**：Maven
- **Java 版本**：JDK 17.0.12
- **主框架**：Spring Boot 4.1.0 (Parent)
- **包路径前缀**：`com.example.demo`
- **代码作者**：郑

## 二、目录结构规范

项目采用标准的 Maven 多模块结构，请严格遵守以下目录树结构：

```text
demo
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── demo
    │   │               ├── controller/   # 控制层
    │   │               ├── service/      # 服务接口
    │   │               │   └── impl/     # 服务实现
    │   │               ├── repository/   # 数据访问层
    │   │               ├── entity/       # 实体类
    │   │               ├── dto/          # 数据传输对象
    │   │               └── config/       # 配置类
    │   └── resources
    │       ├── application.yml           # 主配置文件
    │       └── application-dev.yml       # 开发环境配置
    └── test
        └── java
            └── com
                └── example
                    └── demo
```

## 三、技术栈与依赖规范

### 1. 核心依赖

根据 `pom.xml` 及项目演进需求，主要依赖如下：

- **Spring Boot Starter**：基础核心依赖
- **Spring Boot Starter Test**：单元测试依赖
- **Spring Boot Maven Plugin**：用于打包可执行 JAR

> **注意**：当前 `pom.xml` 较为精简。若后续引入 JPA、Web 或 Lombok，请遵循以下新增规则：
> - 若引入 `spring-boot-starter-web`：需配置 Controller 层规范。
> - 若引入 `spring-boot-starter-data-jpa`：需配置 Repository 层及 Entity 规范。
> - 若引入 `lombok`：需配置 Lombok 注解使用规范。

### 2. 构建配置

- 使用 `spring-boot-maven-plugin` 进行打包。
- 确保 `java.version` 属性设置为 `17`。

## 四、分层架构规范

尽管当前依赖较少，但为保持架构一致性，建议按以下层级组织代码：

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 保持轻量，仅做参数校验和结果封装，不写业务逻辑                 |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 接口与实现分离；接口放在 `service` 包，实现放在 `service.impl` |
| **Repository** | 数据库访问与持久化操作             | 继承 `JpaRepository`（若使用 JPA）；命名规范符合 Spring Data 规范 |
| **Entity**     | 映射数据库表结构                   | 包名统一为 `entity`；使用 Lombok 简化 Getter/Setter           |

### 接口与实现分离

- 所有 Service 接口需放在 `service` 包下。
- 所有 Service 实现类需放在 `service.impl` 子包中，类名以 `Impl` 结尾（如 `UserServiceImpl`）。

## 五、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解。
- 注意：Spring Boot 3.x/4.x 中校验注解位于 `jakarta.validation.constraints.*`。
- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 六、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |

### 注释规范

- **语言要求**：所有类、方法、字段需添加 **Javadoc** 注释，且注释内容必须使用 **中文**（第一语言）。
- 示例：
  ```java
  /**
   * 用户服务接口
   * @author 郑
   */
  public interface UserService {
      // ...
  }
  ```

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| BO   | 业务逻辑封装对象             | `UserBO`     |
| VO   | 视图展示对象                 | `UserVO`     |
| Query| 查询参数封装对象             | `UserQuery`  |

### 实体类简化工具

- 推荐使用 Lombok 注解替代手动编写 getter/setter/构造方法（若项目引入 Lombok）：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 七、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`。
- 日志级别使用规范：
  - `error`：系统错误、异常堆栈
  - `warn`：警告信息
  - `info`：关键业务流程、启动信息
  - `debug`：调试信息（生产环境建议关闭）

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
