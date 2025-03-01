package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.User;
import com.game.other.LoginData;
import com.game.other.PasswordData;
import com.game.other.Result;

public interface IUserService extends IService<User> {

    Result login(LoginData loginData);

    Result sendLoginCode(String email);

    Result sendPwdCode(String email);

    Result changePwd(PasswordData passwordData);

    Result realName(String name, String idNumber);

    Result getRealName();

    Result getBalance();

    Result addBalance(double value);

    Result out();

    Result register(String email, String code);
}
