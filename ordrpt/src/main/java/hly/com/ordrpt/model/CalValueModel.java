package hly.com.ordrpt.model;

import org.apache.poi.ss.usermodel.Row;

public class CalValueModel extends BaseCalValueModel{
	private Long orderId;
	//事业部
	private String buName;
	//分销渠道ID
	private Integer salesChannelId;
	//是否是CPSID
	private String isCpsid;
	//打包产品ID
	private Long productId;
	//打包商品ID
	private Long goodsId;
	
	//成人数，成人数>0时，+1
	private int auditCount;
	private int auditSum;
	//儿童数，儿童数>0时，+1
	private int childrenCount;
	private int childrenSum;
	
	//间夜数
	private int roomNightSum;
	//补贴金额
	private double subsidyAmountSum;
	//特卖补贴占比
	private Float temaiPercent;

	//促销金额
	private double salesPromotionAmountSum;
	//优惠券金额
	private double couponAmountSum;
	//营业额
	private double turnoverAmountSum;
	//毛利
	private double grossMarginSum;
	
	//类型，门票/线路
	private String goodsType;
		
	/**
	 * 写入excel工作簿
	 * @param wb
	 * @return
	 */
	public void writeToSheet(Row row, BaseCalValueModel total){
		row.createCell(0).setCellValue(buName);
		row.createCell(1).setCellValue(salesChannelId);
		row.createCell(2).setCellValue(isCpsid);
		row.createCell(3).setCellValue(productId);
		row.createCell(4).setCellValue(goodsId);
		row.createCell(5).setCellValue(orderId);
		
		Float audit = null;
		Float children = null;
		if("线路".equals(goodsType)){
			audit = getAuditAvg();
			children = getChildrenAvg();
		}else{
			audit = (float) auditSum;
			children = (float) childrenSum;
		}
		row.createCell(6).setCellValue(audit);
		row.createCell(7).setCellValue(children);
		
		row.createCell(8).setCellValue(roomNightSum);
		row.createCell(9).setCellValue(subsidyAmountSum);
		double temaiSubsidyAmountSum = 0;
		if(null != temaiPercent){
			row.createCell(10).setCellValue(temaiPercent.doubleValue());
			temaiSubsidyAmountSum = subsidyAmountSum * temaiPercent.doubleValue();
			row.createCell(11).setCellValue(temaiSubsidyAmountSum);
		}else{
			temaiSubsidyAmountSum = subsidyAmountSum;
			row.createCell(11).setCellValue(subsidyAmountSum);
		}
		
		row.createCell(12).setCellValue(salesPromotionAmountSum);
		row.createCell(13).setCellValue(couponAmountSum);
		row.createCell(14).setCellValue(turnoverAmountSum);
		row.createCell(15).setCellValue(grossMarginSum);
		
		total.addRoomNight(roomNightSum);
		total.addSubsidyAmount(subsidyAmountSum);
		total.addTemaiSubsidyAmountSum(temaiSubsidyAmountSum);
		total.addSalesPromotionAmount(salesPromotionAmountSum);
		total.addCouponAmount(couponAmountSum);
		total.addTurnoverAmountSum(turnoverAmountSum);
		total.addGrossMarginSum(grossMarginSum);
		total.addAudit(audit.intValue());
		total.addChildren(children.intValue());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("buName:");
		sb.append(buName);
		
		sb.append(",salesChannelId:");
		sb.append(salesChannelId);
		
		sb.append(",isCpsid:");
		sb.append(isCpsid);
		
		sb.append(",productId:");
		sb.append(productId);
		
		sb.append(",goodsId:");
		sb.append(goodsId);
		
		sb.append(",auditAvg:");
		sb.append(getAuditAvg());
		
		sb.append(",childrenAvg:");
		sb.append(getChildrenAvg());
		
		sb.append(",roomNightSum:");
		sb.append(roomNightSum);
		
		sb.append(",subsidyAmountSum:");
		sb.append(subsidyAmountSum);
		
		sb.append(",salesPromotionAmountSum:");
		sb.append(salesPromotionAmountSum);
		
		sb.append(",couponAmountSum:");
		sb.append(couponAmountSum);
		
		sb.append(",turnoverAmountSum:");
		sb.append(turnoverAmountSum);
		
		sb.append(",grossMarginSum:");
		sb.append(grossMarginSum);
		sb.append("}");
		return sb.toString();
	}
	
	
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getBuName() {
		return buName;
	}
	public void setBuName(String buName) {
		this.buName = buName;
	}
	public Integer getSalesChannelId() {
		return salesChannelId;
	}
	public void setSalesChannelId(Integer salesChannelId) {
		this.salesChannelId = salesChannelId;
	}
	public String getIsCpsid() {
		return isCpsid;
	}
	public void setIsCpsid(String isCpsid) {
		this.isCpsid = isCpsid;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}
	public int getAuditCount() {
		return auditCount;
	}
	/**
	 * 成人累加
	 * @param audit
	 */
	public void addAudit(Integer audit) {
		if(null != audit && audit.intValue() > 0){
			this.auditCount++;
			this.auditSum += audit.intValue();
		}
	}
	
	/**
	 * 平均成人数
	 * @return
	 */
	public float getAuditAvg(){
		if(this.auditCount > 0){
			return ((float)this.auditSum) / this.auditCount;
		}
		return 0f;
	}
	
	public Integer getChildrenCount() {
		return childrenCount;
	}
	
	/**
	 * 儿童累加
	 * @param children
	 */
	public void addChildren(Integer children) {
		if(null != children && children.intValue() > 0){
			this.childrenCount++;
			this.childrenSum += children.intValue();
		}
	}
	
	/**
	 * 平均儿童数
	 */
	public float getChildrenAvg(){
		if(this.childrenCount > 0){
			return ((float)this.childrenSum) / this.childrenCount;
		}
		return 0f;
	}
	
	public int getRoomNightSum() {
		return roomNightSum;
	}
	
	/**
	 * 间夜数累加求合
	 * @param roomNight
	 */
	public void addRoomNight(Integer roomNight) {
		if(null != roomNight){
			this.roomNightSum += roomNight.intValue();
		}
	}
	public double getSubsidyAmountSum() {
		return subsidyAmountSum;
	}
	/**
	 * 补贴累加求合
	 * @param subsidyAmount
	 */
	public void addSubsidyAmount(Double subsidyAmount) {
		if(null != subsidyAmount){
			this.subsidyAmountSum += subsidyAmount.doubleValue();
		}
	}
	public double getSalesPromotionAmountSum() {
		return salesPromotionAmountSum;
	}
	/**
	 * 促销累加求合
	 * @param salesPromotionAmount
	 */
	public void addSalesPromotionAmount(Double salesPromotionAmount) {
		if(null != salesPromotionAmount){
			this.salesPromotionAmountSum += salesPromotionAmount.doubleValue();
		}
	}
	public double getCouponAmountSum() {
		return couponAmountSum;
	}
	/**
	 * 优惠券累加求合
	 * @param couponAmount
	 */
	public void addCouponAmount(Double couponAmount) {
		if(null != couponAmount){
			this.couponAmountSum += couponAmount.doubleValue();
		}
	}
	public double getTurnoverAmountSum() {
		return turnoverAmountSum;
	}
	/**
	 * 营业额累加求合
	 * @param turnoverAmount
	 */
	public void addTurnoverAmountSum(Double turnoverAmount) {
		if(null != turnoverAmount){
			this.turnoverAmountSum += turnoverAmount.doubleValue();
		}
	}
	public double getGrossMarginSum() {
		return grossMarginSum;
	}
	/**
	 * 毛利累加求合
	 * @param grossMargin
	 */
	public void addGrossMarginSum(Double grossMargin) {
		if(null != grossMargin){
			this.grossMarginSum += grossMargin.doubleValue();
		}
	}

	public String getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(String goodsType) {
		this.goodsType = goodsType;
	}
	
	public Float getTemaiPercent() {
		return temaiPercent;
	}

	public void setTemaiPercent(Float temaiPercent) {
		this.temaiPercent = temaiPercent;
	}
}
