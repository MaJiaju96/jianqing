# 简擎后端开发规范（Backend Conventions）

适用范围：`jianqing-backend/`

## 1. 基础规则

- 严格遵循《阿里巴巴 Java 开发手册（最新版）》。
- 优先简单、可读、可维护实现，避免过度抽象。
- 允许必要重构；禁止在历史问题代码上持续叠补丁。

## 2. 分层与边界

- 严格分层：Controller → Service → Mapper/DAO → Entity。
- Controller 禁止直接调用 Mapper。
- 统一返回结构：`ApiResponse<T>`（`code/msg/data`）。

## 3. 命名与常量

- 类名 PascalCase，方法/变量 camelCase，常量全大写下划线。
- 禁止魔法值，使用常量语义化表达。

## 4. 异常与日志

- 禁止空 catch。
- 业务异常使用业务异常类型（如 `BusinessException`）。
- 日志使用 SLF4J；关键链路日志应包含 traceId。

## 5. 安全与依赖

- 参数校验必须使用 Validation（如 `@Valid`）。
- 密码使用 BCrypt，敏感数据按需脱敏/加密。
- 禁止硬编码密钥/密码。
- 依赖版本由父 POM 统一管理，清理无用依赖。

## 6. 质量门禁（当前仓库）

- pre-commit / CI 已接入：
  - `check-service-structure.sh`
  - `check-http-method-constraints.sh`（仅 GET/POST）
  - `check-mapper-layering.sh`
  - `mvn -DskipTests compile`
  - `mvn checkstyle:check`

## 7. Token 消耗优化（AI 协作）

- 后端任务默认只读取后端上下文与后端 planning 文件。
- 非跨端需求，禁止同时加载前端大文件与前端 planning。
- 先读取最小必要文件（目标模块 + 相关 service/controller），避免全仓扫描。
