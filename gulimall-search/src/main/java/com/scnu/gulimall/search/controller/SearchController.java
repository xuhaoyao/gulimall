package com.scnu.gulimall.search.controller;

import com.scnu.gulimall.search.service.MallSearchService;
import com.scnu.gulimall.search.vo.SearchParam;
import com.scnu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping({"/search.html","/list.html"})
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        //获取请求参数,面包屑用
        searchParam.setQueryParameter(request.getQueryString());
        SearchResult search = mallSearchService.search(searchParam);
        //System.out.println("前端返回结果:--->" + search);
        model.addAttribute("result",search);
        return "list";
    }

}
