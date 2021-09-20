package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.service.ProductSaveService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zero
 * @create 2020-09-16 23:53
 */
@Controller
public class SearchController {

    @Resource
    MallSearchService mallSearchService;

    @Resource
    ProductSaveService productSaveService;


    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) throws IOException {
         searchParam.set_querySearch(request.getQueryString());
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }

    @GetMapping("/list1.html")
    public String listPageTest(SearchParam searchParam, Model model, HttpServletRequest request) throws IOException {
         searchParam.set_querySearch(request.getQueryString());
        SearchResult result = productSaveService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }


}
