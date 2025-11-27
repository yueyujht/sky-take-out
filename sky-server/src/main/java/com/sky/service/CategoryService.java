package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;


public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void addCate(CategoryDTO categoryDTO);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void deleteCate(Integer id);

    /**
     * 启用、禁用分类
     * @param status
     */
    void startOrStop(Integer status, Integer id);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void updateCate(CategoryDTO categoryDTO);

    /**
     * 根据类型查看分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}
