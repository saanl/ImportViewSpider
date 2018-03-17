<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>Spring＆Spring Boot Testing工具提供了一些方便测试的Annotation，本文会对其中的一些做一些讲解。</p> 
 <h2>@TestPropertySource</h2> 
 <p>@TestPropertySource可以用来覆盖掉来自于系统环境变量，Java的系统属性，@PropertySource的属性。</p> 
 <p>同时@TestPropertySource(properties=…)优先级高于@TestPropertySource(locations=…)。</p> 
 <p>利用它我们可以很方便的在测试代码里微调，模拟配置（比如修改操作系统目录分隔符，数据源等）。</p> 
 <h3>例子1：使用Spring Testing工具</h3> 
 <p>我们先使用@PropertySource将一个外部属性文件加载进来，PropertySourceConfig：</p> 
 <pre class="brush: java; gutter: true">@Configuration 
@PropertySource（“ classpath：me / chanjar / annotation / testps / ex1 / property-source.properties ”）
public class PropertySourceConfig {
}</pre> 
 <pre class="brush: java; gutter: true">file: property-source.properties
foo=abc</pre> 
 <p>然后我们用@TestPropertySource覆盖了这个特性：</p> 
 <pre class="brush: java; gutter: true">TestPropertySource（properties  = { “ foo = xyz ”  ...</pre> 
 <p>最后我们测试了是否覆盖成功（结果是成功的）：</p> 
 <pre class="brush: java; gutter: true">@Test 
public void testOverridePropertySource（）{
的assertEquals（环境。的getProperty（ “ FOO ”）， “ XYZ ”）;
}</pre> 
 <p>同时我们还对@TestPropertySource做了一些其他的测试，具体情况你可以自己观察。为了方便你观察@TestPropertySource对系统环境变量和Java的系统属性的覆盖效果，我们在一开始打印出了它们的值。</p> 
 <p>源代码TestPropertyTest：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration（类 =  PropertySourceConfig 。类）
 @TestPropertySource（
     属性 = { “富= XYZ ”，“巴= UVW ”，“ PATH = AAA ”，“ java.runtime.name = BBB ” }，
     位置 =  “类路径：我/chanjar/annotation/testps/ex1/test-property-source.properties “
）
公共 类 TestPropertyTest  扩展 AbstractTestNGSpringContextTests  实现 EnvironmentAware {

  私人 环境环境;

  @覆盖
  公共 无效 setEnvironment（环境 环境）{
     此。环境=环境;
    Map &lt; String，Object &gt; systemEnvironment =（（ConfigurableEnvironment）环境）。getSystemEnvironment（）;
    系统。出去。println（“ ===系统环境=== ”）;
    系统。出去。的println（getMapString（systemEnvironment））;
    系统。出去。的println（）;

    系统。出去。println（“ === Java系统属性=== ”）;
    Map &lt; String，Object &gt; systemProperties =（（ConfigurableEnvironment）环境）。getSystemProperties（）;
    系统。出去。的println（getMapString（systemProperties））;
  }

  @Test 
  public  void  testOverridePropertySource（）{
    的assertEquals（环境。的getProperty（ “ FOO ”）， “ XYZ ”）;
  }

  @Test 
  public  void  testOverrideSystemEnvironment（）{
    的assertEquals（环境。的getProperty（ “ PATH ”）， “ AAA ”）;
  }

  @Test 
  public  void  testOverrideJavaSystemProperties（）{
    的assertEquals（环境。的getProperty（ “ java.runtime.name ”）， “ BBB ”）;
  }

  @Test 
  public  void  testInlineTestPropertyOverrideResourceLocationTestProperty（）{
    的assertEquals（环境。的getProperty（ “条”）， “ UVW ”）;
  }

  private  String  getMapString（Map &lt; String，Object &gt;  map）{
     return  String 。加入（“ \ n ”，
        地图。keySet（）。stream（）。地图（K - &gt; ķ +  “ = ”  +地图。得到（k））的。收集（toList（））
    ）;
  }
}</pre> 
 <h3>例子2：使用Spring Boot Testing工具</h3> 
 <p>@TestPropertySource也可以和@SpringBootTest一起使用。</p> 
 <p>源代码见TestPropertyTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest（类 =  PropertySourceConfig 。类）
 @TestPropertySource（
     属性 = { “富= XYZ ”，“巴= UVW ”，“ PATH = AAA ”，“ java.runtime.name = BBB ” }，
     位置 =  “类路径：我/chanjar/annotation/testps/ex1/test-property-source.properties “
）
公共 类 TestPropertyTest  扩展 AbstractTestNGSpringContextTests  实现 EnvironmentAware {
   // ..</pre> 
 <h2>@ActiveProfiles</h2> 
 <p>@ActiveProfiles可以用来在测试的时候启用某些资料的豆本章节的测试代码使用了下面的这个配置：</p> 
 <pre class="brush: java; gutter: true">@Configuration 
public  class  Config {

  @Bean 
  @Profile（“ dev ”）
   public  Foo  fooDev（）{
     return  new  Foo（“ dev ”）;
  }

  @Bean 
  @Profile（“ product ”）
   public  Foo  fooProduct（）{
     return  new  Foo（“ product ”）;
  }

  @Bean 
  @Profile（“ default ”）
   public  Foo  fooDefault（）{
     return  new  Foo（“ default ”）;
  }

  @Bean 
  public  bar  bar（）{
     return  new  bar（“ no profile ”）;
  }

}</pre> 
 <h3>例子1：不使用ActiveProfiles</h3> 
 <p>在没有@ActiveProfiles的时候，外形=默认和没有设定个人资料的豆会被加载到。</p> 
 <p>源代码ActiveProfileTest：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration（类 =  配置。类）
 公共 类 ActiveProfileTest  延伸 AbstractTestNGSpringContextTests {

  @Autowired 
  私人 Foo foo;

  @Autowired 
  私人 酒吧 ;

  @Test 
  public  void  test（）{
    的assertEquals（FOO 。的getName（）， “默认”）;
    的assertEquals（巴。的getName（）， “无简档”）;
  }

}</pre> 
 <h3>例子二：使用ActiveProfiles</h3> 
 <p>当使用了@ActiveProfiles的时候，轮廓匹配的和没有设定个人资料的豆会被加载到。</p> 
 <p>源代码ActiveProfileTest：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration（类 =  配置。类）
[ @ActiveProfiles] [doc-active-profiles]（“ product ”）
 public  class  ActiveProfileTest  extends  AbstractTestNGSpringContextTests {

  @Autowired 
  私人 Foo foo;

  @Autowired 
  私人 酒吧 ;

  @Test 
  public  void  test（）{
    的assertEquals（FOO 。的getName（）， “产品”）;
    的assertEquals（巴。的getName（）， “无简档”）;
  }

}</pre> 
 <h3>总结</h3> 
 <p>在没有@ActiveProfiles的时候，外形=默认和没有设定个人资料的豆会被加载到。<br> 当使用了@ActiveProfiles的时候，轮廓匹配的和没有设定个人资料的豆会被加载到。<br> @ActiveProfiles同样也可以和@SpringBootTest配合使用，这里就不举例说明了。</p> 
 <h2>Annotations -&nbsp;@JsonTest</h2> 
 <p>@JsonTest是Spring Boot提供的方便测试JSON序列化反序列化的测试工具，在Spring Boot的文档中有一些介绍。</p> 
 <p>需要注意的是@JsonTest需要Jackson的ObjectMapper，事实上如果你的Spring Boot项目添加了spring-web的Maven依赖，JacksonAutoConfiguration就会自动为你配置一个：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
  &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
  &lt;artifactId&gt;spring-boot-autoconfigure&lt;/artifactId&gt;
&lt;/dependency&gt;

&lt;dependency&gt;
  &lt;groupId&gt;org.springframework&lt;/groupId&gt;
  &lt;artifactId&gt;spring-web&lt;/artifactId&gt;
&lt;/dependency&gt;</pre> 
 <p>这里没有提供关于日期时间的例子，关于这个比较复杂，可以看我的另一篇文章：<a href="https://github.com/chanjarster/springboot-jackson-datetime-example" class="external" rel="nofollow" target="_blank">Spring Boot Jackson对于日期时间类型处理的例子</a>。</p> 
 <h3>例子1：简单例子</h3> 
 <p>源代码见SimpleJsonTest：</p> 
 <p>&nbsp;</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = SimpleJsonTest.class)
@JsonTest
public class SimpleJsonTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private JacksonTester&lt;Foo&gt; json;

  @Test
  public void testSerialize() throws Exception {
    Foo details = new Foo("Honda", 12);
    // 使用通包下的json文件测试结果是否正确
    assertThat(this.json.write(details)).isEqualToJson("expected.json");
    // 或者使用基于JSON path的校验
    assertThat(this.json.write(details)).hasJsonPathStringValue("@.name");
    assertThat(this.json.write(details)).extractingJsonPathStringValue("@.name").isEqualTo("Honda");
    assertThat(this.json.write(details)).hasJsonPathNumberValue("@.age");
    assertThat(this.json.write(details)).extractingJsonPathNumberValue("@.age").isEqualTo(12);
  }

  @Test
  public void testDeserialize() throws Exception {
    String content = "{\"name\":\"Ford\",\"age\":13}";
    Foo actual = this.json.parseObject(content);
    assertThat(actual).isEqualTo(new Foo("Ford", 13));
    assertThat(actual.getName()).isEqualTo("Ford");
    assertThat(actual.getAge()).isEqualTo(13);

  }

}</pre> 
 <h3>例子2: 测试@JsonComponent</h3> 
 <p>@JsonTest可以用来测试@JsonComponent。</p> 
 <p>这个例子里使用了自定义的@JsonComponent FooJsonComponent：</p> 
 <pre class="brush: java; gutter: true">@JsonComponent
public class FooJsonComponent {

  public static class Serializer extends JsonSerializer&lt;Foo&gt; {
    @Override
    public void serialize(Foo value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      // ...
    }

  }

  public static class Deserializer extends JsonDeserializer&lt;Foo&gt; {

    @Override
    public Foo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      // ...
    }

  }

}</pre> 
 <p>测试代码JsonComponentJsonTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = { JsonComponentJacksonTest.class, FooJsonComponent.class })
@JsonTest
public class JsonComponentJacksonTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private JacksonTester&lt;Foo&gt; json;

  @Test
  public void testSerialize() throws Exception {
    Foo details = new Foo("Honda", 12);
    assertThat(this.json.write(details).getJson()).isEqualTo("\"name=Honda,age=12\"");
  }

  @Test
  public void testDeserialize() throws Exception {
    String content = "\"name=Ford,age=13\"";
    Foo actual = this.json.parseObject(content);
    assertThat(actual).isEqualTo(new Foo("Ford", 13));
    assertThat(actual.getName()).isEqualTo("Ford");
    assertThat(actual.getAge()).isEqualTo(13);

  }

}</pre> 
 <h3>例子3: 使用@ContextConfiguration</h3> 
 <p>事实上@JsonTest也可以配合@ContextConfiguration一起使用。</p> 
 <p>源代码见ThinJsonTest：</p> 
 <pre class="brush: java; gutter: true">@JsonTest
@ContextConfiguration(classes = JsonTest.class)
public class ThinJsonTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private JacksonTester&lt;Foo&gt; json;

  @Test
  public void testSerialize() throws Exception {
    // ...
  }

  @Test
  public void testDeserialize() throws Exception {
    // ...
  }

}</pre> 
 <h2>@OverrideAutoConfiguration</h2> 
 <p>在 <a title="Spring、Spring Boot 和 TestNG 测试指南 ( 1 )" href="http://www.importnew.com/27523.html">Spring、Spring Boot 和 TestNG 测试指南 ( 1 )</a> 提到：</p> 
 <p>除了单元测试（不需要初始化ApplicationContext的测试）外，尽量将测试配置和生产配置保持一致。比如如果生产配置里启用了AutoConfiguration，那么测试配置也应该启用。因为只有这样才能够在测试环境下发现生产环境的问题，也避免出现一些因为配置不同导致的奇怪问题。</p> 
 <p>那么当我们想在测试代码里关闭Auto Configuration如何处理？</p> 
 <p>方法1：提供另一套测试配置<br> 方法2：使用@OverrideAutoConfiguration</p> 
 <p>方法1虽然能够很好的解决问题，但是比较麻烦。而方法2则能够不改变原有配置、不提供新的配置的情况下，就能够关闭Auto Configuration。</p> 
 <p>在本章节的例子里，我们自己做了一个Auto Configuration类，AutoConfigurationEnableLogger：</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class AutoConfigurationEnableLogger {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoConfigurationEnableLogger.class);

  public AutoConfigurationEnableLogger() {
    LOGGER.info("Auto Configuration Enabled");
  }

}</pre> 
 <p>并且在META-INF/spring.factories里注册了它：</p> 
 <pre class="brush: java; gutter: true">org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
me.chanjar.annotation.overrideac.AutoConfigurationEnableLogger</pre> 
 <p>这样一来，只要Spring Boot启动了Auto Configuration就会打印出日志：</p> 
 <pre class="brush: java; gutter: true">2017-08-24 16:44:52.789  INFO 13212 --- [           main] m.c.a.o.AutoConfigurationEnableLogger    : Auto Configuration Enabled</pre> 
 <h3>例子1：未关闭Auto Configuration</h3> 
 <p>源代码见BootTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
@SpringBootApplication
public class BootTest extends AbstractTestNGSpringContextTests {

  @Test
  public void testName() throws Exception {

  }
}</pre> 
 <p>查看输出的日志，会发现Auto Configuration已经启用。</p> 
 <h3>例子2：关闭Auto Configuration</h3> 
 <p>然后我们用@OverrideAutoConfiguration关闭了Auto Configuration。</p> 
 <p>源代码见BootTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
@OverrideAutoConfiguration(enabled = false)
@SpringBootApplication
public class BootTest extends AbstractTestNGSpringContextTests {

  @Test
  public void testName() throws Exception {

  }
}</pre> 
 <p>再查看输出的日志，就会发现Auto Configuration已经关闭。</p> 
 <h2>@TestConfiguration</h2> 
 <p>@TestConfiguration是Spring Boot Test提供的一种工具，用它我们可以在一般的@Configuration之外补充测试专门用的Bean或者自定义的配置。</p> 
 <p>@TestConfiguration实际上是一种@TestComponent，@TestComponent是另一种@Component，在语义上用来指定某个Bean是专门用于测试的。</p> 
 <p>需要特别注意，你应该使用一切办法避免在生产代码中自动扫描到@TestComponent。 如果你使用@SpringBootApplication启动测试或者生产代码，@TestComponent会自动被排除掉，如果不是则需要像@SpringBootApplication一样添加TypeExcludeFilter：</p> 
 <pre class="brush: java; gutter: true">//...
@ComponentScan(excludeFilters = {
  @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
  // ...})
public @interface SpringBootApplication</pre> 
 <h3>例子1：作为内部类</h3> 
 <p>@TestConfiguration和@Configuration不同，它不会阻止@SpringBootTest去查找机制（在Chapter 1: 基本用法 – 使用Spring Boot Testing工具 – 例子4提到过），正如@TestConfiguration的javadoc所说，它只是对既有配置的一个补充。</p> 
 <p>所以我们在测试代码上添加@SpringBootConfiguration，用@SpringBootTest(classes=…)或者在同package里添加@SpringBootConfiguration类都是可以的。</p> 
 <p>而且@TestConfiguration作为内部类的时候它是会被@SpringBootTest扫描掉的，这点和@Configuration一样。</p> 
 <p>测试代码TestConfigurationTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
@SpringBootConfiguration
public class TestConfigurationTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private Foo foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getName(), "from test config");
  }

  @TestConfiguration
  public class TestConfig {

    @Bean
    public Foo foo() {
      return new Foo("from test config");
    }

  }
}</pre> 
 <h3>例子2：对@Configuration的补充和覆盖</h3> 
 <p>@TestConfiguration能够：</p> 
 <ol> 
  <li>补充额外的Bean</li> 
  <li>覆盖已存在的Bean</li> 
 </ol> 
 <p>要特别注意第二点，@TestConfiguration能够直接覆盖已存在的Bean，这一点正常的@Configuration是做不到的。</p> 
 <p>我们先提供了一个正常的@Configuration（Config）：</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class Config {

  @Bean
  public Foo foo() {
    return new Foo("from config");
  }
}</pre> 
 <p>又提供了一个@TestConfiguration，在里面覆盖了foo Bean，并且提供了foo2 Bean（TestConfig）：</p> 
 <pre class="brush: java; gutter: true">@TestConfiguration
public class TestConfig {

  // 这里不需要@Primary之类的机制，直接就能够覆盖
  @Bean
  public Foo foo() {
    return new Foo("from test config");
  }

  @Bean
  public Foo foo2() {
    return new Foo("from test config2");
  }
}</pre> 
 <p>测试代码TestConfigurationTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = { Config.class, TestConfig.class })
public class TestConfigurationTest extends AbstractTestNGSpringContextTests {

  @Qualifier("foo")
  @Autowired
  private Foo foo;

  @Qualifier("foo2")
  @Autowired
  private Foo foo2;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getName(), "from test config");
    assertEquals(foo2.getName(), "from test config2");

  }

}</pre> 
 <p>再查看输出的日志，就会发现Auto Configuration已经关闭。</p> 
 <h3>例子3：避免@TestConfiguration被扫描到</h3> 
 <p>在上面的这个例子里的TestConfig是会被@ComponentScan扫描到的，如果要避免被扫描到，在本文开头已经提到过了。</p> 
 <p>先来看一下没有做任何过滤的情形，我们先提供了一个@SpringBootConfiguration（IncludeConfig）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootConfiguration
@ComponentScan
public interface IncludeConfig {
}</pre> 
 <p>然后有个测试代码引用了它（TestConfigIncludedTest）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = IncludeConfig.class)
public class TestConfigIncludedTest extends AbstractTestNGSpringContextTests {

  @Autowired(required = false)
  private TestConfig testConfig;

  @Test
  public void testPlusCount() throws Exception {
    assertNotNull(testConfig);

  }

}</pre> 
 <p>从这段代码可以看到TestConfig被加载了。</p> 
 <p>现在我们使用TypeExcludeFilter来过滤@TestConfiguration（ExcludeConfig1）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootConfiguration
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class)
})
public interface ExcludeConfig1 {
}</pre> 
 <p>再来看看结果（TestConfigExclude_1_Test）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = ExcludeConfig1.class)
public class TestConfigExclude_1_Test extends AbstractTestNGSpringContextTests {

  @Autowired(required = false)
  private TestConfig testConfig;

  @Test
  public void test() throws Exception {
    assertNull(testConfig);

  }

}</pre> 
 <p>还可以用@SpringBootApplication来排除TestConfig（ExcludeConfig2）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication
public interface ExcludeConfig2 {
}</pre> 
 <p>看看结果（TestConfigExclude_2_Test）：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = ExcludeConfig2.class)
public class TestConfigExclude_2_Test extends AbstractTestNGSpringContextTests {

  @Autowired(required = false)
  private TestConfig testConfig;

  @Test
  public void testPlusCount() throws Exception {
    assertNull(testConfig);

  }

}</pre> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring框架测试</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">春季启动测试</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#testcontext-ctx-management-property-sources" class="external" rel="nofollow" target="_blank">具有测试属性源的上下文配置</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">Spring Boot Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing-spring-boot-applications-testing-autoconfigured-json-tests" class="external" rel="nofollow" target="_blank">@JsonTest</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/jackson/JsonComponent.html" class="external" rel="nofollow" target="_blank">JsonComponent</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/autoconfigure/jackson/JacksonAutoConfiguration.html" class="external" rel="nofollow" target="_blank">JacksonAutoConfiguration</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/test/json/JacksonTester.html" class="external" rel="nofollow" target="_blank">JacksonTester</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/test/json/GsonTester.html" class="external" rel="nofollow" target="_blank">GsonTester</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/test/json/BasicJsonTester.html" class="external" rel="nofollow" target="_blank">BasicJsonTester</a></li> 
  <li><a href="https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing-spring-boot-applications-detecting-config" class="external" rel="nofollow" target="_blank">Detecting test configuration</a></li> 
  <li><a href="https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing-spring-boot-applications-excluding-config" class="external" rel="nofollow" target="_blank">Excluding test configuration</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>