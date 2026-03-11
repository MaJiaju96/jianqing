# 简擎后端架构草案（v0.1）

## 1. 目标与边界

- `v0.1` 目标：交付可运行的后台管理后端内核（认证、RBAC、审计、配置）。
- 数据库：`MySQL`（唯一必选依赖）。
- `Elasticsearch`、`Nacos`、`RocketMQ`：`v0.1` 仅做适配层与接口预留，默认不强依赖。

## 1.1 工程原则（全局约束）

- 后端编码规范严格遵循阿里巴巴 Java 开发规范。
- 设计与实现优先“简单、直接、可读”，避免为炫技引入不必要复杂度。
- 优先封装重复逻辑，但保持封装边界清晰，避免过度抽象。
- 代码与目录结构需对后续开发者和 AI 持续开发友好。
- 架构需保留多技术路线演进空间：
  - 前端：`JavaScript` 主线，后续 `TypeScript` 分支、`Vue2` 分支。
  - 后端：后续 `SpringBoot2`、`SpringCloud`、`JDK8` 等分支。
  - 长期：向企业级项目框架能力演进。

## 2. 分层与模块

建议先维持单体工程、多包分层（后续可平滑拆分为多模块 Maven）。

### 2.1 包结构（MVP）

```text
com.jianqing
├── common
│   ├── api                # ApiResponse、分页对象、返回码
│   ├── exception          # 全局异常、业务异常
│   ├── enums              # 状态枚举/常量
│   └── util               # 工具类（时间、JSON、IP、Trace）
├── framework
│   ├── security           # JWT、认证过滤器、Security 配置
│   ├── mybatis            # MyBatis-Plus 配置、审计字段填充
│   ├── web                # 拦截器、统一日志、traceId
│   └── plugin             # 扩展点 SPI、事件总线接口
├── module
│   ├── auth               # 登录、刷新 token、登出
│   ├── system
│   │   ├── user           # 用户管理
│   │   ├── role           # 角色管理
│   │   ├── menu           # 菜单与权限点
│   │   ├── dept           # 部门树
│   │   ├── dict           # 字典
│   │   └── config         # 系统参数
│   └── audit              # 登录日志、操作日志查询
└── integration
    ├── search             # ES 适配接口与默认实现
    ├── config             # Nacos 配置适配接口与默认实现
    └── mq                 # RocketMQ 消息适配接口与默认实现
```

### 2.2 依赖方向

- `common` 不依赖业务模块。
- `framework` 仅依赖 `common`。
- `module.*` 依赖 `common + framework`。
- `integration.*` 依赖 `common`，通过接口被 `module` 调用。
- 禁止跨业务模块直接访问彼此 `mapper`，通过 `service`/`facade` 交互。

## 3. 核心能力设计

## 3.1 认证与授权

- 登录：`username + password`，成功后签发 JWT（已具备 `JwtTokenService`）。
- JWT 载荷建议：`sub(username)`、`uid`、`roles`、`tenant(预留)`。
- 鉴权：`Spring Security` + 路径权限 + `perms` 按钮权限。
- 会话策略：无状态，后续可加黑名单（Redis）。

## 3.2 RBAC 与数据权限

- 基础表已具备：`jq_sys_user`、`jq_sys_role`、`jq_sys_menu`、`jq_sys_user_role`、`jq_sys_role_menu`。
- 数据权限字段：`jq_sys_role.data_scope`（已存在）。
- 在 `service` 层统一注入数据权限条件，避免散落在 controller。

### 3.2.1 System 模块服务收口约定

- `SystemServiceImpl` 作为系统聚合入口，负责：用户数据范围校验、查询面聚合、事务边界声明。
- 具体写路径下沉到轻量执行器，避免单个 ServiceImpl 持续膨胀：
  - `UserWriteOperationHandler`
  - `UserRoleAssignmentHandler`
  - `RoleWriteOperationHandler`
  - `RoleMenuAssignmentHandler`
  - `MenuWriteOperationHandler`
  - `DeptWriteOperationHandler`
- 执行器只处理单一写路径编排，不承接无关查询职责。
- 跨表写操作统一通过事务边界和提交后缓存失效保障一致性。

## 3.3 审计与可观测

- 操作日志：写入 `jq_sys_oper_log`。
- 登录日志：写入 `jq_sys_login_log`。
- 每次请求生成/透传 `traceId`，用于日志与审计关联。
- 异常统一由 `GlobalExceptionHandler` 收敛。

## 4. 集成适配层（预留接口）

## 4.1 Elasticsearch 适配

```java
public interface SearchGateway {
    void upsert(String index, String id, Object document);
    void delete(String index, String id);
    <T> PageResult<T> search(String index, SearchQuery query, Class<T> clazz);
}
```

- 默认实现：`NoopSearchGateway`（不报错、记录 warn）。
- 激活方式：`jianqing.integration.search.enabled=true` 且存在 ES 客户端依赖时装配。

## 4.2 Nacos 配置适配

```java
public interface DynamicConfigGateway {
    String get(String dataId, String group, String defaultValue);
    void addListener(String dataId, String group, ConfigChangeListener listener);
}
```

- 默认实现：`LocalConfigGateway`（读取数据库 `jq_sys_config`）。
- Nacos 实现启用后覆盖默认实现。

## 4.3 RocketMQ 消息适配

```java
public interface MessageBusGateway {
    void send(String topic, String tag, Object payload);
    void subscribe(String topic, String group, MessageHandler handler);
}
```

- 默认实现：`InMemoryMessageBusGateway`（开发环境可观测）。
- 生产环境按配置切换 RocketMQ 实现。

## 5. 配置规范（建议）

在 `application.yml` 增加如下配置段：

```yaml
jianqing:
  integration:
    search:
      enabled: false
    nacos:
      enabled: false
    rocketmq:
      enabled: false
```

原则：默认全关闭，按需启用，确保核心管理系统可独立运行。

## 6. 首批接口清单（v0.1）

- 认证
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- 用户与权限
  - `GET /api/system/users`
  - `POST /api/system/users`
  - `PUT /api/system/users/{id}`
  - `GET /api/system/roles`
  - `GET /api/system/menus/tree`
- 审计
  - `GET /api/audit/oper-logs`
  - `GET /api/audit/login-logs`

## 7. 实施顺序（建议）

1. 打通登录链路与 JWT 过滤器。
2. 打通用户/角色/菜单查询接口。
3. 接入操作日志 AOP/拦截器。
4. 加入三个 integration 网关接口与默认实现（先不接外部中间件）。
5. 完善 README 的“开关式集成”说明。
