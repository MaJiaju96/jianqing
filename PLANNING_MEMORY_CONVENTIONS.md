# 简擎 Planning / Memory 文档规范（v1）

适用范围：`/Users/majiaju/Person/code/jianqing` 全仓（后端 + 前端 + 跨端协作）。

目标：在保证长期可续开发的前提下，降低上下文噪音、减少 token 消耗、提升跨 session 恢复效率。

---

## 1. 核心原则

1. **默认只读最小入口，不默认深扫。**
2. **每类信息只保留一个主源头，避免多处重复维护。**
3. **主规划稳定，执行细节下沉。**
4. **本地文档负责当前推进，Memorix 负责长期记忆。**
5. **不采用按状态拆成多个 planning 主文件的方案。**

说明：

- 不建议拆成“已完成并回归 / 已完成未回归 / 未完成 / 未开始”四类 planning 文件。
- 推荐用单一 `backlog.md` 承载状态字段，避免文件碎片化与状态漂移。

---

## 2. 文档分层与职责

每个子项目（`jianqing-backend`、`jianqing-admin-web`）建议逐步收敛为以下结构：

```text
<subproject>/
├── current_state.md
├── task_plan.md
├── backlog.md
├── decisions.md
├── findings.md
├── progress.md
└── logs/
    └── daily/
        ├── 20260317.md
        └── 20260318.md
```

其中：

### 2.1 `current_state.md`（默认入口，必须保持很短）

职责：

- 当前阶段
- 当前基线
- 下一步最小任务（1~3 项）
- 按需展开读取指引

规则：

- **每次续开发优先读取它**
- 只保留当前最重要信息
- 不写长历史，不写流水账

### 2.2 `task_plan.md`（主线规划骨架）

职责：

- 项目目标
- Phase 主线
- 当前活跃阶段
- 中长期规划骨架

规则：

- 保持稳定，不继续堆积日常执行细节
- 作为“主规划索引”，不是“每日施工日志”
- 小任务过程、回归过程不要继续大量堆在这里

### 2.3 `backlog.md`（唯一状态源，推荐新增）

职责：

- 承载当前活跃任务与待办
- 记录任务状态与回归状态

规则：

- **任务状态只能在这里作为主源维护**
- 不再通过多个 planning 文件表达不同完成度

推荐字段：

```md
- [BE-DATA-001] CUSTOM 数据范围后端最小闭环
  Status: in_progress
  Regression: pending
  Priority: high
  Phase: 53
  Ref: decisions.md#custom-数据范围最小闭环
```

状态建议：

- `Status`: `not_started | in_progress | done | blocked`
- `Regression`: `not_needed | pending | verified`

### 2.4 `decisions.md`（关键 why）

职责：

- 关键设计决策
- trade-off
- 为什么选 A 不选 B
- 长期约束与边界

规则：

- 优先记录“为什么”，不要只写“改了什么”
- 与 Memorix 中的 decision / trade-off 记忆保持一致

### 2.5 `findings.md`（耐久发现）

职责：

- 可复用发现
- 重要 gotcha
- 调研结论
- 模块边界或真实环境约束

规则：

- 只记录未来还会复用的发现
- 一次性临时排查过程不要堆在这里

### 2.6 `logs/daily/YYYYMMDD.md`（每日细节，下沉层）

职责：

- 当日执行记录
- 临时发现
- 当日回归结果
- 当日未收口线索

规则：

- **高频写，低频读**
- 默认不作为续开发首读文件
- 不建议再命名为 `planning_YYYYMMDD.md`

### 2.7 `progress.md`（归档层）

职责：

- 阶段里程碑记录
- 已完成阶段摘要
- 发布或阶段收尾归档

规则：

- 逐步弱化“实时推进”职责
- 更偏归档与阶段总结

---

## 3. 默认读取顺序（Token 优化核心）

### 3.1 单侧任务

后端任务默认读取：

1. `jianqing-backend/current_state.md`
2. 若任务仍不清晰，再读 `jianqing-backend/backlog.md`
3. 需要理由时读 `decisions.md`
4. 需要历史发现时读 `findings.md`
5. 需要追溯当日细节时再读 `logs/daily/*.md`

前端任务同理。

### 3.2 跨端任务

仅默认读取：

1. `jianqing-backend/current_state.md`
2. `jianqing-admin-web/current_state.md`

然后按需扩展对应侧 `backlog.md / decisions.md / findings.md`。

### 3.3 禁止事项

- 非跨端任务默认同时加载双端深层 planning
- 续开发时默认一次性读取 `task_plan.md / findings.md / progress.md` 全套
- 用 daily log 替代 `current_state.md`

---

## 4. 维护规则

### 4.1 单一事实源规则

- 当前下一步：`current_state.md`
- 主线阶段：`task_plan.md`
- 任务状态：`backlog.md`
- 决策原因：`decisions.md`
- 耐久发现：`findings.md`
- 每日细节：`logs/daily/*.md`
- 阶段归档：`progress.md`

**同一类信息不要在多个文件中重复维护。**

### 4.2 更新节奏建议

- 每次阶段变化：更新 `current_state.md`
- 每次任务状态变化：更新 `backlog.md`
- 每次关键决策落定：更新 `decisions.md`
- 每次出现长期 gotcha：更新 `findings.md`
- 每日收工：写入当日日志
- 每个阶段完成：归档到 `progress.md`

### 4.3 Weekly Roll-up（推荐）

每周至少做一次轻量收口：

1. 删掉 `current_state.md` 中失效信息
2. 将已完成且已回归项在 `backlog.md` 中标记完成
3. 将当周关键结论沉淀到 `decisions.md / findings.md`
4. 将阶段里程碑补充到 `progress.md`

---

## 5. 与 Memorix 的职责边界

### 5.1 本地文件负责什么

本地文件负责：

- 当前工作推进
- 当前主线规划
- 本仓库内可直接读取的显式上下文

### 5.2 Memorix 负责什么

Memorix 负责：

- 跨 session 持久记忆
- 关键决策与 trade-off
- bug / problem-solution
- gotcha
- milestone 与重要变更
- session latest summary

### 5.3 不推荐做法

- 用 Memorix 替代 `current_state.md`
- 用 Memorix 代替 daily log
- 把 planning 全量重复存入 Memorix

结论：

> `current_state.md` 管“现在做什么”，Memorix 管“以后为什么还能想起来”。

---

## 6. 推荐的 Memorix 使用规则

### 6.1 应存内容

- `decision`：架构/设计决策
- `trade-off`：权衡结论
- `problem-solution`：发现并修复的 bug
- `gotcha`：容易重复踩坑的约束
- `what-changed`：重要配置变化或 milestone
- `session-request`：会话结束总结

### 6.2 不应存内容

- 简单读文件
- `ls` / `pwd` / `git status`
- 一次性临时排查动作
- 本地 planning 内容整段重复备份

### 6.3 topicKey 规则

统一采用以下前缀：

- `backend/...`
- `frontend/...`
- `cross/...`
- `workflow/...`
- `session/latest-summary`

示例：

- `backend/data-scope/custom`
- `frontend/role-page/data-scope`
- `cross/dept-scope-regression-baseline`
- `workflow/context-recovery-strategy`

---

## 7. 迁移建议（从现状平滑过渡）

当前仓库已有：

- `current_state.md`
- `task_plan.md`
- `findings.md`
- `progress.md`

推荐按以下顺序渐进迁移：

### 阶段 1（立即可执行）

1. 保留现有四文件
2. 停止继续把大量执行细节写入 `task_plan.md`
3. 强化 `current_state.md` 为唯一默认入口
4. 新增本规范作为统一约束

### 阶段 2（下一轮规划整理时）

1. 为前后端各新增 `backlog.md`
2. 将活跃任务从 `task_plan.md` 迁到 `backlog.md`
3. 为前后端各新增 `decisions.md`

### 阶段 3（长期收敛）

1. 新增 `logs/daily/`
2. 将日常执行细节改写入 daily log
3. 将 `progress.md` 收敛为阶段归档文档

---

## 8. AI 协作读取协议（推荐直接执行）

### 8.1 续开发协议

当用户说“继续简擎项目”时：

1. 默认先读目标侧 `current_state.md`
2. 输出当前状态 + 下一步最小任务
3. 若摘要不足，再按需读取：`backlog.md / decisions.md / findings.md`
4. 非必要不读 daily log

### 8.2 实施任务时

- 优先读取与当前任务直接相关文件
- 不为“保险起见”做全仓 planning 深扫
- 只有在需要历史决策依据时，才展开 `decisions.md` 或 Memorix

---

## 9. 反模式清单

以下做法会显著增加噪音：

1. 将 planning 按完成度拆成多个主文件
2. 同一任务状态在 `current_state/task_plan/progress/daily` 多处同时更新
3. 每日日志承担主规划职责
4. 将所有历史细节都塞进 `current_state.md`
5. 将 planning 全量复制进 Memorix

---

## 10. 一句话规则

> 用 `current_state.md` 保持当前最小上下文，用 `task_plan.md` 固定主线，用 `backlog.md` 管状态，用 `decisions/findings` 沉淀高价值知识，用 daily log 吞掉细节噪音，用 Memorix 做跨 session 长期记忆。
