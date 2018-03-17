<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>Spring Testing Framework提供了Spring MVC Test Framework，能够很方便的来测试Controller。同时Spring Boot也提供了Auto-configured Spring MVC tests更进一步简化了测试需要的配置工作。</p> 
 <p>本章节将分别举例说明在不使用Spring Boot和使用Spring Boot下如何对Spring MVC进行测试。</p> 
 <h2>例子1：Spring</h2> 
 <p>测试Spring MVC的关键是使用MockMvc对象，利用它我们能够在不需启动Servlet容器的情况下测试Controller的行为。</p> 
 <p>源代码SpringMvc_1_Test.java：</p> 
 <pre class="brush: java; gutter: true">@EnableWebMvc
@WebAppConfiguration
@ContextConfiguration(classes = { FooController.class, FooImpl.class })
public class SpringMvc_1_Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mvc;

  @BeforeMethod
  public void prepareMockMvc() {
    this.mvc = webAppContextSetup(wac).build();
  }

  @Test
  public void testController() throws Exception {

    this.mvc.perform(get("/foo/check-code-dup").param("code", "123"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

  }

}</pre> 
 <p>在这段代码里，主要有三个步骤：</p> 
 <ol> 
  <li>将测试类标记为@WebAppConfiguration</li> 
  <li>通过webAppContextSetup(wac).build()构建MockMvc</li> 
  <li>利用MockMvc对结果进行判断</li> 
 </ol> 
 <h2>例子2：Spring + Mock</h2> 
 <p>在例子1里，FooController使用了一个实体FooImpl的Bean，实际上我们也可以提供一个Foo的mock bean来做测试，这样就能够更多的控制测试过程。如果你还不知道Mock那么请看<a title="Spring、Spring Boot 和 TestNG 测试指南 ( 4 )" href="http://www.importnew.com/27538.html">Spring、Spring Boot 和 TestNG 测试指南 ( 4 )</a>。</p> 
 <p>源代码SpringMvc_2_Test.java：</p> 
 <pre class="brush: java; gutter: true">@EnableWebMvc
@WebAppConfiguration
@ContextConfiguration(classes = { FooController.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class SpringMvc_2_Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private WebApplicationContext wac;

  @MockBean
  private Foo foo;

  private MockMvc mvc;

  @BeforeMethod
  public void prepareMockMvc() {
    this.mvc = webAppContextSetup(wac).build();
  }

  @Test
  public void testController() throws Exception {

    when(foo.checkCodeDuplicate(anyString())).thenReturn(true);

    this.mvc.perform(get("/foo/check-code-dup").param("code", "123"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

  }

}</pre> 
 <h2>例子3：Spring Boot</h2> 
 <p>Spring Boot提供了@WebMvcTest更进一步简化了对于Spring MVC的测试，我们提供了对应例子1的Spring Boot版本。</p> 
 <p>源代码BootMvc_1_Test.java：</p> 
 <pre class="brush: java; gutter: true">@WebMvcTest
@ContextConfiguration(classes = { FooController.class, FooImpl.class })
public class BootMvc_1_Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testController() throws Exception {

    this.mvc.perform(get("/foo/check-code-dup").param("code", "123"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

  }

}</pre> 
 <p>在这里，我们不需要自己构建MockMvc，直接使用@Autowired注入就行了，是不是很方便？</p> 
 <h2>例子4：Spring Boot + Mock</h2> 
 <p>这个是对应例子2的Spring Boot版本，源代码BootMvc_2_Test.java：</p> 
 <pre class="brush: java; gutter: true">@WebMvcTest
@ContextConfiguration(classes = { FooController.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class BootMvc_2_Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private Foo foo;

  @Test
  public void testController() throws Exception {

    when(foo.checkCodeDuplicate(anyString())).thenReturn(true);

    this.mvc.perform(get("/foo/check-code-dup").param("code", "123"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

  }

}</pre> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#testcontext-ctx-management-web" class="external" rel="nofollow" target="_blank">Loading a WebApplicationContext</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#spring-mvc-test-framework" class="external" rel="nofollow" target="_blank">Spring MVC Test Framework</a></li> 
  <li><a href="https://github.com/spring-projects/spring-framework/tree/master/spring-test/src/test/java/org/springframework/test/web/servlet/samples" class="external" rel="nofollow" target="_blank">Spring MVC Official Sample Tests</a></li> 
  <li><a href="https://github.com/spring-projects/spring-mvc-showcase" class="external" rel="nofollow" target="_blank">Spring MVC showcase – with full mvc test</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing-spring-boot-applications-testing-autoconfigured-mvc-tests" class="external" rel="nofollow" target="_blank">Auto-configured Spring MVC tests</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">Spring Boot Testing</a></li> 
  <li><a href="https://spring.io/guides/gs/testing-web/" class="external" rel="nofollow" target="_blank">Spring Guides – Testing the Web Layer</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>