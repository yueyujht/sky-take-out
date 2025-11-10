package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SetmealDishMapper setMealDishMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // 添加一条到dish表
        dishMapper.insert(dish);
        // 获取insert语句返回的主键值
        Long id = dish.getId();
        // 添加到dish_flavor表
        List<DishFlavor> dishFlavorList = dishDTO.getFlavors();
        if(dishFlavorList != null && !dishFlavorList.isEmpty()){
            dishFlavorList.forEach(d -> d.setDishId(id));
            dishFlavorMapper.insertBatch(dishFlavorList);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 根据id查询菜品（包括口味）
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Integer id) {
        // dish表
        DishVO dishVO = dishMapper.getById(id);
        // dish_flavor表
        List<DishFlavor> dishFlavorList = dishFlavorMapper.queryByDishId(id);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            // 根据id查询菜品
            DishVO dishVO = dishMapper.getById(id);
            if(dishVO.getStatus() != StatusConstant.DISABLE){
                // 启售状态，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 根据id查询菜品是否与套餐关联
        List<Integer> setmealIdList = setMealDishMapper.getSetmealIdByDishId(ids);
        if(setmealIdList != null && !setmealIdList.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 停售状态，可以删除
        //  删除dish表
        dishMapper.deleteByIds(ids);
        // 删除dish_flavor表
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 修改菜品
     * @param dishVO
     */
    @Override
    @Transactional
    public void update(DishVO dishVO) {
        // 修改dish表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO,dish);
        dishMapper.update(dish);
        // 修改dish_falvor表
        //   根据菜品id删除菜品口味
        List<Integer> dishIds = new ArrayList<>();
        dishIds.add(Math.toIntExact(dishVO.getId()));
        dishFlavorMapper.deleteByDishIds(dishIds);
        //   根据菜品id新增菜品口味
        List<DishFlavor> flavors = dishVO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(f -> f.setDishId(dishVO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
