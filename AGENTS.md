# 简擎工作区代理规范（AGENTS）

本文件作用域：`/Users/majiaju/Person/code/jianqing` 及其全部子目录。

## 1) 项目总原则（必须遵守）

- 项目定位：极简、可读、开发者友好、AI 协作友好。
- 后端代码：严格遵循阿里巴巴 Java 开发规范。
- 实现方式：优先简单直接逻辑，避免炫技式复杂抽象。
- 允许在必要时先重构方法/组件，再新增功能；禁止在历史问题代码上叠补丁式“继续堆屎山”。
- 注释策略：关键分支/复杂流程/边界处理写必要注释，避免注释噪音。

## 2) 目录与上下文约定

- 工作区根目录：`jianqing/`
- 后端目录：`jianqing-backend/`
- 前端目录：`jianqing-admin-web/`
- 两端均维护三文件：`task_plan.md`、`findings.md`、`progress.md`

## 3) 口令触发：续开发协议

当用户输入以下任一口令时，默认执行“续开发协议”：

- `我们现在继续上次未完成的简擎项目开发`
- `继续简擎项目`
- `恢复简擎开发上下文`

### 续开发协议步骤（自动执行）

1. 先读取以下 6 个文件并总结当前阶段：
   - `jianqing-backend/task_plan.md`
   - `jianqing-backend/findings.md`
   - `jianqing-backend/progress.md`
   - `jianqing-admin-web/task_plan.md`
   - `jianqing-admin-web/findings.md`
   - `jianqing-admin-web/progress.md`
2. 输出“当前状态 + 下一步最小可执行任务（1~3项）”。
3. 在不违背用户新指令前提下，优先按计划中 `in_progress` 和最近未完成项继续。
4. 若涉及跨前后端改动，两个项目的 planning 文件都要同步更新。

## 4) 开发风格补充

- 新增功能先做最小闭环，再渐进增强。
- 优先复用与封装重复逻辑，保持接口与命名语义直白。
- 允许为后续多分支演进预留扩展点（前端 TS/Vue2、后端 SB2/SC/JDK8）。

## 5) Token 消耗优化约定

- 普通任务按侧读取：后端任务优先只读后端规范与后端 planning；前端任务优先只读前端规范与前端 planning。
- 非跨端任务不默认加载双端上下文，避免无关上下文占用 token。
- 先最小必要读取，再按需扩展，避免一次性全仓深扫。
