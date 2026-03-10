# Findings & Decisions

## Requirements
- 项目名：`简擎`
- 目标：打造个人开源 Java 后端管理系统
- 参考对象：`RuoYi`、`Yudao`
- 约束：数据库先使用 `MySQL`
- 后续集成目标：`Elasticsearch`、`Nacos`、`RocketMQ`
- 过程要求：使用 `planning-with-files` 的 plan 模式，减少重复对话和需求讨论
- 核心前提：项目定位为“极简、友好、可演进”的 Java 管理系统
- 编码前提：后端严格按阿里巴巴 Java 开发规范执行
- 实现前提：优先简单易读逻辑，避免炫技式复杂实现
- 协作前提：代码需对后续开发者与 AI 协作都友好
- 规划前提：需提前考虑多技术分支（前端 TS/Vue2，后端 SpringBoot2/SpringCloud/JDK8）

## Research Findings
- 当前仓库已存在初始化能力说明，见 `README.md`。
- 当前 README 已给出初始化 SQL、默认账号说明和后续建议路径（登录/JWT、权限、日志）。
- 对比方向：不追求“大而全”，优先差异化在“轻量内核 + 可插拔集成 + 工程化规范”。
- 当前代码已具备：`ApiResponse`、`GlobalExceptionHandler`、`JwtTokenService`、`JwtProperties`，适合直接按分层补齐。
- 当前 SQL 已覆盖系统管理核心表与审计日志表，满足 v0.1 核心闭环的表结构前置条件。
- 已将 `SysUserMapper`、`SysRoleMapper`、`SysMenuMapper` 的注解 SQL 迁移到 XML。
- 已按规范将 XML 从 `src/main/resources` 迁移到对应 `Mapper` 接口同目录（`src/main/java/.../mapper`）。
- 已配置 `mybatis-plus.mapper-locations=classpath*:com/jianqing/**/mapper/*Mapper.xml`。
- 已在 `pom.xml` 增加 `src/main/java/**/*.xml` 资源打包配置。
- 已新增 `DynamicConfigGateway` + `LocalDynamicConfigGateway` 作为 Nacos 适配层默认实现。
- 已新增 `MessageBusGateway` + `InMemoryMessageBusGateway` 作为 RocketMQ 适配层默认实现。
- 已在 `application.yml` 增加 `jianqing.integration.search/nacos/rocketmq` 开关配置。
- 前端项目已调整为后端同级目录：`/Users/majiaju/Person/code/jianqing/jianqing-admin-web`。
- 前端已从 TS 全量切换为纯 JS（无 `.ts` 文件、无 `lang=\"ts\"`、无 TS 依赖）。
- 前端已补齐 `README.md`，包含项目概要、启动命令、代理联调与常见问题。
- 已补齐系统管理基础 CRUD 与分配能力：用户-角色、角色-菜单。
- 已创建统一工作区目录：`/Users/majiaju/Person/code/jianqing`，用于跨前后端联动开发。
- 已重构后端 `README.md`，补齐项目定位、能力边界、快速开始与路线图。
- 已新增 `CONTRIBUTING.md`，明确分支策略与 PR 提交规范。
- 已新增 `docs/API_EXAMPLES.md`，提供核心接口调用示例。
- 已拆分菜单可见性与权限计算查询：按钮权限不再受 `visible` 字段影响。
- 已补充审计日志菜单与按钮权限初始化数据（SQL）。
- 已新增增量修复脚本 `sql/patch/20260305_permission_fix.sql`，用于旧库补齐审计权限数据。
- 已新增 GitHub Actions 工作流 `backend-ci.yml`，在 `main/master` 的 push/PR 自动执行 `mvn -DskipTests compile` 与 `mvn test`。
- 已在后端接入 `Checkstyle`（`maven-checkstyle-plugin` + `checkstyle.xml`），并将 `mvn checkstyle:check` 纳入 CI 门禁。
- 已将系统管理写操作接口由 `PUT`/`DELETE` 统一迁移为 `POST`，后端对外接口方法统一为 `GET`/`POST`。
- 已接入 Redis：登录 token 写入 Redis 并按 `jianqing.jwt.expire-seconds` 设置 TTL，鉴权改为 JWT + Redis 会话双校验。
- 已引入热点缓存（用户/角色/菜单列表、用户角色/权限/菜单树）并使用延迟双删策略保障 DB-缓存一致性。
- 审计日志接口已支持筛选参数（关键字/状态/登录方式），用于前端列表“分野筛选 + 分页”联动查询。
- 已完成发布准备文档闭环：发布检查清单、v0.1/v0.2 范围说明、v0.1 发布说明。
- 已新增发布执行 SOP（含 tag、Release、回滚步骤），并明确未初始化 git 时的前置处理。
- 已新增首批认证链路回归测试（JWT 生成解析、token 会话存取与失效）。
- 已新增审计服务回归测试，覆盖分页边界与筛选入参场景。
- 已新增控制器回归测试（Auth/Audit），覆盖登录参数校验、默认分页参数与响应结构。
- 已完成服务层结构重构：`auth/audit/system` 模块统一为“接口 + impl”模式，调用方改为依赖接口。
- 已完成 framework 服务结构重构：`JwtTokenService`、`TokenSessionService`、`CacheConsistencyService` 统一为“接口 + impl”。
- 已新增服务层结构守卫脚本，并接入 CI 自动检查，阻止后续出现非接口化 service 回归。
- 已新增本地 pre-commit 检查脚本与一键安装脚本，提交前可自动执行结构与质量门禁。
- 已新增 HTTP 方法约束守卫脚本，并接入 CI 与 pre-commit，自动拦截 `PUT/DELETE` 回归。
- 已修复 `SystemController`、`OperationLogInterceptor` 的 Mapper 直连，统一改为依赖 service。
- 已新增 Mapper 分层守卫脚本，自动拦截非 `service.impl` 层直接引用 Mapper。
- 核心业务服务已统一接入 `IService<T>` / `ServiceImpl<M,T>` 模式，保留 service 契约层并复用 MP 通用能力。
- 已新增 `RoleService`、`MenuService`、`LoginLogService`，将跨实体 mapper 调用收敛为 service 间协作。
- `AuthServiceImpl` 登录日志写入已改为调用 `AuditLogService`，不再直接依赖登录日志 mapper。
- `jq_sys_role` 表已预留 `data_scope` 字段，本轮已在后端实体/DTO/服务层正式接入并开始落地 v0.2 数据权限。
- 当前 v0.2 最小数据权限闭环先支持 `全部数据` / `本部门数据` / `仅本人数据` 三种范围，避免在未引入部门树前过度设计。
- 用户管理模块已作为首个闭环模块接入数据范围控制：列表查询与按 ID 的编辑/删除/分配角色均受当前登录用户数据范围约束。
- `jq_sys_dept` 表此前只有 SQL seed，本轮已补齐后端 `SysDept` 实体、Mapper、Service、Controller，部门管理从“仅有菜单 seed”变为可用模块。
- 后端数据权限专项测试已补齐首批单测，沿用现有 Mockito 服务层测试风格，优先覆盖 `super_admin`、`本部门`、`仅本人` 三条关键分支。
- 已完成一轮最新真实账号联调复核：`dept_user` 仅可见本部门用户（当前为 admin/test/dept_user/self_user/other_user），`self_user` 仅可见自己，`outside_user` 位于“外部协作部”。
- 当前开发联调测试账号口径已统一：`admin/admin123`，其余测试账号统一使用 `test123`。

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| v0.1 采用 MySQL 单数据源 | 先保证可用与稳定，降低初版复杂度 |
| 先交付管理内核能力（认证/RBAC/审计） | 形成可演示的最小闭环 |
| 集成能力做适配层预留（ES/Nacos/RocketMQ） | 支撑后续扩展而不破坏内核 |
| 规划文件作为长期上下文 | 降低跨轮对话的信息丢失 |
| 新增 `ARCHITECTURE.md` 统一实现蓝图 | 让后续编码按同一模块边界推进，减少返工 |
| 后端代码按阿里巴巴 Java 规范推进 | 约束命名、分层与异常处理风格，便于协作与开源 |
| 先以最小 Checkstyle 规则集启动静态检查 | 降低一次性整改成本，先建立自动化门禁再渐进增强规则 |
| Mapper 优先 XML SQL | 便于复杂 SQL 管理与后续审查优化 |
| Mapper.xml 与 Mapper 接口同目录 | SQL 与接口就近维护，避免资源目录分散 |
| 集成组件默认提供本地实现 | 未接入中间件时保持核心模块可独立运行 |
| 前端目录采用与后端平级结构 | 便于前后端独立迭代与部署 |
| 前端技术栈采用纯 JavaScript | 满足当前开发偏好，降低维护复杂度 |
| 关键/复杂逻辑需要必要注释 | 控制注释密度同时保证关键代码可理解 |
| 项目以极简可读为最高优先级 | 保证上手与维护成本可控，避免过度设计 |
| 代码需要面向 AI 持续开发友好 | 提升未来自动化接续与协作效率 |
| 提前规划多框架分支演进 | 降低后续技术路线扩展阻力 |
| 后端接口方法统一为 `GET` / `POST` | 统一联调与网关约束，降低跨端方法分歧 |
| token 会话状态落 Redis，并复用 JWT TTL | 支持自动失效与服务端可控登出 |
| 热点缓存配合延迟双删 | 提升查询性能并降低写后脏读概率 |
| 审计筛选在后端执行 | 避免前端仅筛当前页，保证分页总数与查询条件一致 |
| 先补认证关键路径单测 | 优先稳住登录会话链路，降低后续演进回归风险 |
| 审计服务先覆盖分页与筛选入参 | 快速建立日志查询链路的可回归基线 |
| 控制器测试改用 standalone MockMvc | 避免安全/MyBatis 上下文干扰，聚焦接口行为验证 |
| 服务层统一接口化并落地 impl 包 | 规范分层边界并降低实现替换成本 |
| framework 服务同样接口化 | 保持全项目分层规范一致性，避免后续新代码混用模式 |
| 增加结构守卫脚本并接入 CI | 通过自动化门禁防止分层规范回退 |
| 发布阶段先补文档闭环再打版本 | 降低发布信息缺失和协作偏差风险 |
| v0.2 首批数据权限仅落三种范围 | 复用现有 `dept_id` 与用户 ID，先做最小闭环再扩展子部门/自定义部门 |
| 用户管理作为首个数据权限试点模块 | 现有表结构已具备 `dept_id`，改造成本低且易于验证 |
| 部门管理先交付最小 CRUD | 先让 `dept_id` 有真实业务承载，再决定是否继续做部门树级联与数据权限联动 |
| 数据权限测试优先落服务层单测 | 当前项目已有 Mockito 风格服务测试，先覆盖核心分支再补更重的集成测试 |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| 工具调用反馈出现乱码文本 | 判断为调用协议显示异常，不影响项目文件内容，已继续执行 |

## Resources
- `README.md`
- `sql/jianqing-init-v0.1.sql`
- `sql/patch/20260305_permission_fix.sql`
- `.github/workflows/backend-ci.yml`
- `pom.xml`
- `checkstyle.xml`
- `task_plan.md`
- `progress.md`
- `ARCHITECTURE.md`
- `src/main/java/com/jianqing/module/system/mapper/SysUserMapper.xml`
- `src/main/java/com/jianqing/module/system/mapper/SysRoleMapper.xml`
- `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.xml`
- `src/main/java/com/jianqing/integration/config/DynamicConfigGateway.java`
- `src/main/java/com/jianqing/integration/config/LocalDynamicConfigGateway.java`
- `src/main/java/com/jianqing/integration/mq/MessageBusGateway.java`
- `src/main/java/com/jianqing/integration/mq/InMemoryMessageBusGateway.java`
- `/Users/majiaju/Person/code/jianqing/jianqing-admin-web`
- `README.md`
- `CONTRIBUTING.md`
- `docs/API_EXAMPLES.md`
- `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.java`
- `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.xml`
- `src/main/java/com/jianqing/module/system/service/SystemService.java`
- `sql/jianqing-init-v0.1.sql`
- `src/main/java/com/jianqing/module/system/controller/SystemController.java`
- `src/main/java/com/jianqing/framework/cache/CacheProperties.java`
- `src/main/java/com/jianqing/framework/cache/RedisCacheConfig.java`
- `src/main/java/com/jianqing/framework/cache/CacheConsistencyService.java`
- `src/main/java/com/jianqing/framework/security/TokenSessionService.java`
- `src/main/resources/application.yml`
- `docs/RELEASE_CHECKLIST.md`
- `docs/SCOPE_v0.1_v0.2.md`
- `docs/RELEASE_NOTES_v0.1.md`
- `docs/RELEASE_SOP.md`
- `src/test/java/com/jianqing/framework/security/JwtTokenServiceTest.java`
- `src/test/java/com/jianqing/framework/security/TokenSessionServiceTest.java`
- `src/test/java/com/jianqing/module/audit/service/AuditLogServiceTest.java`
- `src/test/java/com/jianqing/module/auth/controller/AuthControllerTest.java`
- `src/test/java/com/jianqing/module/audit/controller/AuditControllerTest.java`
- `src/main/java/com/jianqing/module/auth/service/AuthService.java`
- `src/main/java/com/jianqing/module/auth/service/impl/AuthServiceImpl.java`
- `src/main/java/com/jianqing/module/audit/service/AuditLogService.java`
- `src/main/java/com/jianqing/module/audit/service/impl/AuditLogServiceImpl.java`
- `src/main/java/com/jianqing/module/system/service/SystemService.java`
- `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java`
- `src/main/java/com/jianqing/framework/security/JwtTokenService.java`
- `src/main/java/com/jianqing/framework/security/impl/JwtTokenServiceImpl.java`
- `src/main/java/com/jianqing/framework/security/TokenSessionService.java`
- `src/main/java/com/jianqing/framework/security/impl/TokenSessionServiceImpl.java`
- `src/main/java/com/jianqing/framework/cache/CacheConsistencyService.java`
- `src/main/java/com/jianqing/framework/cache/impl/CacheConsistencyServiceImpl.java`
- `scripts/check-service-structure.sh`
- `.github/workflows/backend-ci.yml`
- `scripts/pre-commit-check.sh`
- `scripts/install-git-hooks.sh`
- `scripts/check-http-method-constraints.sh`
- `scripts/check-mapper-layering.sh`
- `src/main/java/com/jianqing/module/system/controller/SystemController.java`
- `src/main/java/com/jianqing/framework/web/OperationLogInterceptor.java`

## Visual/Browser Findings
- 本轮无网页或图像类输入。

---
*Update this file after every 2 view/browser/search operations*
*This prevents visual information from being lost*
