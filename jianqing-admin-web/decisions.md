# Decisions

## custom-数据范围前端先做配置闭环

- Date: 2026-03-17
- Status: active
- Scope: frontend/data-scope/custom

### Decision

前端对 `CUSTOM` / 自定义部门数据范围先做“配置闭环 + 联调闭环”，不先引入额外复杂交互层。

### Why

- 当前角色页与用户页数据权限主线已稳定。
- 先把配置入口、文案与联调闭环打通，更容易验证后端模型是否可用。
- 过早增加复杂 UI 容器或通用抽象，会放大页面状态与回归成本。

### Constraints

- 保持统一列表骨架与反馈节奏。
- 优先复用现有角色页数据范围常量、表单结构与部门树能力。
- 联调完成后必须恢复测试基线，确保控制台干净。

---

## 列表页统一骨架继续保持

- Status: active
- Scope: frontend/list-page/skeleton

### Decision

列表页继续遵循“头部工具区 + 表格区 + 分页区”统一骨架。

### Why

- 保持系统页、审计页及后续页面的交互节奏一致。
- 降低样式漂移与局部补丁累积。

---

## 高重复逻辑优先抽共享层

- Status: active
- Scope: frontend/shared/reuse-rule

### Decision

同类页面重复出现第 2 次时，就评估是否抽成 composable、utils 或共享组件。

### Why

- 降低页面脚本膨胀。
- 保持 AI 与开发者都能快速理解页面结构。
