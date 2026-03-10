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
- 已新增 `docs/FRONTEND_CONVENTIONS.md`，沉淀前端注释规则、常量收敛、AI 协作与公共层约定。
- 已新增 `src/constants/app.js`，统一收敛成功码、分页、状态、菜单类型与超级管理员角色等公共常量。
- 已新增 `src/utils/validators.js`，统一收敛手机号、邮箱、角色编码、权限标识校验逻辑。
- 已完成一轮前端可读性整理：减少视图层魔法值、收敛重复校验、补齐 README 对规范文档的入口。
- 已新增 `src/composables/usePermissions.js`，统一收敛页面批量权限显隐的 computed 模式。
- 已完成一轮前端构建体积优化：路由改为懒加载，Element Plus 改为按需组件解析，消除大 chunk 构建告警。
- 已补齐站点 favicon，并统一关键列表页的加载中/暂无数据文案与保存、删除按钮 loading 反馈。
- 已新增 `src/composables/useAsyncState.js`，统一列表页加载态、按钮提交态与行级删除 loading，减少页面重复异步反馈代码。
- 已新增 `src/utils/feedback.js`，统一登录、登出及系统管理写操作成功提示文案，并为审计查询输入增加回车触发。
- 页面巡检发现 Element Plus locale 在按需引入改造后回退为英文，已通过 `App.vue` 的 `ElConfigProvider` 恢复中文分页/下拉等文案。
- 已启动 v0.2 数据权限前端收口：角色页增加“数据范围”展示与编辑，当前与后端最小闭环保持一致，仅支持全部/本部门/本人三种范围。
- 已补齐部门管理页面：前端新增 `DeptsView`、系统 API 与侧边栏路由接入，部门管理不再只是 seed 菜单项。
- 已将用户管理里的“部门ID”输入升级为真实部门下拉，并在列表中显示所属部门名称，降低对纯数字 ID 的依赖。
- 已将部门管理里的“负责人ID”升级为真实用户选择，并在列表中显示负责人姓名（昵称 + 用户名）。
- 已统一用户页与部门页筛选交互：搜索词与状态筛选均改为“查询 / 重置”模式，支持回车触发查询。
- 已补齐列表区自适应固定高度能力：系统页与审计页表格统一改为随视口高度计算 `max-height`，数据超出时走表格内部滚动。
- 已修复部门页弹窗可能被页面内容遮挡的问题：关键弹窗统一追加到 body 渲染。
- 部门页已补齐分页，且分页后去掉树展开按钮，改为层级缩进展示，避免分页与树展开混用造成视觉混乱。
- 已统一角色页与菜单页筛选交互，系统管理四个主列表页现均采用一致的“查询 / 重置”模式。
- 已统一审计页筛选交互，操作日志与登录日志现也采用显式“查询 / 重置”模式，前后台列表页筛选心智已基本一致。
- 已将所有查询列表从“根据数据量伸缩”收敛为“固定占满可用工作区高度”：表格统一使用 `height` 而非 `max-height`，空数据时也保持稳定骨架。
- 已补齐页面宽高响应式适配：`MainLayout` 主内容区允许内部滚动，列表卡片高度随视口变化；工具栏在窄屏下自动换行，小屏时控件自动纵向铺开。
- 已完成 768 / 640 宽度巡检：系统与审计列表页无主内容横向溢出；移动端分页区补齐左对齐与横向滚动，避免窄屏下分页控件拥挤。
- 已完成最新一轮真实账号联调复核：角色页数据范围列与编辑值正常；部门页列表、分页与负责人展示正常；`dept_user` 仅见本部门用户，`self_user` 仅见自己。
- 当前开发联调测试账号口径已统一：`admin/admin123`，其余测试账号统一使用 `test123`。
- 已将系统页与审计页列表统一为“表格卡片 + 独立分页卡片”双区块布局，表格与分页行视觉彻底分离，页面层次更清晰。
- 已进一步重构列表页顶部工具区：移除冗余页面标题，改为左侧筛选/查询、右侧刷新/新增的模块化布局，更贴近管理台内容区操作习惯。
- 已为列表工具栏补齐主题变量：工具栏面板、输入框、普通按钮、刷新按钮、表格面板、分页面板均随主题联动，不再只有白色块感。
- 已完成列表页高度逻辑重构：移除 `useAdaptiveTable` 与全局 `offset` 调参方案，统一改为 `flex` 布局分配高度（分页贴底、表格区 `height=100%` 填充剩余空间）。

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
| 可读性优先通过结构与常量表达 | 让代码先自解释，再只为复杂边界补必要注释 |
| 前端协作规范文档化 | 降低后续 AI/人工接手时的上下文恢复成本 |
| 优先做路由级拆包与组件按需引入 | 以最小改动降低首屏包体积与构建告警 |
| 列表页优先提供明确反馈文案与按钮 loading | 用最小交互成本提升操作可感知性，避免重复提交 |
| 重复异步反馈优先抽轻量 composable | 在不引入复杂抽象前提下收敛 loading/空态样板代码 |
| 成功反馈文案优先复用统一工具 | 保持交互语义一致，避免页面各自拼接提示文本 |
| 全局 locale 优先通过根组件配置 | 避免按需引入或插件调整时出现分页/选择器文案回退 |
| 数据权限首批仅暴露后端已落地范围 | 保持前后端语义一致，先完成最小闭环再扩展更多选项 |
| 部门管理先采用树表 + 轻表单 | 先做最小可用部门树维护，避免过早引入复杂拖拽/级联编辑 |
| 用户表单优先显示真实部门名称 | 让部门字段具备业务语义，减少手填 ID 带来的误操作 |
| 部门负责人优先显示真实用户身份 | 让组织信息具备可读性，避免负责人字段再次退回为纯数字 |
| 列表筛选优先采用显式查询/重置 | 减少输入中途抖动，统一不同页面的筛选心智 |
| 列表区优先固定工作区高度 | 保持页面骨架稳定，数据增减只影响表格内部滚动，不再推高整页 |
| 关键弹窗统一 append-to-body | 避免被表格固定列、卡片容器或布局层级遮挡 |
| 系统管理同类列表优先统一筛选交互 | 避免用户在用户/部门/角色/菜单页来回切换时出现操作心智不一致 |
| 审计页也保持同样的查询/重置节奏 | 保证跨模块操作习惯一致，不因前后端分页差异破坏使用体验 |
| 列表表格优先使用固定 `height` 而非 `max-height` | 让空数据/少数据场景也占满工作区，保持整体布局稳定 |
| 响应式布局优先通过共享样式与 composable 收口 | 减少各页面重复算高、重复写断点样式，统一宽高适配行为 |
| 小屏分页优先允许横向滚动并靠左对齐 | 保证 640px 级别宽度下分页仍可操作，不强行压缩控件宽度 |
| 列表区继续采用“表格面板 + 分页面板”双卡片 | 强化信息层次，让表格主体与翻页操作视觉解耦 |
| 列表顶部不再重复展示页面标题 | 左侧菜单已提供当前位置，内容区优先留给筛选与操作模块 |
| 列表布局优先 `flex` 结构，不再依赖魔法高度参数 | 减少反复微调 `offset` 造成的维护负担，保持结构稳定与可读性 |

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
- `src/composables/useAsyncState.js`
- `src/utils/feedback.js`
- `src/views/LoginView.vue`
- `src/layouts/MainLayout.vue`
- `src/App.vue`
- `src/views/system/RolesView.vue`
- `src/constants/app.js`
- `src/views/system/DeptsView.vue`
- `src/router/index.js`
- `src/layouts/MainLayout.vue`
- `src/api/system.js`
- `src/views/system/UsersView.vue`
- `src/views/system/DeptsView.vue`
- `src/views/system/RolesView.vue`
- `src/views/system/MenusView.vue`
- `src/views/audit/OperLogsView.vue`
- `src/views/audit/LoginLogsView.vue`
- `src/views/system/RolesView.vue`
- `src/views/system/MenusView.vue`
- `src/layouts/MainLayout.vue`
- `src/router/index.js`
- `src/views/system/MenusView.vue`
- `src/views/system/RolesView.vue`
- `src/stores/auth.js`
- `src/api/system.js`
- `src/views/audit/OperLogsView.vue`
- `src/views/audit/LoginLogsView.vue`
- `docs/FRONTEND_CONVENTIONS.md`
- `src/constants/app.js`
- `src/utils/validators.js`
- `src/composables/usePermissions.js`
- `src/router/index.js`
- `src/main.js`
- `vite.config.js`
- `index.html`
- `public/favicon.svg`
- `src/views/system/UsersView.vue`
- `src/views/system/RolesView.vue`
- `src/views/system/MenusView.vue`
- `src/views/audit/OperLogsView.vue`
- `src/views/audit/LoginLogsView.vue`

## Cross-Project Sync
- 若接口字段或认证策略变更，需要同步更新 `jianqing-backend/progress.md` 与本文件。
