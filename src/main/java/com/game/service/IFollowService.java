package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Follow;
import com.game.other.Result;

public interface IFollowService extends IService<Follow> {
    Result followUser(Long userId, Boolean isFollow);

    Result getAllFollower(Long id);

    Result getAllFans(Long id);

    Integer getFollowerCount(Long id);

    Integer getFansCount(Long id);
}
