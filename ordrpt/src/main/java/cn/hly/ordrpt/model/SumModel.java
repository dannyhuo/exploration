package cn.hly.ordrpt.model;

import org.apache.poi.ss.usermodel.Row;

public class SumModel {
	
	private float adultSum;
	
	private float childrenSum;
	
	private int roomNightSum;
	
	private int orderCount;
	
	private double subsidyAmountSum;
	
	private double temaiSubsidyAmountSum;
	
	private double salesPromotionAmountSum;
	
	private double couponAmountSum;
	
	private double turnoverAmountSum;
	
	private double grossMarginSum;

	
	public void writeSum(Row row, int cellStart){
		if(orderCount > 0){
			row.createCell(cellStart-1).setCellValue(orderCount);
		}
		row.createCell(cellStart+0).setCellValue(adultSum);
		row.createCell(cellStart+1).setCellValue(childrenSum);
		row.createCell(cellStart+2).setCellValue(roomNightSum);
		row.createCell(cellStart+3).setCellValue(subsidyAmountSum);
		row.createCell(cellStart+5).setCellValue(temaiSubsidyAmountSum);
		row.createCell(cellStart+6).setCellValue(salesPromotionAmountSum);
		row.createCell(cellStart+7).setCellValue(couponAmountSum);
		row.createCell(cellStart+8).setCellValue(turnoverAmountSum);
		row.createCell(cellStart+9).setCellValue(grossMarginSum);
	}
	
	
	public float getAdultSum() {
		return adultSum;
	}

	public void addAdultSum(Float adult) {
		if(null != adult){
			this.adultSum += adult.floatValue();
		}
	}

	public float getChildrenSum() {
		return childrenSum;
	}

	public void addChildrenSum(Float children) {
		if(null != children){
			this.childrenSum += children.floatValue();
		}
	}

	public int getRoomNightSum() {
		return roomNightSum;
	}

	public int getOrderCount() {
		return orderCount;
	}
	
	public void addOrderCount(Integer count){
		if(null != count){
			this.orderCount += count;
		}
	}
	
	public void addRoomNightSum(Integer roomNight) {
		if(null != roomNight){
			this.roomNightSum += roomNight.intValue();
		}
	}

	public double getSubsidyAmountSum() {
		return subsidyAmountSum;
	}

	public void addSubsidyAmountSum(Double subsidyAmount) {
		if(null != subsidyAmount){
			this.subsidyAmountSum += subsidyAmount.doubleValue();
		}
	}

	public double getTemaiSubsidyAmountSum() {
		return temaiSubsidyAmountSum;
	}

	public void addTemaiSubsidyAmountSum(Double temaiSubsidyAmount) {
		if(null != temaiSubsidyAmount){
			this.temaiSubsidyAmountSum += temaiSubsidyAmount.doubleValue();
		}
	}

	public double getSalesPromotionAmountSum() {
		return salesPromotionAmountSum;
	}

	public void addSalesPromotionAmountSum(Double salesPromotionAmount) {
		if(null != salesPromotionAmount){
			this.salesPromotionAmountSum += salesPromotionAmount.doubleValue();
		}
		
	}

	public double getCouponAmountSum() {
		return couponAmountSum;
	}

	public void addCouponAmountSum(Double couponAmount) {
		if(null != couponAmount){
			this.couponAmountSum += couponAmount.doubleValue();
		}
	}

	public double getTurnoverAmountSum() {
		return turnoverAmountSum;
	}

	public void addTurnoverAmountSum(Double turnoverAmount) {
		if(null != turnoverAmount){
			this.turnoverAmountSum += turnoverAmount.doubleValue();
		}
	}

	public double getGrossMarginSum() {
		return grossMarginSum;
	}

	public void addGrossMarginSum(Double grossMargin) {
		if(null != grossMargin){
			this.grossMarginSum += grossMargin.doubleValue();
		}
	}
	
}
