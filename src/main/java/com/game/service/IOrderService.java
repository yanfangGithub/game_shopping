package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Order;
import com.game.other.Result;

public interface IOrderService extends IService<Order> {
    Result createOrder(Long goodId);

    Result createOrder(Long goodId, boolean isPay);

    Result payOrder(Long orderId);

    Result cancelOrder(Long orderId);

    Result getOrder(Long orderId);

    Result getOrderByStatus(Integer status, Integer current, Integer pageSize);
}
