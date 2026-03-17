# Current State

## 当前阶段
- Phase 54 已完成：系统菜单层级已完成整理，参数/字典与代码生成已从系统管理拆分。

## 最近完成
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
- 当前顶层菜单基线：`系统管理`、`参数管理`、`开发工具`、`审计日志`。
- `dept_scope_role` 已恢复为 `DEPT`，联调基线未污染。
- 当前真实部门树：`简擎总部(1) -> 外部协作部(3)`。
- 可用联调账号：`admin/admin123`；`dept_user/test123`；`self_user/test123`；`other_user/test123`；`outside_user/test123`。

## 下一步最小任务
1. 如需沉淀版本，提交本次菜单层级整理改动。
2. 如需继续治理，可再补查历史文档与说明文案的一致性。

## 关键约束
- 普通后端任务优先只读取本文件；仅在需要历史细节时再展开 `backlog.md / decisions.md / task_plan.md / findings.md / progress.md`。
- 后端继续遵循阿里巴巴 Java 规范、极简可读、先最小闭环再增强。
