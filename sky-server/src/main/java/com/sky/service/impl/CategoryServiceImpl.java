package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void addCate(CategoryDTO categoryDTO) {
        // 将dto封装到实体类
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .id(categoryDTO.getId())
                .type(categoryDTO.getType())
                .sort(categoryDTO.getSort())
                .status(0)
                .build();
        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> catePage = categoryMapper.pageQuery(categoryPageQueryDTO);
        long total = catePage.getTotal();
        List<Category> cateList = catePage.getResult();
        return new PageResult(total,cateList);
    }

    /**
     * 根据id删除分类
     * @param id
     */
    @Override
    public void deleteCate(Integer id) {
        // 查询该分类下是否有内容
        int dish_count = dishMapper.count(id);
        int setmeal_count = setmealMapper.count(id);
        if(dish_count != 0 || setmeal_count != 0){
            throw new DeletionNotAllowedException("该分类有内容！");
        }
        categoryMapper.deleteById(id);
    }

    /**
     * 启用、禁用分类
     * @param status
     */
    @Override
    public void startOrStop(Integer status,Long id) {
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();
        categoryMapper.update(category);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void updateCate(CategoryDTO categoryDTO) {
        Category category = new Category();
        System.out.println(categoryDTO);
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);
    }

    /**
     * 根据类型查看分类
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        List<Category> categoryList = categoryMapper.list(type);
        return categoryList;
    }
}
