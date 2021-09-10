package com.scnu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.scnu.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Data
    static class Account{
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html#java-rest-high-search-sync
     */
    @Test
    public void testSearchApi() throws IOException {
        //1.Creates the SearchRequest. Without arguments this runs against all indices.
        SearchRequest searchRequest = new SearchRequest("bank");
        //2.Most search parameters are added to the SearchSourceBuilder.
        // It offers setters for everything that goes into the search request body.
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //a.检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        //b.按照年龄的值分布进行聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        //c.计算平均薪资
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAgg").field("balance"));
        System.out.println(searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);

        //执行检索
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse.toString());

        //得到检索结果
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            String jsonBean = hit.getSourceAsString();
            Account account = JSON.parseObject(jsonBean,Account.class);
            System.out.println("account--->" + account);
        }

        //聚合分析
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println(keyAsString + "--->" + docCount);
        }

        Avg balanceAgg = aggregations.get("balanceAgg");
        System.out.println(balanceAgg.getName() + "--->" + balanceAgg.getValue());
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html
     */
    @Test
    public void testIndexApi() throws IOException {
        IndexRequest request = new IndexRequest("users");
        User user = new User();
        user.setAge(21);
        user.setGender("男");
        user.setName("VarerLeet");
        request.id("1")
                .source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse indexResponse = client.index(request, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }

}

@Data
class User{
    String name;
    String gender;
    Integer age;
}