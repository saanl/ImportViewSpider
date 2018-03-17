package com.test;

import java.util.concurrent.ArrayBlockingQueue;

public class Test {
	
	private static ArrayBlockingQueue<String> list = new ArrayBlockingQueue<>(50,true);
	
	public static void main(String[] args) throws InterruptedException {
		t1 t1 = new t1(list);
		
		t1.start();
		
		//Thread.sleep(50);
		
		t2 t2 = new t2(list);
		
		t2.start();
		long end = System.currentTimeMillis();
		
		
	}
	
	
	
	
}
