package com.atguigu.gulimall.product.vo.skuitem;

import com.atguigu.gulimall.product.vo.spu.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zero
 * @create 2020-09-21 21:21
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
