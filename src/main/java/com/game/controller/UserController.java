package com.game.controller;


import com.game.entity.UserInfo;
import com.game.other.LoginData;
import com.game.other.PasswordData;
import com.game.other.Result;
import com.game.service.IUserService;
import com.game.service.Impl.UserInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user")

public class UserController {
    @Resource
    private IUserService userService;
    @Resource
    private UserInfoServiceImpl userInfoService;

    /**
     * 发送验证码，通过邮箱登陆的
     * @param email 邮箱
     * @return true of false
     */
    @PostMapping("/sendLoginCode{email}")
    public Result sendLoginCode(@PathVariable("email") String email) {
        // 发送短信验证码并保存验证码
        return userService.sendLoginCode(email);
    }

    /**
     * 发送验证码，修改邮箱密码
     * @param email 邮箱
     * @return true or false
     */
    @PostMapping("/sendPwdCode{email}")
    public Result sendPwdCode(@PathVariable("email") String email) {
        // 发送短信验证码并保存验证码
        return userService.sendPwdCode(email);
    }

    /**
     * 登录账号，可以通过邮箱密码，或者邮箱验证码登录
     * @param loginData code tanch
     * @return true or false
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginData loginData){
        return userService.login(loginData);
    }

    @PostMapping("/register")
    public Result register(@RequestParam("email") String email,
                           @RequestParam("code") String code) {
        //注册用户
        Result register = userService.register(email, code);
        if (!register.getSuccess()) {
            return register;
        }
        //创建userInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setId((Long) register.getData());
        userInfo.setEmail(email);
        userInfoService.save(userInfo);
        return Result.ok();
    }

    /**
     * 修改密码，通过旧密码新密码，或者验证码新密码进行登录
     * @param passwordData code oldPwd newPwd
     * @return true or false
     */
    @PostMapping("/changePwd")
    public Result changePWD(@RequestBody PasswordData passwordData){
        return userService.changePwd(passwordData);
    }

    /**
     * 进行实名认证
     * @param name 名字
     * @param idNumber 身份证件号
     * @return true or false
     */
    @PostMapping("/realName")
    public Result realName(@RequestParam("name") String name,
                           @RequestParam("idNumber") String idNumber){
        return userService.realName(name, idNumber);
    }

    /**
     * 获取实名认证信息
     * @return RealNameData{name， idNUmber}
     */
    @GetMapping("/getRealName")
    public Result getRealName(){
        return userService.getRealName();
    }

    /**
     * 余额获取
     * @return double balance
     */
    @GetMapping("/getBalance")
    public Result getBalance(){
        return userService.getBalance();
    }

    /**
     * 充值
     * @param value 数量
     * @return true or false
     */
    @PostMapping("/addBalance{value}")
    public Result addBalance(@PathVariable double value) {
        return userService.addBalance(value);
    }

    /**
     * 退出登录，清空token
     *
     * @return true or false
     */
    @PostMapping("/out")
    public Result out() {
        return userService.out();
    }
}
