package com.game.controller;

import com.game.other.Result;
import com.game.service.IOrderService;
import com.game.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.game.utils.RedisConstants.GOOD_INFO_KEY;
import static com.game.utils.RedisConstants.GOOD_STOCK_KEY;

@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private IOrderService orderService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建订单，五分钟的付款时间
     * @param goodId 商品id
     * @return 订单id
     */
    @PostMapping("/create")
    public Result createOrder(@RequestParam("goodId") Long goodId, @RequestParam("isPay") boolean isPay) {
        //1. 判断商品状态
        String s = stringRedisTemplate.opsForValue().get(GOOD_STOCK_KEY + goodId);
        //2. 获取商品信息
        String userId = (String) stringRedisTemplate.opsForHash().get(GOOD_INFO_KEY + goodId, "userId");
        if (CurrentUser.getUser().getId().toString().equals(userId)) {
            return Result.fail("不能购买自己的商品");
        }
        //直接支付的情况 ，也就是清空购物车
        if (s != null && isPay) {
            Result order = orderService.createOrder(goodId, true);
            //2. 进行支付
            if (!order.getSuccess()){
                return order;
            }
            return orderService.payOrder((Long) order.getData());
        }
        else
            return orderService.createOrder(goodId);
    }

    /**
     * 订单支付
     * @param orderId 订单号
     * @return true or false
     */
    @PostMapping("/pay{orderId}")
    public Result payOrder(@PathVariable Long orderId) {
        return orderService.payOrder(orderId);
    }

    /**
     * 取消订单
     * @param orderId 订单号
     * @return true or false
     */
    @PostMapping("/cancel{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    /**
     * 获取指定订单号的订单信息
     * @param orderId 订单号
     * @return orderInfo
     */
    @GetMapping("/getOrder{orderId}")
    public Result getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    /**
     * 获取当前用户指定状态的orderDto对象
     * @param status 交易状态 0 未付款， 1 已取消， 2 已付款，
     * @return orderDTOList
     */
    @GetMapping("/getOrderByStatus")
    public Result getOrderByStatus(@RequestParam("status") Integer status,
                                   @RequestParam("current") Integer current,
                                   @RequestParam("pageSize") Integer pageSize) {
        return orderService.getOrderByStatus(status, current, pageSize);
    }

}
