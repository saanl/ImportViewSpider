<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://www.cnkirito.moe/2017/09/20/spring-security-3/">徐靖峰</a>
 </div> 
 <p>上一篇文章《<a title="Spring Security ( 二 ) ：指南" href="http://www.importnew.com/26735.html">Spring Security(二) ：Guides</a>》，通过Spring Security的配置项了解了Spring Security是如何保护我们的应用的，本篇文章对上一次的配置做一个讲解。</p> 
 <h2>3 核心配置解读</h2> 
 <h3>3.1 功能介绍</h3> 
 <p>这是Spring Security入门指南中的配置项：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http
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
      auth
          .inMemoryAuthentication()
              .withUser("admin").password("admin").roles("USER");
  }
}</pre> 
 <p>当配置了上述的javaconfig之后，我们的应用便具备了如下的功能：</p> 
 <ul> 
  <li>除了“/”,”/home”(首页),”/login”(登录),”/logout”(注销),之外，其他路径都需要认证。</li> 
  <li>指定“/login”该路径为登录页面，当未认证的用户尝试访问任何受保护的资源时，都会跳转到“/login”。</li> 
  <li>默认指定“/logout”为注销页面</li> 
  <li>配置一个内存中的用户认证器，使用admin/admin作为用户名和密码，具有USER角色</li> 
  <li>防止CSRF攻击</li> 
  <li>Session Fixation protection(可以参考我之前讲解Spring Session的文章，防止别人篡改sessionId)</li> 
  <li>Security Header(添加一系列和Header相关的控制)</li> 
  <li>HTTP Strict Transport Security for secure requests</li> 
  <li>集成X-Content-Type-Options</li> 
  <li>缓存控制</li> 
  <li>集成X-XSS-Protection.aspx)</li> 
  <li>X-Frame-Options integration to help prevent Clickjacking(iframe被默认禁止使用)</li> 
  <li>为Servlet API集成了如下的几个方法</li> 
  <li>HttpServletRequest#getRemoteUser())</li> 
  <li>HttpServletRequest.html#getUserPrincipal())</li> 
  <li>HttpServletRequest.html#isUserInRole(java.lang.String))</li> 
  <li>HttpServletRequest.html#login(java.lang.String, java.lang.String))</li> 
  <li>HttpServletRequest.html#logout())</li> 
 </ul> 
 <h3>3.2 解读@EnableWebSecurity</h3> 
 <p>我们自己定义的配置类WebSecurityConfig加上了@EnableWebSecurity注解，同时继承了WebSecurityConfigurerAdapter。你可能会在想谁的作用大一点，先给出结论：毫无疑问@EnableWebSecurity起到决定性的配置作用，他其实是个组合注解，背后SpringBoot做了非常多的配置。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>