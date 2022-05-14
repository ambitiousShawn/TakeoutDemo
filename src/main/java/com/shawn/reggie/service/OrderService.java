package com.shawn.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shawn.reggie.entity.Orders;


public interface OrderService extends IService<Orders> {

    void submit (Orders orders);
}
