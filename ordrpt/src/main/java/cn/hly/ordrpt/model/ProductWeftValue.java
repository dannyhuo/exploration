package cn.hly.ordrpt.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;

import cn.hly.ordrpt.model.meta.SubsidyMeta;
import hly.com.ordrpt.model.Tuple;

/**
 * 产品纬度值类型
 * @author danny
 *
 */
public class ProductWeftValue extends BaseValue{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3603021865534759914L;
	
	//产品ID
	private Long productId;
	//事业部
	private String buName;
	//打包商品ID
	private Set<Long> goodsIds = new HashSet<>();
	//订单ID
	private Set<Long> orderIds = new HashSet<>();
	//补贴
	private SubsidyMeta subsidy = new SubsidyMeta();
	
	/**
	 * 分订单下成人
	 * key:订单ID， item1:成人数， item2:有成人的count
	 */
	private Map<Long, Tuple<Integer, Integer>> adultByOrdMap = new HashMap<>();
	/**
	 * 分订单下儿童
	 * key:订单ID， item1:儿童数， item2:有儿童的count
	 */
	private Map<Long, Tuple<Integer, Integer>> childrenByOrdMap = new HashMap<>();
	
	
	public void eat(OrdModel ord, RuleModel rule){
		this.productId = ord.getProductId().data;
		this.buName = ord.getBuName().data;
		this.goodsIds.add(ord.getGoodsId().data);
		this.orderIds.add(ord.getOrderId().data);
		
		this.addSubsidyAmount(ord.getProductId().data, calSubsidy(ord, rule), rule.getSubsidyPercent().data);
		
		super.eat(ord, rule);
	}
	
	
	/**
	 * 写入行
	 * @param row
	 */
	public void write(Row row){
		row.createCell(0).setCellValue(buName);
		row.createCell(1).setCellValue(productId);
		row.createCell(2).setCellValue(getGoodsIdsStr());
		row.createCell(3).setCellValue(getOrderIdsStr());
		if("线路".equals(goodsType)){
			row.createCell(4).setCellValue(getTktAdultSumAvg());
			row.createCell(5).setCellValue(getTktChildrenSumAvg());
		}else{
			row.createCell(4).setCellValue(auditSum);
			row.createCell(5).setCellValue(childrenSum);
		}
		row.createCell(6).setCellValue(roomNightSum);
		
		row.createCell(7).setCellValue(getSubsidySum(this.productId));
		Float tmp2 = getCalPercent(this.productId);
		if(null != tmp2){
			row.createCell(8).setCellValue(tmp2);
		}
		row.createCell(9).setCellValue(getTemaiSubsidySum(this.productId));
		row.createCell(10).setCellValue(salesPromotionAmountSum);
		row.createCell(11).setCellValue(couponAmountSum);
		row.createCell(12).setCellValue(turnoverAmountSum);
		row.createCell(13).setCellValue(grossMarginSum);
	}
	
	
	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getBuName() {
		return buName;
	}

	public void setBuName(String buName) {
		this.buName = buName;
	}

	public Set<Long> getGoodsIds() {
		return goodsIds;
	}

	public void setGoodsIds(Set<Long> goodsIds) {
		this.goodsIds = goodsIds;
	}

	public Set<Long> getOrderIds() {
		return orderIds;
	}

	public void setOrderIds(Set<Long> orderIds) {
		this.orderIds = orderIds;
	}

	public SubsidyMeta getSubsidy() {
		return subsidy;
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

	public void putAdult(Long orderId, Integer adultCount){
		if(null == adultCount || adultCount.intValue() < 1){
			return;
		}
		Tuple<Integer, Integer> tuple = null;
		if(null == (tuple =adultByOrdMap.get(orderId))){
			tuple = new Tuple<>(adultCount, 1);
			adultByOrdMap.put(orderId, tuple);
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
		Set<Entry<Long, Tuple<Integer, Integer>>> entries = adultByOrdMap.entrySet();
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
		if(null == (tuple =childrenByOrdMap.get(orderId))){
			tuple = new Tuple<>(children, 1);
			childrenByOrdMap.put(orderId, tuple);
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
		Set<Entry<Long, Tuple<Integer, Integer>>> entries = childrenByOrdMap.entrySet();
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
}
