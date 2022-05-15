package com.shawn.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shawn.reggie.common.R;
import com.shawn.reggie.dto.SetmealDto;
import com.shawn.reggie.entity.Category;
import com.shawn.reggie.entity.Setmeal;
import com.shawn.reggie.service.CategoryService;
import com.shawn.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Resource
    private SetmealService setmealService;

    @Resource
    private CategoryService categoryService;

    /**
     * 保存加入的数据
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R <String> save (@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增数据成功");
    }

    @GetMapping("/page")
    public R <Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page <Setmeal> pageInfo = new Page <>(page, pageSize);
        Page <SetmealDto> dtoPage = new Page <>();

        LambdaQueryWrapper <Setmeal> queryWrapper = new LambdaQueryWrapper <>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null, Setmeal::getName, name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List <Setmeal> records = pageInfo.getRecords();

        List <SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);

        return R.success("删除数据成功");
    }

    @PostMapping("/status/{type}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> status (@RequestParam List<Long> ids,@PathVariable Integer type){
        setmealService.modifyStatus(ids,type);
        return R.success("修改状态成功");
    }

    /*@GetMapping("/list")
    public R<List<Setmeal>> list (Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,1);
        List <Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }*/
    /**
     * 根据条件查询套餐数据
     * Cacheable注解会优先使方法在setmealCache缓存中查找数据，如果找到，则直接返回数据
     * 如果未找到，那么再执行下述程序。
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R <List <Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper <Setmeal> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List <Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
}
