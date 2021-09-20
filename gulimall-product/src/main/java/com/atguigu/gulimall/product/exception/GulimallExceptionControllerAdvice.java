package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zero
 * @create 2020-07-27 22:24
 *
 * 统一异常处理
 *
 */
@Slf4j

//@ResponseBody
//@ControllerAdvice

@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")

public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.info("数据校验出现问题{}，异常类型{}",e.getMessage(),e.getClass());

        BindingResult bindingResult = e.getBindingResult();

        Map<String,String> errorMap = new HashMap<>();

        bindingResult.getFieldErrors().forEach(errorItem -> {
            errorMap.put(errorItem.getField(),errorItem.getDefaultMessage());
        });

//        return R.error(400,"数据校验异常").put("data",errorMap);
        return  R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(), BizCodeEnume.VAILD_EXCEPTION.getMag()).put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMag());
    }


}
