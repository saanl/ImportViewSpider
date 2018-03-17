<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/hengyunabc/article/details/77458765">hengyunabc</a>
 </div> 
 <h2>前言</h2> 
 <p>对于一个简单的Spring boot应用，它的spring context是只会有一个。</p> 
 <ul> 
  <li>非web spring boot应用，context是AnnotationConfigApplicationContext</li> 
  <li>web spring boot应用，context是AnnotationConfigEmbeddedWebApplicationContext</li> 
 </ul> 
 <p>AnnotationConfigEmbeddedWebApplicationContext是spring boot里自己实现的一个context，主要功能是启动embedded servlet container，比如tomcat/jetty。</p> 
 <p>这个和传统的war包应用不一样，传统的war包应用有两个spring context。参考：http://hengyunabc.github.io/something-about-spring-mvc-webapplicationcontext/</p> 
 <p>但是对于一个复杂点的spring boot应用，它的spring context可能会是多个，下面分析下各种情况。</p> 
 <h2>Demo</h2> 
 <p>这个Demo展示不同情况下的spring boot context的继承情况。</p> 
 <p>https://github.com/hengyunabc/spring-boot-inside/tree/master/demo-classloader-context</p> 
 <h2>配置spring boot actuator/endpoints独立端口时</h2> 
 <p>spring boot actuator默认情况下和应用共用一个tomcat，这样子的话就会直接把应用的endpoints暴露出去，带来很大的安全隐患。</p> 
 <p>尽管 Spring boot后面默认把这个关掉，需要配置management.security.enabled=false才可以访问，但是这个还是太危险了。</p> 
 <p>所以通常都建议把endpoints开在另外一个独立的端口上，比如 management.port=8081。</p> 
 <p>可以增加-Dspring.cloud.bootstrap.enabled=false，来禁止spring cloud，然后启动Demo。比如</p> 
 <pre class="brush: java; gutter: true">mvn spring-boot:run -Dspring.cloud.bootstrap.enabled=false</pre> 
 <p>然后打开 http://localhost:8080/ 可以看到应用的spring context继承结构。</p> 
 <p>打开 http://localhost:8081/contexttree 可以看到Management Spring Contex的继承结构。</p> 
 <ul> 
  <li>可以看到当配置management独立端口时，management context的parent是应用的spring context</li> 
  <li>相关的实现代码在 org.springframework.boot.actuate.autoconfigure.EndpointWebMvcAutoConfiguration 里</li> 
 </ul> 
 <h2>在sprig cloud环境下spring context的情况</h2> 
 <p>在有spring cloud时（通常是引入 spring-cloud-starter），因为spring cloud有自己的一套配置初始化机制，所以它实际上是自己启动了一个Spring context，并把自己置为应用的context的parent。</p> 
 <p>spring cloud context的启动代码在org.springframework.cloud.bootstrap.BootstrapApplicationListener里。</p> 
 <p>spring cloud context实际上是一个特殊的spring boot context，它只扫描BootstrapConfiguration。</p> 
 <pre class="brush: java; gutter: true">ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
// Use names and ensure unique to protect against duplicates
List&lt;String&gt; names = SpringFactoriesLoader
    .loadFactoryNames(BootstrapConfiguration.class, classLoader);
for (String name : StringUtils.commaDelimitedListToStringArray(
    environment.getProperty("spring.cloud.bootstrap.sources", ""))) {
  names.add(name);
}
// TODO: is it possible or sensible to share a ResourceLoader?
SpringApplicationBuilder builder = new SpringApplicationBuilder()
    .profiles(environment.getActiveProfiles()).bannerMode(Mode.OFF)
    .environment(bootstrapEnvironment)
    .properties("spring.application.name:" + configName)
    .registerShutdownHook(false).logStartupInfo(false).web(false);
List&lt;Class&lt;?&gt;&gt; sources = new ArrayList&lt;&gt;();</pre> 
 <p>最终会把这个ParentContextApplicationContextInitializer加到应用的spring context里，来把自己设置为应用的context的parent。</p> 
 <pre class="brush: java; gutter: true">public class ParentContextApplicationContextInitializer implements
        ApplicationContextInitializer&lt;ConfigurableApplicationContext&gt;, Ordered {
    private int order = Ordered.HIGHEST_PRECEDENCE;
    private final ApplicationContext parent;
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (applicationContext != this.parent) {
            applicationContext.setParent(this.parent);
            applicationContext.addApplicationListener(EventPublisher.INSTANCE);
        }
    }</pre> 
 <p>和上面一样，直接启动demo，不要配置-Dspring.cloud.bootstrap.enabled=false，然后访问对应的url，就可以看到spring context的继承情况。</p> 
 <h2>如何在应用代码里获取到 Management Spring Context</h2> 
 <p>如果应用代码想获取到Management Spring Context，可以通过这个bean：org.springframework.boot.actuate.autoconfigure.ManagementContextResolver</p> 
 <p>spring boot在创建Management Spring Context时，就会保存到ManagementContextResolver里。</p> 
 <pre class="brush: java; gutter: true">@Configuration
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@ConditionalOnWebApplication
@AutoConfigureAfter({ PropertyPlaceholderAutoConfiguration.class,
        EmbeddedServletContainerAutoConfiguration.class, WebMvcAutoConfiguration.class,
        ManagementServerPropertiesAutoConfiguration.class,
        RepositoryRestMvcAutoConfiguration.class, HypermediaAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class })
public class EndpointWebMvcAutoConfiguration
        implements ApplicationContextAware, BeanFactoryAware, SmartInitializingSingleton {
      @Bean
        public ManagementContextResolver managementContextResolver() {
            return new ManagementContextResolver(this.applicationContext);
        }

        @Bean
        public ManagementServletContext managementServletContext(
                final ManagementServerProperties properties) {
            return new ManagementServletContext() {

                @Override
                public String getContextPath() {
                    return properties.getContextPath();
                }

            };
        }</pre> 
 <h2>如何在Endpoints代码里获取应用的Spring context</h2> 
 <p>spring boot本身没有提供方法，应用可以自己写一个@Configuration，保存应用的Spring context，然后在endpoints代码里再取出来。</p> 
 <h2>ApplicationContext.setParent(ApplicationContext) 到底发生了什么</h2> 
 <p>从spring的代码就可以看出来，主要是把parent的environment里的propertySources加到child里。这也就是spring cloud config可以生效的原因。</p> 
 <pre class="brush: java; gutter: true">// org.springframework.context.support.AbstractApplicationContext.setParent(ApplicationContext)
/**
 * Set the parent of this application context.
 * &lt;p&gt;The parent {@linkplain ApplicationContext#getEnvironment() environment} is
 * {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) merged} with
 * this (child) application context environment if the parent is non-{@code null} and
 * its environment is an instance of {@link ConfigurableEnvironment}.
 * @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
 */
@Override
public void setParent(ApplicationContext parent) {
  this.parent = parent;
  if (parent != null) {
    Environment parentEnvironment = parent.getEnvironment();
    if (parentEnvironment instanceof ConfigurableEnvironment) {
      getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
    }
  }
}</pre> 
 <pre class="brush: java; gutter: true">// org.springframework.core.env.AbstractEnvironment.merge(ConfigurableEnvironment)

@Override
public void merge(ConfigurableEnvironment parent) {
  for (PropertySource&lt;?&gt; ps : parent.getPropertySources()) {
    if (!this.propertySources.contains(ps.getName())) {
      this.propertySources.addLast(ps);
    }
  }
  String[] parentActiveProfiles = parent.getActiveProfiles();
  if (!ObjectUtils.isEmpty(parentActiveProfiles)) {
    synchronized (this.activeProfiles) {
      for (String profile : parentActiveProfiles) {
        this.activeProfiles.add(profile);
      }
    }
  }
  String[] parentDefaultProfiles = parent.getDefaultProfiles();
  if (!ObjectUtils.isEmpty(parentDefaultProfiles)) {
    synchronized (this.defaultProfiles) {
      this.defaultProfiles.remove(RESERVED_DEFAULT_PROFILE_NAME);
      for (String profile : parentDefaultProfiles) {
        this.defaultProfiles.add(profile);
      }
    }
  }
}</pre> 
 <h2>总结</h2> 
 <ul> 
  <li>当配置management.port 为独立端口时，Management Spring Context也会是独立的context，它的parent是应用的spring context</li> 
  <li>当启动spring cloud时，spring cloud自己会创建出一个spring context，并置为应用的context的parent</li> 
  <li>ApplicationContext.setParent(ApplicationContext) 主要是把parent的environment里的propertySources加到child里</li> 
  <li>理解的spring boot context的继承关系，能避免一些微妙的spring bean注入的问题，还有不当的spring context的问题</li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>