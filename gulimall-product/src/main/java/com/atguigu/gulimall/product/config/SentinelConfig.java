package com.atguigu.gulimall.product.config;

//import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
//import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zero
 * @create 2020-10-16 19:10
 */
@Configuration
public class SentinelConfig {

//    public SentinelConfig(){
//        WebCallbackManager.setUrlBlockHandler(new UrlBlockHandler() {
//            @Override
//            public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
//                R error = R.error(BizCodeEnume.TOO_MACH_REQUEST.getCode(), BizCodeEnume.TOO_MACH_REQUEST.getMag());
//
//                httpServletRequest.setCharacterEncoding("UTF-8");
//                httpServletResponse.setCharacterEncoding("UTF-8");
//                httpServletResponse.setContentType("application/json");
//
//
//
//                httpServletResponse.getWriter().write(JSON.toJSONString(error));
//            }
//        });
//    }

}
