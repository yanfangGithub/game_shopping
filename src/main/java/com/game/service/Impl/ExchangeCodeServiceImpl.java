package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.ExchangeCode;
import com.game.mapper.ExchangeCodeMapper;
import com.game.other.Result;
import com.game.service.IExchangeCodeService;
import com.game.utils.CreateID;
import com.game.utils.CurrentUser;
import com.game.utils.Encoder;
import com.game.utils.RedeemCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.game.utils.RedisConstants.*;
import static com.game.utils.SystemConstants.REDEEM_CODE_LEN;

@Slf4j
@Service
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode>
        implements IExchangeCodeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result createCode(Long goodId, String resource) {
        //1. 查询id是否存在
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(GOOD_INFO_KEY + goodId))) {
            return Result.fail("该商品不存在！");
        }

        //2.生成code
        Long id = new CreateID(stringRedisTemplate).nextId(CODE_ID_KEY);
        String code = RedeemCodeGenerator.getRedeemCode(REDEEM_CODE_LEN);

        //3. 对resource进行加密处理(使用兑换码作为密钥)
        try {
            resource  = Encoder.encryptAES(resource, code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //4. 保存数据即可
        ExchangeCode redeemCode = new ExchangeCode().setId(id).setCode(code).setGoodId(goodId).setResource(resource);
        if (!save(redeemCode)) {
            return Result.fail("生成失败！");
        }
        Map<String, Object> codeMap = BeanUtil.beanToMap(redeemCode, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreProperties("id")
                        .setFieldValueEditor((f, v) -> v.toString())
        );
        stringRedisTemplate.opsForHash().putAll(CODE_INFO_KEY + id, codeMap);
        return Result.ok();
    }

    @Override
    public Result useRedeemCode(String code) {
        //1. 获取操作用户的相关信息
        Long userId = CurrentUser.getUser().getId();
        //2. 获取产品资源resource
        ExchangeCode one = query()
                .select("resource", "good_id")
                .eq("code", code)
                .one();
        //3. 判断该用户是否购买了该产品
        Boolean member = stringRedisTemplate.opsForSet().isMember(GOOD_ORDER_KEY + one.getGoodId(), userId.toString());
        if (Boolean.FALSE.equals(member)) {
            return Result.fail("你没有购买该商品！");
        }
        //4. 进行解码
        String resource = one.getResource();
        try {
            resource = Encoder.decryptAES(resource, code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Result.ok(resource);
    }

    @Override
    public Result getRedeemCode(Long goodId) {
        //1. 获取当前用户信息
        Long userId = CurrentUser.getUser().getId();

        //2. 判断该用户是否购买了该产品
        Boolean member = stringRedisTemplate.opsForSet().isMember(GOOD_ORDER_KEY + goodId, userId.toString());
        if (Boolean.FALSE.equals(member)) {
            return Result.fail("你没有购买该商品！");
        }

        //3. 获取兑换码
        String code = query()
                .select("code")
                .eq("good_id", goodId)
                .one()
                .getCode();
        return Result.ok(code);
    }
}
