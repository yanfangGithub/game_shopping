package com.game.controller;

import com.game.other.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

import static com.game.utils.RedisConstants.GOOD_INFO_KEY;

/**
 * @author yanfang
 * &#064;date  2024/6/20 23:02
 * @version 1.0
 */

@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping("/getGoodInfo")
    public Result get() {
        Map<Object, Object> good = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + "189316737138688");
        return Result.ok(good);
    }

    @PostMapping("/createOrder")
    public Result create() {
        return Result.ok("购买成功");
    }
}
