package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.addDish(dishDTO);
        // 清除redis缓存
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> queryById(@PathVariable Integer id){
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品（>=1）
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品（>=1）")
    public Result getsByCateId(Integer categoryId){
        log.info("根据分类id查询菜品（>=1）:{}",categoryId);
        List<Dish> dishList = dishService.getsByCateId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Integer> ids){
        log.info("批量删除菜品：{}",ids);
        dishService.deleteBatch(ids);
        // 清除所有缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    /**
     * 修改菜品
     * @param dishVO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishVO dishVO){
        log.info("修改菜品:{}",dishVO);
        dishService.update(dishVO);
        // 清除所有缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }

    /**
     * 设置启/停售
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("设置启/停售")
    public Result startOrStop(@PathVariable("status") Integer status,Integer id){
        log.info("设置启/停售：{},{}",status,id);
        dishService.startOrStop(status,id);
        // 清除所有缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();
    }
}
