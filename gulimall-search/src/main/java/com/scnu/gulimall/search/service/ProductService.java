package com.scnu.gulimall.search.service;

import com.scnu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    Boolean productUp(List<SkuEsModel> upProducts) throws IOException;
}
