package cn.hly.ordrpt.model;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import cn.hly.ordrpt.model.meta.CellMeta;

public class OrdModel {
	
	private CellMeta<Integer, Date> createTime = new CellMeta<>(0);
	private CellMeta<Integer, Date> paymentTime = new CellMeta<>(1);
	private CellMeta<Integer, Date> visitTime = new CellMeta<>(2);
	/**渠道名称*/
	private CellMeta<Integer, String> buName = new CellMeta<>(3);
	private CellMeta<Integer, Long> orderId = new CellMeta<>(4);
	private CellMeta<Integer, Long> productId = new CellMeta<>(5);
	private CellMeta<Integer, String> productName = new CellMeta<>(6);
	private CellMeta<Integer, Long> goodsId = new CellMeta<>(7);
	private CellMeta<Integer, String> goodsName = new CellMeta<>(8);
	/**营业额*/
	private CellMeta<Integer, Double> turnover = new CellMeta<>(9);
	/**毛利*/
	private CellMeta<Integer, Double> grossMargin = new CellMeta<>(10);
	private CellMeta<Integer, Integer> adult = new CellMeta<>(11);
	private CellMeta<Integer, Integer> children = new CellMeta<>(12);
	/**间夜数*/
	private CellMeta<Integer, Integer> roomNight = new CellMeta<>(13);
	/**产品销量*/
	private CellMeta<Integer, Integer> productSellCount = new CellMeta<>(14);
	/**促销金额*/
	private CellMeta<Integer, Double> salesPromotionAmount = new CellMeta<>(15);
	/**优惠券金额*/
	private CellMeta<Integer, Double> couponAmount = new CellMeta<>(16);
	/**下单渠道*/
	private CellMeta<Integer, String> ordChannel = new CellMeta<>(17);
	/**分销商渠道ID*/
	private CellMeta<Integer, Integer> saleChannelId = new CellMeta<>(18);
	private CellMeta<Integer, String> cpsId = new CellMeta<>(19);
	/**分销商渠道类型ID*/
	private CellMeta<Integer, Integer> saleChannelTypeId = new CellMeta<>(20);
	/**打包产品经理*/
	private CellMeta<Integer, String> packagePm = new CellMeta<>(21);
	
	
	public static OrdModel getOrdModel(Row row) throws Exception{
		if(null == row){
			return null;
		}
		
		OrdModel ordModel = new OrdModel();
		Cell cell = row.getCell(ordModel.adult.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.adult.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.buName.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.buName.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.children.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.children.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.couponAmount.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.couponAmount.data = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.cpsId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.cpsId.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.createTime.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.createTime.data = cell.getDateCellValue();
		}
		
		cell = row.getCell(ordModel.goodsId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.goodsId.data = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.goodsName.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.goodsName.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.grossMargin.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.grossMargin.data = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.ordChannel.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.ordChannel.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.orderId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.orderId.data = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.packagePm.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.packagePm.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.paymentTime.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.paymentTime.data = cell.getDateCellValue();
		}
		
		cell = row.getCell(ordModel.productId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productId.data = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.productName.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productName.data = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.productSellCount.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productSellCount.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.roomNight.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.roomNight.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.saleChannelId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.saleChannelId.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.saleChannelTypeId.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.saleChannelTypeId.data = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.salesPromotionAmount.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.salesPromotionAmount.data = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.turnover.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.turnover.data = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.visitTime.index, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.visitTime.data = cell.getDateCellValue();
		}
		
		return ordModel;
	}
	
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{");
		
		sb.append("createTime:");
		sb.append(createTime.data);
		sb.append(",paymentTime:");
		sb.append(paymentTime.data);
		sb.append(",visitTime:");
		sb.append(visitTime.data);
		sb.append(",buName:");
		sb.append(buName.data);
		sb.append(",orderId:");
		sb.append(orderId.data);
		sb.append(",productId:");
		sb.append(productId.data);
		sb.append(",productName:");
		sb.append(productName.data);
		
		sb.append(",goodsId:");
		sb.append(goodsId.data);
		
		sb.append(",goodsName:");
		sb.append(goodsName.data);
		
		sb.append(",turnover:");
		sb.append(turnover.data);
		
		sb.append(",grossMargin:");
		sb.append(grossMargin.data);
		
		sb.append(",adult:");
		sb.append(adult.data);
		
		sb.append(",children:");
		sb.append(children.data);
		
		sb.append(",roomNight:");
		sb.append(roomNight.data);
		
		sb.append(",productSellCount:");
		sb.append(productSellCount.data);
		
		sb.append(",salesPromotionAmount:");
		sb.append(salesPromotionAmount.data);
		
		sb.append(",couponAmount:");
		sb.append(couponAmount.data);
		
		sb.append(",ordChannel:");
		sb.append(ordChannel.data);
		
		sb.append(",saleChannelId:");
		sb.append(saleChannelId.data);
		
		sb.append(",cpsId:");
		sb.append(cpsId.data);
		
		sb.append(",saleChannelTypeId:");
		sb.append(saleChannelTypeId.data);
		
		sb.append(",packagePm:");
		sb.append(packagePm.data);
		
		sb.append("}");
		
		return sb.toString();
	}
	
	
	
	
	public CellMeta<Integer, Date> getCreateTime() {
		return createTime;
	}
	public void setCreateTime(CellMeta<Integer, Date> createTime) {
		this.createTime = createTime;
	}
	public CellMeta<Integer, Date> getPaymentTime() {
		return paymentTime;
	}
	public void setPaymentTime(CellMeta<Integer, Date> paymentTime) {
		this.paymentTime = paymentTime;
	}
	public CellMeta<Integer, Date> getVisitTime() {
		return visitTime;
	}
	public void setVisitTime(CellMeta<Integer, Date> visitTime) {
		this.visitTime = visitTime;
	}
	public CellMeta<Integer, String> getBuName() {
		return buName;
	}
	public void setBuName(CellMeta<Integer, String> buName) {
		this.buName = buName;
	}
	public CellMeta<Integer, Long> getOrderId() {
		return orderId;
	}
	public void setOrderId(CellMeta<Integer, Long> orderId) {
		this.orderId = orderId;
	}
	public CellMeta<Integer, Long> getProductId() {
		return productId;
	}
	public void setProductId(CellMeta<Integer, Long> productId) {
		this.productId = productId;
	}
	public CellMeta<Integer, String> getProductName() {
		return productName;
	}
	public void setProductName(CellMeta<Integer, String> productName) {
		this.productName = productName;
	}
	public CellMeta<Integer, Long> getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(CellMeta<Integer, Long> goodsId) {
		this.goodsId = goodsId;
	}
	public CellMeta<Integer, String> getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(CellMeta<Integer, String> goodsName) {
		this.goodsName = goodsName;
	}
	public CellMeta<Integer, Double> getTurnover() {
		return turnover;
	}
	public void setTurnover(CellMeta<Integer, Double> turnover) {
		this.turnover = turnover;
	}
	public CellMeta<Integer, Double> getGrossMargin() {
		return grossMargin;
	}
	public void setGrossMargin(CellMeta<Integer, Double> grossMargin) {
		this.grossMargin = grossMargin;
	}
	public CellMeta<Integer, Integer> getAdult() {
		return adult;
	}
	public void setAdult(CellMeta<Integer, Integer> adult) {
		this.adult = adult;
	}
	public CellMeta<Integer, Integer> getChildren() {
		return children;
	}
	public void setChildren(CellMeta<Integer, Integer> children) {
		this.children = children;
	}
	public CellMeta<Integer, Integer> getRoomNight() {
		return roomNight;
	}
	public void setRoomNight(CellMeta<Integer, Integer> roomNight) {
		this.roomNight = roomNight;
	}
	public CellMeta<Integer, Integer> getProductSellCount() {
		return productSellCount;
	}
	public void setProductSellCount(CellMeta<Integer, Integer> productSellCount) {
		this.productSellCount = productSellCount;
	}
	public CellMeta<Integer, Double> getSalesPromotionAmount() {
		return salesPromotionAmount;
	}
	public void setSalesPromotionAmount(CellMeta<Integer, Double> salesPromotionAmount) {
		this.salesPromotionAmount = salesPromotionAmount;
	}
	public CellMeta<Integer, Double> getCouponAmount() {
		return couponAmount;
	}
	public void setCouponAmount(CellMeta<Integer, Double> couponAmount) {
		this.couponAmount = couponAmount;
	}
	public CellMeta<Integer, String> getOrdChannel() {
		return ordChannel;
	}
	public void setOrdChannel(CellMeta<Integer, String> ordChannel) {
		this.ordChannel = ordChannel;
	}
	public CellMeta<Integer, Integer> getSaleChannelId() {
		return saleChannelId;
	}
	public void setSaleChannelId(CellMeta<Integer, Integer> saleChannelId) {
		this.saleChannelId = saleChannelId;
	}
	public CellMeta<Integer, String> getCpsId() {
		return cpsId;
	}
	public void setCpsId(CellMeta<Integer, String> cpsId) {
		this.cpsId = cpsId;
	}
	public CellMeta<Integer, Integer> getSaleChannelTypeId() {
		return saleChannelTypeId;
	}
	public void setSaleChannelTypeId(CellMeta<Integer, Integer> saleChannelTypeId) {
		this.saleChannelTypeId = saleChannelTypeId;
	}
	public CellMeta<Integer, String> getPackagePm() {
		return packagePm;
	}
	public void setPackagePm(CellMeta<Integer, String> packagePm) {
		this.packagePm = packagePm;
	}

}
