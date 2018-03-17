<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>Spring提供了一套AOP工具，但是当你把各种Aspect写完之后，如何确定这些Aspect都正确的应用到目标Bean上了呢？本章将举例说明如何对Spring AOP做测试。</p> 
 <p>首先先来看我们事先定义的Bean以及Aspect。</p> 
 <p>FooServiceImpl：</p> 
 <pre class="brush: java; gutter: true">@Component
public class FooServiceImpl implements FooService {

  private int count;

  @Override
  public int incrementAndGet() {
    count++;
    return count;
  }

}</pre> 
 <p>FooAspect：</p> 
 <pre class="brush: java; gutter: true">@Component
@Aspect
public class FooAspect {

  @Pointcut("execution(* me.chanjar.aop.service.FooServiceImpl.incrementAndGet())")
  public void pointcut() {
  }

  @Around("pointcut()")
  public int changeIncrementAndGet(ProceedingJoinPoint pjp) {
    return 0;
  }

}</pre> 
 <p>可以看到FooAspect会修改FooServiceImpl.incrementAndGet方法的返回值，使其返回0。</p> 
 <h2>例子1：测试FooService的行为</h2> 
 <p>最简单的测试方法就是直接调用FooServiceImpl.incrementAndGet，看看它是否使用返回0。</p> 
 <p>SpringAop_1_Test：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = { SpringAopTest.class, AopConfig.class })
public class SpringAop_1_Test extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooService fooService;

  @Test
  public void testFooService() {

    assertNotEquals(fooService.getClass(), FooServiceImpl.class);

    assertTrue(AopUtils.isAopProxy(fooService));
    assertTrue(AopUtils.isCglibProxy(fooService));

    assertEquals(AopProxyUtils.ultimateTargetClass(fooService), FooServiceImpl.class);

    assertEquals(AopTestUtils.getTargetObject(fooService).getClass(), FooServiceImpl.class);
    assertEquals(AopTestUtils.getUltimateTargetObject(fooService).getClass(), FooServiceImpl.class);

    assertEquals(fooService.incrementAndGet(), 0);
    assertEquals(fooService.incrementAndGet(), 0);

  }

}</pre> 
 <p>先看这段代码：</p> 
 <pre class="brush: java; gutter: true">assertNotEquals(fooService.getClass(), FooServiceImpl.class);

assertTrue(AopUtils.isAopProxy(fooService));
assertTrue(AopUtils.isCglibProxy(fooService));

assertEquals(AopProxyUtils.ultimateTargetClass(fooService), FooServiceImpl.class);

assertEquals(AopTestUtils.getTargetObject(fooService).getClass(), FooServiceImpl.class);
assertEquals(AopTestUtils.getUltimateTargetObject(fooService).getClass(), FooServiceImpl.class);</pre> 
 <p>这些是利用Spring提供的AopUtils、AopTestUtils和AopProxyUtils来判断FooServiceImpl Bean是否被代理了（Spring AOP的实现是通过动态代理来做的）。</p> 
 <p>但是证明FooServiceImpl Bean被代理并不意味着FooAspect生效了（假设此时有多个@Aspect），那么我们还需要验证FooServiceImpl.incrementAndGet的行为：</p> 
 <pre class="brush: java; gutter: true">assertEquals(fooService.incrementAndGet(), 0);
assertEquals(fooService.incrementAndGet(), 0);</pre> 
 <h2>例子2：测试FooAspect的行为</h2> 
 <p>但是总有一些时候我们是无法通过例子1的方法来测试Bean是否被正确的advised的：</p> 
 <ol> 
  <li>advised方法没有返回值</li> 
  <li>Aspect不会修改advised方法的返回值（比如：做日志）</li> 
 </ol> 
 <p>那么这个时候怎么测试呢？此时我们就需要用到Mockito的Spy方法结合Spring Testing工具来测试。</p> 
 <p>SpringAop_2_Test：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = { SpringAop_2_Test.class, AopConfig.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class SpringAop_2_Test extends AbstractTestNGSpringContextTests {

  @SpyBean
  private FooAspect fooAspect;

  @Autowired
  private FooService fooService;

  @Test
  public void testFooService() {

    // ...
    verify(fooAspect, times(2)).changeIncrementAndGet(any());

  }

}</pre> 
 <p>这段代码和例子1有三点区别：</p> 
 <ol> 
  <li>启用了MockitoTestExecutionListener，这样能够开启Mockito的支持（回顾一下Chapter 3: 使用Mockito）</li> 
  <li>@SpyBean private FooAspect fooAspect，这样能够声明一个被Mockito.spy过的Bean</li> 
  <li>verify(fooAspect, times(2)).changeIncrementAndGet(any())，使用Mockito测试FooAspect.changeIncrementAndGet是否被调用了两次</li> 
 </ol> 
 <p>上面的测试代码测试的是FooAspect的行为，而不是FooServiceImpl的行为，这种测试方法更为通用。</p> 
 <h2>例子3：Spring Boot的例子</h2> 
 <p>上面两个例子使用的是Spring Testing工具，下面举例Spring Boot Testing工具如何测AOP（其实大同小异）：</p> 
 <p>SpringBootAopTest：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = { SpringBootAopTest.class, AopConfig.class })
@TestExecutionListeners(listeners = MockitoTestExecutionListener.class)
public class SpringBootAopTest extends AbstractTestNGSpringContextTests {

  @SpyBean
  private FooAspect fooAspect;

  @Autowired
  private FooService fooService;

  @Test
  public void testFooService() {

    // ...
    verify(fooAspect, times(2)).changeIncrementAndGet(any());

  }

}</pre> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/aop.html" class="external" rel="nofollow" target="_blank">Aspect Oriented Programming with Spring</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/javadoc-api/org/springframework/aop/support/AopUtils.html" class="external" rel="nofollow" target="_blank">AopUtils</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/javadoc-api/org/springframework/test/util/AopTestUtils.html" class="external" rel="nofollow" target="_blank">AopTestUtils</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/javadoc-api/org/springframework/aop/framework/AopProxyUtils.html" class="external" rel="nofollow" target="_blank">AopProxyUtils</a></li> 
  <li><a href="https://github.com/spring-projects/spring-framework/blob/v4.3.9.RELEASE/spring-context/src/test/java/org/springframework/context/annotation/EnableAspectJAutoProxyTests.java" class="external" rel="nofollow" target="_blank">spring源码EnableAspectJAutoProxyTests</a></li> 
  <li><a href="https://github.com/spring-projects/spring-framework/blob/v4.3.9.RELEASE/spring-aop/src/test/java/org/springframework/aop/aspectj/annotation/AbstractAspectJAdvisorFactoryTests.java" class="external" rel="nofollow" target="_blank">spring源码AbstractAspectJAdvisorFactoryTests</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>