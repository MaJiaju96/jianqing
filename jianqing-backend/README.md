# 简擎后端（jianqing-backend）

## 项目定位

`简擎` 是一个极简、可读、开发者友好、AI 协作友好的 Java 管理系统后端内核。

- v0.1：`MySQL First`，优先交付认证、RBAC、审计闭环。
- 可演进：预留 `Elasticsearch`、`Nacos`、`RocketMQ` 适配层，不强依赖中间件即可运行。

## 已实现能力（v0.1）

- 认证鉴权：登录、登出、当前用户信息、JWT 拦截。
- 系统管理：用户/角色/菜单基础查询与分配能力。
- 审计能力：操作日志、登录日志查询。
- 集成预留：搜索网关、动态配置网关、消息总线网关（含本地默认实现）。

## 快速开始

### 1) 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Redis 6+

### 2) 初始化数据库

执行初始化脚本：

```bash
mysql -uroot -p < sql/jianqing-init-v0.1.sql
```

脚本会创建 `jianqing` 库及基础系统表（如 `jq_sys_user`、`jq_sys_role`、`jq_sys_menu`）。

### 3) 配置默认管理员密码

初始化 SQL 中默认管理员密码占位符为 `REPLACE_WITH_BCRYPT_HASH`，需要替换成真实 BCrypt 值。

可用以下工具代码生成：

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "admin123";
        String hash = encoder.encode(raw);
        System.out.println("raw = " + raw);
        System.out.println("bcrypt = " + hash);
        System.out.println("match = " + encoder.matches(raw, hash));
    }
}
```

将输出结果替换到 SQL 对应行：

```sql
('admin', '$2a$10$REPLACE_WITH_BCRYPT_HASH', ...)
```

### 4) 本地启动

可选环境变量（不配置则使用默认值）：

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=jianqing
export DB_USER=root
export DB_PASS=root
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=''
export REDIS_DATABASE=0
export JQ_JWT_SECRET='change-this-secret-at-least-32-bytes-long'
export JQ_JWT_EXPIRE_SECONDS=7200
export JQ_TOKEN_KEY_PREFIX='jq:token:'
export JQ_CACHE_HOT_EXPIRE_SECONDS=300
export JQ_CACHE_DOUBLE_DELETE_DELAY_MILLIS=500
```

启动服务：

```bash
mvn spring-boot:run
```

服务默认地址：`http://127.0.0.1:8080`

## API 文档与示例

- 快速接口清单：见下方“核心接口”。
- 详细调用示例：`docs/API_EXAMPLES.md`。
- 发布前检查清单：`docs/RELEASE_CHECKLIST.md`。
- 版本范围说明：`docs/SCOPE_v0.1_v0.2.md`。
- 首版发布说明：`docs/RELEASE_NOTES_v0.1.md`。
- 发布执行 SOP：`docs/RELEASE_SOP.md`。

### 核心接口

- 认证
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- 系统管理
  - `GET /api/system/users`
  - `GET /api/system/roles`
  - `GET /api/system/menus/tree`
- 审计日志
  - `GET /api/audit/oper-logs?page=1&size=20`
  - `GET /api/audit/login-logs?page=1&size=20`

## 开发路线图

### v0.1（当前）

- 管理内核可用（认证 / RBAC / 审计）。
- 集成层可插拔（ES/Nacos/RocketMQ 预留）。
- 文档与工程化能力持续补齐。

### v0.2（规划）

- 完善数据权限与更细粒度权限模型。
- 增加可选中间件接入示例（ES/Nacos/RocketMQ）。
- 补齐更多自动化质量门禁与发布流程。

## 前端联调

前端目录位于同级路径：`../jianqing-admin-web`。

```bash
cd ../jianqing-admin-web
npm install
npm run dev
```

默认访问：`http://127.0.0.1:5173`，`/api` 已代理到后端 `http://127.0.0.1:8080`。

## 贡献规范

请先阅读：`CONTRIBUTING.md`。

## 质量门禁（CI）

- 工作流文件：`.github/workflows/backend-ci.yml`
- 触发时机：`main/master` 分支的 `push` 与 `pull_request`
- 当前门禁：
  - `mvn -DskipTests compile`
  - `mvn test`
  - `mvn checkstyle:check`
  - `bash scripts/check-service-structure.sh`
  - `bash scripts/check-http-method-constraints.sh`
  - `bash scripts/check-mapper-layering.sh`

本地提交前可安装 pre-commit 守卫：

- `bash scripts/install-git-hooks.sh`

## Redis 缓存与会话说明

- token 过期时间由 `jianqing.jwt.expire-seconds` 配置，登录后会将 token 写入 Redis 并设置相同 TTL。
- 请求鉴权采用“JWT 解析 + Redis token 会话校验”双重检查，过期或已登出 token 会自动失效。
- 热点缓存通过 Redis 承载（用户角色/权限/菜单树、系统用户/角色/菜单列表）。
- DB 与缓存一致性采用延迟双删策略，延迟时间由 `jianqing.cache.delay-double-delete-millis` 配置。

## 目录结构（节选）

```text
jianqing-backend
├── src/main/java/com/jianqing
│   ├── module                # 核心业务模块（auth/system/audit）
│   └── integration           # 可插拔集成网关（search/config/mq）
├── sql
│   └── jianqing-init-v0.1.sql
├── docs
│   └── API_EXAMPLES.md
├── CONTRIBUTING.md
└── ARCHITECTURE.md
```
