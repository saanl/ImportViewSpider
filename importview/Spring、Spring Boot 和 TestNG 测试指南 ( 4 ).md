<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>Mock测试技术能够避免你为了测试一个方法，却需要自行构建整个依赖关系的工作，并且能够让你专注于当前被测试对象的逻辑，而不是其依赖的其他对象的逻辑。</p> 
 <p>举例来说，比如你需要测试Foo.methodA，而这个方法依赖了Bar.methodB，又传递依赖到了Zoo.methodC，于是它们的依赖关系就是Foo-&gt;Bar-&gt;Zoo，所以在测试代码里你必须自行new Bar和Zoo。</p> 
 <p>有人会说：”我直接用Spring的DI机制不就行了吗？”的确，你可以用Spring的DI机制，不过解决不了测试代码耦合度过高的问题：</p> 
 <p>因为Foo方法内部调用了Bar和Zoo的方法，所以你对其做单元测试的时候，必须完全了解Bar和Zoo方法的内部逻辑，并且谨慎的传参和assert结果，一旦Bar和Zoo的代码修改了，你的Foo测试代码很可能就会运行失败。</p> 
 <p>所以这个时候我们需要一种机制，能过让我们在测试Foo的时候不依赖于Bar和Zoo的具体实现，即不关心其内部逻辑，只关注Foo内部的逻辑，从而将Foo的每个逻辑分支都测试到。</p> 
 <p>所以业界就产生了Mock技术，它可以让我们做一个假的Bar（不需要Zoo，因为只有真的Bar才需要Zoo），然后控制这个假的Bar的行为（让它返回什么就返回什么），以此来测试Foo的每个逻辑分支。</p> 
 <p>你肯定会问，这样的测试有意义吗？在真实环境里Foo用的是真的Bar而不是假的Bar，你用假的Bar测试成功能代表真实环境不出问题？</p> 
 <p>其实假Bar代表的是一个行为正确的Bar，用它来测试就能验证”在Bar行为正确的情况下Foo的行为是否正确”，而真Bar的行为是否正确会由它自己的测试代码来验证。</p> 
 <p>Mock技术的另一个好处是能够让你尽量避免集成测试，比如我们可以Mock一个Repository（数据库操作类），让我们尽量多写单元测试，提高测试代码执行效率。</p> 
 <p>spring-boot-starter-test依赖了Mockito，所以我们会在本章里使用Mockito来讲解。</p> 
 <h2>被测试类</h2> 
 <p>先介绍一下接下来要被我们测试的类Foo、Bar俩兄弟。</p> 
 <pre class="brush: java; gutter: true">public interface Foo {

  boolean checkCodeDuplicate(String code);

}

public interface Bar {

  Set&lt;String&gt; getAllCodes();

}

@Component
public class FooImpl implements Foo {

  private Bar bar;

  @Override
  public boolean checkCodeDuplicate(String code) {
    return bar.getAllCodes().contains(code);
  }

  @Autowired
  public void setBar(Bar bar) {
    this.bar = bar;
  }

}</pre> 
 <h2>例子1: 不使用Mock技术</h2> 
 <p>源代码NoMockTest：</p> 
 <pre class="brush: java; gutter: true">public class NoMockTest {

  @Test
  public void testCheckCodeDuplicate1() throws Exception {

    FooImpl foo = new FooImpl();
    foo.setBar(new Bar() {
      @Override
      public Set&lt;String&gt; getAllCodes() {
        return Collections.singleton("123");
      }
    });
    assertEquals(foo.checkCodeDuplicate("123"), true);

  }

  @Test
  public void testCheckCodeDuplicate2() throws Exception {

    FooImpl foo = new FooImpl();
    foo.setBar(new FakeBar(Collections.singleton("123")));
    assertEquals(foo.checkCodeDuplicate("123"), true);

  }

  public class FakeBar implements Bar {

    private final Set&lt;String&gt; codes;

    public FakeBar(Set&lt;String&gt; codes) {
      this.codes = codes;
    }

    @Override
    public Set&lt;String&gt; getAllCodes() {
      return codes;
    }

  }

}</pre> 
 <p>这个测试代码里用到了两种方法来做假的Bar：</p> 
 <ol> 
  <li>匿名内部类</li> 
  <li>做了一个FakeBar</li> 
 </ol> 
 <p>这两种方式都不是很优雅，看下面使用Mockito的例子。</p> 
 <h2>例子2：使用Mockito</h2> 
 <p>源代码[MockitoTest][src-MockitoTest]：</p> 
 <pre class="brush: java; gutter: true">public class MockitoTest {

  @Mock
  private Bar bar;

  @InjectMocks
  private FooImpl foo;

  @BeforeMethod(alwaysRun = true)
  public void initMock() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCheckCodeDuplicate() throws Exception {

    when(bar.getAllCodes()).thenReturn(Collections.singleton("123"));
    assertEquals(foo.checkCodeDuplicate("123"), true);

  }

}</pre> 
 <ol> 
  <li>我们先给了一个Bar的Mock实现：@Mock private Bar bar;</li> 
  <li>然后又规定了getAllCodes方法的返回值：when(bar.getAllCodes()).thenReturn(Collections.singleton(“123″))。这样就把一个假的Bar定义好了。</li> 
  <li>最后利用Mockito把Bar注入到Foo里面，@InjectMocks private FooImpl foo;、MockitoAnnotations.initMocks(this);</li> 
 </ol> 
 <h2>例子3：配合Spring Test</h2> 
 <p>源代码Spring_1_Test：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = FooImpl.class)
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class Spring_1_Test extends AbstractTestNGSpringContextTests {

  @MockBean
  private Bar bar;

  @Autowired
  private Foo foo;

  @Test
  public void testCheckCodeDuplicate() throws Exception {

    when(bar.getAllCodes()).thenReturn(Collections.singleton("123"));
    assertEquals(foo.checkCodeDuplicate("123"), true);

  }

}</pre> 
 <p>要注意，如果要启用Spring和Mockito，必须添加这么一行：@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)。</p> 
 <h2>例子4：配合Spring Test（多层依赖）</h2> 
 <p>当Bean存在这种依赖关系当时候：LooImpl -&gt; FooImpl -&gt; Bar，我们应该怎么测试呢？</p> 
 <p>按照Mock测试的原则，这个时候我们应该mock一个Foo对象，把这个注入到LooImpl对象里，就像例子3里的一样。</p> 
 <p>不过如果你不想mock Foo而是想mock Bar的时候，其实做法和前面也差不多，Spring会自动将mock Bar注入到FooImpl中，然后将FooImpl注入到LooImpl中。</p> 
 <p>源代码Spring_2_Test:</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = { FooImpl.class, LooImpl.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class Spring_2_Test extends AbstractTestNGSpringContextTests {

  @MockBean
  private Bar bar;

  @Autowired
  private Loo loo;

  @Test
  public void testCheckCodeDuplicate() throws Exception {

    when(bar.getAllCodes()).thenReturn(Collections.singleton("123"));
    assertEquals(loo.checkCodeDuplicate("123"), true);

  }

}</pre> 
 <p>也就是说，得益于Spring Test Framework，我们能够很方便地对依赖关系中任意层级的任意Bean做mock。</p> 
 <h2>例子5：配合Spring Boot Test</h2> 
 <p>源代码Boot_1_Test：</p> 
 <pre class="brush: java; gutter: true">@SpringBoot_1_Test(classes = { FooImpl.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class Boot_1_Test extends AbstractTestNGSpringContextTests {

  @MockBean
  private Bar bar;

  @Autowired
  private Foo foo;

  @Test
  public void testCheckCodeDuplicate() throws Exception {

    when(bar.getAllCodes()).thenReturn(Collections.singleton("123"));
    assertEquals(foo.checkCodeDuplicate("123"), true);

  }

}</pre> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testing" class="external" rel="nofollow" target="_blank">Spring Framework Testing</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-testing" class="external" rel="nofollow" target="_blank">Spring Boot Testing</a></li> 
  <li><a href="http://site.mockito.org/" class="external" rel="nofollow" target="_blank">Mockito</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>