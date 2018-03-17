package com.main;
import com.util.MyUtils;

//直接爬取所有的信息
public class Spider {
	
	public static void main(String[] args) {
		 for(int i=1;i<131;i++){
			 MyUtils.get("http://www.importnew.com/all-posts/page/"+i);
			 System.out.println("到"+i);
		 }
		
	}
	
	
}
