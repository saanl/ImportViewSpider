<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://www.cnkirito.moe/2017/09/20/spring-security-2/">徐靖峰</a>
 </div> 
 <p>上一篇文章《<a title="Spring Security ( 一 ) – 架构概述" href="http://www.importnew.com/26712.html">Spring Security(一) ：Architecture Overview</a>》，我们介绍了Spring Security的基础架构，这一节我们通过Spring官方给出的一个guides例子，来了解Spring Security是如何保护我们的应用的，之后会对进行一个解读。</p> 
 <h2>2 Spring Security Guides</h2> 
 <h3>2.1 引入依赖</h3> 
 <pre class="brush: java; gutter: true">&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
        &lt;artifactId&gt;spring-boot-starter-web&lt;/artifactId&gt;
    &lt;/dependency&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
        &lt;artifactId&gt;spring-boot-starter-security&lt;/artifactId&gt;
    &lt;/dependency&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
        &lt;artifactId&gt;spring-boot-starter-thymeleaf&lt;/artifactId&gt;
    &lt;/dependency&gt;
&lt;/dependencies&gt;</pre> 
 <p>由于我们集成了springboot，所以不需要显示的引入Spring Security文档中描述core，config依赖，只需要引入spring-boot-starter-security即可。</p> 
 <h3>2.2 创建一个不受安全限制的web应用</h3> 
 <p>这是一个首页，不受安全限制</p> 
 <p>src/main/resources/templates/home.html</p> 
 <pre class="brush: java; gutter: true">&lt;!DOCTYPE html&gt;
&lt;html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3"&gt;
    &lt;head&gt;
        &lt;title&gt;Spring Security Example&lt;/title&gt;
    &lt;/head&gt;
    &lt;body&gt;
        &lt;h1&gt;Welcome!&lt;/h1&gt;
        &lt;p&gt;Click &lt;a th:href="@{/hello}"&gt;here&lt;/a&gt; to see a greeting.&lt;/p&gt;
    &lt;/body&gt;
&lt;/html&gt;</pre> 
 <p>这个简单的页面上包含了一个链接，跳转到”/hello”。对应如下的页面</p> 
 <p>src/main/resources/templates/hello.html</p> 
 <pre class="brush: java; gutter: true">&lt;!DOCTYPE html&gt;
&lt;html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3"&gt;
    &lt;head&gt;
        &lt;title&gt;Hello World!&lt;/title&gt;
    &lt;/head&gt;
    &lt;body&gt;
        &lt;h1&gt;Hello world!&lt;/h1&gt;
    &lt;/body&gt;
&lt;/html&gt;</pre> 
 <p>接下来配置Spring MVC，使得我们能够访问到页面。</p> 
 <pre class="brush: java; gutter: true">@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/hello").setViewName("hello");
        registry.addViewController("/login").setViewName("login");
    }
}</pre> 
 <h3>2.3 配置Spring Security</h3> 
 <p>一个典型的安全配置如下所示：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@EnableWebSecurity &lt;1&gt;
public class WebSecurityConfig extends WebSecurityConfigurerAdapter { &lt;1&gt;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http &lt;2&gt;
            .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth &lt;3&gt;
            .inMemoryAuthentication()
                .withUser("admin").password("admin").roles("USER");
    }
}</pre> 
 <p>&lt;1&gt; @EnableWebSecurity注解使得SpringMVC集成了Spring Security的web安全支持。另外，WebSecurityConfig配置类同时集成了WebSecurityConfigurerAdapter，重写了其中的特定方法，用于自定义Spring Security配置。整个Spring Security的工作量，其实都是集中在该配置类，不仅仅是这个guides，实际项目中也是如此。<br> &lt;2&gt; configure(HttpSecurity)定义了哪些URL路径应该被拦截，如字面意思所描述：”/“, “/home”允许所有人访问，”/login”作为登录入口，也被允许访问，而剩下的”/hello”则需要登陆后才可以访问。<br> &lt;3&gt; configureGlobal(AuthenticationManagerBuilder)在内存中配置一个用户，admin/admin分别是用户名和密码，这个用户拥有USER角色。<br> 我们目前还没有登录页面，下面创建登录页面：</p> 
 <pre class="brush: java; gutter: true">&lt;!DOCTYPE html&gt;
&lt;html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3"&gt;
    &lt;head&gt;
        &lt;title&gt;Spring Security Example &lt;/title&gt;
    &lt;/head&gt;
    &lt;body&gt;
        &lt;div th:if="${param.error}"&gt;
            Invalid username and password.
        &lt;/div&gt;
        &lt;div th:if="${param.logout}"&gt;
            You have been logged out.
        &lt;/div&gt;
        &lt;form th:action="@{/login}" method="post"&gt;
            &lt;div&gt;&lt;label&gt; User Name : &lt;input type="text" name="username"/&gt; &lt;/label&gt;&lt;/div&gt;
            &lt;div&gt;&lt;label&gt; Password: &lt;input type="password" name="password"/&gt; &lt;/label&gt;&lt;/div&gt;
            &lt;div&gt;&lt;input type="submit" value="Sign In"/&gt;&lt;/div&gt;
        &lt;/form&gt;
    &lt;/body&gt;
&lt;/html&gt;</pre> 
 <p>这个Thymeleaf模板提供了一个用于提交用户名和密码的表单,其中name=”username”，name=”password”是默认的表单值，并发送到“/ login”。 在默认配置中，Spring Security提供了一个拦截该请求并验证用户的过滤器。 如果验证失败，该页面将重定向到“/ login?error”，并显示相应的错误消息。 当用户选择注销，请求会被发送到“/ login?logout”。</p> 
 <p>最后，我们为hello.html添加一些内容，用于展示用户信息。</p> 
 <pre class="brush: java; gutter: true">&lt;!DOCTYPE html&gt;
&lt;html xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity3"&gt;
    &lt;head&gt;
        &lt;title&gt;Hello World!&lt;/title&gt;
    &lt;/head&gt;
    &lt;body&gt;
        &lt;h1 th:inline="text"&gt;Hello [[${#httpServletRequest.remoteUser}]]!&lt;/h1&gt;
        &lt;form th:action="@{/logout}" method="post"&gt;
            &lt;input type="submit" value="Sign Out"/&gt;
        &lt;/form&gt;
    &lt;/body&gt;
&lt;/html&gt;</pre> 
 <p>我们使用Spring Security之后，HttpServletRequest#getRemoteUser()可以用来获取用户名。 登出请求将被发送到“/ logout”。 成功注销后，会将用户重定向到“/ login?logout”。</p> 
 <h3>2.4 添加启动类</h3> 
 <pre class="brush: java; gutter: true">@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Throwable {
        SpringApplication.run(Application.class, args);
    }
}</pre> 
 <h3>2.5 测试</h3> 
 <p>访问首页http://localhost:8080/:</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/26735.html/home-2" rel="attachment wp-att-26738"><img class="aligncenter size-full wp-image-26738" title="home" src="http://incdn1.b0.upaiyun.com/2017/09/75d0b7a1b19f3daba8a02ead00e39cf8.png" alt=""></a></p> 
 <p>点击here，尝试访问受限的页面：/hello,由于未登录，结果被强制跳转到登录也/login：</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/26735.html/login" rel="attachment wp-att-26739"><img class="aligncenter size-full wp-image-26739" title="login" src="http://incdn1.b0.upaiyun.com/2017/09/fe42e08d42d6a4659d87930edf416264.png" alt=""></a></p> 
 <p>输入正确的用户名和密码之后，跳转到之前想要访问的/hello:</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/26735.html/hello" rel="attachment wp-att-26740"><img class="aligncenter size-full wp-image-26740" title="hello" src="http://incdn1.b0.upaiyun.com/2017/09/69a329523ce1ec88bf63061863d9cb14.png" alt=""></a></p> 
 <p>点击Sign out退出按钮，访问:/logout,回到登录页面:</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/26735.html/logout" rel="attachment wp-att-26741"><img class="aligncenter size-full wp-image-26741" title="logout" src="http://incdn1.b0.upaiyun.com/2017/09/f1f10c452feb9df08bd8089dae36e255.png" alt=""></a></p> 
 <h3>2.6 总结</h3> 
 <p>本篇文章没有什么干货，基本算是翻译了Spring Security Guides的内容，稍微了解Spring Security的朋友都不会对这个翻译感到陌生。考虑到受众的问题，一个入门的例子是必须得有的，方便后续对Spring Security的自定义配置进行讲解。下一节，以此guides为例，讲解这些最简化的配置背后，Spring Security都帮我们做了什么工作。</p> 
 <p>本节所有的代码，可以直接在Spring的官方仓库下载得到，git clone https://github.com/spring-guides/gs-securing-web.git。不过，建议初学者根据文章先一步步配置，出了问题，再与demo进行对比。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>