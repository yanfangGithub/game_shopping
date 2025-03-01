package com.game.other;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yanfang
 * &#064;date  2024/6/18 19:23
 * @version 1.0
 */

@Data
@AllArgsConstructor
public class CommentDTO {


    //评价用户昵称
    private String name;

    //评价用户的头像
    private String avatar;

    //等级
    private Integer level;

    //评价
    private String comment;

    //评价时间
    private String createTime;
}
