package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.User;
import com.game.mapper.UserMapper;
import com.game.other.*;
import com.game.service.IUserService;
import com.game.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.game.utils.RedisConstants.*;
import static com.game.utils.SystemConstants.SECRET_NAME_KEY;
import static com.game.utils.SystemConstants.USER_NICKNAME_KEY;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录
     *
     * @param loginData 登录对象体
     * @return ture or false
     */
    @Override
    public Result login(LoginData loginData) {
        //1. 取出邮箱
        String email = loginData.getEmail();
        if (StrUtil.isBlank(email)) {
            return Result.fail("邮箱不能为空");
        }
        //2. 判断是由邮箱还是密码登录
        String code = loginData.getCode();
        String password = loginData.getPassword();
        User user = query().eq("email", email).one();

        //3.
        if (user == null) {
            return Result.fail("该用户不存在");
        }
        //3.1 用户存在 判断密码或者验证码合理
        //验证码为空，使用密码登录
        if (StrUtil.isBlank(code)) {
            // 获取数据库中的密码
            String encoderPassword = user.getPassword();
            // 使用解密算法进行比较
            if (!Encoder.matches(encoderPassword, password)) {
                return Result.fail("密码错误，请重新输入");
            }
        } else {
            //3.2 使用验证码登录
            // 取出redis中的code
            String redisCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + email);
            // 进行比较
            if (redisCode == null || !redisCode.equals(code)){
                return Result.fail("验证码错误！");
            }
        }


        //3.3 用户存在 且通过了验证， (退出登录 或者10个小时后 都会自动删除旧的token)
        //最终会创建新的token 以及userDTO
        // 5.保存用户信息到 redis中
        // 5.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);

        // 5.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        // 5.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 5.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 6.返回token
        return Result.ok(token);
    }

    @Override
    public Result sendLoginCode(String email) {
        String title = "登录提醒";
        String description = "这是你的登录邮箱验证码，请妥善保存：\n";

        EmailData emailData = new EmailData(email, title, description,
                LOGIN_CODE_KEY + email, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        return new Tools(stringRedisTemplate).sendCode(emailData);
    }

    @Override
    public Result sendPwdCode(String email) {
        String title = "修改密码提醒";
        String description = "这是你的验证码，请妥善保存：\n";
        EmailData emailData = new EmailData(email, title, description,
                CHANGE_PWD_KEY + email, CHANGE_PWD_TTL, TimeUnit.SECONDS);
        return new Tools(stringRedisTemplate).sendCode(emailData);
    }

    @Override
    public Result changePwd(PasswordData passwordData) {
        //1.1 获取实体内的信息
        String newPwd = passwordData.getNewPwd();
        String oldPwd = passwordData.getOldPwd();
        String code = passwordData.getCode();

        //1.2 获取当前用户信息
        UserDTO userDTO = CurrentUser.getUser();
        Long id = userDTO.getId();
        User user = query().eq("id", id).one();

        //2. 判断哪种修改密码的方式
        if (StrUtil.isBlank(code)){
            //有新旧密码修改
            //2.1 获取数据库的密码
            String DBPassword = user.getPassword();
            //2.2 进行比较
            if (!Encoder.matches(DBPassword, oldPwd)) {
                //原密码不正确
                return Result.fail("原密码错误,请重新输入！");
            }
            //2.3 修改密码
            user.setPassword(Encoder.encode(newPwd));
            boolean result = updateById(user);
            return Result.ok(result);
        }
        //3. 使用验证码修改密码
        //3.1 获取邮箱信息
        String email = user.getEmail();
        String cacheCode = stringRedisTemplate.opsForValue().get(CHANGE_PWD_KEY + email);
        if (StrUtil.isBlank(cacheCode) || !code.equals(cacheCode)){
            return Result.fail("验证码错误！");
        }
        //3.2 修改密码
        user.setPassword(Encoder.encode(newPwd));
        boolean b = updateById(user);
        return Result.ok(b);
    }

    @Override
    public Result realName(String name, String idNumber) {
        //1. 获取当前用户
        UserDTO userDTO = CurrentUser.getUser();
        if (userDTO == null) {
            return Result.fail("请登录后再实名！");
        }
        //2. 加密并保存
        //2.1 加密储存
        User user = query().eq("id", userDTO.getId()).one();
        try {
            user.setName(Encoder.encryptAES(name, SECRET_NAME_KEY));
            user.setIdNumber(Encoder.encryptAES(idNumber, SECRET_NAME_KEY));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //2.2 储存到数据库
        updateById(user);
        return Result.ok();
    }

    @Override
    public Result getRealName() {
        //1. 获取当前用户信息
        UserDTO userDTO = CurrentUser.getUser();
        if (userDTO == null) {
            return Result.fail("该用户未登录");
        }
        Long userId = userDTO.getId();
        User user = getById(userId);

        //1.1 查看是否进行了实名认证
        if (user.getName() == null) {
            return Result.ok(null);
        }

        //2. 获取实名信息
        String name = "";
        String idNumber = "";
        try {
            name = Encoder.decryptAES(user.getName(), SECRET_NAME_KEY);
            idNumber = Encoder.decryptAES(user.getIdNumber(), SECRET_NAME_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RealNameData data = new RealNameData(name, idNumber);
        return Result.ok(data);
    }

    @Override
    public Result getBalance() {
        Long id = CurrentUser.getUser().getId();
        Double balance = query().select("balance").eq("id", id).one().getBalance();
        return Result.ok(balance);
    }

    @Override
    public Result addBalance(double value) {
        Long id = CurrentUser.getUser().getId();
        User user = query().eq("id", id).one();
        user.setBalance(user.getBalance() + value);
        boolean b = updateById(user);
        if (!b){
            return Result.fail("充值失败！");
        }
        return Result.ok();
    }

    @Override
    public Result out() {
        // 删除登录的token即可
        String token = CurrentUser.getToken();
        if (token.isBlank()) {
            return Result.fail("当前未登录！");
        }
        Boolean delete = stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        if (Boolean.TRUE.equals(delete)) {
            return Result.ok();
        } else
            return Result.fail("退出失败");
    }

    @Override
    public Result register(String email, String code) {
        //1 取出redis中的code
        String redisCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + email);
        //2 进行比较
        if (redisCode == null || !redisCode.equals(code)) {
            return Result.fail("验证码错误！");
        }
        //创建新的用户
        if (query().eq("email", email).count() != 0) {
            return Result.fail("该用户已存在");
        }
        long userId = createUser(email);
        return Result.ok(userId);
    }

    private long createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setNickName(USER_NICKNAME_KEY + RandomUtil.randomNumbers(8));
        user.setCreateTime(TimeFormatUtil.getTime());
        user.setBalance(0.00);

        //生成id
        long userId = new CreateID(stringRedisTemplate).nextUserId();
        if (userId == 0) {
            throw new RuntimeException("注册用户失败");
        }

        user.setId(userId);
        boolean b = save(user);
        if (!b) {
            throw new RuntimeException("注册用户失败");
        }
        return userId;
    }
}
