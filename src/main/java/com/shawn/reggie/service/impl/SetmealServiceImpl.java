package com.shawn.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shawn.reggie.common.CustomException;
import com.shawn.reggie.dto.SetmealDto;
import com.shawn.reggie.entity.Setmeal;
import com.shawn.reggie.entity.SetmealDish;
import com.shawn.reggie.mapper.SetmealMapper;
import com.shawn.reggie.service.SetmealDishService;
import com.shawn.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private SetmealService setmealService;

    /**
     * 保存新增套餐的信息
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto){
        //保存基础的Setmeal
        this.save(setmealDto);

        //取出setmealDto中的菜品信息
        List <SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存附带的Dish
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除某若干数据
     * @param ids
     */
    @Override
    public void deleteWithDish(List<Long> ids) {
        //查询套餐状态
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomException("套餐正在售卖中，无法删除");
        }
        //删除关联表信息
        LambdaQueryWrapper<SetmealDish> queryWrapperL = new LambdaQueryWrapper<>();
        queryWrapperL.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapperL);
        //删除套餐表信息
        setmealService.removeByIds(ids);
    }

    /**
     * 修改套餐的销售状态
     * @param ids
     */
    @Override
    public void modifyStatus(List <Long> ids,Integer type) {
        LambdaUpdateWrapper <Setmeal> updateWrapper = new LambdaUpdateWrapper <>();
        //添加非空条件，否则菜品禁售 无关套餐情况下报错。
        updateWrapper.in(!ids.isEmpty(),Setmeal::getId,ids);
        if (type == 0){
            //批量停售
            updateWrapper.set(Setmeal::getStatus,0);
        }else {
            //批量起售前，必须保证其中包含菜品是在售状态
            updateWrapper.set(Setmeal::getStatus,1);
        }
        this.update(updateWrapper);
    }


}
