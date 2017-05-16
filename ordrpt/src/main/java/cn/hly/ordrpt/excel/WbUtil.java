package cn.hly.ordrpt.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class WbUtil {
	
	/**
	 * 读取WorkBook
	 * @param excelFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Workbook getWorkBook(String excelFile) 
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

	public static void writeSheet(SheetModel sheet){
		if(null == sheet){
			return;
		}
		
		sheet.write();
	}
	
	
}
