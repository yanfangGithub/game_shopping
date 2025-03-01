package com.game.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Comments;
import com.game.mapper.CommentMapper;
import com.game.other.CommentDTO;
import com.game.other.Result;
import com.game.service.ICommentService;
import com.game.utils.CreateID;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.game.utils.RedisConstants.*;

/**
 * @author yanfang
 * @version 1.0
 * &#064;date  2024/5/14 22:20
 */

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comments>
        implements ICommentService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private OrderServiceImpl orderService;

    @Override
    public Result createComment(Comments comments) {
        //1. 进行判断是否合理
        Long goodId = comments.getGoodId();
        Long userId = CurrentUser.getUser().getId();
        Boolean member = stringRedisTemplate.opsForSet().isMember(GOOD_ORDER_KEY + goodId, userId.toString());
        if (Boolean.FALSE.equals(member)){
            return Result.fail("不能进行评价！");
        }

        //2. 生成id
        long id = new CreateID(stringRedisTemplate).nextId(COMMENT_ID_KEY);

        //3. 保存到数据库
        comments.setId(id);
        comments.setCreateTime(TimeFormatUtil.getTime());
        comments.setUserId(userId);
        boolean b = save(comments);
        if (!b){
            return Result.fail("保存到数据库失败！");
        }
        //3.1 更新订单表
        orderService.update().set("status", 2).eq("good_id", goodId).eq("buyer_id", userId).update();
        //4. 保存到redis 保存id集合key = good:comment:goodId  value userId
        Long len = stringRedisTemplate.opsForSet().add(COMMENT_INFO_KEY + goodId, userId.toString());

        return Result.ok();
    }

    @Override
    public Result getCommentByGoodId(Long goodId) {
        //1 去数据库查询
        List<Comments> comments = query().eq("good_id", goodId).list();

        //2. 数据处理
        List<CommentDTO> result = new ArrayList<>();
        comments.forEach(
                comment -> {
                    //查询用户信息
                    Map<Object, Object> userInfo = stringRedisTemplate.opsForHash()
                            .entries(USER_INFORMATION_KEY + comment.getUserId());
                    CommentDTO dto = new CommentDTO(userInfo.get("nickName").toString(),
                            userInfo.get("icon").toString(),
                            comment.getLevel(),
                            comment.getComment(),
                            comment.getCreateTime());
                    result.add(dto);
                }
        );
        return Result.ok(result);
    }
}
