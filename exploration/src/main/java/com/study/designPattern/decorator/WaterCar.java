package com.study.designPattern.decorator;

public class WaterCar extends SuperCar implements ICar {

	public WaterCar(ICar car) {
		super(car);
	}

	@Override
	public void run() {
		super.run();
		swiming();
	}

	private void swiming(){
		System.out.println("Swiming...");
	}
}
