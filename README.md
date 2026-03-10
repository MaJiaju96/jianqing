# 简擎工作区（jianqing）

该目录用于统一管理 `简擎` 前后端项目，便于在一个窗口内进行跨前后端联动开发。

## 目录说明

```text
jianqing
├── jianqing-backend
└── jianqing-admin-web
```

## 使用建议

- 跨前后端联动开发：在本目录启动 `opencode`。
- 只做后端：进入 `jianqing-backend` 启动。
- 只做前端：进入 `jianqing-admin-web` 启动。

## 上下文恢复

两个子项目都已维护独立规划文件：

- `task_plan.md`
- `findings.md`
- `progress.md`

新开窗口时，先让 AI 读取当前目录下这三份文件，即可在上次基础上继续开发。

## AI Prompt 模版

已提供可复用 Prompt 模版（含日常开发、接口改造、发布前检查）：

- `PROMPT_TEMPLATES.md`

## 统一开发规范

工作区统一执行规范见：

- `DEVELOPMENT_CONVENTIONS.md`

前后端分离规范入口：

- 后端：`jianqing-backend/docs/BACKEND_CONVENTIONS.md`
- 前端：`jianqing-admin-web/docs/FRONTEND_CONVENTIONS.md`

## 一句话继续开发（口令）

你可以直接说：`我们现在继续上次未完成的简擎项目开发`

工作区内已配置 `AGENTS.md`，AI 会默认执行续开发协议：

- 自动读取前后端 `task_plan.md / findings.md / progress.md`
- 自动总结当前阶段与下一步
- 自动按未完成项继续推进
