package com.game.utils;

public class SystemConstants {

    public static final String USER_NICKNAME_KEY = "user_";
    //对实名信息进行加密的key
    public static final String SECRET_NAME_KEY = "yanfang";
    //对兑换码商品进行加密的key
    public static final String SECRET_CODE_KEY = "code+code";
    //文件存储地址
    public static final String IMAGE_UPLOAD_DIR = "D:/game/src/assets/";

    //跨域时间
    public static final Long MAX_CORS_TIME = 60 * 60 * 24L;


    //填充长度，aes加密的key的长度
    public static final int PADDING_LEN = 24;
    public static final int REDEEM_CODE_LEN = 16;


}
