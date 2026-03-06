# Findings & Decisions

## Requirements
- 前后端必须是平级目录
- 前端尽量使用纯 JavaScript
- 页面设计不要死板，具备现代感
- 项目整体坚持极简、易读、可演进
- 代码需保证后续 AI 持续开发友好
- 需预留前端 `TS` 分支与 `Vue2` 分支演进空间

## Research Findings
- 后端接口已具备登录、系统管理、审计日志能力，可直接联调。
- Vite 代理 `/api -> http://127.0.0.1:8080` 可满足本地联调。
- 当前前端已无 `.ts` 文件，且 `npm run build` 通过。
- 已新增 `README.md`，覆盖项目概要、启动命令、构建命令、代理联调与常见问题。
- 项目已迁移至统一工作区：`/Users/majiaju/Person/code/jianqing`。
- 菜单权限页已从树列表改为树形表格，支持类型区分（目录/菜单/按钮）。
- 角色分配菜单已支持按“菜单权限/按钮权限”筛选，便于精确分配按钮权限。
- 侧边栏与路由已按权限标识显隐/拦截，审计日志不再默认对无权角色展示。
- 已将系统管理 API 写操作由 `http.put/http.delete` 统一迁移为 `http.post`，前端联调接口方法统一为 `GET`/`POST`。
- 菜单权限页已增加类型筛选（全部/目录菜单/按钮）与层级展示，菜单与按钮权限关系更清晰。
- 所有列表页面已统一接入分页（用户、角色、菜单、操作日志、登录日志）。
- 用户/角色/审计日志页已补齐“分野筛选”（状态/登录方式/关键字）并与分页联动。
- Element Plus 已切换中文本地化，分页统一展示中文“页”语义。

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| 使用 Vue3 + Element Plus | 快速构建管理台并保持组件一致性 |
| 使用 Hash 路由 | 简化本地与静态部署场景 |
| 使用 axios 拦截器处理认证 | 统一处理 token 注入与 401 退出 |
| 关键/复杂逻辑写必要注释 | 保持代码易读且避免过度注释 |
| 系统管理页采用前端搜索+分页 | 降低后端分页改造成本，先保证可用体验 |
| 操作按钮按权限显隐 | 提升前端交互安全性与角色体验一致性 |
| 前端优先简单易读实现 | 保持可维护性并降低学习成本 |
| 前端 API 调用仅使用 `GET` / `POST` | 与后端接口约束保持一致，避免联调歧义 |
| 列表页统一分页布局（total/sizes/pager） | 提升长列表可读性与浏览效率 |
| 分野筛选尽量走后端查询参数 | 保证筛选结果与分页总数一致，避免“只筛当前页”偏差 |

## Resources
- `src/router/index.js`
- `src/api/http.js`
- `src/views/LoginView.vue`
- `src/views/DashboardView.vue`
- `src/views/system/UsersView.vue`
- `src/views/system/RolesView.vue`
- `src/views/system/MenusView.vue`
- `src/views/audit/OperLogsView.vue`
- `src/views/audit/LoginLogsView.vue`
- `src/layouts/MainLayout.vue`
- `src/router/index.js`
- `src/views/system/MenusView.vue`
- `src/views/system/RolesView.vue`
- `src/stores/auth.js`
- `src/api/system.js`
- `src/views/audit/OperLogsView.vue`
- `src/views/audit/LoginLogsView.vue`

## Cross-Project Sync
- 若接口字段或认证策略变更，需要同步更新 `jianqing-backend/progress.md` 与本文件。
