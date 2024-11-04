package com.example.utils.redis;

import cn.hutool.extra.spring.SpringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {

    private static final RedissonClient CLIENT = SpringUtil.getBean(RedissonClient.class);

    /**
     * 限流
     * @param key           限流key
     * @param rateType      限流类型 RateType.OVERALL所有实例共享、RateType.CLIENT单实例端共享
     * @param rate          速率
     * @param rateInterval  速率间隔
     * @return -1 表示失败
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        // 声明一个限流器
        RRateLimiter rateLimiter = CLIENT.getRateLimiter(key);
        // 设置速率，5秒中产生3个令牌
        rateLimiter.trySetRate(rateType, rate, rateInterval, RateIntervalUnit.SECONDS);

        log.info("剩余令牌：{}", rateLimiter.availablePermits());

        if(rateLimiter.tryAcquire()) {
            // 返回可用的许可数
            return rateLimiter.availablePermits();
        } else {
            return -1L;
        }
    }


    public static <T> T getCacheObject(String key) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        return bucket.get();
    }

    /**
     * 缓存基本的对象，保留TTL有效期
     * @param key
     * @param value
     * @param isSaveTtl
     * @Since Redis 6.X以上使用SetAndKeepTTL 兼容5.X 方案
     */
    public static <T> void setCacheObject(final String key, final T value, final boolean isSaveTtl) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        if(isSaveTtl) {
            try {
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                long timeToLive = bucket.remainTimeToLive();
                setCacheObject(key, value, Duration.ofMillis(timeToLive));
            }
        } else {
            bucket.set(value);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     * @param key
     * @param value
     * @param duration
     */
    public static <T> void setCacheObject(final String key, final T value, final Duration duration) {
        RBatch batch = CLIENT.createBatch();
        RBucketAsync<T> bucket = batch.getBucket(key);
        bucket.setAsync(value);
        bucket.expireAsync(duration);
        RBucketAsync<T> bucket2 = batch.getBucket(key+"1");
        bucket2.setAsync(value);
        bucket2.expireAsync(duration);
        batch.execute();
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     * @param key
     * @param value
     */
    public static void setCacheObject(final String key, final String value) {
        setCacheObject(key, value, false);
    }

    /**
     * 删除单个对象
     * @param key
     * @return
     */
    public static boolean deleteObject(final String key) {
        return CLIENT.getBucket(key).delete();
    }

    /**
     * 删除集合对象
     * @param collection
     * @return
     */
    public static void deleteObject(final Collection collection) {
        RBatch batch = CLIENT.createBatch();
        collection.forEach( t -> {
            batch.getBucket(t.toString()).deleteAsync();
        });
        batch.execute();
    }

    public static <T> long getTimeToLive(final String key) {
        RBucket<T> rBucket = CLIENT.getBucket(key);
        return rBucket.remainTimeToLive();
    }

    /**
     * 设置有效期
     * @param key
     * @param duration 超时时间
     * @return 成功/失败
     */
    public static boolean expire(final String key, final Duration duration) {
        RBucket<Object> rBucket = CLIENT.getBucket(key);
        return rBucket.expire(duration);
    }

    public static <T> void setCacheObject(final String key, final T value) {
        setCacheObject(key, value, false);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public static Collection<String> keys(final String pattern) {
        Stream<String> stream = CLIENT.getKeys().getKeysStreamByPattern(pattern);
        return stream.collect(Collectors.toList());
    }

    public static RedissonClient getClient() {
        return CLIENT;
    }
}