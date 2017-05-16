package cn.hly.ordrpt.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class RowModel implements Serializable{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4248474684931468654L;

	public RowModel() {
		cells = new ArrayList<>();
	}
	
	public RowModel(Sheet sheet) {
		this.sheet = sheet;
	}
	
	private Sheet sheet = null;
	
	/**
	 * 行序号
	 */
	private int rowIndex;
	
	/**
	 * 行数据
	 */
	private List<CellModel> cells;
	
	
	/**
	 * 写入行
	 */
	public void write(){
		Row row = sheet.createRow(rowIndex);
		int size = 0;
		if(null != cells && (size = cells.size()) > 0){
			for (int i = 0; i < size; i++) {
				CellModel cell = cells.get(i);
				cell.setRow(row);
				cells.get(i).write();
			}
		}
	}
	
	/**
	 * 从row中读取
	 * @param row
	 * @return
	 */
	public static RowModel read(Row row){
		if(null == row){
			return null;
		}
		short start = row.getFirstCellNum();
		short end = row.getLastCellNum();
		RowModel rowModel = new RowModel();
		rowModel.setRowIndex(row.getRowNum());
		
		for(int i = start; i <=end; i++){
			CellModel cell = CellModel.read(row.getCell(i));
			if(null != cell){
				rowModel.getCells().add(cell);
			}
		}
		return rowModel;
	}
	
	@Override
	public String toString() {
		int size = 0;
		if(null == cells || (size = cells.size()) < 1){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			CellModel cellModel = cells.get(i);
			if(null != cellModel){
				sb.append(cellModel.toString());
			}else{
				System.out.println(cellModel);
			}
			if(i < (size - 1)){
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public List<CellModel> getCells() {
		return cells;
	}
	
	/**
	 * 获取某一列
	 * @param index
	 * @return
	 */
	public CellModel getCell(int index){
		int size = cells.size();
		if(index < 0 || index > size){
			return null;
		}
		return cells.get(index);
	}

	public void setCells(List<CellModel> data) {
		this.cells = data;
	}

}
