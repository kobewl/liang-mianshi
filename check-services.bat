@echo off
echo 检查项目服务状态...
echo.

echo 1. 检查后端服务 (端口 8184):
netstat -an | findstr :8184 >nul
if %errorlevel% equ 0 (
    echo ✅ 后端服务运行中 (http://localhost:8184)
) else (
    echo ❌ 后端服务未运行
)

echo.
echo 2. 检查前端服务 (端口 5173):
netstat -an | findstr :5173 >nul
if %errorlevel% equ 0 (
    echo ✅ 前端服务运行中 (http://localhost:5173)
) else (
    echo ❌ 前端服务未运行
)

echo.
echo 3. 检查数据库连接:
echo 请确保MySQL服务已启动且数据库已创建

echo.
echo 项目访问地址:
echo - 前端主页: http://localhost:5173
echo - 后端API文档: http://localhost:8184/doc.html
echo - 登录页面: http://localhost:5173/login
echo - 注册页面: http://localhost:5173/register

echo.
echo 测试账号:
echo 账号: wangliang
echo 密码: 123456

echo.
pause
