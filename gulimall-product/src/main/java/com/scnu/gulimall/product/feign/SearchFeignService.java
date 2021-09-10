package com.scnu.gulimall.product.feign;

import com.scnu.common.to.es.SkuEsModel;
import com.scnu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productUp(@RequestBody List<SkuEsModel> upProducts);

}
