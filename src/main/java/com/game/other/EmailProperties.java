package com.game.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

//@Component
//@ConfigurationProperties(prefix = "email")
@Data
@AllArgsConstructor
@ToString
public class EmailProperties {

    //发件人邮箱
    public String user;

    //发件人邮箱授权码
    public String code;

    //发件人邮箱对应的服务器域名,如果是163邮箱:smtp.163.com   qq邮箱: smtp.qq.com
    public String host;

    //身份验证开关
    private boolean auth;

}

