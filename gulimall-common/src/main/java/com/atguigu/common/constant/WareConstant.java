package com.atguigu.common.constant;

/**
 * @author zero
 * @create 2020-08-29 23:00
 */
public class WareConstant {

    public enum PurchaseStatusEnum{

        CREATE(0,"新建"),
        ASSIGNED(1,"分配"),
        RECEIVE(2,"已领取"),
        FINISH(3,"已完成"),
        HASERRROR(4,"有异常");

        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }


    }

    public enum PurchaseDetailEnum{

        CREATED(0,"新建"),
        ASSIGNED(1,"已分配"),
        BUYING(2,"正在采购"),
        FINISH(3,"已完成"),
        HASERRROR(4,"采购失败");

        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }


        PurchaseDetailEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

}
