---
typora-root-url: images
---

#OpenFeign



###openfeign 原理

```java

1.先调用 FeignInvocationHandler 的 invoke 比较 equals hashCode toString
 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			//比较 equals hashCode toString
            if (!"equals".equals(method.getName())) {
                if ("hashCode".equals(method.getName())) {
                    return this.hashCode();
                } else {
                //都不是 执行 ((MethodHandler)this.dispatch.get(method)).invoke(args)
                    return "toString".equals(method.getName()) ? this.toString() : ((MethodHandler)this.dispatch.get(method)).invoke(args);
 

2.SynchronousMethodHandler（SynchronousMethodHandler implements MethodHandler）
2.1
public Object invoke(Object[] argv) throws Throwable {
		//封装请求数据，并将请求参数转为 json 格式
        RequestTemplate template = this.buildTemplateFromArgs.create(argv);
        Options options = this.findOptions(argv);
        //获取重试器
        Retryer retryer = this.retryer.clone();
        while(true) {
            try {
                return this.executeAndDecode(template, options);
            } catch (RetryableException var9) {
                try {
                    retryer.continueOrPropagate(e);
2.2
Object executeAndDecode(RequestTemplate template, Options options) throws Throwable {
		//封装请求模板 2.2.1
        Request request = this.targetRequest(template);
        try {
        //执行远程方法
            response = this.client.execute(request, options);
2.2.1
 Request targetRequest(RequestTemplate template) {
 		//获取所有的请求拦截器RequestInterceptor 
        Iterator var2 = this.requestInterceptors.iterator();
        while(var2.hasNext()) {
            RequestInterceptor interceptor = (RequestInterceptor)var2.next();
            //将RequestInterceptor中的请求参数封装到新的请求体中
            interceptor.apply(template); }
        return this.target.apply(template); }
```



```java
重试器 Retryer
public interface Retryer extends Cloneable 
		public Default() {
			//默认最大请求 5次
            this(100L, TimeUnit.SECONDS.toMillis(1L), 5);
        }
		//也可以自定以 maxAttempts 最大重试次数
        public Default(long period, long maxPeriod, int maxAttempts) {
            this.period = period;
            this.maxPeriod = maxPeriod;
            this.maxAttempts = maxAttempts;
            this.attempt = 1;
        }
        

重试核心
public void continueOrPropagate(RetryableException e) {
		//重试大于最大重试次数抛异常退出
            if (this.attempt++ >= this.maxAttempts) {
                throw e;
            } else {
                long interval;
                if (e.retryAfter() != null) {
                	//获取程序的运行时长
                    interval = e.retryAfter().getTime() - this.currentTimeMillis();
                    if (interval > this.maxPeriod) {
                        interval = this.maxPeriod;
                    }

                    if (interval < 0L) {
                        return;
                    }
                } else {
                    interval = this.nextMaxInterval();
                }

                try {
                	//在程序运行时让该方法不要重试
                    Thread.sleep(interval);
                } catch (InterruptedException var5) {
                    Thread.currentThread().interrupt();
                    throw e;
                }

                this.sleptForMillis += interval;
            }
        }
```



### feign远程调用问题

#####远程调用丢失请求头

![Snipaste_2020-10-31_22-40-29](/Snipaste_2020-10-31_22-40-29.jpg)

```java
feign 远程调用通过 SynchronousMethodHandler 
bject executeAndDecode(RequestTemplate template, Options options) throws Throwable {
        Request request = this.targetRequest(template);
 获取 RequestTemplate 请求模板
 targetRequest 方法会先查看有没有RequestInterceptor的拦截器，遍历拦截器为RequestTemplate封装请求模板
  Request targetRequest(RequestTemplate template) {
        Iterator var2 = this.requestInterceptors.iterator();
        while(var2.hasNext()) {
            RequestInterceptor interceptor = (RequestInterceptor)var2.next();
            interceptor.apply(template);
        }
        return this.target.apply(template);
    }

所以要给feign的远程调用封装请求只需要在容器中注入RequestInterceptor，将页面请求过来的请求参数，按照需求封装
    @Bean
    public RequestInterceptor requestInterceptor(){ 
        return (RequestTemplate requestTemplate) -> {
             // RequestContextHolder 拿到页面请求过来的原始请求的上下文
                ServletRequestAttributes attributes = 
                	(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes != null){
                    //老请求
                    HttpServletRequest request = attributes.getRequest();
                    //同步请求头数据
                    String cookie = request.getHeader("Cookie");
                    //设置新请求
                    requestTemplate.header("Cookie",cookie);
                }
        };
    }


```



#####远程调用丢失请求体(异步)

![Snipaste_2020-10-31_22-40-44](/Snipaste_2020-10-31_22-40-44.jpg)

 ```java
为每个线程都同步请求数据
RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes());

 ```





