package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Comments;
import com.game.other.Result;

public interface ICommentService extends IService<Comments> {
    Result createComment(Comments comments);

    Result getCommentByGoodId(Long goodId);
}
