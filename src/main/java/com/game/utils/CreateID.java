package com.game.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.game.utils.RedisConstants.USER_ID_PREFIX;

@Slf4j
public class CreateID {


    private final StringRedisTemplate stringRedisTemplate;

    public CreateID(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //保证时间戳不会太大
    private static final long time = 1715840914995L;

    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        long timestamp = System.currentTimeMillis() - time;

        // 2.生成序列号
        // 2.1.获取当前日期，精确到天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 2.2.自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 3.拼接并返回
        if (count != null) {
            return timestamp << 16;
        }
        return 0;
    }

    //生成用户id
    public long nextUserId() {
        //指定从10000000开始
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + "user");
        //拼接
        if (count == null) {
            return 0;
        }
        return USER_ID_PREFIX + count;
    }
}
