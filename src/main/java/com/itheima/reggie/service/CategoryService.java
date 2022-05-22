package com.itheima.reggie.service;

import com.itheima.reggie.pojo.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 */
public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
