package com.shawn.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.BaseContext;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.OrderDetail;
import com.shawn.reggie.entity.Orders;
import com.shawn.reggie.service.OrderDetailService;
import com.shawn.reggie.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R <String> submit(@RequestBody Orders order){
        orderService.submit(order);
        return R.success("下单成功");
    }

    /**
     * 订单的分页查询，有待优化
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R <Page> page ( Integer page, Integer pageSize){
        Page<OrderDetail> pageInfo = new Page<> (page,pageSize);
        Long userId = BaseContext.getCurrentId();
        //select * from order_detail where userId = ?
        LambdaQueryWrapper <Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);

        List <Orders> orders = orderService.list(queryWrapper);
        List<Long> ordersId = new ArrayList <>();
        for (Orders o:orders){
            ordersId.add(o.getId());
        }

        LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper <>();
        queryWrapper1.in(OrderDetail::getOrderId,ordersId);

        orderDetailService.page(pageInfo, queryWrapper1);
        return R.success(pageInfo);
    }
}
