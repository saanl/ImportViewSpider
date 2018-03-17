<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://www.jianshu.com/p/fc23c94fc439">今天你不奋斗明天你就落后</a>
 </div> 
 <h2>一、前言</h2> 
 <p>Mybatis作为一个优秀的存储过程和高级映射的持久层框架,目前在项目实践中运用的比较广泛，最近做项目时候发现了一种之前没见过的配置方式，这里总结下常用的配置方式以便备忘查找。</p> 
 <h2>二、Spring中Mybatis的配置方案一</h2> 
 <h3>2.1 多数据源配置案例</h3> 
 <pre class="brush: java; gutter: true">（1）数据源配置
 &lt;bean id="dataSourceForA" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"&gt;
        &lt;property name="driverClassName" value="oracle.jdbc.driver.OracleDriver" /&gt;
        &lt;property name="url" value="${db1_url}" /&gt;
        &lt;property name="username" value="$db1_user}" /&gt;
        &lt;property name="password" value="${db1_passwd}" /&gt;
        &lt;property name="maxWait" value="${db1_maxWait}" /&gt;
        &lt;property name="maxActive" value="28" /&gt; 
        &lt;property name="initialSize" value="2" /&gt;
        &lt;property name="minIdle" value="0" /&gt;
        &lt;property name="timeBetweenEvictionRunsMillis" value="300000" /&gt;
        &lt;property name="testOnBorrow" value="false" /&gt;
        &lt;property name="testWhileIdle" value="true" /&gt;
        &lt;property name="validationQuery" value="select 1 from dual" /&gt;
        &lt;property name="filters" value="stat" /&gt;
    &lt;/bean&gt;

（2）创建sqlSessionFactory
&lt;bean id="sqlSessionFactoryForA" class="org.mybatis.spring.SqlSessionFactoryBean"&gt;
        &lt;property name="mapperLocations" value="classpath*:com/**/mapper1/*Mapper*.xml" /&gt; 
        &lt;property name="dataSource" ref="dataSourceForA" /&gt;
        &lt;property name="typeAliasesPackage" value="com.zlx.***.dal" /&gt;
&lt;/bean&gt;
    
（3）配置扫描器，扫描指定路径的mapper生成数据库操作代理类
&lt;bean class="org.mybatis.spring.mapper.MapperScannerConfigurer"&gt;
    &lt;property name="annotationClass" value="javax.annotation.Resource"&gt;&lt;/property&gt;
        &lt;property name="basePackage" value="com.zlx1.***.dal.***.mapper" /&gt;
        &lt;property name="sqlSessionFactory" ref="sqlSessionFactoryForA" /&gt;
&lt;/bean&gt;

（4）数据源配置
 &lt;bean id="dataSourceForB" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"&gt;
        &lt;property name="driverClassName" value="oracle.jdbc.driver.OracleDriver" /&gt;
        &lt;property name="url" value="${db_url}" /&gt;
        &lt;property name="username" value="$db_user}" /&gt;
        &lt;property name="password" value="${db_passwd}" /&gt;
        &lt;property name="maxWait" value="${db_maxWait}" /&gt;
        &lt;property name="maxActive" value="28" /&gt; 
        &lt;property name="initialSize" value="2" /&gt;
        &lt;property name="minIdle" value="0" /&gt;
        &lt;property name="timeBetweenEvictionRunsMillis" value="300000" /&gt;
        &lt;property name="testOnBorrow" value="false" /&gt;
        &lt;property name="testWhileIdle" value="true" /&gt;
        &lt;property name="validationQuery" value="select 1 from dual" /&gt;
        &lt;property name="filters" value="stat" /&gt;
    &lt;/bean&gt;

（5）创建sqlSessionFactory
&lt;bean id="sqlSessionFactoryForB" class="org.mybatis.spring.SqlSessionFactoryBean"&gt;
        &lt;property name="mapperLocations" value="classpath*:com/**/mapper/*Mapper*.xml" /&gt; 
        &lt;property name="dataSource" ref="dataSourceForB" /&gt;
        &lt;property name="typeAliasesPackage" value="com.zlx.***.dal" /&gt;
&lt;/bean&gt;
    
（6）配置扫描器，扫描指定路径的mapper生成数据库操作代理类
&lt;bean class="org.mybatis.spring.mapper.MapperScannerConfigurer"&gt;
    &lt;property name="annotationClass" value="javax.annotation.Resource"&gt;&lt;/property&gt;
        &lt;property name="basePackage" value="com.zlx.***.dal.***.mapper" /&gt;
        &lt;property name="sqlSessionFactory" ref="sqlSessionFactoryForB" /&gt;
&lt;/bean&gt;</pre> 
 <ul> 
  <li>(1)(2)(3)是一组配置， (4)(5)(6)是一组配置，配置指定的数据源到对应的包下扫描配置文件生成数据库操作代理类。</li> 
  <li>（1)(4）分别创建了两个数据源，(2)(5)根据对应的数据源创建SqlSession工厂，(3)(6)配置mybaits扫描器，根据对应的SqlSession工厂和包路径生成代理后的数据库操作类。</li> 
 </ul> 
 <h3>2.1 原理简单介绍</h3> 
 <p><strong>2.1.1 SqlSessionFactory原理</strong></p> 
 <p>(2)(5)作用是根据配置创建一个SqlSessionFactory，看下SqlSessionFactoryBean的代码知道它实现了FactoryBean和InitializingBean类，由于实现了InitializingBean，所以自然它的afterPropertiesSet方法，由于实现了FactoryBean类，所以自然会有getObject方法。下面看下时序图：</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-2835f315649b6e4a" rel="attachment wp-att-27208"><img class="aligncenter size-full wp-image-27208" title="5879294-2835f315649b6e4a" src="http://incdn1.b0.upaiyun.com/2017/11/4d3c1444cec8cea6715ba8086c2a1fe4.png" alt=""></a></p> 
 <p>从时序图可知，SqlSessionFactoryBean类主要是通过属性配置创建SqlSessionFactory实例，具体是解析配置中所有的mapper文件放到configuration,然后作为构造函数参数实例化一个DefaultSqlSessionFactory作为SqlSessionFactory。</p> 
 <p><strong>2.1.2 MapperScannerConfigurer原理</strong></p> 
 <p>扫描指定路径的mapper生成数据库操作代理类<br> MapperScannerConfigurer 实现了 BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware接口，所以会重写一下方法：</p> 
 <pre class="brush: java; gutter: true">//在bean注册到ioc后创建实例前修改bean定义和新增bean注册，这个是在context的refresh方法调用
void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

//在bean注册到ioc后创建实例前修改bean定义或者属性值
void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

//set属性设置后调用
void afterPropertiesSet() throws Exception;

//获取IOC容器上下文，在context的prepareBeanFactory中调用
void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

//获取bean在ioc容器中名字，在context的prepareBeanFactory中调用
void setBeanName(String name);</pre> 
 <p>先上个扫描mapper生成代理类并注册到ioc时序图：</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-e8e5743ab6938b74" rel="attachment wp-att-27209"><img class="aligncenter size-full wp-image-27209" title="5879294-e8e5743ab6938b74" src="http://incdn1.b0.upaiyun.com/2017/11/e4e018f224e5d6c416db5782e7cd04e4.png" alt=""></a></p> 
 <p>首先MapperScannerConfigurer实现的afterPropertiesSet方法用来确保属性basePackage不为空</p> 
 <pre class="brush: java; gutter: true">public void afterPropertiesSet() throws Exception {
    notNull(this.basePackage, "Property 'basePackage' is required");
  }</pre> 
 <p>postProcessBeanFactory里面啥都没做，setBeanName获取了bean的名字，setApplicationContext里面获取了ioc上下文。下面看重要的方法postProcessBeanDefinitionRegistry，由于mybais是运行时候才通过解析mapper文件生成代理类注入到ioc，所以postProcessBeanDefinitionRegistry正好可以干这个事情。</p> 
 <pre class="brush: java; gutter: true">public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    if (this.processPropertyPlaceHolders) {
      processPropertyPlaceHolders();
    }

    //构造一个ClassPathMapperScanner查找mapper
    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    scanner.setAddToConfig(this.addToConfig);
    //javax.annotation.Resource
    scanner.setAnnotationClass(this.annotationClass);
    scanner.setMarkerInterface(this.markerInterface);
    //引用sqlSessionFactory
    scanner.setSqlSessionFactory(this.sqlSessionFactory);
    scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
    scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
    scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
    //ioc上下文
    scanner.setResourceLoader(this.applicationContext);
    scanner.setBeanNameGenerator(this.nameGenerator);
    scanner.registerFilters();
   //basePackage=com.alibaba.***.dal.***.mapper,com.alibaba.rock.auth.mapper,com.alibaba.rock.workflow.dal.workflow.mapper
    scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
  }</pre> 
 <p>下面重点看下scan方法：</p> 
 <pre class="brush: java; gutter: true">public Set&lt;BeanDefinitionHolder&gt; doScan(String... basePackages) {
    //根据指定路径去查找对应mapper的接口类，并转化为beandefination
    Set&lt;BeanDefinitionHolder&gt; beanDefinitions = super.doScan(basePackages);

    if (beanDefinitions.isEmpty()) {
      logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
    } else {
      //修改接口类bean的beandefination
      processBeanDefinitions(beanDefinitions);
    }

    return beanDefinitions;
  }</pre> 
 <p>其中super.doScan(basePackages);根据指定路径查找mapper接口类，并生成bean的定义对象，对象中包含beanclassname,beanclass属性，最后注册该bean到ioc容器。下面看下最重要的processBeanDefinitions方法对bean定义的改造。</p> 
 <pre class="brush: java; gutter: true">private void processBeanDefinitions(Set&lt;BeanDefinitionHolder&gt; beanDefinitions) {
    GenericBeanDefinition definition;
    for (BeanDefinitionHolder holder : beanDefinitions) {
      definition = (GenericBeanDefinition) holder.getBeanDefinition();

      // 上面讲的扫描后beanclass设置的为mapper接口类，但是这里修改为MapperFactoryBean，MapperFactoryBean代理了mapper接口类,并且实际mapper接口类作为构造函数传入了      definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); 
      definition.setBeanClass(this.mapperFactoryBean.getClass());
      definition.getPropertyValues().add("addToConfig", this.addToConfig);

      //设置属性配置中的sqlSessionFactory
      boolean explicitFactoryUsed = false;
      if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
        definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionFactory != null) {
        definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
        explicitFactoryUsed = true;
      }

      if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
        explicitFactoryUsed = true;
      } else if (this.sqlSessionTemplate != null) {
        if (explicitFactoryUsed) {
          logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
        }
        definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
        explicitFactoryUsed = true;
      }

      if (!explicitFactoryUsed) {
        if (logger.isDebugEnabled()) {
          logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
        }
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
      }
    }
  }</pre> 
 <p>注：这里修改了mapper接口类的beandefination中的beanclass为MapperFactoryBean，它则负责生产数据类操作代理类，实际mapper接口类作为构造函数传入了 。由于只修改了beanclass,没有修改beanname，所以我们从容器中获取时候无感知的。</p> 
 <p>在上一个代理bean如何构造的时序图：</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-0a63c94958194270" rel="attachment wp-att-27210"><img class="aligncenter size-full wp-image-27210" title="5879294-0a63c94958194270" src="http://incdn1.b0.upaiyun.com/2017/11/b42803f082a8648852ffe8c4e95e358d.png" alt=""></a></p> 
 <p>下面看下MapperFactoryBean是如何生成代理类的:<br> 首先，上面代码设置了MapperFactoryBean的setSqlSessionFactory方法：</p> 
 <pre class="brush: java; gutter: true">public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    if (!this.externalSqlSession) {
      this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
    }
  }</pre> 
 <p>上面方法创建了sqlSession，由于MapperFactoryBean为工厂bean所以实例化时候会调用getObject方法：</p> 
 <pre class="brush: java; gutter: true"> public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }</pre> 
 <p>其实是调用了SqlSessionTemplate-&gt;getMapper,其中mapperInterface就是创建MapperFactoryBean时候的构造函数参数。</p> 
 <pre class="brush: java; gutter: true">public &lt;T&gt; T getMapper(Class&lt;T&gt; type) {
    return getConfiguration().getMapper(type, this);
  }</pre> 
 <p>这里调用getConfiguration().getMapper(type, this);实际是DefaultSqlSessionFactory里面的configration的getMapper方法:</p> 
 <pre class="brush: java; gutter: true">public &lt;T&gt; T getMapper(Class&lt;T&gt; type, SqlSession sqlSession) {
   //knownMappers是上面时序图中步骤6设置进入的。
    final MapperProxyFactory&lt;T&gt; mapperProxyFactory = (MapperProxyFactory&lt;T&gt;) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
 protected T newInstance(MapperProxy&lt;T&gt; mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
   //代理回调类为MapperProxy
    final MapperProxy&lt;T&gt; mapperProxy = new MapperProxy&lt;T&gt;(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }</pre> 
 <p>在上一个实际执行sql时候调用代理类的序列图：</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-8697f0c89cc8d3e1" rel="attachment wp-att-27211"><img class="aligncenter size-full wp-image-27211" title="5879294-8697f0c89cc8d3e1" src="http://incdn1.b0.upaiyun.com/2017/11/80092ac4509c3185e96923ca00039e8f.png" alt=""></a></p> 
 <p>所以当调用实际的数据库操作时候会调用MapperProxy的invoke方法：</p> 
 <pre class="brush: java; gutter: true">public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }</pre> 
 <p>mapperMethod.execute(sqlSession, args);里面实际是调用当前mapper对应的SqlSessionTemplate的数据库操作，而它有委托给了代理类sqlSessionProxy，sqlSessionProxy是在SqlSessionTemplate的构造函数里面创建的：</p> 
 <pre class="brush: java; gutter: true">public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
      PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
    notNull(executorType, "Property 'executorType' is required");

    this.sqlSessionFactory = sqlSessionFactory;
    this.executorType = executorType;
    this.exceptionTranslator = exceptionTranslator;
    this.sqlSessionProxy = (SqlSession) newProxyInstance(
        SqlSessionFactory.class.getClassLoader(),
        new Class[] { SqlSession.class },
        new SqlSessionInterceptor());
  }</pre> 
 <p>所以最终数据库操作有被代理SqlSessionInterceptor执行：</p> 
 <pre class="brush: java; gutter: true">public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      //有TransactionSynchronizationManager管理
      SqlSession sqlSession = getSqlSession(
          SqlSessionTemplate.this.sqlSessionFactory,
          SqlSessionTemplate.this.executorType,
          SqlSessionTemplate.this.exceptionTranslator);
      try {
        Object result = method.invoke(sqlSession, args);
        if (!isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
          // force commit even on non-dirty sessions because some databases require
          // a commit/rollback before calling close()
          sqlSession.commit(true);
        }
        return result;
      } catch (Throwable t) {
          .....
      }
    }

public static SqlSession getSqlSession(SqlSessionFactory sessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sessionFactory, NO_SQL_SESSION_FACTORY_SPECIFIED);
    notNull(executorType, NO_EXECUTOR_TYPE_SPECIFIED);

    SqlSessionHolder holder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);

    SqlSession session = sessionHolder(executorType, holder);
    if (session != null) {
      return session;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Creating a new SqlSession");
    }
   //这里看到了使用sessionfactory熟悉的打开了一个session
    session = sessionFactory.openSession(executorType);

    registerSessionHolder(sessionFactory, executorType, exceptionTranslator, session);

    return session;
  }</pre> 
 <p>关于事务配置可移步：http://www.jianshu.com/p/1d882343c036</p> 
 <h2>三、Spring中Mybatis的配置方案二</h2> 
 <h3>2.1 多数据源配置案例</h3> 
 <pre class="brush: java; gutter: true">（1）数据源配置
 &lt;bean id="dataSourceForA" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"&gt;
        &lt;property name="driverClassName" value="oracle.jdbc.driver.OracleDriver" /&gt;
        &lt;property name="url" value="${db1_url}" /&gt;
        &lt;property name="username" value="$db1_user}" /&gt;
        &lt;property name="password" value="${db1_passwd}" /&gt;
        &lt;property name="maxWait" value="${db1_maxWait}" /&gt;
        &lt;property name="maxActive" value="28" /&gt; 
        &lt;property name="initialSize" value="2" /&gt;
        &lt;property name="minIdle" value="0" /&gt;
        &lt;property name="timeBetweenEvictionRunsMillis" value="300000" /&gt;
        &lt;property name="testOnBorrow" value="false" /&gt;
        &lt;property name="testWhileIdle" value="true" /&gt;
        &lt;property name="validationQuery" value="select 1 from dual" /&gt;
        &lt;property name="filters" value="stat" /&gt;
    &lt;/bean&gt;

（2）创建sqlSessionFactory
&lt;bean id="sqlSessionFactoryForA" class="org.mybatis.spring.SqlSessionFactoryBean"&gt;
        &lt;property name="mapperLocations" value="classpath*:com/**/mapper1/*Mapper*.xml" /&gt; 
        &lt;property name="dataSource" ref="dataSourceForA" /&gt;
        &lt;property name="typeAliasesPackage" value="com.zlx.***.dal" /&gt;
&lt;/bean&gt;
    
（3）配置扫描器，扫描指定路径的mapper生成数据库操作代理类
  &lt;mybatis:scan base-package="com.zlx1.***.dal" factory-ref="sqlSessionFactoryForA" annotation="javax.annotation.Resource"/&gt;

（4）数据源配置
 &lt;bean id="dataSourceForB" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close"&gt;
        &lt;property name="driverClassName" value="oracle.jdbc.driver.OracleDriver" /&gt;
        &lt;property name="url" value="${db_url}" /&gt;
        &lt;property name="username" value="$db_user}" /&gt;
        &lt;property name="password" value="${db_passwd}" /&gt;
        &lt;property name="maxWait" value="${db_maxWait}" /&gt;
        &lt;property name="maxActive" value="28" /&gt; 
        &lt;property name="initialSize" value="2" /&gt;
        &lt;property name="minIdle" value="0" /&gt;
        &lt;property name="timeBetweenEvictionRunsMillis" value="300000" /&gt;
        &lt;property name="testOnBorrow" value="false" /&gt;
        &lt;property name="testWhileIdle" value="true" /&gt;
        &lt;property name="validationQuery" value="select 1 from dual" /&gt;
        &lt;property name="filters" value="stat" /&gt;
    &lt;/bean&gt;

（5）创建sqlSessionFactory
&lt;bean id="sqlSessionFactoryForB" class="org.mybatis.spring.SqlSessionFactoryBean"&gt;
        &lt;property name="mapperLocations" value="classpath*:com/**/mapper/*Mapper*.xml" /&gt; 
        &lt;property name="dataSource" ref="dataSourceForB" /&gt;
        &lt;property name="typeAliasesPackage" value="com.zlx.***.dal" /&gt;
&lt;/bean&gt;
    
（6）配置扫描器，扫描指定路径的mapper生成数据库操作代理类
    &lt;mybatis:scan base-package="com.zlx.***.dal" factory-ref="sqlSessionFactoryForB" annotation="javax.annotation.Resource"/&gt;</pre> 
 <p>与上节不同在在于(3)(6)</p> 
 <h3>3.2 原理简单介绍</h3> 
 <p>这里只看 &lt;mybatis:scan/&gt; 标签解析，按照惯例看jar包的spring.handler找标签解析</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-cae7a8358b9da111" rel="attachment wp-att-27212"><img class="aligncenter size-full wp-image-27212" title="5879294-cae7a8358b9da111" src="http://incdn1.b0.upaiyun.com/2017/11/331b5cb61e9400e1b28bf5264dec4d9e.png" alt=""></a><a href="http://www.importnew.com/27207.html/5879294-b8e90b2188beb326" rel="attachment wp-att-27213"><img class="aligncenter size-full wp-image-27213" title="5879294-b8e90b2188beb326" src="http://incdn1.b0.upaiyun.com/2017/11/b8cf6c6eecf84a3bfaeafddd0392b078.png" alt=""></a><a href="http://www.importnew.com/27207.html/5879294-6a0906f8cf574e00" rel="attachment wp-att-27214"><img class="aligncenter size-full wp-image-27214" title="5879294-6a0906f8cf574e00" src="http://incdn1.b0.upaiyun.com/2017/11/ee09cb07f86273fedc8ae241c98805f1.png" alt=""></a></p> 
 <p>￼<span style="font-weight: normal;">MapperScannerBeanDefinitionParser的代码如下：</span></p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27207.html/5879294-37a6a21ad87cb78f" rel="attachment wp-att-27215"><img class="aligncenter size-full wp-image-27215" title="5879294-37a6a21ad87cb78f" src="http://incdn1.b0.upaiyun.com/2017/11/afb26b7a07aeafc30303fb5bfd84cb56.png" alt=""></a></p> 
 <p>可知MapperScannerBeanDefinitionParser所做的事情和MapperScannerConfigurer类似都是内部搞了个ClassPathMapperScanner。</p> 
 <h2>四、SpringBoot中Mybatis的配置方案</h2> 
 <h3>4.1 SpringBoot中多数据源使用</h3> 
 <p>数据源一配置：</p> 
 <pre class="brush: java; gutter: true">//三、设置扫描器
@MapperScan(basePackages = "com.alibaba.zlx.web.speech.mapper",sqlSessionFactoryRef="sqlSessionFactory1")
public class TddlAutoConfiguration {

    @Autowired
    private TddlProperties properties;

    //一、创建数据源
    @Primary
    @Bean(name = "dataSource1")
    public DataSource dataSource1() throws TddlException {
        TDataSource dataSource = new TDataSource();
        dataSource.setAppName(properties.getAppName());
        dataSource.setSharding(properties.getSharding());
        dataSource.setDynamicRule(properties.getDynamicRule());
        dataSource.init();

        return dataSource;
    }
    
    //二、创建SqlSessionFactory
    @Bean(name = "sqlSessionFactory1")
    @Primary
    public SqlSessionFactory sqlSessionFactoryBean1() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        sqlSessionFactoryBean.setDataSource(dataSource1());
        sqlSessionFactoryBean.setMapperLocations(resolveMapperLocations(new String[]{"classpath:mapper/*.xml"}));

        return sqlSessionFactoryBean.getObject();
    }
    
     //四、 创建事务管理器
    @Bean(name = "txManager1")
    @Primary
    public PlatformTransactionManager txManager1(@Qualifier("dataSource1")DataSource dataSource) {
        
        
        System.out.println("-----------dataource-----" + dataSource.toString());
        return new DataSourceTransactionManager(dataSource);
    }
    
    @Bean("sqlSessionTemplate1")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory1")SqlSessionFactory sqlSessionFactory) {
        System.out.println("-----------sqlSessionFactory-----" + sqlSessionFactory.toString());

        return new SqlSessionTemplate(sqlSessionFactory);
    }
}</pre> 
 <p>数据源二配置：</p> 
 <pre class="brush: java; gutter: true">//三、设置扫描器
@MapperScan(basePackages = "com.alibaba.gh.web.speech.mapper", sqlSessionFactoryRef = "sqlSessionFactory2")

public class TddlAutoConfiguration2 {

    @Autowired
    private TddlProperties properties;

    //一、创建数据源
    @Bean(name = "dataSource2")
    public DataSource dataSource2() throws TddlException {
        TDataSource dataSource = new TDataSource();
        dataSource.setAppName(properties.getAppName());
        dataSource.setSharding(properties.getSharding());
        dataSource.setDynamicRule(properties.getDynamicRule());
        dataSource.init();

        return dataSource;
    }

    //二、创建SqlSessionFactory
    @Bean(name = "sqlSessionFactory2")
    public SqlSessionFactory sqlSessionFactoryBean1() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        sqlSessionFactoryBean.setDataSource(dataSource2());
        sqlSessionFactoryBean.setMapperLocations(resolveMapperLocations(new String[] { "classpath:mapper2/*.xml" }));

        return sqlSessionFactoryBean.getObject();
    }

    // 四、创建事务管理器
    @Bean(name = "txManager2")
    public PlatformTransactionManager txManager1(@Qualifier("dataSource2") DataSource dataSource) {

        System.out.println("-----------dataource-----" + dataSource.toString());
        return new DataSourceTransactionManager(dataSource);
    }
    @Bean("sqlSessionTemplate2")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory2")SqlSessionFactory sqlSessionFactory) {
        System.out.println("-----------sqlSessionFactory-----" + sqlSessionFactory.toString());

        return new SqlSessionTemplate(sqlSessionFactory);
    }
}</pre> 
 <p>另外SqlSessionTemplate是对SqlSessionFactory的一个包装，这里每个数据源也配置了一个，如果想使用它的话，只需要修改@mapperscan，设置sqlSessionTemplateRef替换sqlSessionFactoryRef。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>