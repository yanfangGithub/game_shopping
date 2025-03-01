package com.game.other;

import lombok.Data;

@Data
public class LoginData {
    //只能通过邮箱密码，或者邮箱验证码登录
    private String email;
    private String password;
    private String code;
}
