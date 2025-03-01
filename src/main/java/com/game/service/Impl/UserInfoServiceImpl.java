package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.User;
import com.game.entity.UserInfo;
import com.game.mapper.UserInfoMapper;
import com.game.other.Result;
import com.game.other.UpdateUserData;
import com.game.other.UserDTO;
import com.game.service.IUserInfoService;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.game.utils.RedisConstants.LOGIN_USER_KEY;
import static com.game.utils.RedisConstants.USER_INFORMATION_KEY;

@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements IUserInfoService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private FollowServiceImpl followService;

    @Override
    public Result updateMe(UpdateUserData data) {
        //1. 获取当前用户
        UserDTO userDTO = CurrentUser.getUser();
        Long userId = userDTO.getId();
        //1.1 判断id是否匹配
        if (!data.getId().equals(userId)) {
            return Result.fail("用户信息错误!");
        }
        //1.2 获取当前用户
        User user = userService.getById(userId);

        //2. 更新数据库中
        //2.1 user对象赋值
        BeanUtil.copyProperties(data, user);
        user.setUpdateTime(TimeFormatUtil.getTime());
        //2.2 获取请求头中的token
        String token = CurrentUser.getToken();
        if (StrUtil.isBlank(token)){
            return Result.fail("未登录，token为空！");
        }
        String key = LOGIN_USER_KEY + token;

        //2.3 更新redis中的token数据
        UserDTO newUserDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userDTOMap = BeanUtil.beanToMap(newUserDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValues) -> (fieldValues.toString()))
        );
        stringRedisTemplate.opsForHash().putAll(key, userDTOMap);

        //3. 将user数据存储到数据库
        boolean b = userService.updateById(user);
        if (!b){
            return Result.fail("更新失败!");
        }

        //4. 更新userInfo的值
        UserInfo userInfo = BeanUtil.copyProperties(user, UserInfo.class);
        BeanUtil.copyProperties(data, userInfo);
        userInfo.setUpdateTime(TimeFormatUtil.getTime());
        userInfo.setFollowee(followService.getFollowerCount(userId));
        userInfo.setFans(followService.getFansCount(userId));

        //log.info(userInfo.toString());

        //4.1 存入redis中
        String userInfoKey = USER_INFORMATION_KEY + userId;
        uploadRedis(userInfoKey, userInfo);
        //4.2 存入数据库
        boolean b1 = updateById(userInfo);
        if (!b1){
            save(userInfo);
        }
        return Result.ok();
    }

    @Override
    public Result getUserDTO(Long userId) {
        //1.获取userDTO对象，
        Result result = getUSerInfo(userId);
        if (!result.getSuccess()){
            return result;
        }
        UserDTO userDTO = BeanUtil.copyProperties(result.getData(), UserDTO.class);
        return Result.ok(userDTO);
    }

    @Override
    public Result getUSerInfo(Long userId) {
        if (userId == null) {
            userId = CurrentUser.getUser().getId();
        }
        //1. 从redis中获取对象
        String key = USER_INFORMATION_KEY + userId;
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        if (!map.isEmpty()){
            UserInfo userInfo = BeanUtil.mapToBean(map, UserInfo.class, false,
                    CopyOptions.create()
                            .setIgnoreNullValue(false)
                            .setFieldValueEditor((fName, fValues) -> fValues != null ? fValues.toString() : null)
            );
            //更新fans和follows的值
            userInfo.setFollowee(followService.getFollowerCount(userId));
            userInfo.setFans(followService.getFansCount(userId));
            //更新redis
            uploadRedis(key, userInfo);
            //log.error(userInfo.toString());
            return Result.ok(userInfo);
        }
        //2. redis中不存在
        //2.1 从数据库中获取
        UserInfo userInfo = getById(userId);
        if (userInfo == null){
            //获取user对象
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("该用户不存在！");
            }
            UserInfo userInfo1 = BeanUtil.copyProperties(user, UserInfo.class);
            return Result.ok(userInfo1);
        }
        //2.2 存储到redis
        userInfo.setFollowee(followService.getFollowerCount(userId));
        userInfo.setFans(followService.getFansCount(userId));
        uploadRedis(key, userInfo);

        return Result.ok(userInfo);
    }

    @Override
    public Result avatar(String url) {
        //获取当前对象
        Long userId = CurrentUser.getUser().getId();
        boolean b = update().eq("id", userId).set("icon", url).update();
        if (!b) {
            return Result.fail("更换失败");
        }
        //更新redis
        stringRedisTemplate.opsForHash().put(USER_INFORMATION_KEY + userId, "icon", url);
        return Result.ok();
    }


    private void uploadRedis(String key, Object o){
        Map<String, Object> Map = BeanUtil.beanToMap(o, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(false)
                        .setFieldValueEditor((fName, fValues) -> fValues != null ? fValues.toString() : null)
        );
        stringRedisTemplate.opsForHash().putAll(key, Map);
    }

}
