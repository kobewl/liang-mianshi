package com.mianshi.backend.constants;


/**
 * redis 常量
 */
public interface RedissonConstants {

    /**
     * 用户签到记录的 redis key 前缀
     */
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "mianshi:backend:";

    /**
     * 获取用户签到记录的 redis key
     *
     * @param year 年份
     * @param userId 用户ID
     * @return redis key
     */
    static String getUserSignInRedisKey(int year, long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }
}
