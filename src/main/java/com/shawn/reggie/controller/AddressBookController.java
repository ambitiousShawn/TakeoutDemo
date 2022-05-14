package com.shawn.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shawn.reggie.common.BaseContext;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.AddressBook;
import com.shawn.reggie.service.AddressBookService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Resource
    private AddressBookService addressBookService;

    @PostMapping
    public R <AddressBook> save (@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<AddressBook> setDefault (@RequestBody AddressBook addressBook){
        //先把传入的对应用户ID对应所有地址信息都设置为非默认
        LambdaUpdateWrapper<AddressBook> lambdaUpdateWrapper = new LambdaUpdateWrapper <>();
        lambdaUpdateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        lambdaUpdateWrapper.set(AddressBook::getIsDefault,0);

        addressBookService.update(lambdaUpdateWrapper);

        addressBook.setIsDefault(1);

        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    @GetMapping("/default")
    public R<AddressBook> getDefault (){
        //select * from address_book where userId = ? and default = 1
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,userId);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(queryWrapper);

        addressBookService.updateById(one);
        return R.success(one);
    }

    @GetMapping("/list")
    public R<List <AddressBook>> list(AddressBook addressBook){
        //获取当前用户的ID
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(addressBook.getUserId()!= null,AddressBook::getUserId,userId);
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List <AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 根据ID查找一个Address信息，用于编辑信息的数据回显
     */
    @GetMapping("/{id}")
    public R<AddressBook> findById(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        return R.success(addressBook);
    }

    @PutMapping
    public R <String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("修改信息成功");
    }

    @DeleteMapping
    public R<String> delete (@RequestParam Long ids){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper <>();
        queryWrapper.eq(AddressBook::getId,ids);

        addressBookService.remove(queryWrapper);
        return R.success("删除成功");
    }
}
