package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealPage = setmealMapper.page(setmealPageQueryDTO);
        // 根据分类id获取分类名称
        List<SetmealVO> setmeals = setmealPage.getResult();
        for(SetmealVO setmealVO : setmeals){
            setmealVO.setCategoryName(categoryMapper.query(setmealVO.getCategoryId()));
        }
        return new PageResult(setmealPage.getTotal(),setmealPage.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void insertWithDishs(SetmealDTO setmealDTO) {
        // 添加套餐（setmeal表）
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        // 添加套餐的菜品（setmeal_dish表）
        //  设置套餐id
        Integer setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            for(SetmealDish setmealDish : setmealDishes){
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }

    }

    /**
     * 根据套餐id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSmBySmIdWithDish(Integer id) {
        // 查询setmeal表，根据套餐id返回套餐
        Setmeal setmeal = setmealMapper.getSmBySmId(id);
        // 查询setmeal_dish表，根据套餐id返回套餐下的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySmId(id);
        // 根据分类id查找分类名称并添加
        Integer cateId = setmeal.getCategoryId();
        String cateName = categoryMapper.query(cateId);
        // 返回完整的SetmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        setmealVO.setCategoryName(cateName);
        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Integer> ids) {
        // 判断是否符合规则，只要有一个不符合则删除失败
        for(Integer id : ids){
            // 根据套餐id查询套餐
            Setmeal setmeal = setmealMapper.getSmBySmId(id);
            if(setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE_FOR_DELETE);
            }
        }
        // 根据套餐id删除setmeal表
        setmealMapper.deleteBatchBySmId(ids);
        // 根据套餐id删除setmeal_dish表
        setmealDishMapper.deleteBatchBySmId(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        // TODO 在前端添加 启售状态无法更新套餐 的逻辑
        // 如果套餐处于启售状态，则不能修改
//        if(setmealDTO.getStatus().equals(StatusConstant.ENABLE)){
//            log.info("修改失败");
//            throw new SetmealUpdateFailedException(MessageConstant.SETMEAL_ON_SALE_FOR_UPDATE);
//        }
        // 修改基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        // 修改套餐内菜品信息
        //   删除
        Integer id = setmealDTO.getId();
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        setmealDishMapper.deleteBatchBySmId(ids);
        //   添加
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            // 设置套餐id
            Integer setmealId = setmealDTO.getId();
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 设置启售、停售状态
     * @param status
     */
    @Override
    public void startOrStop(Integer status, Integer id) {
        // 取出套餐内的菜品，查询是否处于启售状态
        //   根据套餐id查询所有菜品的id(setmeal_dish)
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySmId(id);
        for(SetmealDish setmealDish : setmealDishes){
            // 查询菜品的状态(dish)
            Integer dishId = setmealDish.getDishId();
            Dish dish = dishMapper.getById(Math.toIntExact(dishId));
            if(dish.getStatus().equals(StatusConstant.DISABLE)){
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        // 修改售卖状态
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealMapper.update(setmeal);
    }

    /**
     * List<Setmeal>
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> getSetmealByCategoryId(Setmeal setmeal) {
        return setmealMapper.getSmByCateId(setmeal);
    }


}
