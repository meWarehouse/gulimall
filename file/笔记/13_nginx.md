# NGINX

[示例](https://www.nginx.cn/doc/example/fullexample2.html)

```linux
docker run -p 80:80 --name nginx \
-v /mydata/nginx/html:/usr/share/nginx/html \
-v /mydata/nginx/logs:/var/log/nginx \
-v /mydata/nginx/conf:/etc/nginx \
-d nginx:1.10
```



```java

默认会丢失请求头 proxy_set_header Host	$host;

```

