package hly.com.ordrpt.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class ConfirmDialog extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5017212895593243954L;
	
	private String path;
	
	public ConfirmDialog(String path) {
		this.path = path;
		
		this.init();
		
		this.setSize(600, 400);
		
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
	}
	
	public void init(){
		File file = new File(path);
		
		if(file.exists()){
			File[] files = file.getParentFile().listFiles();
			int size = files.length + 1;
			this.setLayout(new GridLayout(size, 1));
			for (int i = 0; i < files.length; i++) {
				
				JLabel label = new JLabel(files[i].getName());
				this.add(label);
			}
		}
	}
	
	public static void main(String[] args) {
		new ConfirmDialog("E:\\StudyApachePOI\\default-output-201705051012.xlsx");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
