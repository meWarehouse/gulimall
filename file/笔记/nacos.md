```linux
docker run --name nacos -d -p 8848:8848 --privileged=true --restart=always -e JVM_XMS=200m -e JVM_XMX=200m -Xmn=200m -e MODE=standalone -e PREFER_HOST_MODE=hostname -v /mydata/nacos/logs:/home/nacos/logs -v /mydata/nacos/conf:/home/nacos/conf nacos/nacos-server:latest

```

```linux
docker run --name nacos -d -p 8848:8848 --restart=always -e JVM_XMS=250m -e JVM_XMX=250m -e JVM_XMN=200m -e MODE=standalone -e PREFER_HOST_MODE=hostname -v /mydata/nacos/logs:/home/nacos/logs -v /mydata/nacos/conf:/home/nacos/conf nacos/nacos-server:1.2.1
```

