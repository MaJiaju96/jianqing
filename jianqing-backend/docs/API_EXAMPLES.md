# API Examples

以下示例默认后端地址为：`http://127.0.0.1:8080`。

## 1. 登录获取 Token

```bash
curl -X POST 'http://127.0.0.1:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

示例响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "<jwt-token>",
    "tokenType": "Bearer"
  }
}
```

## 2. 获取当前用户信息

```bash
curl -X GET 'http://127.0.0.1:8080/api/auth/me' \
  -H 'Authorization: Bearer <accessToken>'
```

## 3. 登出

```bash
curl -X POST 'http://127.0.0.1:8080/api/auth/logout' \
  -H 'Authorization: Bearer <accessToken>'
```

## 4. 查询用户列表

```bash
curl -X GET 'http://127.0.0.1:8080/api/system/users' \
  -H 'Authorization: Bearer <accessToken>'
```

## 5. 查询角色列表

```bash
curl -X GET 'http://127.0.0.1:8080/api/system/roles' \
  -H 'Authorization: Bearer <accessToken>'
```

## 6. 查询菜单树

```bash
curl -X GET 'http://127.0.0.1:8080/api/system/menus/tree' \
  -H 'Authorization: Bearer <accessToken>'
```

## 7. 查询操作日志

```bash
curl -X GET 'http://127.0.0.1:8080/api/audit/oper-logs?page=1&size=20' \
  -H 'Authorization: Bearer <accessToken>'
```

## 8. 查询登录日志

```bash
curl -X GET 'http://127.0.0.1:8080/api/audit/login-logs?page=1&size=20' \
  -H 'Authorization: Bearer <accessToken>'
```

## 说明

- 统一通过 `Authorization: Bearer <accessToken>` 传递登录态。
- 具体响应字段以实际后端 DTO 输出为准。
