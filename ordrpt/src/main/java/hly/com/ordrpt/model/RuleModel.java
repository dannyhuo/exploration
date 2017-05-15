package hly.com.ordrpt.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class RuleModel implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7626045397873518844L;
	
	/**产品ID*/
	private Tuple<Integer, Long> productId;
	/**商品ID*/
	private Tuple<Integer, Long> goodsId;
	/**秒杀价*/
	private Tuple<Integer, Long> seckillPrice;
	/**补贴单价*/
	private Tuple<Integer, Double> subsidy;
	/**团期，出游日期*/
	private Tuple<Integer, Date> groupRange;
	/**库存*/
	private Tuple<Integer, Integer> inventory;
	/**补贴方式，1：按间夜补，2：按成人补，3：按成人和儿童补*/
	private Tuple<Integer, Short> subsidyType;
	/**补贴占比*/
	private Tuple<Integer, Float> subsidyPercent;
	/**秒杀时间范围*/
	private Tuple<Integer, Tuple<Date, Date>> seckillDateRange;
	/**类型，线路、门票*/
	private Tuple<Integer, String> goodsType;
	/**日期格式化工具*/
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public RuleModel() {
		/**产品ID*/
		productId = new Tuple<>(9);
		/**商品ID*/
		goodsId = new Tuple<>(10);
		/**秒杀价*/
		seckillPrice = new Tuple<>(17);
		/**补贴单价*/
		subsidy = new Tuple<>(20);
		/**团期，出游日期*/
		groupRange = new Tuple<>(23);
		/**库存*/
		inventory = new Tuple<>(18);
		/**补贴方式，1：按间夜补，2：按成人补，3：按成人和儿童补, 4按儿童补*/
		subsidyType = new Tuple<>(25);
		/**补贴占比*/
		subsidyPercent = new Tuple<>(21);
		/**秒杀时间范围*/
		seckillDateRange = new Tuple<>(0);
		/**类型，线路、门票*/
		goodsType = new Tuple<>(5);
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
	    Cell cell = row.getCell(ruleModel.getGoodsId().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGoodsId().item2 = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getProductId().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getProductId().item2 = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getGroupRange().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGroupRange().item2 = cell.getDateCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getInventory().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getInventory().item2 = (int) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSeckillPrice().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSeckillPrice().item2 = (long) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidy().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSubsidy().item2 = cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidyType().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getSubsidyType().item2 = (short) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSubsidyPercent().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	/*String percentStr = cell.getStringCellValue();
	    	if(null != percentStr && !percentStr.trim().equals("")){
	    		try {
		    		ruleModel.getSubsidyPercent().item2 = Float.parseFloat(percentStr.replace("%", "").trim());
				} catch (Exception e) {
					throw new Exception("特卖补贴占比格式错误，请检查规则文件中的特卖补贴占比。如：30%");
				}
	    	}*/
	    	
	    	ruleModel.getSubsidyPercent().item2 = (float) cell.getNumericCellValue();
	    }
	    
	    cell = row.getCell(ruleModel.getSeckillDateRange().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	String range = cell.toString();
	    	String[] rangeArr = range.split("-");
	    	if(rangeArr.length == 2){
	    		try {
	    			Tuple<Date, Date> rangeTuple = new Tuple<>();
	    			rangeTuple.item1 = format.parse(rangeArr[0]);
	    			rangeTuple.item2 = format.parse(rangeArr[1]);
	    			ruleModel.getSeckillDateRange().item2 = rangeTuple;
				} catch (ParseException e) {
					throw new Exception("秒杀时间格式化错误，请坚持规则文件中的秒杀时间格式如：yyyy/MM/dd HH:mm:ss-yyyy/MM/dd HH:mm:ss。");
				}
	    	}
	    }
	    
	    cell = row.getCell(ruleModel.getGoodsType().item1, Row.RETURN_BLANK_AS_NULL);
	    if(null != cell){
	    	ruleModel.getGoodsType().item2 = cell.getStringCellValue();
	    }
	    
	    return ruleModel;
	}


	public Tuple<Integer, Long> getProductId() {
		return productId;
	}

	public void setProductId(Tuple<Integer, Long> productId) {
		this.productId = productId;
	}

	public Tuple<Integer, Long> getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Tuple<Integer, Long> goodsId) {
		this.goodsId = goodsId;
	}

	public Tuple<Integer, Long> getSeckillPrice() {
		return seckillPrice;
	}

	public void setSeckillPrice(Tuple<Integer, Long> seckillPrice) {
		this.seckillPrice = seckillPrice;
	}

	public Tuple<Integer, Double> getSubsidy() {
		return subsidy;
	}

	public void setSubsidy(Tuple<Integer, Double> subsidy) {
		this.subsidy = subsidy;
	}

	public Tuple<Integer, Date> getGroupRange() {
		return groupRange;
	}

	public void setGroupRange(Tuple<Integer, Date> groupRange) {
		this.groupRange = groupRange;
	}

	public Tuple<Integer, Integer> getInventory() {
		return inventory;
	}

	public void setInventory(Tuple<Integer, Integer> inventory) {
		this.inventory = inventory;
	}

	public Tuple<Integer, Short> getSubsidyType() {
		return subsidyType;
	}

	public void setSubsidyType(Tuple<Integer, Short> subsidyType) {
		this.subsidyType = subsidyType;
	}

	public Tuple<Integer, Float> getSubsidyPercent() {
		return subsidyPercent;
	}

	public void setSubsidyPercent(Tuple<Integer, Float> subsidyPercent) {
		this.subsidyPercent = subsidyPercent;
	}

	public Tuple<Integer, Tuple<Date, Date>> getSeckillDateRange() {
		return seckillDateRange;
	}

	public void setSeckillDateRange(Tuple<Integer, Tuple<Date, Date>> seckillDateRange) {
		this.seckillDateRange = seckillDateRange;
	}

	public Tuple<Integer, String> getGoodsType() {
		return goodsType;
	}

	public void setGoodsType(Tuple<Integer, String> goodsType) {
		this.goodsType = goodsType;
	}
	
}
