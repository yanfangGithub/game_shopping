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
@TableName("follow")
public class Follow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //关注者id
    private Long followerId;
    //被关注者id
    private Long followingId;
    //关注时间
    private String followTime;
}
