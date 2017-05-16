package cn.hly.ordrpt.model.meta;

import java.util.HashMap;
import java.util.Map;

public class SubsidyMeta {
	
	/**
	 * 补贴金额
	 * key:纬度ID
	 * value:补贴金额
	 */
	private Map<Long, Double> subsidy = new HashMap<>();
	
	/**
	 * 特卖补占比
	 * key:纬度ID
	 * value:补贴占比
	 */
	private Map<Long, Float> temaiPercents = new HashMap<>();
	
	/**
	 * 特卖补贴金额
	 * key:纬度ID
	 * value:补贴金额
	 */
	private Map<Long, Double> temaiSubsidy = new HashMap<>();
	
	/**
	 * 
	 * @param key	纬度
	 * @param percent	特卖补贴占比
	 * @param subsidy	补贴金额
	 */
	public void eatSubsidy(Long key, Float percent, Double subsidy){
		double dsubsidy = 0;
		if(null == key || null == subsidy || 
				(dsubsidy = subsidy.doubleValue()) < 1){
			return ;
		}
		
		double fpercent = 0;
		if(null == percent){
			fpercent = 1;//为空表示全补
		}else{
			fpercent = percent.doubleValue();
		}
		
		double temaiSubsidy = dsubsidy * fpercent;
		
		this.addPercent(key, percent);
		this.addSubsidy(key, subsidy);
		this.addTemaiSubsidy(key, temaiSubsidy);
	}
	
	
	public Double getSubsidy(Long weft){
		return subsidy.get(weft);
	}
	
	public Double getTemaiSubsidy(Long weft){
		return temaiSubsidy.get(weft);
	}

	public Float getTemaiPercent(Long weft){
		return temaiPercents.get(weft);
	}
	
	/**
	 * 添加补贴
	 * @param key 纬度
	 * @param value 补贴金额
	 * @return －1:参数不合法， 0:新添加，1:累加
	 */
	private int addSubsidy(Long key, Double value){
		double v = 0;
		if(null == key || null == value || 
				(v = value.doubleValue()) < 1){
			return -1;
		}
		Double cv = subsidy.get(key);
		if(null == cv){
			subsidy.put(key, value);
			return 0;
		}else{
			subsidy.put(key, cv.doubleValue() + v);
			return 1;
		}
	}
	
	/**
	 * 添加补贴
	 * @param key 纬度
	 * @param value 补贴金额
	 * @return －1:参数不合法， 0:新添加，1:累加
	 */
	private int addTemaiSubsidy(Long key, Double value){
		Double v = 0d;
		if(null == key || null == value || 
				(v = value.doubleValue()) <= 0){
			return -1;
		}
		Double cv = temaiSubsidy.get(key);
		if(null == cv){
			temaiSubsidy.put(key, value);
			return 0;
		}else{
			temaiSubsidy.put(key, cv.doubleValue() + v);
			return 1;
		}
	}
	
	/**
	 * 添加占比
	 * @param key
	 * @param percent
	 * @return
	 */
	private int addPercent(Long key, Float percent){
		if(null == key || null == percent){
			return -1;
		}
		
		Float cv = temaiPercents.get(key);
		if(null == cv){
			temaiPercents.put(key, percent);
			return 0;
		}else{
			temaiPercents.put(key, cv.floatValue() + percent.floatValue());
			return 1;
		}
	}

}
