package com.atguigu.gulimall.order.exception;

/**
 * @author zero
 * @create 2020-10-09 22:41
 */
public class NoStockException extends RuntimeException {
    private Long skuId;
    private String msg;
    public NoStockException(Long skuId){
        super("商品id:" + skuId +"没有足够的库存了");
    }
    public NoStockException(String msg){
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
