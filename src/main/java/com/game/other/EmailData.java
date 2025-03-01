package com.game.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailData {
    //通过邮箱发送验证码
    private String email;
    //内容标题
    private String title;
    //发送内容
    private String description;
    //redis前缀
    private String prefix;
    //过期时间
    private Long outTime;
    //时间单位
    private TimeUnit unit;
}
