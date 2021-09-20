---
typora-root-url: images
---

# 社交登录

![Snipaste_2020-10-27_17-33-51](/Snipaste_2020-10-27_17-33-51.jpg)







####微博社交登录

```java
微博开放平台
https://open.weibo.com/wiki/%E6%8E%88%E6%9D%83%E6%9C%BA%E5%88%B6%E8%AF%B4%E6%98%8E
https://open.weibo.com/apps/774481333/privilege


1. 引导需要授权的用户到如下地址：
https://api.weibo.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI

2. 如果用户同意授权，页面跳转至 YOUR_REGISTERED_REDIRECT_URI/?code=CODE

3. 使用上一步获取到的code换取Access Token
https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
其中client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET可以使用basic方式加入header中，返回值

4. 使用获得的Access Token调用API
	开发接口文档 https://open.weibo.com/apps/774481333/privilege
```



































































