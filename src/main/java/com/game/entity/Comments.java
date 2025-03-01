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
@TableName("good_comment")
public class Comments implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //商品订单号
    private Long goodId;

    //评价用户id
    private Long userId;

    //等级
    private Integer level;

    //评价
    private String comment;

    //评价时间
    private String createTime;
}
