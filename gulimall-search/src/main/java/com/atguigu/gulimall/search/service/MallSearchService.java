package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

import java.io.IOException;

/**
 * @author zero
 * @create 2020-09-17 20:08
 */
public interface MallSearchService {

    SearchResult search(SearchParam searchParam) throws IOException;
}
