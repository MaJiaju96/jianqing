# OpenCode Plugin Usage Notes

## 1. DCP (`@tarquinen/opencode-dcp`)

### 作用
- 自动裁剪过时上下文
- 减少重复工具输出带来的 token 膨胀
- 长会话更稳

### 当前配置文件
- `~/.config/opencode/dcp.jsonc`

### 日常用法
- 日常直接用，不需要手动触发
- 它会自动做：
  - deduplication
  - supersedeWrites
  - purgeErrors

### 适合什么时候调参数
- 会话很长、上下文很重
- 想减少 chat 中的 pruning 提示
- 想更激进/更保守地压缩

### 当前策略
- `pruneNotification: minimal`
- 自动策略开启
- `nudgeForce: soft`

---

## 2. TokenScope (`@ramtinj95/opencode-tokenscope`)

### 作用
- 分析 token 消耗
- 看 tool / skill / 子代理 / cache 成本
- 帮你判断到底哪里最烧 token

### 当前命令
- `/tokenscope`

### 常见使用时机
- 一段复杂任务做完后跑一次
- 想比较装插件前后 token 变化时
- 想看 skill / task / read / bash 哪个最贵时

### 输出
- 默认会生成 `token-usage-output.txt`

### 当前默认取向
- 保留：context / cache / tool schema / skill 分析
- 关闭：subagent analysis（减少空会话或轻量任务的噪音）

---

## 3. Type Inject (`@nick-vi/opencode-type-inject`)

### 作用
- 给 TypeScript 项目补充类型理解能力
- 增加工具：
  - `lookup_type`
  - `list_types`
  - `type_check`

### 生效前提
- 当前工作目录下必须能找到 `tsconfig.json`

### 你的当前项目注意事项
- `jianqing-admin-web` 当前没有 `tsconfig.json`
- 所以它在简擎主项目目录下不会自动激活
- 我已经在独立 TS 临时工程里验证过它是正常的

### 适合什么时候用
- TS/Vue 项目开始复杂化时
- 需要快速找类型定义、类型引用、类型检查时

---

## 推荐使用顺序

1. 平时先让 DCP 自动工作
2. 任务结束后跑 `/tokenscope`
3. 以后如果前端补齐 `tsconfig.json`，再重点使用 Type Inject
