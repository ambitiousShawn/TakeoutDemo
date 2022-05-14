package com.shawn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shawn.reggie.common.CustomException;
import com.shawn.reggie.entity.Category;
import com.shawn.reggie.entity.Dish;
import com.shawn.reggie.entity.Setmeal;
import com.shawn.reggie.mapper.CategoryMapper;
import com.shawn.reggie.mapper.DishMapper;
import com.shawn.reggie.mapper.SetmealMapper;
import com.shawn.reggie.service.CategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;


    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper <>();

        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);

        int count1 = dishMapper.selectCount(dishLambdaQueryWrapper);

        //查询当前分类是否关联菜，如果关联，抛出业务异常
        if (count1 > 0 ){
            //抛出一个业务异常
            throw new CustomException("当前分类项关联了菜品，无法删除");
        }

        //查询当前分类是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper <>();
        //添加查询条件
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealMapper.selectCount(setmealLambdaQueryWrapper);
        if(count2 > 0){
            //抛出一个业务异常
            throw new CustomException("当前分类项关联了套餐，无法删除");
        }

        //正常删除分类
        super.removeById(id);
    }
}
