package com.shawn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shawn.reggie.dto.DishDto;
import com.shawn.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，并新增口味数据
    void saveWithFlavor(DishDto dishDto);

    //查找连同口味的菜品信息
    DishDto getByIdWithFlavor(Long id);

    //修改菜品信息
    void updateDishWithFlavor(DishDto dishDto);

    //修改菜品状态
    void modifyStatus(List <Long> ids,Integer type);

    //删除菜品
    void deleteDish (List<Long> ids);
}
