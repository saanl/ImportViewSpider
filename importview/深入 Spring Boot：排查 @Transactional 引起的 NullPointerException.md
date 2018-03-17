<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/hengyunabc/article/details/78806296">hengyunabc</a>
 </div> 
 <h2>写在前面</h2> 
 <p>这个demo来说明怎么排查一个@Transactional引起的NullPointerException。</p> 
 <p>https://github.com/hengyunabc/spring-boot-inside/tree/master/demo-Transactional-NullPointerException</p> 
 <h2>定位 NullPointerException 的代码</h2> 
 <p>Demo是一个简单的spring事务例子，提供了下面一个StudentDao，并用@Transactional来声明事务：</p> 
 <pre class="brush: java; gutter: true">@Component
@Transactional
public class StudentDao {

    @Autowired
    private SqlSession sqlSession;

    public Student selectStudentById(long id) {
        return sqlSession.selectOne("selectStudentById", id);
    }

    public final Student finalSelectStudentById(long id) {
        return sqlSession.selectOne("selectStudentById", id);
    }
}</pre> 
 <p>应用启动后，会依次调用selectStudentById和finalSelectStudentById：</p> 
 <pre class="brush: java; gutter: true">    @PostConstruct
    public void init() {
        studentDao.selectStudentById(1);
        studentDao.finalSelectStudentById(1);
    }</pre> 
 <p>用mvn spring-boot:run 或者把工程导入IDE里启动，抛出来的异常信息是：</p> 
 <pre class="brush: java; gutter: true">Caused by: java.lang.NullPointerException
    at sample.mybatis.dao.StudentDao.finalSelectStudentById(StudentDao.java:27)
    at com.example.demo.transactional.nullpointerexception.DemoNullPointerExceptionApplication.init(DemoNullPointerExceptionApplication.java:30)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(InitDestroyAnnotationBeanPostProcessor.java:366)
    at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(InitDestroyAnnotationBeanPostProcessor.java:311)</pre> 
 <p>为什么应用代码里执行selectStudentById没有问题，而执行finalSelectStudentById就抛出NullPointerException?</p> 
 <p>同一个bean里，明明SqlSession sqlSession已经被注入了，在selectStudentById里它是非null的。为什么finalSelectStudentById函数里是null？</p> 
 <h2>获取实际运行时的类名</h2> 
 <p>当然，我们对比两个函数，可以知道是因为finalSelectStudentById的修饰符是final。但是具体原因是什么呢？</p> 
 <p>我们先在抛出异常的地方打上断点，调试代码，获取到具体运行时的class是什么：</p> 
 <pre class="brush: java; gutter: true">System.err.println(studentDao.getClass());</pre> 
 <p>打印的结果是：</p> 
 <pre class="brush: java; gutter: true">class sample.mybatis.dao.StudentDao$$EnhancerBySpringCGLIB$$210b005d</pre> 
 <p>可以看出是一个被spring aop处理过的类，但是它的具体字节码内容是什么呢？</p> 
 <h2>dumpclass分析</h2> 
 <p>我们使用dumpclass工具来把jvm里的类dump出来：</p> 
 <p>https://github.com/hengyunabc/dumpclass</p> 
 <pre class="brush: java; gutter: true">wget http://search.maven.org/remotecontent?filepath=io/github/hengyunabc/dumpclass/0.0.1/dumpclass-0.0.1.jar -O dumpclass.jar</pre> 
 <p>找到java进程pid：</p> 
 <pre class="brush: java; gutter: true">$ jps
5907 DemoNullPointerExceptionApplication</pre> 
 <p>把相关的类都dump下来：</p> 
 <pre class="brush: java; gutter: true">sudo java -jar dumpclass.jar 5907 'sample.mybatis.dao.StudentDao*' /tmp/dumpresult</pre> 
 <h2>反汇编分析</h2> 
 <p>用javap或者图形化工具jd-gui来反编绎sample.mybatis.dao.StudentDao$$EnhancerBySpringCGLIB$$210b005d。</p> 
 <p>反编绎后的结果是：</p> 
 <ol> 
  <li>class StudentDao$$EnhancerBySpringCGLIB$$210b005d extends StudentDao</li> 
  <li>StudentDao$$EnhancerBySpringCGLIB$$210b005d里没有finalSelectStudentById相关的内容</li> 
  <li>selectStudentById实际调用的是this.CGLIB$CALLBACK_0，即MethodInterceptor tmp4_1，等下我们实际debug，看具体的类型</li> 
 </ol> 
 <pre class="brush: java; gutter: true">  public final Student selectStudentById(long paramLong)
  {
    try
    {
      MethodInterceptor tmp4_1 = this.CGLIB$CALLBACK_0;
      if (tmp4_1 == null)
      {
        tmp4_1;
        CGLIB$BIND_CALLBACKS(this);
      }
      MethodInterceptor tmp17_14 = this.CGLIB$CALLBACK_0;
      if (tmp17_14 != null)
      {
        Object[] tmp29_26 = new Object[1];
        Long tmp35_32 = new java/lang/Long;
        Long tmp36_35 = tmp35_32;
        tmp36_35;
        tmp36_35.&lt;init&gt;(paramLong);
        tmp29_26[0] = tmp35_32;
        return (Student)tmp17_14.intercept(this, CGLIB$selectStudentById$0$Method, tmp29_26, CGLIB$selectStudentById$0$Proxy);
      }
      return super.selectStudentById(paramLong);
    }
    catch (RuntimeException|Error localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }</pre> 
 <p>再来实际debug，尽管StudentDao$$EnhancerBySpringCGLIB$$210b005d的代码不能直接看到，但是还是可以单步执行的。</p> 
 <p>在debug时，可以看到</p> 
 <p>1. StudentDao$$EnhancerBySpringCGLIB$$210b005d里的所有field都是null</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27777.html/cglib-field" rel="attachment wp-att-27778"><img class="aligncenter size-full wp-image-27778" title="cglib-field" src="http://incdn1.b0.upaiyun.com/2018/01/77121311362a21b223d1cc06e4a0caca.png" alt=""></a></p> 
 <p>2. this.CGLIB$CALLBACK_0的实际类型是CglibAopProxy$DynamicAdvisedInterceptor，在这个Interceptor里实际保存了原始的target对象</p> 
 <p style="text-align: center;"><a href="http://www.importnew.com/27777.html/cglib-target" rel="attachment wp-att-27779"><img class="aligncenter size-full wp-image-27779" title="cglib-target" src="http://incdn1.b0.upaiyun.com/2018/01/5e1ba3b2a7abda6177e477490e347dfd.png" alt=""></a></p> 
 <p>3. CglibAopProxy$DynamicAdvisedInterceptor在经过TransactionInterceptor处理之后，最终会用反射调用自己保存的原始target对象</p> 
 <h2>抛出异常的原因</h2> 
 <p>所以整理下整个分析：</p> 
 <ol> 
  <li>在使用了@Transactional之后，spring aop会生成一个cglib代理类，实际用户代码里@Autowired注入的StudentDao也是这个代理类的实例</li> 
  <li>cglib生成的代理类StudentDao$$EnhancerBySpringCGLIB$$210b005d继承自StudentDao</li> 
  <li>StudentDao$$EnhancerBySpringCGLIB$$210b005d里的所有field都是null</li> 
  <li>StudentDao$$EnhancerBySpringCGLIB$$210b005d在调用selectStudentById，实际上通过CglibAopProxy$DynamicAdvisedInterceptor，最终会用反射调用自己保存的原始target对象</li> 
  <li>所以selectStudentById函数的调用没有问题</li> 
 </ol> 
 <p>那么为什么finalSelectStudentById函数里的SqlSession sqlSession会是null，然后抛出NullPointerException？</p> 
 <ol> 
  <li>StudentDao$$EnhancerBySpringCGLIB$$210b005d里的所有field都是null</li> 
  <li>finalSelectStudentById函数的修饰符是final，cglib没有办法重写这个函数</li> 
  <li>当执行到finalSelectStudentById里，实际执行的是原始的StudentDao里的代码</li> 
  <li>但是对象是StudentDao$$EnhancerBySpringCGLIB$$210b005d的实例，它里面的所有field都是null，所以会抛出NullPointerException</li> 
 </ol> 
 <h2>解决问题办法</h2> 
 <ol> 
  <li>最简单的当然是把finalSelectStudentById函数的final修饰符去掉</li> 
  <li>还有一种办法，在StudentDao里不要直接使用sqlSession，而通过getSqlSession()函数，这样cglib也会处理getSqlSession()，返回原始的target对象</li> 
 </ol> 
 <h2>总结</h2> 
 <ol> 
  <li>排查问题多debug，看实际运行时的对象信息</li> 
  <li>对于cglib生成类的字节码，可以用dumpclass工具来dump，再反编绎分析</li> 
 </ol> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>