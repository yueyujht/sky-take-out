package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品及其口味
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品及其口味")
    public Result getDishByCategoryId(Integer categoryId){
        log.info("根据分类id查询菜品及其口味：{}",categoryId);
        // redis-key
        String key = "dish_" + categoryId;
        // 查询redis是否有菜品缓存数据
        List<DishVO> dishVOoList = (List<DishVO>)redisTemplate.opsForValue().get(key);
        if(dishVOoList != null && !dishVOoList.isEmpty()){
            // 存在 -> 返!
            return Result.success(dishVOoList);
        }

        // 不存在 -> 将查询到的数据载入redis
        // NOTE:停售的菜品不展示
        dishVOoList = dishService.listWithFlavor(categoryId);
        redisTemplate.opsForValue().set(key,dishVOoList);

        return Result.success(dishVOoList);
    }
}
