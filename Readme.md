# 使用Jsoup爬取ImportView的文章
### 写在前面
	1. 本次爬取的是importview网站上的文章，因为感觉写得很好！
	2. 本次采用jsoup的开源框架官方教程：[link](http://www.open-open.com/jsoup).
	3. 此方式和之前的不同，前面头条是通过api接口获取json，jsoup则是采用dom解析方式。
	4. 而且jsoup选择器使用方法都和jQuery类似。
### 实现思路
	1. 获取importview的文章链接[link]http://www.importnew.com/all-posts/page/x.
	2. 上面的x就是分页码，一共有131页，4000多篇文章。
	3. 首先获取列表下的文章的题目和url储存到数据库。
	4. 然后过滤自己想要的文章类型和方向下载。
	5. 使用jsoup获取url下的具体文章，使用jsoup选择器把有用内容选择到。
	6. 在把获取到的文章内容写到本地磁盘以md格式，这样就不用提取内容中的文字，md格式直接打开效果比HTML好。

### 使用
	main包下面就是。