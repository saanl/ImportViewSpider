<div class="entry"> 
 <div class="copyright-area">
  原文出处： 
  <a ref="nofollow" target="_blank" href="http://blog.csdn.net/lifetragedy/article/details/8110301">袁鸣凯</a>
 </div> 
 <h1>一、前言</h1> 
 <p>上次大家拿了我上传的工程后，有些人自己通过spring3,struts1.3，hibernate3的download的包自行去装配jar包到工程的WEB-INF\lib目录下。有些是通过我上传的alpha_lib包把里面的jar文件一股脑的copy到了工程的WEB-INF\lib目录下去。</p> 
 <p>有时经常还会发生少包了，ClassNotFound这样的错误，或者是一些因为缺包还引起的各种莫名奇妙的错误，呵呵，是不是够折腾的啊？</p> 
 <p>尝过苦头了，才知道幸苦！那么我们有没有一种更好的方式，比如说：</p> 
 <p>我下了博主的工程，打一条命令或者是在eclipse里build一下，这个工程需要的jar文件自动跑到我的工程里呢？</p> 
 <p>有的！这就是maven！！！</p> 
 <p>因此今天就要讲利用maven来构建我们的工程以及如何在eclipse里跑由maven构建出的web工程（网上这方面资料不全，因此个人总结了经验一并分享给了大家）。</p> 
 <h1>二、传统构建与使用maven构建</h1> 
 <h3>传统模式：</h3> 
 <p>《第十八》天中的这个工程的构建就是一个标准的传统模式的构建，为大多数人所接受。</p> 
 <p>它把所有的jar文件都摆放在工程的WEB-INF\lib目录下并使用ant来打包和发布. 这样做的好处是简单、直观、明了。坏处是：构建该工程的人除非很有经验，否则就会出现因为少jar文件或者因为jar文件重复而在布署或者是在运行时引起各种各样的稀奇古怪的错误。</p> 
 <p>有甚者喜欢把一个下载的spring3.1和struts1.3下载包里的的lib目录下所有的jar不管三七二十一全部copy到工程的WEB-INF\lib目录下，有提示需要覆盖他也就选个“ALL”。</p> 
 <p>我曾看到过一个工程，没几个JSP，CLASS，工程达287mb之大，其中286mb为jar文件，彼彼皆是log4j-1.3.8.jar,log4j-1.4.2.jar这样的重复的jar文件的存在。</p> 
 <p>有了junit3.8竟然还发觉工程里有junit4.x的包。。。晕啊。</p> 
 <h3>MAVEN模式：</h3> 
 <p>而使用maven构建工程时就不太会有这样的事情发生了，大家初学者可以把maven看成是一个“自动取包机”。怎么解释呢，举个例子来说：</p> 
 <p>你要装载a.jar，但a.jar依赖b.jar，而b.jar又依赖c.jar和d.jar，如果只是2，3级的依赖关系构建者可以记得住，但如果这个依赖关系达6级，7级以上时那么按照传统的构建模式当你发觉一个目录里有abcdefghijk这些个jar文件时，你一般为了避免出错总是一古脑的把所有的jar文件copy进工程，对吧？</p> 
 <p>但实际这些个jar文件里只有a.jar,b.jar, c.jar才是你需要的。</p> 
 <p>而maven干这个是它的特长，当你告诉maven你要下载a.jar文件时，它会发觉a.jar依赖于其它的jar文件，它就会把你指定的jar文件与相关的依赖文件全部拿下来，不会多拿（90%情况下）。</p> 
 <p>就好比你要拿org.springframework.web.struts-3.0.0.RELEASE.jar这个文件，但其实它还需要用到asm,collection, bean-util等，那么你只要告诉maven我要拿org.springframework.web.struts-3.0.0.RELEASE.jar这个文件，其它的依赖的相关的jar包会自动“下载”到你的工程中去.所以当我拿 maven重新去构建那个286mb的工程时，工程所有的功能一点不影响，靠着maven对jar包的自动依赖整个工程从286mb缩成了21mb，哈哈哈哈。。。。。。搞毛啊原来的那帮人在！</p> 
 <h1>三、使用Maven</h1> 
 <h2>3.1&nbsp;使用前的准备步骤</h2> 
 <p>准备步骤一：</p> 
 <p>请下载最新的maven2，如：apache-maven-3.0.3-bin.tar.gz（我现在使用的就是这个版本），我也放到我的CSDN的“资源”中去了.</p> 
 <p>准备步骤二：</p> 
 <p>确保你的eclipse为WTP版并且升级了mavenfor eclipse，即在你的eclipse-&gt;window-&gt;preference里有maven的选项：</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140601_9623.png" alt=""></p> 
 <p>最好的办法就是去eclipse的官网下载一个eclipsewtp版（<a href="http://download.eclipse.org/webtools/downloads/" target="_blank" class="external" rel="nofollow">http://download.eclipse.org/webtools/downloads/</a>），然后升级，可能升级过程会比较长，5-6小时也是有可能的（笔者家里的网速是20MB光纤）。但是第一次升完级后，以后每周没事就让你的eclipse去update一下也是有必要的，必经这东西是你以后吃饭的家伙，维持它永远是最新版本是个好习惯。</p> 
 <h2><a name="t5"></a><a name="_Toc338932655" target="_blank"></a>3.2&nbsp;理解maven</h2> 
 <p>请把以下几个概念记住就行了：</p> 
 <p>1）&nbsp;&nbsp;maven是通过布署在<em>internet上的maven仓库（开源免费）</em>去拿你需要的jar包，因此建议你因该是在一个宽带连网的环境下工作；</p> 
 <p>2）&nbsp;&nbsp;maven不是万能，有时会多拿包，当然这种情况很少但它会自动帮你把有依赖关系的包全拿到本地来的；</p> 
 <p>3）&nbsp;&nbsp;由于maven是通过internet去拿 你需要的jar包的，因此你的工程发布可以不用再把几十mb的jar文件连同你的工程一起发布了，而只需要把一份“jar包使用清单”伴随着你的工程一起发布就可以了。当别人拿着你的maven工程时，它只要也装有maven也能连入internet环境，它就可以在布署时自动把工程需要用到的jar包取下来然后布署入j2ee服务器的。</p> 
 <p>4）&nbsp;&nbsp;如果你所在的环境比如说一些公司不让员工上网或者让上网但不让下载的，那么这时你就需要建立自己的本地maven库，即maven私服. 把所有的jar包通过正式的手段搞到后在你所在的环境的局域网内建立一台maven服务器，把这些通过下载收集到的jar布署到你的“私服”上去，再把工程的maven连接仓库的url指向你本地的这台私服就行了。</p> 
 <h2><a name="t6"></a><a name="_Toc338932656" target="_blank"></a>3.3使用maven</h2> 
 <h3><a name="t7"></a><a name="_Toc338932657" target="_blank"></a>3.3.1设置环境变量</h3> 
 <p>把apache-maven-3.0.3-bin.tar.gz这个文件解压成maven放在c盘或者是d盘的根目录（你要放什么目录，随便，我喜欢用简单直观的名字放在磁盘根目录下）。</p> 
 <p>然后在系统环境变量中设一个M2_HOME，使其指向你的maven所在的目录：</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140651_8904.jpg" alt=""></p> 
 <h3><a name="t8"></a><a name="_Toc338932658" target="_blank"></a>3.3.2修改settings文件</h3> 
 <p>我们打开M2_HOME/conf目录下的settings.xml文件：</p> 
 <p>修改repositories这段，使其如下内容：</p> 
 <pre class="brush: java; gutter: true">&lt;repositories&gt;  

    &lt;repository&gt;    

        &lt;id&gt;Ibiblio&lt;/id&gt;    

        &lt;name&gt;Ibiblio&lt;/name&gt;    

        &lt;url&gt;http://www.ibiblio.org/maven/&lt;/url&gt;  

    &lt;/repository&gt;  

    &lt;repository&gt;    

        &lt;id&gt;PlanetMirror&lt;/id&gt;    

        &lt;name&gt;Planet Mirror&lt;/name&gt;    

        &lt;url&gt;http://public.planetmirror.com/pub/maven/&lt;/url&gt;  

    &lt;/repository&gt;

&lt;/repositories&gt;</pre> 
 <p>因为maven默认的internet仓库里的文件不全，所以笔者为大家提供两个目前大家用得最多的maven的internet仓库，由其是这个lbiblio的，基本所有的开源的框架的jar文件都有提供.</p> 
 <p>继续看下去，看到proxies这段，把它放开来，改成如下内容（如果你所在的环境是通过proxy上网的），要不然请保证这段proxies为被注释.</p> 
 <pre class="brush: java; gutter: true">  &lt;proxies&gt; 

    &lt;proxy&gt;

      &lt;id&gt;optional&lt;/id&gt;

      &lt;active&gt;true&lt;/active&gt;

      &lt;protocol&gt;http&lt;/protocol&gt;

      &lt;username&gt;username&lt;/username&gt;

      &lt;password&gt;password&lt;/password&gt;

      &lt;host&gt;proxy.mycompany.com&lt;/host&gt;

      &lt;port&gt;8088&lt;/port&gt;

      &lt;nonProxyHosts&gt;local.net|some.host.com&lt;/nonProxyHosts&gt;

    &lt;/proxy&gt;   

  &lt;/proxies&gt;</pre> 
 <p>如果你通过的proxy需要用户名密码的话不要忘了把用户名密码也设上，如果没有用户名密码，请保持&lt;username&gt;为username，&lt;password&gt;为password。</p> 
 <h3><a name="t9"></a><a name="_Toc338932659" target="_blank"></a>3.3.3&nbsp;使用maven构建工程</h3> 
 <p>现在可以打开eclipse了，打开eclipse-&gt;window-&gt;preference-&gt;maven</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140712_8739.jpg" alt=""></p> 
 <p>按照上图，点这个Browse…按钮，它会打开c:\users\YourCurrentUserName\.m2\repository\文件夹。</p> 
 <p>把你的M2_HOME\conf\目录下的settings.xml文件手动copy（是copy不是move）到</p> 
 <p>c:\users\YourCurrentUserName\.m2\repository\文件夹中去后，点ok返回该界面，再点“updatesettings”按钮即可。</p> 
 <p>在eclipse里新建工程时选”mavenproject”。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140763_3088.png" alt=""></p> 
 <p>[Next]</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140779_9473.png" alt=""></p> 
 <p>[Next]</p> 
 <p>这边要选maven-archetype-webapp这个类型，即建立一个标准的基于maven的web工程.<span style="font-weight: normal;">&nbsp;</span></p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140801_6562.png" alt=""></p> 
 <p>[Next]</p> 
 <ul> 
  <li>这边的Groud Id为你的包名我们这边需要填入：org.sky.ssh1.alpha</li> 
  <li>Artifact Id为工程名我们就填入alpha_mvn，代表这个工程是maven建的alpha工程</li> 
  <li>然后version保持不变</li> 
  <li>package填入org.sky.ssh1.alpha_mvn即可</li> 
 </ul> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140821_5978.png" alt=""></p> 
 <p>[Next]</p> 
 <p>Maven将会在eclipse里为你生成这样的一个工程。<span style="font-weight: normal;">&nbsp;</span></p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140888_2867.png" alt=""></p> 
 <p>请展开src目录，并确保你的工程的目录如下图所示</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140845_1021.jpg" alt=""><span style="font-weight: normal;">&nbsp;</span></p> 
 <p>你可能需要手动在src/main下自己建立一个java的目录，其它的maven在构建工程时应该已经为您建好了。</p> 
 <p>接下来我们就开始迁移原来的alpha工程进我们的maven构建的alpha_mvn工程中去了.</p> 
 <h3><a name="t10"></a><a name="_Toc338932660" target="_blank"></a>3.3.4&nbsp;将原有的alpha工程迁移至alpha_mvn工程</h3> 
 <h3><a name="t11"></a><a name="_Toc338932661" target="_blank"></a>迁移jar文件</h3> 
 <p>这边我们就不是手动一个个copyjar文件啦！</p> 
 <p>Maven是通过一个pom.xml文件来描述你的工程和工程中所用到的jar文件有哪些的.在这里我把alpha工程需要用到的pom.xml文件整个在这边提供给了大家，大家可以直接覆盖原有工程的pom.xml文件。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351140938_8548.jpg" alt=""></p> 
 <p>&nbsp;</p> 
 <h3><a name="t12"></a><a name="_Toc338932662" target="_blank"></a>pom.xml</h3> 
 <pre class="brush: java; gutter: true">&lt;project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd"&gt;

  &lt;modelVersion&gt;4.0.0&lt;/modelVersion&gt;

  &lt;groupId&gt;org.sky.ssh1.alpha&lt;/groupId&gt;

  &lt;artifactId&gt;Alpha_MVN&lt;/artifactId&gt;

  &lt;packaging&gt;war&lt;/packaging&gt;

  &lt;version&gt;0.0.1-SNAPSHOT&lt;/version&gt;

  &lt;name&gt;Alpha_MVN Maven Webapp&lt;/name&gt;

  &lt;url&gt;http://maven.apache.org&lt;/url&gt;

  &lt;dependencies&gt;

    &lt;dependency&gt;

      &lt;groupId&gt;junit&lt;/groupId&gt;

      &lt;artifactId&gt;junit&lt;/artifactId&gt;

      &lt;version&gt;3.8.1&lt;/version&gt;

      &lt;scope&gt;test&lt;/scope&gt;

    &lt;/dependency&gt;

    &lt;dependency&gt;

      &lt;groupId&gt;log4j&lt;/groupId&gt;

      &lt;artifactId&gt;log4j&lt;/artifactId&gt;

      &lt;version&gt;1.2.8&lt;/version&gt;

    &lt;/dependency&gt;

    &lt;dependency&gt;

      &lt;groupId&gt;c3p0&lt;/groupId&gt;

      &lt;artifactId&gt;c3p0&lt;/artifactId&gt;

      &lt;version&gt;0.9.1.2&lt;/version&gt;

    &lt;/dependency&gt;

    &lt;dependency&gt;

      &lt;groupId&gt;jaxen&lt;/groupId&gt;

      &lt;artifactId&gt;jaxen&lt;/artifactId&gt;

      &lt;version&gt;1.1.1&lt;/version&gt;

      &lt;exclusions&gt;

                              &lt;exclusion&gt;

                                                      &lt;artifactId&gt;xercesImpl&lt;/artifactId&gt;

                                                      &lt;groupId&gt;xerces&lt;/groupId&gt;

                              &lt;/exclusion&gt;

      &lt;/exclusions&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-core&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-el&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-extras&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-faces&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-mailreader-dao&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-scripting&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-taglib&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.struts&lt;/groupId&gt;

      &lt;artifactId&gt;struts-tiles&lt;/artifactId&gt;

      &lt;version&gt;1.3.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- springframework 3.1 --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-struts&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-core&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-context&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-context-support&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-beans&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-orm&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-jdbc&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-tx&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-aop&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-aspects&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-webmvc-portlet&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-jms&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-asm&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springframework&lt;/groupId&gt;

      &lt;artifactId&gt;spring-test&lt;/artifactId&gt;

      &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.springmodules&lt;/groupId&gt;

      &lt;artifactId&gt;spring-modules-jakarta-commons&lt;/artifactId&gt;

      &lt;version&gt;0.8a&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- aspectj --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.aspectj&lt;/groupId&gt;

      &lt;artifactId&gt;aspectjrt&lt;/artifactId&gt;

      &lt;version&gt;1.6.12&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.aspectj&lt;/groupId&gt;

      &lt;artifactId&gt;aspectjweaver&lt;/artifactId&gt;

      &lt;version&gt;1.6.12&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- hibernate 3.3.1 --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-core&lt;/artifactId&gt;

      &lt;version&gt;3.3.1.GA&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-c3p0&lt;/artifactId&gt;

      &lt;version&gt;3.3.1.GA&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-ehcache&lt;/artifactId&gt;

      &lt;version&gt;3.3.1.GA&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-entitymanager&lt;/artifactId&gt;

      &lt;version&gt;3.3.1.ga&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-commons-annotations&lt;/artifactId&gt;

      &lt;version&gt;3.3.0.ga&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.hibernate&lt;/groupId&gt;

      &lt;artifactId&gt;hibernate-annotations&lt;/artifactId&gt;

      &lt;version&gt;3.3.1.GA&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- log4j 1.2.14 --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;log4j&lt;/groupId&gt;

      &lt;artifactId&gt;log4j&lt;/artifactId&gt;

      &lt;version&gt;1.2.16&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.slf4j&lt;/groupId&gt;

      &lt;artifactId&gt;slf4j-log4j12&lt;/artifactId&gt;

      &lt;version&gt;1.6.4&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- commons utils --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-beanutils&lt;/groupId&gt;

      &lt;artifactId&gt;commons-beanutils&lt;/artifactId&gt;

      &lt;version&gt;1.8.3&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-chain&lt;/groupId&gt;

      &lt;artifactId&gt;commons-chain&lt;/artifactId&gt;

      &lt;version&gt;1.2&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-codec&lt;/groupId&gt;

      &lt;artifactId&gt;commons-codec&lt;/artifactId&gt;

      &lt;version&gt;1.6&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-collections&lt;/groupId&gt;

      &lt;artifactId&gt;commons-collections&lt;/artifactId&gt;

      &lt;version&gt;3.2.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-configuration&lt;/groupId&gt;

      &lt;artifactId&gt;commons-configuration&lt;/artifactId&gt;

      &lt;version&gt;1.7&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-digester&lt;/groupId&gt;

      &lt;artifactId&gt;commons-digester&lt;/artifactId&gt;

      &lt;version&gt;2.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-fileupload&lt;/groupId&gt;

      &lt;artifactId&gt;commons-fileupload&lt;/artifactId&gt;

      &lt;version&gt;1.2.2&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-io&lt;/groupId&gt;

      &lt;artifactId&gt;commons-io&lt;/artifactId&gt;

      &lt;version&gt;2.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-lang&lt;/groupId&gt;

      &lt;artifactId&gt;commons-lang&lt;/artifactId&gt;

      &lt;version&gt;2.6&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-logging&lt;/groupId&gt;

      &lt;artifactId&gt;commons-logging&lt;/artifactId&gt;

      &lt;version&gt;1.1.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-net&lt;/groupId&gt;

      &lt;artifactId&gt;commons-net&lt;/artifactId&gt;

      &lt;version&gt;3.0.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-pool&lt;/groupId&gt;

      &lt;artifactId&gt;commons-pool&lt;/artifactId&gt;

      &lt;version&gt;1.6&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;commons-validator&lt;/groupId&gt;

      &lt;artifactId&gt;commons-validator&lt;/artifactId&gt;

      &lt;version&gt;1.3.1&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;

      &lt;artifactId&gt;commons-compress&lt;/artifactId&gt;

      &lt;version&gt;1.3&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- jsp servlet api --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;javax.servlet&lt;/groupId&gt;

      &lt;artifactId&gt;servlet-api&lt;/artifactId&gt;

      &lt;version&gt;2.4&lt;/version&gt;

      &lt;scope&gt;compile&lt;/scope&gt;

&lt;/dependency&gt;

&lt;!-- mail --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.apache.velocity&lt;/groupId&gt;

      &lt;artifactId&gt;velocity&lt;/artifactId&gt;

      &lt;version&gt;1.7&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;javax.mail&lt;/groupId&gt;

      &lt;artifactId&gt;mail&lt;/artifactId&gt;

      &lt;version&gt;1.4.4&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- jasypt --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.jasypt&lt;/groupId&gt;

      &lt;artifactId&gt;jasypt&lt;/artifactId&gt;

      &lt;version&gt;1.9.0&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.jasypt&lt;/groupId&gt;

      &lt;artifactId&gt;jasypt-spring3&lt;/artifactId&gt;

      &lt;version&gt;1.9.0&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.jasypt&lt;/groupId&gt;

      &lt;artifactId&gt;jasypt-springsecurity3&lt;/artifactId&gt;

      &lt;version&gt;1.9.0&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- ehCache --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;net.sf.ehcache&lt;/groupId&gt;

      &lt;artifactId&gt;ehcache&lt;/artifactId&gt;

      &lt;version&gt;1.6.2&lt;/version&gt;

&lt;/dependency&gt;

&lt;!-- test --&gt;

&lt;dependency&gt;

      &lt;groupId&gt;junit&lt;/groupId&gt;

      &lt;artifactId&gt;junit&lt;/artifactId&gt;

      &lt;version&gt;4.10&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;org.dbunit&lt;/groupId&gt;

      &lt;artifactId&gt;dbunit&lt;/artifactId&gt;

      &lt;version&gt;2.4.8&lt;/version&gt;

&lt;/dependency&gt;

&lt;dependency&gt;

      &lt;groupId&gt;mockit&lt;/groupId&gt;

      &lt;artifactId&gt;jmockit&lt;/artifactId&gt;

      &lt;version&gt;0.999.4&lt;/version&gt;

&lt;/dependency&gt;

  &lt;/dependencies&gt;

  &lt;build&gt;

    &lt;finalName&gt;Alpha_MVN&lt;/finalName&gt;

  &lt;/build&gt;

&lt;/project&gt;</pre> 
 <p>大家可以看到我需要什么包，只要在这个pom.xml文件中加入这样的一段东西</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;

    &lt;groupId&gt;c3p0&lt;/groupId&gt;

    &lt;artifactId&gt;c3p0&lt;/artifactId&gt;

    &lt;version&gt;0.9.1.2&lt;/version&gt;

&lt;/dependency&gt;</pre> 
 <p>那么有人要问我就算知道我要下一个c3p0.jar但这个artifactid与version我怎么填？</p> 
 <p>简单，使用这个网址：<a href="http://www.ibiblio.org/" target="_blank" class="external" rel="nofollow">http://www.ibiblio.org/</a></p> 
 <p>在里面搜你要的jar，搜完后它会出一个列表，然后找到你需要的版本，点进去后看看有没有后缀名为.pom的文件，如果有，直接在IE中打开该文件或者下载下来后使用纯文本编辑器打开,里面就有你要的artifactid与version的正确描述了，然后填入工程的pom.xml文件不就行了.</p> 
 <p>当你的pom.xml文件没有红色的叉叉（我叉叉PLMM的圈圈，嘿）时，eclipse就开始连上在M2_HOME/conf/setting.xml文件中描述的相关的maven的repository去拿 jar了，拿下来的jar文件：</p> 
 <p>1）&nbsp;&nbsp;存放在本地的C:\Users\YourCurrentUserName\.m2\repository目录下；</p> 
 <p>2）&nbsp;&nbsp;在工程中生成一个classpath</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141007_2043.jpg" alt=""></p> 
 <p>&nbsp;</p> 
 <h3><a name="t13"></a><a name="_Toc338932663" target="_blank"></a>迁移原有alpha工程的resource</h3> 
 <p>整个copy到alpha_mvn的src/main/resources目录下即可.</p> 
 <h3><a name="t14"></a><a name="_Toc338932664" target="_blank"></a>迁移原有alpha工程的src</h3> 
 <p>整个copy到alpha_mvn的src/main/java目录下，并把src/main/java设成src目录</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141045_8874.jpg" alt=""></p> 
 <p>&nbsp;</p> 
 <h3><a name="t15"></a><a name="_Toc338932665" target="_blank"></a>迁移原有alpha工程的web文件</h3> 
 <p>直接把原有alpha工程下的WebContent目录下所有的内容（除去WEB-INF\lib）copy到alpha_mvn的src/main/webapp目录下即可</p> 
 <h3><a name="t16"></a><a name="_Toc338932666" target="_blank"></a>3.3.5&nbsp;使用maven打包</h3> 
 <p>右键单击pom.xml文件选择MavenPOM Editor打开</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141070_6377.jpg" alt=""></p> 
 <p>在MavenPOM Editor的视图中切换到pom.xml这个tab，然后它会打开这个pom.xml文件的真实内容，右键选RunAs-&gt;Maven install</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141110_2073.jpg" alt=""></p> 
 <p>等一会，现在maven开始compile和create标准的J2EE的war工程了.</p> 
 <p>当我们看到如下输出：</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141139_5946.jpg" alt=""></p> 
 <p>代表工程打包完毕了，如果是fail或者是其它错误，可以先RunAs-&gt;Maven clean一下，再调整一下你的pom.xml或者是settings.xml文件，然后再Maveninstall。</p> 
 <p>Maven会在你的工程所在的目录生成一个target目录，以下是该目录内容：</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141165_2033.jpg" alt=""></p> 
 <p><span style="font-weight: normal;">这个Alpha_MVN.war就是我们的工程布署包，那么有些人不喜欢.war包，喜欢用打碎掉的war目录结构，那么注意Alpha_MVN这个目录,就是一个标准的war格式的目录，这两个东西都可以直接扔到tomcat的webapp目录下进行布署，布署完后</span></p> 
 <p>此时我们启动Tomcat。。。！</p> 
 <p>然后我们等着激动人心的到来。。。！</p> 
 <p>此时我们看到tomcat停了一会，然后过了半天抛了一个java.net.ConnectException Service Unavilable的错误。。。oh…shit!!!!</p> 
 <p>为啥啦。。。别急 ，别急.</p> 
 <p>解决包冲突，前面说了，Maven在绝大多数情况下会保证你的依赖关系没有问题，都会帮你下到本地的，但是这次我们就碰到了一个问题。</p> 
 <p>用MavenPOM Editor打开我们的pom.xml文件，切换到：DependencyHierarchy视图</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141201_5858.jpg" alt=""></p> 
 <p>我们可以看到为了满足spring-struts这个插件的应用（Spring+ Struts时用的），Maven多给我们拿 一个struts包，因此导致我们的工程中有两个struts核心包，一个是1.2.9,一个是1.3.10，由于struts1.3与struts1.2命名空间的不同，因此才产生了刚才那个狗P错误。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141220_5376.jpg" alt=""></p> 
 <p>右键单击struts:1.2.9[compile]，选ExcludeMaven Artifact后确定并保存pom.xml文件，这步操作相当于我们在原有的pom.xml文件中增加了这样的一段描述：</p> 
 <pre class="brush: java; gutter: true">&lt;dependency&gt;

                                                &lt;groupId&gt;org.springframework&lt;/groupId&gt;

                                                &lt;artifactId&gt;spring-struts&lt;/artifactId&gt;

                                           &lt;version&gt;3.1.0.RELEASE&lt;/version&gt;

                                                &lt;exclusions&gt;

                                                                &lt;exclusion&gt;

                                                                                &lt;artifactId&gt;struts&lt;/artifactId&gt;

                                                                                &lt;groupId&gt;struts&lt;/groupId&gt;

                                                                &lt;/exclusion&gt;

                                                &lt;/exclusions&gt;

&lt;/dependency&gt;</pre> 
 <p>重新Mavenclean一下并Maveninstall一个新的.war文件再布署吧。</p> 
 <p>Tomcat启动正常</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141244_6647.jpg" alt=""></p> 
 <p>功能测试一切正常(<a href="http://localhost:8080/Alpha_MVN/index.do" target="_blank" class="external" rel="nofollow">http://localhost:8080/Alpha_MVN/index.do</a>)。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141299_6489.jpg" alt=""></p> 
 <h1><a name="t17"></a><a name="_Toc338932667" target="_blank"></a>四、如何让Maven构建的工程在eclipse里跑起来</h1> 
 <p>我们刚才利用Maven构建了一个web工程，这个 .war文件才26mb，我们原来的alpha工程加lib库要36-37mb，是不是一下缩水了10多mb啊？因为maven帮我们控制好了所必需的jar，不需要的它不会下载。</p> 
 <p align="left">但是，这个工程无法在eclipse所内嵌的tomcat里运行起来，这不便于我们的调试。一般我们开发人员都喜欢直接在eclipse里点一下server的运行，然后在eclipse里启动起我们的web工程，这样出了错也便于我们调试，而不用每次改一个jsp或者一个java就重新构建，对吧。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141324_6449.jpg" alt=""></p> 
 <h2><a name="t18"></a><a name="_Toc338932668" target="_blank"></a>4.1&nbsp;在eclipse里新增一个jdk的runtime</h2> 
 <div>
  <img src="http://img.my.csdn.net/uploads/201210/25/1351141378_9031.jpg" alt="">
 </div> 
 <p>我们在这边设置的jdk把它的JREhome给定位到了JAVA_HOME\jre目录下了，而不像上面的JDK1.6我们的JREhome是定位到JAVA_HOME这一层目录的，为什么？因为在使用jasypt时，eclipse wtp有一个bug就是在eclipse内运行tomcat时，有时会认不出PBEWITHMD5ANDDES这个加密算法，它会抛一个secret key notavailable的Exception，而你如果把你工程的JDK编译环境定位到了JAVA_HOME\jre目录下，它就能认得出，这是一个BUG，在eclipse的官方论坛中已经有提。</p> 
 <h2><a name="t19"></a><a name="_Toc338932669" target="_blank"></a>4.2&nbsp;在eclipse里设置tomcat</h2> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141350_9488.jpg" alt=""></p> 
 <p align="left">设完后你的工程会在server这个tab下多出一个Tomcat的项。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141433_8722.jpg" alt=""></p> 
 <h2><a name="t20"></a><a name="_Toc338932670" target="_blank"></a>4.3&nbsp;将alpha_mvn转变成可在eclipse里运行的工程</h2> 
 <p align="left">右键你的工程，选project fact，你会看到一个convert的超链接,点这个超链接。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141474_1825.png" alt=""></p> 
 <p align="left">勾选Dynamic Web Module与Java两项</p> 
 <p align="left">打开工程所在的eclipse workspace目录下的.settings目录如：C:\eclipsespace\alpha_mvn\.settings，看到一个叫“org.eclipse.wst.common.project.facet.core.xml”的文件如：</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141497_9312.jpg" alt=""></p> 
 <p align="left">用纯文本编辑工具打开它，把：</p> 
 <p align="left">installedfacet=”jst.web” version从3.0或者其它版本，改成&nbsp;2.5如下：</p> 
 <pre class="brush: java; gutter: true">&lt;installed facet="jst.web" version="2.5"/&gt;</pre> 
 <p align="left"><img style="font-weight: normal;" src="http://img.my.csdn.net/uploads/201210/25/1351141520_2155.jpg" alt=""><br> 回到eclipse里刷新工程，这时你的工程应该会多出一个目录，WebContent，如下结构：</p> 
 <p align="left">等等，等等。。。先别急着给我拷东西。</p> 
 <p align="left">右键单击工程，选properties，在Java Build Path里选Order and Export这个Tab。</p> 
 <p align="left">然后把Maven Dependencies给勾选上，按确定。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141551_1231.jpg" alt=""></p> 
 <p align="left">再右键单击工程选</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141579_9988.jpg" alt=""></p> 
 <p align="left">看着这个mapping关系，自己用Add按钮照图来增加吧</p> 
 <p align="left">记住：</p> 
 <p align="left">在AddMaven Dependencies时要选JavaBuild Path Entries。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141606_8067.jpg" alt=""></p> 
 <p align="left">其它的都用Folder来add，完全按照这个mapping 关系来做。</p> 
 <p align="left">做完后，右键单击server，选”Add andRemove…”</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141634_9306.jpg" alt=""></p> 
 <p align="left">在弹出对话框中把alpha_mvn通过Add&gt;按钮增加到右边的Configured框中，Finish后</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141666_4877.jpg" alt=""></p> 
 <p align="left">你会发觉原来的servers下的Tomcat已经挂载了我们的工程，点右上角绿色的启动按钮，切换到Console窗口看输出。</p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141687_4490.jpg" alt=""></p> 
 <p align="left">输出无误<span style="font-weight: normal;">&nbsp;</span></p> 
 <p><img src="http://img.my.csdn.net/uploads/201210/25/1351141712_8166.jpg" alt=""></p> 
 <p align="left">访问&nbsp;<a href="http://localhost:8080/alpha_mvn/index.do" target="_blank" class="external" rel="nofollow">http://localhost:8080/alpha_mvn/index.do</a>测试功能一切正常，结束教程.</p> 
 <p align="left">今后我们的ssh, ssh2, ssi等工程将基于这个maven来构建，我就不需要为大家再提供jar了，而只是一个pom.xml文件了，真方便（感情着我就是为了图自己发布工程方便啊，呵呵）</p> 
 <h1><a name="t21"></a><a name="_Toc338932671" target="_blank"></a>附录、OracleSun JDK安全限制</h1> 
 <p align="left">经常大家会碰到在使用一些JAVA安全时系统抛出一些notsupport the algorithm，secretkey not available的问题，或者说是DES只支持到512位，不支持1024位的问题。</p> 
 <p align="left">其根本原因在于大家在网上下载的jdk都是“安全受限”的，这是美国数字产品出口限制的一个政策。</p> 
 <p align="left">因此为了使用更多的算法更长位数的算法，大家需要去Oracle网站下载一个叫jce_policy-6.zip的文件，这就像一个补丁一样，需要把这个补丁打入原有的已安装jdk里。当然，对应于jdk1.5,jdk1.4也有相应的jce_policy-4,jce_policy-5的相关文件。</p> 
 <p align="left">下载后解压后到一个目录中，把这个目录中所有的东西放到你的：</p> 
 <p align="left">JAVA_HOME/jre/lib/security目录下，并选择覆盖，重启eclipse或者重新开一个command窗口就可以使用如：PBEWITHMD5ANDDES、Blowfix、rsa1024位以上的一些高层算法了。</p> 
 <p align="left">下面是jce_policy-6.zip里的readme的相关安装说明，供参考:</p> 
 <pre class="brush: java; gutter: true">Installation

----------------------------------------------------------------------

Notes:

  o Unix (Solaris/Linux) and Win32 use different pathname separators, so

    please use the appropriate one ("\", "/") for your

    environment.

  o &lt;java-home&gt; refers to the directory where the Java SE Runtime

    Environment (JRE) was installed.  It is determined based on whether

    you are running JCE on a JRE with or without the JDK installed. The

    JDK contains the JRE, but at a different level in the file

    hierarchy. For example, if the JDK is installed in

    /home/user1/jdk1.6.0 on Unix or in C:\jdk1.6.0 on Win32, then

    &lt;java-home&gt; is

        /home/user1/jdk1.6.0/jre            [Unix]

        C:\jdk1.6.0\jre                    [Win32]

    If on the other hand the JRE is installed in /home/user1/jre1.6.0

    on Unix or in C:\jre1.6.0 on Win32, and the JDK is not

    installed, then &lt;java-home&gt; is

        /home/user1/jre1.6.0                [Unix]

        C:\jre1.6.0                        [Win32]

  o On Win32, for each JDK installation, there may be an additional

    JRE installed under the "Program Files" directory. Please make

    sure that you install the unlimited strength policy JAR files

    for all JREs that you plan to use.

Here are the installation instruction:

1)  Download the unlimited strength JCE policy files.

2)  Uncompress and extract the downloaded file.

    This will create a subdirectory called jce.

    This directory contains the following files:

        README.txt                   This file

        COPYRIGHT.html               Copyright information

        local_policy.jar             Unlimited strength local policy file

        US_export_policy.jar         Unlimited strength US export policy file

3)  Install the unlimited strength policy JAR files.

    To utilize the encryption/decryption functionalities of

    the JCE framework without any limitation, first make a copy of

    the original JCE policy files (US_export_policy.jar and

    local_policy.jar in the standard place for JCE

    jurisdiction policy JAR files) in case you later decide

    to revert to these "strong" versions. Then replace the strong

    policy files with the unlimited strength versions extracted in the

    previous step.

    The standard place for JCE jurisdiction policy JAR files is:

        &lt;java-home&gt;/lib/security            [Unix]

        &lt;java-home&gt;\lib\security           [Win32]</pre> 
 <!-- BEGIN #author-bio --> 
 <!-- END #author-bio --> 
</div>