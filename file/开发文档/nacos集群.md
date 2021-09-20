---
typora-root-url: images
---

# nacos 集群

```java
1.下载docker
https://docs.docker.com/engine/install/centos/
2.下载 docker-compose
https://docs.docker.com/compose/install/
太慢选择使用pip
	2.1：安装docker-compose环境
		yum install -y python3-pip
	2.2：添加 pip 的镜像
		cd ~
		mkdir .pip
		cd .pip
		vi pip.conf
	2.3：pip.conf 中的内容为
		[global]
		index-url=https://pypi.tuna.tsinghua.edu.cn/simple/
		trusted-host=pypi.tuna.tsinghua.edu.cn
	2.4：pip3 install docker-compose
	2.5：检验 docker-compose version 显示版本安装成功
	
3. 开始搭建nacos集群
	官网的地址：https://nacos.io/zh-cn/docs/quick-start-docker.html
	3.1：拉取文件
		git clone https://github.com/nacos-group/nacos-docker.git
		下载完成后可以看见nacos-docker文件夹
```

![Snipaste_2020-10-22_20-14-34](/Snipaste_2020-10-22_20-14-34.jpg)

```java
	3.2：修改配置文件
```

![Snipaste_2020-10-22_20-16-09](/Snipaste_2020-10-22_20-16-09.jpg)

```java
	3.3：启动
		docker-compose -f example/cluster-hostname.yaml up 

```

![Snipaste_2020-10-22_20-17-33](/Snipaste_2020-10-22_20-17-33.jpg)



![Snipaste_2020-10-22_20-19-03](/Snipaste_2020-10-22_20-19-03.jpg)

https://blog.csdn.net/Yuudachi/article/details/105648436































