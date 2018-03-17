<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>Spring Test Framework提供了对JDBC的支持，能够让我们很方便对关系型数据库做集成测试。</p> 
 <p>同时Spring Boot提供了和Flyway的集成支持，能够方便的管理开发过程中产生的SQL文件，配合Spring已经提供的工具能够更方便地在测试之前初始化数据库以及测试之后清空数据库。</p> 
 <p>本章节为了方便起见，本章节使用了H2作为测试数据库。</p> 
 <p>注意：在真实的开发环境中，集成测试用数据库应该和最终的生产数据库保持一致，这是因为不同数据库的对于SQL不是完全相互兼容的，如果不注意这一点，很有可能出现集成测试通过，但是上了生产环境却报错的问题。</p> 
 <p>因为是集成测试，所以我们使用了maven-failsafe-plugin来跑，它和maven-surefire-plugin的差别在于，maven-failsafe-plugin只会搜索*IT.java来跑测试，而maven-surefire-plugin只会搜索*Test.java来跑测试。</p> 
 <p>如果想要在maven打包的时候跳过集成测试，只需要mvn clean install -DskipITs。</p> 
 <h2>被测试类</h2> 
 <p>先介绍一下被测试的类。</p> 
 <p>Foo.java：</p> 
 <pre class="brush: java; gutter: true">public class Foo {

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}</pre> 
 <p>FooRepositoryImpl.java：</p> 
 <pre class="brush: java; gutter: true">@Repository
public class FooRepositoryImpl implements FooRepository {

  private JdbcTemplate jdbcTemplate;

  @Override
  public void save(Foo foo) {
    jdbcTemplate.update("INSERT INTO FOO(name) VALUES (?)", foo.getName());
  }

  @Override
  public void delete(String name) {
    jdbcTemplate.update("DELETE FROM FOO WHERE NAME = ?", name);
  }

  @Autowired
  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

}</pre> 
 <h2>例子1：不使用Spring Testing提供的工具</h2> 
 <p>Spring_1_IT_Configuration.java：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@ComponentScan(basePackageClasses = FooRepository.class)
public class Spring_1_IT_Configuration {

  @Bean(destroyMethod = "shutdown")
  public DataSource dataSource() {

    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.H2)
        .setScriptEncoding("UTF-8")
        .ignoreFailedDrops(true)
        .addScript("classpath:me/chanjar/domain/foo-ddl.sql")
        .build();
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {

    return new JdbcTemplate(dataSource());

  }
}</pre> 
 <p>在Spring_1_IT_Configuration中，我们定义了一个H2的DataSource Bean，并且构建了JdbcTemplate Bean。</p> 
 <p>注意看addScript(“classpath:me/chanjar/domain/foo-ddl.sql”)这句代码，我们让EmbeddedDatabase执行foo-ddl.sql脚本来建表：</p> 
 <pre class="brush: java; gutter: true">CREATE TABLE FOO (
  name VARCHAR2(100)
);</pre> 
 <p>Spring_1_IT.java：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = Spring_1_IT_Configuration.class)
public class Spring_1_IT extends AbstractTestNGSpringContextTests {

  @Autowired
  private FooRepository fooRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  public void testSave() {

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    assertEquals(
        jdbcTemplate.queryForObject("SELECT count(*) FROM FOO", Integer.class),
        Integer.valueOf(1)
    );

  }

  @Test(dependsOnMethods = "testSave")
  public void testDelete() {

    assertEquals(
        jdbcTemplate.queryForObject("SELECT count(*) FROM FOO", Integer.class),
        Integer.valueOf(1)
    );

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    fooRepository.delete(foo.getName());
    assertEquals(
        jdbcTemplate.queryForObject("SELECT count(*) FROM FOO", Integer.class),
        Integer.valueOf(0)
    );
  }

}</pre> 
 <p>在这段测试代码里可以看到，我们分别测试了FooRepository的save和delete方法，并且利用JdbcTemplate来验证数据库中的结果。</p> 
 <h2>例子2：使用Spring Testing提供的工具</h2> 
 <p>在这个例子里，我们会使用JdbcTestUtils来辅助测试。</p> 
 <p>Spring_2_IT_Configuration.java：</p> 
 <pre class="brush: java; gutter: true">@Configuration
@ComponentScan(basePackageClasses = FooRepository.class)
public class Spring_2_IT_Configuration {

  @Bean
  public DataSource dataSource() {

    EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.H2)
        .setScriptEncoding("UTF-8")
        .ignoreFailedDrops(true)
        .addScript("classpath:me/chanjar/domain/foo-ddl.sql")
        .build();
    return db;
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {

    return new JdbcTemplate(dataSource());

  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }

}</pre> 
 <p>这里和例子1的区别在于，我们提供了一个PlatformTransactionManager Bean，这是因为在下面的测试代码里的AbstractTransactionalTestNGSpringContextTests需要它。</p> 
 <p>Spring_2_IT.java：</p> 
 <pre class="brush: java; gutter: true">@ContextConfiguration(classes = Spring_2_IT_Configuration.class)
public class Spring_2_IT extends AbstractTransactionalTestNGSpringContextTests {

  @Autowired
  private FooRepository fooRepository;

  @Test
  public void testSave() {

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    assertEquals(countRowsInTable("FOO"), 1);
    countRowsInTableWhere("FOO", "name = 'Bob'");
  }

  @Test(dependsOnMethods = "testSave")
  public void testDelete() {

    assertEquals(countRowsInTable("FOO"), 0);

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    fooRepository.delete(foo.getName());
    assertEquals(countRowsInTable("FOO"), 0);

  }

}</pre> 
 <p>在这里我们使用countRowsInTable(“FOO”)来验证数据库结果，这个方法是AbstractTransactionalTestNGSpringContextTests对JdbcTestUtils的代理。</p> 
 <p>而且要注意的是，每个测试方法在执行完毕后，会自动rollback，所以在testDelete的第一行里，我们assertEquals(countRowsInTable(“FOO”), 0)，这一点和例子1里是不同的。</p> 
 <p>更多关于Spring Testing Framework与Transaction相关的信息，可以见Spring官方文档 Transaction management。</p> 
 <h2>例子3：使用Spring Boot</h2> 
 <p>Boot_1_IT.java：</p> 
 <pre class="brush: java; gutter: true">@SpringBootTest
@SpringBootApplication(scanBasePackageClasses = FooRepository.class)
public class Boot_1_IT extends AbstractTransactionalTestNGSpringContextTests {

  @Autowired
  private FooRepository fooRepository;

  @Test
  public void testSave() {

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    assertEquals(countRowsInTable("FOO"), 1);
    countRowsInTableWhere("FOO", "name = 'Bob'");
  }

  @Test(dependsOnMethods = "testSave")
  public void testDelete() {

    assertEquals(countRowsInTable("FOO"), 0);

    Foo foo = new Foo();
    foo.setName("Bob");
    fooRepository.save(foo);

    fooRepository.delete(foo.getName());
    assertEquals(countRowsInTable("FOO"), 0);

  }
  
  @AfterTest
  public void cleanDb() {
    flyway.clean();
  }
  
}</pre> 
 <p>因为使用了Spring Boot来做集成测试，得益于其AutoConfiguration机制，不需要自己构建DataSource 、JdbcTemplate和PlatformTransactionManager的Bean。</p> 
 <p>并且因为我们已经将flyway-core添加到了maven依赖中，Spring Boot会利用flyway来帮助我们初始化数据库，我们需要做的仅仅是将sql文件放到classpath的db/migration目录下：</p> 
 <p>V1.0.0__foo-ddl.sql:</p> 
 <pre class="brush: java; gutter: true">CREATE TABLE FOO (
  name VARCHAR2(100)
);</pre> 
 <p>而且在测试最后，我们利用flyway清空了数据库：</p> 
 <pre class="brush: java; gutter: true">@AfterTest
public void cleanDb() {
  flyway.clean();
}</pre> 
 <p>使用flyway有很多好处：</p> 
 <ol> 
  <li>每个sql文件名都规定了版本号</li> 
  <li>flyway按照版本号顺序执行</li> 
  <li>在开发期间，只需要将sql文件放到db/migration目录下就可以了，不需要写类似EmbeddedDatabaseBuilder.addScript()这样的代码</li> 
  <li>基于以上三点，就能够将数据库初始化SQL语句也纳入到集成测试中来，保证代码配套的SQL语句的正确性</li> 
  <li>可以帮助你清空数据库，这在你使用非内存数据库的时候非常有用，因为不管测试前还是测试后，你都需要一个干净的数据库</li> 
 </ol> 
 <h2>参考文档</h2> 
 <p>本章节涉及到的Spring Testing Framework JDBC、SQL相关的工具：</p> 
 <ul> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testcontext-tx" class="external" rel="nofollow" target="_blank">Transaction management</a></li> 
  <li><a href="http://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/htmlsingle/#testcontext-executing-sql" class="external" rel="nofollow" target="_blank">Executing SQL scripts</a></li> 
 </ul> 
 <p>和flyway相关的：</p> 
 <ul> 
  <li><a href="https://flywaydb.org/documentation/" class="external" rel="nofollow" target="_blank">flyway的官方文档</a></li> 
  <li><a href="http://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/#howto-execute-flyway-database-migrations-on-startup" class="external" rel="nofollow" target="_blank">flway和spring boot的集成</a></li> 
 </ul> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>