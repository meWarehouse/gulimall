package com.atguigu.gulimall.seckill.config;

//import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
//import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
//import com.alibaba.csp.sentinel.slots.block.BlockException;
//import com.alibaba.fastjson.JSON;
//import com.atguigu.common.exception.BizCodeEnume;
//import com.atguigu.common.utils.R;
//import org.springframework.context.annotation.Configuration;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;

import org.springframework.context.annotation.Configuration;

/**
 * @author zero
 * @create 2020-10-16 23:07
 */
@Configuration
public class SeckillSentinelConfig {


//    public SeckillSentinelConfig() {
//
//        WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
//            @Override
//            public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
//                httpServletRequest.setCharacterEncoding("utf-8");
//                httpServletResponse.setCharacterEncoding("utf-8");
//                R error = R.error(BizCodeEnume.TOO_MACH_REQUEST.getCode(), BizCodeEnume.TOO_MACH_REQUEST.getMag());
//                httpServletResponse.setContentType("application/json");
//                httpServletResponse.getWriter().write(JSON.toJSONString(error));
//            }
//        });
//
//    }
}
