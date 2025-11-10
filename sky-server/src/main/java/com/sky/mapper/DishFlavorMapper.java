package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 新增菜品口味
     * @param dishFlavorList
     */
//    @AutoFill(value = OperationType.INSERT)  err：为菜品添加口味不需要自动填充字段
    public void insertBatch(List<DishFlavor> dishFlavorList);

    /**
     * 根据菜品id查询菜品口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> queryByDishId(Integer dishId);

    /**
     * 根据菜品id删除菜品口味
     * @param dishIds
     */
    void deleteByDishIds(List<Integer> dishIds);

}
