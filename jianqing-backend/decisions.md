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
