# Progress Log

## Session: 2026-03-03

### Phase 1: 需求对齐与差异化定位
- **Status:** complete
- **Started:** 2026-03-03
- Actions taken:
  - 明确了项目目标、参考框架、首发数据库与后续集成方向。
  - 确认采用 planning-with-files 的 plan 工作流。
- Files created/modified:
  - `task_plan.md` (created)
  - `findings.md` (created)
  - `progress.md` (created)

### Phase 2: 架构与边界规划（MVP）
- **Status:** complete
- Actions taken:
  - 已建立三文件持久化上下文。
  - 已形成 v0.1 的阶段计划与技术决策草案。
  - 已输出 `ARCHITECTURE.md`，明确包结构、依赖方向、首批接口与三类集成网关。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)
  - `ARCHITECTURE.md` (created)

### Phase 3: 基础能力落地（MySQL First）
- **Status:** complete
- Actions taken:
  - 基于现有代码与 SQL 完成实施蓝图，进入编码阶段。
  - 确认优先打通：认证登录链路、用户权限查询、日志链路。
  - 已完成登录鉴权、系统查询接口、审计日志查询接口与操作日志拦截。
  - 已按规范将核心 Mapper 的注解 SQL 迁移到 XML。
- Files created/modified:
  - `ARCHITECTURE.md` (reference)
  - `task_plan.md` (updated)
  - `progress.md` (updated)
  - `src/main/java/com/jianqing/module/system/mapper/SysUserMapper.xml` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysRoleMapper.xml` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.xml` (created)
  - `src/main/resources/application.yml` (updated)

### Phase 4: 集成预留层设计
- **Status:** complete
- Actions taken:
  - 已新增搜索网关接口 `SearchGateway` 与默认实现 `NoopSearchGateway`。
  - 已新增通用查询模型 `SearchQuery`、`SearchPageResult`。
  - 已新增动态配置网关 `DynamicConfigGateway` 与本地默认实现 `LocalDynamicConfigGateway`。
  - 已新增消息总线网关 `MessageBusGateway` 与内存默认实现 `InMemoryMessageBusGateway`。
  - 已增加 `integration` 开关配置，默认关闭外部中间件依赖。
- Files created/modified:
  - `src/main/java/com/jianqing/integration/search/SearchGateway.java` (created)
  - `src/main/java/com/jianqing/integration/search/NoopSearchGateway.java` (created)
  - `src/main/java/com/jianqing/integration/search/model/SearchQuery.java` (created)
  - `src/main/java/com/jianqing/integration/search/model/SearchPageResult.java` (created)
  - `src/main/java/com/jianqing/integration/config/ConfigChangeListener.java` (created)
  - `src/main/java/com/jianqing/integration/config/DynamicConfigGateway.java` (created)
  - `src/main/java/com/jianqing/integration/config/LocalDynamicConfigGateway.java` (created)
  - `src/main/java/com/jianqing/integration/mq/MessageHandler.java` (created)
  - `src/main/java/com/jianqing/integration/mq/MessageBusGateway.java` (created)
  - `src/main/java/com/jianqing/integration/mq/InMemoryMessageBusGateway.java` (created)
  - `src/main/resources/application.yml` (updated)

### Phase 5: 开源化与工程化完善
- **Status:** complete
- Actions taken:
  - 已创建前端管理台并调整为与后端平级目录。
  - 已将前端从 TypeScript 全量切换为纯 JavaScript。
  - 已完成前端构建验证，确认可正常输出产物。
  - 已补齐前端 README，降低新成员接入门槛。
  - 已补齐系统管理用户/角色/菜单基础 CRUD。
  - 已补齐用户分配角色、角色分配菜单能力。
  - 已重构后端 README，补齐定位、路线图与快速开始。
  - 已新增贡献规范与分支策略文档。
  - 已新增 API 示例文档，补齐核心接口调用样例。
  - 已修复按钮权限计算链路，避免按钮权限受菜单可见性影响。
  - 已补充审计日志菜单与按钮权限初始化 SQL。
  - 已新增旧库增量修复 SQL，支持在不重置数据情况下补齐审计权限。
  - 已执行后端编译与测试命令，完成当前质量门禁基线验证。
  - 已新增 GitHub Actions CI 工作流，自动执行 `mvn -DskipTests compile` 与 `mvn test`。
  - 已接入 Checkstyle 最小规则集（禁用 `*` 导入、检测未使用导入、禁止 Tab），建立静态检查门禁。
  - 已将 CI 门禁扩展为“编译 + 测试 + Checkstyle 静态检查”。
- Files created/modified:
  - `/Users/majiaju/Person/code/jianqing/jianqing-admin-web` (created)
  - `jianqing-admin-web/README.md` (updated)
  - `jianqing-backend/README.md` (updated)
  - `CONTRIBUTING.md` (created)
  - `docs/API_EXAMPLES.md` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.java` (updated)
  - `src/main/java/com/jianqing/module/system/mapper/SysMenuMapper.xml` (updated)
  - `src/main/java/com/jianqing/module/system/service/SystemService.java` (updated)
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260305_permission_fix.sql` (created)
  - `.github/workflows/backend-ci.yml` (created)
  - `pom.xml` (updated)
  - `checkstyle.xml` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 6: 发布准备
- **Status:** complete
- Actions taken:
  - 已进入发布准备阶段，待补齐发布前 checklist、v0.1/v0.2 范围说明与首版发布说明。
  - 已完成接口方法收敛：系统管理写操作由 `PUT`/`DELETE` 统一迁移为 `POST`，仅保留 `GET`/`POST` 对外接口方法。
  - 已引入 Redis 会话与缓存能力：token 登录写入 Redis 并按 JWT TTL 自动过期，鉴权链路增加 Redis 会话校验。
  - 已在系统写操作中接入延迟双删，覆盖用户/角色/菜单热点缓存一致性场景。
  - 已扩展审计日志接口查询能力，支持关键字/状态/登录方式筛选并与分页联动。
  - 已新增发布前检查清单文档，沉淀发布门禁步骤。
  - 已新增 v0.1/v0.2 范围说明文档，明确版本边界与下一步计划。
  - 已新增 v0.1 发布说明文档，补齐首版开源发布信息。
  - 已新增发布执行 SOP，提供可复制的 tag/Release/回滚指引，并补充 git 前置条件说明。
  - 已新增首批后端单元测试，覆盖 JWT token 服务与 Redis 会话服务核心行为。
  - 已新增审计服务单元测试，覆盖操作日志/登录日志分页与筛选调用场景。
  - 已新增 Auth/Audit 控制器 MockMvc 测试，覆盖登录参数校验、默认分页参数与响应结构断言。
  - 已完成模块服务层接口化改造（auth/audit/system），实现类统一迁移至 `service.impl` 并改为 `*ServiceImpl` 命名。
  - 已完成 framework 服务接口化改造（jwt/session/cache consistency），实现类统一迁移至 `impl` 包。
  - 已新增服务层结构守卫脚本并接入 CI，确保 service 分层规范持续生效。
  - 已新增本地 pre-commit hook 方案（安装脚本 + 检查脚本），用于开发阶段提前拦截不合规提交。
  - 已新增 HTTP 方法约束守卫（仅允许 GET/POST），并接入 CI 与 pre-commit 自动检查。
  - 已修复控制器/拦截器绕过 service 直接使用 Mapper 的问题，分层调用统一收敛到 service。
  - 已新增 Mapper 分层约束守卫，并接入 CI 与 pre-commit。
  - 已完成核心业务服务与 MP 能力整合：`service` 接口继承 `IService<T>`，实现类继承 `ServiceImpl<M,T>`。
   - 已完成服务协作重构：`SystemService` 通过 `RoleService/MenuService` 完成跨实体协作，`AuthService` 通过 `AuditLogService` 写登录日志。

### Phase 7: v0.2 数据权限最小闭环
- **Status:** complete
- Actions taken:
  - 已确认 `jq_sys_role.data_scope` 可直接复用，无需新增首批数据范围字段。
  - 已在后端接入角色数据范围配置，当前支持 `全部数据 / 本部门数据 / 仅本人数据`。
  - 已在用户管理模块接入首批数据范围约束：用户列表、编辑、删除、查看角色、分配角色、新增用户均按当前登录人范围校验。
  - 已完成前后端编译验证，作为 v0.2 最小闭环基线。
  - 已完成首轮联调：角色页可新增/编辑数据范围，浏览器无控制台错误，后端改动已生效。
  - 已完成真实账号验证：`dept_user`（本部门数据）可见同部门用户且不可见跨部门用户；`self_user`（仅本人数据）仅可见自己。
  - 已补齐部门管理后端最小闭环：部门树查询、创建、更新、删除接口均已可用。
  - 已新增 `SystemServiceImplTest`，覆盖数据权限首批核心分支：`super_admin` 全量查询、`仅本人` 查询、`仅本人` 禁止新增、`本部门` 禁止越权编辑。
  - 已重新校准联调测试账号密码：`admin` 保持 `admin123`，其余测试账号统一为 `test123`。
  - 已完成最新一轮真实账号回归：`dept_user` 列表仅见本部门 5 个用户，`self_user` 列表仅见自己，角色数据范围展示与部门列表均正常。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/constant/DataScopeConstants.java` (created)
  - `src/main/java/com/jianqing/module/system/entity/SysRole.java` (updated)
  - `src/main/java/com/jianqing/module/system/dto/RoleSaveRequest.java` (updated)
  - `src/main/java/com/jianqing/module/system/dto/RoleSummary.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/RoleService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/RoleServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java` (updated)
  - `src/test/java/com/jianqing/module/system/service/impl/SystemServiceImplTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 10: CRUD 代码生成器 MVP（进行中）
- **Status:** complete
- Actions taken:
  - 已收敛代码生成器首版边界：仅支持单表，先做元数据、代码预览与下载，不直接写入仓库。
  - 已新增后端 `module/dev` 代码生成器骨架，落地 `GeneratorMetadataService` 与 `DevGeneratorController`。
  - 已新增表列表接口 `GET /api/dev/gen/tables` 与字段元数据接口 `GET /api/dev/gen/tables/{tableName}/columns`。
  - 元数据查询基于当前数据源 `catalog + information_schema`，并对表名做正则校验，防止非法输入。
  - 已新增控制器与 service 单测，覆盖表列表、字段列表及非法表名场景。
  - 已新增 `GeneratorPreviewService`，支持 `POST /api/dev/gen/preview` 与 `POST /api/dev/gen/download` 两个接口。
  - 已完成首批后端模板渲染：entity、saveRequest、summary、mapper、mapper.xml、service、serviceImpl、controller，外加菜单 SQL。
  - 已补齐 preview/download 控制器测试与 preview service 测试，覆盖主键缺失拒绝生成、ZIP 下载与关键文件内容断言。
  - 本轮中途遇到 1 个断言失败与 1 个无用导入问题，已修正后重新验证通过。
  - 已补齐生成器菜单初始化与增量 patch SQL，保证 `admin/super_admin` 可见前端入口。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenTableSummary.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenColumnMeta.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenPreviewFile.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenPreviewRequest.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/GeneratorMetadataService.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorMetadataServiceImpl.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (created)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (created)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorMetadataServiceImplTest.java` (created)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (created)
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260311_generator_menu.sql` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 11: 代码生成器参数治理与稳定性
- **Status:** complete
- Actions taken:
  - 已为生成器参数新增服务端规范化校验：`tableName/moduleName/businessName/className/permPrefix` 统一走 `trim + pattern`。
  - 已将 preview/download/write 全链路收口为规范化参数透传，避免路径、文件名和权限前缀出现输入态漂移。
  - 已补充生成器参数规范化单测，覆盖前后空白输入场景。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 12: 代码写入安全增强（冲突保护）
- **Status:** complete
- Actions taken:
  - 已为生成器写盘接口增加冲突保护：写入前检测已存在文件，默认阻断覆盖并返回冲突提示。
  - 已新增 `overwrite` 显式开关，只有明确传 `overwrite=true` 才允许覆盖写入。
  - 已补齐控制器与服务测试，覆盖默认阻断和显式覆盖两条链路。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (updated)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 13: 冲突清单查询接口与可视化支撑
- **Status:** complete
- Actions taken:
  - 已新增 `POST /api/dev/gen/write/conflicts`，支持写盘前返回冲突文件清单。
  - 已复用 preview 产物路径做冲突比对，保持与真实写盘目标一致。
  - 已补齐控制器与服务测试，覆盖冲突清单查询链路。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (updated)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 14: 生成标记与按标记快速删除
- **Status:** complete
- Actions taken:
  - 已将 `write` 接口返回升级为包含 `markerId` 的写入结果，写入后自动落标记文件。
  - 已新增 `POST /api/dev/gen/write/delete-by-marker`，支持按标记批量删除该次生成文件。
  - 已补齐控制器与服务测试，覆盖写入标记与按标记删除链路。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/dto/GenWriteResult.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenDeleteResult.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (updated)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 15: 写入记录落库与前端按写入记录删除
- **Status:** complete
- Actions taken:
  - 新增 `jq_dev_gen_write_record` 写入记录模型与持久化逻辑，写入项目成功后自动记录 marker 与参数快照。
  - 新增 `GET /api/dev/gen/write/records` 查询接口，返回最近写入记录供前端快速删除选择。
  - 删除 marker 时同步清理写入记录，保持标记与记录一致性。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/entity/GenWriteRecord.java` (created)
  - `src/main/java/com/jianqing/module/dev/mapper/GenWriteRecordMapper.java` (created)
  - `src/main/java/com/jianqing/module/dev/dto/GenWriteRecordItem.java` (created)
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (updated)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `src/test/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImplTest.java` (updated)
  - `sql/patch/20260312_generator_write_record.sql` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 16: 写入记录过滤查询能力
- **Status:** complete
- Actions taken:
  - 写入记录接口支持 `tableName/startAt/endAt` 过滤参数。
  - 过滤查询保持 `limit` 限制与倒序返回策略，避免列表无限增长。
  - 控制器测试已覆盖写入记录接口链路。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/service/GeneratorPreviewService.java` (updated)
  - `src/main/java/com/jianqing/module/dev/service/impl/GeneratorPreviewServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/dev/controller/DevGeneratorController.java` (updated)
  - `src/test/java/com/jianqing/module/dev/controller/DevGeneratorControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 17: 字典功能最小闭环
- **Status:** complete
- Actions taken:
  - 已新增字典类型/字典数据实体、DTO、Mapper、Service 与 Controller 接口，系统管理补齐字典功能后端主链路。
  - 已支持字典类型 CRUD、字典数据 CRUD，以及按 `dictType` 查询启用字典项 `GET /api/system/dict-options/{dictType}`。
  - 已增加字典类型编码格式校验，并在修改字典类型编码时同步更新字典数据表中的 `dict_type`。
  - 已补齐字典菜单权限初始化：新增 init SQL 按钮权限与旧库 patch `20260313_dict_permissions.sql`。
  - 已新增 `SystemControllerTest`、`DictTypeServiceImplTest`、`DictDataServiceImplTest`，并完成测试/Checkstyle/结构守卫回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/entity/SysDictType.java` (created)
  - `src/main/java/com/jianqing/module/system/entity/SysDictData.java` (created)
  - `src/main/java/com/jianqing/module/system/dto/DictTypeSaveRequest.java` (created)
  - `src/main/java/com/jianqing/module/system/dto/DictTypeSummary.java` (created)
  - `src/main/java/com/jianqing/module/system/dto/DictDataSaveRequest.java` (created)
  - `src/main/java/com/jianqing/module/system/dto/DictDataSummary.java` (created)
  - `src/main/java/com/jianqing/module/system/dto/DictOptionItem.java` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysDictTypeMapper.java` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysDictTypeMapper.xml` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysDictDataMapper.java` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysDictDataMapper.xml` (created)
  - `src/main/java/com/jianqing/module/system/service/DictTypeService.java` (created)
  - `src/main/java/com/jianqing/module/system/service/DictDataService.java` (created)
  - `src/main/java/com/jianqing/module/system/service/impl/DictTypeServiceImpl.java` (created)
  - `src/main/java/com/jianqing/module/system/service/impl/DictDataServiceImpl.java` (created)
  - `src/main/java/com/jianqing/module/system/service/SystemService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/controller/SystemController.java` (updated)
  - `src/test/java/com/jianqing/module/system/controller/SystemControllerTest.java` (created)
  - `src/test/java/com/jianqing/module/system/service/impl/DictTypeServiceImplTest.java` (created)
  - `src/test/java/com/jianqing/module/system/service/impl/DictDataServiceImplTest.java` (created)
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260313_dict_permissions.sql` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 26: 参数删除恢复最小闭环
- **Status:** complete
- Actions taken:
  - 已新增已删除参数历史查询接口，仅返回当前仍未恢复的删除记录。
  - 已新增按删除历史恢复参数接口，恢复后自动重新发布动态配置并记录 `RESTORE` 历史。
  - 已在前端参数页补齐“恢复已删”弹窗与恢复操作，恢复后联动刷新主列表与已删记录。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/service/ConfigService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/SystemService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/controller/SystemController.java` (updated)
  - `src/main/java/com/jianqing/module/system/mapper/SysConfigHistoryMapper.java` (updated)
  - `src/main/java/com/jianqing/module/system/mapper/SysConfigHistoryMapper.xml` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/ConfigServiceImpl.java` (updated)
  - `src/test/java/com/jianqing/module/system/service/impl/ConfigServiceImplTest.java` (updated)
  - `src/test/java/com/jianqing/module/system/controller/SystemControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| planning files initialized | check `task_plan.md/findings.md/progress.md` | files exist and contain initial plan | all files created | ✓ |
| architecture blueprint generated | check `ARCHITECTURE.md` | contains module split and integration gateway design | file created with executable plan | ✓ |
| mapper xml migration | check mapper interfaces and xml files | SQL moved from annotations to xml | moved and mapper-locations configured | ✓ |
| mapper xml colocated verification | check `src/main/resources` and clean compile | no mapper xml under resources and project compiles | verified + clean compile passed | ✓ |
| integration adapters compile | add nacos/rocketmq adapter skeletons and compile | project compiles and adapters available | passed | ✓ |
| frontend javascript migration | check ts residue and run frontend build | no TypeScript residue and build success | passed (`npm run build`) | ✓ |
| frontend readme completion | check `jianqing-admin-web/README.md` | contains quick start and integration notes | passed | ✓ |
| system crud and assignment | compile backend and build frontend | APIs and UI interactions available | passed | ✓ |
| backend docs completion | check `README.md`/`CONTRIBUTING.md`/`docs/API_EXAMPLES.md` | open-source docs and examples available | passed | ✓ |
| backend compile baseline | `mvn -DskipTests compile` | compile success | success | ✓ |
| backend test baseline | `mvn test` | test command success | success（No tests to run） | ✓ |
| backend checkstyle gate | `mvn checkstyle:check` | checkstyle passes | success（0 violations） | ✓ |
| backend method constraint scan | grep `@PutMapping/@DeleteMapping` | no matches in backend source | passed | ✓ |
| backend redis refactor compile | `mvn -DskipTests compile` | compile success after redis/cache changes | success | ✓ |
| backend audit filter compile | `mvn -DskipTests compile` | compile success after audit filter params | success | ✓ |
| backend auth unit tests | `mvn test` | JWT/TokenSession tests pass | 7 passed, 0 failed | ✓ |
| backend audit service unit tests | `mvn test` | AuditLogService tests pass | 4 passed, 0 failed | ✓ |
| backend controller web tests | `mvn test` | Auth/Audit controller tests pass | 6 passed, 0 failed | ✓ |
| backend service interface refactor | `mvn test` + `mvn compile` + `mvn checkstyle:check` | service interface+impl refactor passes all gates | passed | ✓ |
| backend framework service interface refactor | `mvn test` + `mvn compile` + `mvn checkstyle:check` | framework service interface+impl refactor passes all gates | passed | ✓ |
| backend service structure guard | `bash scripts/check-service-structure.sh` | interface+impl convention check passes | passed | ✓ |
| backend pre-commit guard | `bash scripts/pre-commit-check.sh` | local pre-commit checks pass | passed | ✓ |
| backend http method guard | `bash scripts/check-http-method-constraints.sh` | no PUT/DELETE in backend and frontend api | passed | ✓ |
| backend mapper layering guard | `bash scripts/check-mapper-layering.sh` | no mapper import outside service.impl | passed | ✓ |
| backend mp service pattern guard | `mvn test` + `mvn compile` | IService + ServiceImpl integration works | passed | ✓ |
| backend service-collaboration refactor | `mvn test` + guards | service-to-service replaces cross-entity mapper access | passed | ✓ |
| backend data scope baseline | `mvn test` | role data scope DTO/service changes and user-scope restrictions compile and tests pass | passed | ✓ |
| data scope integration smoke test | restart backend + browser role create/update | role data scope create/update works after backend restart | passed | ✓ |
| data scope real-account verification | create test users/roles + browser login verification | dept scope sees same dept only, self scope sees self only | passed | ✓ |
| dept backend minimal CRUD | `mvn test` | dept entity/mapper/service/controller changes compile and backend tests still pass | passed | ✓ |
| backend data scope unit tests | `mvn test` | SystemServiceImpl data-scope branches are covered and all backend tests pass | passed（21 passed, 0 failed） | ✓ |
| latest data scope regression | admin/dept_user/self_user real-account verification | dept scope only sees same dept, self scope only sees self, role/dept pages render normally | passed | ✓ |
| backend jwt filter observability fix | `mvn -q -DskipTests compile` + `mvn -q checkstyle:check` | compile/checkstyle pass after replacing swallowed exception with warn logging | passed | ✓ |
| backend system-service split step2 | `mvn -q -DskipTests compile` + `mvn -q checkstyle:check` | compile/checkstyle pass after extracting user data-scope logic to `UserDataScopeResolver` | passed | ✓ |
| backend system-service split step3 | `mvn -q -DskipTests compile` + `mvn -q checkstyle:check` | compile/checkstyle pass after extracting cache eviction logic to `SystemCacheEvictor` | passed | ✓ |
| backend system-service split step4 | `mvn test` + `mvn checkstyle:check` | user write operations are extracted to `UserWriteOperationHandler` and quality gates still pass | passed | ✓ |
| backend system-service key-path tests | `mvn test` | cache eviction, password validation and data-scope branches are covered by new tests | passed（26 passed, 0 failed） | ✓ |
| frontend system list composable | `npm run build` | users/roles/menus/depts share one list-page composable and frontend build passes | passed | ✓ |
| frontend dialog form composable | `npm run build` | users/roles/menus/depts share one dialog-form state composable and frontend build passes | passed | ✓ |
| frontend delete action composable | `npm run build` | users/roles/menus/depts share one delete-action composable and frontend build passes | passed | ✓ |
| frontend submit action composable | `npm run build` | users/roles/menus/depts share one submit-action composable and frontend build passes | passed | ✓ |
| frontend system pages browser regression | Playwright + admin login + users/roles/menus/depts smoke flow | query/reset/create-open-close/edit-open-close/delete-confirm/pagination work without console cancel warnings | passed | ✓ |
| data-scope account regression | browser-context auth + `/api/system/users` verification | dept_user sees 5 same-dept users, self_user sees self only, outside_user sees 6 users | passed | ✓ |
| audit pages browser regression | Playwright + admin session | oper/login logs query/reset/pagination work and both pages load 20 rows | passed | ✓ |
| frontend failure-handling regression | Playwright + mocked audit 500 response | page keeps operable, loading recovers, and no unhandled warning is emitted | passed | ✓ |
| non-admin login dashboard regression | Playwright + outside_user login | dashboard no longer triggers roles/menus unauthorized noise; unauthorized stats show `--` | passed | ✓ |
| backend p0 safety refactor | `mvn test` + pre-commit hooks | config hardening, transaction boundaries and after-commit cache eviction all pass | passed | ✓ |
| backend service split checkpoint | `mvn test` | handler-based `SystemServiceImpl` refactor keeps behavior stable | passed（42 passed, 0 failed） | ✓ |
| generator metadata baseline | `mvn test` + `mvn checkstyle:check` | dev/gen metadata APIs compile, tests pass, style passes | passed（47 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator preview baseline | `mvn test` + `mvn checkstyle:check` | preview/download APIs and first backend templates pass tests and style checks | passed（52 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator fullstack templates | `mvn test` + `mvn checkstyle:check` | preview/download now includes frontend API/view/route snippet templates | passed（52 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator smart field mapping | `mvn test` + `mvn checkstyle:check` | generated frontend templates map date/time/textarea/switch/number controls correctly | passed（52 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator param normalization | `mvn test` + `mvn checkstyle:check` | preview/download/write use normalized request params and tests cover trim scenarios | passed | ✓ |
| generator write conflict safety | `mvn test` + `mvn checkstyle:check` | write endpoint blocks overwrite by default and supports explicit overwrite=true | passed | ✓ |
| generator write conflict listing | `mvn test` + `mvn checkstyle:check` | write/conflicts endpoint returns deterministic conflict file list before write | passed | ✓ |
| generator marker write and delete | `mvn test` + `mvn checkstyle:check` | write returns markerId and delete-by-marker removes generated files safely | passed（62 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator write records persist | `mvn test` + `mvn checkstyle:check` | write actions persist marker records and expose /write/records query endpoint | passed（62 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator write records filter | `mvn test` + `mvn checkstyle:check` | /write/records supports table/time filters while preserving limit ordering | passed（63 passed, 0 failed；checkstyle 0 violations） | ✓ |
| dict module backend baseline | `mvn test` + `mvn checkstyle:check` + guards | dict type/data APIs compile, tests pass, style与分层守卫均通过 | passed（77 passed, 0 failed；checkstyle 0 violations） | ✓ |
| dict seed consumption baseline | `mvn test` | default dict seeds added without breaking backend tests | passed（77 passed, 0 failed） | ✓ |
| audit dict seed baseline | `mvn test` | audit dict seeds added without breaking backend tests | passed（77 passed, 0 failed） | ✓ |
| dict cache baseline | `mvn test` + `mvn checkstyle:check` + guards | dict type/data/options cache and eviction logic works with rename/update edges | passed（81 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config module baseline | `mvn test` + `mvn checkstyle:check` + guards | config CRUD, gateway publish, cache eviction and menu seed changes all verified | passed（88 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config grouping baseline | `mvn test` + `mvn checkstyle:check` + guards | config group field, grouped publish and CRUD adaptation verified | passed（88 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config history baseline | `mvn test` + `mvn checkstyle:check` + guards | config history persistence and query API verified | passed（89 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config rollback baseline | `mvn test` + `mvn checkstyle:check` + guards | config rollback API, publish refresh and rollback history verified | passed（91 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config diff baseline | `mvn test` + `mvn checkstyle:check` + guards | current-vs-history config diff API verified | passed（93 passed, 0 failed；checkstyle 0 violations） | ✓ |
| config deleted-restore baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | deleted config history list and restore flow verified | passed（98 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config multi-history diff baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | current-vs-history and history-vs-history diff flows verified | passed（100 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config restore preview baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | deleted config restore preview and confirm flow verified | passed（102 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config restore preview diff baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | restore preview shows key occupancy and field diffs before restore | passed（103 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config value-type dict baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | config value type dict seed and frontend dict consumption verified | passed（103 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| dict color-type dict baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | dict color type seed and frontend dict consumption verified | passed（103 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config source dict baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | config source dict seed and frontend dict consumption verified | passed（103 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| dict status dict baseline | `mvn test` + `mvn checkstyle:check` + `npm run build` | dict page consumes sys_common_status for filters/forms/tags | passed（103 passed, 0 failed；checkstyle 0 violations；frontend build passed） | ✓ |
| config legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing config columns/history table on legacy schema | passed（105 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing jq_dev_gen_write_record on legacy schema | passed（107 passed, 0 failed；checkstyle 0 violations） | ✓ |
| generator menu legacy compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing generator menus and admin role bindings | passed（109 passed, 0 failed；checkstyle 0 violations） | ✓ |
| menu legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing jq_sys_menu core columns and jq_sys_role_menu | passed（111 passed, 0 failed；checkstyle 0 violations） | ✓ |
| dict legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing jq_sys_dict_type/jq_sys_dict_data and core columns | passed（113 passed, 0 failed；checkstyle 0 violations） | ✓ |
| dept legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing jq_sys_dept and core columns | passed（115 passed, 0 failed；checkstyle 0 violations） | ✓ |
| audit legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing audit tables/core columns and JSON-compatible payload storage | passed（117 passed, 0 failed；checkstyle 0 violations） | ✓ |
| user-role legacy schema compatibility baseline | `mvn test` + `mvn checkstyle:check` | startup repair covers missing jq_sys_user/jq_sys_role/jq_sys_user_role and core columns | passed（119 passed, 0 failed；checkstyle 0 violations） | ✓ |
| legacy schema docs baseline | docs update | legacy compatibility scope and troubleshooting guide documented | passed | ✓ |
| init sql dialect baseline | docs/sql update | init sql no longer uses mysql8-only collation | passed | ✓ |
| init sql timestamp baseline | `mvn test` + `mvn checkstyle:check` | init sql timestamp columns switched to more compatible TIMESTAMP defaults | passed（119 passed, 0 failed；checkstyle 0 violations） | ✓ |

## Latest Updates
- 已完成字典管理与参数设置真实浏览器回归：字典页覆盖新增类型/新增数据/删除，参数页覆盖新增/编辑/历史/删除/恢复预览/恢复。
- 已修复字典页真实回归 warning：`DictsView` 的颜色标签在“默认”场景不再向 `ElTag` 传空字符串类型，控制台 warning 已清零。
- 已修复参数恢复链路真实回归故障：`/api/system/configs/deleted/history` 查询中的 `config_key` 比较已显式统一为 `utf8mb4_unicode_ci`，解决旧环境 `Illegal mix of collations` 报错。
- 已完成前端构建、后端 119 个测试回归，并重启后端完成参数恢复链路真实复测。
- 已完成参数页双历史 diff / 回滚真实浏览器回归：`diff?compareHistoryId=` 与 `history/{id}/rollback` 均已走通，历史列表新增 `ROLLBACK` 记录且主列表值已回到目标历史版本。
- 已完成生成器写入记录 / marker 删除真实浏览器回归：`write`、`write/records`、`delete-by-marker` 全链路返回 200，按 marker 删除后工作区未发现本次生成残留文件。
- 已完成生成器冲突确认真实浏览器回归：重复写入会展示 12 个冲突文件的分组清单，快捷过滤“前端”后统计正确收敛到 3 / 12。
- 已完成字典页与参数页筛选/分页边界真实回归：查询、重置、单条结果分页与每页条数切换均通过。
- 已完成数据权限扩面：角色数据范围现支持“本部门及以下”，后端用户列表/访问/写操作校验已同步扩展到部门子树。
- 已为部门服务补齐“本部门及以下部门 ID”递归收集能力，并在数据权限解析阶段复用。
- 已完成后端 `mvn test` 与前端 `npm run build` 回归，当前后端 121 个测试全部通过。

### Phase 45: 参数恢复链路 collation 兼容修复
- **Status:** complete
- Actions taken:
  - 已执行参数设置真实浏览器回归，覆盖新增、编辑、历史、删除、恢复预览与恢复。
  - 已定位 `jq_sys_config` 与 `jq_sys_config_history` 的 `config_key` collation 混用导致删除历史查询报错。
  - 已在 `SysConfigHistoryMapper.xml` 中为相关比较显式补齐 `CONVERT(... USING utf8mb4) COLLATE utf8mb4_unicode_ci`。
  - 已完成 `mvn test` 回归并重启后端，浏览器复测 `/api/system/configs/deleted/history`、`/preview`、`/restore` 全部返回 200。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/mapper/SysConfigHistoryMapper.xml` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 46: 参数多历史与生成器回滚链路真实回归
- **Status:** complete
- Actions taken:
  - 已执行参数页真实浏览器回归：新增参数、两次编辑形成多历史，验证“历史 vs 历史”diff 与回滚链路。
  - 已执行生成器真实浏览器回归：基于 `jq_dev_gen_write_record` 生成预览并写入项目，再通过写入记录按 marker 删除。
  - 已验证 `delete-by-marker` 返回“已删除 12 个文件”，并确认工作区无 `*833901*` 残留文件。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 47: 生成器冲突确认与系统筛选边界真实回归
- **Status:** complete
- Actions taken:
  - 已执行生成器冲突确认真实浏览器回归：重复写入已触发冲突清单、目录统计与快捷过滤，并以取消覆盖结束验证。
  - 已执行字典页与参数页真实回归：分别创建临时数据验证查询、重置、每页条数切换，再完成清理。
  - 已确认本轮生成器冲突弹窗与系统页回归后控制台 warning = 0。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 48: 数据权限扩展到本部门及以下
- **Status:** complete
- Actions taken:
  - 已放开角色数据范围校验，新增支持 `DataScopeConstants.DEPT_AND_CHILD`。
  - 已在 `DeptService` / `DeptServiceImpl` 新增 `listSelfAndDescendantDeptIds`，按启用部门父子关系递归收集部门子树。
  - 已将 `UserDataScopeResolver` 改为携带可访问部门 ID 列表，并同步更新用户列表过滤、用户访问判断、用户新增/修改部门校验。
  - 已补齐 `UserDataScopeResolverTest`，覆盖“本部门及以下”解析与访问子部门用户场景。
  - 已完成 `mvn test` 与 `npm run build` 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/service/DeptService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/DeptServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/UserDataScopeResolver.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/RoleServiceImpl.java` (updated)
  - `src/test/java/com/jianqing/module/system/service/impl/UserDataScopeResolverTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 49: 本部门及以下角色配置真实回归
- **Status:** complete
- Actions taken:
  - 已启动本地前后端服务，并在真实浏览器中登录管理员账号进入角色页。
  - 已验证“本部门数据角色”编辑弹窗存在“本部门及以下数据”选项，并成功保存后在列表中展示“本部门及以下”。
  - 已在验证后将该角色恢复为原始“本部门数据”，避免影响现有联调账号口径。
  - 已确认本轮角色页真实回归后控制台 warning/error = 0。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 50: 本部门及以下真实数据范围联调
- **Status:** complete
- Actions taken:
  - 已确认当前真实部门树为“简擎总部 -> 外部协作部”，其中 `dept_user` 挂载角色 4（`dept_scope_role`），`outside_user` 位于子部门 3。
  - 已通过真实后端 API 临时创建树外根部门与树外用户，并将 `dept_scope_role` 暂时切换为 `DEPT_AND_CHILD`。
  - 已验证 `dept_user/test123` 登录后可见 `outside_user`，同时不可见临时树外用户，证明“子部门可见、树外不可见”链路成立。
  - 已在验证后恢复 `dept_scope_role.dataScope = DEPT`，并删除临时部门与临时用户；复核当前用户/部门列表已恢复基线状态。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 51: 数据权限树形边界单测补强
- **Status:** complete
- Actions taken:
  - 已新增 `DeptServiceImplTest`，覆盖“仅收集当前子树部门 ID”与“树外部门返回空列表”两类边界。
  - 已扩展 `UserDataScopeResolverTest`，补齐 `ALL` 优先于 `DEPT_AND_CHILD`、树外用户访问拒绝、树外部门迁移拒绝等场景。
  - 已修复 `ServiceImpl.baseMapper` 在单测中的注入问题，并完成 focused 测试与全量 `mvn test` 回归。
- Files created/modified:
  - `src/test/java/com/jianqing/module/system/service/impl/DeptServiceImplTest.java` (new)
  - `src/test/java/com/jianqing/module/system/service/impl/UserDataScopeResolverTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 52: dept_user 用户页真实回归
- **Status:** complete
- Actions taken:
  - 已通过真实后端 API 临时将 `dept_scope_role` 切换到 `DEPT_AND_CHILD`。
  - 已以 `dept_user/test123` 登录真实前端用户页，确认表格展示 6 条用户数据，包含子部门用户 `outside_user`。
  - 已确认本轮用户页真实回归控制台 warning/error = 0。
  - 已在验证后将 `dept_scope_role.dataScope` 恢复为 `DEPT`。
- Files created/modified:
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 53: 上下文恢复降 token 优化
- **Status:** complete
- Actions taken:
  - 已新增 `jianqing-backend/current_state.md`，用于承载当前阶段、最近完成、下一步任务与关键约束。
  - 已调整工作区 `AGENTS.md` 中的“续开发协议”，默认先读前后端 `current_state.md`，仅在需要历史细节时再展开 `task_plan.md / findings.md / progress.md`。
  - 已补充“按侧优先读取”“progress 以存档价值为主”的约束，降低后续继续开发时的默认 token 消耗。
- Files created/modified:
  - `current_state.md` (new)
  - `/Users/majiaju/Person/code/jianqing/AGENTS.md` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)
- 已完成后端热点扫描，识别 `SystemServiceImpl` 为当前优先重构目标（职责集中、行数最高）。
- 已修复 `JwtAuthenticationFilter` 异常吞掉问题：保留鉴权失败清上下文逻辑，同时输出 `warn` 级日志提升排障可观测性。
- 已完成 `SystemServiceImpl` 第二批重构：新增 `UserDataScopeResolver`，将数据范围解析/查询构造/访问校验从主类中抽离。
- 已完成 `SystemServiceImpl` 第三批重构：新增 `SystemCacheEvictor`，将系统缓存与用户权限缓存失效职责抽离。
- 已完成 `SystemServiceImpl` 第四批重构：新增 `UserWriteOperationHandler`，继续下沉用户创建/更新/删除写操作编排，主服务进一步收敛为访问控制 + 业务编排入口。
- 已补齐系统服务关键路径单测：新增 `UserWriteOperationHandlerTest`、`UserDataScopeResolverTest`，覆盖缓存失效、密码校验、数据范围解析与越权分支。
- 已完成前端系统页列表通用 composable 收口：新增 `useSystemListPage`，统一 users/roles/menus/depts 四页的查询、重置、刷新与分页状态流。
- 已完成前端系统页弹窗表单轻量收口：新增 `useEntityDialogForm`，统一四页 create/edit 弹窗的可见性、编辑态、编辑目标与表单回填流程。
- 已完成前端系统页删除操作轻量收口：新增 `useEntityDeleteAction`，统一删除确认、删除执行、列表刷新与成功提示流程。
- 已完成前端系统页保存提交轻量收口：新增 `useEntitySubmitAction`，统一保存执行、关闭弹窗、列表刷新与成功提示流程。
- 已完成一轮 Playwright 系统页真实回归；过程中发现并修复 `openCreate` 误吃点击事件对象、删除取消产生未处理 `cancel` 警告两处问题，复测已通过。
- 已补做数据权限账号回归：通过浏览器上下文调用登录与用户列表接口，确认 `dept_user` / `self_user` 结果符合预期；并查清 `outside_user` 当前挂载 `Test_User(dataScope=ALL)`，因此返回 6 人属于配置结果而非代码异常。
- 已完成测试账号口径收清：后续数据权限联调应继续使用 `dept_user` / `self_user` 作为受限账号，`outside_user` 仅保留为“全部数据”现状样本，除非后续显式改角色配置。
- 已完成审计页真实回归：操作日志与登录日志页的查询、重置、分页均通过，当前无新增异常。
- 已补一轮前端异常处理收口：system/audit/dashboard 页面在请求失败时不再额外抛出未处理 Promise 警告，loading 能正确回收，失败提示继续由 HTTP 拦截器统一输出。
- 已修复非 admin 账号登录噪音：dashboard 统计卡片改为按权限请求，低权限账号登录后不再因无权限的 roles/menus 请求产生 401 噪音，未授权统计显示为 `--`。
- 已完成后端 P0 收口：移除默认敏感配置，新增 `application-example.yml`，并将本地配置切换为环境变量 + `application-local.yml` 模式。
- 已完成关键写路径一致性加固：`SystemServiceImpl`、`RoleServiceImpl`、`MenuServiceImpl`、`DeptServiceImpl` 关键写方法补齐事务边界，`SystemCacheEvictor` 改为提交后触发失效。
- 已完成 `SystemServiceImpl` 新一轮结构收口：新增 `UserRoleAssignmentHandler`、`RoleMenuAssignmentHandler`、`RoleWriteOperationHandler`、`MenuWriteOperationHandler`、`DeptWriteOperationHandler`，主服务进一步聚焦访问控制、事务边界与跨域编排。
- 已新增 `SystemCacheEvictorTest`、`SystemTransactionAnnotationTest` 以及各 handler 单测，后端当前测试数提升至 42 个且全部通过。
- 已新增根目录 `.github/workflows/frontend-ci.yml`，并将后端 CI 迁移到仓库根目录统一管理，前后端门禁入口已对齐。
- 已将本轮优化经验同步进统一开发规范：后续后端新增复杂写流程时，优先走“聚合入口 + 轻量执行器”，先保证正确性，再做抽象与性能优化。
- 已启动 CRUD 代码生成器主线：后端先完成 `dev/gen` 元数据接口骨架，下一步进入 preview/download 与模板渲染。
- 已完成 CRUD 代码生成器第二步：preview/download 与首批后端模板渲染已打通，下一步可切到前端生成器页或继续扩展模板精度。
- 已完成 CRUD 代码生成器 MVP 最小闭环：后端元数据/预览/下载 + 前端生成器页入口已对齐，下一步可转向浏览器回归或模板精度增强。
- 浏览器真实联调在本轮被本地环境阻断：重启后端后发现 README 默认 MySQL 地址 `127.0.0.1:3306` 当前不可达，暂未恢复真实后端联通，只完成了前端 mock 回归。
- 当前环境已恢复后端真实联通：重新验证 `admin/admin123` 登录成功，`/api/dev/gen/tables` 可正常返回真实库表列表。
- 已继续增强代码生成器产物完整度：preview/download 新增前端 API 文件、前端业务列表页 `.vue` 与路由 snippet，生成器不再只输出后端和 SQL。
- 已继续增强前端模板字段映射：生成视图现在能按字段类型输出 `el-date-picker`、`el-time-picker`、`textarea`、`el-switch`、`el-input-number` 等更贴近真实业务的控件。
- 已完成生成器参数治理：服务端统一 `trim + pattern` 校验并透传规范化参数，下载文件名也同步按规范化业务名生成。
- 已完成生成器写盘安全增强：`write` 默认冲突阻断，需显式 `overwrite=true` 才允许覆盖，避免误覆盖已有源码。
- 已完成冲突清单查询能力：`write/conflicts` 可在写入前返回将被覆盖的文件列表，为前端风险可视化提供稳定数据源。
- 已完成生成标记与按标记删除闭环：写入成功返回 `markerId`，并可通过 `delete-by-marker` 快速回滚该次生成文件，模板内容保持不变。
- 已完成字典功能最小闭环：系统管理现支持字典类型/字典数据维护，且提供按类型读取启用字典项接口，便于后续业务消费接入。
- 已补齐字典默认 seed/patch：`sys_common_status`、`sys_menu_visible`、`sys_dept_status` 已可直接供前端系统页消费。
- 已补齐审计页默认字典 seed/patch：`audit_exec_status`、`audit_login_type` 已可直接供审计页消费。
- 已完成字典缓存收口：字典类型列表、字典数据列表、启用字典项查询均已支持缓存，并在写操作后提交后失效。
- 已完成参数设置最小闭环：系统管理现支持参数列表/新增/编辑/删除，并在写入后同步发布到 DynamicConfigGateway。
- 已完成参数分组最小闭环：参数现支持分组维护，并按分组发布到 DynamicConfigGateway。
- 已完成参数变更历史最小闭环：参数每次新增/修改/删除都会自动记录历史，并可在参数页查看。
- 已完成参数版本回滚最小闭环：参数可按历史快照回滚，回滚后自动重新发布并记录回滚历史。
- 已完成默认端口 `CUSTOM` 数据范围真实接口回归：重启 `127.0.0.1:8080` 后，将 `dept_scope_role` 切到 `CUSTOM + [3]`，`dept_user/test123` 仅可见 `outside_user`，验证结束后已恢复原始 `DEPT` 配置。
- 已确认默认联调实例已切到新版代码，`/api/system/roles/{id}/update` 在 `8080` 上可直接保存 `CUSTOM` 数据范围。
- 已完成参数 diff 对比最小闭环：可查看当前参数与指定历史快照的字段级差异。
- 已完成参数删除恢复最小闭环：后端新增已删除历史列表与按删除历史恢复接口，前端参数页新增“恢复已删”弹窗与恢复操作。
- 已完成参数多版本 diff 最小闭环：参数现支持“当前 vs 历史”与“历史 vs 历史”两种字段级差异对比模式。
- 已完成参数删除恢复前预览最小闭环：恢复前可先查看删除快照详情，再决定是否恢复。
- 已完成参数恢复前差异对比最小闭环：同键冲突场景会显示字段级差异并阻止直接恢复。
- 已完成字典消费扩面首个高频页面：参数页值类型已改为优先消费 `sys_config_value_type` 字典。
- 已完成字典消费扩面第二个高频页面：字典管理页颜色类型已改为优先消费 `sys_dict_color_type` 字典。
- 已完成字典消费扩面第三个高频页面：参数页来源已改为优先消费 `sys_config_source` 字典。
- 已完成字典消费扩面第四个高频页面：字典管理页状态已改为优先消费 `sys_common_status` 字典。
- 已完成参数表旧库缺列兼容：启动时会自动补齐参数扩展字段与历史表，避免旧库直接把参数页打挂。
- 已完成代码生成器旧库缺表兼容：启动时会自动补齐 `jq_dev_gen_write_record`，避免生成器链路因缺表直接失败。
- 已完成生成器菜单旧库兼容：启动时会自动补齐生成器菜单与管理员角色绑定，避免旧库看不到入口。
- 已完成菜单表核心字段旧库兼容：启动时会自动补齐 `jq_sys_menu` 核心字段与 `jq_sys_role_menu` 表，避免菜单系统级失效。
- 已完成字典表核心结构旧库兼容：启动时会自动补齐 `jq_sys_dict_type/jq_sys_dict_data` 与其核心字段，避免字典系统级失效。
- 已完成部门表核心结构旧库兼容：启动时会自动补齐 `jq_sys_dept` 与其核心字段，避免部门树链路失效。
- 已完成审计表核心结构兼容：启动时会自动补齐审计表及其核心字段，并用 LONGTEXT 兼容请求/响应体存储。
- 已完成用户角色核心结构旧库兼容：启动时会自动补齐用户、角色和用户角色关联表及其核心字段，避免登录与授权链路失效。
- 已完成旧库兼容文档收口：README 和独立文档已说明当前覆盖范围、接入方式、排障顺序与边界。
- 已完成 init SQL 方言风险首轮收口：初始化脚本已不再使用 MySQL 8 专属 collation。
- 已完成 init SQL 时间字段兼容收口：初始化脚本中的自动时间戳字段已优先切换为 TIMESTAMP 方案。

### Phase 44: init SQL 时间字段兼容收口
- **Status:** complete
- Actions taken:
  - 已将 `sql/jianqing-init-v0.1.sql` 中的自动时间戳字段优先改为 `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` 方案。
  - 已在 README 与旧库兼容文档补充时间字段兼容说明。
  - 已完成后端测试、Checkstyle 与规划同步。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `README.md` (updated)
  - `docs/LEGACY_SCHEMA_COMPATIBILITY.md` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 43: init SQL 方言风险收口
- **Status:** complete
- Actions taken:
  - 已将 `sql/jianqing-init-v0.1.sql` 的库级 collation 从 `utf8mb4_0900_ai_ci` 调整为 `utf8mb4_unicode_ci`。
  - 已在 README 与旧库兼容文档补充说明，明确该调整的目的与边界。
  - 已完成规划与进度同步。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `README.md` (updated)
  - `docs/LEGACY_SCHEMA_COMPATIBILITY.md` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 42: 旧库兼容说明文档收口
- **Status:** complete
- Actions taken:
  - 已新增 `docs/LEGACY_SCHEMA_COMPATIBILITY.md`，汇总当前所有启动期 schema 自修复覆盖范围。
  - 已补充旧库接入方式、推荐操作顺序、排障建议与已知边界。
  - README 已新增旧库兼容说明入口，便于快速定位。
  - 已完成规划与进度同步。
- Files created/modified:
  - `docs/LEGACY_SCHEMA_COMPATIBILITY.md` (created)
  - `README.md` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 41: 用户角色核心结构旧库兼容
- **Status:** complete
- Actions taken:
  - 已新增 `UserRoleSchemaInitializer`，启动时自动检查并补齐 `jq_sys_user/jq_sys_role/jq_sys_user_role` 及其核心字段。
  - 已补齐旧库缺少用户表、角色表、用户角色关联表的场景，确保登录、角色分配和权限计算依赖可用。
  - 已补充 `UserRoleSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/startup/UserRoleSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/system/startup/UserRoleSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 40: 审计表核心结构兼容
- **Status:** complete
- Actions taken:
  - 已新增 `AuditSchemaInitializer`，启动时自动检查并补齐 `jq_sys_oper_log/jq_sys_login_log` 及其核心字段。
  - 已将 `jq_sys_oper_log.request_param/response_data` 的初始化类型调整为 LONGTEXT，降低旧库方言兼容风险。
  - 已补充 `AuditSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `src/main/java/com/jianqing/module/audit/startup/AuditSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/audit/startup/AuditSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 39: 部门表核心结构旧库兼容
- **Status:** complete
- Actions taken:
  - 已新增 `DeptSchemaInitializer`，启动时自动检查并补齐 `jq_sys_dept` 及其核心字段。
  - 已补齐旧库缺少部门表的场景，确保部门树、部门筛选和组织架构依赖可用。
  - 已补充 `DeptSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/startup/DeptSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/system/startup/DeptSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 38: 字典表核心结构旧库兼容
- **Status:** complete
- Actions taken:
  - 已新增 `DictSchemaInitializer`，启动时自动检查并补齐 `jq_sys_dict_type/jq_sys_dict_data` 及其核心字段。
  - 已补齐旧库缺少字典类型表、字典数据表的场景，确保字典管理与前端 dict-options 依赖可用。
  - 已补充 `DictSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/startup/DictSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/system/startup/DictSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 37: 菜单表核心字段旧库兼容
- **Status:** complete
- Actions taken:
  - 已新增 `MenuSchemaInitializer`，启动时自动检查并补齐 `jq_sys_menu` 的核心字段。
  - 已补齐 `jq_sys_role_menu` 缺表场景，确保菜单权限关系可用。
  - 已补充 `MenuSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/startup/MenuSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/system/startup/MenuSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 36: 生成器菜单旧库兼容
- **Status:** complete
- Actions taken:
  - 已新增 `GeneratorMenuInitializer`，启动时自动检查并补齐 `system:generator:list/query` 菜单。
  - 已自动为管理员补齐生成器菜单角色绑定，避免有菜单无权限。
  - 已补充 `GeneratorMenuInitializerTest`，覆盖“需要创建”和“已存在无需创建”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/startup/GeneratorMenuInitializer.java` (created)
  - `src/test/java/com/jianqing/module/dev/startup/GeneratorMenuInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 35: 代码生成器旧库缺表兼容
- **Status:** complete
- Actions taken:
  - 已新增 `GeneratorSchemaInitializer`，启动时自动检查并补齐 `jq_dev_gen_write_record`。
  - 已补充 `GeneratorSchemaInitializerTest`，覆盖“缺表需创建”和“已存在无需创建”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/dev/startup/GeneratorSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/dev/startup/GeneratorSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 34: 参数表旧库缺列兼容
- **Status:** complete
- Actions taken:
  - 已新增 `ConfigSchemaInitializer`，启动时自动检查并补齐 `jq_sys_config` 缺失的 `config_group/value_type/is_builtin` 字段。
  - 已新增对 `jq_sys_config_history` 的建表与缺列补齐逻辑，兼容尚未执行参数模块 patch 的老库。
  - 已补充 `ConfigSchemaInitializerTest`，覆盖“需要修复”和“已完整无需修复”两类场景。
  - 已完成后端测试与 Checkstyle 回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/startup/ConfigSchemaInitializer.java` (created)
  - `src/test/java/com/jianqing/module/system/startup/ConfigSchemaInitializerTest.java` (created)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 33: 字典管理页状态字典消费扩面
- **Status:** complete
- Actions taken:
  - 字典管理页状态筛选已改为优先读取 `sys_common_status` 字典 options。
  - 字典类型/字典数据表单状态选项已改为优先消费字典 options。
  - 状态标签文案与颜色已改为字典驱动，并保留前端兜底值。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `../jianqing-admin-web/src/views/system/DictsView.vue` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 32: 参数来源字典消费扩面
- **Status:** complete
- Actions taken:
  - 已补齐 `sys_config_source` 默认字典 seed 与旧库 patch。
  - 参数页来源筛选、表单、列表标签与恢复预览文案已改为优先读取字典 options。
  - 已同步改为字典驱动的标签类型展示，并保留前端兜底映射。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260316_config_source_dict.sql` (created)
  - `../jianqing-admin-web/src/views/system/ConfigsView.vue` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 31: 字典颜色类型字典消费扩面
- **Status:** complete
- Actions taken:
  - 已补齐 `sys_dict_color_type` 默认字典 seed 与旧库 patch。
  - 字典管理页颜色下拉已改为优先读取字典 options。
  - 字典数据列表颜色展示已改为字典文案 + 标签类型，并保留前端兜底选项。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260316_dict_color_type_dict.sql` (created)
  - `../jianqing-admin-web/src/views/system/DictsView.vue` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 30: 参数值类型字典消费扩面
- **Status:** complete
- Actions taken:
  - 已补齐 `sys_config_value_type` 默认字典 seed 与旧库 patch。
  - 参数页值类型筛选、表单和文案展示已改为优先读取字典 options。
  - 前端保留本地兜底选项，确保旧环境缺字典时仍可用。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `sql/jianqing-init-v0.1.sql` (updated)
  - `sql/patch/20260313_config_value_type_dict.sql` (created)
  - `../jianqing-admin-web/src/views/system/ConfigsView.vue` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 28: 参数删除恢复前预览最小闭环
- **Status:** complete
- Actions taken:
  - 已新增删除历史恢复预览接口，返回待恢复删除快照详情。
  - 前端已删记录列表已补齐“预览”入口与恢复前详情弹窗。
  - 预览弹窗支持直接确认恢复，恢复后联动刷新主列表与已删记录列表。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/service/ConfigService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/SystemService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/controller/SystemController.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/ConfigServiceImpl.java` (updated)
  - `src/test/java/com/jianqing/module/system/service/impl/ConfigServiceImplTest.java` (updated)
  - `src/test/java/com/jianqing/module/system/controller/SystemControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

### Phase 29: 参数恢复前差异对比最小闭环
- **Status:** complete
- Actions taken:
  - 恢复预览接口已补充同键占用状态、当前同键参数信息与字段级差异列表。
  - 前端恢复预览弹窗已展示同键占用提示与差异表格。
  - 同键已存在时会禁用确认恢复，避免无效恢复尝试。
  - 已完成后端测试、Checkstyle 与前端构建回归。
- Files created/modified:
  - `src/main/java/com/jianqing/module/system/dto/ConfigRestorePreviewSummary.java` (created)
  - `src/main/java/com/jianqing/module/system/mapper/SysConfigMapper.java` (updated)
  - `src/main/java/com/jianqing/module/system/mapper/SysConfigMapper.xml` (updated)
  - `src/main/java/com/jianqing/module/system/service/ConfigService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/SystemService.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/SystemServiceImpl.java` (updated)
  - `src/main/java/com/jianqing/module/system/controller/SystemController.java` (updated)
  - `src/main/java/com/jianqing/module/system/service/impl/ConfigServiceImpl.java` (updated)
  - `src/test/java/com/jianqing/module/system/service/impl/ConfigServiceImplTest.java` (updated)
  - `src/test/java/com/jianqing/module/system/controller/SystemControllerTest.java` (updated)
  - `task_plan.md` (updated)
  - `findings.md` (updated)
  - `progress.md` (updated)

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-03-03 | 工具调用参数缺失/显示乱码 | 1 | 重试并补齐参数后执行成功 |
| 2026-03-11 | Java LSP `jdtls` 未安装，无法执行 `lsp_diagnostics` | 1 | 改用 `mvn test` 与 `mvn checkstyle:check` 完成本轮编译与静态校验 |
| 2026-03-11 | `GeneratorPreviewServiceImplTest` 路由断言失败 | 1 | 将生成控制器路由补全为 `/api/{module}/{business}` 后复测通过 |
| 2026-03-11 | `GeneratorPreviewServiceImpl` 存在无用导入 `Locale` | 1 | 删除无用导入并重新执行 Checkstyle，通过 |
| 2026-03-11 | 重启后端后 MySQL `127.0.0.1:3306` 不可达 | 1 | 记录为本地环境阻断项，浏览器回归改用 Playwright mock 验证前端链路 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 44（init SQL 时间字段兼容收口） |
| Where am I going? | 继续清理 init SQL 其他数据库方言风险，或回到前端继续字典消费扩面 |
| What's the goal? | 构建可开源的简擎后端内核，并预留 ES/Nacos/RocketMQ 扩展 |
| What have I learned? | 初始化脚本里的时间字段兼容问题虽然不如 collation 显眼，但影响面更广，因为几乎所有基础表都会复用自动时间戳列 |
| What have I done? | 已完成 init SQL 时间字段兼容收口，并通过 119 条后端测试与 Checkstyle 校验 |

---
*Update after completing each phase or encountering errors*
