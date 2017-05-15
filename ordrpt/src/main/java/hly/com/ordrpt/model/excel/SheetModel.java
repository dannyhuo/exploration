package hly.com.ordrpt.model.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class SheetModel implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 280912459677702030L;
	
	public SheetModel() {
		rows = new ArrayList<>();
	}
	
	/**
	 * 工作簿名称
	 */
	private String sheetName;
	
	/**
	 * 工作簿行数据
	 */
	private List<RowModel> rows;
	
	/**
	 * 写入
	 */
	public void write(){
		if(null != rows && rows.size() > 0){
			for(int i = 0; i < rows.size(); i++){
				rows.get(i).write();
			}
		}
	}
	
	/**
	 * 将工作簿读取至对象中
	 * @param sheet
	 * @return
	 */
	public static SheetModel read(Sheet sheet){
		if(null == sheet){
			return null;
		}
		int startRow = sheet.getFirstRowNum();
		int endRow = sheet.getLastRowNum();
		SheetModel sheetModel = new SheetModel();
		for(int i = startRow; i <= endRow; i++){
			Row row = sheet.getRow(i);
			RowModel rowModel = RowModel.read(row);
			if(null != rowModel){
				List<RowModel> rows = sheetModel.getRows();
				rows.add(rowModel);
			}
		}
		return sheetModel;
	}

	
	@Override
	public String toString() {
		int size = 0;
		if(null == rows || (size = rows.size()) < 1){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			RowModel rowModel = rows.get(i);
			sb.append(rowModel.toString());
			if(i < (size - 1)){
				sb.append("\n");
			}
		}
		return sb.toString();
		
	}
	
	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public List<RowModel> getRows() {
		return rows;
	}

	public void setRows(List<RowModel> rows) {
		this.rows = rows;
	}
	
}
