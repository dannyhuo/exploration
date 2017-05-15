package hly.com.ordrpt.ui.filefilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExcelFilter extends FileFilter{

	@Override
	public boolean accept(File f) {
		if(null != f && (f.isDirectory() || f.isFile() && 
				(f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx") || f.getName().endsWith(".csv")))){
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "excel & directory";
	}


}
