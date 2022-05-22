package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.pojo.Category;
import com.itheima.reggie.pojo.Dish;
import com.itheima.reggie.pojo.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * @author Su
 * @create 2022-05-19 15:18
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    DishService dishService;

    @Autowired
    DishFlavorService dishFlavorService;

    @Autowired
    CategoryService categoryService;


    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }


    /**
     * 批量停售功能
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> stopbuy(@PathVariable("status") Integer status,@RequestParam("ids") List<Long> ids){

        //先查询出数据
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        List<Dish> list = dishService.list(queryWrapper);

        //批量修改数据
        for(Dish dish:list){
                //修改状态数，前端已经帮住了
                dish.setStatus(status);
                //最后进行修改
                dishService.updateById(dish);
        }

        return R.success("停售成功");

    }


    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteBatch(@RequestParam("ids") List<Long> ids){

        dishService.removeByIds(ids);
        return R.success("批量删除成功");
    }



    /**
     * 新增菜品保存功能
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }



    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }


    /**
     * 分页查询
     * 里面还需要查询菜品类名，
     * 所以需要DishDto对象，里面设置categoryName就可以
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> queryPage(
            int page,
            int pageSize,
            String name
            ){
        //创造分页构造器对象
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> disDotPage = new Page(page,pageSize);


        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //模糊查询条件
        queryWrapper.like(!StringUtils.isEmpty(name),Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);


        //拷贝页面信息，排除records字段，因为要处理，重新赋值发给页面。
        BeanUtils.copyProperties(pageInfo,disDotPage,"records");

        //获取Dish页的数据,并且定义空的list
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list =records.stream().map((item)->{
            //1.获取菜品的分类id
            Long categoryId = item.getCategoryId();

            //2.根据id查询所有对象
            Category category = categoryService.getById(categoryId);

            //3.获取DishDto对象
            DishDto dishDto = new DishDto();

            if (category != null){
                //4.获取菜品分类的名字
                String categoryName = category.getName();

                //5.把菜品分类的名字set到new的dishDto里，
                //但是这里有个问题,这边只赋值了categoryName,别的值是空的，
                //这边就需要把item拷贝到new的dishDto里
                dishDto.setCategoryName(categoryName);
            }


            //拷贝到新的
            BeanUtils.copyProperties(item,dishDto);

            return  dishDto;

        }).collect(Collectors.toList());

        //重新赋值Records
        disDotPage.setRecords(list);

        return R.success(disDotPage);
    }





    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }







}

















