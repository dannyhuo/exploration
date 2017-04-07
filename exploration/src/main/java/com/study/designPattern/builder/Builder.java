package com.study.designPattern.builder;

import com.study.designPattern.builder.model.Room;

public interface Builder {
	
	void makeWindow();
	
	void makeDoor();
	
	void makeFloor();
	
	Room getRoom();

}
