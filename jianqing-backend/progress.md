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

## Latest Updates
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

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-03-03 | 工具调用参数缺失/显示乱码 | 1 | 重试并补齐参数后执行成功 |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 9（架构收口与规范固化） |
| Where am I going? | 在保持当前结构优势的前提下，继续选择高收益的小步优化或回到功能开发主线 |
| What's the goal? | 构建可开源的简擎后端内核，并预留 ES/Nacos/RocketMQ 扩展 |
| What have I learned? | 后端热点服务应先做事务/缓存正确性加固，再按“聚合入口 + 轻量执行器”方式小步收口 |
| What have I done? | 已完成 system 关键写路径一致性加固、`SystemServiceImpl` handler 化收口，以及相关规划/规范文档同步 |

---
*Update after completing each phase or encountering errors*
