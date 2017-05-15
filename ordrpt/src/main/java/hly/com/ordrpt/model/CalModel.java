package hly.com.ordrpt.model;

import java.util.HashMap;
import java.util.Map;

public class CalModel {
	
	/**
	 * 按订单号分组计算
	 */
	private Map<Long, CalValueModel> groupByOrderId = null;
	
	
	private Map<Long, CalValueModel2> groupByProductId = null;
	
	
	public CalModel() {
		groupByOrderId = new HashMap<>();
		groupByProductId = new HashMap<>();
	}
	
	/**
	 * 计算
	 * @param ruleModel
	 * @param ordModel
	 * @throws Exception
	 */
	public void calModel(RuleModel ruleModel, OrdModel ordModel) throws Exception{
		this.calculate(ruleModel, ordModel);
		
		this.calculate2(ruleModel, ordModel);
	}
	
	/**
	 * sheet2计算
	 * @param ruleModel
	 * @param ordModel
	 */
	private void calculate2(RuleModel ruleModel, OrdModel ordModel){
		CalValueModel2 curCalValue = null;
		if(null == (curCalValue = groupByProductId.get(ordModel.getProductId().item2))){
			curCalValue = new CalValueModel2();
			curCalValue.setProductId(ordModel.getProductId().item2);
			curCalValue.setBuName(ordModel.getBuName().item2);
			curCalValue.setSalesChannelId(ordModel.getSaleChannelId().item2);
			curCalValue.setIsCpsid(ordModel.getCpsId().item2);
			curCalValue.setGoodsType(ruleModel.getGoodsType().item2);
			groupByProductId.put(ordModel.getProductId().item2, curCalValue);
		}
		
		curCalValue.addGoodsId(ordModel.getGoodsId().item2);
		curCalValue.addOrderId(ordModel.getOrderId().item2);
		curCalValue.putAdult(ordModel.getOrderId().item2, ordModel.getAdult().item2);
		curCalValue.putChildren(ordModel.getOrderId().item2, ordModel.getChildren().item2);
		
		//计算数据
		calCommData(curCalValue, ruleModel, ordModel);
	}
	
	/**
	 * sheet1计算
	 * @param ruleModel
	 * @param ordModel
	 */
	private void calculate(RuleModel ruleModel, OrdModel ordModel){
		CalValueModel curCalValue = null;
		if(null == (curCalValue = groupByOrderId.get(ordModel.getOrderId().item2))){
			//基础数据部分
			curCalValue = new CalValueModel();
			curCalValue.setGoodsId(ordModel.getGoodsId().item2);
			curCalValue.setProductId(ordModel.getProductId().item2);
			curCalValue.setBuName(ordModel.getBuName().item2);
			curCalValue.setSalesChannelId(ordModel.getSaleChannelId().item2);
			curCalValue.setIsCpsid(ordModel.getCpsId().item2);
			curCalValue.setOrderId(ordModel.getOrderId().item2);
			curCalValue.setGoodsType(ruleModel.getGoodsType().item2);
			curCalValue.setTemaiPercent(ruleModel.getSubsidyPercent().item2);
			groupByOrderId.put(ordModel.getOrderId().item2, curCalValue);
		}
		
		//计算数据
		calCommData(curCalValue, ruleModel, ordModel);
	}
	
	/**
	 * 公用计算部分
	 * @param curCalValue
	 * @param ruleModel
	 * @param ordModel
	 */
	private void calCommData(BaseCalValueModel curCalValue, RuleModel ruleModel, OrdModel ordModel){
		//1、计算补贴
		if(Canstant.SUBSIDY_TYPE.BY_AUDIT.getValue() == 
				ruleModel.getSubsidyType().item2.shortValue()){
			Integer adult = ordModel.getAdult().item2;
			if(adult != null && adult.intValue() > 0){
				double subsidy = ruleModel.getSubsidy().item2.doubleValue() * adult.doubleValue();
				curCalValue.addSubsidyAmount(subsidy);
			}
		}else if(Canstant.SUBSIDY_TYPE.BY_AUDIT_AND_CHILDREN.getValue() == 
				ruleModel.getSubsidyType().item2.shortValue()){
			Integer adult = ordModel.getAdult().item2;
			Integer children = ordModel.getChildren().item2;
			double subsidy = 0;
			if(adult != null && adult.intValue() > 0){
				subsidy += ruleModel.getSubsidy().item2.doubleValue() * adult.doubleValue();
			}
			if(children != null && children.intValue() > 0){
				subsidy += ruleModel.getSubsidy().item2.doubleValue() * children.doubleValue();
			}
			curCalValue.addSubsidyAmount(subsidy);
		}else if(Canstant.SUBSIDY_TYPE.BY_ROOM_NIGHT.getValue() == 
				ruleModel.getSubsidyType().item2.shortValue()){
			Integer roomNight = ordModel.getRoomNight().item2;
			if(roomNight != null && roomNight.intValue() > 0){
				double subsidy = ruleModel.getSubsidy().item2.doubleValue() * roomNight.doubleValue();
				curCalValue.addSubsidyAmount(subsidy);
			}
		}else if(Canstant.SUBSIDY_TYPE.BY_CHILDREN.getValue() == 
				ruleModel.getSubsidyType().item2.shortValue()){
			Integer children = ordModel.getChildren().item2;
			if(children != null && children.intValue() > 0){
				double subsidy = ruleModel.getSubsidy().item2.doubleValue() * children.doubleValue();
				curCalValue.addSubsidyAmount(subsidy);
			}
		}
		
		//2、算成人与儿童数
		curCalValue.addAudit(ordModel.getAdult().item2);
		curCalValue.addChildren(ordModel.getChildren().item2);
		
		//3、计算间夜数
		curCalValue.addRoomNight(ordModel.getRoomNight().item2);
		
		//4、计算促销金额
		curCalValue.addSalesPromotionAmount(ordModel.getSalesPromotionAmount().item2);
		
		//5、计算优惠券金额
		curCalValue.addCouponAmount(ordModel.getCouponAmount().item2);
		
		//6、计算营业额
		curCalValue.addTurnoverAmountSum(ordModel.getTurnover().item2);
		
		//7、计算毛利
		curCalValue.addGrossMarginSum(ordModel.getGrossMargin().item2);
	}

	public Map<Long, CalValueModel> getGroupByOrderId() {
		return groupByOrderId;
	}

	public void setGroupByOrderId(Map<Long, CalValueModel> groupByOrderId) {
		this.groupByOrderId = groupByOrderId;
	}

	public Map<Long, CalValueModel2> getGroupByProductId() {
		return groupByProductId;
	}

	public void setGroupByProductId(Map<Long, CalValueModel2> groupByProductId) {
		this.groupByProductId = groupByProductId;
	}
	
	
}
