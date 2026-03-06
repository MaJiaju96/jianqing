# Contributing Guide

## 目标

本项目欢迎围绕 `简擎` 后端内核的功能完善、文档改进和工程化能力增强。

## 提交前约定

- 后端代码遵循阿里巴巴 Java 开发规范。
- 优先简单、直接、可读实现，避免过度抽象。
- 关键分支、复杂流程、边界处理补充必要注释。
- 保持与现有目录结构、命名风格一致。

## 分支策略

- `main`：稳定主分支，仅合并已验证改动。
- `feature/*`：新功能开发分支。
- `fix/*`：缺陷修复分支。
- `docs/*`：文档改进分支。

建议流程：

1. 从 `main` 拉取最新代码并创建个人分支。
2. 在个人分支完成改动与自测。
3. 提交 PR，描述变更背景、方案与验证结果。
4. 通过 review 后合并到 `main`。

## Commit 建议

建议保持一个提交只做一类事情，便于审查和回滚。

示例：

- `feat(auth): add token refresh endpoint`
- `fix(system): correct role-menu relation query`
- `docs(readme): update quick start and roadmap`

## PR 模板建议

- 变更背景：为什么改。
- 变更内容：改了什么。
- 影响范围：涉及哪些模块。
- 验证记录：如何验证、结果如何。
- 回滚说明：异常时如何回退。

## 质量门禁（当前建议）

- 本地编译通过：`mvn -q -DskipTests compile`
- 单元测试通过：`mvn test`
- 核心接口手工联调通过（登录、系统管理、审计日志）

## 本地 pre-commit 守卫（推荐）

建议安装本地 git hook，在提交前自动执行结构与质量检查：

```bash
bash scripts/install-git-hooks.sh
```

安装后每次 `git commit` 前会自动运行：

- `bash scripts/check-service-structure.sh`
- `bash scripts/check-http-method-constraints.sh`
- `bash scripts/check-mapper-layering.sh`
- `mvn -DskipTests compile`
- `mvn checkstyle:check`

## 文档同步

涉及架构/能力变更时，请同步更新：

- `README.md`
- `docs/API_EXAMPLES.md`
- `ARCHITECTURE.md`（如存在架构变化）
- `task_plan.md` / `findings.md` / `progress.md`
