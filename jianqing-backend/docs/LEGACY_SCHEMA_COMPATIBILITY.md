# 旧库兼容说明

## 背景

简擎后端在后续迭代中新增了参数、字典、生成器、审计等能力。

如果运行环境使用的是较早期初始化过的数据库，但没有及时执行后续 patch SQL，常见现象是：

- 页面打开即报 SQL 异常
- 某些功能入口看不到
- 生成器/字典/参数/审计链路局部不可用

为降低这类“代码已升级、数据库未升级”导致的故障风险，当前后端已经补齐一组**启动期 schema 自修复器**。

## 当前已覆盖的旧库兼容范围

### 1. 参数模块

- `ConfigSchemaInitializer`
- 自动补齐：
  - `jq_sys_config`
  - `jq_sys_config_history`
- 自动修复字段：
  - `config_group`
  - `value_type`
  - `is_builtin`
  - 以及参数历史表相关核心字段

适用问题：

- 参数设置页报 `Unknown column 'config_group'`
- 参数历史、diff、回滚、恢复链路因旧库缺表/缺列失效

### 2. 代码生成器

- `GeneratorSchemaInitializer`
- 自动补齐：
  - `jq_dev_gen_write_record`

- `GeneratorMenuInitializer`
- 自动补齐：
  - `system:generator:list`
  - `system:generator:query`
  - 管理员角色菜单绑定

适用问题：

- 生成器写入记录/按 marker 回滚报表不存在
- 管理台看不到代码生成器入口

### 3. 菜单与权限关系

- `MenuSchemaInitializer`
- 自动补齐：
  - `jq_sys_menu` 核心字段
  - `jq_sys_role_menu`

重点字段包括：

- `route_path`
- `component`
- `perms`
- `icon`
- `sort_no`
- `visible`
- `status`
- `is_deleted`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`

适用问题：

- 菜单树查询异常
- 新菜单 patch 执行后依然不可见
- 角色菜单绑定链路失效

### 4. 字典模块

- `DictSchemaInitializer`
- 自动补齐：
  - `jq_sys_dict_type`
  - `jq_sys_dict_data`

重点字段包括：

- `status`
- `remark`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- `color_type`
- `css_class`
- `sort_no`

适用问题：

- 字典管理页报表/字段不存在
- 前端 `dict-options` 接口失效
- 多个页面的字典消费链路一起退化

### 5. 部门模块

- `DeptSchemaInitializer`
- 自动补齐：
  - `jq_sys_dept`

重点字段包括：

- `leader_user_id`
- `phone`
- `email`
- `sort_no`
- `status`
- `is_deleted`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`

适用问题：

- 部门树查询失败
- 用户/部门关联页面异常
- 组织架构筛选链路失效

### 6. 审计模块

- `AuditSchemaInitializer`
- 自动补齐：
  - `jq_sys_oper_log`
  - `jq_sys_login_log`

兼容性调整：

- `jq_sys_oper_log.request_param`
- `jq_sys_oper_log.response_data`

已统一按 `LONGTEXT` 兼容方案处理，不再依赖更敏感的 JSON 类型初始化。

适用问题：

- 审计表在旧 MySQL/MariaDB 环境初始化失败
- 登录日志/操作日志查询失败

### 7. 用户 / 角色 / 用户角色关联

- `UserRoleSchemaInitializer`
- 自动补齐：
  - `jq_sys_user`
  - `jq_sys_role`
  - `jq_sys_user_role`

用户表重点字段包括：

- `mobile`
- `email`
- `dept_id`
- `status`
- `is_deleted`
- `last_login_ip`
- `last_login_time`
- `remark`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`

角色表重点字段包括：

- `data_scope`
- `status`
- `is_deleted`
- `remark`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`

适用问题：

- 登录后查用户/角色报错
- 用户分配角色失败
- 权限计算链路异常

## 使用方式

不需要额外手工调用修复器。

只要升级到当前版本并**重启后端服务**，启动器会在应用启动时自动检查并补齐上述旧库缺口。

## 推荐操作顺序

1. 先备份数据库
2. 升级后端代码
3. 重启后端服务
4. 观察启动日志是否出现建表/补列
5. 再进入对应功能页面验证

## 排障建议

如果页面仍报旧库相关异常，优先按下面顺序检查：

1. 是否真的重启了后端服务
2. 当前连接的数据库是否就是预期库
3. 启动账号是否具备 `ALTER TABLE` / `CREATE TABLE` 权限
4. 是否存在数据库方言差异（例如更老版本 MySQL / MariaDB）
5. 是否还有未覆盖的旧表结构差异

## 已知边界

- 当前修复器聚焦“最小可用闭环”，优先保证功能不因缺表/缺列直接报错。
- 对于索引、字符集、历史 patch 数据内容差异，不保证全部自动收口。
- 如果环境限制了 DDL 权限，修复器无法生效，需要 DBA 手工执行对应建表/补列 SQL。
- `sql/jianqing-init-v0.1.sql` 当前已去掉 `utf8mb4_0900_ai_ci`，改为兼容性更高的 `utf8mb4_unicode_ci`；但这不等于对所有历史数据库版本都完全无差异兼容。
- `sql/jianqing-init-v0.1.sql` 中的自动时间戳字段已优先改为 `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`，用于降低旧环境对 `DATETIME DEFAULT CURRENT_TIMESTAMP` 的兼容风险；但仍建议在目标数据库版本上先做一次初始化验证。

## 何时仍建议执行 patch SQL

启动期自修复主要解决“能不能跑起来”的问题。

如果要确保：

- 默认菜单/按钮/角色绑定完全一致
- 默认字典 seed 数据完整
- 历史环境结构与最新版本完全对齐

仍建议结合 `sql/patch/` 下的增量脚本做一次正式补齐。
