<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://javadoop.com/post/spring-properties">JavaDoop</a>
 </div> 
 <p>对 Spring 里面的 Properties 不理解的开发者可能会觉得有点乱，主要是因为配置方式很多种，使用方式也很多种。</p> 
 <p>本文不是原理分析、源码分析文章，只是希望可以帮助读者更好地理解和使用 Spring Properties。</p> 
 <h2 id="Properties%20%E7%9A%84%E4%BD%BF%E7%94%A8">Properties 的使用</h2> 
 <p>本文的读者都是使用过 Spring 的，先来看看 Properties 是怎么使用的，Spring 中常用的有以下几种使用方式：</p> 
 <h3 id="1.%20%E5%9C%A8%20xml%20%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6%E4%B8%AD%E4%BD%BF%E7%94%A8">1. 在 xml 配置文件中使用</h3> 
 <p>即自动替换&nbsp;<code>${}</code>&nbsp;里面的值。</p> 
 <pre class="brush: java; gutter: true">&lt;bean id="xxx" class="com.javadoop.Xxx"&gt;
      &lt;property name="url" value="${javadoop.jdbc.url}" /&gt;
&lt;/bean&gt;</pre> 
 <h3 id="2.%20%E9%80%9A%E8%BF%87%20%40Value%20%E6%B3%A8%E5%85%A5%E4%BD%BF%E7%94%A8">2. 通过 @Value 注入使用</h3> 
 <pre class="brush: java; gutter: true">@Value("${javadoop.jdbc.url}")
private String url;</pre> 
 <h3 id="3.%20%E9%80%9A%E8%BF%87%20Environment%20%E8%8E%B7%E5%8F%96">3. 通过 Environment 获取</h3> 
 <p>此法有需要注意的地方。并不是所有的配置方式都支持通过 Environment 接口来获取属性值，亲测只有使用注解 @PropertySource 的时候可以用，否则会得到&nbsp;null，至于怎么配置，下面马上就会说。</p> 
 <pre class="brush: java; gutter: true">@Autowired
private Environment env;

public String getUrl() {
    return env.getProperty("javadoop.jdbc.url");
}</pre> 
 <blockquote>
  <p>如果是 Spring Boot 的 application.properties 注册的，那也是可以的。</p>
 </blockquote> 
 <h2 id="Properties%20%E9%85%8D%E7%BD%AE">Properties 配置</h2> 
 <p>前面我们说了怎么使用我们配置的 Properties，那么该怎么配置呢？Spring 提供了很多种配置方式。</p> 
 <h3 id="1.%20%E9%80%9A%E8%BF%87%20xml%20%E9%85%8D%E7%BD%AE">1. 通过 xml 配置</h3> 
 <p>下面这个是最常用的配置方式了，很多项目都是这么写的：</p> 
 <pre class="brush: java; gutter: true">&lt;context:property-placeholder location="classpath:sys.properties" /&gt;</pre> 
 <h3 id="2.%20%E9%80%9A%E8%BF%87%20%40PropertySource%20%E9%85%8D%E7%BD%AE">2. 通过 @PropertySource 配置</h3> 
 <p>前面的通过 xml 配置非常常用，但是如果你也有一种要消灭所有 xml 配置文件的冲动的话，你应该使用以下方式：</p> 
 <pre class="brush: java; gutter: true">@PropertySource("classpath:sys.properties")
@Configuration
public class JavaDoopConfig {

}</pre> 
 <p>注意一点，@PropertySource 在这里必须搭配 @Configuration 来使用，具体不展开说了。</p> 
 <h3 id="3.%20PropertyPlaceholderConfigurer">3. PropertyPlaceholderConfigurer</h3> 
 <p>如果读者见过这个，也不必觉得奇怪，在 Spring 3.1 之前，经常就是这么使用的：</p> 
 <pre class="brush: java; gutter: true">&lt;bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"&gt;
    &lt;property name="locations"&gt;
        &lt;list&gt;
            &lt;value&gt;classpath:sys.properties&lt;/value&gt;
        &lt;/list&gt;
    &lt;/property&gt;
    &lt;property name="ignoreUnresolvablePlaceholders" value="true"/&gt;
      &lt;!-- 这里可以配置一些属性 --&gt;
&lt;/bean&gt;</pre> 
 <p>当然，我们也可以用相应的 java configuration 的版本：</p> 
 <pre class="brush: java; gutter: true">@Bean
public PropertyPlaceholderConfigurer propertiess() {
    PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
    Resource[] resources = new ClassPathResource[]{new ClassPathResource("sys.properties")};
    ppc.setLocations(resources);
    ppc.setIgnoreUnresolvablePlaceholders(true);
    return ppc;
}</pre> 
 <h3 id="4.%20PropertySourcesPlaceholderConfigurer">4. PropertySourcesPlaceholderConfigurer</h3> 
 <p>到了 Spring 3.1 的时候，引入了&nbsp;PropertySourcesPlaceholderConfigurer，这是一个新的类，注意看和之前的 PropertyPlaceholderConfigurer 在名字上多了一个&nbsp;Sources，所属的包也不一样，它在 Spring-Context 包中。</p> 
 <p>在配置上倒是没有什么区别：</p> 
 <pre class="brush: java; gutter: true">&lt;bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer"&gt;
    &lt;property name="locations"&gt;
        &lt;list&gt;
            &lt;value&gt;classpath:sys.properties&lt;/value&gt;
        &lt;/list&gt;
    &lt;/property&gt;
    &lt;property name="ignoreUnresolvablePlaceholders" value="true"/&gt;
    &lt;!-- 这里可以配置一些属性 --&gt;
&lt;/bean&gt;</pre> 
 <p>也来一个 java configuration 版本吧：</p> 
 <pre class="brush: java; gutter: true">@Bean
public PropertySourcesPlaceholderConfigurer properties() {
    PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
    Resource[] resources = new ClassPathResource[]{new ClassPathResource("sys.properties")};
    pspc.setLocations(resources);
    pspc.setIgnoreUnresolvablePlaceholders(true);
    return pspc;
}</pre> 
 <h2 id="Spring%20Boot%20%E7%9B%B8%E5%85%B3">Spring Boot 相关</h2> 
 <p>Spring Boot 真的是好东西，开箱即用的感觉实在是太好了。这里简单介绍下相关的内容。</p> 
 <p>快速生成一个 Spring Boot 项目：<a href="https://start.spring.io/" class="external" rel="nofollow" target="_blank">https://start.spring.io/</a></p> 
 <h3 id="application.properties">application.properties</h3> 
 <p>我们每个项目都默认有一个 application.properties 文件，这个配置文件不需要像前面说的那样进行<em>注册</em>，Spring Boot 会帮我们自动注册。</p> 
 <p>当然，也许你想换个名字也是可以的，在启动的时候指定你的文件名字就可以了：</p> 
 <pre class="brush: java; gutter: true">java -Dspring.config.location=classpath:sys.properties -jar app.jar</pre> 
 <h3 id="application-%7Benv%7D.properties">application-{env}.properties</h3> 
 <p>为了给不同的环境指定不同的配置，我们会用到这个。</p> 
 <p>比如测试环境和生产环境的数据库连接信息就不一样。</p> 
 <p>所以，在 application.properties 的基础上，我们还需要新建 application-dev.properties 和 application-prd.properties，用于配置环境相关的信息，然后启动的时候指定环境。</p> 
 <pre class="brush: java; gutter: true">java -Dspring.profiles.active=prd -jar app.jar</pre> 
 <p>结果就是，application.properties 和 application-prd.properties 两个文件中的配置都会注册进去，如果有重复的 key，application-prd.properties 文件中的优先级较高。</p> 
 <h3 id="%40ConfigurationProperties">@ConfigurationProperties</h3> 
 <p>这个注解是 Spring Boot 中才有的。</p> 
 <p>即使大家不使用这个注解，大家也可能会在开源项目中看到这个，这里简单介绍下。</p> 
 <p>来一个例子直观一些。按照之前说的，在配置文件中填入下面的信息，你可以选择写入 application.properties 也可以用第一节介绍的方法。</p> 
 <pre class="brush: java; gutter: true">javadoop.database.url=jdbc:mysql:
javadoop.database.username=admin
javadoop.database.password=admin123456</pre> 
 <p>java 文件：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@ConfigurationProperties(prefix = "javadoop.database")
public class DataBase {
    String url;
    String username;
    String password;
    // getters and setters
}</pre> 
 <p>这样，就在 Spring 的容器中就自动注册了一个类型为 DataBase 的 bean 了，而且属性都已经 set 好了。</p> 
 <h3 id="%E5%9C%A8%E5%90%AF%E5%8A%A8%E8%BF%87%E7%A8%8B%E4%B8%AD%E5%8A%A8%E6%80%81%E4%BF%AE%E6%94%B9%E5%B1%9E%E6%80%A7%E5%80%BC">在启动过程中动态修改属性值</h3> 
 <p>这个我觉得都不需要太多介绍，用 Spring Boot 的应该基本上都知道。</p> 
 <p>属性配置有个覆盖顺序，也就是当出现相同的 key 的时候，以哪里的值为准。</p> 
 <p>启动参数 &gt; application-{env}.properties &gt; application.properties</p> 
 <p>启动参数动态设置属性：</p> 
 <pre class="brush: java; gutter: true">java -Djavadoop.database.password=admin4321 -jar app.jar</pre> 
 <p>另外，还可以利用系统环境变量设置属性，还可以指定随机数等等，确实很灵活，不过没什么用，就不介绍了。</p> 
 <h2 id="%E6%80%BB%E7%BB%93">总结</h2> 
 <p>读者如果想要更加深入地了解 Spring 的 Properties，需要去理解 Spring 的 Environment 接口相关的源码。建议感兴趣的读者去翻翻源代码看看。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>