<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://blog.stormma.me/2017/05/08/springboot%E5%AE%9A%E6%97%B6%E4%BB%BB%E5%8A%A1%E8%B8%A9%E5%9D%91%E8%AE%B0%E5%BD%95/">StormMa</a>
 </div> 
 <h2>前言</h2> 
 <p>springboot已经支持了定时任务Schedule模块，一般情况已经完全能够满足我们的实际需求。今天就记录一下我使用 schedule 时候踩的坑吧。</p> 
 <p>想要使用定时，我们首先要开启支持，其实就是在启动类上面加个注解就 Ok。</p> 
 <pre class="brush: java; gutter: true">@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}</pre> 
 <p>这篇博客的主题是记录踩的坑，具体定时任务怎么使用我就不写了，有需要的参考我的博客&nbsp;<a style="font-weight: normal;" href="http://blog.csdn.net/strommaybin/article/details/54767485" class="external" rel="nofollow" target="_blank">Spring定时任务</a>。</p> 
 <p>今天踩的这个坑和 cron 表达式有关，我们就先来看看 cron 表达式的解释吧:<br> Cron表达式是一个字符串，字符串以5或6个空格隔开，分为6或7个域，每一个域代表一个含义</p> 
 <p>解释</p> 
 <pre class="brush: java; gutter: true">* 第一位，表示秒，取值0-59
* 第二位，表示分，取值0-59
* 第三位，表示小时，取值0-23
* 第四位，日期天/日，取值1-31
* 第五位，日期月份，取值1-12
* 第六位，星期，取值1-7，星期一，星期二...，注：不是第1周，第二周的意思
          另外：1表示星期天，2表示星期一。
* 第7为，年份，可以留空，取值1970-2099</pre> 
 <pre class="brush: java; gutter: true">(*)星号：可以理解为每的意思，每秒，每分，每天，每月，每年...
(?)问号：问号只能出现在日期和星期这两个位置，表示这个位置的值不确定，每天3点执行，所以第六位星期的位置，我们是不需要关注的，就是不确定的值。同时：日期和星期是两个相互排斥的元素，通过问号来表明不指定值。比如，1月10日，比如是星期1，如果在星期的位置是另指定星期二，就前后冲突矛盾了。
(-)减号：表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12
(,)逗号：表达一个列表值，如在星期字段中使用“1,2,4”，则表示星期一，星期二，星期四
(/)斜杠：如：x/y，x是开始值，y是步长，比如在第一位（秒） 0/15就是，从0秒开始，每15秒，最后就是0，15，30，45，60    另：*/y，等同于0/y</pre> 
 <p>注: 这个是官方解释</p> 
 <pre class="brush: java; gutter: true">0 0 3 * * ?     每天3点执行
0 5 3 * * ?     每天3点5分执行
0 5 3 ? * *     每天3点5分执行，与上面作用相同
0 5/10 3 * * ?  每天3点的 5分，15分，25分，35分，45分，55分这几个时间点执行
0 10 3 ? * 1    每周星期天，3点10分 执行，注：1表示星期天    
0 10 3 ? * 1#3  每个月的第三个星期，星期天 执行，#号只能出现在星期的位置</pre> 
 <p>在此我要说明，springBoot 中的 schedule 支持的 cron 表达式和这个不太相符，官方说的星期表示，1是周天，依次类推，但是我在测试过程中，1实际上代表的就是周一，口说无凭<br> 那我就来贴代码和测试结果吧.</p> 
 <pre class="brush: java; gutter: true">@Component
@EnableScheduling
public class Task {
    private static final Logger LOGGER = MyLogger.getLogger(Task.class);
    @Scheduled(cron = "0 46 20 ? * 1")
    public void task() {
        LOGGER.info("听说今天是周日");
    }</pre> 
 <p>测试结果:</p> 
 <pre class="brush: java; gutter: true">2017-05-08 20:46:00.006  INFO 18838 --- [pool-1-thread-1] com.yiyexy.task.Task                     : 听说今天是周日</pre> 
 <p>按照上面的解释来讲，第六域是星期，并且值是1那么代表是周日运行，但是我的运行结果表明是周一运行，我在此表示很无奈。<br> 最后我觉得用单词来表示周几，这样就不会出这种问题了，于是</p> 
 <pre class="brush: java; gutter: true">@Component
@EnableScheduling
public class Task {
    private static final Logger LOGGER = MyLogger.getLogger(Task.class);
    @Scheduled(cron = "0 49 20 ? * MON")
    public void task() {
        LOGGER.info("听说今天是周日");
    }
}</pre> 
 <p>测试结果:</p> 
 <pre class="brush: java; gutter: true">2017-05-08 20:49:00.005  INFO 18864 --- [pool-1-thread-1] com.yiyexy.task.Task                     : 听说今天是周日</pre> 
 <p>好了，这个坑就记录到这吧，最后奉上一句，时间是检验真理的唯一标准。</p> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>