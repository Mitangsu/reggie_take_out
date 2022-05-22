package com.itheima.reggie.util;

/**
 * 基于ThreadLocal封装工具类,用户保存和获取当前登录用户.
 * @author Su
 * @create 2022-05-19 8:23
 */
public class BaseContext {
    private static  ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }


}
