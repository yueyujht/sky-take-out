package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void insertWithDishs(SetmealDTO setmealDTO);

    /**
     * 根据套餐id查询套餐
     * @param id
     * @return
     */
    SetmealVO getSmBySmIdWithDish(Integer id);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 设置启售、停售状态
     * @param status
     */
    void startOrStop(Integer status, Integer id);

    /**
     * 根据分类id获取套餐
     * @param categoryId
     * @return
     */
    List<Setmeal> getSetmealByCategoryId(Setmeal setmeal);
}
