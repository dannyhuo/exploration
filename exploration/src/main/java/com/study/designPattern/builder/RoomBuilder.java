package com.study.designPattern.builder;

import com.study.designPattern.builder.model.Door;
import com.study.designPattern.builder.model.Floor;
import com.study.designPattern.builder.model.Room;
import com.study.designPattern.builder.model.Window;

public class RoomBuilder implements Builder {
	
	private Room room = new Room();

	@Override
	public void makeWindow() {
		this.room.setWindow(new Window());
	}

	@Override
	public void makeDoor() {
		this.room.setDoor(new Door());
	}

	@Override
	public void makeFloor() {
		this.room.setFloor(new Floor());
	}
	
	@Override
	public Room getRoom(){
		return room;
	}

}
