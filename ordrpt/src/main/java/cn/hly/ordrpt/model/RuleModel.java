package cn.hly.ordrpt.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import cn.hly.ordrpt.model.meta.CellMeta;
import cn.hly.ordrpt.model.meta.Tuple;

public class RuleModel {
	
	/**产品ID*/
	private CellMeta<Integer, Long> productId;
	/**商品ID*/
	private CellMeta<Integer, Long> goodsId;
	/**秒杀价*/
	private CellMeta<Integer, Long> seckillPrice;
	/**补贴单价*/
	private CellMeta<Integer, Double> subsidy;
	/**团期，出游日期*/
	private CellMeta<Integer, Date> groupRange;
	/**库存*/
	private CellMeta<Integer, Integer> inventory;
	/**补贴方式，1：按间夜补，2：按成人补，3：按成人和儿童补*/
	private CellMeta<Integer, Short> subsidyType;
	/**补贴占比*/
	private CellMeta<Integer, Float> subsidyPercent;
	/**秒杀时间范围*/
	private CellMeta<Integer, Tuple<Date, Date>> seckillDateRange;
	/**类型，线路、门票*/
	private CellMeta<Integer, String> goodsType;
	/**供应商打包间数*/
	private CellMeta<Integer, Integer> supplierPackageRoomCount;
	/**日期格式化工具*/
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public RuleModel() {
		/**产品ID*/
		productId = new CellMeta<>(9);
		
		/**商品ID*/
		goodsId = new CellMeta<>(10);
		
		/**秒杀价*/
		seckillPrice = new CellMeta<>(17);
		
		/**补贴单价*/
		subsidy = new CellMeta<>(20);
		
		/**团期，出游日期*/
		groupRange = new CellMeta<>(23);
		
		/**库存*/
		inventory = new CellMeta<>(18);
		
		/**补贴方式，1：按间夜补，2：按成人补，3：按成人和儿童补, 4按儿童补*/
		subsidyType = new CellMeta<>(25);
		
		/**补贴占比*/
		subsidyPercent = new CellMeta<>(21);
		
		/**秒杀时间范围*/
		seckillDateRange = new CellMeta<>(0);
		
		/**类型，线路、门票*/
		goodsType = new CellMeta<>(5);
		/**供应商打包间数*/
		supplierPackageRoomCount = new CellMeta<>(26);
		
	}
	
	
	
	/**
	 * 将规则文件中的行转换成对应的RuleModel
	 * @param row
	 * @return
	 */
	public static RuleModel getRuleModel(Row row) throws Exception{
		if(null == row){
			return null;
		}
		
		RuleModel ruleModel = new RuleModel();
	    Cell cell = row.getCell(ruleModel.getGoodsId().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGoodsId().data = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getProductId().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getProductId().data = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getGroupRange().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGroupRange().data = cell.getDateCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getInventory().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getInventory().data = (int) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSeckillPrice().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSeckillPrice().data = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidy().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSubsidy().data = cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidyType().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSubsidyType().data = (short) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidyPercent().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSubsidyPercent().data = (float) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSeckillDateRange().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	String range = cell.toString();
	    	String[] rangeArr = range.split("-");
	    	if(rangeArr.length == 2){
	    		try {
	    			Tuple<Date, Date> rangeTuple = new Tuple<>();
	    			rangeTuple.item1 = format.parse(rangeArr[0]);
	    			rangeTuple.item2 = format.parse(rangeArr[1]);
	    			ruleModel.getSeckillDateRange().data = rangeTuple;
				} catch (ParseException e) {
					throw new Exception("秒杀时间格式化错误，请坚持规则文件中的秒杀时间格式如：yyyy/MM/dd HH:mm:ss-yyyy/MM/dd HH:mm:ss。");
				}
	    	}
	    }
	    
	    cell = row.getCell(ruleModel.getGoodsType().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGoodsType().data = cell.getStringCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSupplierPackageRoomCount().index, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSupplierPackageRoomCount().data = (int)cell.getNumericCellValue();
	    }
	    
	    return ruleModel;
	}


	public CellMeta<Integer, Long> getProductId() {
		return productId;
	}

	public void setProductId(CellMeta<Integer, Long> productId) {
		this.productId = productId;
	}

	public CellMeta<Integer, Long> getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(CellMeta<Integer, Long> goodsId) {
		this.goodsId = goodsId;
	}

	public CellMeta<Integer, Long> getSeckillPrice() {
		return seckillPrice;
	}

	public void setSeckillPrice(CellMeta<Integer, Long> seckillPrice) {
		this.seckillPrice = seckillPrice;
	}

	public CellMeta<Integer, Double> getSubsidy() {
		return subsidy;
	}

	public void setSubsidy(CellMeta<Integer, Double> subsidy) {
		this.subsidy = subsidy;
	}

	public CellMeta<Integer, Date> getGroupRange() {
		return groupRange;
	}

	public void setGroupRange(CellMeta<Integer, Date> groupRange) {
		this.groupRange = groupRange;
	}

	public CellMeta<Integer, Integer> getInventory() {
		return inventory;
	}

	public void setInventory(CellMeta<Integer, Integer> inventory) {
		this.inventory = inventory;
	}

	public CellMeta<Integer, Short> getSubsidyType() {
		return subsidyType;
	}

	public void setSubsidyType(CellMeta<Integer, Short> subsidyType) {
		this.subsidyType = subsidyType;
	}

	public CellMeta<Integer, Float> getSubsidyPercent() {
		return subsidyPercent;
	}

	public void setSubsidyPercent(CellMeta<Integer, Float> subsidyPercent) {
		this.subsidyPercent = subsidyPercent;
	}

	public CellMeta<Integer, Tuple<Date, Date>> getSeckillDateRange() {
		return seckillDateRange;
	}

	public void setSeckillDateRange(CellMeta<Integer, Tuple<Date, Date>> seckillDateRange) {
		this.seckillDateRange = seckillDateRange;
	}

	public CellMeta<Integer, String> getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(CellMeta<Integer, String> goodsType) {
		this.goodsType = goodsType;
	}

	public CellMeta<Integer, Integer> getSupplierPackageRoomCount() {
		return supplierPackageRoomCount;
	}

	public void setSupplierPackageRoomCount(CellMeta<Integer, Integer> supplierPackageRoomCount) {
		this.supplierPackageRoomCount = supplierPackageRoomCount;
	}
	
	

}
