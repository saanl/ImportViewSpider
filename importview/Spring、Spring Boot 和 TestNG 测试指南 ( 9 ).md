<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>在使用Spring Boot Testing工具中提到：</p> 
 <p>在测试代码之间尽量做到配置共用。 … 能够有效利用Spring TestContext Framework的缓存机制，ApplicationContext只会创建一次，后面的测试会直接用已创建的那个，加快测试代码运行速度。</p> 
 <p>本章将列举几种共享测试配置的方法</p> 
 <h2>@Configuration</h2> 
 <p>我们可以将测试配置放在一个@Configuration里，然后在测试@SpringBootTest或ContextConfiguration中引用它。</p> 
 <p>PlainConfiguration：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication(scanBasePackages = "me.chanjar.shareconfig")
public class PlainConfiguration {
}</pre> 
 <p>FooRepositoryIT：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = PlainConfiguration.class)
public class FooRepositoryIT extends ...</pre> 
 <h2>@Configuration on interface</h2> 
 <p>也可以把@Configuration放到一个interface上。</p> 
 <p>PlainConfiguration：</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication(scanBasePackages = "me.chanjar.shareconfig")
public interface InterfaceConfiguration {
}</pre> 
 <p>FooRepositoryIT：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = InterfaceConfiguration.class)
public class FooRepositoryIT extends ...</pre> 
 <h2>Annotation</h2> 
 <p>也可以利用Spring的Meta-annotations及自定义机制，提供自己的Annotation用在测试配置上。</p> 
 <p>PlainConfiguration：</p> 
 <pre class="brush: java; gutter: true">@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication(scanBasePackages = "me.chanjar.shareconfig")
public @interface AnnotationConfiguration {
}</pre> 
 <p>FooRepositoryIT：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest(classes = FooRepositoryIT.class)
@AnnotationConfiguration
public class FooRepositoryIT extends ...</pre> 
 <h2>参考文档</h2> 
 <ul> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/beans.html#beans-meta-annotations" class="external" rel="nofollow" target="_blank">Meta-annotations</a></li> 
  <li><a href="https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/integration-testing.html#integration-testing-annotations-meta" class="external" rel="nofollow" target="_blank">Meta-Annotation Support for Testing</a></li> 
  <li><a href="https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model" class="external" rel="nofollow" target="_blank">Spring Annotation Programming Model</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>