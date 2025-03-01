package com.game.other;

import lombok.Data;

@Data
public class PasswordData {
    /*
    修改密码的两种方式
    1. 使用邮箱验证码（在首次注册的时候可以使用）
    2. 使用旧密码
     */
    private String code;
    private String oldPwd;
    private String newPwd;
}
