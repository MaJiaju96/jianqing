# Task Plan: 简擎后端开源路线（v0.1）

## Goal
在保留后台管理系统高效率开发体验的前提下，完成 `简擎` 的可开源后端内核：以 `MySQL` 为首发数据源，预留 `Elasticsearch`、`Nacos`、`RocketMQ` 的可插拔集成能力。

## Current Phase
Phase 16

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

### Phase 7: v0.2 数据权限最小闭环
- [x] 明确最小数据权限策略（全部/本部门/本人）
- [x] 将角色数据范围字段接入后端 DTO / 实体 / 服务
- [x] 在用户管理模块落首批数据范围校验
- [x] 补齐部门管理后端最小 CRUD
- [x] 补齐前后端联调与回归测试
- **Status:** complete

### Phase 8: 代码健康治理（去屎山重构）
- [x] 完成后端热点扫描并识别主复杂度集中点（`SystemServiceImpl`）
- [x] 修复 `JwtAuthenticationFilter` 异常吞掉问题，补齐可观测日志
- [x] 抽离数据范围判定职责（`UserDataScopeResolver`）
- [x] 抽离系统缓存失效职责（`SystemCacheEvictor`）
- [x] 继续拆分 `SystemServiceImpl` 用户写操作编排（创建/更新/删除）
- [x] 增补系统服务关键路径单测（缓存失效与数据范围分支）
- [x] 评估并落地前端系统页列表通用 composable（users/roles/menus/depts）
- **Status:** complete

### Phase 9: 架构收口与规范固化
- [x] 清理后端默认敏感配置并补本地示例配置
- [x] 为 system 关键写路径补齐事务边界与提交后缓存失效
- [x] 将 `SystemServiceImpl` 继续收口为“聚合入口 + 轻量执行器”结构
- [x] 补齐 handler/缓存/事务相关单测并完成回归
- [x] 同步更新后端规划文件与统一开发规范，固化后续优化规则
- **Status:** complete

### Phase 10: CRUD 代码生成器 MVP（预览前置）
- [x] 明确生成器 MVP 边界：仅支持单表、先做预览/下载、不直接落盘
- [x] 建立后端 `dev/gen` 模块骨架与元数据 DTO / service / controller
- [x] 提供表列表、字段列表元数据接口（`/api/dev/gen/tables`、`/columns`）
- [x] 实现 preview/download 接口与模板渲染骨架
- [x] 输出首批后端代码模板（entity/mapper/xml/service/controller/dto）
- [x] 输出前端 API / 业务列表页 / 路由片段模板
- [x] 评估并接入前端生成器页最小闭环
- **Status:** complete

### Phase 11: 代码生成器参数治理与稳定性
- [x] 补齐生成参数服务端统一校验与规范化透传（trim + pattern）
- [x] 统一预览/下载/写入链路使用规范化参数，避免路径与命名漂移
- [x] 补充生成器参数规范化相关单测并完成回归
- **Status:** complete

### Phase 12: 代码写入安全增强（冲突保护）
- [x] 写入前检测已存在文件并阻断默认覆盖
- [x] 增加 `overwrite` 显式覆盖开关，避免误写盘
- [x] 补齐冲突保护与覆盖写入测试并完成回归
- **Status:** complete

### Phase 13: 冲突清单查询接口与可视化支撑
- [x] 新增写入前冲突文件清单查询接口
- [x] 复用生成器 preview 产物路径做冲突比对，输出稳定文件列表
- [x] 补齐冲突清单接口与服务测试并完成回归
- **Status:** complete

### Phase 14: 生成标记与按标记快速删除
- [x] 写入项目返回自动生成标记（markerId）并持久化标记文件
- [x] 新增按标记删除接口，支持快速回滚本次生成写入
- [x] 保持生成模板内容不变，仅通过标记元数据实现删除定位
- [x] 补齐控制器与服务测试并完成回归
- **Status:** complete

### Phase 15: 写入记录落库与前端按写入记录删除
- [x] 新增写入记录表与后端查询接口（仅记录已点击“写入项目”的操作）
- [x] 写入成功时自动落库 marker + 配置快照（table/module/business/class/perm）
- [x] 前端快速删除入口优先读取后端写入记录，非写入历史仍保留本地缓存
- [x] 完成前后端回归验证
- **Status:** complete

### Phase 16: 写入记录过滤查询能力
- [x] 写入记录接口增加表名与时间范围过滤参数
- [x] 保持写入记录查询限制与排序一致（limit + 倒序）
- [x] 完成接口过滤回归验证
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
| 热点服务优先按职责分层拆分（解析/编排/失效） | 降低单类复杂度，避免在历史代码上持续叠补丁 |
| `SystemServiceImpl` 定位为聚合入口 | 仅保留访问控制、事务边界与跨域编排，避免再次膨胀 |
| 跨表写先保证事务与提交后缓存失效 | 正确性优先于抽象与性能，减少数据与缓存不一致风险 |
| 代码生成器 MVP 先走“预览/下载，不直接落盘” | 先验证模板与约束一致性，避免首版写盘带来的破坏性风险 |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| 工具调用出现乱码/参数异常 | 1 | 重新发起调用并补齐必填参数后继续 |

## Notes
- 开发联调测试账号：`admin/admin123`；`dept_user/test123`；`self_user/test123`；`other_user/test123`；`outside_user/test123`。
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
- 重构执行约束：先做行为不变的小步拆分（职责抽离），再做接口级重排，避免一次性大改带来回归风险。
- 聚合服务约束：`SystemServiceImpl` 只保留访问控制、事务边界与编排入口，新增复杂写流程优先下沉到 `*WriteOperationHandler` / `*AssignmentHandler`。
- 一致性约束：缓存失效优先在事务提交后触发，不允许新增“事务未完成先删缓存”的写路径。
