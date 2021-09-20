package com.atguigu.gulimall.order.exception;

/**
 * @author zero
 * @create 2020-10-09 20:57
 */
public class CompareException extends RuntimeException {
    public CompareException() {
        super("价格有误");
    }
}
