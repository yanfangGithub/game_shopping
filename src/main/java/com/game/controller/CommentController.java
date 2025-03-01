package com.game.controller;

import com.game.entity.Comments;
import com.game.other.Result;
import com.game.service.ICommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yanfang
 * @version 1.0
 * &#064;date  2024/5/14 22:24
 */

@Slf4j
@RestController
@RequestMapping("comment")
public class CommentController {
    @Resource
    private ICommentService commentService;

    /**
     * 进行评价
     * @param comments 只需要三个字段即可 goodId level comment
     * @return 该商品的评价数量
     */
    @PostMapping("/create")
    public Result createComment(@RequestBody Comments comments){
        return commentService.createComment(comments);
    }

    @GetMapping("/getComment{goodId}")
    public Result getCommentList(@PathVariable Long goodId) {
        return commentService.getCommentByGoodId(goodId);
    }
}
