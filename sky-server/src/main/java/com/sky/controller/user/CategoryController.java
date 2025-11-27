package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCategory")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 条件查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("条件查询分类")
    public Result list(Integer type){
        List<Category> categoryList;
        if(type==null){
            log.info("查询全部分类");
            categoryList = categoryService.list(1);
            categoryList.addAll(categoryService.list(2));
        } else {
            log.info("条件查询分类:{}",type == 1 ? "菜品分类" : "套餐分类");
            categoryList = categoryService.list(type);
        }
        return Result.success(categoryList);
    }
}
