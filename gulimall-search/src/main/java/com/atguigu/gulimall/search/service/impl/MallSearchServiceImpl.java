package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrRespVo;
import com.atguigu.gulimall.search.vo.BrandInfoVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zero
 * @create 2020-09-17 20:09
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {


    @Resource
    RestHighLevelClient client;

    @Resource
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        /**
         * ????????????
         * ??????????????????????????????????????????????????????????????????
         * ??????
         * ??????
         * ??????
         * ??????
         */

        /**
         * 1.??????????????????
         * 2.????????????
         * 3.??????????????????
         */

        SearchResult result = null;

        //1.?????????????????? ??????????????????????????????DSL??????
        SearchRequest searchRequest = buildSearchRequest(param);

        //2.????????????
        try {
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            //3.???????????????????????????????????????
            result = buildSearchResult(response, param);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * ??????????????????
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        /**
         * 1.??????????????????
         * 2.??????????????????
         */


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * ??????  ?????????????????????????????????????????????????????????
         */

        //1?????????bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1.1???must????????????
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //1.2???bool - filter - ??????????????????id??????
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //1.2???bool - filter - ????????????id??????
        if (param.getBrandId() != null) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //1.2???bool - filter - ???????????????????????????????????????
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            //attrs=1_3G:4G:5G&attr2=2_??????845&attrs=4_?????????  attrs=??????id_?????????1:?????????2...
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] s = attrStr.split("_");
                String attrId = s[0];

                String[] attrValues = s[1].split(":");

                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                //?????????????????????????????????nestedQuery
                boolQuery.filter(QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None));

            }


        }

        //1.2???bool - filter - ?????????????????????????????????
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //1.2???bool - filter - ??????????????????????????????
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //skuPrice=1_500(1???500) _500(500??????) 500_(500??????)
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) { //1_500
                //??????
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) { // _500(500??????)
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) { //500_(500??????)
                    rangeQuery.gte(s[0]);
                }

            }

            boolQuery.filter(rangeQuery);

        }
        //???query????????????????????????????????????
        sourceBuilder.query(boolQuery);


        /**
         * ????????????????????????
         */

        //?????? sort=saleCount_desc
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");

            SortOrder order = s[1].equalsIgnoreCase("ace") ? SortOrder.ASC : SortOrder.DESC;

            sourceBuilder.sort(s[0], order);
        }

        //??????
        // pageNum:1 from:0 size:5 [0,1,2,3,4]
        // pageNum:2 from:5 size:5 [5,6,7,8,9]
        //from = (pageNum - 1)*srze
        sourceBuilder.from((param.getPageNum() - 1)*EsConstant.PRODUCT_PAGE_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        //?????? ?????????????????? skuTitle ??????
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            sourceBuilder.highlighter(builder);
        }



        /**
         * ????????????
         */

        //1??????????????? brand_agg
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //2??????????????? catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catagoryName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //3??????????????? attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

//        String s = sourceBuilder.toString();
//        System.out.println("?????????DSL?????????"+s);

        //public SearchRequest(String[] indices, SearchSourceBuilder source) {
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();

        //1.???????????????????????????
        List<com.atguigu.common.to.es.SkuEsModel> skuEsModels = new ArrayList<>();
        if(hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String s = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(s, SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                skuEsModels.add(esModel);
            }
        }
        result.setProducts(skuEsModels);


        //2.??????????????????
        result.setPageNum(param.getPageNum());
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPage = (int) total % EsConstant.PRODUCT_PAGE_SIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGE_SIZE : ((int) total / EsConstant.PRODUCT_PAGE_SIZE + 1);
        result.setTotalPage(totalPage);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        //===================?????????????????????=====================

        //3.???????????????????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> brandBuckets = ((ParsedLongTerms) response.getAggregations().get("brand_agg")).getBuckets();
        if(brandBuckets != null && brandBuckets.size() > 0){
            for (Terms.Bucket bucket : brandBuckets) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                //??????id
                long brandId = bucket.getKeyAsNumber().longValue();
                brandVo.setBrandId(brandId);
                //?????????
                List<? extends Terms.Bucket> brand_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets();
                if(brand_name_agg != null && brand_name_agg.size() > 0){

                    String brandName = brand_name_agg.get(0).getKeyAsString();
                    brandVo.setBrandName(brandName);
                }
                //????????????
                List<? extends Terms.Bucket> brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets();
                if(brand_img_agg != null && brand_img_agg.size() > 0){

                    String brandImg = brand_img_agg.get(0).getKeyAsString();
                    brandVo.setBrandImg(brandImg);
                }


                brandVos.add(brandVo);
            }
        }
        result.setBrands(brandVos);

        //4.???????????????????????????
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        if(buckets != null && buckets.size() > 0){
            for (Terms.Bucket bucket : buckets) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                //????????????id
                long catalogId = bucket.getKeyAsNumber().longValue();
                //???????????????
                List<? extends Terms.Bucket> catalog_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("catalog_name_agg")).getBuckets();
                if(catalog_name_agg != null && catalog_name_agg.size() > 0){
                    String catalogName = catalog_name_agg.get(0).getKeyAsString();
                    catalogVo.setCatalogName(catalogName);
                }

                catalogVo.setCatalogId(catalogId);

                catalogVos.add(catalogVo);
            }
        }

        result.setCatalogs(catalogVos);

        //5.???????????????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrBuckets = attr_id_agg.getBuckets();
        if(attrBuckets != null && attrBuckets.size() > 0){
            for (Terms.Bucket bucket : attrBuckets) {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                //????????????id
                long attrid = bucket.getKeyAsNumber().longValue();
                //???????????????
                List<? extends Terms.Bucket> attr_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets();
                if(attr_name_agg != null && attr_name_agg.size() > 0){

                    String attrName = attr_name_agg.get(0).getKeyAsString();
                    attrVo.setAttrName(attrName);
                }
                //???????????????
                List<? extends Terms.Bucket> attr_value_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets();
                if(attr_value_agg != null && attr_value_agg.size() > 0){

                    List<String> attrValue = attr_value_agg.stream().map(item -> {
                        return item.getKeyAsString();
                    }).collect(Collectors.toList());
                    attrVo.setAttrValue(attrValue);
                }

                attrVo.setAttrId(attrid);

                attrVos.add(attrVo);
            }
        }

        result.setAttrs(attrVos);

        //6.?????????????????????
        List<String> attrs = param.getAttrs();
        if(attrs != null && attrs.size() > 0){
            List<SearchResult.Navo> navos = attrs.stream().map(attr -> {
                //1.???????????????attrs????????????????????????
                SearchResult.Navo navo = new SearchResult.Navo();
                //attrs=2_5???:6???
                String[] s = attr.split("_");
                navo.setNavValue(s[1]);

                //???????????? ?????? attr ???id??????attr?????????
                R info = productFeignService.info(Long.parseLong(s[0]));

                //??????????????????id?????????
                result.getAttrIds().add(Long.parseLong(s[0]));

                if(info.getCode() == 0){
                    AttrRespVo attrRespVo = info.getData("attr", new TypeReference<AttrRespVo>() {
                    });
                    navo.setNavName(attrRespVo.getAttrName());
                }else{
                    navo.setNavName(s[0]);
                }

                //2???????????????????????????????????????????????????????????????????????????????????????url??????
                //??????????????????????????????????????????
                String querySearch = param.get_querySearch();
                String replace = replaceQueryString(param, attr,"attrs");
                navo.setLink("http://search.gulimall.com/list.html?"+replace);

                return navo;
            }).collect(Collectors.toList());

            result.setNavs(navos);
        }

        //???????????????
        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            List<SearchResult.Navo> navs = result.getNavs();

            SearchResult.Navo navo = new SearchResult.Navo();
            navo.setNavName("??????");
            //TODO ????????????????????????
            R r = productFeignService.getBrands(param.getBrandId());
            if(r.getCode() == 0){ //??????????????????
                List<BrandInfoVo> brand = r.getData("brand", new TypeReference<List<BrandInfoVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                if(brand != null && brand.size() > 0){
                    for (BrandInfoVo vo : brand) {
                        buffer.append(vo.getName() + "");
                        replace = replaceQueryString(param,vo.getBrandId()+"","brandId");
                    }
                }
                navo.setNavValue(buffer.toString());
                navo.setLink("http://search.gulimall.com/list.html?"+replace);

            }
            navs.add(navo);

        }

        //TODO ??????????????????????????????


        return result;
    }

    private String replaceQueryString(SearchParam param, String attr,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");//9_??????%2010%20Pro%20(5G)
            encode = encode.replace("+","%20").replace("%28","(").replace("%29",")").replace("%3B",";");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // 17_??????(Qualcomm)
        }

        if(param.get_querySearch().startsWith(key)){
            return param.get_querySearch().replace(key+"=" + encode, "");
        }else{

            return param.get_querySearch().replace("&"+key+"=" + encode, "");
        }


    }


}
