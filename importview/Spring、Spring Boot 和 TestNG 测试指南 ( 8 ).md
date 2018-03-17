<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples/blob/master/chapter_7_configuration.md">chanjarster</a>
 </div> 
 <p>在Spring引入Java Config机制之后，我们会越来越多的使用@Configuration来注册Bean，并且Spring Boot更广泛地使用了这一机制，其提供的大量Auto Configuration大大简化了配置工作。那么问题来了，如何确保@Configuration和Auto Configuration按照预期运行呢，是否正确地注册了Bean呢？本章举例测试@Configuration和Auto Configuration的方法（因为Auto Configuration也是@Configuration，所以测试方法是一样的）。</p> 
 <h2>例子1：测试@Configuration</h2> 
 <p>我们先写一个简单的@Configuration：</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class FooConfiguration {

  @Bean
  public Foo foo() {
    return new Foo();
  }

}</pre> 
 <p>然后看FooConfiguration是否能够正确地注册Bean：</p> 
 <pre class="brush: java; gutter: true">public class FooConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeMethod
  public void init() {
    context = new AnnotationConfigApplicationContext();
  }

  @AfterMethod(alwaysRun = true)
  public void reset() {
    context.close();
  }

  @Test
  public void testFooCreation() {
    context.register(FooConfiguration.class);
    context.refresh();
    assertNotNull(context.getBean(Foo.class));
  }

}</pre> 
 <p>注意上面代码中关于Context的代码：</p> 
 <ol> 
  <li>首先，我们构造一个Context</li> 
  <li>然后，注册FooConfiguration</li> 
  <li>然后，refresh Context</li> 
  <li>最后，在测试方法结尾close Context</li> 
 </ol> 
 <p>如果你看Spring Boot中关于@Configuration测试的源代码会发现和上面的代码有点不一样：</p> 
 <pre class="brush: java; gutter: true">public class DataSourceAutoConfigurationTests {

	private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@Before
	public void init() {
		EmbeddedDatabaseConnection.override = null;
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.datasource.initialize:false",
				"spring.datasource.url:jdbc:hsqldb:mem:testdb-" + new Random().nextInt());
	}

	@After
	public void restore() {
		EmbeddedDatabaseConnection.override = null;
		this.context.close();
	}</pre> 
 <p>这是因为Spring和Spring Boot都是用JUnit做测试的，而JUnit的特性是每次执行测试方法前，都会new一个测试类实例，而TestNG是在共享同一个测试类实例的。</p> 
 <h2>例子2：测试@Conditional</h2> 
 <p>Spring Framework提供了一种可以条件控制@Configuration的机制，即只在满足某条件的情况下才会导入@Configuration，这就是@Conditional。</p> 
 <p>下面我们来对@Conditional做一些测试，首先我们自定义一个Condition FooConfiguration：</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class FooConfiguration {

  @Bean
  @Conditional(FooCondition.class)
  public Foo foo() {
    return new Foo();
  }

  public static class FooCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      if (context.getEnvironment() != null) {
        Boolean property = context.getEnvironment().getProperty("foo.create", Boolean.class);
        return Boolean.TRUE.equals(property);
      }
      return false;
    }

  }
}</pre> 
 <p>该Condition判断Environment中是否有foo.create=true。</p> 
 <p>如果我们要测试这个Condition，那么就必须往Environment里添加相关property才可以，在这里我们测试了三种情况：</p> 
 <ol> 
  <li>没有配置foo.create=true</li> 
  <li>配置foo.create=true</li> 
  <li>配置foo.create=false</li> 
 </ol> 
 <p>FooConfigurationTest：</p> 
 <pre class="brush: java; gutter: true">public class FooConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeMethod
  public void init() {
    context = new AnnotationConfigApplicationContext();
  }

  @AfterMethod(alwaysRun = true)
  public void reset() {
    context.close();
  }

  @Test(expectedExceptions = NoSuchBeanDefinitionException.class)
  public void testFooCreatePropertyNull() {
    context.register(FooConfiguration.class);
    context.refresh();
    context.getBean(Foo.class);
  }

  @Test
  public void testFooCreatePropertyTrue() {
    context.getEnvironment().getPropertySources().addLast(
        new MapPropertySource("test", Collections.singletonMap("foo.create", "true"))
    );
    context.register(FooConfiguration.class);
    context.refresh();
    assertNotNull(context.getBean(Foo.class));
  }

  @Test(expectedExceptions = NoSuchBeanDefinitionException.class)
  public void testFooCreatePropertyFalse() {
    context.getEnvironment().getPropertySources().addLast(
        new MapPropertySource("test", Collections.singletonMap("foo.create", "false"))
    );
    context.register(FooConfiguration.class);
    context.refresh();
    assertNotNull(context.getBean(Foo.class));
  }

}</pre> 
 <p>注意我们用以下方法来给Environment添加property：</p> 
 <pre class="brush: java; gutter: true">context.getEnvironment().getPropertySources().addLast(
  new MapPropertySource("test", Collections.singletonMap("foo.create", "true"))
);</pre> 
 <p>所以针对@Conditional和其对应的Condition的测试的根本就是给它不一样的条件，判断其行为是否正确，在这个例子里我们的Condition比较简单，只是判断是否存在某个property，如果复杂Condition的话，测试思路也是一样的。</p> 
 <h2>例子3：测试@ConditionalOnProperty</h2> 
 <p>Spring framework只提供了@Conditional，Spring boot对这个机制做了扩展，提供了更为丰富的@ConditionalOn*，这里我们以@ConditionalOnProperty举例说明。</p> 
 <p>先看FooConfiguration：</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class FooConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "foo", name = "create", havingValue = "true")
  public Foo foo() {
    return new Foo();
  }

}</pre> 
 <p>FooConfigurationTest：</p> 
 <pre class="brush: java; gutter: true">public class FooConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeMethod
  public void init() {
    context = new AnnotationConfigApplicationContext();
  }

  @AfterMethod(alwaysRun = true)
  public void reset() {
    context.close();
  }

  @Test(expectedExceptions = NoSuchBeanDefinitionException.class)
  public void testFooCreatePropertyNull() {
    context.register(FooConfiguration.class);
    context.refresh();
    context.getBean(Foo.class);
  }

  @Test
  public void testFooCreatePropertyTrue() {
    EnvironmentTestUtils.addEnvironment(context, "foo.create=true");
    context.register(FooConfiguration.class);
    context.refresh();
    assertNotNull(context.getBean(Foo.class));
  }

  @Test(expectedExceptions = NoSuchBeanDefinitionException.class)
  public void testFooCreatePropertyFalse() {
    EnvironmentTestUtils.addEnvironment(context, "foo.create=false");
    context.register(FooConfiguration.class);
    context.refresh();
    assertNotNull(context.getBean(Foo.class));
  }

}</pre> 
 <p>这段测试代码和例子2的逻辑差不多，只不过例子2里使用了我们自己写的Condition，这里使用了Spring Boot提供的@ConditionalOnProperty。</p> 
 <p>并且利用了Spring Boot提供的EnvironmentTestUtils简化了给Environment添加property的工作：</p> 
 <pre class="brush: java; gutter: true">EnvironmentTestUtils.addEnvironment(context, "foo.create=false");</pre> 
 <h2>例子4：测试Configuration Properties</h2> 
 <p>Spring Boot还提供了类型安全的Configuration Properties，下面举例如何对其进行测试。</p> 
 <p>BarConfiguration：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@EnableConfigurationProperties(BarConfiguration.BarProperties.class)
public class BarConfiguration {

  @Autowired
  private BarProperties barProperties;

  @Bean
  public Bar bar() {
    return new Bar(barProperties.getName());
  }

  @ConfigurationProperties("bar")
  public static class BarProperties {

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

}</pre> 
 <p>BarConfigurationTest：</p> 
 <pre class="brush: java; gutter: true">public class BarConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @BeforeMethod
  public void init() {
    context = new AnnotationConfigApplicationContext();
  }

  @AfterMethod(alwaysRun = true)
  public void reset() {
    context.close();
  }

  @Test
  public void testBarCreation() {
    EnvironmentTestUtils.addEnvironment(context, "bar.name=test");
    context.register(BarConfiguration.class, PropertyPlaceholderAutoConfiguration.class);
    context.refresh();
    assertEquals(context.getBean(Bar.class).getName(), "test");
  }

}</pre> 
 <p>注意到因为我们使用了Configuration Properties机制，需要注册PropertyPlaceholderAutoConfiguration，否则在BarConfiguration里无法注入BarProperties。</p> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#beans-java-conditional" class="external" rel="nofollow" target="_blank">Conditionally include @Configuration classes or @Bean methods</a></li> 
  <li><a href="https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-condition-annotations" class="external" rel="nofollow" target="_blank">Condition annotations</a></li> 
  <li><a href="https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-external-config-typesafe-configuration-properties" class="external" rel="nofollow" target="_blank">Type-safe Configuration Properties</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">Spring Boot Testing</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>