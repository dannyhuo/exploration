package com.study.javabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectExploration implements Cloneable, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8679118852290685936L;
	
	private int count = 100;
	private Prop objectExploration = new Prop();
	
	
	/**
	 * 深层复制
	 * Object中默认是浅层复制
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object deepClone() throws IOException, ClassNotFoundException{
		//1、将对象写到Object流中
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this);
		
		//2、从Object流中读取对象
		ByteArrayInputStream bai = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bai);
		return ois.readObject();
	}
	
	/**
	 * Object的clone
	 * 浅层复制
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public Object objectClone() throws CloneNotSupportedException{
		return super.clone();
	}
	
	public boolean equals(ObjectExploration obj) {
		return this.count == obj.count && this.getObjectExploration() == obj.getObjectExploration();
	}
	
	/**
	 * 测试对象深层clone与浅层clone的区别
	 */
	public static void testcloneAndDeepClone(){
		ObjectExploration oe = new ObjectExploration();
		try {
			ObjectExploration oe2 = (ObjectExploration) oe.deepClone();
			ObjectExploration oe3 = (ObjectExploration) oe.clone();
			
			
			System.out.println("源对象与deepClone对象相等？" + oe.equals(oe2));
			System.out.println("源对象与objectClone对象相等？" + oe.equals(oe3));
			System.out.println(oe == oe2);
			System.out.println(oe == oe3);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		testcloneAndDeepClone();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Prop getObjectExploration() {
		return objectExploration;
	}

	public void setObjectExploration(Prop objectExploration) {
		this.objectExploration = objectExploration;
	}

	
}

class Prop implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2002685556458142059L;
	private Long abc;

	public Long getAbc() {
		return abc;
	}

	public void setAbc(Long abc) {
		this.abc = abc;
	}
	
	
}
