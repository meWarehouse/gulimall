---
typora-root-url: images
---

# Elasticsearch&Kibana

7.x及以后移除了类型

![Snipaste_2020-10-19_12-51-50](Snipaste_2020-10-19_12-51-50.jpg)

## docker  安装

### 1.拉取镜像

```linux
docker pull elasticsearch:7.4.2
docker pull kibana:7.4.2
```

### 2.创建容器

```linux
1.创建外部配置文件 
#配置文件
mkdir -p /mydata/elasticsearch/config 
#存储的数据
mkdir -p /mydata/elasticsearch/data 
echo "http.host: 0.0.0.0" >> /mydata/elasticsearch/config/elasticsearch.yml
#http.host: 0.0.0.0 ==>远程的任意主机皆可访问

运行后报错->
Caused by: java.nio.file.AccessDeniedException: /usr/share/elasticsearch/data/nodes"
修改权限-任何用户，组可读写执行
chmod -R 777 /mydata/elasticsearch/

```

### 3.运行

```linux
运行 ElasticSearch

#9200 Rest风格API通信端口 9300分布式下集群的通信端口
#discovery.type=single-node 单节点模式
#ES_JAVA_OPTS="-Xms64m -Xmx512m" 设置占用内存大小 
#将容器中的 config data plugins(插件) 挂载到外部
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 \
-e "discovery.type=single-node" \
-e ES_JAVA_OPTS="-Xms64m -Xmx512m" \
-v /mydata/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
-v /mydata/elasticsearch/data:/usr/share/elasticsearch/data \
-v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins \
-d elasticsearch:7.4.2


运行kibana
docker run --name kibana -e ELASTICSEARCH_URL=http://自己的主机地址:9200 -p 5601:5601 -d kibana:7.4.2


```

## Elasticsearch API

[https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-search.html](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-search.html)

#### 1._cat 查看节点信息

```linux
GET /_cat/nodes			查看所有节点
GET /_cat/health		查看es健康状况
GET /_cat/master		查看主节点
GET /_cat/indices		查看所有索引  ==>show databases;
```

#### 2.增 PUT POST

```java
1.PUT /INDEX/INDEXID
#PUT必须指定指定id 每一次put都会 使得版本 _seq_no 更新
PUT /yourIndex/indexID
{
  "DataName":"DataValue"
}

2.POST /INDEX
#POST会随机生成一个id 每一次 POST 都会新增一个数据
POST /yourIndex
{
 "DataName":"DataValue"
}

if_seq_no=? & if_primary_term =? 可以做成乐观锁
```

#### 3.更新 POST

```java
1.POST /INDEX/INDEXID/_update
# 更新数据与原来的数据相同则不会改变 "result" : "noop" 必须加 "doc":{} 
POST /customer/1/_update
{
  "doc": {
    "name": "pop1"
  }
}
2.POST /INDEX/INDEXID/
#不会检查原来的数据 直接跟新 版本号叠加
POST /customer/external/1
{
  "name":"11"
}
```

#### 4.删除 DELETE

```java
DELETE /INDEX/_doc/INDEXID	 删除指定id的数据
DELETE /INDEX				删除INDEX 相当于MySQL的删除数据库

```

#### 5.bulk 批量操作

```java
POST customer/external/ _bulk
{"index":{"_id":"1"}}
{"name":"wei-xhh"}
{"index":{"_id":"2"}}
{"name":"wei-xhh66"}

语法格式：
{action: {metadata}}
{request body}      
{action: {metadata}}
{request body}      

复杂实例
POST / _bulk
{ "delete":{ "_index":"website", "_type":"blog", "_id":"123"}}
{ "create":{ "_index":"website", "_type":"blog", "_id":"123"}}
{ "title":"my first blog post"}
{ "index":{ "_index":"website", "_type":"blog"}}
{ "title":"my second blog post"}
{ "update":{ "_index":"website", "_type":"blog", "_id":"123"}}
{ "doc":{ "title":"my updated blog post"}}
```

#### 6.数据迁移 POST _reindex

```java
https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
POST _reindex
{"source":{"index":"bank","type":"account"},"dest":{"index":"newbank-001"}}

```

#### 7.映射 mapping

```java
https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html
1.查看映射
GET /INDEX/_mapping
2.添加
PUT /my-index-000001/_mapping
{"properties":{"employee-id":{"type":"keyword","index":false}}}
3.修改，
!!!映射无法修改，要想修改必须新建一个 INDEX 指定好映射，然后将数据迁移到新建的 INDEX

```

#### 8.检索

```java
查询语句的典型结构
{
    QUERY_NAME:{
        ARGUMENT:VALUE,
        ARGUMENT:VALUE,...
    }
}

如果针对某个字段,结构如下
{
     QUERY_NAME:{
        FIELD_NAME:{
            ARGUMENT:VALUE,
            ARGUMENT:VALUE,...
        }
    }  
}

#通过使用 REST request body 来发送它们 (url+请求体）
GET /bank/_search?q=*&sort=account_number:asc
#通过使用 REST request URI 发送搜索参数 (uri+检索参数）
GET /bank/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "account_number": {
        "order": "asc"
      }
    }
  ]
}

```

##### 8.1 match 匹配

```java
1.match 模糊查询  会进行分词匹配
GET /bank/_search
{"query":{"match":{"address":"mill lane"}}}

2.match_phrase 不分词匹配
GET /bank/_search
{"query":{"match_phrase":{"address":"mill lane"}}}

3.multi_match 多字段匹配
GET /bank/_search
{"query":{"multi_match":{"query":"Street Movico","fields":["address","city"]}}}

```

##### 8.2 term&match

```java
模糊检索推荐使用match -> 文本字段使用
精确检索推荐使用term -> 非文本字段使用
1.term
GET /bank/_search
{"query":{"term":{"age":{"value":"20"}}}}
2.match
GET /bank/_search
{"query":{"match":{"address":"132 Gunnison Court"}}}
GET /bank/_search
{"query":{"match":{"address.keyword":"132 Gunnison Court"}}}

```

#####8.3 bool 复合查询

```java
复合查询可以合并任何其他查询语句，包括复合语句，这就意味则，复合语句之间可以互相嵌套，可以表达非常复杂的逻辑
GET /bank/_search
{"query":{"bool":{"must":[{"match":{"gender":"F"}},{"match":{"address":"Street"}}],"must_not":[{"match":{"age":"28"}}],"should":[{"match":{"lastname":"Mcpherson"}}]}}}
```

##### 8.4 filter 过滤

```java
并不是所有的查询都需要产生分数，特别是那些仅用于filtering (过滤)的文档，为了不计算分数Elasticsearch会自动检查场景并且优化查询的执行
GET /bank/_search
{"query":{"bool":{"filter":{"range":{"age":{"gte":20,"lte":29}}}}}}
```

##### 8.5 agg 聚合

```java
聚合提供了从数据中分组和提取数据的能力，最简单的聚合方法大致等于 SQL GROUP BY 和 SQL聚合函数。在Elasticsearch中，您有执行搜索返回hits(命中结果)，并且同时返回聚合结果，把一个响应的所有hits(命中结构)分隔开的能力。可以执行查询和多个聚合，并且在一次使用中得到各自的(任何一个的)返回结果，使用一次简洁和简化的API来避免网络往返
GET /bank/_search
{"query":{"match":{"address":"mill"}},"sort":[{"balance":{"order":"desc"}}],"aggs":{"aggAge":{"terms":{"field":"age","size":10},"aggs":{"balanceAvg":{"avg":{"field":"balance"}}}},"aggAvg":{"avg":{"field":"age"}},"balanceAvg":{"avg":{"field":"balance"}}}}

```

### 9.nested的数据类型介绍

https://www.elastic.co/guide/en/elasticsearch/reference/7.x/nested.html

```
定义的属性重重嵌套，使用nested嵌入式属性，就不会出现扁平化。
```



### 分词

```java
https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-standard-analyzer.html

安装ik分词器
https://github.com/medcl/elasticsearch-analysis-ik/releases?after=v7.6.0
解压到 elasticsearch 中的 plugings 目录下
检查是否安装成功
进入 /usr/share/elasticsearch/bin 
执行 elasticsearch-plugin list 查看安装的分词器

#ik_smart ik_max_word
POST _analyze 
{
  "analyzer": "ik_smart",
  "text": "尚硅谷电商项目"
}
POST _analyze
{
  "analyzer": "ik_max_word",
  "text": "尚硅谷电商项目"
}	
```

##### 自定义分词

elasticsearch/plugins/ik/config

![Snipaste_2020-10-19_16-26-14](/Snipaste_2020-10-19_16-26-14.jpg)

http://192.168.44.104/es/fenci.txt 将词库放入nginx的html文件下

### 



## [Java High Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)

### springboot 整合 elasticsearch



```java
1.导入依赖 Getting started -》 Maven Repository
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started.html
2.注入连接客户端 Getting started -》 Initialization
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-getting-started-initialization.html
3.构造请求选项 Getting started -》 RequestOptions
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-low-usage-requests.html#java-rest-low-usage-request-options

```



### 保存

```java
构造需要发送的数据 --》 一般以 JSON 的形式发送
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html

单个：
向指定的INDEX 保存数据
	IndexRequest request = new IndexRequest(INDEX); 
设置数据的id
	request.id(INDEXID); 
将数据转为JSON 格式 进行上传保存  XContentType.JSON 指明上传类型
	String jsonString = json
	request.source(jsonString, XContentType.JSON); 
发送请求保存数据
	IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
批量：
	构造批量请求
	BulkRequest bulkRequest = new BulkRequest();
	IndexRequest request = new IndexRequest(INDEX); 
	request.id(INDEXID); 
	将单个放入批量中
	bulkRequest.add(request);
	调用批量执行
	BulkResponse bulk = client.bulk(bulkRequest, ESConfig.COMMON_OPTIONS);
      


```



### 检索

```java
https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html
封装数据
SearchRequest searchRequest = new SearchRequest(); 
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
searchSourceBuilder.query(QueryBuilders.matchAllQuery()); 
searchRequest.source(searchSourceBuilder); 
发送数据
SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
解析响应数据

SearchSourceBuilder searchBulider = new SearchSourceBuilder();
//xxxBuilders.xxx 获取该类型的查询
BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
searchBulider.query(boolQuery);
SearchRequest searchRequest = new SearchRequest(
  new String[{EsConstant.PRODUCT_INDEX},searchBulider);



```



![Snipaste_2020-10-29_15-49-18](/Snipaste_2020-10-29_15-49-18.jpg)







































































































