package com.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import com.mysql.jdbc.Connection;
import com.pojo.Article;

public class CommonDao {
	//请输入你的数据库名称
	private static Connection conn = 
			MysqlConnection.getConnection("importview");
	
	public static Map<String,String> getFields(Object obj){
		Map<String,String>  map= new LinkedHashMap<>();
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for(Field f:declaredFields){
			map.put(f.getName(),f.getType().getName());
		}
		//System.out.println(map);
		return map;
	}
	
	
	
	public static void insert(Object obj,String tableName) {
		String sql = "";
		Map<String,String>  map = getFields(obj);
		String tablename = tableName;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into "+tablename+" (");
		int count = map.size();
		int i=0;
		for(String s:map.keySet()) {
			i++;
			sb.append(s);
			if(i==count) {
				sb.append(" ");
			}else {
				sb.append(", ");
			}
		}
		sb.append(") values(");
		int j=0;
		for(String s:map.keySet()) {
			j++;
			try {
				
				if(map.get(s).equalsIgnoreCase("java.lang.Integer")||map.get(s).equalsIgnoreCase("java.lang.int")){
					sb.append(" "+ BeanUtils.getProperty(obj, s)+" ");
				}else{
					sb.append(" '"+ BeanUtils.getProperty(obj, s)+"' ");
				}
				
				
				if(j==count) {
					sb.append("");
				}else {
					sb.append(", ");
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		sb.append(")");
		sql=sb.toString();
		System.out.println(sql);
		
			try {
				Statement st = conn.createStatement();
				int p = st.executeUpdate(sql);
				if(p>0) {
						System.out.println("succeess");
				}
				
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	
	
	
	public static List<Article> queryByLimit(int start,int end){
		String sql = "select id,title,url_num from views limit "+start+","+end;
		List<Article> list = new ArrayList<>();
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet re = st.executeQuery(sql);
			while(re.next()) {			
				Article p =new Article();
				p.setId(re.getInt("id"));
				p.setTitle(re.getString("title"));
				p.setUrl_num(re.getString("url_num"));
				list.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
			return list;
	}
	
	public static void main(String[] args) {
		
		System.out.println(queryByLimit(0,8));

	}
}
