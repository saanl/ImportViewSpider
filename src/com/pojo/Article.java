package com.pojo;

public class Article {
	
	
	
	@Override
	public String toString() {
		return "Article [id=" + id + ", title=" + title + ", descs=" + descs + ", url_num=" + url_num + ", tag=" + tag
				+ "]";
	}
	private Integer id;
	private String title;
	private String descs;
	private String url_num;
	private String tag;
	
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDescs() {
		return descs;
	}
	public void setDescs(String descs) {
		this.descs = descs;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl_num() {
		return url_num;
	}
	public void setUrl_num(String url_num) {
		this.url_num = url_num;
	}
	
	
}
