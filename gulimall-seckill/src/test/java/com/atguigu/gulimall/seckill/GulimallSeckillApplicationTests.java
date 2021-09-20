package com.atguigu.gulimall.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GulimallSeckillApplicationTests {

    @Test
    void contextLoads() {

//        LocalDate now = LocalDate.now();
//        LocalDate d1 = now.plusDays(1);
//        LocalDate d2 = now.plusDays(2);
//
//        System.out.println(now);
//        System.out.println(d1);
//        System.out.println(d2);
//
//        LocalTime min = LocalTime.MIN;
//        LocalTime max = LocalTime.MAX;
//        System.out.println(min);
//        System.out.println(max);
//
//        // 2020-10-13 00:00   2020-10-15 23:59:59.999999999
//        LocalDateTime of = LocalDateTime.of(now, min);
//        LocalDateTime to = LocalDateTime.of(d2, max);
//        String start = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        String end = to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));


        LocalDate localDate = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
        String end = LocalDateTime.of(localDate, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        String start = LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println(start);
        System.out.println(end);



    }

}
