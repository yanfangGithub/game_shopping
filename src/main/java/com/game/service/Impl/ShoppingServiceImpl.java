package com.game.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.ShoppingCar;
import com.game.mapper.ShoppingMapper;
import com.game.other.Result;
import com.game.service.IOrderService;
import com.game.service.IShoppingService;
import com.game.utils.CreateID;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.game.utils.RedisConstants.*;

@Slf4j
@Service
public class ShoppingServiceImpl extends ServiceImpl<ShoppingMapper, ShoppingCar>
        implements IShoppingService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IOrderService orderService;

    @Override
    public Result addGoodToCar(Long goodId) {
        //1. 判断商品是否存在
        Boolean b = stringRedisTemplate.hasKey(GOOD_INFO_KEY + goodId);
        if (Boolean.FALSE.equals(b)) {
            return Result.fail("该商品不存在！");
        }
        //2.获取当前用户id
        Long userId = CurrentUser.getUser().getId();
        //2.2 判断是否已经加入购物车
        Boolean member = stringRedisTemplate.opsForSet().isMember(CAR_ID_INFO + userId, goodId.toString());
        if (Boolean.TRUE.equals(member)) {
            return Result.fail("该商品已经加入过购物车！");
        }

        //3. 加入购物车
        //获取商品信息
        Map<Object, Object> good = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + goodId);
        //3.1 用户自己的商品不允许加入购物车
        if (Objects.equals(userId, Long.valueOf(good.get("userId").toString()))) {
            return Result.fail("不能购买自己的商品");
        }
        ShoppingCar shoppingCar = new ShoppingCar();
        long id = new CreateID(stringRedisTemplate).nextId(CAR_ID_KEY);
        shoppingCar.setId(id);
        shoppingCar.setGoodId(goodId);
        shoppingCar.setUserId(userId);
        //log.error(String.valueOf(good.get("price")));
        Double price = Double.parseDouble(good.get("price").toString());
        shoppingCar.setPrice(price);
        //图片
        String[] img = good.get("images").toString().split(",");
        shoppingCar.setImages(img[0]);
        shoppingCar.setTitle(good.get("title").toString());
        shoppingCar.setCreateTime(TimeFormatUtil.getTime());

        boolean b1 = save(shoppingCar);
        if (!b1) {
            return Result.fail("添加到数据库失败！");
        }

        //4. 增加到redis中 保存购物车id
        stringRedisTemplate.opsForSet().add(CAR_ID_INFO + userId, goodId.toString());
        return Result.ok();
    }

    @Override
    public Result getCarList(Integer current, Integer pageSize) {
        //1. 获取当前用户id
        Long userId = CurrentUser.getUser().getId();

        //2. 获取goodIdList
        Set<String> goodset = stringRedisTemplate.opsForSet().members(CAR_ID_INFO + userId);
        if (goodset == null || goodset.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> goodlist = goodset.stream()
                .map(Long::valueOf)
                .toList();
        //3. 获取数据
        QueryWrapper<ShoppingCar> query = new QueryWrapper<ShoppingCar>()
                .in("good_id", goodlist)
                .orderByDesc("create_time");
        Page<ShoppingCar> page = new Page<>(current, pageSize);
        Page<ShoppingCar> result = baseMapper.selectPage(page, query);

        return Result.ok(result);
    }

    @Override
    public Result settleGood(Long carId) {
        //传过来的参数为购物车id
        //1. 获取当前用户信息
        Long userId = CurrentUser.getUser().getId();
        //2. 判断该商品是否在购物车
        Boolean member = stringRedisTemplate.opsForSet().isMember(CAR_ID_INFO + userId, carId.toString());
        if (Boolean.FALSE.equals(member)) {
            return Result.fail("该商品不在购物车内！");
        }
        //3. 购买逻辑
        Long goodId = query().select("good_id").eq("id", carId).one().getGoodId();
        //3.1 创建订单
        Result order = orderService.createOrder(goodId, true);
        log.error(order.toString());
        if (!order.getSuccess()) {
            return order;
        }
        //3.2 进行支付
        return orderService.payOrder((Long) order.getData());
    }

    @Override
    public Result delById(Long carId) {
        //1. 删除数据库
        Long goodId = query().select("good_id").eq("id", carId).one().getGoodId();
        boolean b = removeById(carId);
        if (!b) {
            return Result.fail("删除失败！");
        }
        //2. 删除缓存
        Long userId = CurrentUser.getUser().getId();
        stringRedisTemplate.opsForSet().remove(CAR_ID_INFO + userId, goodId.toString());
        return Result.ok();
    }

    @Override
    public Result delByIds(List<Long> list) {
        //1. 先查询
        List<Long> goodIdList = baseMapper.selectList(new QueryWrapper<ShoppingCar>()
                        .in("id", list)).stream()
                .map(ShoppingCar::getGoodId)
                .toList();
        //1. 删除数据库
        boolean b = removeByIds(list);
        if (!b) {
            return Result.fail("删除失败！");
        }
        //2. 删除缓存
        Long userId = CurrentUser.getUser().getId();
        Object[] array = goodIdList.stream().map(String::valueOf).toArray();

        stringRedisTemplate.opsForSet().remove(CAR_ID_INFO + userId, array);
        return Result.ok();
    }
}
