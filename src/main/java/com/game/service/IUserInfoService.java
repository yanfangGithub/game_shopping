package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.UserInfo;
import com.game.other.Result;
import com.game.other.UpdateUserData;

public interface IUserInfoService extends IService<UserInfo> {
    Result updateMe(UpdateUserData userInfo);

    Result getUserDTO(Long userId);

    Result getUSerInfo(Long userId);

    Result avatar(String url);
}
