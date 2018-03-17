<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/hengyunabc/article/details/75453307">hengyunabc</a>
 </div> 
 <h2>Spring里的占位符</h2> 
 <p>spring里的占位符通常表现的形式是：</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="dataSource" destroy-method="close" class="org.apache.commons.dbcp.BasicDataSource"&gt;
&lt;property name="url" value="${jdbc.url}"/&gt;
&lt;/bean&gt;</pre> 
 <p>或者</p> 
 <pre class="brush: java; gutter: true">@Configuration
@ImportResource("classpath:/com/acme/properties-config.xml")
public class AppConfig {
    @Value("${jdbc.url}")
    private String url;
}</pre> 
 <p>Spring应用在有时会出现占位符配置没有注入，原因可能是多样的。</p> 
 <p>本文介绍两种比较复杂的情况。</p> 
 <h2>占位符是在Spring生命周期的什么时候处理的</h2> 
 <p>Spirng在生命周期里关于Bean的处理大概可以分为下面几步：</p> 
 <ol> 
  <li>加载Bean定义（从xml或者从@Import等）</li> 
  <li>处理BeanFactoryPostProcessor</li> 
  <li>实例化Bean</li> 
  <li>处理Bean的property注入</li> 
  <li>处理BeanPostProcessor</li> 
 </ol> 
 <p>当然这只是比较理想的状态，实际上因为Spring Context在构造时，也需要创建很多内部的Bean，应用在接口实现里也会做自己的各种逻辑，整个流程会非常复杂。</p> 
 <p>那么占位符（${}表达式）是在什么时候被处理的？</p> 
 <ul> 
  <li>实际上是在org.springframework.context.support.PropertySourcesPlaceholderConfigurer里处理的，它会访问了每一个bean的BeanDefinition，然后做占位符的处理</li> 
  <li>PropertySourcesPlaceholderConfigurer实现了BeanFactoryPostProcessor接口</li> 
  <li>PropertySourcesPlaceholderConfigurer的 order是Ordered.LOWEST_PRECEDENCE，也就是最低优先级的</li> 
 </ul> 
 <p>结合上面的Spring的生命周期，如果Bean的创建和使用在PropertySourcesPlaceholderConfigurer之前，那么就有可能出现占位符没有被处理的情况。</p> 
 <h2>例子1：Mybatis 的 MapperScannerConfigurer引起的占位符没有处理</h2> 
 <p>例子代码：<a href="https://github.com/hengyunabc/hengyunabc.github.io/files/1158339/mybatis-demo.zip" class="external" rel="nofollow" target="_blank">mybatis-demo.zip</a></p> 
 <ul> 
  <li>首先应用自己在代码里创建了一个DataSource，其中${db.user}是希望从application.properties里注入的。代码在运行时会打印出user的实际值。</li> 
 </ul> 
 <pre class="brush: java; gutter: true">@Configuration
public class MyDataSourceConfig {
    @Bean(name = "dataSource1")
    public DataSource dataSource1(@Value("${db.user}") String user) {
        System.err.println("user: " + user);
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:˜/test");
        ds.setUser(user);
        return ds;
    }
}</pre> 
 <ul> 
  <li>然后应用用代码的方式来初始化mybatis相关的配置，依赖上面创建的DataSource对象</li> 
 </ul> 
 <pre class="brush: java; gutter: true">@Configuration
public class MybatisConfig1 {

    @Bean(name = "sqlSessionFactory1")
    public SqlSessionFactory sqlSessionFactory1(DataSource dataSource1) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        org.apache.ibatis.session.Configuration ibatisConfiguration = new org.apache.ibatis.session.Configuration();
        sqlSessionFactoryBean.setConfiguration(ibatisConfiguration);

        sqlSessionFactoryBean.setDataSource(dataSource1);
        sqlSessionFactoryBean.setTypeAliasesPackage("sample.mybatis.domain");
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    MapperScannerConfigurer mapperScannerConfigurer(SqlSessionFactory sqlSessionFactory1) {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory1");
        mapperScannerConfigurer.setBasePackage("sample.mybatis.mapper");
        return mapperScannerConfigurer;
    }
}</pre> 
 <p>当代码运行时，输出结果是：</p> 
 <pre class="brush: java; gutter: true">user: ${db.user}</pre> 
 <p>为什么会user这个变量没有被注入？</p> 
 <p>分析下Bean定义，可以发现MapperScannerConfigurer它实现了BeanDefinitionRegistryPostProcessor。这个接口在是Spring扫描Bean定义时会回调的，远早于BeanFactoryPostProcessor。</p> 
 <p>所以原因是：</p> 
 <ul> 
  <li>MapperScannerConfigurer它实现了BeanDefinitionRegistryPostProcessor，所以它会Spring的早期会被创建</li> 
  <li>从bean的依赖关系来看，mapperScannerConfigurer依赖了sqlSessionFactory1，sqlSessionFactory1依赖了dataSource1</li> 
  <li>MyDataSourceConfig里的dataSource1被提前初始化，没有经过PropertySourcesPlaceholderConfigurer的处理，所以@Value(“${db.user}”) String user 里的占位符没有被处理</li> 
 </ul> 
 <p>要解决这个问题，可以在代码里，显式来处理占位符：</p> 
 <pre class="brush: java; gutter: true">environment.resolvePlaceholders("${db.user}")</pre> 
 <h2>例子2：Spring boot自身实现问题，导致Bean被提前初始化</h2> 
 <p>例子代码：<a href="https://github.com/spring-projects/spring-boot/files/773587/demo.zip" class="external" rel="nofollow" target="_blank">demo.zip</a></p> 
 <p>Spring Boot里提供了@ConditionalOnBean，这个方便用户在不同条件下来创建bean。里面提供了判断是否存在bean上有某个注解的功能。</p> 
 <pre class="brush: java; gutter: true">@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {
    /**
     * The annotation type decorating a bean that should be checked. The condition matches
     * when any of the annotations specified is defined on a bean in the
     * {@link ApplicationContext}.
     * @return the class-level annotation types to check
     */
    Class&lt;? extends Annotation&gt;[] annotation() default {};</pre> 
 <p>比如用户自己定义了一个Annotation：</p> 
 <pre class="brush: java; gutter: true">@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {
}</pre> 
 <p>然后用下面的写法来创建abc这个bean，意思是当用户显式使用了@MyAnnotation（比如放在main class上），才会创建这个bean。</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class MyAutoConfiguration {
    @Bean
    // if comment this line, it will be fine.
    @ConditionalOnBean(annotation = { MyAnnotation.class })
    public String abc() {
        return "abc";
    }
}</pre> 
 <p>这个功能很好，但是在spring boot 1.4.5 版本之前都有问题，会导致FactoryBean提前初始化。</p> 
 <p>在例子里，通过xml创建了javaVersion这个bean，想获取到java的版本号。这里使用的是spring提供的一个调用static函数创建bean的技巧。</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="sysProps" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
  &lt;property name="targetClass" value="java.lang.System" /&gt;
  &lt;property name="targetMethod" value="getProperties" /&gt;
&lt;/bean&gt;

&lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean"&gt;
  &lt;property name="targetObject" ref="sysProps" /&gt;
  &lt;property name="targetMethod" value="getProperty" /&gt;
  &lt;property name="arguments" value="${java.version.key}" /&gt;
&lt;/bean&gt;</pre> 
 <p>我们在代码里获取到这个javaVersion，然后打印出来：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication
@ImportResource("classpath:/demo.xml")
public class DemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        System.err.println(context.getBean("javaVersion"));
    }
}</pre> 
 <p>在实际运行时，发现javaVersion的值是null。</p> 
 <p>这个其实是spring boot的锅，要搞清楚这个问题，先要看@ConditionalOnBean的实现。</p> 
 <ul> 
  <li>@ConditionalOnBean实际上是在ConfigurationClassPostProcessor里被处理的，它实现了BeanDefinitionRegistryPostProcessor</li> 
  <li>BeanDefinitionRegistryPostProcessor是在spring早期被处理的</li> 
  <li>@ConditionalOnBean的具体处理代码在org.springframework.boot.autoconfigure.condition.OnBeanCondition里</li> 
  <li>OnBeanCondition在获取bean的Annotation时，调用了beanFactory.getBeanNamesForAnnotation</li> 
 </ul> 
 <pre class="brush: java; gutter: true">private String[] getBeanNamesForAnnotation(
    ConfigurableListableBeanFactory beanFactory, String type,
    ClassLoader classLoader, boolean considerHierarchy) throws LinkageError {
  String[] result = NO_BEANS;
  try {
    @SuppressWarnings("unchecked")
    Class&lt;? extends Annotation&gt; typeClass = (Class&lt;? extends Annotation&gt;) ClassUtils
        .forName(type, classLoader);
    result = beanFactory.getBeanNamesForAnnotation(typeClass);</pre> 
 <ul> 
  <li>beanFactory.getBeanNamesForAnnotation 会导致FactoryBean提前初始化，创建出javaVersion里，传入的${java.version.key}没有被处理，值为null。</li> 
  <li>spring boot 1.4.5 修复了这个问题：https://github.com/spring-projects/spring-boot/issues/8269</li> 
 </ul> 
 <h2>实现spring boot starter要注意不能导致bean提前初始化</h2> 
 <p>用户在实现spring boot starter时，通常会实现Spring的一些接口，比如BeanFactoryPostProcessor接口，在处理时，要注意不能调用类似beanFactory.getBeansOfType，beanFactory.getBeanNamesForAnnotation 这些函数，因为会导致一些bean提前初始化。</p> 
 <p>而上面有提到PropertySourcesPlaceholderConfigurer的order是最低优先级的，所以用户自己实现的BeanFactoryPostProcessor接口在被回调时很有可能占位符还没有被处理。</p> 
 <p>对于用户自己定义的@ConfigurationProperties对象的注入，可以用类似下面的代码：</p> 
 <pre class="brush: java; gutter: true">@ConfigurationProperties(prefix = "spring.my")
public class MyProperties {
    String key;
}</pre> 
 <pre class="brush: java; gutter: true">public static MyProperties buildMyProperties(ConfigurableEnvironment environment) {
  MyProperties myProperties = new MyProperties();

  if (environment != null) {
    MutablePropertySources propertySources = environment.getPropertySources();
    new RelaxedDataBinder(myProperties, "spring.my").bind(new PropertySourcesPropertyValues(propertySources));
  }

  return myProperties;
}</pre> 
 <h2>总结</h2> 
 <ul> 
  <li>占位符（${}表达式）是在PropertySourcesPlaceholderConfigurer里处理的，也就是BeanFactoryPostProcessor接口</li> 
  <li>spring的生命周期是比较复杂的事情，在实现了一些早期的接口时要小心，不能导致spring bean提前初始化</li> 
  <li>在早期的接口实现里，如果想要处理占位符，可以利用spring自身的api，比如 environment.resolvePlaceholders(“${db.user}”)</li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>