package com.atguigu.gulimall.search.test;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author zero
 * @create 2020-09-23 17:47
 */
public class Md5Test {
    public static void main(String[] args) {

        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);

    }
}
