package hly.com.ordrpt.task;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import hly.com.ordrpt.model.OrdModel;
import hly.com.ordrpt.ui.MainForm;


public class DoProcessor extends Thread{
	
	/**
	 * 完成进度
	 */
	private JProgressBar progress = null;
	//日志输出区域
	private JTextArea logTextArea = null;
	//文件列表
	private JList<String> fileList = null;
	
	private String rulePath;//规则文件路径
	private String orderPath;//订单文件路径
	private String outputPath;//结果输出路径
	
	/**执行计算的任务*/
	private OrderCalTask orderCalTask = null;
	
	/**订单是否处理完毕标志位*/
	private boolean orderDidOutflag = false;
	
	/**主界面*/
	private MainForm myForm = null;
	
	/**
	 * 构造函数
	 * @param myForm
	 */
	public DoProcessor(MainForm myForm) {
		this.progress = myForm.getProgressBar();
		this.logTextArea = myForm.getLogTestArea();
		this.fileList = myForm.getFileList();
		this.myForm = myForm;
	}
	
	
	@Override
	public void run() {
		//执行前
		this.initialize();
		
		//执行中
		this.doProcess();
		
		//执行后
		this.finished();
	}
	
	/**
	 * 执行前
	 */
	private void initialize(){
		progress.setStringPainted(true);
		progress.setValue(1);
		progress.setString("Wait a moment...");
	}
	
	/**
	 * 执行中
	 */
	private void doProcess(){
		orderCalTask = new OrderCalTask(rulePath, orderPath, outputPath);
		orderCalTask.start();
		while(this.progress.getValue() < 100 && !orderCalTask.isTaskFinished()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//设置进度
			progress.setString(progress.getValue()+"%");
			progress.setValue(percentFinished().intValue());
			
			//控制台输出日志
			String info = this.getOrdTaskInfo();
			if(info.length() > 1){
				logTextArea.append(info);
				logTextArea.selectAll();
			}
		}
		this.progress.setValue(100);
	}
	
	/**
	 * 执行完成后
	 */
	private void finished(){
		progress.setIndeterminate(false);
		progress.setString(orderCalTask.getTaskRunResult().item2);

		//将窗口设置为最大化
		myForm.maxSize();
		
		File file = new File(this.outputPath);
		final DefaultListModel<String> listModel = new DefaultListModel<>();
		listModel.addElement(null);
		listModel.addElement("");
		if(file.exists()){
			File[] fies = file.getParentFile().listFiles();
			for (int i = 0; i < fies.length; i++) {
				listModel.addElement(fies[i].getName());
			}
		}
		fileList.setModel(listModel);
	}

	private BigDecimal percentFinished(){
		if(orderCalTask.getTotalRow() > 0){
			BigDecimal cur = new BigDecimal(orderCalTask.getCurRow());
			BigDecimal total = new BigDecimal(orderCalTask.getTotalRow());
			BigDecimal percent = cur.divide(total, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
			return percent;
		}
		
		return new BigDecimal("0");
	}
	
	/**
	 * 获取运行时信息
	 * @return
	 */
	private String getOrdTaskInfo(){
		String info = null;
		if(null != this.orderCalTask){
			if(orderCalTask.isTaskFinished()){
				info = orderCalTask.getTaskRunResult().item2;
			}
			
			StringBuilder msg = new StringBuilder();
			if(!orderDidOutflag && orderCalTask.getCurRow() > orderCalTask.getTotalRow()){
				List<OrdModel> errorOrdersModel = orderCalTask.getCaledErrorModel();
				msg.append("已完成");
				msg.append(new DecimalFormat("##.###").format(percentFinished()));
				msg.append("% ！\r\n");
				if(null != errorOrdersModel && errorOrdersModel.size() > 0){
					msg.append("异常订单信息如下：\r\n");
					for (int i = 0; i < errorOrdersModel.size(); i++) {
						OrdModel ordModel = errorOrdersModel.get(i);
						msg.append(ordModel.toString());
						msg.append("\r\n");
					}
				}
				orderDidOutflag = true;
				return msg.toString();
			}
			
			msg.append("当前正在处理第");
			msg.append(orderCalTask.getCurRow());
			msg.append("条订单, 已完成");
			msg.append(percentFinished());
			msg.append("% ！");
			msg.append("\r\n");
			info = msg.toString();
		}
		return info;
	}
	
	
	public String getRulePath() {
		return rulePath;
	}

	public void setRulePath(String rulePath) {
		this.rulePath = rulePath;
	}

	public String getOrderPath() {
		return orderPath;
	}

	public void setOrderPath(String orderPath) {
		this.orderPath = orderPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
}
