package com.game.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.game.entity.Good;
import com.game.other.Result;
import com.game.other.UserDTO;
import com.game.service.IGoodService;
import com.game.service.IVoteService;
import com.game.service.Impl.UserInfoServiceImpl;
import com.game.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.game.utils.RedisConstants.GOOD_INFO_KEY;

@Slf4j
@RestController
@RequestMapping("/good")
public class GoodController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IGoodService goodService;
    @Resource
    private IVoteService voteService;
    @Resource
    private UserInfoServiceImpl userInfoService;

    /**
     * 创建商品
     *  商品对象 good包含的字段 title， stock， tags， price， images，description，其他的均为空即可
     * @return true or false
     */
    @PostMapping("/createGood")
    public Result createGood(@RequestParam("title") String title,
                             @RequestParam("stock") long stock,
                             @RequestParam("tags") String tags,
                             @RequestParam("price") double price,
                             @RequestParam("description") String description,
                             @RequestParam("images") MultipartFile[] images) {
        Good good = new Good();
        good.setTitle(title);
        good.setStock(stock);
        good.setTags(tags);
        good.setPrice(price);
        good.setDescription(description);
        String[] filenames = FileUtil.uploadFiles(images);
        //对文件名称进行拼接
        StringBuilder img = new StringBuilder();
        for (String name : filenames) {
            img.append(name).append(',');
        }
        good.setImages(img.toString());
        return goodService.createGood(good);
    }

    /**
     * 下架商品
     * @param id 下架商品的id
     * @return true or false
     */
    @PostMapping("/removeGood{id}")
    public Result removeGood(@PathVariable Long id) {
        return goodService.removeGood(id);
    }

    /**
     * 重新上架商品
     * @param id 商品id
     * @return true or false
     */
    @PostMapping("/againAddGood{id}")
    public Result addGood(@PathVariable Long id) {
        return goodService.addGood(id);
    }

    /**
     * 修改商品信息
     * @param good 必要字段 商品id 其他仅只需要修改的字段即可
     *             仅有这个几个字段可以修改（title， amount， tags， images，description）
     * @return true or false
     */
    @PostMapping("/updateGood")
    public Result updateGood(@RequestBody Good good){
        return goodService.updateGood(good);
    }

    /**
     * 修改商品价格
     * @param id 商品id
     * @param price 最后的价格
     * @return true or false
     */
    @PostMapping("/updatePrice")
    public Result updatePrice(@RequestParam("id") Long id,
                              @RequestParam("price") Double price){
        //1. 修改售卖价格
        boolean b = goodService.update(new UpdateWrapper<Good>()
                .set("price", price)
                .set("update_time", LocalDateTime.now()));
        if (!b){
            return Result.fail("修改价格失败！");
        }
        //2. 更新redis
        stringRedisTemplate.opsForHash().put(GOOD_INFO_KEY + id, "price", price);

        //3. 判断是否需要删除该商品的vote数据
        if (goodService.checkPrice(id, price)) {
            //需要删除vote的数据
            if (voteService.removeVoteByGoodId(id)) {
                return Result.ok();
            }
            return Result.fail("价格修改失败！");
        }
        return Result.ok();
    }

    /**
     * 获取商品信息
     * @param id 商品id
     * @return good
     */
    @GetMapping("/getGoodInfo{id}")
    public Result getGoodInfo(@PathVariable("id") Long id){
        Result goodInfo = goodService.getGoodInfo(id);
        //log.error(goodInfo.toString());
        Good data = (Good) goodInfo.getData();
        //获取昵称
        UserDTO data1 = (UserDTO) userInfoService.getUserDTO(data.getUserId()).getData();
        JSONObject res = JSONUtil.parseObj(data).putOpt("nickName", data1.getNickName());
        //处理图片
        String img = (String) res.get("images");
        res.remove("images");
        String[] imgs = img.split(",");
        res.putOpt("images", imgs);
        return Result.ok(res);
    }

    /**
     * 根据userId或者 tags查询 商品
     * @param key key
     * @param value value
     * @return page
     */
    @GetMapping("/getGoodList")
    public Result getGoodList(@RequestParam("key") String key,
                              @RequestParam("value") String value,
                              @RequestParam("current") Integer current,
                              @RequestParam("pageSize") Integer pageSize) {
        return goodService.getGoodPage(current, pageSize, key, value);
    }

    /**
     * 获取全部数据
     *
     * @return goodDTOList
     */
    @GetMapping("/getAll")
    public Result getAllGood(@RequestParam("current") Integer current,
                             @RequestParam("pageSize") Integer pageSize) {
        return goodService.getAllGood(current, pageSize);
    }

    /**
     * 根据description或者 title模糊查询 商品
     * @param value value
     * @return page
     */
    @GetMapping("/getGoodByMo")
    public Result getGoodByMo(@RequestParam("value") String value,
                              @RequestParam("current") Integer current,
                              @RequestParam("pageSize") Integer pageSize) {
        return goodService.getGoodByMo(current, pageSize, value);
    }

    /**
     * 查询不同状态的商品
     * @param status 状态
     * @return goodDTO list
     */
    @GetMapping("/getGoodByStatus")
    public Result getGoodByStatus(@RequestParam("status") Integer status,
                                  @RequestParam("current") Integer current,
                                  @RequestParam("pageSize") Integer pageSize) {
        return goodService.getGoodByStatus(current, pageSize, status);
    }

    /**
     * 判断对某个商品是否投票,测试使用
     * @param goodId 商品
     * @return true or false
     */
    @GetMapping("/idVote{goodId}")
    public Result isVote(@PathVariable("goodId") Long goodId){
        if (voteService.isVote(goodId)) {
            return Result.ok();
        }
        return Result.fail("未对售价进行投票！");
    }
}
