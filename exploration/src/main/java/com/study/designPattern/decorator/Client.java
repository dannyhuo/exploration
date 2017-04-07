package com.study.designPattern.decorator;

public class Client {

	public static void main(String[] args) {
		ICar car = new Car();
		car.run();
		
		System.out.println("change mode...");
		FlayCar flayCar = new FlayCar(car);
		flayCar.run();
		
		System.out.println("change mode agin...");
		WaterCar waterCar = new WaterCar(flayCar);
		waterCar.run();
	}
}
