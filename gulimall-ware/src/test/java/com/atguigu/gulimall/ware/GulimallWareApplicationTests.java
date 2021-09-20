package com.atguigu.gulimall.ware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GulimallWareApplicationTests {

    @Test
    void contextLoads() {
        BigDecimal divide = new BigDecimal("10").divide(new BigDecimal("3"),2,BigDecimal.ROUND_CEILING);
        System.out.println(divide);
    }

}
