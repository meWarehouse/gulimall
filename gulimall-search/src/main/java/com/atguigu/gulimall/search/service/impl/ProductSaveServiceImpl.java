package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zero
 * @create 2020-09-10 0:18
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    RestHighLevelClient client;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        if (skuEsModels == null && skuEsModels.size() <= 0) {
            return true;
        }

        //保存到es
        //1.给es中创建索引：product 建立好映射关系

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);

            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        //BulkRequest bulkRequest, RequestOptions options
        BulkResponse bulk = client.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //TODO 如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        log.info("商品上架完成：{}，返回数据 {}", collect, bulk.toString());

        return !b;


    }

    @Override
    public SearchResult search(SearchParam param) {

        //封装数据
        SearchRequest searchRequest = getSearchRequest(param);
        SearchResult result = null;
        try {
            //发送请求得到响应
            SearchResponse search = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            //分析响应数据
            result = getResult(search,param);


        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }

    private SearchResult getResult(SearchResponse search, SearchParam param) {

        SearchResult result = new SearchResult();

        SearchHits hits = search.getHits();

        //构建所有商品 SkuEsModel
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                SkuEsModel esModel = JSON.parseObject(hit.getSourceAsString(), SkuEsModel.class);
                skuEsModels.add(esModel);
            }
        }
        result.setProducts(skuEsModels);

        //分页信息
        result.setPageNum(param.getPageNum());
        result.setTotal(hits.getTotalHits().value);
        result.setTotalPage((int)hits.getTotalHits().value%EsConstant.PRODUCT_PAGE_SIZE == 0?(int)hits.getTotalHits().value/EsConstant.PRODUCT_PAGE_SIZE:((int)hits.getTotalHits().value/EsConstant.PRODUCT_PAGE_SIZE+1));


        //======================聚合=======================
        Aggregations aggregations = search.getAggregations();
        //品牌
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> brand_aggBuckets = ((ParsedLongTerms) aggregations.get("brand_agg")).getBuckets();
        if(brand_aggBuckets != null && brand_aggBuckets.size() > 0){
            for (Terms.Bucket bucket : brand_aggBuckets) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

                brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
                List<? extends Terms.Bucket> brand_name_aggBuckets = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets();
                if(brand_name_aggBuckets != null && brand_name_aggBuckets.size() > 0){
                    brandVo.setBrandName(brand_name_aggBuckets.get(0).getKeyAsString());
                }
                List<? extends Terms.Bucket> brand_img_aggBuckets = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets();
                if(brand_img_aggBuckets != null && brand_img_aggBuckets.size()>0){
                    brandVo.setBrandImg(brand_img_aggBuckets.get(0).getKeyAsString());
                }

                brandVos.add(brandVo);
            }
        }
        result.setBrands(brandVos);

        //分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        if(catalog_agg.getBuckets() != null && catalog_agg.getBuckets().size() > 0){
            for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                catalogVo.setCatalogId(bucket.getKeyAsNumber().longValue());
                List<? extends Terms.Bucket> catalog_name_aggBuckets = ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets();
                if(catalog_name_aggBuckets != null && catalog_name_aggBuckets.size() > 0){
                    catalogVo.setCatalogName(catalog_name_aggBuckets.get(0).getKeyAsString());
                }
                catalogVos.add(catalogVo);
            }
        }
        result.setCatalogs(catalogVos);

        //属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        List<? extends Terms.Bucket> attr_id_aggBuckets = ((ParsedLongTerms) ((ParsedNested) aggregations.get("attr_agg")).getAggregations().get("attr_id_agg")).getBuckets();
        if(attr_id_aggBuckets != null && attr_id_aggBuckets.size() > 0){
            for (Terms.Bucket bucket : attr_id_aggBuckets) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

                attrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                List<? extends Terms.Bucket> attr_name_aggBuckets = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets();
                if(attr_name_aggBuckets != null && attr_name_aggBuckets.size() > 0){
                    attrVo.setAttrName(attr_name_aggBuckets.get(0).getKeyAsString());
                }
                List<? extends Terms.Bucket> attr_value_aggBuckets = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets();
                if(attr_value_aggBuckets != null && attr_value_aggBuckets.size() > 0){
                    attrVo.setAttrValue(attr_value_aggBuckets.stream().map(item->item.getKeyAsString()).collect(Collectors.toList()));
                }
                attrVos.add(attrVo);
            }
        }

        result.setAttrs(attrVos);

        return result;
    }

    private SearchRequest getSearchRequest(SearchParam param) {

        //构建 查询条件
        SearchSourceBuilder searchBuilder = new SearchSourceBuilder();

        //1.query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1.1：must
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        //1.2：filter

        if(param.getCatalog3Id() != null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        //attrs
        if(param.getAttrs() != null && param.getAttrs().size() > 0){

            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedQuery = QueryBuilders.boolQuery();
                //attrs=1_3G:4G:5G&attr2=2_骁龙845&attrs=4_高清屏  attrs=属性id_属性值1:属性值2...
                String[] s = attr.split("_");

                nestedQuery.must(QueryBuilders.termsQuery("attrs.attrId",s[0]));
                nestedQuery.must(QueryBuilders.termsQuery("attrs.attrValue", s[1].split(":")));

                boolQuery.filter(QueryBuilders.nestedQuery("attrs",nestedQuery,ScoreMode.None));

            }
        }

        if(param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));
        }

        if(!StringUtils.isEmpty(param.getSkuPrice())){
            ////skuPrice=1_500(1到500) _500(500以内) 500_(500以外)
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2){
                rangeQuery.gt(s[0]).to(s[1]);

            }else if(s.length ==1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.to(s[0]);
                }else if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.from(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        searchBuilder.query(boolQuery);

        //2.sort
        if(!StringUtils.isEmpty(param.getSort())){
            //sort=hostScore_asc/desc

            String[] s = param.getSort().split("_");
            searchBuilder.sort(s[0],s[1].equalsIgnoreCase("desc")? SortOrder.DESC:SortOrder.ASC);

        }

        //3.from size highlight
        searchBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.postTags("</b>");
            builder.preTags("<b style='color:red'>");
            searchBuilder.highlighter(builder);
        }

        //4.agg
        // brand_agg
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId");
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchBuilder.aggregation(brand_agg);


        // catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId")
                .subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catagoryName"));
        searchBuilder.aggregation(catalog_agg);

        // attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue"));
        attr_agg.subAggregation(attr_id_agg);
        searchBuilder.aggregation(attr_agg);


        System.out.println("dsl语句："+searchBuilder.toString());

        //获取 SearchRequest
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchBuilder);


        return searchRequest;
    }
}
