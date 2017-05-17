package cn.hly.ordrpt.model;

import org.apache.poi.ss.usermodel.Row;

import cn.hly.ordrpt.model.meta.SubsidyMeta;

/**
 * 订单纬度值类型
 * @author danny
 *
 */
public class OrderWeftValue extends BaseValue{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8469607455053857318L;
	
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
	
	private SubsidyMeta subsidy = new SubsidyMeta();
	
	public void eat(OrdModel ord, RuleModel rule){
		this.orderId = ord.getOrderId().data;
		this.buName = ord.getBuName().data;
		this.salesChannelId = ord.getSaleChannelId().data;
		this.isCpsid = ord.getCpsId().data;
		this.productId = ord.getProductId().data;
		this.goodsId = ord.getProductId().data;
		
		this.addSubsidyAmount(ord.getOrderId().data, calSubsidy(ord, rule), rule.getSubsidyPercent().data);
		
		super.eat(ord, rule);
	}
	
	/**
	 * 写入行
	 * @param row
	 */
	public void write(Row row){
		row.createCell(0).setCellValue(buName);
		row.createCell(1).setCellValue(salesChannelId);
		row.createCell(2).setCellValue(isCpsid);
		row.createCell(3).setCellValue(productId);
		row.createCell(4).setCellValue(goodsId);
		row.createCell(5).setCellValue(orderId);
		if("线路".equals(goodsType)){
			row.createCell(6).setCellValue(getAuditAvg());
			row.createCell(7).setCellValue(getChildrenAvg());
		}else{
			row.createCell(6).setCellValue(auditSum);
			row.createCell(7).setCellValue(childrenSum);
		}
		row.createCell(8).setCellValue(roomNightSum);
		row.createCell(9).setCellValue(getSubsidySum(this.orderId));
		Float tmp2 = getTemaiPercent(this.orderId);
		if(null != tmp2){
			row.createCell(10).setCellValue(tmp2);
		}
		row.createCell(11).setCellValue(getTemaiSubsidySum(this.orderId));
		
		row.createCell(12).setCellValue(salesPromotionAmountSum);
		row.createCell(13).setCellValue(couponAmountSum);
		row.createCell(14).setCellValue(turnoverAmountSum);
		row.createCell(15).setCellValue(grossMarginSum);
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
	public SubsidyMeta getSubsidy() {
		return subsidy;
	}
	
}
