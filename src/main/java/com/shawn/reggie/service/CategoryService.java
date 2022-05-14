package com.shawn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shawn.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
