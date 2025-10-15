package com.mianshi.backend.interceptor;

import com.mianshi.backend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 认证拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取请求路径和方法
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 放行登录和注册接口
        if ("/api/user/login".equals(path) && "POST".equals(method)) {
            return true;
        }
        if ("/api/user".equals(path) && "POST".equals(method)) {
            return true;
        }
        if ("/api/user/create".equals(path) && "POST".equals(method)) {
            return true;
        }

        // 从请求头中获取 Token
        String token = request.getHeader("Authorization");

        // 如果 Token 为空或不是以 Bearer 开头，返回未授权
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\",\"data\":null}");
            return false;
        }

        // 去除 Bearer 前缀
        token = token.substring(7);

        // 验证 Token
        try {
            if (!JwtUtils.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\",\"data\":null}");
                return false;
            }

            // 将用户信息存入请求属性中，方便后续使用
            Long userId = JwtUtils.getUserIdFromToken(token);
            String userAccount = JwtUtils.getUserAccountFromToken(token);
            String userRole = JwtUtils.getUserRoleFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("userAccount", userAccount);
            request.setAttribute("userRole", userRole);

            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token解析失败\",\"data\":null}");
            return false;
        }
    }
}
