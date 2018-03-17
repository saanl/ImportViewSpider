<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="https://github.com/chanjarster/spring-test-examples">chanjarster</a>
 </div> 
 <p>在了解学习本项目提供的例子之前，先了解一下什么是单元测试（Unit Testing，简称UT)和集成测试（Integration Testing，简称IT）。</p> 
 <p>如果你之前没有深究过这两个概念，那么你可能会得出如下错误的答案：</p> 
 <p><strong>错误答案1：</strong></p> 
 <blockquote>
  <p>单元测试就是对一个方法进行测试的测试</p>
 </blockquote> 
 <p>听上去很像那么回事儿，对吧？单元测试，就是测一个逻辑单元，所以就测一个方法就是单元测试，听上去很有道理是不是？但是，那么测试两个方法（这两个方法互相关联）的话叫什么呢？</p> 
 <p><strong>错误答案2：</strong></p> 
 <blockquote>
  <p>集成测试是把几个方法或者几个类放在一起测试</p>
 </blockquote> 
 <p>既然前面单元测试只测一个方法，那么几个方法放在一起测就是集成测试，听上去挺有道理的。那么是不是只要测一个以上的方法就是集成测试呢？</p> 
 <p><strong>错误答案3：</strong></p> 
 <blockquote>
  <p>集成测试就是和其他系统联合调试做的测试</p>
 </blockquote> 
 <p>听上去有点像SOA或者现在流行的微服务是吧。做这种测试的时候必须得各个开发团队紧密配合，一个不小心就会测试失败，然后就是各种返工，总之难度和火箭发射有的一拼。</p> 
 <p>那么正确答案是什么？其实这两个概念的解释比较冗长这里就不细讲了，只需记住UT和IT具备以下特征：</p> 
 <ol> 
  <li>UT和IT必须是自动化的。</li> 
  <li>UT只专注于整个系统里的某一小部分，粒度没有规定，一般都比较小可以到方法级别。比如某个字符串串接方法。</li> 
  <li>UT不需要连接外部系统，在内存里跑跑就行了。</li> 
  <li>IT需要连接外部系统，比如连接数据库做CRUD测试。</li> 
  <li>测试环境和生产环境是隔离的。</li> 
  <li>能做UT的就不要做IT。</li> 
 </ol> 
 <h2>参考链接：</h2> 
 <ol> 
  <li><a href="https://martinfowler.com/bliki/UnitTest.html" class="external" rel="nofollow" target="_blank">Martin Fowler – Unit Test</a></li> 
  <li><a href="https://en.wikipedia.org/wiki/Unit_testing" class="external" rel="nofollow" target="_blank">Wikipedia – Unit Testing</a></li> 
  <li><a href="https://en.wikipedia.org/wiki/Integration_testing" class="external" rel="nofollow" target="_blank">Wikipedia – Integration Testing</a></li> 
 </ol> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>