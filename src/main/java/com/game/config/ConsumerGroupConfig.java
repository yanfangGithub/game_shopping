package com.game.config;

import io.lettuce.core.RedisCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.game.utils.RedisConstants.CONSUMER_GROUPS_NAME;
import static com.game.utils.RedisConstants.CONSUMER_QUEUE_NAME;

/**
 * @author yanfang
 * @version 1.0
 * &#064;date  2024/5/16 17:58
 */
@Slf4j
@Configuration
public class ConsumerGroupConfig {
    private final StringRedisTemplate stringRedisTemplate;

    public ConsumerGroupConfig(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Bean
    public void ensureStreamExists() {
        try {
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(CONSUMER_QUEUE_NAME))) {
                String group = stringRedisTemplate.opsForStream()
                        .createGroup(CONSUMER_QUEUE_NAME, ReadOffset.from("0"), CONSUMER_GROUPS_NAME);
                log.info("消费者组创建成功！" + group);
            }
        } catch (RedisCommandExecutionException e) {
            log.error(e.getMessage());
        }
    }

}
