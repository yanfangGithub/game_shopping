package com.game.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("good")
public class Good implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //主键
    @TableId(value = "id")
    private Long id;

    //发布人id
    private Long userId;

    //商品标题
    private String title;

    //商品状态 -1 下架 0 在售 1 已售罄
    private Integer status;

    //商品数量
    private Long stock;

    //商品价格趋向 -1 价格偏低 0 价格正常 1 价格偏高
    private Integer usePrice;

    //商品标签
    private String tags;

    //商品价格
    private Double price;

    //图片描述一张
    private String images;

    //商品文本表述
    private String description;

    //创建时间
    private String createTime;

    //更新时间
    private String updateTime;

    //是否评论
    @TableField(exist = false)
    private Boolean isVote;
    //是否加入购物车
    @TableField(exist = false)
    private Boolean isAddCar;
    //是否评价
    @TableField(exist = false)
    private Boolean isComment;
}
