# 简擎前端管理台（jianqing-admin-web）

`jianqing-admin-web` 是 `简擎` 的前端管理台项目，基于 `Vue 3 + Vite + Element Plus`，用于对接 `jianqing-backend` 的认证、系统管理与审计能力。

## 项目概要

- 技术栈：`Vue 3`、`Vite`、`Element Plus`、`Vue Router`、`Axios`
- 代码规范：默认使用纯 `JavaScript`（不引入 TypeScript）
- 目录关系：与后端 `jianqing-backend` 保持平级目录

## 目录结构

```text
jianqing-admin-web
├── src
│   ├── api/           # 接口封装
│   ├── layouts/       # 布局
│   ├── router/        # 路由与守卫
│   ├── stores/        # 登录态存储
│   ├── styles/        # 全局样式
│   └── views/         # 页面
├── index.html
├── package.json
└── vite.config.js
```

## 环境要求

- `Node.js >= 18`
- `npm >= 9`

## 快速启动

在前端目录执行：

```bash
cd /Users/majiaju/Person/code/jianqing/jianqing-admin-web
npm install
npm run dev
```

> 若你在跨前后端联调，建议先进入 `/Users/majiaju/Person/code/jianqing` 再按需进入子目录。

默认访问地址：`http://127.0.0.1:5173`

## 构建命令

```bash
npm run build
npm run preview
```

## 后端联调说明

- 前端通过 Vite 代理调用后端接口：`/api -> http://127.0.0.1:8080`
- 代理配置文件：`vite.config.js`
- 后端需先启动：`jianqing-backend`（默认端口 `8080`）

## 已对接接口

- 认证：`/api/auth/login`、`/api/auth/logout`、`/api/auth/me`
- 系统管理：`/api/system/users`、`/api/system/roles`、`/api/system/menus/tree`
- 审计日志：`/api/audit/oper-logs`、`/api/audit/login-logs`

## 常见问题

- 登录后跳回登录页：请检查后端是否启动、JWT 配置是否有效。
- 接口请求失败：优先检查 `vite.config.js` 的代理目标地址与后端端口。
- 页面空白：先执行 `npm install`，再重新 `npm run dev`。

## 维护约定

- 本文件作为前端项目入口文档，后续新增功能或改动启动方式时，请同步补充。
- 如涉及后端接口或联调策略变化，请同步更新 `findings.md` 与 `progress.md`。
