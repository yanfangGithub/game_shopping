package com.game.controller;

import com.game.other.Result;
import com.game.other.UpdateUserData;
import com.game.service.IUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/userInfo")
public class UserInfoController {
    @Resource
    private IUserInfoService userInfoService;

    /**
     * 更新个人信息
     * @param userInfo UpdateUserData对象，必须填充全部数据
     *     //用户id
     *     private Long id;
     *     //个人介绍，不要超过128个字符
     *     private String introduce;
     *     //性别，true：男，false：女
     *     private Boolean sex;
     *     //生日
     *     private LocalDate birthday;
     *     //昵称，默认是随机字符
     *     private String nickName;
     *     //用户头像
     *     private String icon = "";
     * @return ture or false
     */
    @PostMapping("/updateMe")
    public Result updateMe(@RequestBody UpdateUserData userInfo){
        return userInfoService.updateMe(userInfo);
    }

    @PostMapping("/avatar")
    public Result updateAvatar(@RequestParam String url) {
        return userInfoService.avatar(url);
    }

    /**
     * 获取userDTO对象
     * @param userId 用户id
     * @return userDTO
     */
    @GetMapping("/userDTO")
    public Result getUserDTO(@RequestParam("userId") Long userId){
        return userInfoService.getUserDTO(userId);
    }

    /**
     * 获取用户信息
     * @param userId 根据id获取
     * @return userInfo对象
     */
    @GetMapping("/getUserInfo")
    public Result getUserInfo(@RequestParam(required = false) Long userId) {
        return userInfoService.getUSerInfo(userId);
    }


}
