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
@TableName("orders")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //商品id
    private Long goodId;

    //卖家id
    private Long userId;

    //买家id
    private Long buyerId;

    //商品标题
    private String title;

    //图片描述(一张即可)
    private String images;

    //交易金额
    private Double price;

    //交易状态 0 未付款， 1 已取消， 2 已付款已评价 3 待评价
    private Integer status;

    //交易时间
    private String createTime;

}
