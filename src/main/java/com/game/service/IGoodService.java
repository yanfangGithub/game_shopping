package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Good;
import com.game.other.Result;

public interface IGoodService extends IService<Good> {
    Result createGood(Good good);

    Result removeGood(Long id);

    Result addGood(Long id);

    Result updateGood(Good good);

    boolean checkPrice(Long id, Double price);

    Result getGoodInfo(Long id);

    Result getGoodPage(Integer current, Integer pageSize, String key, String value);

    Result getGoodByMo(Integer current, Integer pageSize, String value);

    Result getGoodByStatus(Integer current, Integer pageSize, Integer status);

    Result getAllGood(Integer current, Integer pageSize);
}
