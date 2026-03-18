# Decisions

## custom-数据范围先走最小闭环

- Date: 2026-03-17
- Status: active
- Scope: backend/data-scope/custom

### Decision

`CUSTOM` / 自定义部门数据范围先做最小闭环，不先扩成复杂授权矩阵。

### Why

- 当前“全部 / 本部门及以下 / 本部门 / 本人”主线已经稳定。
- 先补最小闭环更利于控制复杂度、验证模型边界与真实联调可行性。
- 过早引入复杂抽象，会增加后端服务编排、前端配置与真实回归成本。

### Constraints

- 继续遵循阿里巴巴 Java 规范与极简可读原则。
- 优先复用现有数据权限判定与部门树能力，不叠补丁式扩散逻辑。
- 联调后必须恢复测试基线，避免污染真实权限账号状态。

---

## system-serviceimpl-保持聚合入口定位

- Status: active
- Scope: backend/architecture/system-serviceimpl

### Decision

`SystemServiceImpl` 只保留访问控制、事务边界与跨域编排；新增复杂写流程优先下沉到独立 handler。

### Why

- 避免热点类再次膨胀。
- 保持写路径职责直白，便于测试与 AI/人工协作接续。

---

## 缓存失效走提交后触发

- Status: active
- Scope: backend/cache/post-commit-eviction

### Decision

写操作优先在事务提交后触发缓存失效，不允许新增“事务未完成先删缓存”的写路径。

### Why

- 正确性优先于抽象与性能。
- 可以降低 DB 与缓存短暂不一致风险。

---

## system-notice-先做站内通知最小闭环

- Date: 2026-03-17
- Status: active
- Scope: backend/system-notice/mvp

### Decision

系统通知首期采用“通知定义 + 用户收件箱”双层模型，只做站内通知最小闭环，不提前扩展到短信、邮件、企微等外部通道。

### Why

- 当前需求核心是后台内消息触达、未读提示、消息列表与已读状态，站内闭环最能直接验证产品路径。
- 将通知定义与用户收件箱拆开，可以同时覆盖定时发布、已读状态、撤回/统计等后续能力。
- 过早引入外部通道会放大配置、重试、模板与失败补偿复杂度，不利于当前阶段快速稳定落地。

### Constraints

- 保持后端实现极简可读，优先复用现有 system 模块风格与统一 `GET/POST` API 约束。
- 发布时应固化接收人快照，避免角色/部门后续变化污染历史消息可见性。
- 定时发布首期优先使用应用内调度，后续再视体量评估消息总线或独立任务中心。
