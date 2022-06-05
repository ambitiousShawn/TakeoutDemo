package com.shawn.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.BaseContext;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.OrderDetail;
import com.shawn.reggie.entity.Orders;
import com.shawn.reggie.service.OrderDetailService;
import com.shawn.reggie.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        queryWrapper.eq(userId != null,Orders::getUserId,userId);

        List <Orders> orders = orderService.list(queryWrapper);
        List<Long> ordersId = new ArrayList <>();
        for (Orders o:orders){
            ordersId.add(o.getId());
        }

        if (orders.isEmpty()) {
            return R.success(pageInfo);
        }

        LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper <>();
        queryWrapper1.in(OrderDetail::getOrderId,ordersId);

        orderDetailService.page(pageInfo, queryWrapper1);
        return R.success(pageInfo);
    }

    @GetMapping("/page")
    public R <Page> page (Integer page, Integer pageSize, String number, String beginTime, String endTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin = null,end = null;
        if (beginTime != null&&endTime != null){
            begin = sdf.parse(beginTime);
            end = sdf.parse(endTime);
        }

        Page<Orders> pageInfo = new Page <>(page,pageSize);
        //select * from order where user_id=?,id like number,order_time between begin and end
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.like(number != null,Orders::getId,number);
        queryWrapper.between(beginTime!=null&&endTime!=null,Orders::getOrderTime,begin,end);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R <String> status (@RequestBody Orders orders) {
        //update orders set status=? where id = ?
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper <>();
        updateWrapper.eq(Orders::getId,orders.getId());
        updateWrapper.set(Orders::getStatus,orders.getStatus());

        orderService.update(updateWrapper);

        return R.success("修改状态成功");
    }
}
