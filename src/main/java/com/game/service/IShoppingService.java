package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.ShoppingCar;
import com.game.other.Result;

import java.util.List;

/**
 * @author yanfang
 * @version 1.0
 * &#064;date  2024/5/16 12:46
 */

public interface IShoppingService extends IService<ShoppingCar> {
    Result addGoodToCar(Long goodId);

    Result getCarList(Integer current, Integer pageSize);

    Result settleGood(Long carId);

    Result delById(Long carId);

    Result delByIds(List<Long> list);
}
