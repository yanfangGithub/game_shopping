package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Follow;
import com.game.mapper.FollowMapper;
import com.game.other.Result;
import com.game.other.UserDTO;
import com.game.service.IFollowService;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.game.utils.RedisConstants.FOLLOW_USER_KEY;
import static com.game.utils.RedisConstants.USER_INFORMATION_KEY;

@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
        implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result followUser(Long followUserId, Boolean isFollow) {
        //1. 获取当前用户
        Long currentId = CurrentUser.getUser().getId();
        String key = FOLLOW_USER_KEY + currentId;
        // 1.判断到底是关注还是取关
        if (isFollow) {
            // 2.关注，新增数据
            Follow follow = new Follow();
            follow.setFollowerId(currentId);
            follow.setFollowingId(followUserId);
            follow.setFollowTime(TimeFormatUtil.getTime());

            boolean isSuccess = save(follow);

            if (isSuccess) {
                // 把关注用户的id，放入redis的set集合(key唯一，values唯一且无序)
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.取关，删除
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("follower_id", currentId)
                    .eq("following_id", followUserId));
            if (isSuccess) {
                // 把关注用户的id从Redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result getAllFollower(Long id) {
        //1. 获取到已关注用户的id
        String key = FOLLOW_USER_KEY + id;
        Set<String> set = stringRedisTemplate.opsForSet().members(key);
        if (set == null){
            return Result.ok();
        }

        //2. 查询关注用户的userDTO信息
        //2.1 创建keys集合
        List<String> keys = new ArrayList<>();
        for (String s : set) {
            keys.add(USER_INFORMATION_KEY + s);
        }
        List<String> userlist = stringRedisTemplate.opsForValue().multiGet(keys);
        ArrayList<UserDTO> userDTOS = new ArrayList<>();
        if (userlist != null) {
            for (String user : userlist) {
                UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
                userDTOS.add(userDTO);
            }
        }
        return Result.ok(userDTOS);
    }

    @Override
    public Result getAllFans(Long id) {
        //只能去数据库查询
        List<Follow> followList = query().eq("following_id", id).list();
        return Result.ok(followList);
    }

    @Override
    public Integer getFollowerCount(Long id) {
        String key = FOLLOW_USER_KEY + id;
        Long size =  stringRedisTemplate.opsForSet().size(key);
        return Integer.valueOf(String.valueOf(size));
    }

    @Override
    public Integer getFansCount(Long id) {
        return query().eq("following_id", id).count();
    }
}
