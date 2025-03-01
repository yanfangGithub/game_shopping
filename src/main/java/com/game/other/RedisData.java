package com.game.other;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RedisData {
    //redis数据
    private LocalDateTime expireTime;
    private Object data;
}