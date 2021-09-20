package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * 监听支付宝支付成功的回调
 *
 * @author zero
 * @create 2020-10-13 12:54
 */
@Slf4j
@RestController
public class OrderPayedController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @PostMapping("/alipay/notify")
    public String AliPayed(PayAsyncVo payAsyncVo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {

       /* Map<String, String[]> map = request.getParameterMap();
        for (String s : map.keySet()) {
            String parameter = request.getParameter(s);
            System.out.println(s+ " = " + parameter);
        }*/

       //==》必须验签
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayTemplate.alipay_public_key, AlipayTemplate.charset, AlipayTemplate.sign_type); //调用SDK验证签名

        if(signVerified){
            System.out.println("签名验证成功............");
            String result = orderService.handlePayResult(payAsyncVo);
            return "success";
        }else{
            System.out.println("签名验证失败................");
            return "error";
        }


    }

}
