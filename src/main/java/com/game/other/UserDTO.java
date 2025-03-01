package com.game.other;

import lombok.Data;

@Data
public class UserDTO {
    //存储在redis中的用户基本信息
    private Long id;
    private String nickName;
    private String icon;
}