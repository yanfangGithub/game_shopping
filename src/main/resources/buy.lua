-- 1.参数列表
-- 1.1.商品id
local goodId = ARGV[1]
-- 1.2.用户id
local buyerId = ARGV[2]
-- 1.3.订单id
local orderId = ARGV[3]

-- 2.数据key
-- 2.1.库存key
local stockKey = 'good:stock:' .. goodId
-- 2.2.订单key
local orderKey = 'good:order:' .. goodId
-- 2.3.支付时间key
local payKey = 'order:time:' .. orderId

-- 3.脚本业务
-- 3.1.判断库存是否充足 get stockKey
if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.2.库存不足，返回1
    return 1
end
-- 3.2.判断用户是否下单 SISMEMBER orderKey userId
if(redis.call('sismember', orderKey, buyerId) == 1) then
    -- 3.3.存在，说明是重复下单，返回2
    return 2
end
-- 3.4.扣库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)
-- 3.5.下单（保存用户）sadd orderKey userId,
redis.call('sadd', orderKey, buyerId)
--并设置过期时间300s（五分钟）,单独存储
redis.call('SETEX', payKey, 300, buyerId)
-- 3.6.发送消息到队列中， XADD stream.orders * k1 v1 k2 v2 ...
redis.call('xadd', 'stream.orders', '*', 'buyerId', buyerId, 'goodId', goodId, 'id', orderId)
return 0