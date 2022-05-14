package com.shawn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.BaseContext;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.OrderDetail;
import com.shawn.reggie.entity.Orders;
import com.shawn.reggie.entity.ShoppingCart;
import com.shawn.reggie.service.ShoppingCartService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;




    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add (@RequestBody ShoppingCart shoppingCart){
        //获取并设置用户ID
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //由于套餐不用选择口味，可以直接添加数量，而并非添加一条新的数据
        shoppingCart.setCreateTime(LocalDateTime.now());

        if (shoppingCart.getSetmealId() != null){
            //select * from shopping_cart where userId = ? and setmealId = ?
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper <>();
            queryWrapper.eq(ShoppingCart::getUserId,userId);
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            //update shopping_cart set numble = ? where userId = ? and setmealId = ?
            ShoppingCart one = shoppingCartService.getOne(queryWrapper);

            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper <>();
            updateWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            updateWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

            if (one != null){
                //获取到原来的数量
                Integer num = one.getNumber();
                updateWrapper.set(ShoppingCart::getNumber,num+1);
                shoppingCartService.update(updateWrapper);
                return R.success(shoppingCart);
            }
        }
        shoppingCartService.save(shoppingCart);

        return R.success(shoppingCart);
    }


    /**
     * 查看购物车数据
     * @return
     */
    @GetMapping("/list")
    public R <List<ShoppingCart>>  list (){
        //查询对应UserId的所有数据
        Long userId = BaseContext.getCurrentId();
        //select * from shopping_cart where userId = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List <ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean (){
        //拿到当前用户id，清除购物车表中所有相关数据即可
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }


    @PostMapping("/sub")
    public R <String> sub (@RequestBody ShoppingCart shoppingCart){
        //获取用户ID
        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCart.getDishId();
        if (dishId != null){
            //拿到的是菜品，直接删除菜品数据即可
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper <>();
            queryWrapper.eq(ShoppingCart::getUserId,userId);
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            shoppingCartService.remove(queryWrapper);
        }else {
            //拿到的是套餐，修改数量，判断数量为0时自动删除
            Long setmealId = shoppingCart.getSetmealId();
            LambdaUpdateWrapper<ShoppingCart> updateWrapper = new LambdaUpdateWrapper <>();
            updateWrapper.eq(ShoppingCart::getUserId,userId);
            updateWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            shoppingCart = shoppingCartService.getOne(updateWrapper);
            if (shoppingCart.getNumber() <= 1){
                shoppingCartService.remove(updateWrapper);
            }else{
                updateWrapper.set(ShoppingCart::getNumber,shoppingCart.getNumber()-1);
                shoppingCartService.update(updateWrapper);
            }
        }
        return R.success("减少数量成功");
    }


}
