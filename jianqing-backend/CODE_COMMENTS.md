# 简擎后端注释规范

本文档用于约束 `jianqing-backend` 的代码注释风格，提升可读性与 AI 协作效率。

## 1. 总原则

- 注释服务于可读性，避免无意义的注释噪音。
- 关键逻辑必须写注释，普通逻辑不强制要求。
- 注释解释"为什么这样做"，而非"代码在做什么"。

## 2. 必须写注释的场景

### 2.1 关键分支
```java
// 登录失败需要区分「用户不存在」和「密码错误」，防止账号枚举攻击
// if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//     throw new IllegalArgumentException("用户名或密码错误");
// }
```

### 2.2 复杂流程
```java
// 递归构建菜单树：先按 parentId 分组，再按 sort 排序，最后挂载到父节点
private List<MenuTreeNode> buildMenuTree(List<SysMenu> menus) {
    // ...
}
```

### 2.3 边界处理
```java
// 根节点 parentId 为 0 或 null，前端树组件需要明确区分这两种情况
if (parentId == null || parentId == 0L) {
    return menus.stream()
            .filter(m -> m.getParentId() == null || m.getParentId().equals(0L))
            // ...
}
```

### 2.4 业务约束与规则
```java
// 密码加密采用 BCrypt，每次加密结果不同，登录时使用 matches 对比
// 缓存采用延迟双删策略：先删缓存 -> 写 DB -> 延迟 500ms 再删缓存
```

### 2.5 多方协作点
```java
// 此方法被 OperationLogInterceptor 调用，需保证不抛异常影响主业务
// Redis 连接失败时降级为内存会话，仅影响「服务端主动登出」功能
```

### 2.6 配置与开关
```java
// integration.search.enabled=false 时，SearchGateway 降级为 NoopSearchGateway
// 避免外部中间件未接入时项目无法启动
```

## 3. 类注释规范

每个业务 Service、Controller、Mapper 接口必须添加类注释：

```java
/**
 * 认证服务：登录、登出、获取用户信息
 * 登录成功后写入 Redis 会话，token 失效时同步清除
 */
public class AuthServiceImpl {

}
```

## 4. 方法注释规范

公开方法（public）必须添加方法注释，说明：
- 方法职责
- 输入参数含义
- 返回值含义
- 可能的异常

```java
/**
 * 用户登录
 * @param request 登录请求（username/password）
 * @param httpRequest 用于获取客户端 IP
 * @return 登录响应（token/Bearer/过期时间）
 * @throws IllegalArgumentException 用户名或密码错误
 */
public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    // ...
}
```

私有方法建议添加简短注释，说明方法作用即可：

```java
// 提取客户端真实 IP，支持 X-Forwarded-For 代理
private String clientIp(HttpServletRequest request) {
    // ...
}
```

## 5. 常量与配置注释

```java
public class JwtProperties {
    /**
     * JWT 密钥，需从环境变量或配置中心注入，禁止硬编码
     * 生产环境务必修改默认值
     */
    @Value("${jianqing.jwt.secret:default-secret-change-in-prod}")
    private String secret;

    /** Token 过期时间（秒），默认 2 小时 */
    @Value("${jianqing.jwt.expire-seconds:7200}")
    private int expireSeconds;
}
```

## 6. 注释禁区

- ❌ 不要写「这是用户服务」这类复述性注释
- ❌ 不要在方法内部为每行代码都加注释
- ❌ 不要用注释代码块代替删除
- ❌ 不要在注释中暴露生产密钥或敏感信息

## 7. AI 协作约定

- AI 修改代码时，优先复用现有代码风格
- AI 新增业务逻辑必须遵守本文注释规则
- 涉及接口变更、公共组件修改时，同步更新：
  - `task_plan.md`
  - `findings.md`
  - `progress.md`
  - 本规范文档

## 8. 示例：从无注释到有注释

### 改造前
```java
public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    SysUser user = baseMapper.selectOne(...);
    if (user == null || user.getStatus() != 1) {
        throw new IllegalArgumentException("用户名或密码错误");
    }
    String token = jwtTokenService.generateToken(user.getUsername());
    tokenSessionService.saveToken(token, user.getUsername());
    return new LoginResponse(token, "Bearer", jwtProperties.getExpireSeconds());
}
```

### 改造后
```java
/**
 * 用户登录
 * @param request 登录请求（username/password）
 * @param httpRequest 用于获取客户端 IP
 * @return 登录响应（token/Bearer/过期时间）
 * @throws IllegalArgumentException 账号不存在、禁用或密码错误
 */
public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    // 查询用户：username 唯一 + 未删除
    SysUser user = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, request.getUsername())
            .eq(SysUser::getIsDeleted, 0)
            .last("limit 1"));

    // 校验用户状态：存在 + 启用(1) + 密码匹配
    // 统一返回「用户名或密码错误」，防止账号枚举
    if (user == null || user.getStatus() == null || user.getStatus() != 1
            || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        saveLoginLog(0L, request.getUsername(), httpRequest, 0, "用户名或密码错误");
        throw new IllegalArgumentException("用户名或密码错误");
    }

    // 更新最后登录信息
    user.setLastLoginIp(clientIp(httpRequest));
    user.setLastLoginTime(LocalDateTime.now());
    baseMapper.updateById(user);

    // 生成 JWT 并写入 Redis 会话（支持服务端主动失效）
    String token = jwtTokenService.generateToken(user.getUsername());
    tokenSessionService.saveToken(token, user.getUsername());
    saveLoginLog(user.getId(), user.getUsername(), httpRequest, 1, "登录成功");

    return new LoginResponse(token, "Bearer", jwtProperties.getExpireSeconds());
}
```
