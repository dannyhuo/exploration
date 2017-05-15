package hly.com.ordrpt.model;

public class BaseCalValueModel {
	
	public int getAuditSum() {
		return auditSum;
	}
	public int getChildrenSum() {
		return childrenSum;
	}
	//成人数，成人数>0时，+1
	protected int auditCount;
	protected int auditSum;
	//儿童数，儿童数>0时，+1
	protected int childrenCount;
	protected int childrenSum;
	
	//间夜数
	protected int roomNightSum;
	//补贴金额
	protected double subsidyAmountSum;
	//特卖补贴占比
	protected Float temaiPercent;
	//特卖补贴总金额
	protected double temaiSubsidyAmountSum;
	//促销金额
	protected double salesPromotionAmountSum;
	//优惠券金额
	protected double couponAmountSum;
	//营业额
	protected double turnoverAmountSum;
	//毛利
	protected double grossMarginSum;
	
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
	
	public double getTemaiSubsidyAmountSum() {
		return temaiSubsidyAmountSum;
	}
	public void setTemaiSubsidyAmountSum(double temaiSubsidyAmountSum) {
		this.temaiSubsidyAmountSum = temaiSubsidyAmountSum;
	}
	public void addTemaiSubsidyAmountSum(double temaiSubsidyAmount) {
		this.temaiSubsidyAmountSum += temaiSubsidyAmount;
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

	public Float getTemaiPercent() {
		return temaiPercent;
	}

	public void setTemaiPercent(Float temaiPercent) {
		this.temaiPercent = temaiPercent;
	}
}
