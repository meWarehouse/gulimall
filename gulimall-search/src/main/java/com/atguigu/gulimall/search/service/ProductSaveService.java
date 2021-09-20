package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

import java.io.IOException;
import java.util.List;

/**
 * @author zero
 * @create 2020-09-10 0:15
 */
public interface ProductSaveService {

    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;

    SearchResult search(SearchParam searchParam);
}
