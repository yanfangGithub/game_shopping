package com.game.controller;

import com.game.other.Result;
import com.game.service.IShoppingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yanfang
 * @version 1.0
 * &#064;date  2024/5/16 12:45
 */

@Slf4j
@RestController
@RequestMapping("/car")
public class ShoppingController {

    @Resource
    private IShoppingService shoppingService;

    /**
     * 添加商品到购物车
     * @param goodId 商品id
     * @return true or false
     */
    @PostMapping("/add{goodId}")
    public Result addGoodToCar(@PathVariable Long goodId) {
        return shoppingService.addGoodToCar(goodId);
    }

    /**
     * 获取当前用户的购物车信息
     * @return ShoppingCarList
     */
    @GetMapping("/getCarList")
    public Result getCarList(@RequestParam("current") Integer current,
                             @RequestParam("pageSize") Integer pageSize) {
        return shoppingService.getCarList(current, pageSize);
    }

    /**
     * 进行支付，清空购物车
     * @param carId 购物车id
     * @return true of false
     */
    @PostMapping("/settle{carId}")
    public Result settleGood(@PathVariable("carId") Long carId) {
        return shoppingService.settleGood(carId);
    }

    /**
     * 删除指定商品
     * @param carId 商品id
     * @return true of false
     */
    @PostMapping("/remove{carId}")
    public Result delById(@PathVariable("carId") Long carId) {
        return shoppingService.delById(carId);
    }

    @PostMapping("/delete")
    public Result delByIds(@RequestBody List<Long> list) {
        return shoppingService.delByIds(list);
    }
}
