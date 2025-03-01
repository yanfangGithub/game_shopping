package com.game.service.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Good;
import com.game.entity.Vote;
import com.game.mapper.VoteMapper;
import com.game.other.Result;
import com.game.service.IVoteService;
import com.game.utils.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.game.utils.RedisConstants.GOOD_INFO_KEY;
import static com.game.utils.RedisConstants.VOTE_USER_GOOD;

@Slf4j
@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements IVoteService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private GoodServiceImpl goodService;


    @Override
    public Result goodVote(Long goodId, int num) {
        //1. 查询是否已经投票
        Long userId = CurrentUser.getUser().getId();
        Boolean member = stringRedisTemplate.opsForSet().isMember(VOTE_USER_GOOD + goodId, userId.toString());
        //2 已投票更新数据
        if (Boolean.TRUE.equals(member)){
            //2.1 更新good_vote的值
            //2.2获取以前的记录
            Vote vote = query().eq("good_id", goodId).eq("user_id", userId).one();
            Integer oldNumber = vote.getNumber();

            //2.3更新数据库的值
            boolean b = update(new UpdateWrapper<Vote>().set("number", num)
                    .eq("good_id", goodId).eq("user_id", userId));

            //2.4计算新的usePrice的值
            if (!b){
                return Result.fail("投票失败！");
            }

            //2.5 更新goodInfo的usePrice
            int newNumber = num - oldNumber;
            if (updateUsePrice(goodId, newNumber)){
                return Result.fail("投票失败！");
            }
            return Result.ok(count(goodId).getData().toString());
        }

        //3. 将记录保存到数据库
        Vote vote = new Vote()
                .setGoodId(goodId)
                .setUserId(userId)
                .setNumber(num);
        boolean b = save(vote);
        if (!b){
            return Result.fail("投票失败！");
        }
        //4. 存储到redis中 使用set
        Long count = stringRedisTemplate.opsForSet().add(VOTE_USER_GOOD + goodId, userId.toString());
        //5. 更新更新goodInfo的usePrice
        if (updateUsePrice(goodId, num)){
            return Result.fail("投票失败！");
        }
        return Result.ok(count);
    }

    @Override
    public Result count(Long goodId) {
        //1.获取评价总人数
        Long size = stringRedisTemplate.opsForSet().size(VOTE_USER_GOOD + goodId);

        if (Integer.parseInt(String.valueOf(size)) != 0){
            return Result.ok(size);
        }
        //2. 如果为零，可能缓存丢失，去数据库查询
        Integer count = query()
                .eq("good_id", goodId)
                .eq("user_id", CurrentUser.getUser().getId())
                .count();
        return Result.ok(count);
    }

    @Override
    public boolean removeVoteByGoodId(Long goodId) {
        //1. 获取数据库的vote记录
        List<Vote> list = query().select("id").eq("good_id", goodId).list();
        //使用stream流获取idList集合
        List<Integer> idList = list.stream()
                .map(Vote::getId)
                .toList();
        //2. 删除数据库的数据
        boolean b = removeByIds(idList);
        if (!b){
            return false;
        }
        //3. 删除redis中的数据
        Boolean b1 = stringRedisTemplate.delete(VOTE_USER_GOOD + goodId);
        return Boolean.TRUE.equals(b1);
    }

    @Override
    public boolean isVote(Long goodId) {
        //判断当前登录用户是否投票某商品
        //1. 获取当前用户
        Long userId = CurrentUser.getUser().getId();
        if (userId == null){
            return false;
        }

        //2. 进行查询
        Boolean member = stringRedisTemplate.opsForSet()
                .isMember(VOTE_USER_GOOD + goodId.toString(), userId.toString());
        if (Boolean.TRUE.equals(member)){
            return true;
        }
        //查询数据库
        Integer count = query().eq("user_id", userId).eq("good_id", goodId).count();
        return count == 1;
    }

    //更新good的usePrice字段
    private boolean updateUsePrice(Long goodId, int addNumber) {
        if (addNumber == 0){
            //不会改变状态
            return false;
        }
        //1. 获取good的usePrice的值
        String key = GOOD_INFO_KEY + goodId;
        Object o = stringRedisTemplate.opsForHash().get(key, "usePrice");
        if (o == null){
            return true;
        }
        //2. 将其转化为Int类型
        int usePrice = Integer.parseInt(o.toString());
        String newUsePrice = (usePrice + addNumber) + "";

        //3. 更新goodInfo的usePrice的值
        boolean b = goodService.update(new UpdateWrapper<Good>().set("use_price", newUsePrice).eq("id",goodId));
        if (!b){
            return true;
        }
        //4. 更新redis中goodInfo的usePrice的值

        stringRedisTemplate.opsForHash().put(key, "usePrice", newUsePrice);

        return false;
    }
}
