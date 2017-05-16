package cn.hly.ordrpt.excel;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class CellModel implements Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6458772788572307975L;
	
	
	public CellModel() {
		
	}
	
	public CellModel(Row row) {
		this.row = row;
	}
	
	private Row row = null;
	
	
	/**
	 * 单元格列索引
	 */
	private int index;
	
	/**
	 * 数据
	 */
	private Object content;
	
	/**
	 *写入
	 */
	public boolean write(){
		if(null == row){
			return false;
		}
		
		if(null != content){
			row.createCell(index).setCellValue(content.toString());
		}
		return true;
	}
	
	
	/**
	 * 读取
	 */
	public static CellModel read(Cell cell){
		CellModel cellModel = null;
		if(null != cell){
			cellModel = new CellModel();
			
			cellModel.setIndex(cell.getColumnIndex());
			
			int type = cell.getCellType();
			if(Cell.CELL_TYPE_STRING == type){
				cellModel.setContent(cell.getStringCellValue());
			}else if(Cell.CELL_TYPE_NUMERIC == type){
				cellModel.setContent(cell.getNumericCellValue());
			}else if(Cell.CELL_TYPE_BOOLEAN == type){
				cellModel.setContent(cell.getBooleanCellValue());
			}else if(Cell.CELL_TYPE_FORMULA == type){
				cellModel.setContent(cell.getCellFormula());
			}
		}
		
		return cellModel;
	}
	
	@Override
	public String toString() {
		if(null == content){
			return null;
		}
		return content.toString();
	}
	
	public Row getRow() {
		return row;
	}

	public void setRow(Row row) {
		this.row = row;
	}

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T getContent() {
		if(null == content){
			return null;
		}
		return (T) content;
	}
	
	public <T extends Object> void setContent(T content) {
		this.content = content;
	}
}
