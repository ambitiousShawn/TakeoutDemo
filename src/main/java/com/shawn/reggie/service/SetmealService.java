package com.shawn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shawn.reggie.dto.SetmealDto;
import com.shawn.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void saveWithDish(SetmealDto setmealDto);

    void deleteWithDish(List <Long> ids);

    void modifyStatus(List<Long> ids,Integer type);
}
