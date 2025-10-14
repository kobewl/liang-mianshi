# 亮票面试刷题系统 API 说明

## 项目简介

本项目是基于 Spring Boot 3.2.12 + MyBatis-Plus 3.5.14 + Knife4j 4.3.0 构建的面试刷题系统后端。

## 技术栈

- **Spring Boot**: 3.2.12
- **MyBatis-Plus**: 3.5.14（集成分页、逻辑删除、自动填充等功能）
- **Knife4j**: 4.3.0（API 文档）
- **MySQL**: 8.0
- **Hutool**: 5.8.23（工具类库）
- **Lombok**: 1.18.30

## 项目结构

```
backend/
├── src/main/java/com/liangpiao/backend/
│   ├── config/              # 配置类
│   │   ├── Knife4jConfig.java        # Knife4j 配置
│   │   ├── MybatisPlusConfig.java    # MyBatis-Plus 配置
│   │   └── CorsConfig.java           # 跨域配置
│   ├── controller/          # 控制器层
│   │   ├── UserController.java
│   │   ├── QuestionController.java
│   │   ├── QuestionBankController.java
│   │   └── QuestionBankQuestionController.java
│   ├── dto/                 # 数据传输对象
│   │   ├── UserDTO.java
│   │   ├── QuestionDTO.java
│   │   ├── QuestionBankDTO.java
│   │   └── QuestionBankQuestionDTO.java
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Question.java
│   │   ├── QuestionBank.java
│   │   └── QuestionBankQuestion.java
│   ├── mapper/              # MyBatis Mapper 接口
│   ├── service/             # 服务层
│   │   └── impl/            # 服务实现类
│   └── vo/                  # 视图对象
│       ├── UserVO.java
│       ├── QuestionVO.java
│       ├── QuestionBankVO.java
│       └── ApiResponse.java
└── src/main/resources/
    ├── application.yml      # 应用配置
    └── sql/
        └── createSql.sql    # 建表 SQL
```

## 核心功能

### 1. MyBatis-Plus 集成

- **分页插件**: 已配置，支持自动分页
- **逻辑删除**: 使用 `@TableLogic` 注解，删除字段为 `isDelete`
- **自动填充**: 自动填充 `createTime`、`updateTime`、`editTime`
- **驼峰命名**: 自动映射数据库下划线命名到 Java 驼峰命名

### 2. Knife4j API 文档

- **访问地址**: http://localhost:8184/doc.html
- **功能**: 提供可视化的 API 文档和在线测试功能
- **语言**: 中文界面

### 3. 实体说明

#### User（用户）
- id: 主键 ID
- userAccount: 账号
- userPassword: 密码
- userName: 用户昵称
- userAvatar: 用户头像
- userProfile: 用户简介
- userRole: 用户角色（默认 user）

#### Question（题目）
- id: 主键 ID
- title: 标题
- content: 内容
- tags: 标签列表（JSON 数组）
- answer: 推荐答案
- userId: 创建用户 ID

#### QuestionBank（题库）
- id: 主键 ID
- title: 标题
- description: 描述
- picture: 图片
- userId: 创建用户 ID

#### QuestionBankQuestion（题库题目关联）
- id: 主键 ID
- questionBankId: 题库 ID
- questionId: 题目 ID
- userId: 创建用户 ID

## API 接口

### 用户管理 (/api/user)
- `POST /api/user` - 创建用户
- `PUT /api/user/{id}` - 更新用户
- `DELETE /api/user/{id}` - 删除用户
- `GET /api/user/{id}` - 获取用户详情
- `GET /api/user/page` - 分页查询用户

### 题库管理 (/api/question-bank)
- `POST /api/question-bank` - 创建题库
- `PUT /api/question-bank/{id}` - 更新题库
- `DELETE /api/question-bank/{id}` - 删除题库
- `GET /api/question-bank/{id}` - 获取题库详情
- `GET /api/question-bank/page` - 分页查询题库

### 题目管理 (/api/question)
- `POST /api/question` - 创建题目
- `PUT /api/question/{id}` - 更新题目
- `DELETE /api/question/{id}` - 删除题目
- `GET /api/question/{id}` - 获取题目详情
- `GET /api/question/page` - 分页查询题目

### 题库题目关联管理 (/api/question-bank-question)
- `POST /api/question-bank-question` - 添加题目到题库
- `DELETE /api/question-bank-question/{id}` - 从题库移除题目
- `GET /api/question-bank-question/by-bank/{bankId}` - 查询题库下的题目

## 启动步骤

1. **创建数据库**
   ```bash
   # 执行 sql/createSql.sql 创建数据库和表
   ```

2. **修改配置**
   ```yaml
   # src/main/resources/application.yml
   # 修改数据库连接信息
   spring:
     datasource:
       url: jdbc:mysql://your-host:3306/liang_mianshi
       username: your-username
       password: your-password
   ```

3. **启动项目**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **访问 API 文档**
   ```
   http://localhost:8184/doc.html
   ```

## MyBatis-Plus 优化说明

1. **简化 CRUD**: 继承 `BaseMapper` 和 `IService`，无需编写基础 CRUD 代码
2. **分页查询**: 使用 `Page` 对象，自动处理分页逻辑
3. **逻辑删除**: 使用 `@TableLogic` 注解，删除操作自动转为更新
4. **自动填充**: 创建时间、更新时间自动填充，无需手动设置
5. **Lambda 查询**: 支持类型安全的 Lambda 表达式查询

## 注意事项

1. 已移除 JPA 依赖，完全使用 MyBatis-Plus
2. 所有时间字段使用 `LocalDateTime` 类型
3. 逻辑删除字段 `isDelete`: 0-未删除，1-已删除
4. API 返回统一使用 `ApiResponse` 封装
5. 所有请求参数使用 DTO，返回数据使用 VO
6. 使用 Hutool 工具类进行 Bean 复制等操作

## 后续扩展建议

1. 添加参数校验（已集成 validation）
2. 添加异常处理（已有 GlobalExceptionHandler）
3. 添加权限控制（JWT 已集成）
4. 添加 Redis 缓存
5. 添加分布式 ID 生成器
6. 添加操作日志记录


