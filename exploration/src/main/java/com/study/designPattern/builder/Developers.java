package com.study.designPattern.builder;

import com.study.designPattern.builder.model.Room;

/**
 * 开发商
 * @author huoqiang
 *
 */
public class Developers {

	public static void main(String[] args) {
		
		//建造者
		Builder builder = new RoomBuilder();
		
		//设计师
		RoomDesigner designer = new RoomDesigner();
		
		//按设计师的要求建造
		designer.command(builder);
		
		Room myRoom = builder.getRoom();
		
		System.out.println(myRoom);
	}
}
