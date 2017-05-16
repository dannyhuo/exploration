package cn.hly.ordrpt.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cn.hly.ordrpt.taks.DoProcessor;
import cn.hly.ordrpt.ui.fileFilter.DirFilter;
import cn.hly.ordrpt.ui.fileFilter.ExcelFilter;

public class MainForm extends BaseForm implements ActionListener, ListSelectionListener{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5621161978896437051L;
	
	private JScrollPane myMainPan = null;
	private JScrollPane myLogPan = null;
	private JScrollPane fileViewPan = null;
	
	private JTextField rulePath = null;
	private JTextField orderPath = null;
	private JTextField outputDir = null;
	private JTextField outputFileName = null;
	private JButton openRuleFile = null;
	private JButton openOrderFile = null;
	private JButton openOutputDir = null;
	private JButton sbmitBtn = null;
	private JButton cancelBtn = null;
	/**进度条*/
	private JProgressBar progressBar = null;
	
	
	/**菜单控件区****************************************/
	private JMenuBar menuBar = null;
	private JMenu metSet = null;
	private JMenuItem miWcStyle = null;
	private JMenuItem miWinStyle = null;
	
	private JMenu metSize = null;
	private JMenuItem miSizeDefault = null;
	private JMenuItem miSizeCaling = null;
	private JMenuItem miSizeMax = null;
	
	/******************************************/
	
	private JTextArea logTextArea = null;
	private JList<String> fileList = null;
	
	/**处理线程*/
	private DoProcessor doProcessor = null;
	
	private Dimension defaultSize = new Dimension(545, 351);
	private Dimension calingSize = new Dimension(545, 557);
	private Dimension maxSize = new Dimension(751, 557);
	
	/**
	 * 调整窗口大小
	 * @param size
	 */
	private void fresh(Dimension size){
		this.setSize(size);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * 最大化
	 */
	public void maxSize(){
		this.fresh(maxSize);
	}
	
	public MainForm() {
		super("HLY");
		this.setResizable(false);
		this.initElement();
		fontStyle();
		this.setBackground(Color.white);
		this.fresh(defaultSize);
	}
	
	private void fontStyle(){
		UIManager.put("JButton.font", new Font("宋体",0, 16));
		/*String lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel";
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		SwingUtilities.updateComponentTreeUI(metSet);
		
	}
	
	
	private void initElement(){
		//菜单
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		metSet = new JMenu("Set");
		menuBar.add(metSet);
		miWcStyle = new JMenuItem("Win Classic");
		metSet.add(miWcStyle);
		miWcStyle.addActionListener(this);
		
		miWinStyle = new JMenuItem("Windows");
		metSet.add(miWinStyle);
		miWinStyle.addActionListener(this);
		
		metSize = new JMenu("Size");
		menuBar.add(metSize);
		
		miSizeDefault = new JMenuItem("Default");
		metSize.add(miSizeDefault);
		miSizeDefault.addActionListener(this);
		
		miSizeCaling = new JMenuItem("Middle");
		metSize.add(miSizeCaling);
		miSizeCaling.addActionListener(this);
		
		miSizeMax = new JMenuItem("Max");
		metSize.add(miSizeMax);
		miSizeMax.addActionListener(this);
		
		//主pannel设置
		myMainPan = new JScrollPane();
		myMainPan.setLayout(null);
		myMainPan.setVisible(true);
		myMainPan.setBackground(Color.white);
		//myMainPan.setBorder(BorderFactory.createLineBorder(Color.gray));
		this.addEle(myMainPan, 530, 289, 5, 5);
		
		//日志pannel设置
		myLogPan = new JScrollPane();
		myLogPan.setLayout(null);
		myLogPan.setVisible(true);
		myLogPan.setBackground(Color.white);
		this.addEle(myLogPan, 530, 200, 5, 300);
		
		//设置fileViewPan
		fileViewPan = new JScrollPane();
		fileViewPan.setLayout(null);
		fileViewPan.setVisible(true);
		fileViewPan.setBackground(Color.white);
		this.addEle(fileViewPan, 200, 495, 540, 5);
		
		//规则文件
		this.addEle(myMainPan, new JLabel("规则文件"), 60, 30, 30, 30);
		rulePath = new JTextField();
		rulePath.setEditable(false);
		rulePath.setBackground(Color.white);
		this.addEle(myMainPan, rulePath, 300, 30, 90, 30);
		openRuleFile = new JButton("浏览");
		this.addEle(myMainPan, openRuleFile, 80, 30, 410, 30);
		openRuleFile.addActionListener(this);
		
		//订单文件
		this.addEle(myMainPan, new JLabel("订单文件"), 60, 30, 30, 90);
		orderPath = new JTextField();
		orderPath.setEditable(false);
		orderPath.setBackground(Color.white);
		this.addEle(myMainPan, orderPath, 300, 30, 90, 90);
		openOrderFile = new JButton("浏览");
		this.addEle(myMainPan, openOrderFile, 80, 30, 410, 90);
		openOrderFile.addActionListener(this);
		
		//输出目录
		this.addEle(myMainPan, new JLabel("输出目录"), 60, 30, 30, 150);
		outputDir = new JTextField();
		outputDir.setEditable(false);
		outputDir.setBackground(Color.white);
		this.addEle(myMainPan, outputDir, 160, 30, 90, 150);
		openOutputDir = new JButton("浏览");
		this.addEle(myMainPan, openOutputDir, 80, 30, 410, 150);
		openOutputDir.addActionListener(this);
		
		outputFileName = new JTextField("default-output.xlsx");
		this.addEle(myMainPan, outputFileName, 130, 30, 260, 150);
		outputFileName.addActionListener(this);
		
		//计算按钮
		sbmitBtn = new JButton("开始计算");
		this.addEle(myMainPan, sbmitBtn, 120, 30, 370, 210);
		sbmitBtn.addActionListener(this);
		
		//取消按钮
		cancelBtn = new JButton("取消");
		this.addEle(myMainPan, cancelBtn, 120, 30, 200, 210);
		cancelBtn.addActionListener(this);
		
		//进度条
		progressBar = new JProgressBar();
		progressBar.setBackground(Color.white);
		progressBar.setName("执行进度");
		progressBar.setBorder(BorderFactory.createEmptyBorder());
		this.addEle(myMainPan, progressBar, 526, 25, 1, 260);
		
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		logTextArea.setAutoscrolls(true);
		logTextArea.setBorder(BorderFactory.createEmptyBorder());
		//logTextArea.setFont(new Font("宋体", Font.PLAIN, 14));
		logTextArea.setBackground(Color.white);
		this.addEle(myLogPan, new JScrollPane(logTextArea), 530, 200, 0, 0);
		
		fileList = new JList<>();
		fileList.setAutoscrolls(true);
		fileList.setBorder(BorderFactory.createEmptyBorder());
		fileList.addListSelectionListener(this);
		this.addEle(fileViewPan, new JScrollPane(fileList), 200, 495, 0, 0);
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if(s.equals(openRuleFile)){
			JFileChooser jfc=new JFileChooser();
	        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
	        jfc.setFileFilter(new ExcelFilter());
	        jfc.showDialog(this, "选择规则文件");
	        File file=jfc.getSelectedFile();
	        if(file != null){
	            this.rulePath.setText(file.getAbsolutePath());
	            this.rulePath.setForeground(Color.black);
	        }
		}else if(s.equals(openOrderFile)){
			JFileChooser jfc=new JFileChooser();
	        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY );
	        jfc.setFileFilter(new ExcelFilter());
	        jfc.showDialog(new JLabel(), "选择订单文件");
	        File file=jfc.getSelectedFile();
	        if(file != null){
	            this.orderPath.setText(file.getAbsolutePath());
	            this.orderPath.setForeground(Color.black);
	            String fileName = file.getName();
	            this.outputFileName.setText(fileName.substring(0, fileName.lastIndexOf(".")) + "-计算结果.xlsx");
	        }
		}else if(s.equals(openOutputDir)){
			JFileChooser jfc=new JFileChooser();
	        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
	        jfc.setFileFilter(new DirFilter());
	        jfc.showDialog(new JLabel(), "指定结果输出目录");
	        File file=jfc.getSelectedFile();
	        if(file != null){
	        	this.outputDir.setForeground(Color.black);
	            this.outputDir.setText(file.getAbsolutePath());
	        }
		}else if(s.equals(sbmitBtn)){//提交计算
			boolean canSbmt = true;
			if("".equals(rulePath.getText().trim()) ||
					rulePath.getForeground().equals(Color.red)){
				textShowMsg("请输入规则文件。。。", rulePath);
				canSbmt = false;
			}
			if("".equals(orderPath.getText().trim()) ||
					orderPath.getForeground().equals(Color.red)){
				textShowMsg("请输入订单文件。。。", orderPath);
				canSbmt = false;
			}
			if("".equals(outputDir.getText().trim()) ||
					outputDir.getForeground().equals(Color.red)){
				textShowMsg("输指定输出文件目录。。。", outputDir);
				canSbmt = false;
			}
			if(canSbmt){
				doProcessor = new DoProcessor(this);
				doProcessor.setRulePath(rulePath.getText().trim());
				doProcessor.setOrderPath(orderPath.getText().trim());
				doProcessor.setOutputPath(outputDir.getText().trim() + "\\" + outputFileName.getText().trim());
				doProcessor.start();
				sbmitBtn.setEnabled(false);
				cancelBtn.setEnabled(true);
				this.fresh(calingSize);
			}
		} else if(s.equals(cancelBtn)){//取消
			if(null != doProcessor){
				sbmitBtn.setEnabled(true);
				cancelBtn.setEnabled(false);
			}
		} else if(s.equals(miWcStyle)){
			refreshStyle("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		}else if(s.equals(miWinStyle)){
			refreshStyle("javax.swing.plaf.metal.MetalLookAndFeel");
		} else if(s.equals(miSizeDefault)){
			fresh(defaultSize);
		} else if(s.equals(miSizeCaling)){
			fresh(calingSize);
		} else if(s.equals(miSizeMax)){
			fresh(maxSize);
		}
	}
	
	private void refreshStyle(String style){
		try {
			UIManager.setLookAndFeel(style);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	private void textShowMsg(String msg, JTextField jtf){
		jtf.setForeground(Color.red);
		jtf.setText(msg);
	}

	public JTextField getRulePath() {
		return rulePath;
	}

	public JTextField getOrderPath() {
		return orderPath;
	}

	public JTextField getOutputDir() {
		return outputDir;
	}

	public JTextField getOutputFileName() {
		return outputFileName;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JTextArea getLogTestArea() {
		return logTextArea;
	}

	public JList<String> getFileList(){
		return this.fileList;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()){
			return ;
		}
		if(this.fileList.equals(e.getSource())){
			String selectedPath = this.outputDir.getText() + "\\" + this.fileList.getSelectedValue();
			
			System.out.println(selectedPath);
			
			File file = new File(selectedPath);
			if(file.exists()){
				try {
					Process process = Runtime.getRuntime().exec("cmd /c start " + file.getAbsolutePath());
					process.waitFor();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
