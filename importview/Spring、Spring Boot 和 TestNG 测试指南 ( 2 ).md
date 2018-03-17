<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples/blob/master/chapter_1_intro.md">chanjarster</a>
 </div> 
 <h2>引言</h2> 
 <p>本项目所有的项目均采用Maven的标准目录结构：</p> 
 <ul> 
  <li>src/main/java，程序java文件目录</li> 
  <li>src/main/resource，程序资源文件目录</li> 
  <li>src/test/java，测试代码目录</li> 
  <li>src/test/resources，测试资源文件目录</li> 
 </ul> 
 <p>并且所有Maven项目都可以使用mvn clean test方式跑单元测试，特别需要注意，只有文件名是*Test.java才会被执行，一定要注意这一点哦。</p> 
 <h2>认识TestNG</h2> 
 <p>先认识一下TestNG，这里有一个FooServiceImpl，里面有两个方法，一个是给计数器+1，一个是获取当前计数器的值：</p> 
 <pre class="brush: java; gutter: true">@Component
public class FooServiceImpl implements FooService {

  private int count = 0;

  @Override
  public void plusCount() {
    this.count++;
  }

  @Override
  public int getCount() {
    return count;
  }

}</pre> 
 <p>然后我们针对它有一个FooServiceImplTest作为UT：</p> 
 <pre class="brush: java; gutter: true">public class FooServiceImplTest {

  @Test
  public void testPlusCount() {
    FooService foo = new FooServiceImpl();
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <p>注意看代码里的assertEquals(…)，我们利用它来判断Foo.getCount方法是否按照预期执行。所以，所谓的测试其实就是给定输入、执行一些方法，assert结果是否符合预期的过程。</p> 
 <h2>使用Spring Testing工具</h2> 
 <p>既然我们现在开发的是一个Spring项目，那么肯定会用到Spring Framework的各种特性，这些特性实在是太好用了，它能够大大提高我们的开发效率。那么自然而然，你会想在测试代码里也能够利用Spring Framework提供的特性，来提高测试代码的开发效率。这部分我们会讲如何使用Spring提供的测试工具来做测试。</p> 
 <h3>例子1</h3> 
 <p>源代码见<a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/spring/ex1/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImplTest</a>：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = FooServiceImpl.class)
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <p>在上面的源代码里我们要注意三点：</p> 
 <ol> 
  <li>测试类继承了AbstractTestNGSpringContextTests，如果不这么做测试类是无法启动Spring容器的</li> 
  <li>使用了[@ContextConfiguration][javadoc-ContextConfiguration]来加载被测试的Bean：FooServiceImpl</li> 
  <li>FooServiceImpl是@Component</li> 
 </ol> 
 <p>以上三点缺一不可。</p> 
 <h3>例子2</h3> 
 <p>在这个例子里，我们将@Configuration作为nested static class放在测试类里，根据<a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#testcontext-ctx-management-javaconfig" class="external" rel="nofollow" target="_blank">@ContextConfiguration</a>的文档，它会在默认情况下查找测试类的nested static @Configuration class，用它来导入Bean。</p> 
 <p>源代码见<a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/spring/ex2/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImplTest</a>：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

  @Configuration
  @Import(FooServiceImpl.class)
  static class Config {
  }

}</pre> 
 <h3>例子3</h3> 
 <p>在这个例子里，我们将@Configuration放到外部，并让@ContextConfiguration去加载。</p> 
 <p>源代码见<a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/spring/ex3/Config.java" class="external" rel="nofollow" target="_blank">Config</a>：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@Import(FooServiceImpl.class)
public class Config {
}</pre> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/spring/ex3/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImplTest</a>：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = Config.class)
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <p>需要注意的是，如果@Configuration是专供某个测试类使用的话，把它放到外部并不是一个好主意，因为它有可能会被@ComponentScan扫描到，从而产生一些奇怪的问题。</p> 
 <h2>使用Spring Boot Testing工具</h2> 
 <p>前面一个部分讲解了如何使用Spring Testing工具来测试Spring项目，现在我们讲解如何使用Spring Boot Testing工具来测试Spring Boot项目。</p> 
 <p>在Spring Boot项目里既可以使用Spring Boot Testing工具，也可以使用Spring Testing工具。 在Spring项目里，一般使用Spring Testing工具，虽然理论上也可以使用Spring Boot Testing，不过因为Spring Boot Testing工具会引入Spring Boot的一些特性比如AutoConfiguration，这可能会给你的测试带来一些奇怪的问题，所以一般不推荐这样做。</p> 
 <h3>例子1：直接加载Bean</h3> 
 <p>使用Spring Boot Testing工具只需要将@ContextConfiguration改成@SpringBootTest即可，源代码见<a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex1/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = FooServiceImpl.class)
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <h3>例子2：使用内嵌@Configuration加载Bean</h3> 
 <p>源代码见<a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex2/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

  @Configuration
  @Import(FooServiceImpl.class)
  static class Config {
  }

}</pre> 
 <h3>例子3：使用外部@Configuration加载Bean</h3> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex3/Config.java" class="external" rel="nofollow" target="_blank">Config</a>：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@Import(FooServiceImpl.class)
public class Config {
}</pre> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex3/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = Config.class)
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <p>这个例子和例子2差不多，只不过将@Configuration放到了外部。</p> 
 <h3>例子4：使用@SpringBootConfiguration</h3> 
 <p>前面的例子@SpringBootTest的用法和@ContextConfiguration差不多。不过根据@SpringBootTest的<a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/api/org/springframework/boot/test/context/SpringBootTest.html" class="external" rel="nofollow" target="_blank">文档</a>：</p> 
 <ol> 
  <li>它会尝试加载@SpringBootTest(classes=…)的定义的Annotated classes。Annotated classes的定义在ContextConfiguration中有说明。</li> 
  <li>如果没有设定@SpringBootTest(classes=…)，那么会去找当前测试类的nested @Configuration class</li> 
  <li>如果上一步找到，则会尝试查找@SpringBootConfiguration，查找的路径有：1)看当前测试类是否@SpringBootConfiguration，2)在当前测试类所在的package里找。</li> 
 </ol> 
 <p>所以我们可以利用这个特性来进一步简化测试代码。</p> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex4/Config.java" class="external" rel="nofollow" target="_blank">Config</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootConfiguration
@Import(FooServiceImpl.class)
public class Config {
}</pre> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex4/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <h3>例子5：使用@ComponentScan扫描Bean</h3> 
 <p>前面的例子我们都使用@Import来加载Bean，虽然这中方法很精确，但是在大型项目中很麻烦。</p> 
 <p>在常规的Spring Boot项目中，一般都是依靠自动扫描机制来加载Bean的，所以我们希望我们的测试代码也能够利用自动扫描机制来加载Bean。</p> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex5/Config.java" class="external" rel="nofollow" target="_blank">Config</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootConfiguration
@ComponentScan(basePackages = "me.chanjar.basic.service")
public class Config {
}</pre> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex5/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <h3>例子6：使用@SpringBootApplication</h3> 
 <p>也可以在测试代码上使用@SpringBootApplication，它有这么几个好处：</p> 
 <ol> 
  <li>自身SpringBootConfiguration</li> 
  <li>提供了@ComponentScan配置，以及默认的excludeFilter，有了这些filter Spring在初始化ApplicationContext的时候会排除掉某些Bean和@Configuration</li> 
  <li>启用了EnableAutoConfiguration，这个特性能够利用Spring Boot来自动化配置所需要的外部资源，比如数据库、JMS什么的，这在集成测试的时候非常有用。</li> 
 </ol> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex6/Config.java" class="external" rel="nofollow" target="_blank">Config</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication(scanBasePackages = "me.chanjar.basic.service")
public class Config {
}</pre> 
 <p><a href="https://github.com/chanjarster/spring-test-examples/blob/master/basic/src/test/java/me/chanjar/basic/springboot/ex6/FooServiceImplTest.java" class="external" rel="nofollow" target="_blank">FooServiceImpltest</a>：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService foo;

  @Test
  public void testPlusCount() throws Exception {
    assertEquals(foo.getCount(), 0);

    foo.plusCount();
    assertEquals(foo.getCount(), 1);
  }

}</pre> 
 <h3>避免@SpringBootConfiguration冲突</h3> 
 <p>当@SpringBootTest没有定义(classes=…，且没有找到nested @Configuration class的情况下，会尝试查询@SpringBootConfiguration，如果找到多个的话则会抛出异常：</p> 
 <pre class="brush: java; gutter: true">Caused by: java.lang.IllegalStateException: Found multiple @SpringBootConfiguration annotated classes [Generic bean: class [...]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in file [/Users/qianjia/workspace-os/spring-test-examples/basic/target/test-classes/me/chanjar/basic/springboot/ex7/FooServiceImplTest1.class], Generic bean: class [me.chanjar.basic.springboot.ex7.FooServiceImplTest2]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in file [...]]</pre> 
 <p>比如以下代码就会造成这个问题：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication(scanBasePackages = "me.chanjar.basic.service")
public class Config1 {
}

@SpringBootApplication(scanBasePackages = "me.chanjar.basic.service")
public class Config2 {
}

@SpringBootTest
public class FooServiceImplTest extends AbstractTestNGSpringContextTests {
  // ...
}</pre> 
 <p>解决这个问题的方法有就是避免自动查询@SpringBootConfiguration：</p> 
 <ol> 
  <li>定义@SpringBootTest(classes=…)</li> 
  <li>提供nested @Configuration class</li> 
 </ol> 
 <h3>最佳实践</h3> 
 <p>除了单元测试（不需要初始化ApplicationContext的测试）外，尽量将测试配置和生产配置保持一致。比如如果生产配置里启用了AutoConfiguration，那么测试配置也应该启用。因为只有这样才能够在测试环境下发现生产环境的问题，也避免出现一些因为配置不同导致的奇怪问题。</p> 
 <p>在测试代码之间尽量做到配置共用，这么做的优点有3个：</p> 
 <ol> 
  <li>能够有效利用Spring TestContext Framework的缓存机制，ApplicationContext只会创建一次，后面的测试会直接用已创建的那个，加快测试代码运行速度。</li> 
  <li>当项目中的Bean很多的时候，这么做能够降低测试代码复杂度，想想如果每个测试代码都有一套自己的@Configuration或其变体，那得多吓人。</li> 
 </ol> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html" class="external" rel="nofollow" target="_blank">Maven Standard Directory Layout</a></li> 
  <li><a href="http://testng.org/doc/documentation-main.html" class="external" rel="nofollow" target="_blank">TestNG documentation</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#testcontext-ctx-management-javaconfig" class="external" rel="nofollow" target="_blank">Context configuration with annotated classes</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">Spring Boot Testing</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#testcontext-framework" class="external" rel="nofollow" target="_blank">Spring TestContext Framework</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>