package Main;

import java.awt.Dimension;

import javax.swing.JFrame;

import TCP.ConnectionManager;

public class Main {
	
	static ConnectionManager cm;

	public static final int PORT = 8787;
	public static final int BACKPORT = 8686;
	
	public static void main(String[] args) {
		createWindow();
		
		cm = new ConnectionManager(PORT, BACKPORT);
		cm.init();
		
		while(true) {
			if(!cm.running) {System.out.println("NEW CONNECTIONMANAGER CREATED"); cm = new ConnectionManager(PORT, BACKPORT); cm.init();}
			System.out.println("Loopcheck");
		}
		
	}
	
	public static void createWindow() {
		CustomFrame cF = new CustomFrame();
		cF.createCustomFrame();
		
		/*JFrame frame = new JFrame("DesktopControlServer");
		frame.setPreferredSize(new Dimension(300, 0));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);*/
	}	
}
