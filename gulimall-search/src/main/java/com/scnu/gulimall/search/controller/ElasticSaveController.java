package com.scnu.gulimall.search.controller;

import com.scnu.common.exception.ErrorCode;
import com.scnu.common.to.es.SkuEsModel;
import com.scnu.common.utils.R;
import com.scnu.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {

    @Autowired
    private ProductService productService;

    @PostMapping("/product")
    public R productUp(@RequestBody List<SkuEsModel> upProducts){
        try {
            Boolean flag = productService.productUp(upProducts);
            if(!flag){
                log.info("保存数据成功");
                return R.ok();
            }
            else{
                log.error("hasFailures,保存数据失败");
                return R.error(ErrorCode.PRODUCT_UP_ERROR.getCode(),ErrorCode.PRODUCT_UP_ERROR.getMsg());
            }
        } catch (IOException e) {
            log.error("ElasticSaveController上架商品出错:{}",e.getMessage());
            e.printStackTrace();
            return R.error(ErrorCode.PRODUCT_UP_ERROR.getCode(),ErrorCode.PRODUCT_UP_ERROR.getMsg());
        }
    }

}
