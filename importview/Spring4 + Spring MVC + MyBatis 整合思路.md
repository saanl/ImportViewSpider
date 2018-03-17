<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://my.oschina.net/langxSpirit/blog/877396">斯武丶风晴</a>
 </div> 
 <h2>1、Spring框架的搭建</h2> 
 <p>这个很简单，只需要web容器中注册org.springframework.web.context.ContextLoaderListener，并指定spring加载配置文件，那么spring容器搭建完成。（当然org.springframework的核心jar包需要引入）</p> 
 <p>当然为了更加易用支持J2EE应用，一般我们还会加上如下：</p> 
 <p>Spring监听HTTP请求事件：org.springframework.web.context.request.RequestContextListener</p> 
 <pre class="brush: java; gutter: true">&lt;!-- spring配置文件开始 --&gt;
	&lt;context-param&gt;
		&lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;&lt;!-- spring配置文件，请根据需要选取 --&gt;
		&lt;param-value&gt;classpath*:webconfig/service-all.xml&lt;/param-value&gt;
	&lt;/context-param&gt;
	&lt;listener&gt;&lt;!-- Spring负责监听web容器启动和关闭的事件 --&gt;&lt;!-- Spring ApplicationContext载入 --&gt;
		&lt;listener-class&gt;org.springframework.web.context.ContextLoaderListener&lt;/listener-class&gt;
	&lt;/listener&gt;
	&lt;listener&gt;&lt;!-- Spring监听HTTP请求事件 --&gt;
		&lt;!-- 使spring支持request与session的scope,如: --&gt;
		&lt;!-- &lt;bean id="loginAction" class="com.foo.LoginAction" scope="request"/&gt; --&gt;
		&lt;!-- 使用： --&gt;
		&lt;!-- 1、注解获取：@Autowired HttpServletRequest request; --&gt;
		&lt;!-- 2、java代码：HttpServletRequest request = 
		((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest(); --&gt;
		&lt;!-- 3、直接在参数中传递：public String sayHi(HttpServletRequest request) --&gt;
		&lt;listener-class&gt;org.springframework.web.context.request.RequestContextListener&lt;/listener-class&gt;
	&lt;/listener&gt;
	&lt;listener&gt;&lt;!-- Spring 刷新Introspector防止内存泄露 --&gt;
		&lt;listener-class&gt;org.springframework.web.util.IntrospectorCleanupListener&lt;/listener-class&gt;
	&lt;/listener&gt;
	&lt;filter&gt;
		&lt;filter-name&gt;encodingFilter&lt;/filter-name&gt;
		&lt;filter-class&gt;org.springframework.web.filter.CharacterEncodingFilter&lt;/filter-class&gt;
		&lt;init-param&gt;
			&lt;param-name&gt;encoding&lt;/param-name&gt;
			&lt;param-value&gt;UTF-8&lt;/param-value&gt;
		&lt;/init-param&gt;
		&lt;init-param&gt;
			&lt;param-name&gt;forceEncoding&lt;/param-name&gt;
			&lt;param-value&gt;false&lt;/param-value&gt;
		&lt;/init-param&gt;
	&lt;/filter&gt;
	&lt;filter-mapping&gt;
		&lt;filter-name&gt;encodingFilter&lt;/filter-name&gt;
		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
	&lt;/filter-mapping&gt;
	&lt;!-- spring配置文件结束 --&gt;</pre> 
 <h2>2、Spring MVC的搭建</h2> 
 <p>首先我们知道Spring MVC的核心是org.springframework.web.servlet.DispatcherServlet，所以web容器中少不了它的注册。（当然org.springframework的web、mvc包及其依赖jar包需要引入）</p> 
 <pre class="brush: java; gutter: true">&lt;!-- spring mvc配置开始 --&gt;
	&lt;servlet&gt;
		&lt;servlet-name&gt;Spring-MVC&lt;/servlet-name&gt;
		&lt;servlet-class&gt;org.springframework.web.servlet.DispatcherServlet&lt;/servlet-class&gt;
		&lt;init-param&gt;
			&lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
			&lt;param-value&gt;classpath*:spring/spring-mvc.xml&lt;/param-value&gt;&lt;!-- spring mvc配置文件 --&gt;
		&lt;/init-param&gt;
		&lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
	&lt;/servlet&gt;
	&lt;servlet-mapping&gt;
		&lt;servlet-name&gt;Spring-MVC&lt;/servlet-name&gt;
		&lt;url-pattern&gt;*.do&lt;/url-pattern&gt;
	&lt;/servlet-mapping&gt;
	&lt;!-- spring mvc配置结束 --&gt;</pre> 
 <p>同时为了更好使用MVC，spring-mvc.xml需要配置以下：</p> 
 <p>1）（可选）多部分请求解析器（MultipartResolver）配置，与上传文件有关 需要类库commons-io、commons-fileupload</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver"&gt;
		&lt;property name="defaultEncoding" value="utf-8"&gt;&lt;/property&gt;&lt;!-- 默认编码--&gt;
		&lt;property name="maxUploadSize" value="104857600"&gt;&lt;/property&gt;&lt;!-- 文件大小最大值--&gt;
		&lt;property name="maxInMemorySize" value="40960"&gt;&lt;/property&gt;&lt;!-- 内存中的最大值--&gt;
	&lt;/bean&gt;</pre> 
 <p>2）（可选）本地化（LocaleResolver）配置</p> 
 <p>3）（可选）主题解析器（ThemeResolver）配置</p> 
 <p>4）（必选）处理器映射器（HandlerMapping）配置，可以配置多个，一般采用RequestMappingHandlerMapping或者自定义</p> 
 <p>这里我们自定义了一个处理器映射器，继承重写RequestMappingHandlerMapping，支持@RequestMapping无需任何path参数自动装载类名或方法作为url路径匹配。</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="handlerMapping" 
		class="io.flysium.framework.web.servlet.mvc.method.annotation.CustomHandlerMapping"&gt;
		&lt;property name="order" value="-1" /&gt;
	&lt;/bean&gt;</pre> 
 <p>CustomHandlerMapping实现：</p> 
 <pre class="brush: java; gutter: true">@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class handlerType) {
		RequestMappingInfo info = createRequestMappingInfoDefault(method);
		if (info != null) {
			RequestMappingInfo typeInfo = createRequestMappingInfoDefault(handlerType);
			if (typeInfo != null)
				info = typeInfo.combine(info);
		}
		return info;
	}

	private RequestMappingInfo createRequestMappingInfoDefault(AnnotatedElement element) {
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element,
				RequestMapping.class);
		RequestCondition condition = (element instanceof Class)
				? getCustomTypeCondition((Class) element)
				: getCustomMethodCondition((Method) element);
		/**
		* 以类名和方法名映射请求，参照@RequestMapping
		* 默认不需要添加任何参数(如：/className/methodName.do)
		*/
		String defaultName = (element instanceof Class)
				? ((Class) element).getSimpleName()
				: ((Method) element).getName();
		return requestMapping == null
				? null
				: createRequestMappingInfo(requestMapping, condition, defaultName);
	}

	protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation,
			RequestCondition&lt;?&gt; customCondition, String defaultName) {
		String[] patterns = resolveEmbeddedValuesInPatterns(annotation.value());
		if (patterns != null &amp;&amp; (patterns.length == 0)) {
			patterns = new String[]{defaultName};
		}
		return new RequestMappingInfo(
				new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(),
						this.useSuffixPatternMatch, this.useTrailingSlashMatch,
						this.fileExtensions),
				new RequestMethodsRequestCondition(annotation.method()),
				new ParamsRequestCondition(annotation.params()),
				new HeadersRequestCondition(annotation.headers()),
				new ConsumesRequestCondition(annotation.consumes(), annotation.headers()),
				new ProducesRequestCondition(annotation.produces(), annotation.headers(),
						this.contentNegotiationManager),
				customCondition);
	}</pre> 
 <p>5）（必选）处理器适配器（HandlerAdapter）配置，可以配置多个，主要是配置messageConverters，其主要作用是映射前台传参与handler处理方法参数。一般扩展RequestMappingHandlerAdapter，或者自定义。如果我们需要json请求的处理，这里必须扩展。同时我们需要注意的是日期格式的转换。</p> 
 <p>另外Spring 4.2新特性，加之注解会自动注入@ControllerAdvice，可以定义RequestBodyAdvice、ResponseBodyAdvice，可以更方便地在参数处理方面着手自定义。</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="handlerAdapter"
	class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"&gt;
	&lt;property name="order" value="-1" /&gt;
	&lt;property name="messageConverters"&gt;
		&lt;list&gt;
		&lt;!-- &lt;bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter" 
			/&gt; --&gt;
			&lt;ref bean="mappingJacksonHttpMessageConverter" /&gt;
		&lt;/list&gt;
	&lt;/property&gt;
	&lt;property name="webBindingInitializer"&gt;
		&lt;bean
			class="org.springframework.web.bind.support.ConfigurableWebBindingInitializer"&gt;
			&lt;property name="conversionService"&gt;
				&lt;!-- 针对普通请求(非application/json) 前台的日期字符串与后台的Java Date对象转化,
				此情况,应使用spring 
					mvc本身的内置日期处理 --&gt;
				&lt;!-- 可以在VO属性上加注解：@DateTimeFormat 需要类库joda-time --&gt;
		&lt;bean
		class="org.springframework.format.support.FormattingConversionServiceFactoryBean"&gt;
		&lt;/bean&gt;
			&lt;/property&gt;
		&lt;/bean&gt;
	&lt;/property&gt;
&lt;/bean&gt;
&lt;!-- json请求(application/json)返回值Date转String，全局配置 --&gt;
&lt;bean name="jacksonObjectMapper"
	class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean"&gt;
	&lt;property name="featuresToDisable"&gt;
		&lt;array&gt;
		&lt;util:constant
static-field="com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS" /&gt;
		&lt;/array&gt;
	&lt;/property&gt;
	&lt;!-- 如果想自定义，可以在VO属性上加注解：@JsonFormat(shape = JsonFormat.Shape.STRING, pattern 
		= Consts.DATE_PATTERN.DATE_PATTERN_OBLIQUE,timezone = "GMT+8") --&gt;
	&lt;property name="simpleDateFormat"&gt;
		&lt;value&gt;yyyy-MM-dd HH:mm:ss&lt;/value&gt;
	&lt;/property&gt;
&lt;/bean&gt;
&lt;!--避免IE执行Ajax时，返回JSON出现下载文件 --&gt;
&lt;!-- 自定义 --&gt;
&lt;bean id="mappingJacksonHttpMessageConverter"
	class="io.flysium.framework.http.converter.json.CustomJackson2HttpMessageConverter"&gt;
	&lt;property name="objectMapper" ref="jacksonObjectMapper" /&gt;
	&lt;property name="supportedMediaTypes"&gt;
		&lt;list&gt;
			&lt;value&gt;text/html;charset=UTF-8&lt;/value&gt;
			&lt;value&gt;application/json;charset=UTF-8&lt;/value&gt;
		&lt;/list&gt;
	&lt;/property&gt;
&lt;/bean&gt;</pre> 
 <p>6）（可选）处理器异常解析器（HandlerExceptionResolver）配置，可以配置多个，配置Controller异常抛出后，我们是怎么样处理的，一般需要日志或做反馈的可以自定义。</p> 
 <p>7）（可选）请求到视图名翻译器（RequestToViewNameTranslator）配置，RequestToViewNameTranslator可以在处理器返回的View为空时使用它根据Request获得viewName。</p> 
 <p>8）（可选）视图解析器（ViewResolver）配置，可以配置多个，定义跳转的文件的前后缀 ，视图模式配置，主要针对@Controller返回ModelAndView的视图路径解析，动给后面控制器的方法return的字符串 加上前缀和后缀，变成一个 可用的url地址 。</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="viewResolver"
		class="org.springframework.web.servlet.view.InternalResourceViewResolver"&gt;
		&lt;property name="prefix" value="/" /&gt;
		&lt;property name="suffix" value=".jsp" /&gt;
		&lt;property name="viewClass"
			value="org.springframework.web.servlet.view.JstlView" /&gt;
	&lt;/bean&gt;</pre> 
 <p>最后给Controller加入组件扫描吧，这样减少xml配置，直接在Java代码中加入注解即可。</p> 
 <pre class="brush: java; gutter: true">    &lt;!-- 自动扫描类包，将标志Spring注解的类自动转化为Bean，同时完成Bean的注入 --&gt;
	&lt;!-- 扫描控制器 --&gt;
	&lt;context:component-scan base-package="io.flysium" use-default-filters="false"&gt;
		&lt;context:include-filter type="annotation" 
			expression="org.springframework.stereotype.Controller" /&gt;
		&lt;context:include-filter type="annotation" 
			expression="org.springframework.web.bind.annotation.RestController" /&gt;
		&lt;context:include-filter type="annotation" 
			expression="org.springframework.web.bind.annotation.ControllerAdvice" /&gt;
	&lt;/context:component-scan&gt;</pre> 
 <h2>3、Mybatis整合</h2> 
 <p>整合mybatis到Spring框架，我们需要mybatis的jar包，及mybatis-spring整合jar包。然后在Spring容器中注册配置org.mybatis.spring.SqlSessionFactoryBean（需要数据源，及指定Mybatis配置文件）及org.mybatis.spring.SqlSessionTemplate即可。</p> 
 <p>&nbsp;</p> 
 <p>更多整合请参照Git项目：https://git.oschina.net/svenaugustus/app-ss4m-less</p> 
 <p>目前除了ssm，另外整合redis（支持切换单节点配置、主从哨兵配置，集群配置）、spring session方案。</p> 
 <p>其中包括spring MVC的简单demo,用于学习交流。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>