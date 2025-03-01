package com.game.utils;

import cn.hutool.core.util.RandomUtil;
import com.game.other.EmailData;
import com.game.other.EmailProperties;
import com.game.other.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
public class Tools {

    private static final EmailProperties emailProperties = new EmailProperties(
            "2489566936@qq.com",
            "jcampqkbpaumebcj",
            "smtp.qq.com",
            true
    );

    private final StringRedisTemplate stringRedisTemplate;

    public Tools(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public  Result sendCode(EmailData emailData) {
        // 1.校验邮箱
        if (RegexUtils.isEmailInvalid(emailData.getEmail())) {
            log.error(emailData.getEmail());
            // 2.如果不符合，返回错误信息
            return Result.fail("邮箱格式错误！");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4.发送验证码
        log.info("发送短信验证码成功，验证码：{}", code);
        // 5.发送到邮箱
        boolean b = MailUtil.sendMail(emailProperties, emailData.getEmail(),
                emailData.getTitle(), emailData.getDescription() + code);
        if (b) {
            // 保存验证码到 redis
            stringRedisTemplate.opsForValue().set(emailData.getPrefix(), code, emailData.getOutTime(), emailData.getUnit());
            return Result.ok(code);
        }
        return Result.fail("验证码发送失败");
    }
}
