package com.game.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author yanfang
 * @version 1.0
 * &#064;description:  购物车
 * &#064;date  2024/5/16 12:40
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("shopping_car")
public class ShoppingCar implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //商品id
    private Long goodId;

    //用户id
    private Long userId;
    //商品价格
    private Double price;
    //商品标题
    private String title;
    //商品图片
    private String images;

    //创建时间
    private String createTime;
}
