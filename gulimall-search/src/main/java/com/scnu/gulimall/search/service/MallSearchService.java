package com.scnu.gulimall.search.service;

import com.scnu.gulimall.search.vo.SearchParam;
import com.scnu.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     * 根据商城搜索条件返回对应数据
     * @param searchParam
     * @return
     */
    SearchResult search(SearchParam searchParam);

}
