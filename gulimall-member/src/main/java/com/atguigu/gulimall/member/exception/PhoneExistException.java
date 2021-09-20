package com.atguigu.gulimall.member.exception;

/**
 * @author zero
 * @create 2020-09-23 17:06
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("phone已存在");
    }

    public PhoneExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
