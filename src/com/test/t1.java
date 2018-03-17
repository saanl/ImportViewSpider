package com.test;

import java.util.concurrent.ArrayBlockingQueue;

class t1 extends Thread{
	private ArrayBlockingQueue<String> list;
	public t1(ArrayBlockingQueue<String> list){
		this.list = list;
	}
	@Override
	public void run() {
		super.run();
		for(int i=0;i<100;i++){
			list.add("index"+i+"");
			System.out.println("生产者"+list.size());
		}
		
	}
	
}
