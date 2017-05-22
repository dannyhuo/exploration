package cn.hly.ordrpt.model;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RptModel {
	
	private XSSFWorkbook wb;
	
	/**
	 * 按订单分组
	 */
	private Map<String, Map<Long, OrderWeftValue>> rptGrpByOrd = new TreeMap<>();
	
	
	/**
	 * 按产品分组
	 */
	private Map<String, Map<Long, ProductWeftValue>> rptGrpByProd = new TreeMap<>();
	

	public void eat(RuleModel rule, OrdModel ord) throws Exception{
		//1、空校验
		if(null == rule || null == ord || null == rule.getGoodsId().data 
				|| null == ord.getGoodsId().data || null == ord.getProductId().data
				|| null == ord.getOrderId().data){
			return ;
		}
		//2、规则文件和订单文件不匹配
		if(!rule.getGoodsId().data.equals(ord.getGoodsId().data)){
			throw new Exception("此条规则和订单不匹配，请检查异常");
		}
		
		//3、计算-按订单纬度分
		String bu = ord.getBuName().data;
		Map<Long, OrderWeftValue> buOrdV = rptGrpByOrd.get(bu);
		if(null == buOrdV){
			buOrdV = new TreeMap<>();
			rptGrpByOrd.put(bu, buOrdV);
		}
		Long orderId = ord.getOrderId().data;//订单ID
		OrderWeftValue owv = buOrdV.get(orderId);
		if(null == owv){
			owv = new OrderWeftValue();
			buOrdV.put(orderId, owv);
		}
		owv.eat(ord, rule);
		
		//4、计算-按产品纬度分
		bu = ord.getBuName().data;
		Map<Long, ProductWeftValue> buProdV = rptGrpByProd.get(bu);
		if(null == buProdV){
			buProdV = new TreeMap<>();
			rptGrpByProd.put(bu, buProdV);
		}
		Long productId = ord.getProductId().data;//产品ID
		ProductWeftValue pwv = buProdV.get(productId);
		if(null == pwv){
			pwv = new ProductWeftValue();
			buProdV.put(productId, pwv);
		}
		pwv.eat(ord, rule);
	}
	
	/**
	 * 写入excel结果工作簿
	 * @param ordSheet
	 * @param prdSheet
	 */
	public void write(String outputPath) throws Exception{
		this.wb = new XSSFWorkbook();
		
		if(this.rptGrpByOrd.size() > 0){
			Sheet ordSheet = wb.createSheet("结果1-订单纬度");
			createOrdRptHeader(ordSheet.createRow(0));//写表头
			Iterator<Entry<String, Map<Long, OrderWeftValue>>> buIter = this.rptGrpByOrd.entrySet().iterator();
			int i = 1;
			SumModel rptGrpByOrdSum = new SumModel();
			while(buIter.hasNext()){
				Entry<String, Map<Long, OrderWeftValue>> buGroup = buIter.next();
				if(buGroup.getValue().size() > 0){
					Iterator<Entry<Long, OrderWeftValue>> weftIter = buGroup.getValue().entrySet().iterator();
					while(weftIter.hasNext()){
						Entry<Long, OrderWeftValue> wftValue = weftIter.next();
						Row row = ordSheet.createRow(i);
						OrderWeftValue orderWeftValue = wftValue.getValue();
						orderWeftValue.write(row);
						
						sumByOrd(rptGrpByOrdSum, orderWeftValue);
						i++;
					}
				}
			}
			//写合计行
			rptGrpByOrdSum.writeSum(ordSheet.createRow(i+1), 6);
		}
		
		
		if(this.rptGrpByProd.size() > 0){
			Sheet prdSheet = wb.createSheet("结果2-产品纬度");
			createProdRptHeader(prdSheet.createRow(0));//写表头
			Iterator<Entry<String, Map<Long, ProductWeftValue>>> buIter = this.rptGrpByProd.entrySet().iterator();
			int i = 1;
			SumModel rptGrpByProdSum = new SumModel();
			while(buIter.hasNext()){
				Entry<String, Map<Long, ProductWeftValue>> buGroup = buIter.next();
				if(buGroup.getValue().size() > 0){
					Iterator<Entry<Long, ProductWeftValue>> weftIter = buGroup.getValue().entrySet().iterator();
					while(weftIter.hasNext()){
						Entry<Long, ProductWeftValue> wftValue = weftIter.next();
						Row row = prdSheet.createRow(i);
						ProductWeftValue productWeftValue = wftValue.getValue();
						productWeftValue.write(row);
						
						sumByProd(rptGrpByProdSum, productWeftValue);
						i++;
					}
				}
			}
			
			//写合计行
			rptGrpByProdSum.writeSum(prdSheet.createRow(i+1), 5);
		}
		
		//写入文件流
		FileOutputStream fos = null;
	    try {
	    	fos = new FileOutputStream(outputPath);
	    	wb.write(fos);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			if(null != fos){
				fos.close();
			}
		}
	}
	
	private void sumByOrd(SumModel sumModel, OrderWeftValue value){
		if("线路".equals(value.goodsType)){
			sumModel.addAdultSum(value.getAuditAvg());
			sumModel.addChildrenSum(value.getChildrenAvg());
		}else{
			sumModel.addAdultSum((float)value.getAuditSum());
			sumModel.addChildrenSum((float)value.getChildrenSum());
		}
		sumModel.addSubsidyAmountSum(value.getSubsidySum(value.getOrderId()));
		sumModel.addTemaiSubsidyAmountSum(value.getTemaiSubsidySum(value.getOrderId()));
		
		this.sum(sumModel, value);
	}
	
	private void sumByProd(SumModel sumModel, ProductWeftValue value){
		if("线路".equals(value.goodsType)){
			sumModel.addAdultSum(value.getTktAdultSumAvg());
			sumModel.addChildrenSum(value.getTktChildrenSumAvg());
		}else{
			sumModel.addAdultSum((float)value.getAuditSum());
			sumModel.addChildrenSum((float)value.getChildrenSum());
		}
		sumModel.addSubsidyAmountSum(value.getSubsidySum(value.getProductId()));
		sumModel.addTemaiSubsidyAmountSum(value.getTemaiSubsidySum(value.getProductId()));
		sumModel.addOrderCount(value.getOrderIds().size());
		
		this.sum(sumModel, value);
	}
	
	private void sum(SumModel sumModel, BaseValue value){
		sumModel.addCouponAmountSum(value.getCouponAmountSum());
		sumModel.addGrossMarginSum(value.getGrossMarginSum());
		sumModel.addRoomNightSum(value.getRoomNightSum());
		sumModel.addSalesPromotionAmountSum(value.getSalesPromotionAmountSum());
		sumModel.addTurnoverAmountSum(value.getTurnoverAmountSum());
	}
	
	/**
	 * 创建订单纬度报表表头
	 */
	private void createOrdRptHeader(Row row){
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
	 * 创建产品纬度报表表头
	 */
	private void createProdRptHeader(Row row){
		createHeadCell(row, 0).setCellValue("事业部");
		createHeadCell(row, 1).setCellValue("打包产品ID");
		createHeadCell(row, 2).setCellValue("商品ID");
		createHeadCell(row, 3).setCellValue("订单号");
		createHeadCell(row, 4).setCellValue("订单数量");
		createHeadCell(row, 5).setCellValue("成人数");
		createHeadCell(row, 6).setCellValue("儿童数");
		createHeadCell(row, 7).setCellValue("间夜数");
		createHeadCell(row, 8).setCellValue("补贴金额");
		createHeadCell(row, 9).setCellValue("特卖补贴占比");
		createHeadCell(row, 10).setCellValue("特卖补贴");
		createHeadCell(row, 11).setCellValue("促销金额");
		createHeadCell(row, 12).setCellValue("优惠券金额");
		createHeadCell(row, 13).setCellValue("营业额");
		createHeadCell(row, 14).setCellValue("毛利");
	}
	
	/**
	 * 创建表头单元格
	 * @param row
	 * @param index
	 * @return
	 */
	private Cell createHeadCell(Row row, int index){
		Cell cell = row.createCell(index);
		CellStyle cellStyle = wb.createCellStyle();
		Font cellFont = wb.createFont();
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
	
}
