package com.shawn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.R;
import com.shawn.reggie.dto.DishDto;
import com.shawn.reggie.entity.Category;
import com.shawn.reggie.entity.Dish;
import com.shawn.reggie.entity.DishFlavor;
import com.shawn.reggie.service.CategoryService;
import com.shawn.reggie.service.DishFlavorService;
import com.shawn.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Resource
    private DishService dishService;

    @Resource
    private DishFlavorService dishFlavorService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 保存新增的菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R <String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        //修改菜品信息为保证缓存和数据库一致，需要清空对应分类下的redis缓存信息
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("菜品添加成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R <Page> page(int page, int pageSize, String name) {

        //构造分页构造器对象
        Page <Dish> pageInfo = new Page <>(page, pageSize);
        Page <DishDto> dishDtoPage = new Page <>();

        //条件构造器
        LambdaQueryWrapper <Dish> queryWrapper = new LambdaQueryWrapper <>();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List <Dish> records = pageInfo.getRecords();

        List <DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据Restful路径中的id查找到DTO对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get (@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update (@RequestBody DishDto dishDto){
        dishService.updateDishWithFlavor(dishDto);
        //修改菜品信息为保证缓存和数据库一致，需要清空对应分类下的redis缓存信息
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("菜品添加成功");
    }

    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        System.out.println(dish.getCategoryId());
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper <>();

        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId ,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List <Dish> dishes = dishService.list(queryWrapper);
        return R.success(dishes);
    }*/

    /**
     * 重构list方法，需要用于前台显示菜品数据口味使用
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        List<DishDto> dishDtos = null;

        //生成一个对应类型的动态key值
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        //优先查看Redis中是否存在菜品数据
        dishDtos = (List <DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtos != null){
            return R.success(dishDtos);
        }

        //找到目前可售的对应分类的菜品信息
        //select * from dish where status = 1 and categoryId = ?
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(dish.getCategoryId()!= null,
                Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        List <Dish> dishes = dishService.list(queryWrapper);

        //由于需要展示口味信息，创建一个流去构造一个dishDto
        dishDtos = dishes.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null){
                dishDto.setCategoryName(category.getName());
            }

            //当前菜品的id
            //select * from dish_flavor where dishId = ?
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper <>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);

            List <DishFlavor> list = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(list);
            return dishDto;
        }).collect(Collectors.toList());

        //将查询到的菜品数据加入Redis缓存
        redisTemplate.opsForValue().set(key,dishDtos,60, TimeUnit.MINUTES);

        return R.success(dishDtos);
    }

    /**
     * 修改菜品售卖状态
     * 注意如果菜品状态为停售，那么所对套餐也应更改为停售
     * @param ids
     * @param type
     * @return
     */
    @PostMapping("/status/{type}")
    public R<String > status (@RequestParam List<Long> ids,@PathVariable Integer type){
        dishService.modifyStatus(ids,type);
        return R.success("修改菜品状态成功");
    }

    /**
     * 删除菜品，如果菜品关联，则无法删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R <String> delete (@RequestParam List<Long> ids){
        dishService.deleteDish(ids);
        return R.success("菜品删除成功");
    }

}
