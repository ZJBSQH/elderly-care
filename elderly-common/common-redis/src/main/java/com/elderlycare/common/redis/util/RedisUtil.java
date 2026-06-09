package com.elderlycare.common.redis.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 通过构造方法注入 RedisTemplate */
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 设置带有过期时间的缓存 */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 设置永久缓存（不过期） */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /** 获取缓存值 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /** 删除缓存 */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /** 判断 key 是否存在 */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /** 设置 key 的过期时间 */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 分布式锁：仅当 key 不存在时设置值（SETNX）
     *
     * @return true 表示设置成功（获取锁），false 表示 key 已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 递增值（计数器）
     *
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递增值（计数器，默认 +1）
     *
     * @return 递增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
