package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 根据分类id获取套餐
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id获取套餐")
    public Result getSetmealByCategaryId(Integer categoryId){
        log.info("根据分类id获取套餐:{}",categoryId);
        // NOTE:只有启售状态才能被客户端看到
        Setmeal setmeal = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Setmeal> setmealList = setmealService.getSetmealByCategoryId(setmeal);
        return Result.success(setmealList);
    }

    /**
     * 根据套餐id查询包含的菜品
     */
    @GetMapping("/dish/{id}")
    public Result getDishesBySetmealId(@PathVariable Integer id){
        log.info("根据套餐id查询包含的菜品：{}",id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySmId(id);
        return Result.success(setmealDishes);
    }
}
