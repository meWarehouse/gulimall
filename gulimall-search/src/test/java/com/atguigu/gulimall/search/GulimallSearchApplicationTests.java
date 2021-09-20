package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sun.nio.cs.HistoricallyNamedCharset;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class GulimallSearchApplicationTests {

    @Resource
    RestHighLevelClient client;



    /**
     *
     * GET bank/_search
     * {
     *   "query": {
     *     "multi_match": {
     *       "query": "mill brogan",
     *       "fields": ["address","city"]
     *     }
     *   },
     *   "aggs": {
     *     "ageAgg": {
     *       "terms": {
     *         "field": "age",
     *         "size": 100
     *       },
     *       "aggs": {
     *         "balanceAvg": {
     *           "avg": {
     *             "field": "balance"
     *           }
     *         },
     *         "ageAvg": {
     *           "avg": {
     *             "field": "age"
     *           }
     *         }
     *       }
     *     },
     *     "balanceSum": {
     *       "sum": {
     *         "field": "balance"
     *       }
     *     },
     *     "balanceAvg": {
     *       "avg": {
     *         "field": "age"
     *       }
     *     }
     *   }
     * }
     */
    @Test
    void searchData1() throws IOException {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] fileNames = {"address", "city"};
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("mill brogan", fileNames));
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(100);
        ageAgg.subAggregation(AggregationBuilders.avg("balanceAgv").field("balance"));
        ageAgg.subAggregation(AggregationBuilders.avg("ageAvg").field("age"));
        searchSourceBuilder.aggregation(ageAgg);

        searchSourceBuilder.aggregation(AggregationBuilders.sum("balanceSum").field("balance"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg1").field("balance"));


        System.out.println("查询条件:" + searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);


        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("查询出的数据：" + searchResponse);

        SearchHits hits = searchResponse.getHits();
        System.out.println("总记录数："+hits.getTotalHits());

        SearchHit[] searchHits = hits.getHits();
        System.out.println("每条记录对象：");
        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        System.out.println("聚合信息：");
        Aggregations aggregations = searchResponse.getAggregations();

        Sum balanceSum = aggregations.get("balanceSum");
        System.out.println("总的工资balanceSum："+balanceSum.getValue());

        Avg balanceAvg1 = aggregations.get("balanceAvg1");
        System.out.println("平均工资balanceAvg1:"+balanceAvg1.getValue());

        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {

            System.out.println("key:"+bucket.getKeyAsString()+",doc_count:"+bucket.getDocCount());

            Avg ageAvg = bucket.getAggregations().get("ageAvg");
            System.out.println("ageAvg:"+ageAvg.getValue());

            Avg balanceAgv = bucket.getAggregations().get("balanceAgv");
            System.out.println("balanceAgv:"+balanceAgv.getValue());

        }


    }

    @Test
    void searchData() throws IOException {

        /**
         * GET bank/_search
         * {
         *   "query": {
         *     "term": {
         *       "address": {
         *         "value": "mill"
         *       }
         *     }
         *   },
         *   "aggs": {
         *     "ageAgg": {
         *       "terms": {
         *         "field": "age",
         *         "size": 100
         *       },
         *       "aggs": {
         *         "ageAvg": {
         *           "avg": {
         *             "field": "balance"
         *           }
         *         }
         *       }
         *     }
         *   }
         * }
         */

        //1.创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //2.指定索引
        searchRequest.indices("bank");
        //3.指定DSL,检索条件 SearchSourceBuilder封装检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.termQuery("address","mill"));
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        ageAgg.subAggregation(balanceAvg);
        searchSourceBuilder.aggregation(ageAgg);



        System.out.println("检索条件："+searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);

        //4.执行检索 并返回检索结果
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("searchResponse:"+searchResponse);


        //5.分析结果 searchResponse
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("account:"+account);

        }

        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("key:"+keyAsString+"->"+bucket.getDocCount());

            Avg balanceAvg1 = bucket.getAggregations().get("balanceAvg");
            System.out.println("balanceAvg:"+balanceAvg1.getValue());

        }


    }

    @Data
    @ToString
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
    void testElasticSearchIndex() throws IOException {

        //获取index 并设置 索引 users
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");

        User user = new User();
        user.setId(1L);
        user.setName("张珊");
        user.setAge(12);

        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);
        indexRequest.timeout("10s");

        //保存数据
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(index);

    }

    @Data
    class User{
        private Long id;
        private String name;
        private Integer age;
    }



    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
