package com.shawn.reggie.dto;

import com.shawn.reggie.entity.Setmeal;
import com.shawn.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List <SetmealDish> setmealDishes;

    private String categoryName;

    public List <SetmealDish> getSetmealDishes() {
        return setmealDishes;
    }

    public void setSetmealDishes(List <SetmealDish> setmealDishes) {
        this.setmealDishes = setmealDishes;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
