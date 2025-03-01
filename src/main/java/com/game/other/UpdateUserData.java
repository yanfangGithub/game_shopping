package com.game.other;

import lombok.Data;

@Data
public class UpdateUserData {

    //用户id
    private Long id;
    //个人介绍，不要超过128个字符
    private String introduce;
    //性别，false：男，true：女
    private Boolean sex;
    //生日
    private String birthday;
    //昵称，默认是随机字符
    private String nickName;
    //用户头像
    private String icon = "";
}
