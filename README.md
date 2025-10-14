# Liang Piao 项目

这是一个基于Spring Boot 3.2.x和Vue 3的全栈应用项目。

## 项目结构

```
liang-piao/
├── piao_backend/          # 后端项目 (Spring Boot 3.2.x + JDK 21)
│   ├── src/main/java/com/liangpiao/backend/
│   │   ├── controller/    # 控制器层
│   │   ├── service/       # 服务层
│   │   ├── repository/    # 数据访问层
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   └── config/        # 配置类
│   └── src/main/resources/
│       └── application.yml # 配置文件
├── piao_frontend/         # 前端项目 (Vue 3 + TypeScript)
│   ├── src/
│   │   ├── components/    # Vue组件
│   │   ├── views/         # 页面视图
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia状态管理
│   │   └── api/           # API接口调用
│   └── package.json       # 前端依赖配置
└── README.md
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.x
- **JDK**: 21
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建工具**: Maven

### 前端
- **框架**: Vue 3
- **语言**: TypeScript
- **路由**: Vue Router
- **状态管理**: Pinia
- **UI组件**: 默认安装，可根据需要选择
- **构建工具**: Vite

## 快速开始

### 后端启动
```bash
cd piao_backend
mvn clean install
mvn spring-boot:run
```

### 前端启动
```bash
cd piao_frontend
npm install
npm run dev
```

## 默认端口
- 后端服务: http://localhost:8080
- 前端服务: http://localhost:3000

## API接口
- 健康检查: GET http://localhost:8080/api/health
- 测试接口: GET http://localhost:8080/api/hello

## 功能特性
- ✅ Spring Boot 3.2.x + JDK 21
- ✅ Vue 3 + TypeScript
- ✅ 跨域配置
- ✅ 健康检查接口
- ✅ 基础项目结构
- 🔄 待实现: 用户认证、数据库集成、业务逻辑等

## 开发建议
1. 后端代码遵循分层架构：Controller → Service → Repository
2. 前端代码按功能模块组织
3. 使用环境变量管理配置
4. 添加适当的日志记录
5. 编写单元测试和集成测试