<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/lifetragedy/article/details/8122621">袁鸣凯</a>
 </div> 
 <h1>一、前言</h1> 
 <p>上次讲了Struts结合Spring并使用Spring的JdbcTemplate来搭建工程框架后我们面临着jar库无法管理，工程发布不方便，jar包在工程内太占空间，jar包冲突，管理，甚至漏包都问题。于是我们在讲“万能框架spring(二)”前，传授了一篇番外篇，即讲利用maven来管理我们的jar库。</p> 
 <p>从今天开始我们将结合“万能框架spring(一)”与番外篇maven来更进一步丰富我们的ssx框架，那么今天讲的是使用iBatis3结合SS来构建我们的ssi框架，我们把这个框架命名为beta吧。</p> 
 <h1><a name="t1"></a>二、SSI框架</h1> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/29/1351443567_7276.jpg" alt="">
 </div> 
 <div> 
  <p>还记得我们在第十八天中讲到的我们的框架的架构图吗？上面这张是我们今天的架构图，除了Struts，Spring层，我们需要变换的是DAO层即把原来的SQL这部分换成iBatis，我们在次使用的是iBatis版本3。</p> 
  <p>由于我们在第十八天中已经说了这样的一个框架的好处其中就有：</p> 
  <p><em>层中相关技术的替换不影响到其它层面</em></p> 
  <p>所以对于我们来说我们需要改动的代码只有datasource.xml与dao层的2个接口两个类，那我们就一起来看看这个基于全注解的SSi框架是怎么样搭起来的吧。</p> 
  <h1><a name="t2"></a>三、搭建SSI框架</h1> 
  <h2><a name="t3"></a>3.1建立工程</h2> 
  <p>我们还是使用maven来建立我们的工程</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443479_6354.jpg" alt=""></p>
 </div> 
 <div> 
  <p>建完后照着翻外篇《第十九天》中的“四、如何让Maven构建的工程在eclipse里跑起来”对工程进行设置。</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443596_1163.jpg" alt=""></p>
 </div> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/29/1351443617_8118.jpg" alt="">
 </div> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/29/1351443666_7953.jpg" alt="">
 </div> 
 <div> 
  <h2></h2> 
  <h2>3.2&nbsp;增加iBatis3的jar相关包</h2> 
  <p>打开pom.xml</p> 
  <h2>第一步</h2> 
  <p>找到“slf4j”，将它在pom中的描述改成如下内容：</p> 
  <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.slf4j&lt;/groupId&gt;
    &lt;artifactId&gt;slf4j-api&lt;/artifactId&gt;
    &lt;version&gt;1.5.10&lt;/version&gt;
&lt;/dependency&gt;</pre> 
  <h2>第二步</h2> 
  <p>增加两个jar包</p> 
  <pre class="brush: java; gutter: true">&lt;dependency&gt;
    &lt;groupId&gt;org.slf4j&lt;/groupId&gt;
    &lt;artifactId&gt;slf4j-log4j12&lt;/artifactId&gt;
    &lt;version&gt;1.5.10&lt;/version&gt;
&lt;/dependency&gt;

&lt;dependency&gt;
    &lt;groupId&gt;org.apache.ibatis&lt;/groupId&gt;
    &lt;artifactId&gt;ibatis-core&lt;/artifactId&gt;
    &lt;version&gt;3.0&lt;/version&gt;
&lt;/dependency&gt;</pre> 
  <h2>3.3&nbsp;开始配置ibatis与spring结合</h2> 
  <p>打开/src/main/resources/spring/datasource下的datasource.xml，增加如下几行</p> 
  <pre class="brush: java; gutter: true">&lt;bean id="iBatisSessionFactory" class="org.sky.ssi.ibatis.IBatis3SQLSessionFactoryBean" scope="singleton"&gt;

    &lt;property name="configLocation" value="sqlmap.xml"&gt;&lt;/property&gt;

    &lt;property name="dataSource" ref="dataSource"&gt;&lt;/property&gt;

&lt;/bean&gt;

&lt;bean id="iBatisDAOSupport" class="org.sky.ssi.ibatis.IBatisDAOSupport"&gt;

&lt;/bean&gt;

&lt;bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager"&gt;

    &lt;property name="dataSource" ref="dataSource" /&gt;

&lt;/bean&gt;</pre> 
  <p>此处，我们需要4个类，它们是：</p> 
  <h3>org.sky.ssi.ibatis.IBatis3SQLSessionFactoryBean类</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.ibatis;
 
import java.io.IOException;
import java.io.Reader;
import javax.sql.DataSource;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;


/**

 *

 * IBatis3SQLSessionFactoryBean is responsible for integrating iBatis 3 &lt;p&gt;

 * with spring 3. Since all environment configurations have been moved to &lt;p&gt;

 * spring, this class takes the responsibility to get environment information&lt;p&gt;

 *  from spring configuration to generate SqlSessionFactory.

 * @author lifetragedy

 *

 */

public class IBatis3SQLSessionFactoryBean implements FactoryBean&lt;SqlSessionFactory&gt;, InitializingBean{

    rivate String configLocation;   

    private DataSource dataSource;   

    private SqlSessionFactory sqlSessionFactory;   

    private boolean useTransactionAwareDataSource = true;   

    private String environmentId = "development";

    public String getConfigLocation() {

        return configLocation;

    }

    public void setConfigLocation(String configLocation) {

        this.configLocation = configLocation;

    }

    public DataSource getDataSource() {

        return dataSource;

    }

    public void setDataSource(DataSource dataSource) {

        this.dataSource = dataSource;

    }

    public SqlSessionFactory getSqlSessionFactory() {

        return sqlSessionFactory;

    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {

        this.sqlSessionFactory = sqlSessionFactory;

    }

    public boolean isUseTransactionAwareDataSource() {

        return useTransactionAwareDataSource;

    }

    public void setUseTransactionAwareDataSource(

        boolean useTransactionAwareDataSource) {

            this.useTransactionAwareDataSource = useTransactionAwareDataSource;

    }

    public String getEnvironmentId() {

        return environmentId;

    }

    public void setEnvironmentId(String environmentId) {

        this.environmentId = environmentId;

    }

   

    public SqlSessionFactory getObject() throws Exception {   

        return this.sqlSessionFactory;   

    }   


    public Class&lt;SqlSessionFactory&gt; getObjectType() {   

        return  SqlSessionFactory.class;   

    }

   

    public boolean isSingleton() {   

        return true;   

    }   


    public void afterPropertiesSet() throws Exception {   

        this.sqlSessionFactory = this.buildSqlSessionFactory(configLocation);   

    }

  
    protected SqlSessionFactory buildSqlSessionFactory(String configLocation)   

    throws IOException {   

    if (configLocation == null) {   

        throw new IllegalArgumentException(   

        "configLocation entry is required");   

    }   

    DataSource dataSourceToUse = this.dataSource;   

    if (this.useTransactionAwareDataSource  &amp;&amp; !(this.dataSource instanceof TransactionAwareDataSourceProxy)) {   

        dataSourceToUse = new TransactionAwareDataSourceProxy(this.dataSource);   

    }   


    Environment environment = new Environment(environmentId, new IBatisTransactionFactory(dataSourceToUse), dataSourceToUse);   

    Reader reader = Resources.getResourceAsReader(configLocation);   

    XMLConfigBuilder parser = new XMLConfigBuilder(reader, null, null);   

    Configuration config = parser.parse();   

    config.setEnvironment(environment);   

    return new DefaultSqlSessionFactory(config);   

    }

}</pre> 
  <h3>org.sky.ssi.ibatis.IBatisDAOSupport</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.ibatis;

import javax.annotation.Resource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;


/**

 * Base class for all DAO class. The subclass extends this class to get

 * &lt;p&gt;

 * DAO implementation proxy.

 *

 * @author lifetragedy

 *

 * @param &lt;T&gt;

 */

public class IBatisDAOSupport&lt;T&gt; {

 

    protected Logger log = Logger.getLogger(this.getClass());

    @Resource

    private SqlSessionFactory ibatisSessionFactory;

    private T mapper;

    public SqlSessionFactory getSessionFactory() {

       return ibatisSessionFactory;

    }



    protected SqlSession getSqlSession() {

        return ibatisSessionFactory.openSession();

    }



    public T getMapper(Class&lt;T&gt; clazz) {

        mapper = getSqlSession().getMapper(clazz);

        return mapper;

    }



    public T getMapper(Class&lt;T&gt; clazz, SqlSession session) {

        mapper = session.getMapper(clazz);

        return mapper;

    }



    /**

     * close SqlSession

     */

    protected void closeSqlSession(SqlSession sqlSession) throws Exception {

        try {

                if (sqlSession != null) {

                    sqlSession.close();

                    sqlSession = null;

                }

        } catch (Exception e) {

        }

    }

}</pre> 
  <h3>org.sky.ssi.ibatis.IBatisTransaction</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class IBatisTransaction implements Transaction{

    private DataSource dataSource;

    private Connection connection;

    public IBatisTransaction(DataSource dataSource, Connection con, boolean autoCommit){

        this.dataSource = dataSource;

        this.connection = con;

    }



    public Connection getConnection(){

        eturn connection;

    }

 

    public void commit()

        throws SQLException{                        }

 

    public void rollback()

        throws SQLException{                        }

 

    public void close()

        throws SQLException{

            if(dataSource != null &amp;&amp; connection != null){

            DataSourceUtils.releaseConnection(connection, dataSource);

            }

    }

}

</pre> 
  <h3>org.sky.ssi.ibatis.IBatisTransactionFactory</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.ibatis;

 

import java.sql.Connection;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.Transaction;

import org.apache.ibatis.transaction.TransactionFactory;


public class IBatisTransactionFactory implements TransactionFactory{
                     

     private DataSource dataSource;

  
     public IBatisTransactionFactory(DataSource dataSource){

        this.dataSource = dataSource;

     }

     

     public void setProperties(Properties properties){      }

     

     public Transaction newTransaction(Connection connection, boolean flag){

        return new IBatisTransaction(dataSource,connection,flag);

     } 

}</pre> 
  <p>此三个类的作用就是在datasource.xml文件中描述的，把spring与datasource.xml中的datasource和transaction连接起来，此处尤其是“IBatis3SQLSessionFactoryBean”的写法，它通过spring中的“注入”特性，把iBatis的配置注入进spring并委托spring的context来管理iBatis（此属网上没有的资料，全部为本人在历年工程中的经验总结，并且已经在至少3个项目中进行了集成使用与相关测试）。</p> 
  <h2>建立iBatis配置文件</h2> 
  <p>我们先在/src/main/resources目录下建立一个叫sqlmap.xml的文件，内容如下：</p> 
  <pre class="brush: java; gutter: true">&lt;?xml version="1.0" encoding="UTF-8"?&gt;

&lt;!DOCTYPE configuration PUBLIC "-//ibatis.apache.org//DTD Config 3.0//EN" "http://ibatis.apache.org/dtd/ibatis-3-config.dtd"&gt;

&lt;configuration&gt;

    &lt;mappers&gt;

        &lt;mapper resource="ibatis/index.xml" /&gt;

        &lt;mapper resource="ibatis/login.xml" /&gt;

    &lt;/mappers&gt;

&lt;/configuration&gt;</pre> 
  <p>然后我们在/src/main/resources&nbsp;目录下建立index.xml与login.xml这2个xml文件。</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443716_1407.jpg" alt=""></p>
 </div> 
 <div> 
  <p>看到这儿，有人会问了：为什么不把这两个xml文件也建立在spring目录下？</p> 
  <p>原因很简单：</p> 
  <p>在datasource.xml文件内我们已经通过</p> 
  <pre class="brush: java; gutter: true">&lt;bean id="iBatisSessionFactory" class="org.sky.ssi.ibatis.IBatis3SQLSessionFactoryBean" scope="singleton"&gt;

    &lt;property name="configLocation" value="sqlmap.xml"&gt;&lt;/property&gt;

    &lt;property name="dataSource" ref="dataSource"&gt;&lt;/property&gt;

&lt;/bean&gt;</pre> 
  <p>这样的方式把iBatis委托给了spring，iBatis的核心就是这个sqlmap.xml文件了，而在这个sqlmap.xml文件已经引用了login.xml与index.xml文件了。</p> 
  <p>而我们的web.xml文件里有这么一句：</p> 
  <pre class="brush: java; gutter: true">&lt;context-param&gt;

    &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;

    &lt;param-value&gt;/WEB-INF/classes/spring/**/*.xml&lt;/param-value&gt;

&lt;/context-param&gt;</pre> 
  <p>因此如果我们再把ibatis/index.xml与ibatis/login.xml再建立到src/main/resources/spring目录下，spring于是会在容器启动时试图加载这两个xml文件，然后一看这两个xml文件不是什么spring的bean，直接抛错，对吧？</p> 
  <p>其们等一会再来看login.xml文件与index.xml文件，我们先来搞懂iBatis调用原理.</p> 
  <h2>3.4 iBatis调用原理</h2> 
  <p>1）iBatis就是一个dao层，它又被称为sqlmapping，它的sql是书写在一个.xml文件内的，在该xml文件内会将相关的sql绑定到相关的dao类的方法。</p> 
  <p>2）在调用结束时我们需要在finally块中关闭相关的sql调用。</p> 
  <p>我们来看一个例子。</p> 
  <h3>login.xml文件</h3> 
  <pre class="brush: java; gutter: true">&lt;?xml version="1.0" encoding="UTF-8"?&gt;

&lt;!DOCTYPE mapper

PUBLIC "-//ibatis.apache.org//DTD Mapper 3.0//EN"

"http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd"&gt;

&lt;mapper namespace="org.sky.ssi.dao.LoginDAO"&gt;

    &lt;select id="validLogin" resultType="int" parameterType="java.util.Map"&gt;

        &lt;![CDATA[

        SELECT count(1) from t_login where login_id= #{loginId} and login_pwd=#{loginPwd}

        ]]&gt;

    &lt;/select&gt;

&lt;/mapper&gt;</pre> 
  <p>该DAO指向了一个接口org.sky.ssi.dao.LoginDAO，该dao接受一个sql，并且接受一个Map类型的参数。</p> 
  <p>那么我们来看该DAO</p> 
  <h3>LoginDao.java</h3> 
 </div> 
 <div> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.dao;

import java.util.Map;

public interface LoginDAO {

    public int validLogin(Map&lt;String, Object&gt; paraMap) throws Exception;

}</pre> 
 </div> 
 <div> 
  <h3>LoginImpl.java</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.dao.impl;


import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import org.sky.ssi.dao.LoginDAO;

import org.sky.ssi.ibatis.IBatisDAOSupport;

import org.springframework.stereotype.Repository;
 

@Repository

public class LoginDAOImpl extends IBatisDAOSupport&lt;LoginDAO&gt; implements LoginDAO {

    public int validLogin(Map&lt;String, Object&gt; paraMap) throws Exception {

        SqlSession session = this.getSqlSession();

        try {

            return this.getMapper(LoginDAO.class, session).validLogin(paraMap);

        } catch (Exception e) {

            log.error(e.getMessage(), e);

            throw new Exception(e);

        } finally {

            this.closeSqlSession(session);

        }

    }

}</pre> 
  <p>很简单吧，一切逻辑都在xml文件内。</p> 
  <p>一定记得不要忘了在finally块中关闭相关的sql调用啊，要不然将来工程出了OOM的错误不要怪我啊.</p> 
  <h2>3.5 index模块</h2> 
  <h3>Index.xml文件</h3> 
  <pre class="brush: java; gutter: true">&lt;?xml version="1.0" encoding="UTF-8"?&gt;

&lt;!DOCTYPE mapper

PUBLIC "-//ibatis.apache.org//DTD Mapper 3.0//EN"

"http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd"&gt;

&lt;mapper namespace="org.sky.ssi.dao.StudentDAO"&gt;

    &lt;select id="getAllStudent" resultType="org.sky.ssi.dbo.StudentDBO"&gt;

        &lt;![CDATA[

        SELECT student_no studentNo, student_name studentName from t_student

        ]]&gt;

    &lt;/select&gt;



    &lt;update id="addStudent" parameterType="java.util.Map"&gt;

        insert into t_student(student_no, student_name)values(seq_student_no.nextval,#{stdName})

    &lt;/update&gt;

    &lt;update id="delStudent" parameterType="java.util.Map"&gt;

        delete from t_student where student_no=#{stdNo}

    &lt;/update&gt;

&lt;/mapper&gt;</pre> 
  <p>它指向了StudentDAO这个接口</p> 
  <h3>StudentDAO.java</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.dao;


import org.sky.ssi.dbo.StudentDBO;

import org.sky.ssi.student.form.*;

import java.util.*;


public interface StudentDAO {


    public List&lt;StudentDBO&gt; getAllStudent() throws Exception;

    public void addStudent(Map&lt;String, Object&gt; paraMap) throws Exception;

    public void delStudent(Map&lt;String, Object&gt; paraMap) throws Exception;

}</pre> 
  <h3>StudentDAOImpl.java</h3> 
  <pre class="brush: java; gutter: true">package org.sky.ssi.dao.impl;

import java.util.List;

import java.util.Map;

import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;

import org.apache.ibatis.session.SqlSession;

import org.sky.ssi.dao.StudentDAO;

import org.sky.ssi.ibatis.IBatisDAOSupport;

import org.sky.ssi.dbo.StudentDBO;
 
import org.springframework.stereotype.Repository;


@Repository

public class StudentDAOImpl extends IBatisDAOSupport&lt;StudentDAO&gt; implements StudentDAO {


    @Override

    public List&lt;StudentDBO&gt; getAllStudent() throws Exception {

        SqlSession session = this.getSqlSession();

        try {

            return this.getMapper(StudentDAO.class, session).getAllStudent();

        } catch (Exception e) {

            throw new Exception(e);

        } finally {

            this.closeSqlSession(session);

        }

    }



    public void addStudent(Map&lt;String, Object&gt; paraMap) throws Exception {

        SqlSession session = this.getSqlSession();

        try {

            this.getMapper(StudentDAO.class, session).addStudent(paraMap);

        } catch (Exception e) {

            throw new Exception(e);

        } finally {

            this.closeSqlSession(session);

        }

    }



    public void delStudent(Map&lt;String, Object&gt; paraMap) throws Exception {

        SqlSession session = this.getSqlSession();

        try {

            this.getMapper(StudentDAO.class, session).delStudent(paraMap);

        } catch (Exception e) {

            throw new Exception(e);

        } finally {

            this.closeSqlSession(session);

        }

    }

}</pre> 
  <h2>3.6 Service接口微微有些改变</h2> 
  <p>为了演示给大家看 iBatis接受多个参数的例子因此我们把原来的如：login(String loginId, String loginPwd)这样的方法改成了public int validLogin(Map&lt;String, Object&gt; paraMap) throws Exception;这样的结构，请大家注意。</p> 
  <h1>四、beta工程中的增加功能</h1> 
  <h2>4.1&nbsp;增加了一个filter</h2> 
  <p>在我们的web.xml文件中</p> 
  <pre class="brush: java; gutter: true">&lt;filter&gt;

    &lt;filter-name&gt;LoginFilter&lt;/filter-name&gt;

    &lt;filter-class&gt;org.sky.ssi.filter.LoginFilter&lt;/filter-class&gt;

    &lt;init-param&gt;

	    &lt;param-name&gt;exclude&lt;/param-name&gt;

	    &lt;param-value&gt;/jsp/login/login.jsp,

	                 /login.do

	    &lt;/param-value&gt;

    &lt;/init-param&gt;

&lt;/filter&gt;

&lt;filter-mapping&gt;

    &lt;filter-name&gt;LoginFilter&lt;/filter-name&gt;

    &lt;url-pattern&gt;*.jsp&lt;/url-pattern&gt;

&lt;/filter-mapping&gt;

&lt;filter-mapping&gt;

    &lt;filter-name&gt;LoginFilter&lt;/filter-name&gt;

    &lt;url-pattern&gt;/servlet/*&lt;/url-pattern&gt;

&lt;/filter-mapping&gt;

&lt;filter-mapping&gt;

    &lt;filter-name&gt;LoginFilter&lt;/filter-name&gt;

    &lt;url-pattern&gt;*.do&lt;/url-pattern&gt;

&lt;/filter-mapping&gt;</pre> 
  <p>有了这个filter我们就不用在我们的web工程中每一个action、每 个jsp里进行“用户是否登录”的判断了，它会自动根据配置除去“exclude”中的相关web resource，全部走这个“是否登录”的判断。</p> 
  <p>注意此处这个exclude是笔者自己写的，为什么要exclude？</p> 
  <p><em>如果你不exclude，试想一个用户在login.jsp中填入相关的登录信息后点一下login按钮跳转到了login.do，而这两个web resource由于没有被“排除”出“需要登录校验”，因此每次你一调用login.jsp, login.do这个filter就都会强制要求你再跳转到login.jsp，那么我们一个用户从login.jsp登录完后再跳回login.jsp再跳回，再跳回，如此重复，进入死循环。</em></p> 
  <h2>4.2&nbsp;增加了一个自动记录异常的日志功能</h2> 
  <p>在我们的applicationContext.xml文件中</p> 
  <pre class="brush: java; gutter: true">&lt;bean

    id="methodLoggerAdvisor"

    class="org.sky.ssi.util.LoggerAdvice" &gt;

&lt;/bean&gt;

&lt;aop:config&gt;

    &lt;aop:aspect

        id="originalBeanAspect"

        ref="methodLoggerAdvisor" &gt;

        &lt;aop:pointcut

            id="loggerPointCut"

            expression="execution(* org.sky.ssi.service.impl.*.*(..))" /&gt;

        &lt;aop:around

            method="aroundAdvice"

            pointcut-ref="loggerPointCut" /&gt;

    &lt;/aop:aspect&gt;

&lt;/aop:config&gt;</pre> 
  <p>这样，我们的dao层、service层、有错就只管往外throw，框架一方面在接到相关的exception会进行数据库事务的自动回滚外，还会自动把service层抛出的exception记录在log文件中。</p> 
  <h1>五、测试我们的工程</h1> 
  <p>确认我们的StudentServiceImpl中删除学生的delStudent方法内容如下：</p> 
  <pre class="brush: java; gutter: true">public void delStudent(String[] stdNo) throws Exception {

  for (String s : stdNo) {

         Map&lt;String, Object&gt; paraMap = new HashMap&lt;String, Object&gt;();

         paraMap.put("stdNo", s);

         studentDAO.delStudent(paraMap);

         throw new Exception("force system to throw a exception");

  }

}</pre> 
  <p>我们把beta工程添加入我们在eclipse中配好的j2eeserver中去并启动起来。</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443749_2788.jpg" alt=""></p>
 </div> 
 <div> 
  <p>在IE中输入：<a href="http://localhost:8080/beta/index.do" target="_blank">http://localhost:8080/beta/index.do</a>。 系统直接跳到login界面</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443763_4774.jpg" alt=""></p>
 </div> 
 <div> 
  <p>我们输入相关的用户名写密码。</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443783_2483.jpg" alt=""></p>
 </div> 
 <div> 
  <p>我们选中“13号学生高乐高”与“9号学生”，点“deletestudent”按钮。</p> 
 </div> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/29/1351443800_1410.jpg" alt="">
 </div> 
 <div> 
  <p>后台抛错了，查看数据库内的数据</p> 
  <p><img src="http://img.my.csdn.net/uploads/201210/29/1351443818_1679.jpg" alt=""></p>
 </div> 
 <div> 
  <p>数据还在，说明我们的iBatis的事务已经在spring中启作用了.</p> 
  <p>再次更改StudentServiceImpl.java类中的delStudent方法，把“throw new Exception(“force system to throw a exception”);”注释掉，再来运行</p> 
 </div> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/29/1351443832_3922.jpg" alt="">
 </div> 
 <div> 
  <p>我们再次选 中9号和13号学生，点deletestudent按钮，删除成功，这个够13的人终于被删了，呵呵。</p> 
 </div> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>