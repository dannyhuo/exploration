package cn.hly.ordrpt.model;

import java.io.Serializable;

import cn.hly.ordrpt.model.meta.SubsidyMeta;
import hly.com.ordrpt.model.Canstant;

public class BaseValue implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1340664643803078584L;
	
	//成人数，成人数>0时，+1
	protected int auditCount;
	protected int auditSum;
	//儿童数，儿童数>0时，+1
	protected int childrenCount;
	protected int childrenSum;
	
	//间夜数
	protected int roomNightSum;
	//促销金额
	protected double salesPromotionAmountSum;
	//优惠券金额
	protected double couponAmountSum;
	//营业额
	protected double turnoverAmountSum;
	//毛利
	protected double grossMarginSum;
	//类型，门票/线路
	protected String goodsType;
	//补贴
	protected SubsidyMeta subsidy = new SubsidyMeta();
	
	protected void eat(OrdModel ord, RuleModel rule){
		this.goodsType = rule.getGoodsType().data;
		this.addAudit(ord.getAdult().data);
		this.addChildren(ord.getChildren().data);
		this.addCouponAmount(ord.getCouponAmount().data);
		this.addGrossMarginSum(ord.getGrossMargin().data);
		this.addRoomNight(ord.getRoomNight().data);
		this.addSalesPromotionAmount(ord.getSalesPromotionAmount().data);
		this.addTurnoverAmountSum(ord.getTurnover().data);
	}
	
	/**
	 * 计算补贴
	 * @param ord
	 * @param rule
	 * @return
	 */
	protected double calSubsidy(OrdModel ord, RuleModel rule){
		if(Canstant.SUBSIDY_TYPE.BY_AUDIT.getValue() == 
				rule.getSubsidyType().data.shortValue()){
			Integer adult = ord.getAdult().data;
			if(adult != null && adult.intValue() > 0){
				return rule.getSubsidy().data.doubleValue() * adult.doubleValue();
			}
		}else if(Canstant.SUBSIDY_TYPE.BY_AUDIT_AND_CHILDREN.getValue() == 
				rule.getSubsidyType().data.shortValue()){
			Integer adult = ord.getAdult().data;
			Integer children = ord.getChildren().data;
			double subsidy = 0;
			if(adult != null && adult.intValue() > 0){
				subsidy += rule.getSubsidy().data.doubleValue() * adult.doubleValue();
			}
			if(children != null && children.intValue() > 0){
				subsidy += rule.getSubsidy().data.doubleValue() * children.doubleValue();
			}
			return subsidy;
		}else if(Canstant.SUBSIDY_TYPE.BY_ROOM_NIGHT.getValue() == 
				rule.getSubsidyType().data.shortValue()){
			Integer roomNight = ord.getRoomNight().data;
			if(roomNight != null && roomNight.intValue() > 0){
				return rule.getSubsidy().data.doubleValue() * roomNight.doubleValue();
			}
		}else if(Canstant.SUBSIDY_TYPE.BY_CHILDREN.getValue() == 
				rule.getSubsidyType().data.shortValue()){
			Integer children = ord.getChildren().data;
			if(children != null && children.intValue() > 0){
				return rule.getSubsidy().data.doubleValue() * children.doubleValue();
			}
		}
		return 0;
	}
	

	public int getAuditSum() {
		return auditSum;
	}
	public int getChildrenSum() {
		return childrenSum;
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
	
	

	/**
	 * 获取补贴总金额
	 * @param weft
	 * @return
	 */
	public Double getSubsidySum(Long weft) {
		return subsidy.getSubsidy(weft);
	}
	/**
	 * 获取特卖补贴总金额
	 * @param weft
	 * @return
	 */
	public Double getTemaiSubsidySum(Long weft){
		return subsidy.getTemaiSubsidy(weft);
	}
	/**
	 * 获取特卖占比，给定的
	 * @param weft
	 * @return
	 */
	public Float getTemaiPercent(Long weft){
		return this.subsidy.getTemaiPercent(weft);
	}
	/**
	 * 计算新的占比，新计算的
	 * @param weft
	 * @return
	 */
	public Float getCalPercent(Long weft){
		Double v1 = getSubsidySum(weft);
		Double v2 = getTemaiSubsidySum(weft);
		if(null != v1 && null != v2 && v1.floatValue() != 0f){
			return (v2.floatValue() / v1.floatValue());
		}
		return null;
	}
	/**
	 * 分纬度补贴计算
	 * @param weft	纬度
	 * @param subsidyAmount	补贴金额
	 * @param temaiPercent	特卖补贴百分比
	 */
	public void addSubsidyAmount(Long weft, Double subsidyAmount, 
			Float temaiPercent) {
		this.subsidy.eatSubsidy(weft, temaiPercent, subsidyAmount);
	}

}
