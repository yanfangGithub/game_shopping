package com.game.controller;

import com.game.other.Result;
import com.game.service.IExchangeCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/code")
public class ExchangeCodeController {
    @Resource
    private IExchangeCodeService exchangeCodeService;

    /**
     * 生成兑换码
     * @param goodId 商品id
     * @param resource 资源
     * @return true or false
     */
    @PostMapping("/create")
    public Result createCode(@RequestParam("goodId") Long goodId,
                             @RequestParam("resource") String resource){
        return exchangeCodeService.createCode(goodId, resource);
    }

    /**
     * 使用兑换码获取资源
     * @param code 兑换码
     * @return 资源
     */
    @GetMapping("/useRedeemCode{code}")
    public Result useRedeemCode(@PathVariable String code) {
        // @RequestParam("goodId") Long goodId
        return exchangeCodeService.useRedeemCode(code);
    }

    /**
     * 获取兑换码
     * @param goodId 使用商品id
     * @return redeemCode
     */
    @GetMapping("/getRedeemCode{goodId}")
    public Result getRedeemCode(@PathVariable("goodId") Long goodId){
        return exchangeCodeService.getRedeemCode(goodId);
    }

}
