# Current State

## 当前阶段
- Phase 55 进行中：系统级消息通知功能进入方案落地与最小闭环实现阶段。

## 最近完成
- 已完成真实通知链路烟测：本地运行中的 `127.0.0.1:8080` 已验证 `admin/admin123` 登录、新建通知、发布、未读数增加、单条已读、全部已读、删除进垃圾箱、恢复、彻底删除整条链路可用。
- 已完成通知交互增强提交并推送：`f800125 完善系统通知交互并补齐垃圾箱能力` 已推送至 `origin/master`。
- 已补齐通知实时链路：`/api/system/my-notices/stream` SSE、`/api/system/my-notices/realtime` 快照接口、发布/已读/删除后的实时刷新已打通。
- 已补齐通知垃圾箱能力：通知删除改为移入垃圾箱，支持按已发布/未发布分类查询、恢复、彻底删除。
- 已补齐通知后端新回归：`mvn -Dtest=NoticeServiceImplTest,NoticeSchemaInitializerTest test` 11 passed。
- 已完成默认端口通知接口联调复核：`127.0.0.1:8080` 上 `unread-count`、`realtime`、`notices`、`notices/trash` 均返回 `code=0`，SSE 流可返回 `notice-sync` 事件。
- 已补齐通知菜单与权限种子：新增 `消息中心(/messages)`、`我的消息`、`通知管理` 及 `system:notice:*` 按钮权限初始化。
- 已完成通知菜单初始化回归：`mvn -Dtest=NoticeMenuInitializerTest,NoticeSchemaInitializerTest,NoticeServiceImplTest test` 10 passed。
- 已完成默认端口验证：`127.0.0.1:8080` 上管理员菜单树已出现 `消息中心 -> 我的消息/通知管理`，且 `auth/me` 已返回完整 `system:notice:list/add/edit/publish/cancel/delete` 权限。
- 已补齐通知表旧库兼容迁移：`NoticeSchemaInitializer` 现会补列并修正通知相关字段类型定义。
- 已完成通知真实联调烟测：创建通知 -> 发布 -> 未读数 +1 -> 打开详情自动已读 -> 未读数归零。
- 通知后端最新回归通过：`mvn clean -Dtest=NoticeServiceImplTest,NoticeSchemaInitializerTest test` 8 passed。
- 已落系统通知后端基础闭环：新增通知主表、目标表、用户收件箱表与对应 schema initializer。
- 已新增通知管理接口：列表、详情、新增、编辑、发布、取消、删除。
- 已新增我的消息接口：列表、详情、未读数、最近消息、弹窗候选、单条已读、全部已读。
- 已接入应用内定时发布扫描任务，支持 `PENDING` 通知按计划时间发布。
- 已完成通知后端回归：`mvn -Dtest=NoticeServiceImplTest,NoticeSchemaInitializerTest test` 通过。
- 新增 `参数管理(/settings)` 与 `开发工具(/dev-tools)` 两个顶层菜单编排初始化。
- `system:config:list`、`system:dict:list` 已归入 `参数管理`；`system:generator:list` 已归入 `开发工具`。
- 已同步更新初始化 SQL，避免新库仍落到旧菜单层级。
- 已完成菜单初始化回归：`mvn -Dtest=SystemMenuCatalogInitializerTest,GeneratorMenuInitializerTest test` 通过。
- 新增 `jq_sys_role_dept` 角色-部门关联表，并支持角色保存/删除时同步维护自定义部门范围。
- `UserDataScopeResolver` 已支持合并 `CUSTOM` / `DEPT_AND_CHILD` / `DEPT` 多角色部门范围。
- 已补充 `CUSTOM` 相关后端测试，目标回归 `17 passed`。
- 已完成默认端口真实接口回归：在 `127.0.0.1:8080` 上验证 `dept_scope_role -> CUSTOM + [3]` 后，`dept_user` 仅可见 `outside_user`，随后已恢复基线。
- 数据权限已从“全部 / 本部门 / 本人”扩展到“全部 / 本部门及以下 / 本部门 / 本人”。
- 已完成角色页、用户页、真实账号联调与树形边界单测补强。
- 后端最新回归通过：`mvn test` 126 passed。

## 当前基线
- 当前顶层菜单基线：`系统管理`、`参数管理`、`开发工具`、`消息中心`、`审计日志`。
- 新需求基线已确认：系统通知采用“通知定义 + 用户收件箱”双层模型，已覆盖立即/定时发布、站内列表、详情、批量已读、铃铛实时更新、全局弹窗分页、垃圾箱。
- `dept_scope_role` 已恢复为 `DEPT`，联调基线未污染。
- 当前真实部门树：`简擎总部(1) -> 外部协作部(3)`。
- 可用联调账号：`admin/admin123`；`dept_user/test123`；`self_user/test123`；`other_user/test123`；`outside_user/test123`。

## 下一步最小任务
1. 补一轮浏览器级真实联调，确认弹窗分页、铃铛实时更新、垃圾箱恢复/彻底删除在页面交互上完全一致。
2. 视使用反馈决定是否补通知保留/归档策略，避免垃圾箱与物理清理边界长期膨胀。
3. 如继续增强，可补普通用户菜单分配策略或通知统计能力。

## 关键约束
- 普通后端任务优先只读取本文件；仅在需要历史细节时再展开 `backlog.md / decisions.md / task_plan.md / findings.md / progress.md`。
- 后端继续遵循阿里巴巴 Java 规范、极简可读、先最小闭环再增强。
