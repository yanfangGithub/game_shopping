package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Good;
import com.game.entity.Order;
import com.game.mapper.OrderMapper;
import com.game.other.Result;
import com.game.service.IGoodService;
import com.game.service.IOrderService;
import com.game.service.IUserService;
import com.game.utils.CreateID;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.game.utils.RedisConstants.*;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements IOrderService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private IGoodService goodService;
    @Resource
    private IUserService userService;


    //执行Redis lua脚本的工具类
    private static final DefaultRedisScript<Long> luaScript;

    static {
        luaScript = new DefaultRedisScript<>();
        luaScript.setLocation(new ClassPathResource("buy.lua"));
        luaScript.setResultType(Long.class);
    }


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        //进行初始化操作
        executorService.submit(new OrderHandler());
    }
    //通过redis中Stream实现消息队列，实现消费者组来
    private class OrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    //log.error("进入到消息队列run");
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            //确定消费者组名称 和消费者的名称
                            Consumer.from(CONSUMER_GROUPS_NAME, "n1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(CONSUMER_QUEUE_NAME, ReadOffset.lastConsumed())
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有消息，继续下一次循环
                        continue;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    Order order = BeanUtil.fillBeanWithMap(value, new Order(), true);
                    // 3.创建订单

                    log.info("创建订单！");
                    createRedisOrder(order);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge(CONSUMER_QUEUE_NAME, CONSUMER_GROUPS_NAME, record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常run方法", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    //log.error("进入到消息队列pendingList");
                    //读取最新的消息即可
                    // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from(CONSUMER_GROUPS_NAME, "n1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(CONSUMER_QUEUE_NAME, ReadOffset.from("0"))
                    );
                    // 2.判断订单信息是否为空
                    if (list == null || list.isEmpty()) {
                        // 如果为null，说明没有异常消息，结束循环
                        break;
                    }
                    // 解析数据
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    Order order = BeanUtil.fillBeanWithMap(value, new Order(), true);
                    // 3.创建订单
                    log.info("创建订单！");
                    createRedisOrder(order);
                    // 4.确认消息 XACK
                    stringRedisTemplate.opsForStream().acknowledge(CONSUMER_QUEUE_NAME, CONSUMER_GROUPS_NAME, record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常handlePendingList方法", e);
                }
            }
        }
    }

    private void  createRedisOrder(Order order) {
        //有三个参数 商品id goodId 订单id orderId 买家id buyerId
        //买家id
        Long buyerId = order.getBuyerId();
        Long goodId = order.getGoodId();
        // 创建锁对象
        RLock redisLock = redissonClient.getLock(LOCK_GOOD_KEY + buyerId);
        // 尝试获取锁
        boolean isLock = redisLock.tryLock();
        // 判断
        if (!isLock) {
            // 获取锁失败，直接返回失败或者重试
            log.error("获取互斥锁失败！");
            return;
        }
        try {
            // 查询订单
            int count = query().eq("buyer_id", buyerId)
                    .eq("good_id", goodId)
                    .notIn("status", 1)
                    .count();
            // 判断是否存在
            if (count > 0) {
                log.error("不允许重复下单！");
                return;
            }

            // 扣减库存
            boolean success = goodService.update()
                    .setSql("stock = stock - 1")
                    .eq("id", goodId).gt("stock", 0)
                    .update();
            if (!success) {
                // 扣减失败
                log.error("库存不足！");
                return;
            }
            // 7.创建订单
            //补充其他的参数
            Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + goodId);
            Good good = BeanUtil.mapToBean(map, Good.class, false,
                    CopyOptions.create()
                            .setIgnoreNullValue(false)
                            .setFieldValueEditor((f, v) -> (v == null ? null : v.toString()))
            );
            order.setUserId(good.getUserId());//卖家id
            order.setStatus(0);//未付款
            order.setCreateTime(TimeFormatUtil.getTime());//创建时间
            order.setPrice(good.getPrice());//成交价格
            order.setImages(good.getImages().split(",")[0]);//图片
            order.setTitle(good.getTitle());//标题

            boolean b = save(order);
            if (!b) {
                throw new RuntimeException("订单创建失败！");
            }
        } finally {
            // 释放锁
            redisLock.unlock();
        }
    }

    @Override
    public Result createOrder(Long goodId) {

        Long userId = CurrentUser.getUser().getId();
        long orderId = new CreateID(stringRedisTemplate).nextId(ORDER_ID_KEY);
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                luaScript,
                Collections.emptyList(),
                goodId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = 0;
        if (result != null) {
            r = result.intValue();
        }
        // 2.判断结果是否为0
        if (r != 0) {
            // 2.1.不为0 ，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 3.返回订单id，订单完成，待付款

        return Result.ok();
    }

    /**
     * 创建订单并直接支付
     * @param goodId 商品id
     * @param isPay bool类型 是否直接付款
     * @return true fo false
     */
    @Override
    public synchronized Result createOrder(Long goodId, boolean isPay) {
        //1. 查看商品是否存在
        Boolean b = stringRedisTemplate.hasKey(GOOD_INFO_KEY + goodId);
        if (Boolean.FALSE.equals(b)) {
            return Result.fail("该商品不存在！");
        }
        if (!isPay) {
            return Result.fail("需要传入true！");
        }

        //2. 获取当前用户信息
        Long userId = CurrentUser.getUser().getId();
        //3. 生成订单
        Order order = new Order();
        long id = new CreateID(stringRedisTemplate).nextId(ORDER_ID_KEY);
        //补充其他的参数(获取商品参数)
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + goodId);
        Good good = BeanUtil.mapToBean(map, Good.class, false,
                CopyOptions.create()
                        .setIgnoreNullValue(false)
                        .setFieldValueEditor((f, v) -> (v == null ? null : v.toString()))
        );
        // 查看用户余额是否充足
        Double price = good.getPrice();
        Double balance = userService.query().select("balance").eq("id", userId).one().getBalance();
        if (balance < price) {
            return Result.fail("余额不足");
        }
        order.setId(id);
        order.setGoodId(goodId);
        order.setBuyerId(userId);
        order.setUserId(good.getUserId());//卖家id
        order.setStatus(0);//未付款
        order.setCreateTime(TimeFormatUtil.getTime());
        order.setPrice(price);
        order.setImages(good.getImages().split(",")[0]);//图片
        order.setTitle(good.getTitle());//标题

        boolean b1 = save(order);
        if (!b1) {
            return Result.fail("订单信息保存失败！");
        }

        //4.存储值上redis中
        //过期时间
        stringRedisTemplate.opsForValue().set(ORDER_PAY_TIME + id, userId.toString(), Duration.ofMinutes(5));
        //购买的用户id
        stringRedisTemplate.opsForSet().add(GOOD_ORDER_KEY + goodId, userId.toString());
        //商品库存
        stringRedisTemplate.opsForValue().increment(GOOD_STOCK_KEY + goodId, -1);

        return Result.ok(id);
    }

    @Override
    public Result payOrder(Long orderId) {
        //1. 获取订单信息
        Long userId = CurrentUser.getUser().getId();
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在！");
        }
        Long goodId = order.getGoodId();
        Double price = order.getPrice();
        Long buyerId = order.getUserId();
        //2. 查看订单是否超时限
        //查看支付时间的键是否存在
        Boolean member = stringRedisTemplate.hasKey(ORDER_PAY_TIME + orderId);
        if (Boolean.FALSE.equals(member)){
            //订单超时限未支付，取消
            Result result = cancelOrder(orderId);
            return Result.fail(result.getSuccess() ? "订单已取消" : result.getErrorMsg());
        }

        //3. 进行支付操作
        //3.1 查看余额
        Double balance = userService.getById(userId).getBalance();
        if (price > balance) {
            return Result.fail("余额不足，请充值！");
        }
        //4. 更新用户余额
        //4.1 买家余额
        boolean b = userService.update().setSql("balance = balance - " + price).eq("id", userId).update();
        if (!b){
            return Result.fail("用户余额更新失败！");
        }
        //4.2 卖家余额
        boolean b1 = userService.update().setSql("balance = balance + " + price).eq("id", buyerId).update();
        if (!b1){
            return Result.fail("用户余额更新失败！");
        }
        //4.3 订单状态
        boolean b2 = update().set("status", 3).eq("id", orderId).update();
        if (!b2) {
            return Result.fail("订单状态修改失败！");
        }
        //4.4 判断是否售罄
        Long stock = goodService.query().eq("id", goodId).one().getStock();
        if (stock == 0) {
            goodService.update().eq("id", goodId).setSql("status = 1").update();
            stringRedisTemplate.opsForHash().put(GOOD_INFO_KEY + goodId, "status", "1");
        }
        //5. 更新goodInfo
        stringRedisTemplate.opsForHash().increment(GOOD_INFO_KEY + goodId, "stock", -1);
        return Result.ok();
    }

    @Override
    public Result cancelOrder(Long orderId) {
        //1. 获取订单相关信息
        Long userId = CurrentUser.getUser().getId();
        Order order = query().eq("id", orderId).one();
        if (order == null){
            return Result.fail("订单不存在！");
        }
        Long goodId = order.getGoodId();
        //2. 获取互斥锁
        RLock lock = redissonClient.getLock(LOCK_CANCEL_ORDER + orderId);
        if (!lock.tryLock()) {
            return Result.fail("获取互斥锁失败！");
        }
        try {
            //3. 处理数据
            //3.1 删除临时数据
            //删除set集合中的用户id
            stringRedisTemplate.opsForSet().remove(GOOD_ORDER_KEY + goodId, userId.toString());
            //删除支付时间的锁
            stringRedisTemplate.delete(ORDER_PAY_TIME + orderId);
            //恢复库存
            stringRedisTemplate.opsForValue().increment(GOOD_STOCK_KEY + goodId, 1);
            //3.2 处理数据库
            boolean b1 = update().set("status", 1).eq("id", orderId).update();
            boolean b = goodService.update().setSql("stock = stock + 1").eq("id", goodId).update();
            if (!b1 || !b){
                return Result.fail("数据库更新异常！");
            }
        }catch (Exception e){
            log.error(e + "取消订单失败！");
        }finally {
            lock.unlock();
        }
        return Result.ok();
    }

    @Override
    public Result getOrder(Long orderId) {
        return Result.ok(getById(orderId));
    }

    @Override
    public Result getOrderByStatus(Integer status, Integer current, Integer pageSize) {
        //1. 获取当前用户的id
        Long userId = CurrentUser.getUser().getId();
        //2. 查询器
        QueryWrapper<Order> query = new QueryWrapper<Order>()
                .select("id", "user_id", "good_id", "price", "title", "create_time", "images")
                .eq("buyer_id", userId)
                .eq("status", status)
                .orderByDesc("create_time");
        //3. 分页对象
        Page<Order> page = new Page<>(current, pageSize);
        //4. 查询订单信息
        return Result.ok(baseMapper.selectPage(page, query));
    }
}
