package com.study.javabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程同步锁问题的研究
 * {ReentrantLock} & synchrolized
 * @author huoqiang
 *
 */
public class ThreadSync {
	
	
	public static void main(String[] args) {
		List<Object> continar = new ArrayList<>();
		
		int threadCount = 10;
		Thread[] threads1 = new Thread[threadCount];
		Long time = System.nanoTime();
		for(int i = 0; i < threadCount; i++){
			threads1[i] = new Thread(new GeneratorLock(continar));
			threads1[i].start();
		}
		
		Thread[] threads2 = new Thread[threadCount];
		for(int i = 0; i < threadCount; i++){
			threads2[i] = new Thread(new GeneratorSynchrolized(continar));
			threads2[i].start();
		}
		
	}

}


class GeneratorLock implements Runnable{
	
	private List<Object> list = null;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Random random = new Random();
		while(list.size() < 1000000){
			ReentrantLock lock = new ReentrantLock();
			lock.lock();
			list.add(random.nextInt());
			lock.unlock();
		}
	}
	
	public GeneratorLock(List<Object> continar){
		this.list = continar;
	}
	
}

class GeneratorSynchrolized implements Runnable{
	
	private List<Object> list = null;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Random random = new Random();
		while(list.size() < 10000000){
			synchronized (list) {
				list.add(random.nextInt());
			}
		}
	}
	
	public GeneratorSynchrolized(List<Object> continar){
		this.list = continar;
	}
	
}