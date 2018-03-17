package com.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.dao.CommonDao;
import com.pojo.Article;

public class DownLoadArticles extends Thread{
	
	private String url;
	private String filename;
	
	public DownLoadArticles(String url, String filename) {
		super();
		this.url = url;
		this.filename = filename;
	}

	private void get(String url,String filename){
		try {
			Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
			String num = url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
			parse(doc,num,filename);
			doc =null;
			num =null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parse(Document doc,String num,String filename) {
		
		Elements article = doc.select("#post-"+num+" div.entry");
		String s = article.toString();
		
		File file = new File("D://importview/"+filename+".md");
		
		if(file!=null&&!file.exists()){
			FileWriter fr = null;
			try {
				 fr = new FileWriter(file);
				 fr.write(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(fr!=null){fr.close();}	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}	
	}
	
	
	
	@Override
	public void run() {
		get(url,filename);
		super.run();
	}

	public static void main(String[] args) {
		//采用阻塞队列，一边从数据库获取，一边IO操作
		ArrayBlockingQueue<Article> blist = new ArrayBlockingQueue<>(100,true);
		
		
		for(int i=0;i<8;i++){
					List<Article> list = CommonDao.queryByLimit(0+i*20, 20+i*20);
					for(Article s:list){
						//过滤
						if(s.getTitle().contains("Spring")||s.getTitle().contains("spring")){
							if(blist.size()<100){
								blist.add(s);
							}
						}
					}
				}

		
		
		while(!blist.isEmpty()){
			System.out.println("working----");
			Article remove = blist.remove();
			new DownLoadArticles(remove.getUrl_num(),remove.getTitle()).start();
		}
		
		System.out.println("保存至D://importview下");
	}
	
}
