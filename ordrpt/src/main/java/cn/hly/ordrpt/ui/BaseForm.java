package cn.hly.ordrpt.ui;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class BaseForm extends JFrame {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5555928957502488286L;
	
	public BaseForm() {
		this.setLayout(null);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public BaseForm(String title) {
		this.setTitle(title);
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Font font = new Font("宋体", Font.ITALIC, 12);
		this.setFont(font);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	public <T extends JComponent> void addEle(T component, int width, int height, int x, int y){
		component.setSize(width, height);
		component.setLocation(x,y);
		component.setVisible(true);
		this.getContentPane().add(component);
	}
	
	public <T extends JComponent> void addEle(JComponent parent, T component, int width, int height, int x, int y){
		component.setSize(width, height);
		component.setLocation(x,y);
		component.setVisible(true);
		parent.add(component);
	}
	
}
