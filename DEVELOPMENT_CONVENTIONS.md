# 简擎统一开发规范（执行版）

适用范围：`/Users/majiaju/Person/code/jianqing` 全仓（后端 + 前端 + 数据库 + 协作流程）。

> 子规范入口：
> - 后端：`jianqing-backend/docs/BACKEND_CONVENTIONS.md`
> - 前端：`jianqing-admin-web/docs/FRONTEND_CONVENTIONS.md`

---

## 0. 执行分级与落地方式

### 0.1 规则分级

- **MUST**：必须遵守，违反默认阻断提交或阻断合并。
- **SHOULD**：强烈建议遵守，特殊场景可说明后例外。
- **MAY**：可选优化项。

### 0.2 执行矩阵（当前）

| 规则类型 | 当前执行方式 | 状态 |
|---|---|---|
| 后端分层约束（Controller→Service→Mapper） | pre-commit + CI 脚本 | 已自动化 |
| 后端 HTTP 方法约束（仅 GET/POST） | pre-commit + CI 脚本 | 已自动化 |
| Mapper 引用边界（仅 service.impl 可引 Mapper） | pre-commit + CI 脚本 | 已自动化 |
| 后端 Checkstyle / 编译 / 测试 | Maven + CI | 已自动化 |
| 前端风格与结构规范 | 文档 + Code Review | 文档约束 |
| 数据库命名/SQL规范 | 文档 + Code Review | 文档约束 |

---

## 1. 全局工程原则（MUST）

1. 先保证可读、可维护，再做技巧型优化。
2. 优先简单直接实现，避免过度抽象。
3. **必要时允许重构方法/组件，禁止在历史屎山上继续堆叠补丁式逻辑。**
4. 改动必须有最小闭环验证（至少编译或构建通过）。

---

## 2. Java 后端开发规范

### 2.1 基础遵循

- **MUST**：遵循《阿里巴巴 Java 开发手册（最新版）》。
- **MUST**：禁止魔法值，使用常量表达（`MAX_PAGE_SIZE`）。
- **SHOULD**：在性能敏感热点循环中，避免无意义对象重复创建。

### 2.2 命名规范

- **MUST**：类名 PascalCase，方法/变量 camelCase，常量全大写下划线。
- **MUST**：包名全小写，按模块分层。
- **SHOULD**：布尔 POJO 字段避免 `isXxx` 命名（规避序列化歧义）。

### 2.3 分层与结构

- **MUST**：Controller → Service → Mapper/DAO → Entity，禁止跨层直连。
- **MUST**：Controller 不直接调用 Mapper。
- **MUST**：统一返回结构 `Result<T>` / `ApiResponse<T>`，包含 `code/msg/data`。

### 2.4 异常与日志

- **MUST**：禁止空 `catch`。
- **MUST**：业务异常使用业务异常类型（如 `BusinessException`），避免裸抛 `RuntimeException`。
- **MUST**：日志使用 SLF4J，区分级别；关键链路日志包含 traceId。

### 2.5 依赖与构建

- **MUST**：依赖版本在父 POM 统一管理，禁止重复和无用依赖。
- **SHOULD**：第三方 SDK 经适配层封装后再进入业务层。

### 2.6 安全

- **MUST**：参数校验（`@Valid` / Validation）不可缺失。
- **MUST**：密码使用 BCrypt，敏感数据按需脱敏/加密。
- **MUST**：禁止硬编码密钥/密码。

---

## 3. 前端开发规范

### 3.1 基础规范

- **MUST**：遵循 ESLint + Prettier，提交前通过 lint/build。
- **MUST**：组件 PascalCase、变量/方法 camelCase、常量全大写下划线。
- **SHOULD**：避免行内样式；仅临时调试场景允许且应尽快收敛。

### 3.2 结构规范

- **MUST**：按模块划分目录，禁止无边界堆文件。
- **SHOULD**：复杂逻辑抽离到 composables/hooks/utils。
- **MUST**：模板层不写复杂表达式，抽到计算属性/方法。

### 3.3 接口与交互

- **MUST**：统一请求封装（token、超时、错误处理、拦截器）。
- **MUST**：统一解析 `code/msg/data`。
- **MUST**：异步操作提供加载态或占位态。

### 3.4 性能与兼容

- **MUST**：禁止直接 DOM 操作，优先框架 API。
- **SHOULD**：图片/大资源懒加载，按需加载大文件。

---

## 4. 数据库规范

### 4.1 命名与通用字段

- **MUST**：表名、字段名小写下划线；表名前缀统一。
- **MUST**：通用字段统一：`create_time/update_time/creator/status`。
- **MUST**：表与字段必须有注释。

### 4.2 类型与索引

- **MUST**：金额字段使用 `DECIMAL`，禁止 `FLOAT/DOUBLE`。
- **MUST**：高频查询字段建索引，遵循最左前缀。
- **MUST**：禁止滥建索引。

### 4.3 SQL 规范

- **MUST**：禁止 `SELECT *`。
- **MUST**：大表查询必须有索引条件。
- **SHOULD**：SQL 关键字大写，统一格式化。

---

## 5. Git 与协作规范

### 5.1 分支与提交流程

- **SHOULD**：使用 `feature/*`、`fix/*`、`docs/*` 分支开发。
- **MUST**：提交信息可读、可检索，禁止 `fix bug` 这类无语义提交。

### 5.2 文档同步

- **MUST**：涉及接口/架构/规范变更，必须同步更新：
  - `README.md`
  - `task_plan.md`
  - `findings.md`
  - `progress.md`

---

## 6. 与当前仓库现状对齐说明

1. 后端已有 pre-commit + CI 自动门禁，继续保持。
2. 前端与数据库当前以“文档 + CR”约束为主，后续可逐步增加自动化门禁。
3. 若规范与现状冲突，以“先可执行再收紧”原则推进，不写无法落地的条款。

---

## 6.1 架构优化执行规则（MUST）

本节用于固化本轮前后端代码健康治理的经验，避免后续开发只补功能、不维护项目整体美感与架构优势。

### A. 优化目标排序

1. **先正确性，再可读性，再复用，再性能。**
2. 不允许为了“看起来抽象”而牺牲可读性。
3. 不允许为了赶功能而在已识别热点类/热点页面上继续堆补丁。

### B. 后端优化规则

- **MUST**：跨表写操作必须声明清晰事务边界。
- **MUST**：缓存失效必须优先保证提交后触发，不允许事务未提交先删缓存。
- **MUST**：`SystemServiceImpl` 这类聚合入口只保留访问控制、事务边界、跨域编排；具体写路径下沉到独立 handler。
- **SHOULD**：新增复杂写流程时，优先判断是否应落在现有 `*WriteOperationHandler` / `*AssignmentHandler`，而不是继续塞回主服务。
- **MUST**：新增跨实体协作优先走 service/handler，不允许重新回退为跨层直接拼 mapper。

### C. 前端优化规则

- **MUST**：列表页继续遵循“头部工具区 + 表格区 + 分页区”统一骨架。
- **MUST**：同类页面出现第 2 次重复时就要评估是否抽共享 composable / utils / 组件。
- **SHOULD**：优先抽“固定节奏”逻辑（loading、reset、refresh、submit 收尾），不要把领域校验一次性抽成大而全通用层。
- **MUST**：共享组件优先承载外壳结构与稳定交互节奏，页面保留领域差异。
- **MUST**：树结构处理、菜单类型元数据、列表头部工具栏等高重复逻辑，不允许再次复制回页面内部。

### D. 重构边界规则

- **MUST**：采用“小步、行为不变、可回归”的收口方式。
- **MUST**：每轮重构都要给出最小验证结果（后端至少 `mvn test`，前端至少 `npm run build`）。
- **SHOULD**：优先把一个热点拆成“聚合入口 + 轻量执行器/工具层”，而不是一次性改全模块。
- **MUST**：若重构影响公共约定，必须同步更新 `README`、planning 文件、规范文档。

### E. 美感约束

- **MUST**：管理台页面保持统一布局骨架、统一反馈节奏、统一按钮语义。
- **MUST**：代码层面保持“少量明确结构”优于“很多分散补丁”。
- **SHOULD**：新功能接入时优先复用现有共享层，避免视觉与代码风格分叉。

---

## 7. Token 消耗优化规则（MUST）

1. **按任务侧读取上下文**：
   - 后端任务仅加载后端规范与后端 planning；
   - 前端任务仅加载前端规范与前端 planning。
2. **非跨端任务禁止默认加载双端 planning 文件**。
3. **先最小读取，再按需扩展**：先读目标模块文件，再读依赖文件，避免全仓大范围扫描。
4. 规范查询优先读子规范文件（前端/后端），统一规范仅作总览入口。
