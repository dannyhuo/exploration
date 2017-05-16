package cn.hly.ordrpt.ui.fileFilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class DirFilter extends FileFilter{

	@Override
	public boolean accept(File f) {
		if(null != f && f.isDirectory()){
			return true;
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "目录";
	}

}
