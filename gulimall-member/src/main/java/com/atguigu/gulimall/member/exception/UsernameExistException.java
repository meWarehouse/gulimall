package com.atguigu.gulimall.member.exception;

/**
 * @author zero
 * @create 2020-09-23 17:06
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已存在");
    }

    public UsernameExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
