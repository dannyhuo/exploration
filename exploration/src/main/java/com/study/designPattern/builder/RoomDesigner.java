package com.study.designPattern.builder;

public class RoomDesigner {
	
	public void command(Builder builder){
		
		//建地基
		builder.makeFloor();
		
		//建造门
		builder.makeDoor();
		
		//建造窗
		builder.makeWindow();
	}

}
