package hly.com.ordrpt.model;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class OrdModel {

	private Tuple<Integer, Date> createTime = new Tuple<>(0);
	private Tuple<Integer, Date> paymentTime = new Tuple<>(1);
	private Tuple<Integer, Date> visitTime = new Tuple<>(2);
	private Tuple<Integer, String> buName = new Tuple<>(3);
	private Tuple<Integer, Long> orderId = new Tuple<>(4);
	private Tuple<Integer, Long> productId = new Tuple<>(5);
	private Tuple<Integer, String> productName = new Tuple<>(6);
	private Tuple<Integer, Long> goodsId = new Tuple<>(7);
	private Tuple<Integer, String> goodsName = new Tuple<>(8);
	//营业额
	private Tuple<Integer, Double> turnover = new Tuple<>(9);
	//毛利
	private Tuple<Integer, Double> grossMargin = new Tuple<>(10);
	private Tuple<Integer, Integer> adult = new Tuple<>(11);
	private Tuple<Integer, Integer> children = new Tuple<>(12);
	//间夜数
	private Tuple<Integer, Integer> roomNight = new Tuple<>(13);
	//产品销量
	private Tuple<Integer, Integer> productSellCount = new Tuple<>(14);
	//促销金额
	private Tuple<Integer, Double> salesPromotionAmount = new Tuple<>(15);
	//优惠券金额
	private Tuple<Integer, Double> couponAmount = new Tuple<>(16);
	//下单渠道
	private Tuple<Integer, String> ordChannel = new Tuple<>(17);
	//分销商渠道ID
	private Tuple<Integer, Integer> saleChannelId = new Tuple<>(18);
	private Tuple<Integer, String> cpsId = new Tuple<>(19);
	//分销商渠道类型ID
	private Tuple<Integer, Integer> saleChannelTypeId = new Tuple<>(20);
	//打包产品经理
	private Tuple<Integer, String> packagePm = new Tuple<>(21);
	
	
	
	
	public static OrdModel getOrdModel(Row row) throws Exception{
		if(null == row){
			return null;
		}
		
		OrdModel ordModel = new OrdModel();
		Cell cell = row.getCell(ordModel.adult.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.adult.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.buName.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.buName.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.children.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.children.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.couponAmount.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.couponAmount.item2 = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.cpsId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.cpsId.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.createTime.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.createTime.item2 = cell.getDateCellValue();
		}
		
		cell = row.getCell(ordModel.goodsId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.goodsId.item2 = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.goodsName.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.goodsName.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.grossMargin.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.grossMargin.item2 = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.ordChannel.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.ordChannel.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.orderId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.orderId.item2 = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.packagePm.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.packagePm.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.paymentTime.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.paymentTime.item2 = cell.getDateCellValue();
		}
		
		cell = row.getCell(ordModel.productId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productId.item2 = (long) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.productName.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productName.item2 = cell.getStringCellValue();
		}
		
		cell = row.getCell(ordModel.productSellCount.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.productSellCount.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.roomNight.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.roomNight.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.saleChannelId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.saleChannelId.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.saleChannelTypeId.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.saleChannelTypeId.item2 = (int) cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.salesPromotionAmount.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.salesPromotionAmount.item2 = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.turnover.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.turnover.item2 = cell.getNumericCellValue();
		}
		
		cell = row.getCell(ordModel.visitTime.item1, Row.RETURN_BLANK_AS_NULL);
		if(null != cell){
			ordModel.visitTime.item2 = cell.getDateCellValue();
		}
		
		return ordModel;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("{");
		
		sb.append("createTime:");
		sb.append(createTime.item2);
		sb.append(",paymentTime:");
		sb.append(paymentTime.item2);
		sb.append(",visitTime:");
		sb.append(visitTime.item2);
		sb.append(",buName:");
		sb.append(buName.item2);
		sb.append(",orderId:");
		sb.append(orderId.item2);
		sb.append(",productId:");
		sb.append(productId.item2);
		sb.append(",productName:");
		sb.append(productName.item2);
		
		sb.append(",goodsId:");
		sb.append(goodsId.item2);
		
		sb.append(",goodsName:");
		sb.append(goodsName.item2);
		
		sb.append(",turnover:");
		sb.append(turnover.item2);
		
		sb.append(",grossMargin:");
		sb.append(grossMargin.item2);
		
		sb.append(",adult:");
		sb.append(adult.item2);
		
		sb.append(",children:");
		sb.append(children.item2);
		
		sb.append(",roomNight:");
		sb.append(roomNight.item2);
		
		sb.append(",productSellCount:");
		sb.append(productSellCount.item2);
		
		sb.append(",salesPromotionAmount:");
		sb.append(salesPromotionAmount.item2);
		
		sb.append(",couponAmount:");
		sb.append(couponAmount.item2);
		
		sb.append(",ordChannel:");
		sb.append(ordChannel.item2);
		
		sb.append(",saleChannelId:");
		sb.append(saleChannelId.item2);
		
		sb.append(",cpsId:");
		sb.append(cpsId.item2);
		
		sb.append(",saleChannelTypeId:");
		sb.append(saleChannelTypeId.item2);
		
		sb.append(",packagePm:");
		sb.append(packagePm.item2);
		
		sb.append("}");
		
		return sb.toString();
	}
	
	
	
	
	public Tuple<Integer, Date> getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Tuple<Integer, Date> createTime) {
		this.createTime = createTime;
	}
	public Tuple<Integer, Date> getPaymentTime() {
		return paymentTime;
	}
	public void setPaymentTime(Tuple<Integer, Date> paymentTime) {
		this.paymentTime = paymentTime;
	}
	public Tuple<Integer, Date> getVisitTime() {
		return visitTime;
	}
	public void setVisitTime(Tuple<Integer, Date> visitTime) {
		this.visitTime = visitTime;
	}
	public Tuple<Integer, String> getBuName() {
		return buName;
	}
	public void setBuName(Tuple<Integer, String> buName) {
		this.buName = buName;
	}
	public Tuple<Integer, Long> getOrderId() {
		return orderId;
	}
	public void setOrderId(Tuple<Integer, Long> orderId) {
		this.orderId = orderId;
	}
	public Tuple<Integer, Long> getProductId() {
		return productId;
	}
	public void setProductId(Tuple<Integer, Long> productId) {
		this.productId = productId;
	}
	public Tuple<Integer, String> getProductName() {
		return productName;
	}
	public void setProductName(Tuple<Integer, String> productName) {
		this.productName = productName;
	}
	public Tuple<Integer, Long> getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(Tuple<Integer, Long> goodsId) {
		this.goodsId = goodsId;
	}
	public Tuple<Integer, String> getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(Tuple<Integer, String> goodsName) {
		this.goodsName = goodsName;
	}
	public Tuple<Integer, Double> getTurnover() {
		return turnover;
	}
	public void setTurnover(Tuple<Integer, Double> turnover) {
		this.turnover = turnover;
	}
	public Tuple<Integer, Double> getGrossMargin() {
		return grossMargin;
	}
	public void setGrossMargin(Tuple<Integer, Double> grossMargin) {
		this.grossMargin = grossMargin;
	}
	public Tuple<Integer, Integer> getAdult() {
		return adult;
	}
	public void setAdult(Tuple<Integer, Integer> adult) {
		this.adult = adult;
	}
	public Tuple<Integer, Integer> getChildren() {
		return children;
	}
	public void setChildren(Tuple<Integer, Integer> children) {
		this.children = children;
	}
	public Tuple<Integer, Integer> getRoomNight() {
		return roomNight;
	}
	public void setRoomNight(Tuple<Integer, Integer> roomNight) {
		this.roomNight = roomNight;
	}
	public Tuple<Integer, Integer> getProductSellCount() {
		return productSellCount;
	}
	public void setProductSellCount(Tuple<Integer, Integer> productSellCount) {
		this.productSellCount = productSellCount;
	}
	public Tuple<Integer, Double> getSalesPromotionAmount() {
		return salesPromotionAmount;
	}
	public void setSalesPromotionAmount(Tuple<Integer, Double> salesPromotionAmount) {
		this.salesPromotionAmount = salesPromotionAmount;
	}
	public Tuple<Integer, Double> getCouponAmount() {
		return couponAmount;
	}
	public void setCouponAmount(Tuple<Integer, Double> couponAmount) {
		this.couponAmount = couponAmount;
	}
	public Tuple<Integer, String> getOrdChannel() {
		return ordChannel;
	}
	public void setOrdChannel(Tuple<Integer, String> ordChannel) {
		this.ordChannel = ordChannel;
	}
	public Tuple<Integer, Integer> getSaleChannelId() {
		return saleChannelId;
	}
	public void setSaleChannelId(Tuple<Integer, Integer> saleChannelId) {
		this.saleChannelId = saleChannelId;
	}
	public Tuple<Integer, String> getCpsId() {
		return cpsId;
	}
	public void setCpsId(Tuple<Integer, String> cpsId) {
		this.cpsId = cpsId;
	}
	public Tuple<Integer, Integer> getSaleChannelTypeId() {
		return saleChannelTypeId;
	}
	public void setSaleChannelTypeId(Tuple<Integer, Integer> saleChannelTypeId) {
		this.saleChannelTypeId = saleChannelTypeId;
	}
	public Tuple<Integer, String> getPackagePm() {
		return packagePm;
	}
	public void setPackagePm(Tuple<Integer, String> packagePm) {
		this.packagePm = packagePm;
	}
	
}
