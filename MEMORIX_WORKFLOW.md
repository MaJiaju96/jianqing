# 简擎 Memorix 落地工作流（v1）

适用范围：`/Users/majiaju/Person/code/jianqing` 工作区。

目标：让 Memorix 真正服务于“长期续开发 + 低 token 消耗”，而不是成为第二套噪音来源。

---

## 1. 先记住一句话

> 本地文档负责“当前推进”，Memorix 负责“跨 session 还能想起来的高价值记忆”。

换句话说：

- `current_state.md`：现在做什么
- `backlog.md`：任务状态是什么
- `decisions.md`：为什么这么做
- Memorix：下次开新会话时，为什么还能快速恢复这些关键信息

---

## 2. 当前项目的推荐配置结论

基于当前 `memorix.yml`：

- `embedding.provider: off`
- `behavior.sessionInject: minimal`
- `behavior.autoCleanup: true`
- `behavior.formationMode: active`
- `git.ingestOnCommit: true`

当前结论：

1. **继续保持 `sessionInject: minimal`**，避免自动灌入过多历史上下文。
2. **继续保持 `embedding.provider: off`**，当前阶段优先稳定、低成本、低复杂度。
3. **继续保持 `ingestOnCommit: true`**，让 git 成为工程事实记忆。
4. `maxDiffSize: 500` 暂可接受；后续若 commit 经常偏大，可再评估上调到 `800`。

---

## 3. Memorix 的最小使用规则

### 3.1 会话开始：先轻量恢复，再按需检索

推荐顺序：

1. 先读目标侧 `current_state.md`
2. 若需要任务状态，读 `backlog.md`
3. 若需要 why，读 `decisions.md`
4. 只有这三步仍不足时，再用 Memorix 检索

原则：

- 不要一上来就把记忆库当默认首读入口
- Memorix 是“按需召回”，不是“默认灌入”

---

### 3.2 会话中：只在形成结论时 store

应该 store 的场景：

- 架构/设计决策落定
- trade-off 有结论
- bug 根因确认并修复
- 出现可复用 gotcha
- 配置变化
- milestone 完成

不应该 store 的场景：

- 只是读了几个文件
- 跑了 `ls` / `git status`
- 一次性排查动作
- planning 文档整段复制进去

判断口诀：

> 这条信息下周、下月、下次开新会话还值得被搜到吗？

如果答案是否，就不要存。

---

### 3.3 会话结束：只存 decision chain summary

结束时不写流水账，而是写：

1. Goal
2. Key Decisions & Reasoning
3. What Changed
4. Current State
5. Next Steps

重点不是“做了哪些文件”，而是：

- 为什么这样做
- 哪些约束已经确定
- 下一次该从哪里接着做

---

### 3.4 完成后要 resolve

一旦任务完成、问题修复、旧信息失效：

- 及时 `memorix_resolve`

否则后面搜索时，旧问题会反复浮上来，造成记忆污染。

---

## 4. 简擎项目的推荐 topicKey 规则

统一前缀：

- `backend/...`
- `frontend/...`
- `cross/...`
- `workflow/...`
- `session/latest-summary`

推荐示例：

- `backend/data-scope/custom`
- `frontend/data-scope/custom`
- `cross/dept-scope-regression-baseline`
- `workflow/context-recovery-strategy`
- `workflow/planning-memory-layering`

规则：

1. 同一主题必须长期复用同一个 topicKey
2. 不要今天一个名字、明天一个名字
3. 跨前后端共同约束放 `cross/...`

---

## 5. 记忆类型映射表

| 场景 | Memorix type | 本地文档主落点 |
|---|---|---|
| 架构/设计决策 | `decision` | `decisions.md` |
| trade-off 结论 | `trade-off` | `decisions.md` |
| bug 根因与修复 | `problem-solution` | `findings.md` |
| gotcha / 隐性约束 | `gotcha` | `findings.md` |
| milestone / 阶段完成 | `what-changed` | `progress.md` |
| 会话总结 | `session-request` | 不要求同步本地 |

说明：

- Memorix 与本地文档不是二选一
- 本地文档是仓库内显式上下文
- Memorix 是跨 session 检索层

---

## 6. 推荐操作模板

### 6.1 decision 模板

适用：设计、架构、边界、实现策略落定。

核心内容应包含：

- 决策内容
- 为什么这样选
- 没选什么以及为什么
- 影响范围
- topicKey

示例结构：

```text
type: decision
title: CUSTOM 数据范围先做最小闭环
topicKey: backend/data-scope/custom

facts:
- 先实现 CUSTOM 数据范围最小闭环
- 暂不扩展复杂授权矩阵
- 优先复用现有部门树与数据范围判定能力

reasoning:
- 当前 ALL/DEPT_AND_CHILD/DEPT/SELF 主线已稳定
- 先做最小闭环更利于控制复杂度和联调成本
- 过早抽象会放大服务编排与前端配置复杂度
```

### 6.2 problem-solution 模板

适用：bug 已找到根因并已修复。

```text
type: problem-solution
title: 树外部门访问校验遗漏已修复
topicKey: backend/data-scope/tree-boundary

problem:
- 某类数据范围校验未覆盖树外访问边界

solution:
- 在 resolver 中补齐树外访问拒绝逻辑
- 增加对应单测与回归验证

reasoning:
- 仅补前端限制无法保证后端数据安全
```

### 6.3 gotcha 模板

适用：容易重复踩坑但不一定是 bug。

```text
type: gotcha
title: 联调后必须恢复 dept_scope_role 基线
topicKey: cross/dept-scope-regression-baseline

facts:
- 真实联调会临时修改 dept_scope_role 数据范围
- 回归完成后必须恢复为默认基线

reasoning:
- 否则后续权限验证会串场，污染真实联调环境
```

### 6.4 session summary 模板

```text
type: session-request
topicKey: session/latest-summary

## Goal
[本轮具体目标]

## Key Decisions & Reasoning
- 选择 A，因为 B；不选 C，因为 D。

## What Changed
- [文件路径] - [变更及原因]

## Current State
- [现在可用什么]
- [还缺什么]

## Next Steps
- [下一步 1]
- [下一步 2]
```

---

## 7. 当前 CUSTOM 任务的演练示例

这里只做“规范演练”，不代表已经开始实现。

### 7.1 本地文档应该怎么落

#### backend/backlog.md

```md
- [BE-DATA-001] CUSTOM 数据范围后端最小闭环
  Status: not_started
  Regression: pending
  Priority: high
  Phase: 53
  Ref: decisions.md#custom-数据范围先走最小闭环
```

#### backend/decisions.md

```md
## custom-数据范围先走最小闭环

- Scope: backend/data-scope/custom

### Decision
CUSTOM / 自定义部门数据范围先做最小闭环，不先扩成复杂授权矩阵。

### Why
- 当前主线已稳定
- 先做最小闭环更便于联调与测试
```

#### frontend/backlog.md

```md
- [FE-DATA-001] 配合后端完成 CUSTOM 数据范围前端配置闭环
  Status: not_started
  Regression: pending
  Priority: high
  Phase: 49
  Ref: decisions.md#custom-数据范围前端先做配置闭环
```

---

### 7.2 Memorix 应该怎么记

#### 后端 decision

- type: `decision`
- topicKey: `backend/data-scope/custom`

#### 前端 decision

- type: `decision`
- topicKey: `frontend/data-scope/custom`

#### 跨端 gotcha

- type: `gotcha`
- topicKey: `cross/dept-scope-regression-baseline`

内容示意：

- 后端先做最小闭环
- 前端只做配置闭环，不先引入复杂交互层
- 真实联调后必须恢复测试基线

---

## 8. 当 MCP 工具暂不可用时怎么办

如果当前客户端会话里暂时无法直接调用 Memorix MCP 工具：

1. 先按本地文档分层维护：`current_state / backlog / decisions / findings / progress`
2. 保持 topicKey 与记忆结构先写在 `decisions.md` / `findings.md`
3. 待工具可用时再补 store / resolve

原则：

> 不因为工具暂不可用，就把高价值 why 丢回 `task_plan.md` 大杂烩里。

---

## 9. 简擎项目的最终建议

当前最优实践：

1. `current_state.md` 做默认入口
2. `backlog.md` 做状态主源
3. `decisions.md` 做 why 主源
4. Memorix 只存高价值跨 session 记忆
5. `sessionInject: minimal` 保持不变
6. 已完成记忆及时 resolve

一句话：

> 先靠本地文档把“当前上下文”控制住，再用 Memorix 把“长期高价值记忆”提纯出来。
