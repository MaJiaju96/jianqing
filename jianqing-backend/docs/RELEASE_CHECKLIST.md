# 简擎后端发布前检查清单（v0.1）

## 1. 代码与规范

- [x] 后端接口方法仅使用 `GET` / `POST`
- [x] 关键分支与边界处理具备必要注释
- [x] `Mapper.xml` 与 `Mapper` 接口同目录维护
- [x] 无明显无关重构或破坏性改动

## 2. 质量门禁

- [x] `mvn -DskipTests compile`
- [x] `mvn test`
- [x] `mvn checkstyle:check`
- [x] GitHub Actions 已配置并可自动执行门禁

## 3. 运行依赖与配置

- [x] MySQL 初始化脚本可用：`sql/jianqing-init-v0.1.sql`
- [x] Redis 配置项已在 `application.yml` 提供（含 token TTL 与缓存参数）
- [x] 本地启动环境变量说明完整

## 4. 功能闭环验收（v0.1）

- [x] 认证鉴权：登录 / 登出 / `me`
- [x] RBAC：用户、角色、菜单、分配能力
- [x] 审计：登录日志、操作日志、分页查询
- [x] 审计筛选：关键字/状态/登录方式

## 5. 文档与开源准备

- [x] `README.md`（定位、快速开始、路线图、CI）
- [x] `CONTRIBUTING.md`（分支策略、提交规范）
- [x] `docs/API_EXAMPLES.md`（核心接口示例）
- [x] 发布说明：`docs/RELEASE_NOTES_v0.1.md`
- [x] 版本范围与路线：`docs/SCOPE_v0.1_v0.2.md`

## 6. 发布动作建议

- [x] 发布执行 SOP 已沉淀：`docs/RELEASE_SOP.md`
- [ ] 创建 `v0.1.0` tag
- [ ] 生成 GitHub Release 并附带发布说明
- [ ] 在 Release 备注中声明已知限制与 v0.2 方向

> 注：若当前目录尚未初始化 git，请先完成仓库初始化与远端绑定后再执行以上动作。
