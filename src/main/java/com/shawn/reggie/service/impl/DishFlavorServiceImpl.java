package com.shawn.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shawn.reggie.entity.DishFlavor;
import com.shawn.reggie.mapper.DishFlavorMapper;
import com.shawn.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl <DishFlavorMapper, DishFlavor> implements DishFlavorService {
}

