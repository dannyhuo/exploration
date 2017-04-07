package com.study.designPattern.decorator;

/**
 * 装饰器
 * @author huoqiang
 *
 */
public abstract class SuperCar implements ICar {

	private ICar car;
	
	public SuperCar(ICar car) {
		this.car = car;
	}
	
	@Override
	public void run() {
		car.run();
	}

}
