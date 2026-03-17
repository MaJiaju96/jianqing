# Task Plan: 简擎后端开源路线（v0.1）

## Goal
在保留后台管理系统高效率开发体验的前提下，完成 `简擎` 的可开源后端内核：以 `MySQL` 为首发数据源，预留 `Elasticsearch`、`Nacos`、`RocketMQ` 的可插拔集成能力。

## Current Phase
Phase 53

## Phases

### Phase 1: 需求对齐与差异化定位
- [x] 明确项目目标：个人开源后端管理系统
- [x] 锁定首发约束：数据库优先使用 MySQL
- [x] 识别后续集成目标：ES、Nacos、RocketMQ
- [x] 输出“简擎”特色方向（轻量内核 + 可插拔）
- **Status:** complete

### Phase 2: 架构与边界规划（MVP）
- [x] 建立 planning-with-files 三文件机制
- [x] 定义 v0.1 模块边界（认证、RBAC、审计）
- [x] 细化模块拆分与依赖关系
- [x] 明确统一扩展点（事件、SPI、配置）
- **Status:** complete

### Phase 3: 基础能力落地（MySQL First）
- [x] 完成登录与 JWT 鉴权
- [x] 完成用户/角色/菜单权限模型
- [x] 完成统一响应、异常处理与操作日志
- [x] 提供初始化 SQL 与本地启动说明
- **Status:** complete

### Phase 4: 集成预留层设计
- [x] 设计搜索适配层（ES Connector）
- [x] 设计配置中心适配层（Nacos Config）
- [x] 设计消息总线适配层（RocketMQ Producer/Consumer）
- [x] 保证未接入时核心模块可独立运行
- **Status:** complete

### Phase 5: 开源化与工程化完善
- [x] 完善 README（定位、路线图、快速开始）
- [x] 增加贡献规范与分支策略
- [x] 增加 API 文档与示例调用
- [x] 补齐质量门禁（基础测试/CI）
- [x] 补齐静态检查（Checkstyle）
- **Status:** complete

### Phase 6: 发布准备
- [x] 完成发布前 checklist
- [x] 标注 v0.1 scope 与 v0.2 计划
- [x] 输出首次开源发布说明
- **Status:** complete

### Phase 7: v0.2 数据权限最小闭环
- [x] 明确最小数据权限策略（全部/本部门/本人）
- [x] 将角色数据范围字段接入后端 DTO / 实体 / 服务
- [x] 在用户管理模块落首批数据范围校验
- [x] 补齐部门管理后端最小 CRUD
- [x] 补齐前后端联调与回归测试
- **Status:** complete

### Phase 8: 代码健康治理（去屎山重构）
- [x] 完成后端热点扫描并识别主复杂度集中点（`SystemServiceImpl`）
- [x] 修复 `JwtAuthenticationFilter` 异常吞掉问题，补齐可观测日志
- [x] 抽离数据范围判定职责（`UserDataScopeResolver`）
- [x] 抽离系统缓存失效职责（`SystemCacheEvictor`）
- [x] 继续拆分 `SystemServiceImpl` 用户写操作编排（创建/更新/删除）
- [x] 增补系统服务关键路径单测（缓存失效与数据范围分支）
- [x] 评估并落地前端系统页列表通用 composable（users/roles/menus/depts）
- **Status:** complete

### Phase 9: 架构收口与规范固化
- [x] 清理后端默认敏感配置并补本地示例配置
- [x] 为 system 关键写路径补齐事务边界与提交后缓存失效
- [x] 将 `SystemServiceImpl` 继续收口为“聚合入口 + 轻量执行器”结构
- [x] 补齐 handler/缓存/事务相关单测并完成回归
- [x] 同步更新后端规划文件与统一开发规范，固化后续优化规则
- **Status:** complete

### Phase 10: CRUD 代码生成器 MVP（预览前置）
- [x] 明确生成器 MVP 边界：仅支持单表、先做预览/下载、不直接落盘
- [x] 建立后端 `dev/gen` 模块骨架与元数据 DTO / service / controller
- [x] 提供表列表、字段列表元数据接口（`/api/dev/gen/tables`、`/columns`）
- [x] 实现 preview/download 接口与模板渲染骨架
- [x] 输出首批后端代码模板（entity/mapper/xml/service/controller/dto）
- [x] 输出前端 API / 业务列表页 / 路由片段模板
- [x] 评估并接入前端生成器页最小闭环
- **Status:** complete

### Phase 11: 代码生成器参数治理与稳定性
- [x] 补齐生成参数服务端统一校验与规范化透传（trim + pattern）
- [x] 统一预览/下载/写入链路使用规范化参数，避免路径与命名漂移
- [x] 补充生成器参数规范化相关单测并完成回归
- **Status:** complete

### Phase 12: 代码写入安全增强（冲突保护）
- [x] 写入前检测已存在文件并阻断默认覆盖
- [x] 增加 `overwrite` 显式覆盖开关，避免误写盘
- [x] 补齐冲突保护与覆盖写入测试并完成回归
- **Status:** complete

### Phase 13: 冲突清单查询接口与可视化支撑
- [x] 新增写入前冲突文件清单查询接口
- [x] 复用生成器 preview 产物路径做冲突比对，输出稳定文件列表
- [x] 补齐冲突清单接口与服务测试并完成回归
- **Status:** complete

### Phase 14: 生成标记与按标记快速删除
- [x] 写入项目返回自动生成标记（markerId）并持久化标记文件
- [x] 新增按标记删除接口，支持快速回滚本次生成写入
- [x] 保持生成模板内容不变，仅通过标记元数据实现删除定位
- [x] 补齐控制器与服务测试并完成回归
- **Status:** complete

### Phase 15: 写入记录落库与前端按写入记录删除
- [x] 新增写入记录表与后端查询接口（仅记录已点击“写入项目”的操作）
- [x] 写入成功时自动落库 marker + 配置快照（table/module/business/class/perm）
- [x] 前端快速删除入口优先读取后端写入记录，非写入历史仍保留本地缓存
- [x] 完成前后端回归验证
- **Status:** complete

### Phase 16: 写入记录过滤查询能力
- [x] 写入记录接口增加表名与时间范围过滤参数
- [x] 保持写入记录查询限制与排序一致（limit + 倒序）
- [x] 完成接口过滤回归验证
- **Status:** complete

### Phase 17: 字典功能最小闭环
- [x] 补齐字典类型/字典数据后端实体、Mapper、Service 与 Controller 接口
- [x] 支持字典类型 CRUD、字典数据 CRUD 与按类型查询启用项
- [x] 补齐字典菜单权限初始化与旧库 patch
- [x] 完成控制器/服务测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 18: 字典业务下拉首批接入
- [x] 补齐通用状态/菜单可见性/部门状态默认字典 seed 与旧库 patch
- [x] 以字典项驱动前端用户/角色/菜单/部门页状态下拉与状态文案展示
- [x] 保持后端消费接口不变，仅通过字典 options 接口完成首批业务接入
- [x] 完成前后端构建与测试回归
- **Status:** complete

### Phase 19: 审计页字典消费接入
- [x] 补齐审计执行状态与登录方式默认字典 seed
- [x] 新增旧库 patch，确保历史环境可补齐审计字典项
- [x] 登录日志与操作日志页改为消费字典项展示与筛选
- [x] 完成前后端构建与测试回归
- **Status:** complete

### Phase 20: 字典缓存与失效收口
- [x] 复用现有系统缓存模式为字典类型、字典数据、字典 options 增加缓存
- [x] 在字典类型/字典数据写操作后补齐提交后失效逻辑
- [x] 覆盖类型改名、数据跨类型修改等缓存边界场景
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 21: 参数设置最小闭环
- [x] 补齐系统参数实体、Mapper、Service 与 Controller 接口
- [x] 支持参数列表、新增、编辑、删除，并同步发布到 DynamicConfigGateway
- [x] 补齐参数菜单按钮权限与默认参数 seed/旧库 patch
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 22: 参数分组最小闭环
- [x] 为参数表补齐 `config_group` 字段并同步 seed/patch
- [x] 参数发布改为按参数分组推送到 DynamicConfigGateway
- [x] 前后端参数 CRUD 适配分组字段
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 23: 参数变更历史最小闭环
- [x] 新增参数变更历史表与后端查询接口
- [x] 参数新增/修改/删除自动落历史记录
- [x] 前端参数页补历史查看入口
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 24: 参数版本回滚最小闭环
- [x] 新增按历史记录回滚参数接口
- [x] 回滚后自动重新发布动态配置并记录一条回滚历史
- [x] 前端历史弹窗补“回滚”操作
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 25: 参数 diff 对比最小闭环
- [x] 新增当前参数与指定历史快照的 diff 接口
- [x] 输出参数名称/分组/参数值/值类型/内置状态字段级差异
- [x] 前端历史弹窗补“对比”查看
- [x] 完成测试、Checkstyle 与结构守卫回归
- **Status:** complete

### Phase 26: 参数删除恢复最小闭环
- [x] 新增已删除参数历史查询接口，仅返回当前仍未恢复的删除记录
- [x] 新增按删除历史恢复参数接口，恢复后自动重新发布动态配置并记录恢复历史
- [x] 前端参数页补“恢复已删”入口与恢复操作
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 27: 参数多版本 diff 最小闭环
- [x] 扩展参数 diff 接口，支持“当前 vs 历史”与“历史 vs 历史”两种模式
- [x] 输出对比目标元数据，便于前端识别当前对比模式与版本信息
- [x] 前端历史弹窗补“设为基准”并支持双历史记录对比
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 28: 参数删除恢复前预览最小闭环
- [x] 新增删除历史恢复预览接口，返回待恢复快照详情
- [x] 前端已删记录列表补“预览”入口与恢复前预览弹窗
- [x] 预览弹窗展示关键字段并支持确认恢复
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 29: 参数恢复前差异对比最小闭环
- [x] 恢复预览接口补充“当前是否已有同键参数”与字段级差异信息
- [x] 前端恢复预览弹窗展示同键占用状态与差异表格
- [x] 同键已存在时禁用直接恢复，避免误操作
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 30: 参数值类型字典消费扩面
- [x] 为参数值类型补齐默认字典 seed 与旧库 patch
- [x] 前端参数页值类型筛选/表单改为优先消费字典 options
- [x] 保留前端兜底选项，避免字典未初始化时页面不可用
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 31: 字典颜色类型字典消费扩面
- [x] 为字典颜色类型补齐默认字典 seed 与旧库 patch
- [x] 字典管理页颜色下拉改为优先消费字典 options
- [x] 字典数据列表颜色展示改为字典文案 + 标签类型
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 32: 参数来源字典消费扩面
- [x] 为参数来源补齐默认字典 seed 与旧库 patch
- [x] 参数页来源筛选/表单改为优先消费字典 options
- [x] 来源展示文案与标签类型改为消费字典配置
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 33: 字典管理页状态字典消费扩面
- [x] 字典管理页状态筛选改为优先消费 `sys_common_status`
- [x] 字典类型/字典数据表单状态选项改为优先消费字典 options
- [x] 状态标签文案与颜色改为字典驱动，并保留前端兜底
- [x] 完成测试、Checkstyle 与前端构建回归
- **Status:** complete

### Phase 34: 参数表旧库缺列兼容
- [x] 启动时自动探测 `jq_sys_config` 缺失的参数扩展字段
- [x] 自动补齐 `config_group` / `value_type` / `is_builtin` 与历史表结构
- [x] 为旧库缺少 `jq_sys_config_history` 场景补齐建表能力
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 35: 代码生成器旧库缺表兼容
- [x] 启动时自动探测 `jq_dev_gen_write_record` 是否缺失
- [x] 缺表时自动补齐代码生成写入记录表
- [x] 补充“需要创建 / 已存在无需创建”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 36: 生成器菜单旧库兼容
- [x] 启动时自动探测生成器菜单是否缺失
- [x] 自动补齐 `system:generator:list/query` 菜单与管理员角色绑定
- [x] 补充“需要创建 / 已存在无需创建”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 37: 菜单表核心字段旧库兼容
- [x] 启动时自动探测 `jq_sys_menu` 核心字段是否缺失
- [x] 自动补齐菜单核心字段与 `jq_sys_role_menu` 关联表
- [x] 补充“需要修复 / 已完整无需修复”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 38: 字典表核心结构旧库兼容
- [x] 启动时自动探测 `jq_sys_dict_type/jq_sys_dict_data` 是否缺失
- [x] 自动补齐字典核心字段与默认结构
- [x] 补充“需要修复 / 已完整无需修复”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 39: 部门表核心结构旧库兼容
- [x] 启动时自动探测 `jq_sys_dept` 是否缺失
- [x] 自动补齐部门核心字段与默认结构
- [x] 补充“需要修复 / 已完整无需修复”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 40: 审计表核心结构兼容
- [x] 启动时自动探测 `jq_sys_oper_log/jq_sys_login_log` 是否缺失
- [x] 自动补齐审计核心字段，并将操作日志请求/响应体改为兼容性更高的 LONGTEXT
- [x] 补充“需要修复 / 已完整无需修复”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 41: 用户角色核心结构旧库兼容
- [x] 启动时自动探测 `jq_sys_user/jq_sys_role/jq_sys_user_role` 是否缺失
- [x] 自动补齐用户、角色与用户角色关联核心字段
- [x] 补充“需要修复 / 已完整无需修复”两类测试
- [x] 完成测试与 Checkstyle 回归
- **Status:** complete

### Phase 42: 旧库兼容说明文档收口
- [x] 汇总当前已覆盖的旧库兼容范围与对应启动修复器
- [x] 补充旧库接入方式、推荐操作顺序与排障建议
- [x] README 增加旧库兼容说明入口
- [x] 同步规划与进度记录
- **Status:** complete

### Phase 43: init SQL 方言风险收口
- [x] 移除初始化脚本中的 MySQL 8 专属 collation
- [x] README 与旧库兼容文档补充说明
- [x] 同步规划与进度记录
- **Status:** complete

### Phase 44: init SQL 时间字段兼容收口
- [x] 将初始化脚本中的自动时间戳字段优先切换为 TIMESTAMP 方案
- [x] README 与旧库兼容文档补充时间字段兼容说明
- [x] 完成测试、Checkstyle 与规划同步
- **Status:** complete

### Phase 45: 参数恢复链路 collation 兼容修复
- [x] 完成参数设置真实浏览器回归，覆盖新增/编辑/历史/删除/恢复链路
- [x] 修复已删参数列表查询中的 `config_key` collation 冲突
- [x] 完成前端构建、后端测试与真实浏览器复测
- **Status:** complete

### Phase 46: 参数多历史与生成器回滚链路真实回归
- [x] 完成参数页双历史 diff 与历史回滚真实浏览器回归
- [x] 完成生成器写入记录删除链路真实浏览器回归
- [x] 验证生成文件已按 marker 删除且无残留
- **Status:** complete

### Phase 47: 生成器冲突确认与系统筛选边界真实回归
- [x] 完成生成器冲突清单 / 快捷过滤 / 取消覆盖真实浏览器回归
- [x] 完成字典页与参数页筛选 / 重置 / 每页条数边界真实浏览器回归
- [x] 同步修复生成器冲突弹窗 `el-radio` 兼容 warning 并验证控制台归零
- **Status:** complete

### Phase 48: 数据权限扩展到本部门及以下
- [x] 放开角色数据范围校验，支持 `DEPT_AND_CHILD`
- [x] 为部门服务补齐“本部门及以下部门 ID”查询能力
- [x] 将用户列表/访问/写操作权限校验扩展到部门子树
- [x] 补齐后端单测并完成回归
- **Status:** complete

### Phase 49: 本部门及以下角色配置真实回归
- [x] 在真实浏览器中验证角色页出现“本部门及以下数据”选项
- [x] 验证编辑角色后列表文案更新为“本部门及以下”
- [x] 完成回归后恢复测试角色原始数据范围
- **Status:** complete

### Phase 50: 本部门及以下真实数据范围联调
- [x] 基于真实后端临时创建树外根部门与树外用户
- [x] 临时将 `dept_scope_role` 切到 `DEPT_AND_CHILD` 并验证 `dept_user` 可见子部门用户
- [x] 验证 `dept_user` 不可见树外用户
- [x] 完成后恢复角色配置并清理临时部门/用户
- **Status:** complete

### Phase 51: 数据权限树形边界单测补强
- [x] 为部门子树收集逻辑补齐单测
- [x] 为 `UserDataScopeResolver` 补齐“ALL 优先级 / 树外访问拒绝 / 树外迁移拒绝”单测
- [x] 完成 focused + 全量后端测试回归
- **Status:** complete

### Phase 52: dept_user 用户页真实回归
- [x] 临时将 `dept_scope_role` 切到 `DEPT_AND_CHILD`
- [x] 使用 `dept_user` 登录真实前端用户页并验证子部门用户可见
- [x] 确认控制台 warning/error 为 0
- [x] 恢复角色基线为 `DEPT`
- **Status:** complete

### Phase 53: 上下文恢复降 token 优化
- [x] 新增后端 `current_state.md` 轻量摘要文件
- [x] 调整工作区续开发协议，优先读取轻量摘要而非默认全量 planning
- [x] 保持历史 planning 文件不丢失，仅调整默认读取策略
- **Status:** complete

## Planning Notes

- 本文件只保留后端目标与 phase 主线骨架，不再承载大段执行细节与历史决策列表。
- 当前任务状态优先查看：`backlog.md`
- 当前轻量摘要优先查看：`current_state.md`
- 关键决策与 why 优先查看：`decisions.md`
- 长期发现与 gotcha 优先查看：`findings.md`
- 阶段归档与里程碑优先查看：`progress.md`
