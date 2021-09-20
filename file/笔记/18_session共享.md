---
typora-root-url: images
---

# session共享



### session原理

![Snipaste_2020-10-27_20-29-24](/Snipaste_2020-10-27_20-29-24.jpg)



### 分布式下session共享问题

![Snipaste_2020-10-27_20-29-13](/Snipaste_2020-10-27_20-29-13.jpg)

#####1.session复制（不用）

![Snipaste_2020-10-27_20-29-37](/Snipaste_2020-10-27_20-29-37.jpg)

#####2.客服端存储（不用）

![Snipaste_2020-10-27_20-31-24](/Snipaste_2020-10-27_20-31-24.jpg)

#####3.hash一致性

![Snipaste_2020-10-27_20-31-47](/Snipaste_2020-10-27_20-31-47.jpg)

#####4.统一存储



![Snipaste_2020-10-27_20-32-07](/Snipaste_2020-10-27_20-32-07.jpg)



###springsession

https://spring.io/projects/spring-session-data-redis#learn



#####springsession原理

```java

1.@EnableRedisHttpSession 导入  RedisHttpSessionConfiguration 配置
 
2.RedisHttpSessionConfiguration 给容器中注入了一个 RedisIndexedSessionRepository(redis操作session，session的增删改查封装类)
  
3.SpringHttpSessionConfiguration
	@PostConstruct
	public void init() {
		CookieSerializer cookieSerializer = (this.cookieSerializer != null) ? 	   
		                         this.cookieSerializer: createDefaultCookieSerializer();
		this.defaultHttpSessionIdResolver.setCookieSerializer(cookieSerializer);
	}
	@Bean
	public <S extends Session> SessionRepositoryFilter<? extends Session> springSessionRepositoryFilter(SessionRepository<S> sessionRepository) {
		SessionRepositoryFilter<S> sessionRepositoryFilter = 
							new SessionRepositoryFilter<>(sessionRepository);
		sessionRepositoryFilter.setHttpSessionIdResolver(this.httpSessionIdResolver);
		return sessionRepositoryFilter;
	}
4.SessionRepositoryFilter 的本质就是一个 servert Filter
	SessionRepositoryFilter 的构造器在容器中注入了 SessionRepository
	public SessionRepositoryFilter(SessionRepository<S> sessionRepository) 
  	SessionRepository 的实现类就是RedisIndexedSessionRepository对redis操作session，session的		增删改查封装类
  	
	doFilterInternal 是springsession 的核心  	
  		@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);
		//包装原始的请求对象
		SessionRepositoryRequestWrapper wrappedRequest = 
          				new SessionRepositoryRequestWrapper(request, response);
  		//包装原始的响应对象
		SessionRepositoryResponseWrapper wrappedResponse = 
          				new SessionRepositoryResponseWrapper(wrappedRequest,
				response);
		try {
          	//包装后的对象应用到后面的整个执行链 以后获取的session都是从wrappedRequest获取的
			filterChain.doFilter(wrappedRequest, wrappedResponse);
5.SessionRepositoryRequestWrapper 获取session
  public HttpSessionWrapper getSession(boolean create) {
		S requestedSession = getRequestedSession();
  private S getRequestedSession() {
			SessionRepositoryFilter.this.httpSessionIdResolver.resolveSessionIds(this);
    以后所有的session都是通过 HttpSessionIdResolver 获取
      
      
      
      
 1.@EnableRedisHttpSession 导入  RedisHttpSessionConfiguration 配置
       1.1.给容器中添加了一个组件
       RedisIndexedSessionRepository
          SessionRepository ==》 RedisIndexedSessionRepository ：redis操作session，session的增删改查封装类
       1.2.SessionRepositoryFilter ==》 servlet原生的Filter session存储过滤器，每个请求过来都必须经过filter
          1.2.1：创建该过滤器是会自动从容器中获取得到 SessionRepository
       	  1.2.2：原始的 request response 都被包装 SessionRepositoryRequestWrapper SessionRepositoryResponseWrapper
          1.2.3：以后获取session request.getSession()
          1.2.4：wrappedRequest.getSeeeion() ==> SessionRepository 中获取
 
```



























