package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param ids
     * @return
     */
    List<Integer> getSetmealIdByDishId(List<Integer> ids);

    /**
     * 批量添加套餐中的菜品
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询该套餐下的所有菜品
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getSetmealDishBySmId(Integer setmealId);

    /**
     * 根据套餐id批量删除
     * @param setmealIds
     */
    void deleteBatchBySmId(List<Integer> setmealIds);
}
