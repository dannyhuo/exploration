package hly.com.ordrpt.task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import hly.com.ordrpt.model.BaseCalValueModel;
import hly.com.ordrpt.model.CalModel;
import hly.com.ordrpt.model.CalValueModel;
import hly.com.ordrpt.model.CalValueModel2;
import hly.com.ordrpt.model.OrdModel;
import hly.com.ordrpt.model.RuleModel;
import hly.com.ordrpt.model.Tuple;

public class OrderCalTask extends Thread{
	
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
	 * 计算模型
	 */
	private CalModel callModel;
	
	/**
	 * Excel操作对象
	 */
	private XSSFWorkbook xssfWb;
	
	/**
	 * 有参构造
	 * @param rulePath 规则文件路径
	 * @param orderPath 订单文件路径
	 */
	public OrderCalTask(String rulePath, String orderPath, String outputPath) {
		this.rulePath = rulePath;
		this.orderPath = orderPath;
		this.outputPath = outputPath;
		callModel = new CalModel();
		caledErrorModel = new ArrayList<>();
		taskRunResult = new Tuple<>(false);
	}
	
	/**
	 * 读取WorkBook
	 * @param excelFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Workbook getWorkBook(String excelFile) 
			throws FileNotFoundException, IOException{
		Workbook book = null;
		FileInputStream fis = null;
		FileInputStream fis2 = null;
        try {
        	fis = new FileInputStream(excelFile);
            book = new XSSFWorkbook(fis);
        } catch (Exception ex) {
        	fis2 = new FileInputStream(excelFile);
            book = new HSSFWorkbook(fis2);
        } finally {
        	if(null != fis){
        		fis.close();
        	}
			if(null != fis2){
				fis2.close();
			}
		}
        return book;
	}
	
	/**
	 * 初始化规则文件
	 * @param rulePath
	 * @return
	 */
	private static Map<Long, RuleModel> initRule(String rulePath) throws Exception{
		Workbook ruleWb = null;
		try {
			ruleWb = getWorkBook(rulePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Sheet sheet = ruleWb.getSheetAt(0);
		
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
	    	   result.put(ruleModel.getGoodsId().item2, ruleModel);
	       }
	    }
		
		return result;
	}
	
	@Override
	public void run() {
		try {
			this.startRuning();
			taskRunResult.item1 = true;
			taskRunResult.item2 = "任务已执行成功，共处理" + this.totalRow + "条订单！";
		} catch (Exception e) {
			taskRunResult.item1 = false;
			taskRunResult.item2 = "任务已执行失败，系统异常：" + e.getMessage();
			e.printStackTrace();
		}
		taskFinished = true;
	}
	
	/**
	 * 读取订单并计算
	 * @param orderPath
	 */
	public void startRuning() throws Exception{
		//1、初始化规则文件
		this.ruleMap = initRule(this.rulePath);
		
		//2、创建Excel工作簿操作对象
		Workbook orderWb = null;
		orderWb = getWorkBook(orderPath);
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
	    		   this.calculate(ordModel);//计算
	    	   } catch (Exception e) {
	    		   caledErrorModel.add(ordModel);//计算发生异常
	    		   throw new Exception(e);
	    	   }
	       }
	    }
	    
	    //4、计算完后输出
	    this.outputResult();
	}
	
	
	/**
	 * 计算
	 * @param ordModel
	 * @throws Exception 
	 */
	public void calculate(OrdModel ordModel) throws Exception{
		RuleModel ruleModel = this.ruleMap.get(ordModel.getGoodsId().item2);
		if(null != ruleModel){
			//1、筛选
			if(ordModel.getCreateTime().item2.before(ruleModel.getSeckillDateRange().item2.item1)
					&& ordModel.getCreateTime().item2.after(ruleModel.getSeckillDateRange().item2.item2)){
				return;//下单时间不在秒杀范围内
			}
			
			if(null == ordModel.getPaymentTime().item2){
				return;//支付时间为空
			}
			
			if(null != ruleModel.getGroupRange().item2){
				if(!ruleModel.getGroupRange().item2.equals(ordModel.getVisitTime().item2)){
					return;//出游日期不在团期内
				}
			}
			
			if(!ruleModel.getProductId().item2.equals(ordModel.getProductId().item2)){
				return ;//不是此产品下的商品
			}
			
			//2、计算
			callModel.calModel(ruleModel, ordModel);
		}
	}

	/**
	 * 结果输出
	 */
	private boolean outputResult() throws Exception{
		boolean result = true;
		FileOutputStream fos = null;
		try {
			xssfWb = new XSSFWorkbook();
			Sheet sheet = xssfWb.createSheet("结果1－按订单分组");
			Row header = sheet.createRow(0);
			createHeader1(header);//表头
			
			Sheet sheet2 = xssfWb.createSheet("结果2－按产品分组");
			Row header2 = sheet2.createRow(0);
			createHeader2(header2);//表头
			
			Map<Long, CalValueModel> calResult = callModel.getGroupByOrderId();
			Set<Entry<Long, CalValueModel>> entries = calResult.entrySet();
			Iterator<Entry<Long, CalValueModel>> iter = entries.iterator();
			int i = 1;
			BaseCalValueModel totalModel1 = new BaseCalValueModel();
			while(iter.hasNext()){
				Row row = sheet.createRow(i);
				CalValueModel value = iter.next().getValue();
				//System.out.println(value.toString());
				value.writeToSheet(row, totalModel1);
				i++;
			}
			Row totalRow1 = sheet.createRow(i+1);
			totalRow1.createCell(6).setCellValue(totalModel1.getAuditSum());
			totalRow1.createCell(7).setCellValue(totalModel1.getChildrenSum());
			totalRow1.createCell(8).setCellValue(totalModel1.getRoomNightSum());
			totalRow1.createCell(9).setCellValue(totalModel1.getSubsidyAmountSum());
			totalRow1.createCell(11).setCellValue(totalModel1.getTemaiSubsidyAmountSum());
			totalRow1.createCell(12).setCellValue(totalModel1.getSalesPromotionAmountSum());
			totalRow1.createCell(13).setCellValue(totalModel1.getCouponAmountSum());
			totalRow1.createCell(14).setCellValue(totalModel1.getTurnoverAmountSum());
			totalRow1.createCell(15).setCellValue(totalModel1.getGrossMarginSum());
			
			
			Map<Long, CalValueModel2> calResult2 = callModel.getGroupByProductId();
			Set<Entry<Long, CalValueModel2>> entries2 = calResult2.entrySet();
			Iterator<Entry<Long, CalValueModel2>> iter2 = entries2.iterator();
			int j = 1;
			BaseCalValueModel totalModel = new BaseCalValueModel();
			while(iter2.hasNext()){
				Row row = sheet2.createRow(j);
				CalValueModel2 value = iter2.next().getValue();
				value.writeToSheet(row, totalModel);
				j++;
			}
			Row totalRow = sheet2.createRow(j+1);
			totalRow.createCell(4).setCellValue(totalModel.getAuditSum());
			totalRow.createCell(5).setCellValue(totalModel.getChildrenSum());
			totalRow.createCell(6).setCellValue(totalModel.getRoomNightSum());
			totalRow.createCell(7).setCellValue(totalModel.getSubsidyAmountSum());
			totalRow.createCell(9).setCellValue(totalModel.getTemaiSubsidyAmountSum());//特卖补贴
			totalRow.createCell(10).setCellValue(totalModel.getSalesPromotionAmountSum());
			totalRow.createCell(11).setCellValue(totalModel.getCouponAmountSum());
			totalRow.createCell(12).setCellValue(totalModel.getTurnoverAmountSum());
			totalRow.createCell(13).setCellValue(totalModel.getGrossMarginSum());
			
			fos = new FileOutputStream(this.outputPath);
			xssfWb.write(fos);
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
			throw new Exception(e);
		} finally {
			try {
				if(null != xssfWb){
					xssfWb.close();
				}
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 创建Excel表头
	 */
	private void createHeader1(Row row){
		createHeadCell(row, 0).setCellValue("事业部");
		createHeadCell(row, 1).setCellValue("分销商渠道ID");
		createHeadCell(row, 2).setCellValue("是否标示CPSID");
		createHeadCell(row, 3).setCellValue("打包产品ID");
		createHeadCell(row, 4).setCellValue("商品ID");
		createHeadCell(row, 5).setCellValue("订单号");
		createHeadCell(row, 6).setCellValue("成人数");
		createHeadCell(row, 7).setCellValue("儿童数");
		createHeadCell(row, 8).setCellValue("间夜数");
		createHeadCell(row, 9).setCellValue("补贴金额");
		createHeadCell(row, 10).setCellValue("特卖补贴占比");
		createHeadCell(row, 11).setCellValue("特卖补贴");
		createHeadCell(row, 12).setCellValue("促销金额");
		createHeadCell(row, 13).setCellValue("优惠券金额");
		createHeadCell(row, 14).setCellValue("营业额");
		createHeadCell(row, 15).setCellValue("毛利");
	}
	
	/**
	 * 创建Excel表头
	 */
	private void createHeader2(Row row){
		createHeadCell(row, 0).setCellValue("事业部");
		createHeadCell(row, 1).setCellValue("打包产品ID");
		createHeadCell(row, 2).setCellValue("商品ID");
		createHeadCell(row, 3).setCellValue("订单号");
		createHeadCell(row, 4).setCellValue("成人数");
		createHeadCell(row, 5).setCellValue("儿童数");
		createHeadCell(row, 6).setCellValue("间夜数");
		createHeadCell(row, 7).setCellValue("补贴金额");
		createHeadCell(row, 8).setCellValue("特卖补贴占比");
		createHeadCell(row, 9).setCellValue("特卖补贴");
		createHeadCell(row, 10).setCellValue("促销金额");
		createHeadCell(row, 11).setCellValue("优惠券金额");
		createHeadCell(row, 12).setCellValue("营业额");
		createHeadCell(row, 13).setCellValue("毛利");
	}
	
	private Cell createHeadCell(Row row, int index){
		Cell cell = row.createCell(index);
		CellStyle cellStyle = xssfWb.createCellStyle();
		XSSFFont cellFont = xssfWb.createFont();
		cellFont.setBold(true);
		cellFont.setColor(IndexedColors.WHITE.getIndex());//设置字体颜色
		cellStyle.setFont(cellFont);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
		cellStyle.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
		cellStyle.setWrapText(true);//设置内容自动换行
		cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cell.setCellStyle(cellStyle);
		return cell;
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
