package com.game.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Good;
import com.game.mapper.GoodMapper;
import com.game.other.GoodDTO;
import com.game.other.Result;
import com.game.service.IGoodService;
import com.game.utils.CreateID;
import com.game.utils.CurrentUser;
import com.game.utils.TimeFormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.game.utils.RedisConstants.*;

@Slf4j
@Service
public class GoodServiceImpl extends ServiceImpl<GoodMapper, Good> implements IGoodService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result createGood(Good goodParam) {
        //1. 获取good
        Good good = goodFactory(goodParam);
        if (good.getImages() == null) {
            return Result.fail("至少要选择一张图片");
        }

        //2. 存储数据
        //2.1 存储到数据库
        boolean b = save(good);
        if (!b) {
            return Result.fail("创建失败！");
        }
        //2.2 存储到redis（good）
        //2.2.1存储商品集合
        Long userId = CurrentUser.getUser().getId();
        stringRedisTemplate.opsForSet().add(CREATE_GOOD_USER_KEY + userId, good.getId().toString());

        //2.2.2 上传商品数量到redis
        stringRedisTemplate.opsForValue().set(GOOD_STOCK_KEY + good.getId(), good.getStock().toString());

        //2.2.2 存储goodInfo到redis
        return upload(good);
    }

    @Override
    public Result removeGood(Long id) {
        //1. 判断该商品的发布人
        Good good = check(id);
        if (good == null) {
            return Result.fail("你无权操作该商品！");
        }
        //2. 修改数据库的该商品状态
        boolean b = update(new UpdateWrapper<Good>().set("status", -1).eq("id", id));
        if (!b) {
            return Result.fail("下架失败！");
        }

        //4. 删除redis中的数据
        String goodKey = GOOD_INFO_KEY + id;
        stringRedisTemplate.delete(goodKey);

        return Result.ok();
    }

    @Override
    public Result addGood(Long id) {
        //1. 获取该商品
        Good good = check(id);
        if (good == null) {
            return Result.fail("无权操作该商品！");
        }
        //2. 修改数据库内容
        boolean b = update(new UpdateWrapper<Good>().set("status", 0).eq("id", id));
        if (!b) {
            return Result.fail("重新上架失败！");
        }
        //3. 重新存储到redis
        return upload(good);
    }

    @Override
    public Result updateGood(Good good) {
        //1. 获取数据库的商品信息
        Long goodId = good.getId();
        Good newGood = check(goodId);
        if (newGood == null) {
            return Result.fail("没有权限操作该商品！");
        }
        //2. 更新数据

        BeanUtil.copyProperties(good, newGood, CopyOptions.create().setIgnoreNullValue(true));
        log.info(newGood.toString());
        //2.1 设置修改时间
        newGood.setUpdateTime(TimeFormatUtil.getTime());

        //3. 重新存储到数据库
        boolean b = updateById(newGood);
        if (!b) {
            return Result.fail("上传数据库失败！");
        }
        //4. 更新redis中good：info的数据
        return upload(newGood);
    }

    @Override
    public boolean checkPrice(Long id, Double newPrice) {
        //判断是否要删除该商品的vote数据
        //1. 获取该对象
        Good good;
        Map<Object, Object> goodCache = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + id);
        if (goodCache.isEmpty()) {
            good = query().eq("id", id).one();
        } else {
            good = BeanUtil.mapToBean(goodCache, Good.class, false,
                    CopyOptions.create()
                            .setIgnoreNullValue(false)
                            .setFieldValueEditor((f, v) -> (v == null ? null : v.toString()))
            );
        }
        //2. 获取原本的价格数据oldPrice
        Double oldPrice = good.getPrice();

        //3. 查询usePrice的值
        Integer usePrice = good.getUsePrice();

        //4. 根据usePrice的值判断是否要删除vote的数据
        if (usePrice == 0) {
            //价格合适，删除vote数据即可
            return true;
        } else if (usePrice > 0 && newPrice < oldPrice) {
            //符合修改价格的条件(偏贵，且价格得到了降低)
            boolean b = update(new UpdateWrapper<Good>().set("use_price", 0).eq("id", id));
            if (!b) {
                throw new RuntimeException("use_price更新失败！");
            }
            return true;
        } else if (usePrice < 0 && newPrice > oldPrice) {
            //符合修改价格的条件(偏便宜，且价格得到了提升)，理论上不存在（狗头）
            boolean b = update(new UpdateWrapper<Good>().set("use_price", 0).eq("id", id));
            if (!b) {
                throw new RuntimeException("use_price更新失败！");
            }
            return true;
        }
        //其他情况保持原样即可
        return false;
    }

    @Override
    public Result getGoodInfo(Long id) {
        //1. 从redis中获取
        Good good;
        Map<Object, Object> goodMap = stringRedisTemplate.opsForHash().entries(GOOD_INFO_KEY + id);
        if (!goodMap.isEmpty()) {
            good = BeanUtil.mapToBean(goodMap, Good.class, false,
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
            );
        } else {
            //2. 从数据库获取
            good = getById(id);
            if (good == null) {
                return Result.fail("该id的商品不存在！");
            }

            //2.1 缓存到redis中
            Map<String, Object> cacheMap = BeanUtil.beanToMap(good, new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(false)
                            .setFieldValueEditor((f, v) -> (v == null ? null : v.toString()))
            );
            stringRedisTemplate.opsForHash().putAll(GOOD_INFO_KEY + id, cacheMap);
        }

        //3. 求出isVote 和 isAddCar, isComment
        Long userId = CurrentUser.getUser().getId();
        Boolean vote = stringRedisTemplate.opsForSet().isMember(VOTE_USER_GOOD + id, userId.toString());
        good.setIsVote(vote);

        Boolean car = stringRedisTemplate.opsForSet().isMember(CAR_ID_INFO + userId, id.toString());
        good.setIsAddCar(car);

        Boolean comment = stringRedisTemplate.opsForSet().isMember(GOOD_ORDER_KEY + id, userId.toString());
        good.setIsComment(comment);
        return Result.ok(good);
    }

    @Override
    public Result getGoodPage(Integer current, Integer pageSize, String key, String value) {
        //userId, tags 仅支持这两个个字段的查询
        Page<Good> page = new Page<>(current, pageSize);
        QueryWrapper<Good> query = new QueryWrapper<>();
        //默认不获取当前用户的商品信息
        Long userId = CurrentUser.getUser().getId();
        query = query.select("id", "title", "images", "stock", "price")
                .eq("status", 0)
                .orderByDesc("create_time");
        if (key.equals("userId")) {
            //按照id查找
            query = query.eq("userId", Long.parseLong(value));
        } else {
            query = query.eq("tags", value);
        }
        return Result.ok(selectByPage(query, page));
    }

    @Override
    public Result getGoodByMo(Integer current, Integer pageSize, String value) {
        //根据 description 或者 title查询 商品
        //直接从数据库查询即可
        Page<Good> page = new Page<>(current, pageSize);
        QueryWrapper<Good> query = new QueryWrapper<Good>().like("title", value)
                .or().like("description", value)
                .orderByDesc("create_time");
        return Result.ok(selectByPage(query, page));
    }

    @Override
    public Result getGoodByStatus(Integer current, Integer pageSize, Integer status) {
        //1. 获取当前用户id
        Long userId = CurrentUser.getUser().getId();
        if (userId == null) {
            return Result.fail("请登陆后操作！");
        }
        QueryWrapper<Good> queryWrapper = new QueryWrapper<Good>()
                .select("id", "title", "images", "stock", "price")
                .eq("status", status)
                .eq("user_id", userId)
                .orderByDesc("create_time");
        Page<Good> page = new Page<>(current, pageSize);
        return Result.ok(selectByPage(queryWrapper, page));
    }

    @Override
    public Result getAllGood(Integer current, Integer pageSize) {
        Long userId = CurrentUser.getUser().getId();
        QueryWrapper<Good> goodQueryWrapper = new QueryWrapper<Good>()
                .select("id", "title", "images", "stock", "price")
                .eq("status", 1)
                .notIn("user_id", userId)
                .notIn("stock", 0)
                .orderByDesc("create_time");
        Page<Good> page = new Page<>(current, pageSize);

        return Result.ok(selectByPage(goodQueryWrapper, page));
    }

    //工厂
    private Good goodFactory(Good good) {
        /*
        good包含的字段 title， stock， tags， price， images，description，其他的均为空即可
         */
        //1. 生成goodId
        long goodId = new CreateID(stringRedisTemplate).nextId(GOOD_ID_KEY);
        good.setId(goodId);
        //2. 获取当前用户id
        Long userId = CurrentUser.getUser().getId();
        good.setUserId(userId);
        //3. 设置状态 status,在售
        good.setStatus(0);
        //4. 设置usePrice建议价格
        good.setUsePrice(0);
        //5. 设置发布时间和更新时间
        good.setCreateTime(TimeFormatUtil.getTime())
                .setUpdateTime(TimeFormatUtil.getTime());
        return good;
    }

    //确认是否为商品的发起人,并返回该商品
    private Good check(Long id) {
        //1. 获取当前用户
        Long userId = CurrentUser.getUser().getId();
        //2. 返回该商品
        return query().eq("id", id).eq("user_id", userId).one();
    }

    //将good 数据存储到redis中
    private Result upload(Good good) {
        if (good == null) {
            return Result.fail("商品为空！");
        }
        try {
            //全部存储到redis中
            Map<String, Object> goodMap = BeanUtil.beanToMap(good, new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((field, value) -> value != null ? value.toString() : null)
            );
            stringRedisTemplate.opsForHash().putAll(GOOD_INFO_KEY + good.getId(), goodMap);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.fail("存储到redis失败！");
        }
        return Result.ok(good.getId());
    }

    //good -> goodDTO
    private List<GoodDTO> toGoodDTO(List<Good> list) {
        List<GoodDTO> goodDTOS = new ArrayList<>();
        list.forEach(
                good -> {
                    GoodDTO goodDTO = new GoodDTO();
                    goodDTO.setPrice(good.getPrice());
                    goodDTO.setTitle(good.getTitle());
                    goodDTO.setId(good.getId());
                    goodDTO.setStock(good.getStock());
                    String[] split = good.getImages().split(",");
                    goodDTO.setImages(split[0]);

                    goodDTOS.add(goodDTO);
                }
        );
        return goodDTOS;
    }

    //处理图片对象
    private Page<Good> selectByPage(QueryWrapper<Good> query, Page<Good> page) {
        Page<Good> list = baseMapper.selectPage(page, query);
        list.getRecords().stream().peek(good -> {
            String newImages = good.getImages().split(",")[0];
            good.setImages(newImages);
        }).toList();
        if (list.getSize() == 0) {
            return null;
        }
        return list;
    }

}
