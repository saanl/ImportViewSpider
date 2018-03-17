<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/hengyunabc/article/details/78762121">hengyunabc</a>
 </div> 
 <h2>写在前面</h2> 
 <p>这个demo来说明怎么排查一个常见的spring expected single matching bean but found 2的异常。</p> 
 <p>https://github.com/hengyunabc/spring-boot-inside/tree/master/demo-expected-single</p> 
 <h2>调试排查 expected single matching bean but found 2 的错误</h2> 
 <p>把工程导入IDE里，直接启动应用，抛出来的异常信息是：</p> 
 <pre class="brush: java; gutter: true">Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'javax.sql.DataSource' available: expected single matching bean but found 2: h2DataSource1,h2DataSource2
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveNamedBean(DefaultListableBeanFactory.java:1041) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:345) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:340) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.context.support.AbstractApplicationContext.getBean(AbstractApplicationContext.java:1090) ~[spring-context-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer.init(DataSourceInitializer.java:71) ~[spring-boot-autoconfigure-1.4.7.RELEASE.jar:1.4.7.RELEASE]
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_112]
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_112]
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_112]
    at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_112]
    at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(InitDestroyAnnotationBeanPostProcessor.java:366) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(InitDestroyAnnotationBeanPostProcessor.java:311) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:134) ~[spring-beans-4.3.9.RELEASE.jar:4.3.9.RELEASE]
    ... 30 common frames omitted</pre> 
 <p>很多人碰到这种错误时，就乱配置一通，找不到下手的办法。其实耐心排查下，是很简单的。</p> 
 <h2>抛出异常的原因</h2> 
 <p>异常信息写得很清楚了，在spring context里需要注入/获取到一个DataSource bean，但是现在spring context里出现了两个，它们的名字是：h2DataSource1,h2DataSource2</p> 
 <p>那么有两个问题：</p> 
 <ol> 
  <li>应用是在哪里要注入/获取到一个DataSource bean？</li> 
  <li>h2DataSource1,h2DataSource2 是在哪里定义的？</li> 
 </ol> 
 <h2>使用 Java Exception Breakpoint</h2> 
 <p>在IDE里，新建一个断点，类型是Java Exception Breakpoint（如果不清楚怎么添加，可以搜索对应IDE的使用文档），异常类是上面抛出来的NoUniqueBeanDefinitionException。</p> 
 <p>当断点停住时，查看栈，可以很清楚地找到是在DataSourceInitializer.init() line: 71这里要获取DataSource：</p> 
 <pre class="brush: java; gutter: true">Thread [main] (Suspended (exception NoUniqueBeanDefinitionException))
    owns: ConcurrentHashMap&lt;K,V&gt;  (id=49)
    owns: Object  (id=50)
    DefaultListableBeanFactory.resolveNamedBean(Class&lt;T&gt;, Object...) line: 1041
    DefaultListableBeanFactory.getBean(Class&lt;T&gt;, Object...) line: 345
    DefaultListableBeanFactory.getBean(Class&lt;T&gt;) line: 340
    AnnotationConfigEmbeddedWebApplicationContext(AbstractApplicationContext).getBean(Class&lt;T&gt;) line: 1090
    DataSourceInitializer.init() line: 71
    NativeMethodAccessorImpl.invoke0(Method, Object, Object[]) line: not available [native method]
    NativeMethodAccessorImpl.invoke(Object, Object[]) line: 62
    DelegatingMethodAccessorImpl.invoke(Object, Object[]) line: 43
    Method.invoke(Object, Object...) line: 498
    InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(Object) line: 366
    InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(Object, String) line: 311
    CommonAnnotationBeanPostProcessor(InitDestroyAnnotationBeanPostProcessor).postProcessBeforeInitialization(Object, String) line: 134
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).applyBeanPostProcessorsBeforeInitialization(Object, String) line: 409
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).initializeBean(String, Object, RootBeanDefinition) line: 1620
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).doCreateBean(String, RootBeanDefinition, Object[]) line: 555
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).createBean(String, RootBeanDefinition, Object[]) line: 483
    AbstractBeanFactory$1.getObject() line: 306
    DefaultListableBeanFactory(DefaultSingletonBeanRegistry).getSingleton(String, ObjectFactory&lt;?&gt;) line: 230
    DefaultListableBeanFactory(AbstractBeanFactory).doGetBean(String, Class&lt;T&gt;, Object[], boolean) line: 302
    DefaultListableBeanFactory(AbstractBeanFactory).getBean(String, Class&lt;T&gt;, Object...) line: 220
    DefaultListableBeanFactory.resolveNamedBean(Class&lt;T&gt;, Object...) line: 1018
    DefaultListableBeanFactory.getBean(Class&lt;T&gt;, Object...) line: 345
    DefaultListableBeanFactory.getBean(Class&lt;T&gt;) line: 340
    DataSourceInitializerPostProcessor.postProcessAfterInitialization(Object, String) line: 62
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).applyBeanPostProcessorsAfterInitialization(Object, String) line: 423
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).initializeBean(String, Object, RootBeanDefinition) line: 1633
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).doCreateBean(String, RootBeanDefinition, Object[]) line: 555
    DefaultListableBeanFactory(AbstractAutowireCapableBeanFactory).createBean(String, RootBeanDefinition, Object[]) line: 483
    AbstractBeanFactory$1.getObject() line: 306
    DefaultListableBeanFactory(DefaultSingletonBeanRegistry).getSingleton(String, ObjectFactory&lt;?&gt;) line: 230
    DefaultListableBeanFactory(AbstractBeanFactory).doGetBean(String, Class&lt;T&gt;, Object[], boolean) line: 302
    DefaultListableBeanFactory(AbstractBeanFactory).getBean(String) line: 197
    DefaultListableBeanFactory.preInstantiateSingletons() line: 761
    AnnotationConfigEmbeddedWebApplicationContext(AbstractApplicationContext).finishBeanFactoryInitialization(ConfigurableListableBeanFactory) line: 867
    AnnotationConfigEmbeddedWebApplicationContext(AbstractApplicationContext).refresh() line: 543
    AnnotationConfigEmbeddedWebApplicationContext(EmbeddedWebApplicationContext).refresh() line: 122
    SpringApplication.refresh(ApplicationContext) line: 762
    SpringApplication.refreshContext(ConfigurableApplicationContext) line: 372
    SpringApplication.run(String...) line: 316
    SpringApplication.run(Object[], String[]) line: 1187
    SpringApplication.run(Object, String...) line: 1176
    DemoExpectedSingleApplication.main(String[]) line: 17</pre> 
 <h3>定位哪里要注入/使用DataSource</h3> 
 <p>要获取DataSource具体的代码是：</p> 
 <pre class="brush: java; gutter: true">//org.springframework.boot.autoconfigure.jdbc.DataSourceInitializer.init()
    @PostConstruct
    public void init() {
        if (!this.properties.isInitialize()) {
            logger.debug("Initialization disabled (not running DDL scripts)");
            return;
        }
        if (this.applicationContext.getBeanNamesForType(DataSource.class, false,
                false).length &gt; 0) {
            this.dataSource = this.applicationContext.getBean(DataSource.class);
        }
        if (this.dataSource == null) {
            logger.debug("No DataSource found so not initializing");
            return;
        }
        runSchemaScripts();
    }</pre> 
 <p>this.applicationContext.getBean(DataSource.class); 要求spring context里只有一个DataSource的bean，但是应用里有两个，所以抛出了NoUniqueBeanDefinitionException。</p> 
 <h3>从BeanDefinition获取bean具体定义的代码</h3> 
 <p>我们再来看 h2DataSource1,h2DataSource2 是在哪里定义的？</p> 
 <p>上面进程断在了DefaultListableBeanFactory.resolveNamedBean(Class&lt;T&gt;, Object…) 函数里的 throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet()); 这一行。</p> 
 <p>那么我们在这里执行一下（如果不清楚，先搜索下IDE怎么在断点情况下执行代码）：</p> 
 <pre class="brush: java; gutter: true">this.getBeanDefinition("h2DataSource1")</pre> 
 <p>返回的信息是：</p> 
 <pre class="brush: java; gutter: true">Root bean: class [null]; scope=; abstract=false; lazyInit=false; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=demoExpectedSingleApplication; factoryMethodName=h2DataSource1; initMethodName=null; destroyMethodName=(inferred);
defined in com.example.demo.expected.single.DemoExpectedSingleApplication</pre> 
 <p>可以很清楚地定位到h2DataSource1这个bean是在 com.example.demo.expected.single.DemoExpectedSingleApplication里定义的。</p> 
 <p>所以上面两个问题的答案是：</p> 
 <ol> 
  <li>是spring boot代码里的DataSourceInitializer.init() line: 71这里要获取DataSource，并且只允许有一个DataSource实例</li> 
  <li>h2DataSource1,h2DataSource2 是在com.example.demo.expected.single.DemoExpectedSingleApplication里定义的</li> 
 </ol> 
 <h2>解决问题</h2> 
 <p>上面排查到的原因是：应用定义了两个DataSource实例，但是spring boot却要求只有一个。那么有两种办法来解决：</p> 
 <ol> 
  <li>使用@Primary来指定一个优先使用的DataSource，这样子spring boot里自动初始的代码会获取到@Primary的bean</li> 
  <li>把spring boot自动初始化DataSource相关的代码禁止掉，应用自己来控制所有的DataSource相关的bean</li> 
 </ol> 
 <p>禁止的办法有两种：</p> 
 <p>在main函数上配置exclude</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })</pre> 
 <p>在application.properties里配置：</p> 
 <pre class="brush: java; gutter: true">spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration</pre> 
 <h2>总结</h2> 
 <ul> 
  <li>排查spring初始化问题时，灵活使用Java Exception Breakpoint</li> 
  <li>从异常栈上，可以很容易找到哪里要注入/使用bean</li> 
  <li>从BeanDefinition可以找到bean是在哪里定义的（哪个Configuration类/xml）</li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>