package com.game.utils;

public class RedisConstants {

    //登录验证码以及过期时间
    public static final String LOGIN_CODE_KEY = "user:login:";
    public static final Long LOGIN_CODE_TTL = 30L;

    //token信息
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    //修改密码
    public static final String CHANGE_PWD_KEY = "change:pwd:";
    public static final Long CHANGE_PWD_TTL = 300L;

    //个人信息
    public static final String USER_INFORMATION_KEY = "user:info:";
    public static final Long USER_ID_PREFIX = 10000000L;
    //用户创建的商品的的id集合
    public static final String CREATE_GOOD_USER_KEY = "user:good:";

    //关注用户
    public static final String FOLLOW_USER_KEY = "follows:";

    //设置空数据缓存的过期时间
    public static final Long CACHE_NULL_TTL = 30L;

    //商品互斥锁字段
    public static final String LOCK_GOOD_KEY = "lock:good:";
    public static final String LOCK_CANCEL_ORDER = "lock:cancel:";

    //商品id字段
    public static final String GOOD_ID_KEY = "good";
    public static final String GOOD_INFO_KEY = "good:info:";
    public static final String GOOD_STOCK_KEY = "good:stock:";

    //评论id字段
    public static final String COMMENT_ID_KEY = "comment";
    public static final String COMMENT_INFO_KEY = "good:comment:";

    //存储临时的订单信息
    public static final String GOOD_ORDER_KEY = "good:order:";
    public static final String ORDER_PAY_TIME = "order:time:";


    //投票字段
    public static final String VOTE_USER_GOOD = "vote:goodId:";

    //兑换码字段
    public static final String CODE_ID_KEY = "code";
    public static final String CODE_INFO_KEY = "code:info:";

    //order订单类
    public static final String ORDER_ID_KEY = "order";

    //购物车字段
    public static final String CAR_ID_KEY = "car";
    public static final String CAR_ID_INFO = "user:car:";//CAR_ID_INFO + userId , goodId

    //消息队列
    public static final String CONSUMER_QUEUE_NAME = "stream.orders";
    //消费者组名称
    public static final String CONSUMER_GROUPS_NAME = "group1";
}
