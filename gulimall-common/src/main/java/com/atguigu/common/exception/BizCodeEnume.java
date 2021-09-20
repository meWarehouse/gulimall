package com.atguigu.common.exception;

/**
 * @author zero
 * @create 2020-07-27 22:42
 */
public enum BizCodeEnume {

    /**
     *
     * 错误码列表
     * 10:通用
     *     001:参数格式校验
     *     002:短信验证码频率太高
     * 11:商品
     * 12:订单
     * 13:购物侧
     * 14:物流
     * 15:用户
     * 21:库存
     *
     */

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    TOO_MACH_REQUEST(10002,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，稍后再试"),
    USERNAME_EXIST_EXCEPTION(15001,"用户存在异常"),
    PHONE_EXIST_EXCEPTION(15003,"电话号存在异常"),
    NO_STOCK_EXCEPTION(210000,"没有库存"),
    ACCOUT_PASSEORD_EXCEPTION(15004,"用户名密码错误");

    private int code;
    private String mag;

    BizCodeEnume(int code, String mag) {
        this.code = code;
        this.mag = mag;
    }

    public int getCode() {
        return code;
    }

    public String getMag() {
        return mag;
    }
}
