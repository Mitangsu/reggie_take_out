package com.itheima.reggie.util;

/**
 * 自定义业务异常
 * @author Su
 * @create 2022-05-19 9:37
 */
public class CustomException extends RuntimeException{

    public CustomException(String message){
        super(message);
    }
}
