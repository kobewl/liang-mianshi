package com.mianshi.backend.utils;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 */
@Component
public class PasswordUtils {

    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 验证密码
     * @param password 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean checkPassword(String password, String encryptedPassword) {
        return BCrypt.checkpw(password, encryptedPassword);
    }
}