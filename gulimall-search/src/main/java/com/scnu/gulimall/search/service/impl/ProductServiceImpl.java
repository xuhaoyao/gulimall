package com.scnu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.scnu.common.to.es.SkuEsModel;
import com.scnu.gulimall.search.config.ElasticSearchConfig;
import com.scnu.gulimall.search.constant.ProductConstant;
import com.scnu.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean productUp(List<SkuEsModel> upProducts) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel upProduct : upProducts) {
            String s = JSON.toJSONString(upProduct);
            IndexRequest indexRequest = new IndexRequest(ProductConstant.INDEX_NAME);
            indexRequest.id(upProduct.getSkuId().toString());  //设置在Elasticsearch中的Id
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        BulkItemResponse[] items = bulk.getItems();
        List<String> collect = Arrays.stream(items).map(item -> item.getId()).collect(Collectors.toList());
        log.info("商品上架完成:{}",collect);
        //TODO 如果批量保存有失败情况？
        return bulk.hasFailures();
    }
}
