<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-mvc-error-handling-example">chanjarster</a>
 </div> 
 <h2>默认行为</h2> 
 <p>根据Spring Boot官方文档的说法：</p> 
 <blockquote>
  <p>For machine clients it will produce a JSON response with details of the error, the HTTP status and the exception message. For browser clients there is a ‘whitelabel’ error view that renders the same data in HTML format</p>
 </blockquote> 
 <p>也就是说，当发生异常时：</p> 
 <ul> 
  <li>如果请求是从浏览器发送出来的，那么返回一个Whitelabel Error Page</li> 
  <li>如果请求是从machine客户端发送出来的，那么会返回相同信息的json</li> 
 </ul> 
 <p>你可以在浏览器中依次访问以下地址：</p> 
 <ol> 
  <li>http://localhost:8080/return-model-and-view</li> 
  <li>http://localhost:8080/return-view-name</li> 
  <li>http://localhost:8080/return-view</li> 
  <li>http://localhost:8080/return-text-plain</li> 
  <li>http://localhost:8080/return-json-1</li> 
  <li>http://localhost:8080/return-json-2</li> 
 </ol> 
 <p>会发现FooController和FooRestController返回的结果都是一个Whitelabel Error Page也就是html。</p> 
 <p>但是如果你使用curl访问上述地址，那么返回的都是如下的json：</p> 
 <pre class="brush: java; gutter: true">{
  "timestamp": 1498886969426,
  "status": 500,
  "error": "Internal Server Error",
  "exception": "me.chanjar.exception.SomeException",
  "message": "...",
  "trace": "...",
  "path": "..."
}</pre> 
 <p>但是有一个URL除外：http://localhost:8080/return-text-plain，它不会返回任何结果，原因稍后会有说明。</p> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/def" class="external" rel="nofollow" target="_blank">me.chanjar.boot.def</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/def/DefaultExample.java" class="external" rel="nofollow" target="_blank">DefaultExample</a>运行。</p> 
 <p>注意：我们必须在application.properties添加server.error.include-stacktrace=always才能够得到stacktrace。</p> 
 <h3>为何curl text/plain资源无法获得error</h3> 
 <p>如果你在logback-spring.xml里一样配置了这么一段：</p> 
 <pre class="brush: java; gutter: true">&lt;logger name="org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod" level="TRACE"/&gt;</pre> 
 <p>那么你就能在日志文件里发现这么一个异常：</p> 
 <pre class="brush: java; gutter: true">org.springframework.web.HttpMediaTypeNotAcceptableException: Could not find acceptable representation
...</pre> 
 <p>要理解这个异常是怎么来的，那我们来简单分析以下Spring MVC的处理过程：</p> 
 <ol> 
  <li>curl http://localhost:8080/return-text-plain，会隐含一个请求头Accept: */*，会匹配到FooController.returnTextPlain(produces=text/plain)方法，注意：如果请求头不是Accept: */*或Accept: text/plain，那么是匹配不到FooController.returnTextPlain的。</li> 
  <li>RequestMappingHandlerMapping根据url匹配到了(见AbstractHandlerMethodMapping.lookupHandlerMethod#L341)FooController.returnTextPlan(produces=text/plain)。</li> 
  <li>方法抛出了异常，forward到/error。</li> 
  <li>RequestMappingHandlerMapping根据url匹配到了(见AbstractHandlerMethodMapping.lookupHandlerMethod#L341)BasicErrorController的两个方法errorHtml(produces=text/html)和error(produces=null，相当于produces=*/*)。</li> 
  <li>因为请求头Accept: */*，所以会匹配error方法上(见AbstractHandlerMethodMapping#L352，RequestMappingInfo.compareTo，ProducesRequestCondition.compareTo)。</li> 
  <li>error方法返回的是ResponseEntity&lt;Map&lt;String, Object&gt;&gt;，会被HttpEntityMethodProcessor.handleReturnValue处理。</li> 
  <li>HttpEntityMethodProcessor进入AbstractMessageConverterMethodProcessor.writeWithMessageConverters，发现请求要求*/*(Accept: */*)，而能够产生text/plain(FooController.returnTextPlan produces=text/plain)，那它会去找能够将Map转换成String的HttpMessageConverter(text/plain代表String)，结果是找不到。</li> 
  <li>AbstractMessageConverterMethodProcessor抛出HttpMediaTypeNotAcceptableException。</li> 
 </ol> 
 <p>那么为什么浏览器访问http://localhost:8080/return-text-plain就可以呢？你只需打开浏览器的开发者模式看看请求头就会发现Accept:text/html,…，所以在第4步会匹配到BasicErrorController.errorHtml方法，那结果自然是没有问题了。</p> 
 <p>那么这个问题怎么解决呢？我会在自定义ErrorController里说明。</p> 
 <h2>自定义Error页面</h2> 
 <p>前面看到了，Spring Boot针对浏览器发起的请求的error页面是Whitelabel Error Page，下面讲解如何自定义error页面。</p> 
 <p>注意2：自定义Error页面不会影响machine客户端的输出结果</p> 
 <h3>方法1</h3> 
 <p>根据Spring Boot官方文档，如果想要定制这个页面只需要：</p> 
 <p>to customize it just add a View that resolves to ‘error’<br> 这句话讲的不是很明白，其实只要看ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration的代码就知道，只需注册一个名字叫做error的View类型的Bean就行了。</p> 
 <p>本例的CustomDefaultErrorViewConfiguration注册将error页面改到了templates/custom-error-page/error.html上。</p> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customdefaulterrorview" class="external" rel="nofollow" target="_blank">me.chanjar.boot.customdefaulterrorview</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customdefaulterrorview/CustomDefaultErrorViewExample.java" class="external" rel="nofollow" target="_blank">CustomDefaultErrorViewExample</a>运行。</p> 
 <h3>方法2</h3> 
 <p>方法2比方法1简单很多，在Spring官方文档中没有说明。其实只需要提供error View所对应的页面文件即可。</p> 
 <p>比如在本例里，因为使用的是Thymeleaf模板引擎，所以在classpath /templates放一个自定义的error.html就能够自定义error页面了。</p> 
 <p>本章节就不提供代码了，有兴趣的你可以自己尝试。</p> 
 <h2>自定义Error属性</h2> 
 <p>前面看到了不论error页面还是error json，能够得到的属性就只有：timestamp、status、error、exception、message、trace、path。</p> 
 <p>如果你想自定义这些属性，可以如Spring Boot官方文档所说的：</p> 
 <p>simply add a bean of type ErrorAttributes to use the existing mechanism but replace the contents<br> 在ErrorMvcAutoConfiguration.errorAttributes提供了DefaultErrorAttributes，我们也可以参照这个提供一个自己的CustomErrorAttributes覆盖掉它。</p> 
 <p>如果使用curl访问相关地址可以看到，返回的json里的出了修改过的属性，还有添加的属性：</p> 
 <pre class="brush: java; gutter: true">{
  "exception": "customized exception",
  "add-attribute": "add-attribute",
  "path": "customized path",
  "trace": "customized trace",
  "error": "customized error",
  "message": "customized message",
  "timestamp": 1498892609326,
  "status": 100
}</pre> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorattributes" class="external" rel="nofollow" target="_blank">me.chanjar.boot.customerrorattributes</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorattributes/CustomErrorAttributesExample.java" class="external" rel="nofollow" target="_blank">CustomErrorAttributesExample</a>运行。</p> 
 <h2>自定义ErrorController</h2> 
 <p>在前面提到了curl http://localhost:8080/return-text-plain得不到error信息，解决这个问题有两个关键点：</p> 
 <ol> 
  <li>请求的时候指定Accept头，避免匹配到BasicErrorController.error方法。比如：curl -H ‘Accept: text/plain’ http://localhost:8080/return-text-plain</li> 
  <li>提供自定义的ErrorController。</li> 
 </ol> 
 <p>下面将如何提供自定义的ErrorController。按照Spring Boot官方文档的说法：</p> 
 <blockquote>
  <p>To do that just extend BasicErrorController and add a public method with a @RequestMapping that has a produces attribute, and create a bean of your new type.</p>
 </blockquote> 
 <p>所以我们提供了一个CustomErrorController，并且通过CustomErrorControllerConfiguration将其注册为Bean。</p> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorcontroller" class="external" rel="nofollow" target="_blank">me.chanjar.boot.customerrorcontroller</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorcontroller/CustomErrorControllerExample.java" class="external" rel="nofollow" target="_blank">CustomErrorControllerExample</a>运行。</p> 
 <h2>ControllerAdvice定制特定异常返回结果</h2> 
 <p>根据Spring Boot官方文档的例子，可以使用@ControllerAdvice和@ExceptionHandler对特定异常返回特定的结果。</p> 
 <p>我们在这里定义了一个新的异常：AnotherException，然后在BarControllerAdvice中对SomeException和AnotherException定义了不同的@ExceptionHandler：</p> 
 <ul> 
  <li>SomeException都返回到controlleradvice/some-ex-error.html上</li> 
  <li>AnotherException统统返回JSON</li> 
 </ul> 
 <p>在BarController中，所有*-a都抛出SomeException，所有*-b都抛出AnotherException。下面是用浏览器和curl访问的结果：</p> 
 <p style="text-align: center;"> </p>
 <table> 
  <thead> 
   <tr> 
    <th>url</th> 
    <th>Browser</th> 
    <th>curl</th> 
   </tr> 
  </thead> 
  <tbody> 
   <tr> 
    <td><a href="http://localhost:8080/bar/html-a" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/html-a</a></td> 
    <td>some-ex-error.html</td> 
    <td>some-ex-error.html</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/bar/html-b" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/html-b</a></td> 
    <td>No converter found for return value of type: class AnotherExceptionErrorMessage<a href="https://github.com/spring-projects/spring-framework/blob/v4.3.9.RELEASE/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodProcessor.java#L187" class="external" rel="nofollow" target="_blank">AbstractMessageConverterMethodProcessor#L187</a></td> 
    <td>error(json)</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/bar/json-a" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/json-a</a></td> 
    <td>some-ex-error.html</td> 
    <td>some-ex-error.html</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/bar/json-b" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/json-b</a></td> 
    <td>Could not find acceptable representation</td> 
    <td>error(json)</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/bar/text-plain-a" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/text-plain-a</a></td> 
    <td>some-ex-error.html</td> 
    <td>some-ex-error.html</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/bar/text-plain-b" class="external" rel="nofollow" target="_blank">http://localhost:8080/bar/text-plain-b</a></td> 
    <td>Could not find acceptable representation</td> 
    <td>Could not find acceptable representation</td> 
   </tr> 
  </tbody> 
 </table> 
 <p>注意上方表格的Could not find acceptable representation错误，产生这个的原因和之前为何curl text/plain资源无法获得error是一样的：无法将@ExceptionHandler返回的数据转换@RequestMapping.produces所要求的格式。</p> 
 <p>所以你会发现如果使用@ExceptionHandler，那就得自己根据请求头Accept的不同而输出不同的结果了，办法就是定义一个void @ExceptionHandler，具体见@ExceptionHandler javadoc。</p> 
 <h2>定制不同Status Code的错误页面</h2> 
 <p>Spring Boot 官方文档提供了一种简单的根据不同Status Code跳到不同error页面的方法，见<a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-error-handling-custom-error-pages" class="external" rel="nofollow" target="_blank">这里</a>。</p> 
 <p>我们可以将不同的Status Code的页面放在classpath: public/error或classpath: templates/error目录下，比如400.html、5xx.html、400.ftl、5xx.ftl。</p> 
 <p>打开浏览器访问以下url会获得不同的结果：</p> 
 <table> 
  <thead> 
   <tr> 
    <th>url</th> 
    <th>Result</th> 
   </tr> 
  </thead> 
  <tbody> 
   <tr> 
    <td><a href="http://localhost:8080/loo/error-403" class="external" rel="nofollow" target="_blank">http://localhost:8080/loo/error-403</a></td> 
    <td>static resource: public/error/403.html</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/loo/error-406" class="external" rel="nofollow" target="_blank">http://localhost:8080/loo/error-406</a></td> 
    <td>thymeleaf view: templates/error/406.html</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/loo/error-600" class="external" rel="nofollow" target="_blank">http://localhost:8080/loo/error-600</a></td> 
    <td>Whitelabel error page</td> 
   </tr> 
   <tr> 
    <td><a href="http://localhost:8080/loo/error-601" class="external" rel="nofollow" target="_blank">http://localhost:8080/loo/error-601</a></td> 
    <td>thymeleaf view: templates/error/6xx.html</td> 
   </tr> 
  </tbody> 
 </table> 
 <p>注意/loo/error-600返回的是Whitelabel error page，但是/loo/error-403和loo/error-406能够返回我们期望的错误页面，这是为什么？先来看看代码。</p> 
 <p>在loo/error-403中，我们抛出了异常Exception403：</p> 
 <pre class="brush: java; gutter: true">@ResponseStatus(HttpStatus.FORBIDDEN)
public class Exception403 extends RuntimeException</pre> 
 <p>在loo/error-406中，我们抛出了异常Exception406：</p> 
 <pre class="brush: java; gutter: true">@ResponseStatus(NOT_ACCEPTABLE)
public class Exception406 extends RuntimeException</pre> 
 <p>注意到这两个异常都有@ResponseStatus注解，这个是注解标明了这个异常所对应的Status Code。 但是在loo/error-600中抛出的SomeException没有这个注解，而是尝试在Response.setStatus(600)来达到目的，但结果是失败的，这是为什么呢？：</p> 
 <pre class="brush: java; gutter: true">@RequestMapping("/error-600")
public String error600(HttpServletRequest request, HttpServletResponse response) throws SomeException {
  request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, 600);
  response.setStatus(600);
  throw new SomeException();
}</pre> 
 <p>要了解为什么就需要知道Spring MVC对于异常的处理机制，下面简单讲解一下：</p> 
 <p>Spring MVC处理异常的地方在DispatcherServlet.processHandlerException，这个方法会利用HandlerExceptionResolver来看异常应该返回什么ModelAndView。</p> 
 <p>目前已知的HandlerExceptionResolver有这么几个：</p> 
 <ol> 
  <li>DefaultErrorAttributes，只负责把异常记录在Request attributes中，name是org.springframework.boot.autoconfigure.web.DefaultErrorAttributes.ERROR</li> 
  <li>ExceptionHandlerExceptionResolver，根据@ExceptionHandler resolve</li> 
  <li>ResponseStatusExceptionResolver，根据@ResponseStatus resolve</li> 
  <li>DefaultHandlerExceptionResolver，负责处理Spring MVC标准异常</li> 
 </ol> 
 <p>Exception403和Exception406都有被ResponseStatusExceptionResolver处理了，而SomeException没有任何Handler处理，这样DispatcherServlet就会将这个异常往上抛至到容器处理（见DispatcherServlet#L1243），以Tomcat为例，它在StandardHostValve#L317、StandardHostValve#L345会将Status Code设置成500，然后跳转到/error，结果就是BasicErrorController处理时就看到Status Code=500，然后按照500去找error page找不到，就只能返回White error page了。</p> 
 <p>实际上，从Request的attributes角度来看，交给BasicErrorController处理时，和容器自己处理时，有几个相关属性的内部情况时这样的：</p> 
 <table> 
  <thead> 
   <tr> 
    <th>Attribute name</th> 
    <th>When throw up to Tomcat</th> 
    <th>Handled by HandlerExceptionResolver</th> 
   </tr> 
  </thead> 
  <tbody> 
   <tr> 
    <td><code>DefaultErrorAttributes.ERROR</code></td> 
    <td>Has value</td> 
    <td>Has Value</td> 
   </tr> 
   <tr> 
    <td><code>DispatcherServlet.EXCEPTION</code></td> 
    <td>No value</td> 
    <td>Has Value</td> 
   </tr> 
   <tr> 
    <td><code>javax.servlet.error.exception</code></td> 
    <td>Has value</td> 
    <td>No Value</td> 
   </tr> 
  </tbody> 
 </table> 
 <p>PS. DefaultErrorAttributes.ERROR = org.springframework.boot.autoconfigure.web.DefaultErrorAttributes.ERROR</p> 
 <p>PS. DispatcherServlet.EXCEPTION = org.springframework.web.servlet.DispatcherServlet.EXCEPTION</p> 
 <p>解决办法有两个：</p> 
 <p>1.给SomeException添加@ResponseStatus，但是这个方法有两个局限：</p> 
 <ul> 
  <li>如果这个异常不是你能修改的，比如在第三方的Jar包里</li> 
  <li>如果@ResponseStatus使用HttpStatus作为参数，但是这个枚举定义的Status Code数量有限</li> 
 </ul> 
 <p>2. 使用@ExceptionHandler，不过得注意自己决定view以及status code</p> 
 <p>第二种解决办法的例子loo/error-601，对应的代码：</p> 
 <pre class="brush: java; gutter: true">@RequestMapping("/error-601")
public String error601(HttpServletRequest request, HttpServletResponse response) throws AnotherException {
  throw new AnotherException();
}

@ExceptionHandler(AnotherException.class)
String handleAnotherException(HttpServletRequest request, HttpServletResponse response, Model model)
    throws IOException {
  // 需要设置Status Code，否则响应结果会是200
  response.setStatus(601);
  model.addAllAttributes(errorAttributes.getErrorAttributes(new ServletRequestAttributes(request), true));
  return "error/6xx";
}</pre> 
 <h3>总结：</h3> 
 <p>1. 没有被HandlerExceptionResolverresolve到的异常会交给容器处理。已知的实现有（按照顺序）：</p> 
 <ul> 
  <li>DefaultErrorAttributes，只负责把异常记录在Request attributes中，name是org.springframework.boot.autoconfigure.web.DefaultErrorAttributes.ERROR</li> 
  <li>ExceptionHandlerExceptionResolver，根据@ExceptionHandler resolve</li> 
  <li>ResponseStatusExceptionResolver，根据@ResponseStatus resolve</li> 
  <li>DefaultHandlerExceptionResolver，负责处理Spring MVC标准异常</li> 
 </ul> 
 <p>2. @ResponseStatus用来规定异常对应的Status Code，其他异常的Status Code由容器决定，在Tomcat里都认定为500（StandardHostValve#L317、StandardHostValve#L345）<br> 3. @ExceptionHandler处理的异常不会经过BasicErrorController，需要自己决定如何返回页面，并且设置Status Code（如果不设置就是200）<br> 4. BasicErrorController会尝试根据Status Code找error page，找不到的话就用Whitelabel error page</p> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customstatuserrorpage" class="external" rel="nofollow" target="_blank">me.chanjar.boot.customstatuserrorpage</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customstatuserrorpage/CustomStatusErrorPageExample.java" class="external" rel="nofollow" target="_blank">CustomStatusErrorPageExample</a>运行。</p> 
 <h2>利用ErrorViewResolver来定制错误页面</h2> 
 <p>前面讲到BasicErrorController会根据Status Code来跳转对应的error页面，其实这个工作是由DefaultErrorViewResolver完成的。</p> 
 <p>实际上我们也可以提供自己的ErrorViewResolver来定制特定异常的error页面。</p> 
 <pre class="brush: java; gutter: true">@Component
public class SomeExceptionErrorViewResolver implements ErrorViewResolver {

  @Override
  public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map&lt;String, Object&gt; model) {
    return new ModelAndView("custom-error-view-resolver/some-ex-error", model);
  }

}</pre> 
 <p>不过需要注意的是，无法通过ErrorViewResolver设定Status Code，Status Code由@ResponseStatus或者容器决定（Tomcat里一律是500）。</p> 
 <p>本章节代码在<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorviewresolver" class="external" rel="nofollow" target="_blank">me.chanjar.boot.customerrorviewresolver</a>，使用<a href="https://github.com/chanjarster/spring-mvc-error-handling-example/blob/master/src/main/java/me/chanjar/boot/customerrorviewresolver/CustomErrorViewResolverExample.java" class="external" rel="nofollow" target="_blank">CustomErrorViewResolverExample</a>运行。</p> 
 <h2>@ExceptionHandler 和 @ControllerAdvice</h2> 
 <p>前面的例子中已经有了对@ControllerAdvice和@ExceptionHandler的使用，这里只是在做一些补充说明：</p> 
 <ol> 
  <li>@ExceptionHandler配合@ControllerAdvice用时，能够应用到所有被@ControllerAdvice切到的Controller</li> 
  <li>@ExceptionHandler在Controller里的时候，就只会对那个Controller生效</li> 
 </ol> 
 <h2>参考文档：</h2> 
 <ul> 
  <li>Spring Boot 1.5.4.RELEASE <a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#boot-features-error-handling" class="external" rel="nofollow" target="_blank">Documentation</a></li> 
  <li>Spring framework 4.3.9.RELEASE <a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#mvc-exceptionhandlers" class="external" rel="nofollow" target="_blank">Documentation</a></li> 
  <li><a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc" class="external" rel="nofollow" target="_blank">Exception Handling in Spring MVC</a></li> 
 </ul> 
 <h2>附录I</h2> 
 <p>下表列出哪些特性是Spring Boot的，哪些是Spring MVC的：</p> 
 <table> 
  <thead> 
   <tr> 
    <th><span style="color: #333333;"><span style="letter-spacing: normal; text-transform: none;">Feature</span></span></th> 
    <th><span style="color: #333333;"><span style="letter-spacing: normal; text-transform: none;">Spring Boot</span></span></th> 
    <th><span style="color: #333333;"><span style="letter-spacing: normal; text-transform: none;">Spring MVC</span></span></th> 
   </tr> 
  </thead> 
  <tbody> 
   <tr> 
    <td><span style="color: #333333;">BasicErrorController</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">ErrorAttributes</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">ErrorViewResolver</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">@ControllerAdvice</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">@ExceptionHandler</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">@ResponseStatus</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
   </tr> 
   <tr> 
    <td><span style="color: #333333;">HandlerExceptionResolver</span></td> 
    <td><span style="color: #333333;">&nbsp;</span></td> 
    <td><span style="color: #333333;">Yes</span></td> 
   </tr> 
  </tbody> 
 </table> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>