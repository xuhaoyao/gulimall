package com.scnu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scnu.common.to.es.SkuEsModel;
import com.scnu.common.utils.R;
import com.scnu.gulimall.search.config.ElasticSearchConfig;
import com.scnu.gulimall.search.constant.ProductConstant;
import com.scnu.gulimall.search.feign.ProductFeignService;
import com.scnu.gulimall.search.service.MallSearchService;
import com.scnu.gulimall.search.vo.AttrRespVo;
import com.scnu.gulimall.search.vo.SearchParam;
import com.scnu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        //1.构造请求条件
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult searchResponse = null;

        try {
            //2.得到响应
            SearchResponse result = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //3.封装结果
            searchResponse = buildSearchResponse(result,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponse;
    }

    /**
     * 根据从Elasticsearch的返回结果封装成页面指定的模型
     * @param response
     * @return
     */
    private SearchResult buildSearchResponse(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();

        //返回的所有商品信息 hits中
        List<SkuEsModel> products = new ArrayList<>();
        SearchHits hits = response.getHits();
        if(!ObjectUtils.isEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsJson = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsJson, SkuEsModel.class);

                //高亮显示用户搜索的内容
                if(StringUtils.hasLength(param.getKeyword())){
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].toString();
                    skuEsModel.setSkuTitle(skuTitle);
                }

                products.add(skuEsModel);
            }
        }
        result.setProducts(products);

        //1.分类的聚合信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //bucket中key就是分类的id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //分类id的子聚合中有分类的名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            for (Terms.Bucket catalog_name_aggBucket : catalog_name_agg.getBuckets()) {
                //buck中key就是分类的名字 一个id对应一个名字,循环一次就可以break退出
                catalogVo.setCatalogName(catalog_name_aggBucket.getKeyAsString());
                break;
            }
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //2.品牌的聚合信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //品牌的id就在brand_agg的bucked的key中
            brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));

            //品牌的图片在brand_agg的子聚合中,子聚合包装在了bucket
            ParsedStringTerms brand_image_agg = bucket.getAggregations().get("brand_image_agg");
            brandVo.setBrandImg(brand_image_agg.getBuckets().get(0).getKeyAsString());

            //品牌的名字在brand_agg的子聚合中,子聚合包装在了bucket
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //3.属性的聚合信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        //id的聚合中包含了name和value
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1.属性id
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //2.属性name
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //3.属性value
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            attrVo.setAttrValue(attr_value_agg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList()));
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);


        //总记录数
        Long total = hits.getTotalHits().value;
        result.setTotal(total);
        //当前页码
        result.setPageNum(param.getPageNum());
        //总页码
        result.setTotalPages((int) (total % ProductConstant.PRUDUCT_PAGESIZE == 0 ?
                total / ProductConstant.PRUDUCT_PAGESIZE : total / ProductConstant.PRUDUCT_PAGESIZE + 1));


        //封装面包屑数据
        if(!ObjectUtils.isEmpty(param.getAttrs())){
            List<SearchResult.NavVo> navs = param.getAttrs().stream().map(attr -> {
                //attrs=1_安卓:小米
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);

                R info = productFeignService.info(Long.parseLong(s[0]));
                if(info.getCode() == 0) {
                    AttrRespVo attrRespVo = info.getData("attr", new TypeReference<AttrRespVo>() {
                    });
                    navVo.setNavName(attrRespVo.getAttrName());
                }
                else{
                    navVo.setNavName(s[0]);
                }

                try {
                    String encode = URLEncoder.encode(attr, "UTF-8");
                    /**
                     * 浏览器对空格的编码和java不一样
                     * 对于空格
                     *  java编码成+
                     *  浏览器编码成%20
                     */
                    encode = encode.replace("+","%20");
                    String queryParam = param.getQueryParameter();
                    queryParam = queryParam.replace("attrs=" + encode,"");
                    //删除之后可能的情况:url?a=xxx&&attrs=xxx
                    queryParam = queryParam.replace("&&","&");
                    //删除之后可能的情况:url?a=xxx&
                    if(queryParam.endsWith("&")){
                        queryParam = queryParam.substring(0,queryParam.length() - 1);
                    }
                    //删除之后可能的情况: url?&a=xxx
                    if(queryParam.startsWith("&")){
                        queryParam = queryParam.substring(1);
                    }
                    String link = "http://search.gulimall.com/search.html?" + queryParam;
                    //若后面不跟参数,返回的时候不带?
                    if(link.endsWith("?")){
                        link = link.substring(0,link.length() - 1);
                    }
                    navVo.setLink(link);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navs);
        }

        return result;
    }

    /**
     * 构造发送给Elasticsearch的请求条件
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//构造DQL语句

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if(StringUtils.hasLength(param.getKeyword())){
            //1.must 全文检索关键字
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        if(param.getCatalog3Id() != null){
            //2.filter term 按照三级分类id
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        if(!ObjectUtils.isEmpty(param.getBrandId())){
            //3.filter terms 按照品牌id 可多选
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        //4.filter nested 按照属性
        if(!ObjectUtils.isEmpty(param.getAttrs())){
            /**
             *      *  attrs=1_安卓:小米
             *      *  attrs=2_4G:8G
             */
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestBoolQuery = new BoolQueryBuilder();
                //attrs=1_安卓:小米
                String[] s = attr.split("_");
                String attrId = s[0];   //检索的属性Id
                String[] attrValues = s[1].split(":"); //检索的属性值
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //每一个属性查询都必须生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        //filter term 是否有库存
        if(param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //5.filter 按照价格区间
        /**
         *      * 约定:
         *      *  skuPrice=1_500 1-500之间
         *      *  skuPrice=_500 小于等于500
         *      *  skuPrice=500_ 大于等于500
         */
        if(StringUtils.hasLength(param.getSkuPrice())){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            // 10000_ --> s.length = 1
            // _10000 或 10000_20000 --> s.length = 2
            if (!"".equals(s[0])) {
                rangeQueryBuilder.gte(s[0]);
            }
            if(s.length == 2) {
                if (!"".equals(s[1])) {
                    rangeQueryBuilder.lte(s[1]);
                }
            }
            //System.out.println("rangeQueryBuilder--->" + rangeQueryBuilder.toString());

            boolQuery.filter(rangeQueryBuilder);
        }

        //封装好查询条件
        sourceBuilder.query(boolQuery);


        /**
         * 排序:
         * sort=saleCount_asc/desc
         * sort=skuPrice_asc/desc
         * sort=hotScore_asc/desc
         */
        if(StringUtils.hasLength(param.getSort())){
            String[] s = param.getSort().split("_");
            //System.out.println(Arrays.toString(s));
            sourceBuilder.sort(s[0],"desc".equals(s[1]) ? SortOrder.DESC : SortOrder.ASC);
        }

        //分页
        if(param.getPageNum() != null){
            sourceBuilder.from((param.getPageNum() - 1) * ProductConstant.PRUDUCT_PAGESIZE);
        }
        else{
            sourceBuilder.from(0);
        }
        sourceBuilder.size(ProductConstant.PRUDUCT_PAGESIZE);

        //高亮
        if(StringUtils.hasLength(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 聚合分析
         */
        //1.品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_image_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //2.分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //3.属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(5));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(5));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        //System.out.println("DSL构建语句--->" + sourceBuilder.toString());

        SearchRequest searchRequest =new SearchRequest(ProductConstant.INDEX_NAME);
        searchRequest.source(sourceBuilder);

        return searchRequest;

    }
}
