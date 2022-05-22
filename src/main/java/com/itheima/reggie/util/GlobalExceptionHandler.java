package com.itheima.reggie.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 *
 */
//给Controller控制器添加统一的操作或处理
@Slf4j
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    /**
     * 进行异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exdceptionHanlder(SQLIntegrityConstraintViolationException ex){

        log.error(ex.getMessage());

        //contains：包含
        if (ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            //简单定义：String msg = "用户已存在"
            String msg =  split[2] + "用户已存在";
            return R.error(msg);
        }
        return  R.error("未知错误");
    }

    /**
     * 进行异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exdceptionHanlder(CustomException ex){
        log.error(ex.getMessage());

        return  R.error(ex.getMessage());
    }
}
