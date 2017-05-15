package hly.com.ordrpt.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;

public class CalValueModel2 extends BaseCalValueModel{
	private Set<Long> orderIds = new HashSet<>();
	//事业部
	private String buName;
	//分销渠道ID
	private Integer salesChannelId;
	//是否是CPSID
	private String isCpsid;
	//打包产品ID
	private Long productId;
	//打包商品ID
	private Set<Long> goodsIds = new HashSet<>();
	
	//类型，门票/线路
	private String goodsType;
	
	private Map<Long, Tuple<Integer, Integer>> adultMap = new HashMap<>();
	private Map<Long, Tuple<Integer, Integer>> childrenMap = new HashMap<>();
	
	//特卖会补贴
	private Map<Long, Tuple<Double, Float>> temaiSubsidyAmounts = new HashMap<>();
	
	public void putAdult(Long orderId, Integer adultCount){
		if(null == adultCount || adultCount.intValue() < 1){
			return;
		}
		Tuple<Integer, Integer> tuple = null;
		if(null == (tuple =adultMap.get(orderId))){
			tuple = new Tuple<>(adultCount, 1);
			adultMap.put(orderId, tuple);
			return;
		}
		tuple.item1 += adultCount;
		tuple.item2++;
	}
	
	/**
	 * 获取门票订单成人数avg的和
	 * @return
	 */
	public float getTktAdultSumAvg(){
		/*if(!"线路".equals(goodsType)){
			return 0f;
		}*/
		float sumAvg = 0f;
		Set<Entry<Long, Tuple<Integer, Integer>>> entries = adultMap.entrySet();
		Iterator<Entry<Long, Tuple<Integer, Integer>>> iter = entries.iterator();
		while(iter.hasNext()){
			Tuple<Integer, Integer> v = iter.next().getValue();
			if(v.item1 != null && v.item1.intValue() > 0
					&& v.item2 != null && v.item2.intValue() > 0){
				float avg = v.item1.floatValue()/v.item2.floatValue();
				sumAvg += avg;
			}
		}
		return sumAvg;
	}
	
	public void putChildren(Long orderId, Integer children){
		if(null == children || children.intValue() < 1){
			return;
		}
		Tuple<Integer, Integer> tuple = null;
		if(null == (tuple =childrenMap.get(orderId))){
			tuple = new Tuple<>(children, 1);
			childrenMap.put(orderId, tuple);
			return;
		}
		tuple.item1 += children;
		tuple.item2++;
	}
	/**
	 * 获取门票儿童的订单儿童数avg的和
	 * @return
	 */
	public float getTktChildrenSumAvg(){
		/*if(!"线路".equals(goodsType)){
			return 0f;
		}*/
		float sumAvg = 0f;
		Set<Entry<Long, Tuple<Integer, Integer>>> entries = childrenMap.entrySet();
		Iterator<Entry<Long, Tuple<Integer, Integer>>> iter = entries.iterator();
		while(iter.hasNext()){
			Tuple<Integer, Integer> v = iter.next().getValue();
			if(v.item1 != null && v.item1.intValue() > 0
					&& v.item2 != null && v.item2.intValue() > 0){
				float avg = v.item1.floatValue()/v.item2.floatValue();
				sumAvg += avg;
			}
		}
		return sumAvg;
	}
	
	/**
	 * 写入excel工作簿
	 * @param wb
	 * @return
	 */
	public void writeToSheet(Row row, BaseCalValueModel total){
		row.createCell(0).setCellValue(buName);
		row.createCell(1).setCellValue(productId);
		row.createCell(2).setCellValue(getGoodsIdsStr());
		row.createCell(3).setCellValue(getOrderIdsStr());
		
		Float audit = null;
		Float children = null;
		if("线路".equals(goodsType)){
			audit = getTktAdultSumAvg();
			children = getTktChildrenSumAvg();
		}else{
			audit = (float) auditSum;
			children = (float) childrenSum;
		}
		row.createCell(4).setCellValue(audit);
		row.createCell(5).setCellValue(children);
		
		row.createCell(6).setCellValue(roomNightSum);
		row.createCell(7).setCellValue(subsidyAmountSum);
		
		Tuple<Double, Double> temaiSubsidy = getTemaiSubsidyAmountByProduct();
		double totalTemaiSubsidy = 0;
		if(null != temaiSubsidy){
			if(temaiSubsidy.item1 != null){
				row.createCell(9).setCellValue(temaiSubsidy.item1);
				totalTemaiSubsidy += temaiSubsidy.item1;
			}
			if(temaiSubsidy.item2 != null){
				row.createCell(8).setCellValue(temaiSubsidy.item2);
			}
		}
		row.createCell(10).setCellValue(salesPromotionAmountSum);
		row.createCell(11).setCellValue(couponAmountSum);
		row.createCell(12).setCellValue(turnoverAmountSum);
		row.createCell(13).setCellValue(grossMarginSum);
		
		total.addRoomNight(roomNightSum);
		total.addSubsidyAmount(subsidyAmountSum);
		total.addTemaiSubsidyAmountSum(totalTemaiSubsidy);
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
		
		sb.append(",goodsIds:");
		sb.append(getGoodsIdsStr());
		
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
	
	public Set<Long> getOrderIds() {
		return orderIds;
	}
	
	public String getOrderIdsStr() {
		Iterator<Long> iter = orderIds.iterator();
		StringBuilder sb = new StringBuilder();
		while(iter.hasNext()){
			sb.append(iter.next());
			sb.append(";");
		}
		sb.substring(0, sb.length() - 1);
		return sb.toString();
	}

	public void addOrderId(Long orderId) {
		this.orderIds.add(orderId);
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
	public Set<Long> getGoodsIds() {
		return goodsIds;
	}
	public void addGoodsId(Long goodsId) {
		this.goodsIds.add(goodsId);
	}
	
	public String getGoodsIdsStr() {
		Iterator<Long> iter = goodsIds.iterator();
		StringBuilder sb = new StringBuilder();
		while(iter.hasNext()){
			sb.append(iter.next());
			sb.append(";");
		}
		sb.substring(0, sb.length() - 1);
		return sb.toString();
	}

	public String getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(String goodsType) {
		this.goodsType = goodsType;
	}

	public Map<Long, Tuple<Double, Float>> getTemaiSubsidyAmounts() {
		return temaiSubsidyAmounts;
	}

	public void setTemaiSubsidyAmounts(Map<Long, Tuple<Double, Float>> temaiSubsidyAmounts) {
		this.temaiSubsidyAmounts = temaiSubsidyAmounts;
	}
	
	public void putTemaiSubsidyAmount(Long goodsId, Tuple<Double, Float> subsideyAmount){
		if(null != goodsId && null != subsideyAmount){
			this.temaiSubsidyAmounts.put(goodsId, subsideyAmount);
		}
	}
	
	/**
	 * 获取按产品纬度计算出的特卖补贴的钱及特卖的占比
	 * item1 特卖补贴
	 * item2 特卖占比
	 * @return
	 */
	public Tuple<Double, Double> getTemaiSubsidyAmountByProduct(){
		Tuple<Double, Double> result = new Tuple<>();
		Double temaiSubsidy = null;
		if(null != temaiSubsidyAmounts && temaiSubsidyAmounts.size() > 0){
			Iterator<Entry<Long, Tuple<Double, Float>>> iter = temaiSubsidyAmounts.entrySet().iterator();
			while(iter.hasNext()){
				Tuple<Double, Float> entryValue = iter.next().getValue();
				if(null != entryValue.item2){
					temaiSubsidy = entryValue.item1 * entryValue.item2;
				}else{
					temaiSubsidy = entryValue.item1; 
				}
			}
		}
		if(null != temaiSubsidy && subsidyAmountSum > 0){
			result.item2 = temaiSubsidy/subsidyAmountSum;
		}
		result.item1 = temaiSubsidy;
		return result;
	}
}
