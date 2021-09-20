package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zero
 * @create 2020-09-08 23:18
 */
@Configuration
public class ElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient(){
//
//        RestClientBuilder builder = null;
//        //String hostname, int port, String scheme
//        builder = RestClient.builder(new HttpHost("192.168.44.104",9200,"http"));
//
//        RestHighLevelClient client = new RestHighLevelClient(builder);

        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.44.104", 9200, "http")
                ));

    }

}
