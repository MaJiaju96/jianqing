# Task Plan: 简擎后端开源路线（v0.1）

## Goal
在保留后台管理系统高效率开发体验的前提下，完成 `简擎` 的可开源后端内核：以 `MySQL` 为首发数据源，预留 `Elasticsearch`、`Nacos`、`RocketMQ` 的可插拔集成能力。

## Current Phase
Phase 6

## Phases

### Phase 1: 需求对齐与差异化定位
- [x] 明确项目目标：个人开源后端管理系统
- [x] 锁定首发约束：数据库优先使用 MySQL
- [x] 识别后续集成目标：ES、Nacos、RocketMQ
- [x] 输出“简擎”特色方向（轻量内核 + 可插拔）
- **Status:** complete

### Phase 2: 架构与边界规划（MVP）
- [x] 建立 planning-with-files 三文件机制
- [x] 定义 v0.1 模块边界（认证、RBAC、审计）
- [x] 细化模块拆分与依赖关系
- [x] 明确统一扩展点（事件、SPI、配置）
- **Status:** complete

### Phase 3: 基础能力落地（MySQL First）
- [x] 完成登录与 JWT 鉴权
- [x] 完成用户/角色/菜单权限模型
- [x] 完成统一响应、异常处理与操作日志
- [x] 提供初始化 SQL 与本地启动说明
- **Status:** complete

### Phase 4: 集成预留层设计
- [x] 设计搜索适配层（ES Connector）
- [x] 设计配置中心适配层（Nacos Config）
- [x] 设计消息总线适配层（RocketMQ Producer/Consumer）
- [x] 保证未接入时核心模块可独立运行
- **Status:** complete

### Phase 5: 开源化与工程化完善
- [x] 完善 README（定位、路线图、快速开始）
- [x] 增加贡献规范与分支策略
- [x] 增加 API 文档与示例调用
- [x] 补齐质量门禁（基础测试/CI）
- [x] 补齐静态检查（Checkstyle）
- **Status:** complete

### Phase 6: 发布准备
- [x] 完成发布前 checklist
- [x] 标注 v0.1 scope 与 v0.2 计划
- [x] 输出首次开源发布说明
- **Status:** complete

## Key Questions
1. v0.1 是否只交付后端内核（不绑定前端）？
2. 鉴权方案选 `Spring Security + JWT` 还是 `Sa-Token`？
3. 插件机制优先用 Spring SPI 还是独立模块装配器？
4. ES/Nacos/RocketMQ 在 v0.1 做“可接入”还是“默认接入”？

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| 项目命名为 `简擎` | 保持品牌统一，便于开源传播 |
| 数据库首发使用 MySQL | 与目标用户习惯一致，上手成本低 |
| 先做管理内核再扩展集成 | 保证核心能力先稳定，降低早期复杂度 |
| 预留 ES/Nacos/RocketMQ 适配层 | 避免后续重构核心域模型 |
| 使用 planning-with-files 管理执行上下文 | 减少重复沟通与需求漂移 |
| 新增 ARCHITECTURE.md 作为实现蓝图 | 降低实现期分歧，明确包结构与扩展边界 |
| 后端编码遵循阿里巴巴 Java 开发规范 | 统一编码风格和工程可维护性 |
| Mapper 尽量使用 XML 承载 SQL | 降低注解 SQL 复杂度并提升可维护性 |
| Mapper.xml 与 Mapper 接口同目录放置 | 保持映射文件就近维护，便于定位与审查 |
| 前端与后端保持平级目录 | 便于独立构建、部署与仓库边界管理 |
| 前后端统一收拢到 `code/jianqing` 工作区 | 便于跨前后端联动且不影响其它项目 |
| 前端默认使用纯 JavaScript | 降低入门门槛，减少 TS 维护成本 |
| 关键/复杂逻辑必须写注释 | 提升可读性与后续维护效率，降低理解成本 |
| 项目坚持“极简可读”实现原则 | 优先简单直接逻辑，避免炫技式复杂实现 |
| 面向 AI 协作友好 | 代码结构清晰、语义直白，便于 AI 接续开发 |
| 预留多技术分支演进路线 | 支持前后端多框架分支与企业级扩展规划 |
| 后端接口方法仅允许 `GET` / `POST` | 统一接口约束与协作规范，避免 `PUT` / `DELETE` 漂移 |
| 引入 Redis 承载 token 会话与热点缓存 | 支撑可配置 token TTL 与高频查询缓存能力 |
| DB 与缓存一致性采用延迟双删 | 降低并发读写下的缓存脏读窗口 |
| 服务层统一采用“接口 + impl”结构 | 明确契约边界，提升可测试性与后续演进稳定性 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| 工具调用出现乱码/参数异常 | 1 | 重新发起调用并补齐必填参数后继续 |

## Notes
- 每完成一个 Phase 都同步更新状态。
- 每次关键技术决策先回看本计划文件。
- 错误先记录再修复，避免重复踩坑。
- SQL 编写规范：优先 `Mapper.xml`，并与对应 `Mapper` 接口同目录。
- 前端工程规范：`jianqing-admin-web` 与 `jianqing-backend` 保持平级目录。
- 前端语法规范：默认使用纯 `JavaScript`，不引入 `TypeScript`。
- 注释规范：不要求每行注释，但关键分支、复杂流程、边界处理必须写清楚注释。
- 核心编码原则：在满足业务需求前提下，优先最简单、最直接、最易读的实现。
- 技术规范原则：后端严格遵循阿里巴巴 Java 开发规范，保持统一风格。
- 演进规划原则：代码需为后续 `TS`/`Vue2`、`SpringBoot2`/`SpringCloud`/`JDK8` 等分支保留可迁移性。
- 接口约束原则：后端对外接口仅允许 `GET` 与 `POST`，不新增 `PUT` / `DELETE`。
- 方法约束守卫：通过脚本与 CI/pre-commit 自动扫描，禁止引入 `PUT` / `DELETE`。
- 缓存一致性原则：写操作统一执行“先删缓存 + 延迟再删”策略，避免 DB 与缓存短暂不一致。
- 服务层规范：模块内服务必须定义 `service` 接口，并在 `service.impl` 提供 `*ServiceImpl` 实现；控制器与调用方仅依赖接口。
- framework 层同名 `*Service` 亦遵循“接口 + impl”模式，避免出现分层规范例外。
- 推荐安装本地 pre-commit hook，提交前自动执行结构守卫与基础质量门禁。
- 分层约束原则：`Mapper` 仅允许在 `service.impl` 与 `mapper` 自身定义中出现，其他层禁止直接引用。
- MyBatis-Plus 约束：核心业务 `service` 接口统一继承 `IService<T>`，实现类统一继承 `ServiceImpl<M, T>`。
- 跨实体协作约束：在 `service.impl` 中优先通过对应 `service` 协作，避免跨模块直接拼装持久层调用。
- 认证审计协作约束：登录日志写入由审计服务统一承担，认证服务不得直接依赖登录日志 mapper。
