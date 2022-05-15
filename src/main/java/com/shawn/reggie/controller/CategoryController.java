package com.shawn.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.Category;
import com.shawn.reggie.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 新增菜品或者套餐
     * @param category
     * @return
     */
    @PostMapping
    public R <String> save (@RequestBody Category category){
        //System.out.println(category+"已被添加");
        categoryService.save(category);
        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page (int page,int pageSize){

        //分页构造器
        Page<Category> pageInfo = new Page <>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper <>();
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //进行分页查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){

        categoryService.remove(ids);
        return R.success("删除菜品成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){

        categoryService.updateById(category);

        return R.success("修改分类信息成功");
    }

    /**
     * 查询所有菜品分类
     * @param category 获取到是菜品 1 还是套餐 0
     * @return
     */
    //根据条件查询分类数据
    @GetMapping("/list")
    public R<List<Category>> list (Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper <>();
        //添加条件
        queryWrapper.eq(category.getType()!= null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
