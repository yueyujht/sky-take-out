package com.sky.mapper;

import com.sky.entity.DishFlavor;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingcartMapper {
    @Select("select number from sky_take_out.shopping_cart where user_id = #{userId} and dish_id = #{dishId} and dish_flavor = #{dishFlavor}")
    Integer countDishNumber(Integer userId, Integer dishId,String dishFlavor);

    @Select("select number from sky_take_out.shopping_cart where user_id = #{userId} and setmeal_id = #{setmealId}")
    Integer countSetmealNumber(Integer userId, Integer setmealId);

    Integer countNumber(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     */
    void addCart(ShoppingCart shoppingCart);

    List<ShoppingCart> list(Integer userId);

    void updateCart(ShoppingCart shoppingCart);

    void delOneCart(ShoppingCart shoppingCart);

    // 清空购物车
    @Delete("delete from sky_take_out.shopping_cart where user_id = #{userId}")
    void delAllCart(Integer userId);

    @Select("select sum(number) from shopping_cart")
    Integer countCartNumber();

    @Select("select dish_flavor from sky_take_out.shopping_cart where dish_id = #{dishId}")
    List<String> listFlavorByDishId(Integer dishId);
}
