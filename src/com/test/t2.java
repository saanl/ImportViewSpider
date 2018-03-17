package com.test;

import java.util.concurrent.ArrayBlockingQueue;

class t2 extends Thread{
	private ArrayBlockingQueue<String> list;
	public t2(ArrayBlockingQueue<String> list){
		this.list = list;
	}
	@Override
	public void run() {
		super.run();
		while(!list.isEmpty()){
			list.remove();
			System.out.println("消费者"+list.size());
		}
		
	}
	
}