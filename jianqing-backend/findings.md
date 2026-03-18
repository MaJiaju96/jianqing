# Findings & Decisions

## Requirements
- 项目名：`简擎`
- 目标：打造个人开源 Java 后端管理系统
- 参考对象：`RuoYi`、`Yudao`
- 约束：数据库先使用 `MySQL`
- 后续集成目标：`Elasticsearch`、`Nacos`、`RocketMQ`
- 过程要求：使用“轻量摘要优先 + planning / memory 分层按需展开”模式，减少重复对话和上下文噪音
- 核心前提：项目定位为“极简、友好、可演进”的 Java 管理系统
- 编码前提：后端严格按阿里巴巴 Java 开发规范执行
- 实现前提：优先简单易读逻辑，避免炫技式复杂实现
- 协作前提：代码需对后续开发者与 AI 协作都友好
- 规划前提：需提前考虑多技术分支（前端 TS/Vue2，后端 SpringBoot2/SpringCloud/JDK8）

## Research Findings
- 已确认系统通知后端首期最稳模型为 `jq_sys_notice + jq_sys_notice_target + jq_sys_notice_user`，其中通知定义与用户收件箱分层，便于同时承接定时发布与已读状态。
- 当前后端通知接口已按既有 system 模块风格接入 `/api/system`，并继续保持 `GET/POST` 约束，不额外拆 controller 风格。
- 应用内定时发布已通过 `@EnableScheduling + NoticePublishScheduler` 落地，首期无需引入 MQ 即可补齐 `PENDING -> PUBLISHED` 链路。
- 我的消息详情接口当前采用“打开即标已读”，与消息中心常见交互一致，额外保留显式 `read` 与 `read-all` 接口。
- 通知管理前端虽然已接 `system:notice:*` 细粒度显隐，但若后端菜单种子不补齐，管理员 `auth/me` 仍拿不到这些权限，页面按钮会持续被隐藏。
- 当前最稳的消息中心菜单补种方式仍是启动期 initializer：补 `消息中心(/messages)` 根菜单、`我的消息`、`通知管理` 与 `system:notice:add/edit/publish/cancel/delete` 按钮权限，再为管理员自动绑定。
- 旧库里的 `jq_sys_notice.status` 可能仍是整型遗留字段；仅补列不够，必须在 schema initializer 中补做 `MODIFY COLUMN` 类型归一，否则 `DRAFT/PENDING` 写入会触发 `Incorrect integer value`。
- 当前后端真实烟测已确认通知链路可用：`POST /api/system/notices` -> `publish` -> `GET /api/system/my-notices/unread-count` -> `GET /api/system/my-notices/{id}` 可形成未读到已读闭环。
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
- 已完成一轮最新真实账号联调复核：`dept_user` 仅可见本部门用户（当前为 admin/test/dept_user/self_user/other_user），`self_user` 仅可见自己；`outside_user` 当前数据库实际挂载角色为 `Test_User`（`dataScope=ALL`），因此可见全部 6 人，不应再按“跨部门受限账号”理解。
- 当前开发联调测试账号口径已统一：`admin/admin123`，其余测试账号统一使用 `test123`。
- 已确认 CRUD 代码生成器 MVP 的最稳边界：仅支持单表，首版只做元数据 + 预览/下载，不直接写入仓库。
- 当前后端已新增 `module/dev` 代码生成器骨架，先落 `GeneratorMetadataService` 与 `/api/dev/gen/tables`、`/tables/{tableName}/columns` 两个元数据接口。
- 元数据查询采用当前数据源的 `information_schema`，以数据库 catalog 作为 schema 名，兼容项目当前 MySQL First 约束。
- 元数据字段首批统一返回：字段名、注释、`dataType`、`columnType`、推导 `javaType`、是否可空、是否主键、是否自增，用于后续 preview 模板直接消费。
- 当前仓库虽已存在 `jq_sys_dict_type`、`jq_sys_dict_data`、`jq_sys_config` 表，但尚无实际模块实现，因此“代码生成器优先于字典/参数页实现”的排序仍成立。
- 本地 `jdtls` 未安装，无法使用 Java LSP 诊断；本轮改以 `mvn test` + `mvn checkstyle:check` 作为编译与静态校验兜底，结果均通过。
- 已完成 `POST /api/dev/gen/preview` 与 `POST /api/dev/gen/download`，首版返回后端模板文件预览并支持 ZIP 下载。
- 预览输出当前包含 9 份文件：entity、saveRequest、summary、mapper、mapper.xml、service、serviceImpl、controller、菜单 SQL。
- 生成模板首版已对齐现有约束：接口方法仅 `GET/POST`、`IService + ServiceImpl`、Mapper XML 共置、默认软删字段 `is_deleted` 优先走逻辑删除。
- 生成控制器路由已改为 `/api/{module}/{business}`，与现有模块化路由风格保持一致。
- preview 服务当前要求表存在主键，否则直接拒绝生成，避免输出不可执行的 CRUD 模板。
- 已同步补齐生成器菜单 seed：初始化 SQL 新增“代码生成”菜单与查询权限，另补 `sql/patch/20260311_generator_menu.sql` 兼容已有数据库。
- 为避免破坏原有初始化脚本的硬编码父子 ID，本轮将“代码生成”菜单插入移动到现有 system/audit 初始化段尾部，避免把历史菜单 ID 整体顶乱。
- README 仍然声明本地默认后端依赖 `DB_URL=jdbc:mysql://127.0.0.1:3306/jianqing...` 与 `REDIS_HOST=127.0.0.1`；但本轮重启后端时，当前机器上的 `127.0.0.1:3306` 实际不可达，因此无法恢复真实后端联调链路。
- 本轮浏览器回归改为 mock 模式执行：已通过 Playwright mock 登录、`/api/dev/gen/tables`、`/columns`、`/preview`、`/download`，确认生成器页最小交互链路可走通。
- 环境恢复后已完成真实后端验证：`/api/auth/login` 与 `/api/dev/gen/tables` 均已返回成功，当前真实可选表包括 `jq_sys_dept`、`jq_sys_user`、`jq_sys_config` 等系统表。
- 代码生成器现已补齐“前后端+SQL”三件套输出：除后端 8+1 文件外，新增前端 API 模板、业务列表页模板与路由接入片段。
- 当前 preview/download 总输出文件数提升为 12 个：后端 8 个、前端 3 个、SQL 1 个。
- 前端模板字段控件映射已增强：`status` 走下拉、`LocalDate/LocalDateTime/LocalTime` 走日期时间控件、长文本/备注描述字段走 textarea、`Boolean` 走 switch、数值字段走 input-number。
- 已完成生成器参数治理：后端对 `tableName/moduleName/businessName/className/permPrefix` 统一做 `trim + pattern` 校验，并在 preview/download/write 全链路透传规范化参数，减少命名与路径漂移。
- 已完成生成器写入安全增强：`/api/dev/gen/write` 默认先做目标文件冲突检测，检测到已存在文件时阻断写入并返回可读提示；需要覆盖时显式传 `overwrite=true`。
- 已新增冲突清单查询接口：`POST /api/dev/gen/write/conflicts`，可在真正写盘前返回将被覆盖的文件路径列表，便于前端先展示风险再确认。
- 已新增生成标记能力：`/api/dev/gen/write` 写入成功后返回 `markerId` 并落地标记文件，后续可通过标记精确定位该次写入文件集合。
- 已新增按标记快速删除接口：`POST /api/dev/gen/write/delete-by-marker`，可在不改动模板内容的前提下回滚本次生成写入。
- 已新增写入记录落库：仅当用户点击“写入项目”时记录 marker 与生成参数快照，未写入场景不入库。
- 已新增写入记录查询接口：`GET /api/dev/gen/write/records`，供前端快速删除列表优先展示服务端记录。
- 写入记录查询已支持过滤：可按 `tableName` 与 `startAt/endAt` 筛选，便于在高频生成场景快速定位目标 marker。
- 已补齐字典功能最小闭环：新增 `jq_sys_dict_type` / `jq_sys_dict_data` 对应实体、Mapper、Service、Controller 与前端联调接口。
- 字典类型当前支持列表、新增、编辑、删除；若该类型下仍存在字典数据则禁止删除，避免脏引用。
- 字典数据当前支持按类型列表、新增、编辑、删除，并新增 `GET /api/system/dict-options/{dictType}` 供后续业务侧按类型读取启用项。
- 字典类型编码服务端已收口校验：仅允许小写字母、数字、下划线，且需以字母开头。
- 字典类型编码变更时，后端会在同一事务内同步更新对应字典数据表中的 `dict_type`，保持类型与数据一致。
- 已新增字典权限 seed/patch：补齐 `system:dict:list/query/add/edit/remove` 与 `system:dict-data:add/edit/remove`。
- 已补齐首批业务消费字典 seed：`sys_common_status`、`sys_menu_visible`、`sys_dept_status`，用于驱动系统页真实下拉与展示文案。
- 已新增旧库 patch `sql/patch/20260313_dict_seed_data.sql`，避免旧环境缺少默认字典项导致前端下拉为空。
- 已继续补齐审计页字典 seed：`audit_exec_status`（成功/失败）与 `audit_login_type`（登录方式）。
- 已新增旧库 patch `sql/patch/20260313_audit_dict_seed_data.sql`，用于为历史环境补齐审计字典项。
- 已为字典查询增加缓存：字典类型列表、按类型字典数据、按类型启用字典项均已进入缓存层。
- 字典写操作已接入提交后缓存失效：新增/编辑/删除字典类型、字典数据后，会精确清理对应缓存 key。
- 字典类型改名与字典数据跨类型修改时，会同时清理旧类型与新类型缓存，避免前端读到旧值。
- 已补齐参数设置最小闭环：新增 `jq_sys_config` 对应实体、Mapper、Service、Controller 与前端联调接口。
- 参数模块当前支持列表、新增、编辑、删除；删除时禁止删除内置参数，编辑时内置参数禁止修改参数键。
- 参数写操作当前会同步调用 `DynamicConfigGateway.publish`，使本地动态配置网关可立即感知变更。
- 已为参数列表增加缓存，并在参数写操作后接入提交后失效。
- 已新增默认参数 seed：`sys.theme.default`、`sys.login.captcha.enabled`。
- 已新增旧库 patch `sql/patch/20260313_config_menu_and_seed.sql`，补齐参数 seed 与按钮权限。
- 参数模块已补齐 `config_group` 字段，当前使用如 `UI`、`AUTH`、`DEFAULT_GROUP` 这类大写分组标识。
- 参数发布已改为按 `config_group` 推送到 `DynamicConfigGateway.publish(dataId, group, value)`，不再固定使用 `DEFAULT_GROUP`。
- 前端参数页已支持按分组筛选，并在编辑/新增时维护参数分组。
- 已补齐参数变更历史：新增 `jq_sys_config_history`，参数新增/修改/删除都会自动留痕。
- 已新增参数历史查询接口：`GET /api/system/configs/{id}/history`，前端可查看单个参数的最近变更轨迹。
- 已新增参数回滚接口：`POST /api/system/configs/{id}/history/{historyId}/rollback`。
- 当前回滚能力仅支持回滚到非删除态历史快照；删除记录暂不支持直接恢复已删参数。
- 参数回滚成功后会重新发布到 `DynamicConfigGateway`，并额外记录一条 `ROLLBACK` 类型历史。
- 已新增参数 diff 接口：`GET /api/system/configs/{id}/history/{historyId}/diff`。
- 当前 diff 首版聚焦字段级对比：参数名称、参数分组、参数值、值类型、内置状态。
- 已新增已删除参数历史查询接口：`GET /api/system/configs/deleted/history`，仅返回当前仍未恢复的删除记录，避免恢复后重复展示。
- 已新增删除历史恢复接口：`POST /api/system/configs/history/{historyId}/restore`，会按删除快照重建参数、重新发布动态配置并记录一条 `RESTORE` 历史。
- 参数恢复当前按 `config_key` 做冲突保护：若同键参数已存在，则恢复会被拒绝，避免覆盖现有配置。
- 已扩展参数 diff 接口：`GET /api/system/configs/{id}/history/{historyId}/diff?compareHistoryId=7`，当前同时支持“当前 vs 指定历史”和“历史 vs 历史”。
- 参数 diff 响应已补齐对比目标元数据：包含 `compareHistoryId`、`compareChangeType`、`compareCreatedAt` 与 `compareWithCurrent`，前端可据此切换展示文案。
- 已新增删除历史恢复预览接口：`GET /api/system/configs/deleted/history/{historyId}/preview`，用于在真正恢复前查看删除快照详情。
- 恢复预览接口现已补充同键占用信息：若当前存在同 `config_key` 参数，会返回占用标记、当前参数信息及字段级差异列表。
- 已补齐参数值类型默认字典：`sys_config_value_type`，当前包含 `string/number/boolean/json` 四类选项，可直接供参数页消费。
- 已补齐字典颜色类型默认字典：`sys_dict_color_type`，当前包含默认/主色/成功/信息/警告/危险六类选项，可直接供字典管理页消费。
- 已补齐参数来源默认字典：`sys_config_source`，当前包含 `0=自定义`、`1=内置` 两类选项，可直接供参数页消费。
- `sys_common_status` 已继续扩展到字典管理页，当前字典类型/字典数据的状态筛选、表单和状态标签均可直接复用该字典。
- 旧库仍可能停留在参数模块上线前版本，`jq_sys_config` 缺少 `config_group/value_type/is_builtin` 时会直接导致参数页列表查询失败。
- 已新增启动期 schema 自修复：服务启动时会自动补齐 `jq_sys_config`、`jq_sys_config_history` 的参数扩展字段，并在缺失历史表时自动建表。
- 旧库若未执行 `20260312_generator_write_record.sql`，代码生成器写入记录查询/回滚链路会因缺少 `jq_dev_gen_write_record` 表而失败。
- 已新增生成器启动期 schema 自修复：服务启动时会自动补齐 `jq_dev_gen_write_record`，避免代码生成页因旧库缺表直接报错。
- 旧库若未执行 `20260311_generator_menu.sql`，前端即使已有生成器页面，也会因为缺少菜单与角色绑定而看不到入口。
- 已新增生成器菜单启动期自修复：服务启动时会自动补齐 `system:generator:list/query` 菜单，并为管理员补齐角色菜单关联。
- 旧库若 `jq_sys_menu` 缺少 `route_path/component/perms/is_deleted` 等核心字段，菜单查询、补种子和权限绑定都会同时失效。
- 已新增菜单 schema 启动期自修复：服务启动时会自动补齐 `jq_sys_menu` 核心字段，并在缺失时补齐 `jq_sys_role_menu` 表。
- 旧库若缺少 `jq_sys_dict_type/jq_sys_dict_data` 或其核心字段，字典管理、系统状态文案和前端 dict-options 消费都会失效。
- 已新增字典 schema 启动期自修复：服务启动时会自动补齐字典类型表、字典数据表及其核心字段。
- 旧库若缺少 `jq_sys_dept` 或 `leader_user_id/sort_no/status/is_deleted` 等核心字段，部门树查询和用户部门选择链路都会失效。
- 已新增部门 schema 启动期自修复：服务启动时会自动补齐部门表及其核心字段。
- `jq_sys_oper_log` 原始建表使用 JSON 字段，在更老的 MySQL/MariaDB 环境中兼容性较差，容易导致审计表初始化失败。
- 已新增审计 schema 启动期自修复：服务启动时会自动补齐操作日志/登录日志表及其核心字段，并将请求/响应体按 LONGTEXT 兼容方案创建。
- 旧库若缺少 `jq_sys_user/jq_sys_role/jq_sys_user_role` 或其核心字段，登录鉴权、用户管理、角色分配和 RBAC 联动都会直接失效。
- 已新增用户角色 schema 启动期自修复：服务启动时会自动补齐用户表、角色表、用户角色关联表及其核心字段。
- 已新增旧库兼容说明文档：`docs/LEGACY_SCHEMA_COMPATIBILITY.md`，集中说明当前已覆盖范围、使用方式、排障顺序与已知边界。
- 初始化脚本库级 collation 已从 `utf8mb4_0900_ai_ci` 调整为 `utf8mb4_unicode_ci`，减少因 MySQL 8 专属方言导致的旧环境初始化失败风险。
- 初始化脚本中的自动时间戳字段已优先从 `DATETIME DEFAULT CURRENT_TIMESTAMP` 调整为 `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`，降低旧 MySQL 环境初始化兼容风险。
- 参数设置真实浏览器回归中发现：`GET /api/system/configs/deleted/history` 在旧 collation 混用环境下会因 `jq_sys_config` 与 `jq_sys_config_history.config_key` 比较报 `Illegal mix of collations`。
- 已在 `SysConfigHistoryMapper.xml` 中将删除历史查询里的 `config_key` 比较显式收口为 `utf8mb4_unicode_ci`，避免新旧表/旧库 patch 混用时恢复链路直接报错。
- 参数页真实回归已确认“双历史 diff + 历史回滚”链路可用：历史 vs 历史对比接口与回滚接口均返回 200，回滚后列表值与历史记录同步刷新。
- 生成器真实回归已确认“写入项目 → 写入记录 → 按 marker 删除”链路可用；删除后对应写入记录消失，工作区也未残留本次生成文件。
- 生成器冲突确认真实回归已确认：重复写入时会先拉取冲突清单，再展示目录统计、快捷过滤与覆盖确认，不会直接静默覆盖。
- 本轮前端真实回归还确认了字典页与参数页的筛选、重置、每页条数切换在空列表/单条数据场景下均可正常工作。
- 数据权限现已从 `全部/本部门/本人` 扩展到 `全部/本部门及以下/本部门/本人`，角色保存与前端展示均已对齐新范围。
- 后端部门服务已补齐 `listSelfAndDescendantDeptIds`，当前通过拉取启用部门列表并按父子关系递归收集子树 ID，供数据权限直接复用。
- `UserDataScopeResolver` 当前已改为基于可访问部门 ID 列表做用户列表过滤、访问判断与用户写操作校验；`DEPT` 走单部门列表，`DEPT_AND_CHILD` 走部门子树列表。
- 真实浏览器回归已确认：角色编辑弹窗中可见“本部门及以下数据”选项，保存后角色列表会正确展示“本部门及以下”，且控制台 warning/error 为 0。
- 本轮真实回归使用既有 `dept_scope_role` 做最小验证，保存后已恢复为原始“本部门数据”，避免污染联调基线。
- 当前真实部门树为：`简擎总部(1) -> 外部协作部(3)`；其中 `outside_user` 位于子部门 3，可作为“子部门可见”样本。
- 本轮额外通过真实后端临时创建了树外根部门与树外用户，并验证 `dept_scope_role` 切到 `DEPT_AND_CHILD` 后，`dept_user` 可见 `outside_user`，但不可见该树外用户。
- 真实联调结束后已确认：临时树外部门/用户已删除，`dept_scope_role.dataScope` 已恢复为 `DEPT`，当前用户与部门基线未被污染。
- `DeptServiceImpl.listSelfAndDescendantDeptIds` 现已补单测，确认只会收集当前子树内部门，不会混入并列分支或树外根节点。
- `UserDataScopeResolver` 现已补树形边界单测：`ALL` 会优先覆盖 `DEPT_AND_CHILD`，`DEPT_AND_CHILD` 会拒绝访问树外用户，也会拒绝把用户迁移到树外部门。
- 真实前端回归已确认：当 `dept_scope_role` 临时切到 `DEPT_AND_CHILD` 时，`dept_user` 登录用户页可见 6 条用户数据，其中包含子部门用户 `outside_user`。
- 本轮用户页真实回归后控制台 warning/error = 0，且 `dept_scope_role` 已恢复为原始 `DEPT` 配置。
- 当前 token 消耗偏高的核心原因不是业务代码本身，而是“续开发协议”默认读取双端 6 个 planning 文件；随着 phase 和回归记录增长，该流程会持续放大上下文成本。
- 本轮已为前后端新增 `current_state.md`，并将工作区续开发协议改为“优先读轻量摘要、按侧展开、必要时才读完整 planning”，在不影响代码质量的前提下降低默认上下文体积。

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
| 后端热点重构优先采用“聚合入口 + 轻量执行器” | 在保持行为不变前提下降低主服务复杂度，避免继续堆补丁 |
| 先修正确性再做抽象收口 | 事务边界、缓存提交后失效、安全配置优先于进一步模式化优化 |
| 代码生成器元数据阶段优先读取 `information_schema` | 简单直接，足够支撑 MySQL 首发场景，避免过早抽象多数据库适配 |
| 代码生成器模板首版先固定输出后端 8+1 文件 | 先验证主干模板质量，再决定是否继续扩展前端页与更多 SQL 片段 |
| 生成器参数先做服务端规范化再参与模板渲染 | 避免前端输入空白或格式差异导致路径、文件名和权限前缀不一致 |
| 生成器写盘默认禁止静默覆盖 | 优先保护现有代码安全，覆盖行为必须显式确认 |
| 冲突清单查询与写入动作分离 | 让前端在写盘前可视化风险，避免“先失败再提示”的被动体验 |
| 生成标记与模板内容解耦 | 避免向模板注入额外注释导致代码风格漂移，同时支持可追溯删除 |
| 仅对“写入项目”行为落库 | 区分草稿态与落盘态历史，避免无效数据污染数据库 |
| 写入记录查询提供轻量过滤能力 | 降低历史记录检索成本，提升按 marker 回滚效率 |
| 字典功能首版先做管理侧最小闭环 | 先补齐类型/数据维护与基础消费接口，再决定是否扩展缓存、批量导入导出 |
| 参数删除恢复先走“删除历史列表 + 按历史恢复” | 已删除参数不在当前列表中，先补单独入口可形成最小闭环，避免改造现有历史弹窗语义 |
| 参数多版本 diff 先复用现有 diff 接口加可选基准历史参数 | 避免新增第二套对比接口，保持“当前 vs 历史”和“历史 vs 历史”走同一返回结构 |
| 参数恢复预览先直接返回删除历史快照 | 现阶段恢复目标就是按删除快照重建参数，直接复用删除历史详情即可满足预览需求 |
| 参数恢复前差异对比先聚焦同键冲突场景 | 真正的恢复风险主要来自同键已被重新占用，先把占用状态和字段差异展示清楚即可 |
| 字典消费扩面优先挑高频且改动最小的参数值类型 | 可复用现有 dict-options 接口快速替换硬编码枚举，同时风险低、收益直接 |
| 字典颜色类型继续沿用“字典 options + 前端兜底” | 既能逐步去硬编码，也能兼容历史环境尚未补齐 seed 的情况 |
| 参数来源字典化要同步覆盖标签类型 | 避免仍在页面里手写 warning/info 分支，保持展示与配置来源一致 |
| 通用状态字典扩面优先复用现有 `sys_common_status` | 避免为同一组启禁用语义重复造新字典，保持系统内文案和颜色一致 |
| 旧库兼容优先用启动期自修复收口 | 比要求人工先跑 patch 更稳妥，能直接消除“代码已升级但库未升级”导致的页面不可用 |
| 生成器旧库兼容优先补核心写入记录表 | 这是生成器写入、记录查询、按 marker 回滚的共同依赖点，缺表时整条链路都会失效 |
| 生成器入口兼容要同时补菜单和角色绑定 | 只补菜单不补 `jq_sys_role_menu`，管理员依然看不到入口，等于没有真正修复 |
| 菜单旧库兼容优先补“所有菜单都会依赖”的核心字段 | 比只修单个功能菜单更值，因为它同时影响菜单加载、权限、补种子和新增功能接入 |
| 字典旧库兼容优先补核心表结构 | 字典已成为多个前端页面的基础依赖，缺表或缺核心字段会让多处页面同时退化或不可用 |
| 部门旧库兼容优先补树结构与状态核心字段 | 这些字段直接决定部门树能否查询、排序和过滤，缺失时影响整条组织架构链路 |
| 审计旧库兼容优先去掉高版本方言依赖 | 像 JSON 这种类型一旦不兼容，会直接让整张日志表不可建，收益高于只补普通缺列 |
| 用户角色旧库兼容优先补认证与授权基础表 | 这是系统能否登录、分配角色、计算权限的底座，缺表影响面最大 |
| 旧库兼容能力积累到一定规模后要及时文档化 | 否则团队只知道“启动能自修复”，却不知道覆盖范围、边界和排障路径 |
| init SQL 方言风险要优先清理库级定义 | 一旦 `CREATE DATABASE` 就失败，后续所有表级兼容逻辑都没有机会执行 |
| init SQL 时间字段兼容要优先处理自动时间戳列 | 这类列分布广、复用高，一旦方言不兼容会让多张基础表一起初始化失败 |
| 参数恢复链路优先在查询层显式统一 collation | 相比要求用户先全库改 collation，先保证“已删参数列表/恢复”链路可直接工作，风险更低 |
| 数据权限子部门扩展优先复用现有部门树关系 | 当前 `jq_sys_dept` 已具备父子结构，先在服务层递归收集子树 ID，比引入新字段或更重 SQL 方言更稳妥 |

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
- `src/main/java/com/jianqing/framework/security/JwtAuthenticationFilter.java`
- `src/main/java/com/jianqing/module/system/entity/SysDictType.java`
- `src/main/java/com/jianqing/module/system/entity/SysDictData.java`
- `src/main/java/com/jianqing/module/system/service/DictTypeService.java`
- `src/main/java/com/jianqing/module/system/service/DictDataService.java`
- `src/main/java/com/jianqing/module/system/service/impl/DictTypeServiceImpl.java`
- `src/main/java/com/jianqing/module/system/service/impl/DictDataServiceImpl.java`
- `src/main/java/com/jianqing/module/system/mapper/SysDictTypeMapper.java`
- `src/main/java/com/jianqing/module/system/mapper/SysDictTypeMapper.xml`
- `src/main/java/com/jianqing/module/system/mapper/SysDictDataMapper.java`
- `src/main/java/com/jianqing/module/system/mapper/SysDictDataMapper.xml`
- `src/test/java/com/jianqing/module/system/controller/SystemControllerTest.java`
- `src/test/java/com/jianqing/module/system/service/impl/DictTypeServiceImplTest.java`
- `src/test/java/com/jianqing/module/system/service/impl/DictDataServiceImplTest.java`
- `sql/patch/20260313_dict_permissions.sql`
- `sql/patch/20260313_dict_seed_data.sql`
- `sql/patch/20260313_audit_dict_seed_data.sql`
- `src/main/java/com/jianqing/module/system/service/impl/SystemCacheEvictor.java`
- `src/test/java/com/jianqing/module/system/service/impl/SystemCacheEvictorTest.java`
- `src/main/java/com/jianqing/module/system/entity/SysConfig.java`
- `src/main/java/com/jianqing/module/system/service/ConfigService.java`
- `src/main/java/com/jianqing/module/system/service/impl/ConfigServiceImpl.java`
- `src/main/java/com/jianqing/module/system/mapper/SysConfigMapper.java`
- `src/main/java/com/jianqing/module/system/mapper/SysConfigMapper.xml`
- `src/main/java/com/jianqing/module/system/entity/SysConfigHistory.java`
- `src/main/java/com/jianqing/module/system/dto/ConfigHistorySummary.java`
- `src/main/java/com/jianqing/module/system/mapper/SysConfigHistoryMapper.java`
- `src/main/java/com/jianqing/module/system/mapper/SysConfigHistoryMapper.xml`
- `src/test/java/com/jianqing/module/system/service/impl/ConfigServiceImplTest.java`
- `sql/patch/20260313_config_menu_and_seed.sql`
- `src/main/java/com/jianqing/module/system/dto/ConfigDiffItem.java`
- `src/main/java/com/jianqing/module/system/dto/ConfigDiffSummary.java`

## Additional Findings
- 已完成后端首轮热点扫描：`SystemServiceImpl`（391 行）是当前主要复杂度集中点，后续应按“用户域操作/缓存失效/数据范围判定”继续拆分职责。
- 已修复安全过滤器异常吞掉问题：`JwtAuthenticationFilter` 捕获 JWT 解析异常后新增 `warn` 级日志，避免无痕失败导致排障困难。
- 已完成 `SystemServiceImpl` 第二批重构：抽离数据范围判定与校验职责到 `UserDataScopeResolver`，主服务类聚焦编排逻辑。
- 当前后端热点复杂度已初步下降，后续可继续抽离用户写操作与缓存失效策略，进一步压缩主服务体积。
- 已完成 `SystemServiceImpl` 第三批重构：抽离缓存失效职责到 `SystemCacheEvictor`，主服务不再直接编排缓存 key 细节。
- 已完成 `SystemServiceImpl` 第四批重构：新增 `UserWriteOperationHandler`，将用户创建/更新/删除的字段装配、持久化与缓存失效从主服务继续下沉。
- 已补齐系统服务关键路径单测：新增 `UserWriteOperationHandlerTest` 与 `UserDataScopeResolverTest`，覆盖用户写操作缓存失效、密码校验，以及数据范围解析/越权分支。
- 已完成前端系统页列表通用 composable 收口：新增 `useSystemListPage`，统一 users/roles/menus/depts 四个列表页的查询/重置/刷新/分页状态流。
- 已完成前端系统页弹窗表单轻量收口：新增 `useEntityDialogForm`，统一 create/edit 对话框的可见性、编辑态、编辑目标与表单重置/回填流程。
- 已完成前端系统页删除操作轻量收口：新增 `useEntityDeleteAction`，统一删除确认、行级 loading、删除后刷新与成功提示流程。
- 已完成前端系统页保存提交轻量收口：新增 `useEntitySubmitAction`，统一保存时 loading、成功后关闭弹窗、列表刷新与成功提示流程。
- 浏览器真实回归中发现并修复两处前端回归：`useEntityDialogForm.openCreate` 需忽略点击事件对象；`useEntityDeleteAction` 需吞掉 `ElMessageBox.confirm` 的 `cancel/close`，避免控制台未处理警告。
- 已补做数据权限账号回归：在浏览器上下文中完成 `dept_user / self_user / outside_user` 登录鉴权与用户列表数据校验；当前结果为 `dept_user` 可见本部门 5 人、`self_user` 仅见自己、`outside_user` 当前可见 6 人。
- 已澄清测试账号口径：`outside_user` 当前数据库实际挂载角色为 `Test_User(dataScope=ALL)`，因此可见 6 人属于配置现状；不应再把它当作“跨部门受限账号”使用。
- 已完成审计页真实回归：`/audit/oper-logs` 与 `/audit/login-logs` 的查询、重置、分页均通过，当前两页各返回 20 条数据且无异常。
- 已补一轮前端异常处理收口：新增 `ignoreHandledError`，用于吞掉已由全局 HTTP 拦截器提示过的请求异常，避免 system/audit/dashboard 页面在加载失败时继续抛出未处理 Promise 警告。
- 已修复非 admin 账号登录后的 dashboard 噪音：`DashboardView` 改为按权限拉取统计数据，避免低权限账号登录后立即请求无权访问的 roles/menus 接口导致 401 噪音。
- 已完成 `CUSTOM` 数据范围真实接口回归：在默认后端 `127.0.0.1:8080` 上，将 `dept_scope_role` 临时切到 `CUSTOM + [外部协作部(3)]` 后，`dept_user/test123` 查询 `/api/system/users` 仅返回 `outside_user`，验证后已恢复为 `DEPT` 基线。
- 已确认默认联调口径已恢复到新版代码：`127.0.0.1:8080` 可直接保存 `CUSTOM` 数据范围，不再受旧版“四种数据范围”限制。

## Visual/Browser Findings
- 本轮无网页或图像类输入。

---
*Update this file after every 2 view/browser/search operations*
*This prevents visual information from being lost*
