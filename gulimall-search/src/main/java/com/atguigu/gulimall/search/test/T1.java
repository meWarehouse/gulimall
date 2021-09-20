package com.atguigu.gulimall.search.test;

import javax.jnlp.IntegrationService;

/**
 * @author zero
 * @create 2020-09-21 7:57
 */
public class T1 {
    public static void main(String[] args) {
       /* double a = 2.0;
        double b = 2.0;
        Double c = 2.0;
        Double d = 2.0;
        System.out.println(a == b);
        System.out.println(c == d);
        System.out.println(a == d);*/

        Integer a = new Integer(1);
        Integer b = new Integer(1);
        System.out.println(a == b);

        Integer i = 1;
        Integer i1 = 1;
        System.out.println(i == i1);

        Double d = 2.0;
        Double d1 = 2.0;
        System.out.println(d==d1);

        Long l1 = 1L;
        Long l2 = 2L;
        System.out.println(l1 == l2);


    }

}
