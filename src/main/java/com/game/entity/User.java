package com.game.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //邮箱
    private String email;

    //密码，加密存储
    private String password;

    //账户余额
    private Double balance;

    //昵称，默认是随机字符
    private String nickName;

    //用户头像
    private String icon = "";

    //创建时间
    private String createTime;

    //更新时间
    private String updateTime;

    //实名信息
    private String name;

    //身份证号码
    private String idNumber;


}

