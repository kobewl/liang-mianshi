# Token 登录使用说明

## 功能说明

已实现基于 JWT Token 的登录认证系统，无需使用第三方认证框架，完全自己实现。

## 主要组件

1. **JwtUtils.java** - JWT Token 工具类
   - 生成 Token
   - 解析 Token
   - 验证 Token
   - Token 有效期：7天

2. **AuthInterceptor.java** - 认证拦截器
   - 拦截所有需要认证的请求
   - 验证请求头中的 Token
   - 将用户信息存入请求属性

3. **WebConfig.java** - Web 配置
   - 配置拦截器规则
   - 排除不需要认证的路径（登录、注册等）

## API 接口

### 1. 用户登录
```
POST /user/login
Content-Type: application/json

{
  "userAccount": "wangliang",
  "userPassword": "123456"
}

响应：
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userAccount": "wangliang",
    "userName": "王亮",
    "userAvatar": null,
    "userProfile": null,
    "userRole": "user",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 2. 获取当前登录用户
```
GET /user/current
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

响应：
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userAccount": "wangliang",
    "userName": "王亮",
    ...
  }
}
```

### 3. 用户登出
```
POST /user/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

响应：
{
  "code": 200,
  "message": "success",
  "data": true
}
```

## 前端使用指南

### 1. 登录流程
```javascript
// 登录
const response = await fetch('http://localhost:8184/user/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userAccount: 'wangliang',
    userPassword: '123456'
  })
});

const result = await response.json();
if (result.code === 200) {
  // 保存 Token 到 localStorage
  localStorage.setItem('token', result.data.token);
  // 保存用户信息
  localStorage.setItem('userInfo', JSON.stringify(result.data));
}
```

### 2. 请求携带 Token
```javascript
// 所有需要认证的请求都需要在请求头中携带 Token
const token = localStorage.getItem('token');
const response = await fetch('http://localhost:8184/api/xxx', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### 3. 登出流程
```javascript
// 登出
const token = localStorage.getItem('token');
await fetch('http://localhost:8184/user/logout', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// 清除本地存储
localStorage.removeItem('token');
localStorage.removeItem('userInfo');
```

### 4. Token 过期处理
```javascript
// 在请求拦截器中统一处理
const response = await fetch(url, options);
if (response.status === 401) {
  // Token 过期或无效，跳转到登录页
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  window.location.href = '/login';
}
```

## 不需要认证的路径

以下路径无需携带 Token：
- `/user/login` - 登录接口
- `/user` (POST) - 注册接口
- `/doc.html` - API 文档
- `/webjars/**` - 静态资源
- `/swagger-resources/**` - Swagger 资源
- `/v3/api-docs/**` - OpenAPI 文档

## 安全性说明

1. Token 有效期为 7 天，过期后需要重新登录
2. Token 包含用户 ID、账号、角色等信息
3. Token 使用 HS256 算法签名，密钥为固定字符串（生产环境应从配置文件读取）
4. 密码目前为明文存储（生产环境应使用 BCrypt 等算法加密）

## 后续优化建议

1. 密码加密存储（使用 BCrypt）
2. Token 刷新机制（refresh token）
3. Token 黑名单（用于强制登出）
4. Redis 存储 Token（实现单点登录）
5. 密钥从配置文件读取
6. 添加请求频率限制

