<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://www.cnblogs.com/yougewe/p/7466677.html">等你归去来</a>
 </div> 
 <p>事务的作用，使我们操作能够连贯起来。而spring则是提供了一个更简单的方法，只要使用 @Transactional 一个注解，就可以保证操作的连贯性了。</p> 
 <p>普通用法，稍后再说，这里要说的是： 在最外面的方法中，有一个@Transactional 的注解，当有抛出异常时，则进行回滚操作：</p> 
 <pre class="brush: java; gutter: true">@Transactional(readOnly = false, rollbackFor = Throwable.class, isolation = Isolation.REPEATABLE_READ)</pre> 
 <p>原本这个方法运行得好好的，但是有一天，我们需要在这个方法里添加一个新业务操作，而且这个业务操作是不要求回滚的，类似于做日志记录一类的。WHAT SHOULD I DO ?</p> 
 <p>由于业务的独特性，我能够快速想到的是，在这个类里面加一个private方法，然后直接去调用就ok了，如果说还是考虑到回滚的话，我也快速想到 @Transactional 的NOT_SUPPORTED传播特性，如：</p> 
 <pre class="brush: java; gutter: true">@Transactional(propagation = Propagation.NOT_SUPPORTED)
private void doMyExJob(UserDebitCardBean userDebitCard) {
    System.out.println("do my job...");
    //do my job...
}</pre> 
 <p>这看起来很合理，没毛病。</p> 
 <p>然而就是运行不起来，只要外面调用的方法一抛出异常，那么这个新方法的数据操作将会被回滚。妈蛋，到底哪里出了问题？？？仔细查了下资料，原来 @Transactional 注解由于原理决定了他只能作用于public方法中，而这里改为private，就完全被忽略无视了。OK，改呗：</p> 
 <pre class="brush: java; gutter: true">@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void doMyExJob(UserDebitCardBean userDebitCard) {
    System.out.println("do my job...");
    //do my job...
}</pre> 
 <p>感觉应该好了，然而并没有。我也是醉了，这个问题，如果仔细花时间，找原理是没有问题的，但是在关键时刻来这么一下，还是很不爽的。 网上看到一哥们说，还必须要将方法写到另一个类中，而且要通过spring的注入方式进行调用，才可以。好吧，那我就按照他的来，结果真的成功了。</p> 
 <pre class="brush: java; gutter: true">//在接口中进行了定义，能够注入
@Override 
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void doMyExJob(UserDebitCardBean userDebitCard) {
    System.out.println("do my job...");
    //do my job...
}</pre> 
 <p>总算可以了，在赶时间的时候，能够解决问题的，就是好方法。至此，问题解决。1. 使用public访求；2. 写在外部类中，可被调用； 3. 使用注入的方式进行该方法的执行。</p> 
 <p>说实话，spring这种事务还是有点不太好用的，要求太多，当然了，有很大部分原因是我没有理解其精髓。OK，下面我们来看看spring事务的讲解：</p> 
 <p>在配置文件中，默认情况下，&lt;tx:annotation-driven&gt;会自动使用名称为transactionManager的事务管理器。所以，如果定义的事务管理器名称为transactionManager，那么就可以直接使用&lt;tx:annotation-driven/&gt;。如下：</p> 
 <pre class="brush: java; gutter: true">&lt;!-- 配置事务管理器 --&gt;
&lt;beanid="transactionManager"
    class="org.springframework.jdbc.datasource.DataSourceTransactionManager"
    p:dataSource-ref="dataSource"&gt;
&lt;/bean&gt;

&lt;!-- enables scanning for @Transactional annotations --&gt;
&lt;tx:annotation-driven/&gt;</pre> 
 <p>&lt;tx:annotation-driven&gt;一共有四个属性如下，</p> 
 <ul> 
  <li>mode：指定Spring事务管理框架创建通知bean的方式。可用的值有proxy和aspectj。前者是默认值，表示通知对象是个JDK代理；后者表示Spring AOP会使用AspectJ创建代理</li> 
  <li>proxy-target-class：如果为true，Spring将创建子类来代理业务类；如果为false，则使用基于接口的代理。（如果使用子类代理，需要在类路径中添加CGLib.jar类库）</li> 
  <li>order：如果业务类除事务切面外，还需要织入其他的切面，通过该属性可以控制事务切面在目标连接点的织入顺序。</li> 
  <li>transaction-manager：指定到现有的PlatformTransaction Manager bean的引用，通知会使用该引用</li> 
 </ul> 
 <p>@Transactional的属性</p> 
 <p>isolation 枚举org.springframework.transaction.annotation.Isolation的值 事务隔离级别</p> 
 <p>noRollbackFor Class&lt;? extends Throwable&gt;[] 一组异常类，遇到时不回滚。默认为{}</p> 
 <p>noRollbackForClassName Stirng[] 一组异常类名，遇到时不回滚，默认为{}</p> 
 <p>propagation 枚举org.springframework.transaction.annotation.Propagation的值 事务传播行为</p> 
 <p>readOnly boolean 事务读写性</p> 
 <p>rollbackFor Class&lt;? extends Throwable&gt;[] 一组异常类，遇到时回滚</p> 
 <p>rollbackForClassName Stirng[] 一组异常类名，遇到时回滚</p> 
 <p>timeout int 超时时间，以秒为单位</p> 
 <p>value String 可选的限定描述符，指定使用的事务管理器</p> 
 <p>@Transactional标注的位置<br> @Transactional注解可以标注在类和方法上，也可以标注在定义的接口和接口方法上。<br> 如果我们在接口上标注@Transactional注解，会留下这样的隐患：因为注解不能被继承，所以业务接口中标注的@Transactional注解不会被业务实现类继承。所以可能会出现不启动事务的情况。所以，spring建议我们将@Transaction注解在实现类上。<br> 在方法上的@Transactional注解会覆盖掉类上的@Transactional。</p> 
 <p>注意：</p> 
 <p>@Transactional 可以作用于接口、接口方法、类以及类方法上。当作用于类上时，该类的所有 public 方法将都具有该类型的事务属性，同时，我们也可以在方法级别使用该标注来覆盖类级别的定义。</p> 
 <p>虽然 @Transactional 注解可以作用于接口、接口方法、类以及类方法上，但是 Spring 建议不要在接口或者接口方法上使用该注解，因为这只有在使用基于接口的代理时它才会生效。另外， @Transactional 注解应该只被应用到 public 方法上，这是由 Spring AOP 的本质决定的。如果你在 protected、private 或者默认可见性的方法上使用 @Transactional 注解，这将被忽略，也不会抛出任何异常。</p> 
 <p>默认情况下，只有来自外部的方法调用才会被AOP代理捕获，也就是，类内部方法调用本类内部的其他方法并不会引起事务行为，即使被调用方法使用@Transactional注解进行修饰。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>