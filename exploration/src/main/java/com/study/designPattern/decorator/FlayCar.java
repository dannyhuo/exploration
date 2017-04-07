package com.study.designPattern.decorator;

public class FlayCar extends SuperCar {

	public FlayCar(ICar car) {
		super(car);
	}

	@Override
	public void run() {
		super.run();
		fly();
	}

	private void fly(){
		System.out.println("fly....");
	}
}
