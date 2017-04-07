package com.study.javabase;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		Object obj = new Object();
		synchronized (obj) {
			obj.notify();
		}
	}

}
