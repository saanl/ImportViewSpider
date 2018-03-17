package com.util;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dao.CommonDao;
import com.pojo.Article;

public class MyUtils {
	
	public static void get(String url){
		try {
			Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
			parse(doc);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void parse(Document doc){
		Elements lists_a = doc.select("#archive div.floated-thumb > div.post-meta > p > a");
		Elements lists_desc = doc.select("#archive div.floated-thumb > div.post-meta > span.excerpt > p");
		
		int j=0;//对于‘描述‘和’题目‘等a标签不对应，60:20所以加上额外计数器
		System.out.println(lists_a.size()+"|"+lists_desc.size());
		
		if(lists_a.size()!=60){
			
			for(int i=0;i<lists_a.size();i++){
				Article a = new Article();
				Element element = lists_a.get(i); 
				String title = element.attr("title");
				String url = element.attr("href");
				a.setTitle(title);
				a.setUrl_num(url);
				
				saveToDataBase(a);
				element = null;
				a=null;
				title=null;
				url=null;
			}
			
			
		}else{
		for(int i=0;i<lists_a.size();i++){
			
			if(i%3==0){ 
						//System.out.println(i);
						Article a = new Article();
						
						//描述，长度不能大于255
						Element desc = null;
						String descs =null;
						if(j<20){
							desc = lists_desc.get(j);
							descs = desc.text().length()>254?desc.text().substring(0, 254):desc.text();
						}
						j++;
						
						//题目和url
						Element element = lists_a.get(i); 
						String title = element.attr("title");
						String url = element.attr("href");
						
						//标签
					
						Element  tag = lists_a.get(i+1);
						String  tags = tag.text();
					
							
						a.setTitle(title);
						a.setUrl_num(url);
						a.setDescs(descs);
						a.setTag(tags);
						
						saveToDataBase(a);
						//System.out.println(a);																
						tag =null;
						desc = null;
						element = null;
						a=null;
						title=null;
						tags=null;
						url=null;
						descs=null;
				}
			}  
		}
				lists_a =null;
				lists_desc=null;
	}
			
	public static void getText(String url){
		try {
			Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
			String num = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
			System.out.println(num);
			parseText(doc,num);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//post-28306
	private static void parseText(Document doc,String num) {
		Element first = doc.select("#post-"+num+" > div.entry").first();
		System.out.println(first.toString());
	}
	
	public static void saveToDisk(String filename){
		File file = new File("D://importview/"+filename);
		if(!file.exists()){
			
		}
	}
	
	public static void saveToDataBase(Article a){
		CommonDao.insert(a, "views");
	}
	
	
	
}
