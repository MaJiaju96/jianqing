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
- 两端均维护轻量摘要文件：`current_state.md`
- 两端当前主规划骨架文件：`task_plan.md`
- 两端任务状态主源文件：`backlog.md`
- 两端关键决策文件：`decisions.md`
- 两端均保留：`findings.md`、`progress.md`
- 工作区 planning / memory 分层规范：`PLANNING_MEMORY_CONVENTIONS.md`

## 3) 口令触发：续开发协议

当用户输入以下任一口令时，默认执行“续开发协议”：

- `我们现在继续上次未完成的简擎项目开发`
- `继续简擎项目`
- `继续未完成的简擎项目`
- `恢复简擎开发上下文`

### 续开发协议步骤（自动执行）

1. 默认先做轻量恢复，优先只读取：
   - `jianqing-backend/current_state.md`
   - `jianqing-admin-web/current_state.md`
2. 输出“当前状态 + 下一步最小可执行任务（1~3项）”。
3. 若用户指令已明确偏后端或偏前端，则后续仅按该侧继续读取与执行。
4. 仅在以下情况再按需展开读取对应侧的 `backlog.md / decisions.md / task_plan.md / findings.md / progress.md`：
   - 需要当前任务状态与优先级
   - 需要历史决策依据
   - 需要追溯旧 phase 明细
   - 需要同步更新 planning 文件
   - 当前摘要不足以支撑实现
5. 若涉及跨前后端改动，两个项目对应的 planning / memory 文件都要同步更新。

## 4) 开发风格补充

- 新增功能先做最小闭环，再渐进增强。
- 优先复用与封装重复逻辑，保持接口与命名语义直白。
- 允许为后续多分支演进预留扩展点（前端 TS/Vue2、后端 SB2/SC/JDK8）。

## 5) Token 消耗优化约定

- 普通任务按侧读取：后端任务优先只读后端规范与后端 `current_state.md`；前端任务优先只读前端规范与前端 `current_state.md`。
- 非跨端任务不默认加载双端上下文，避免无关上下文占用 token。
- 先最小必要读取，再按需扩展，避免一次性全仓深扫。
- 日常续开发优先读取 `current_state.md`，不要默认一次性读取两端全部 `task_plan.md / findings.md / progress.md`。
- 当前任务状态优先读取 `backlog.md`，不要通过多个 planning 文件反推状态。
- 关键 why 优先读取 `decisions.md`，不要把大段决策历史继续堆回 `task_plan.md`。
- `progress.md` 以存档价值为主，非需要历史细节时不作为默认首读文件。


# Memorix — Automatic Memory Rules

You have access to Memorix memory tools. Follow these rules to maintain persistent context across sessions.

## RULE 1: Session Start — Load Context

At the **beginning of every conversation**, BEFORE responding to the user:

1. Call `memorix_session_start` to get the previous session summary and key memories (this is a direct read, not a search — no fragmentation risk)
2. Then call `memorix_search` with a query related to the user's first message for additional context
3. If search results are found, use `memorix_detail` to fetch the most relevant ones
4. Reference relevant memories naturally — the user should feel you "remember" them

## RULE 2: Store Important Context

**Proactively** call `memorix_store` when any of the following happen:

### What MUST be recorded:
- Architecture/design decisions → type: `decision`
- Bug identified and fixed → type: `problem-solution`
- Unexpected behavior or gotcha → type: `gotcha`
- Config changed (env vars, ports, deps) → type: `what-changed`
- Feature completed or milestone → type: `what-changed`
- Trade-off discussed with conclusion → type: `trade-off`

### What should NOT be recorded:
- Simple file reads, greetings, trivial commands (ls, pwd, git status)

### Use topicKey for evolving topics:
For decisions, architecture docs, or any topic that evolves over time, ALWAYS use `topicKey` parameter.
This ensures the memory is UPDATED instead of creating duplicates.
Use `memorix_suggest_topic_key` to generate a stable key.

Example: `topicKey: "architecture/auth-model"` — subsequent stores with the same key update the existing memory.

### Track progress with the progress parameter:
When working on features or tasks, include the `progress` parameter:
```json
{
  "progress": {
    "feature": "user authentication",
    "status": "in-progress",
    "completion": 60
  }
}
```
Status values: `in-progress`, `completed`, `blocked`

## RULE 3: Resolve Completed Memories

When a task is completed, a bug is fixed, or information becomes outdated:

1. Call `memorix_resolve` with the observation IDs to mark them as resolved
2. Resolved memories are hidden from default search, preventing context pollution

This is critical — without resolving, old bug reports and completed tasks will keep appearing in future searches.

## RULE 4: Session End — Store Decision Chain Summary

When the conversation is ending, create a **decision chain summary** (not just a checklist):

1. Call `memorix_store` with type `session-request` and `topicKey: "session/latest-summary"`:

   **Required structure:**
   ```
   ## Goal
   [What we were working on — specific, not vague]

   ## Key Decisions & Reasoning
   - Chose X because Y. Rejected Z because [reason].
   - [Every architectural/design decision with WHY]

   ## What Changed
   - [File path] — [what changed and why]

   ## Current State
   - [What works now, what's pending]
   - [Any blockers or risks]

   ## Next Steps
   - [Concrete next actions, in priority order]
   ```

   **Critical: Include the "Key Decisions & Reasoning" section.** Without it, the next AI session will lack the context to understand WHY things were done a certain way and may suggest conflicting approaches.

2. Call `memorix_resolve` on any memories for tasks completed in this session

## RULE 5: Compact Awareness

Memorix automatically compacts memories on store:
- **With LLM API configured:** Smart dedup — extracts facts, compares with existing, merges or skips duplicates
- **Without LLM (free mode):** Heuristic dedup — uses similarity scores to detect and merge duplicate memories
- **You don't need to manually deduplicate.** Just store naturally and compact handles the rest.
- If you notice excessive duplicate memories, call `memorix_deduplicate` for batch cleanup.

## Guidelines

- **Use concise titles** (~5-10 words) and structured facts
- **Include file paths** in filesModified when relevant
- **Include related concepts** for better searchability
- **Always use topicKey** for recurring topics to prevent duplicates
- **Always resolve** completed tasks and fixed bugs
- **Always include reasoning** — "chose X because Y" is 10x more valuable than "did X"
- Search defaults to `status="active"` — use `status="all"` to include resolved memories
