package com.shawn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shawn.reggie.common.CustomException;
import com.shawn.reggie.dto.DishDto;
import com.shawn.reggie.entity.Dish;
import com.shawn.reggie.entity.DishFlavor;
import com.shawn.reggie.entity.SetmealDish;
import com.shawn.reggie.mapper.DishMapper;
import com.shawn.reggie.service.DishFlavorService;
import com.shawn.reggie.service.DishService;
import com.shawn.reggie.service.SetmealDishService;
import com.shawn.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl <DishMapper, Dish> implements DishService {

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private SetmealService setmealService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List <DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查找连同口味的菜品信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查找菜品的基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查找当前菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List <DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    public void updateDishWithFlavor(DishDto dishDto) {
        //查询菜品的基本信息
        this.updateById(dishDto);

        //查询菜品的口味信息
        //清理当前菜品对应数据-dish flavor表中delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        //添加当前口味数据的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 修改菜品的销售状态
     * 如果菜品存在对应套餐，那么套餐也应该被停售(有些小bug，后面再改)
     * @param ids
     */
    @Override
    public void modifyStatus(List <Long> ids, Integer type) {
        LambdaUpdateWrapper <Dish> updateWrapper = new LambdaUpdateWrapper <>();
        updateWrapper.in(Dish::getId,ids);

        if (type == 0){
            //禁用包含该菜品的套餐

            LambdaQueryWrapper <SetmealDish> queryWrapper = new LambdaQueryWrapper <>();
            queryWrapper.in(SetmealDish::getDishId,ids);
            List <SetmealDish> list = setmealDishService.list(queryWrapper);

            List<Long> setmealId = new LinkedList <>();
            for (SetmealDish sd : list){
                setmealId.add(sd.getSetmealId());
            }

            setmealService.modifyStatus(setmealId,0);
            //批量停售
            updateWrapper.set(Dish::getStatus,0);
        }else {
            updateWrapper.set(Dish::getStatus,1);
        }
        this.update(updateWrapper);
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    public void deleteDish(List<Long> ids){
        //查询菜品状态，并查询菜品是否在其他套餐中
        //select count(*) from setmeal_dish where dishId = ?
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.in(SetmealDish::getDishId,ids);

        int count = setmealDishService.count(queryWrapper);
        if (count > 0){
            //其余套餐有关联餐品信息
            throw new CustomException("菜品与某套餐关联，无法删除");
        }
        //select count(*) from dish where status = ? & id = ?
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper <>();
        queryWrapper1.in(Dish::getId,ids);
        queryWrapper1.eq(Dish::getStatus,1);

        int count1 = this.count(queryWrapper1);
        if (count1 > 0){
            throw new CustomException("有菜品仍在售卖，无法删除");
        }


        LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper <>();
        queryWrapper2.in(Dish::getId,ids);
        this.remove(queryWrapper2);
    }
}
