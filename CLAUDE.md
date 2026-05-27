# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

老年护理云平台 (Elderly Care Cloud Platform) — a Spring Boot microservices system for elderly care management, including health tracking, medication reminders, news, and user management.

- **Base package**: `com.elderlycare`
- **Java**: 17
- **Spring Boot**: 3.5.11
- **Spring Cloud**: 2025.0.2
- **Spring Cloud Alibaba**: 2025.0.0.0
- **Build**: Maven (`pom.xml`)
- **Author**: 郑

## Quick Commands

```bash
# Build the project
./mvnw clean package -DskipTests

# Run tests (needs running Docker infrastructure)
./mvnw test

# Run a single test
./mvnw test -Dtest=ClassName#methodName

# Start infrastructure (MySQL, Redis, Nacos)
docker-compose up -d

# Stop infrastructure
docker-compose down
```

## Infrastructure (Docker Compose)

| Service | Port | Credentials / Notes |
|---------|------|---------------------|
| MySQL 8.0 | 13306 | root / Zheng666 |
| Redis 7.0 | 6379 | default |
| Nacos 3.2.1 | 8848 (API/gRPC), 9848 (gRPC), **8080 (Console)** | Standalone mode, console at `http://localhost:8080/` |
| Sentinel | 8858 | **Currently disabled** (image pull failed) |

## Microservices Architecture

The system is designed as **13 modules** — 4 common libraries + 1 gateway + 7 business services + 1 AI service. Each business service has its own database (shared MySQL instance). Cross-service references use **soft references** (ID-based) — **no cross-database foreign keys**.

### Common Modules (`elderly-common/`)

| Module | Purpose |
|--------|---------|
| `common-core` | `Result<T>`, `BusinessException`, `ErrorCode` interface, `GlobalExceptionHandler`, `BeanUtil` |
| `common-security` | `JwtUtil`, `JwtAuthenticationFilter` (stores userId in `Authentication.details`), `SecurityUtil` (reads from SecurityContext), `BaseSecurityConfig` (abstract, subclasses override `permitAllPaths()`) |
| `common-redis` | `RedisConfig` (Jackson2JsonRedisSerializer), `RedisUtil` (get/set/delete/hasKey) |
| `common-mybatis` | `MybatisPlusConfig` (pagination interceptor), `MyMetaObjectHandler` (auto-fill createTime/updateTime) |

### Business Services

| Service | DB | Port | Key Tables |
|---------|-----|------|------------|
| `elderly-gateway` | - | 8080 | Spring Cloud Gateway + `AuthGlobalFilter` (JWT → X-User-Id/X-User-Phone/X-User-Type headers) |
| `elderly-auth` | `db_elderly_auth` | 8081 | `user` (accounts, BCrypt passwords, roles: 0-elder/1-family/2-admin) |
| `elderly-user` | `db_elderly_user` | 8082 | `elder` (health profiles, QR codes), `family` — soft-refs to auth.user |
| `elderly-health` | `db_elderly_health` | 8083 | `health` (BP, blood sugar, heart rate, weight, warning flag) — soft-refs to user.elder |
| `elderly-medicine` | `db_elderly_medicine` | 8084 | `medicine` (plans), `record` (dosing history) — soft-refs to user.elder |
| `elderly-remind` | `db_elderly_remind` | 8085 | `remind` (settings), `remind_task`, `notification` — WebSocket push + `@EnableScheduling` |
| `elderly-news` | `db_elderly_news` | 8086 | `news`, `news_collect`, `news_like` — Redis caching |
| `elderly-ai` | - (无数据库) | 8087 | AI assistant (SSE streaming via WebFlux), Feign aggregation |
| `elderly-admin` | `db_elderly_admin` | 8088 | `system_config`, `disease` (dictionary) — fully independent |

### Cross-Service Communication

- **Gateway → Services**: Gateway parses JWT, passes `X-User-Id`/`X-User-Phone`/`X-User-Type` headers downstream
- **Service ↔ Service**: OpenFeign + LoadBalancer, all Feign interfaces return `Result<T>`, callers unwrap via `.getData()`
- **Auth → User**: Feign `AuthFeignClient` (in elderly-user) calls auth for user lookup by phone/id
- **AI → Medicine/Remind**: Feign `MedicineFeignClient` + `RemindFeignClient` (in elderly-ai)

### Security Architecture

- `BaseSecurityConfig` provides a default `SecurityFilterChain` bean: all requests require auth unless listed in `permitAllPaths()`
- Each service subclass overrides `jwtAuthenticationFilter()` and `permitAllPaths()` to expose only public endpoints
- Public endpoints per service: auth (sms/register/login/password/reset, user lookup), news (health-knowledge read APIs), all others have zero permit-all paths
- `SecurityUtil.getCurrentUserId()` reads from `SecurityContextHolder` → `Authentication.details` — no database dependency

### Error Code Ranges

| Service | Range |
|---------|-------|
| common-core | 200, 400, 401, 403, 404, 500 |
| elderly-auth | 1000-1999 |
| elderly-user | 2000-2999 (含 3001-3006) |
| elderly-health | 4000-4999 |
| elderly-medicine | 5000-5999 |
| elderly-remind | 6000-6999 |
| elderly-ai | 7000-7999 |
| elderly-news | 8000-8999 |
| elderly-admin | 9000-9999 |

## Code Conventions (from `.feisuan/rules/project_rule.md`)

- **Comments must be in Chinese**, with Javadoc on all classes, methods, and fields.
- **Lombok**: Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` instead of manual getters/setters.
- **Interface-implementation separation**: Service interfaces in e.g. `service/`, implementations in `service/impl/`.
- **Type naming suffixes** (Alibaba style): `*DTO`, `*DO`, `*BO`, `*VO`, `*Query`.
- **`@Slf4j`** for logging, never `System.out.println`.
- **Validation**: Use `@Valid` + Jakarta annotations (`jakarta.validation.constraints.*`), not `javax.validation`.
- **`@Transactional`** only on Service-layer methods; avoid in loops.
- **No manual SQL string concatenation** — prevent SQL injection.
- **Controller → Service → Repository**: Controllers never touch the database directly; Services return DTOs not entities.
- Principles: SOLID, DRY, KISS, YAGNI, OWASP Top 10 awareness.

## Current Project State

**Phase 0-8 complete** (as of 2026-05-23). All 14 Maven modules compile successfully (`./mvnw clean install -DskipTests` → BUILD SUCCESS, 137 Java source files).

- 4 common libraries implemented (core, security, redis, mybatis)
- Gateway with JWT global filter and route rules
- 7 business services with full Controller/Service/Mapper/Entity/DTO/VO layers
- OpenFeign cross-service communication established
- 7 SQL schema files in `init-sql/`, auto-loaded on `docker-compose up`

**Next steps** (see PLAN.md):
1. Start Docker infrastructure and run integration tests
2. End-to-end flow validation (register → login → health → medicine → remind WebSocket push)
3. AI SSE streaming verification
4. Sentinel integration (image currently unavailable)
5. Unit/integration test coverage
