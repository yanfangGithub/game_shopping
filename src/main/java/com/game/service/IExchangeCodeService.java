package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.ExchangeCode;
import com.game.other.Result;

public interface IExchangeCodeService extends IService<ExchangeCode> {

    Result createCode(Long goodId, String resource);


    Result useRedeemCode(String code);

    Result getRedeemCode(Long goodId);
}
