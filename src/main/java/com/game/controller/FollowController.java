package com.game.controller;

import com.game.other.Result;
import com.game.service.IFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private IFollowService followService;

    @PostMapping("/")
    public Result followUser(@RequestParam("userId") Long userId, @RequestParam("isFollow") Boolean isFollow){
        return followService.followUser(userId, isFollow);
    }

    @GetMapping("/getAllFollower{id}")
    public Result getAllFollower(@PathVariable Long id) {
        return followService.getAllFollower(id);
    }

    @GetMapping("/getAllFans{id}")
    public Result getAllFans(@PathVariable Long id) {
        return followService.getAllFans(id);
    }
}
