<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://oopsguy.com/2017/10/13/spring-boot-starter-intro/">Oopsguy</a>
 </div> 
 <h2>1、概述</h2> 
 <p>依赖管理是任何复杂项目的关键部分。以手动的方式来实现依赖管理不太现实，你得花更多时间，同时你在项目的其他重要方面能付出的时间就会变得越少。</p> 
 <p>Spring Boot starter 就是为了解决这个问题而诞生的。Starter POM 是一组方便的依赖描述符，您可以将其包含在应用程序中。您可以获得所需的所有 Spring 和相关技术的一站式服务，无需通过示例代码搜索和复制粘贴依赖。</p> 
 <p>我们有超过 30 个 Boot starter — 下文将提到其中一部分。</p> 
 <h2>2、Web Starter</h2> 
 <p>首先，让我们来看看 REST 服务开发。我们可以使用像 Spring MVC、Tomcat 和 Jackson 这样的库，这对于单个应用程序来说是还是存在许多依赖。</p> 
 <p>Spring Boot starter 通过添加一个依赖来帮助减少手动添加依赖的数量。 因此，不要手动指定依赖，您只需要添加一个 starter 即可，如下所示：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-starter-web&lt;/artifactId&gt;
&lt;/dependency&gt;</pre> 
 <p>现在我们可以创建一个 REST 控制器。为了简单起见，我们不会使用数据库，只专注于 REST 控制器：</p> 
 <pre class="brush: java; gutter: true">@RestController
public class GenericEntityController {
    private List&lt;GenericEntity&gt; entityList = new ArrayList&lt;&gt;();
 
    @RequestMapping("/entity/all")
    public List&lt;GenericEntity&gt; findAll() {
        return entityList;
    }
 
    @RequestMapping(value = "/entity", method = RequestMethod.POST)
    public GenericEntity addEntity(GenericEntity entity) {
        entityList.add(entity);
        return entity;
    }
 
    @RequestMapping("/entity/findby/{id}")
    public GenericEntity findById(@PathVariable Long id) {
        return entityList.stream().
                 filter(entity -&gt; entity.getId().equals(id)).
                   findFirst().get();
    }
}</pre> 
 <p>GenericEntity 是一个简单的 bean，id 的类型为 Long，value 为 String 类型。</p> 
 <p>就是这样，应用程序可以开始运行了，您可以访问 http://localhost:8080/springbootapp/entity/all 并检查控制器是否正常工作。</p> 
 <p>我们已经创建了一个配置非常少的 REST 应用程序。</p> 
 <h2>3、Test Starter</h2> 
 <p>对于测试，我们通常使用以下组合：Spring Test、JUnit、Hamcrest 和 Mockito。我们可以手动包含所有这些库，但使用以下 Spring Boot starter 方式可以自动包含这些库：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-starter-test&lt;/artifactId&gt;
    &lt;scope&gt;test&lt;/scope&gt;
&lt;/dependency&gt;</pre> 
 <p>请注意，您不需要指定工件的版本号。Spring Boot 会自动选择合适的版本 — 您仅需要指定 spring-boot-starter-parent-artifact 的版本。 如果之后您想要升级 Boot 库和依赖，只需在一个地方升级 Boot 版本即可，它将会处理其余部分。</p> 
 <p>让我们来测试一下之前创建的控制器。</p> 
 <p>测试控制器有两种方法：</p> 
 <ul> 
  <li>使用 mock 环境</li> 
  <li>使用嵌入式 Servlet 容器（如 Tomcat 或 Jetty）</li> 
 </ul> 
 <p>在本例中，我们将使用一个 mock 环境：</p> 
 <pre class="brush: java; gutter: true">@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class SpringBootApplicationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
 
    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
 
    @Test
    public void givenRequestHasBeenMade_whenMeetsAllOfGivenConditions_thenCorrect()
      throws Exception { 
        MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
        mockMvc.perform(MockMvcRequestBuilders.get("/entity/all")).
        andExpect(MockMvcResultMatchers.status().isOk()).
        andExpect(MockMvcResultMatchers.content().contentType(contentType)).
        andExpect(jsonPath("$", hasSize(4))); 
    } 
}</pre> 
 <p>这里重要的是 @WebAppConfiguration 注解和 MockMVC 是 spring-test 模块的一部分，hasSize 是一个 Hamcrest matcher，@Before 是一个 JUnit 注解。这些都可以通过导入这一个这样的 starter 依赖来引入。</p> 
 <h2>4、Data JPA Starter</h2> 
 <p>大多数 Web 应用程序都存在某些持久化 — 常见的是 JPA。</p> 
 <p>让我们使用 starter 来开始，而不是手动定义所有关联的依赖：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-starter-data-jpa&lt;/artifactId&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
    &lt;groupId&gt;com.h2database&lt;/groupId&gt;
    &lt;artifactId&gt;h2&lt;/artifactId&gt;
    &lt;scope&gt;runtime&lt;/scope&gt;
&lt;/dependency&gt;</pre> 
 <p>请注意，我们对这些数据库已经有了开箱即用的自动支持：H2、Derby 和 Hsqldb。在我们的示例中，我们将使用 H2。</p> 
 <p>现在让我们为实体创建仓储（repository）：</p> 
 <pre class="brush: java; gutter: true">public interface GenericEntityRepository extends JpaRepository&lt;GenericEntity, Long&gt; {}</pre> 
 <p>现在是测试代码的时候了。这是 JUnit 测试：</p> 
 <pre class="brush: java; gutter: true">@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class SpringBootJPATest {
     
    @Autowired
    private GenericEntityRepository genericEntityRepository;
 
    @Test
    public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() {
        GenericEntity genericEntity = 
          genericEntityRepository.save(new GenericEntity("test"));
        GenericEntity foundedEntity = 
          genericEntityRepository.findOne(genericEntity.getId());
         
        assertNotNull(foundedEntity);
        assertEquals(genericEntity.getValue(), foundedEntity.getValue());
    }
}</pre> 
 <p>我们没有花时间指定数据库厂商、URL 连接和凭据。没有额外所需的配置，这些都受益于 Boot 的默认支持。 但是，如果您需要，可以进行详细配置。</p> 
 <h2>5、Mail Starter</h2> 
 <p>企业开发中一个非常常见的任务就是发送电子邮件，直接使用 Java Mail API 来处理通常很困难。</p> 
 <p>Spring Boot starter 屏蔽了这些复杂性 — mail 依赖可以通过以下方式指定：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-starter-mail&lt;/artifactId&gt;
&lt;/dependency&gt;</pre> 
 <p>现在我们可以直接使用 JavaMailSender。让我们开始编写一些测试。</p> 
 <p>为了测试，我们需要一个简单的 SMTP 服务器。在此例中，我们将使用 Wiser。将其包含到我们的 POM 中：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.subethamail&lt;/groupId&gt;
    &lt;artifactId&gt;subethasmtp&lt;/artifactId&gt;
    &lt;version&gt;3.1.7&lt;/version&gt;
    &lt;scope&gt;test&lt;/scope&gt;
&lt;/dependency&gt;</pre> 
 <p>最新版本的 Wiser 可以在 Maven 中央仓库（http://search.maven.org/#search%7Cga%7C1%7Csubethasmtp）中找到。</p> 
 <p>以下是测试源码：</p> 
 <pre class="brush: java; gutter: true">@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class SpringBootMailTest {
    @Autowired
    private JavaMailSender javaMailSender;
 
    private Wiser wiser;
 
    private String userTo = "user2@localhost";
    private String userFrom = "user1@localhost";
    private String subject = "Test subject";
    private String textMail = "Text subject mail";
 
    @Before
    public void setUp() throws Exception {
        final int TEST_PORT = 25;
        wiser = new Wiser(TEST_PORT);
        wiser.start();
    }
 
    @After
    public void tearDown() throws Exception {
        wiser.stop();
    }
 
    @Test
    public void givenMail_whenSendAndReceived_thenCorrect() throws Exception {
        SimpleMailMessage message = composeEmailMessage();
        javaMailSender.send(message);
        List&lt;WiserMessage&gt; messages = wiser.getMessages();
 
        assertThat(messages, hasSize(1));
        WiserMessage wiserMessage = messages.get(0);
        assertEquals(userFrom, wiserMessage.getEnvelopeSender());
        assertEquals(userTo, wiserMessage.getEnvelopeReceiver());
        assertEquals(subject, getSubject(wiserMessage));
        assertEquals(textMail, getMessage(wiserMessage));
    }
 
    private String getMessage(WiserMessage wiserMessage)
      throws MessagingException, IOException {
        return wiserMessage.getMimeMessage().getContent().toString().trim();
    }
 
    private String getSubject(WiserMessage wiserMessage) throws MessagingException {
        return wiserMessage.getMimeMessage().getSubject();
    }
 
    private SimpleMailMessage composeEmailMessage() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userTo);
        mailMessage.setReplyTo(userFrom);
        mailMessage.setFrom(userFrom);
        mailMessage.setSubject(subject);
        mailMessage.setText(textMail);
        return mailMessage;
    }
}</pre> 
 <p>在测试中，@Before 和 @After 方法负责启动和停止邮件服务器。</p> 
 <p>请注意，我们装配了 JavaMailSender bean — 该 bean 是由 Spring Boot 自动创建。</p> 
 <p>与 Boot 中的其他默认值一样，JavaMailSender 的 email 设置可以在 application.properties 中自定义：</p> 
 <pre class="brush: java; gutter: true">spring.mail.host=localhost
spring.mail.port=25
spring.mail.properties.mail.smtp.auth=false</pre> 
 <p>我们在 localhost:25 上配置了邮件服务器，不需要身份验证。</p> 
 <h2>6、结论</h2> 
 <p>在本文中，我们介绍了 Starter，解释了为什么我们需要它们，并提供了如何在项目中使用它们的示例。</p> 
 <p>让我们回顾一下使用 Spring Boot starter 的好处：</p> 
 <ul> 
  <li>增加 pom 可管理性</li> 
  <li>生产就绪、测试与依赖配置支持</li> 
  <li>减少项目的整体配置时间</li> 
 </ul> 
 <p><a href="ttps://github.com/spring-projects/spring-boot/tree/master/spring-boot-starters">这里</a>可以找到相关的 starter 列表。示例源码可以在<a href="https://github.com/eugenp/tutorials/tree/master/spring-boot" class="external" rel="nofollow" target="_blank">这里</a>找到。</p> 
 <h2>原文示例代码</h2> 
 <ul> 
  <li>https://github.com/eugenp/tutorials/tree/master/spring-boot</li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>