package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingcartMapper {
    @Select("select number from sky_take_out.shopping_cart where user_id = #{userId} and dish_id = #{dishId}")
    Integer countDishNumber(Integer userId, Integer dishId);

    @Select("select number from sky_take_out.shopping_cart where user_id = #{userId} and setmeal_id = #{setmealId}")
    Integer countSetmealNumber(Integer userId, Integer setmealId);

    Integer countNumber(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     */
    void addCart(ShoppingCart shoppingCartDTO);

    List<ShoppingCart> list();

    void updateCart(ShoppingCart shoppingCart);

    void delOneCart(ShoppingCart shoppingCart);

    @Delete("delete from sky_take_out.shopping_cart")
    void delAllCart();
}
