package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Vote;
import com.game.other.Result;

public interface IVoteService extends IService<Vote> {
    Result goodVote(Long id, int num);

    Result count(Long id);

    boolean removeVoteByGoodId(Long goodId);

    boolean isVote(Long goodId);
}
