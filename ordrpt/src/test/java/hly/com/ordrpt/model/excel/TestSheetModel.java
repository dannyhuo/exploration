package hly.com.ordrpt.model.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestSheetModel {
	
	public static void main(String[] args) {
		Workbook ruleWb = null;
		try {
			ruleWb = getWorkBook("E:\\StudyApachePOI\\default-output-201705051012.xlsx");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Sheet sheet = ruleWb.getSheetAt(0);
		
		SheetModel model = SheetModel.read(sheet);
		System.out.println(model.toString());
	}

	/**
	 * 读取WorkBook
	 * @param excelFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Workbook getWorkBook(String excelFile) throws FileNotFoundException, IOException{
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
}
