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

        //1.??????????????????
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult searchResponse = null;

        try {
            //2.????????????
            SearchResponse result = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //3.????????????
            searchResponse = buildSearchResponse(result,param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponse;
    }

    /**
     * ?????????Elasticsearch?????????????????????????????????????????????
     * @param response
     * @return
     */
    private SearchResult buildSearchResponse(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();

        //??????????????????????????? hits???
        List<SkuEsModel> products = new ArrayList<>();
        SearchHits hits = response.getHits();
        if(!ObjectUtils.isEmpty(hits.getHits())) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsJson = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsJson, SkuEsModel.class);

                //?????????????????????????????????
                if(StringUtils.hasLength(param.getKeyword())){
                    HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                    String skuTitle = highlightField.getFragments()[0].toString();
                    skuEsModel.setSkuTitle(skuTitle);
                }

                products.add(skuEsModel);
            }
        }
        result.setProducts(products);

        //1.?????????????????????
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //bucket???key???????????????id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //??????id?????????????????????????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            for (Terms.Bucket catalog_name_aggBucket : catalog_name_agg.getBuckets()) {
                //buck???key????????????????????? ??????id??????????????????,?????????????????????break??????
                catalogVo.setCatalogName(catalog_name_aggBucket.getKeyAsString());
                break;
            }
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //2.?????????????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //?????????id??????brand_agg???bucked???key???
            brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));

            //??????????????????brand_agg???????????????,?????????????????????bucket
            ParsedStringTerms brand_image_agg = bucket.getAggregations().get("brand_image_agg");
            brandVo.setBrandImg(brand_image_agg.getBuckets().get(0).getKeyAsString());

            //??????????????????brand_agg???????????????,?????????????????????bucket
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //3.?????????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        //id?????????????????????name???value
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1.??????id
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //2.??????name
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //3.??????value
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            attrVo.setAttrValue(attr_value_agg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList()));
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);


        //????????????
        Long total = hits.getTotalHits().value;
        result.setTotal(total);
        //????????????
        result.setPageNum(param.getPageNum());
        //?????????
        result.setTotalPages((int) (total % ProductConstant.PRUDUCT_PAGESIZE == 0 ?
                total / ProductConstant.PRUDUCT_PAGESIZE : total / ProductConstant.PRUDUCT_PAGESIZE + 1));


        //?????????????????????
        if(!ObjectUtils.isEmpty(param.getAttrs())){
            List<SearchResult.NavVo> navs = param.getAttrs().stream().map(attr -> {
                //attrs=1_??????:??????
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
                     * ??????????????????????????????java?????????
                     * ????????????
                     *  java?????????+
                     *  ??????????????????%20
                     */
                    encode = encode.replace("+","%20");
                    String queryParam = param.getQueryParameter();
                    queryParam = queryParam.replace("attrs=" + encode,"");
                    //???????????????????????????:url?a=xxx&&attrs=xxx
                    queryParam = queryParam.replace("&&","&");
                    //???????????????????????????:url?a=xxx&
                    if(queryParam.endsWith("&")){
                        queryParam = queryParam.substring(0,queryParam.length() - 1);
                    }
                    //???????????????????????????: url?&a=xxx
                    if(queryParam.startsWith("&")){
                        queryParam = queryParam.substring(1);
                    }
                    String link = "http://search.gulimall.com/search.html?" + queryParam;
                    //?????????????????????,??????????????????????
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
     * ???????????????Elasticsearch???????????????
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//??????DQL??????

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if(StringUtils.hasLength(param.getKeyword())){
            //1.must ?????????????????????
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        if(param.getCatalog3Id() != null){
            //2.filter term ??????????????????id
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        if(!ObjectUtils.isEmpty(param.getBrandId())){
            //3.filter terms ????????????id ?????????
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        //4.filter nested ????????????
        if(!ObjectUtils.isEmpty(param.getAttrs())){
            /**
             *      *  attrs=1_??????:??????
             *      *  attrs=2_4G:8G
             */
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestBoolQuery = new BoolQueryBuilder();
                //attrs=1_??????:??????
                String[] s = attr.split("_");
                String attrId = s[0];   //???????????????Id
                String[] attrValues = s[1].split(":"); //??????????????????
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //??????????????????????????????????????????nested??????
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        //filter term ???????????????
        if(param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //5.filter ??????????????????
        /**
         *      * ??????:
         *      *  skuPrice=1_500 1-500??????
         *      *  skuPrice=_500 ????????????500
         *      *  skuPrice=500_ ????????????500
         */
        if(StringUtils.hasLength(param.getSkuPrice())){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            // 10000_ --> s.length = 1
            // _10000 ??? 10000_20000 --> s.length = 2
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

        //?????????????????????
        sourceBuilder.query(boolQuery);


        /**
         * ??????:
         * sort=saleCount_asc/desc
         * sort=skuPrice_asc/desc
         * sort=hotScore_asc/desc
         */
        if(StringUtils.hasLength(param.getSort())){
            String[] s = param.getSort().split("_");
            //System.out.println(Arrays.toString(s));
            sourceBuilder.sort(s[0],"desc".equals(s[1]) ? SortOrder.DESC : SortOrder.ASC);
        }

        //??????
        if(param.getPageNum() != null){
            sourceBuilder.from((param.getPageNum() - 1) * ProductConstant.PRUDUCT_PAGESIZE);
        }
        else{
            sourceBuilder.from(0);
        }
        sourceBuilder.size(ProductConstant.PRUDUCT_PAGESIZE);

        //??????
        if(StringUtils.hasLength(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * ????????????
         */
        //1.????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //??????????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_image_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //2.????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //3.????????????
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10);
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(5));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(5));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        //System.out.println("DSL????????????--->" + sourceBuilder.toString());

        SearchRequest searchRequest =new SearchRequest(ProductConstant.INDEX_NAME);
        searchRequest.source(sourceBuilder);

        return searchRequest;

    }
}
