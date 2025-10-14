package com.mianshi.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 密钥 - 生产环境应该从配置文件读取
    private static final String SECRET_KEY = "liang-mianshi-secret-key-for-jwt-token-generation-2024";

    // Token 有效期：7天（单位：毫秒）
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    // 生成密钥
    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * 生成 Token
     *
     * @param userId      用户ID
     * @param userAccount 用户账号
     * @param userRole    用户角色
     * @return Token字符串
     */
    public static String generateToken(Long userId, String userAccount, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userAccount", userAccount);
        claims.put("userRole", userRole);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userAccount)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token Token字符串
     * @return Claims对象
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Token无效或已过期", e);
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从 Token 中获取用户账号
     *
     * @param token Token字符串
     * @return 用户账号
     */
    public static String getUserAccountFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token Token字符串
     * @return 用户角色
     */
    public static String getUserRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("userRole");
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token Token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token Token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
