package cn.hly.ordrpt.taks;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cn.hly.ordrpt.excel.WbUtil;
import cn.hly.ordrpt.model.OrdModel;
import cn.hly.ordrpt.model.RptModel;
import cn.hly.ordrpt.model.RuleModel;
import cn.hly.ordrpt.model.meta.Tuple;

public class OrdRptTask extends Thread{
	
	private String rulePath;//规则文件路径
	private String orderPath;//订单文件路径
	private String outputPath;//输出结果路径
	
	private int totalRow;//订单文件总行数
	private int curRow;//已处理的订单文件的行数
	
	/**
	 * 此任务是否结束标志
	 */
	private boolean taskFinished = false;
	
	/**
	 * 规则模型
	 */
	private Map<Long, RuleModel> ruleMap;
	
	/**
	 * 计算发生异常的订单
	 */
	private List<OrdModel> caledErrorModel;
	
	/**
	 * 任务执行结果
	 * item1执行是否成功的结果
	 * item2执行结果的消息
	 */
	private Tuple<Boolean, String> taskRunResult;
	
	/**
	 * 报表模型
	 */
	private RptModel rptModel;
	
	/**
	 * 有参构造
	 * @param rulePath 规则文件路径
	 * @param orderPath 订单文件路径
	 */
	public OrdRptTask(String rulePath, String orderPath, String outputPath) {
		this.rulePath = rulePath;
		this.orderPath = orderPath;
		this.outputPath = outputPath;
		rptModel = new RptModel();
		caledErrorModel = new ArrayList<>();
		taskRunResult = new Tuple<>(false);
	}
	
	
	
	
	@Override
	public void run() {
		try {
			this.invoke();
			taskRunResult.item1 = true;
			taskRunResult.item2 = "任务已执行成功，共处理" + this.totalRow + "条订单！";
		} catch (Exception e) {
			taskRunResult.item1 = false;
			taskRunResult.item2 = "任务执行失败，系统异常：\r\n" + e.getMessage();
			
			e.printStackTrace();
		}
		taskFinished = true;
	}
	
	/**
	 * 入口
	 * @param orderPath
	 */
	private void invoke() throws Exception{
		//1、初始化规则文件
		this.ruleMap = initRule(WbUtil.getWorkBook(this.rulePath));
		
		//2、创建Excel工作簿操作对象
		Workbook orderWb = WbUtil.getWorkBook(orderPath);
		Sheet sheet = orderWb.getSheetAt(0);
		
		//3、循环读取订单工作簿中的每一行
		int rowStart = sheet.getFirstRowNum();
		totalRow = sheet.getLastRowNum();
	    for (curRow = rowStart+1; curRow <= totalRow; curRow++) {
	       Row r = sheet.getRow(curRow);
	       if (r == null) {
	          continue;
	       }
	       
	       OrdModel ordModel = OrdModel.getOrdModel(r);
	       if(null != ordModel && null != ordModel.getOrderId() &&
	    		   ordModel.getGoodsId() != null && ordModel.getProductId() != null){
	    	   try {
	    		   this.filtrateAndCalculate(ordModel);//筛选并计算
	    	   } catch (Exception e) {
	    		   caledErrorModel.add(ordModel);//计算发生异常
	    		   throw new Exception(e);
	    	   }
	       }
	    }
	    
	    //4、计算完后输出
	    rptModel.write(outputPath);
	}
	
	
	/**
	 * 初始化规则文件
	 * @param rulePath
	 * @return
	 */
	private Map<Long, RuleModel> initRule(Workbook wb) throws Exception{
		Sheet sheet = wb.getSheetAt(0);
		int rowStart = sheet.getFirstRowNum();
	    int rowEnd = sheet.getLastRowNum();
	    Map<Long, RuleModel> result = new HashMap<>();
	    for (int rowNum = rowStart+1; rowNum <= rowEnd; rowNum++) {
	       Row r = sheet.getRow(rowNum);
	       if (r == null) {
	          continue;
	       }
	       
	       RuleModel ruleModel = RuleModel.getRuleModel(r);
	       if(null != ruleModel){
	    	   result.put(ruleModel.getGoodsId().data, ruleModel);
	       }
	    }
		
		return result;
	}
	
	/**
	 * 筛选
	 * @param ordModel
	 * @throws Exception 
	 */
	public void filtrateAndCalculate(OrdModel ordModel) throws Exception{
		RuleModel ruleModel = this.ruleMap.get(ordModel.getGoodsId().data);
		if(null != ruleModel){
			//1、筛选
			if(ordModel.getCreateTime().data.before(ruleModel.getSeckillDateRange().data.item1)
					&& ordModel.getCreateTime().data.after(ruleModel.getSeckillDateRange().data.item2)){
				return;//下单时间不在秒杀范围内
			}
			
			if(null == ordModel.getPaymentTime().data){
				return;//支付时间为空
			}
			
			if(null != ruleModel.getGroupRange().data){
				if(!ruleModel.getGroupRange().data.equals(ordModel.getVisitTime().data)){
					return;//出游日期不在团期内
				}
			}
			
			
			if(null != ruleModel.getProductId().data && 
					!ruleModel.getProductId().data.equals(ordModel.getProductId().data)){
				return ;//不是此产品下的商品
			}
			
			//2、计算
			this.rptModel.eat(ruleModel, ordModel);
		}
	}

	
	
	public int getTotalRow() {
		return totalRow;
	}

	public int getCurRow() {
		return curRow;
	}

	public boolean isTaskFinished() {
		return taskFinished;
	}

	public Tuple<Boolean, String> getTaskRunResult() {
		return taskRunResult;
	}

	public List<OrdModel> getCaledErrorModel() {
		return caledErrorModel;
	}
}
