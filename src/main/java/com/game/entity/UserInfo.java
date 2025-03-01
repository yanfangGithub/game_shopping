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
@TableName("userinfo")
public class UserInfo implements Serializable {
    //序列化版本号
    @Serial
    private static final long serialVersionUID = 1L;

    //主键，用户id
    @TableId(value = "id")
    private Long id;
    //邮箱
    private String email;

    //昵称，默认是随机字符
    private String nickName;

    //用户头像
    private String icon = "";

     //个人介绍，不要超过128个字符
    private String introduce;

    //粉丝数量
    private Integer fans;

    //关注的人的数量
    private Integer followee;

    //性别，false：男，true：女
    private Boolean sex;

    //生日
    private String birthday;

    //创建时间
    private String createTime;

    //更新时间，账号数据
    private String updateTime;


}
