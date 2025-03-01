package com.game.controller;

import com.game.other.Result;
import com.game.service.IVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/vote")
public class VoteController {

    @Resource
    private IVoteService voteService;

    /**
     * 商品价格投票
     * @param id 商品id
     * @param num -1 0 1
     * @return 已经评价的人数
     */
    @PostMapping("/")
    public Result goodVote(@RequestParam("id") Long id, @RequestParam("num") int num){
        return voteService.goodVote(id, num);
    }

    /**
     * 获取商品的投票数量
     * @param goodId 商品id
     * @return long count
     */
    @GetMapping("/count{goodId}")
    public Result count(@PathVariable Long goodId) {
        return voteService.count(goodId);
    }

    /**
     * 删除商品的vote信息
     * @param goodId 商品id
     * @return true or false
     */
    @PostMapping("/delete{goodId}")
    public Boolean deleteVote(@PathVariable Long goodId) {
        return voteService.removeVoteByGoodId(goodId);
    }


}
