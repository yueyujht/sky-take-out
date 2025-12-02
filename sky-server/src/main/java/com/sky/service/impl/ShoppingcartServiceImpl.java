package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingcartMapper;
import com.sky.service.ShoppingcartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingcartServiceImpl implements ShoppingcartService {
    @Autowired
    private ShoppingcartMapper shoppingcartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    @Transactional
    public void addCart(ShoppingCartDTO shoppingCartDTO) {
        Integer userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart;  // 购物车对象
        Integer number;  // 购物车一条记录中的数量
        if (shoppingCartDTO.getDishId() != null) {
            // 判断菜品在购物车是否存在
            Integer dishId = shoppingCartDTO.getDishId();
            number = shoppingcartMapper.countDishNumber(userId,dishId);
            // 获取name、userId、dishId、dishFlavor、image、amount
            Dish dish = dishMapper.getById(dishId);
            shoppingCart = ShoppingCart.builder()
                    .image(dish.getImage())
                    .name(dish.getName())
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .dishId(dish.getId())
                    .userId(userId)
                    .amount(dish.getPrice())
                    .build();
        } else {
            Integer setmealId = shoppingCartDTO.getSetmealId();
            number = shoppingcartMapper.countSetmealNumber(userId,setmealId);
            Setmeal setmeal = setmealMapper.getSmBySmId(setmealId);
            shoppingCart = ShoppingCart.builder()
                    .image(setmeal.getImage())
                    .name(setmeal.getName())
                    .setmealId(setmeal.getId())
                    .userId(userId)
                    .amount(setmeal.getPrice())
                    .build();
        }
        // 判断套餐/菜品在购物车是否存在
        if(number == null){
            // insert
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingcartMapper.addCart(shoppingCart);
        } else {
            // update
            shoppingCart.setNumber(number + 1);
            shoppingcartMapper.updateCart(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Integer userId = BaseContext.getCurrentId();
        return shoppingcartMapper.list(userId);
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    @Transactional
    public void delOneCart(ShoppingCartDTO shoppingCartDTO) {
        Integer userId = BaseContext.getCurrentId();
        // 查看数量是否为1
        Integer number;
        if(shoppingCartDTO.getDishId() != null){
            number = shoppingcartMapper.countDishNumber(userId,shoppingCartDTO.getDishId());
        } else {
            number = shoppingcartMapper.countSetmealNumber(userId,shoppingCartDTO.getSetmealId());
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);
        if(number == 1){
            // delete
            shoppingcartMapper.delOneCart(shoppingCart);
        } else {
            // update
            shoppingCart.setNumber(number - 1);
            shoppingcartMapper.updateCart(shoppingCart);
        }
    }

    @Override
    public void cleanCart() {
        Integer userId = BaseContext.getCurrentId();
        shoppingcartMapper.delAllCart(userId);
    }
}
