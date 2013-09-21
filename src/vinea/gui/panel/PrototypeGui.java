/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.gui.panel;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.Timer;

import javax.swing.JFrame;

import rina.config.RINAConfig;



public class PrototypeGui extends JPanel  {

	private JMenuBar mMBar;
	private JPanel mInfo;
	private JFrame mFrame;//, mCADConsole;
	private GuiControl mGuiControl;
	private ActionListener mActionListener;
	private ControlPanel mControlPanel =null;
	private StatusBar mStatusBar; 
	private RINAConfig config;
	private JTextArea textArea;
	private CADSysConsole  mCADConsole;



	/**
	 * Constructor
	 */
	public PrototypeGui() {

	}
	/**
	 * 
	 */
	public void initComponents(RINAConfig config){

		this.config = config; 
		mGuiControl=new GuiControl(this,config);
		mActionListener=new ActionHandler(mGuiControl);

		mInfo = new ControlPanel(mGuiControl, config);

		// mSimGuiControl.setGraphPanel(mGraph);
		// mSimGuiControl.setControlPanel(mInfo);


		setLayout(new BorderLayout());
		setLayout(new FlowLayout(FlowLayout.LEADING));
		//Adding BoardPanel Contents
		//		  mGraph.setBorder(new javax.swing.border.EtchedBorder());
		//		  mGraph.setPreferredSize(new java.awt.Dimension(480, 480));
		//		  add(mGraph, java.awt.BorderLayout.CENTER);



		//Adding InformationPanel Contents
		mInfo.setBorder(new javax.swing.border.EtchedBorder());
		mInfo.setPreferredSize(new java.awt.Dimension(480, 480));
		add(mInfo, java.awt.BorderLayout.WEST);




		mStatusBar = new StatusBar("Please load CADSys config file.");
		mStatusBar.setBorder(new javax.swing.border.EtchedBorder());
		mStatusBar.setPreferredSize(new java.awt.Dimension(400,20));
		mStatusBar.setPreferredSize(new java.awt.Dimension(400,20));
		//add(mStatusBar, java.awt.BorderLayout.AFTER_LAST_LINE);



		mCADConsole = new CADSysConsole();
		//    add(mCADConsole);

		//		ControlPanel culo2 = new ControlPanel(mGuiControl, config);
		//		culo2.setBorder(new javax.swing.border.EtchedBorder());
		//		culo2.setPreferredSize(new java.awt.Dimension(280, 280));
		//		add(culo2, java.awt.BorderLayout.SOUTH);
		//	JTextArea textarea = MakeTextFrame();

		//	JTextArea textarea1 = MakeConsolPanel();
		//	JTextArea textarea = MakeConsolPanel();



		System.out.println("In this console you will see all messages:  ");
/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		System.out.println("The CADSys Configuration file is loading...  ");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}




	public void go(JFrame win){
		win.setContentPane(this);
		setMenu();
		win.setJMenuBar(mMBar);
		win.addWindowListener(new WindowHandler(win));
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setLocationRelativeTo(null);
		win.setVisible(true);
		mFrame=win;
	}



	public JFrame getFrame(){
		return mFrame;
	}



	public JPanel getInfoPanel(){
		return mInfo;
	}
	/**
	 * terminate
	 */
	public void quit(){
		mFrame.setVisible(false);
		mFrame.dispose();
		System.exit(0);  
	}

	/**
	 * 
	 * @author 
	 *
	 */
	public final static class ActionHandler implements ActionListener{

		private GuiControl mSimGuiControl;
		
		ActionHandler(GuiControl simGuiControl){
			mSimGuiControl=simGuiControl;
		}
		
		public void actionPerformed(ActionEvent e){
			String cmd=e.getActionCommand();
			if(cmd.equals("Load Config File")) {
				mSimGuiControl.openConf();
			}

			else if(cmd.equals("Save Config File")) {
				mSimGuiControl.saveConf();
			}
			else if(cmd.equals("Open Graph File")) {	
				mSimGuiControl.openGraph();

			}
			else if(cmd.equals("Run")) {
				mSimGuiControl.run();
			}

			else if(cmd.equals("Pause")) {
				mSimGuiControl.stop();
			}
			else if(cmd.equals("Resume")) {
				//	mSimGuiControl.resume();
			}

			else if(cmd.equals("Stop")) {
				//	mSimGuiControl.terminate();
			}
			else if(cmd.equals("Quit")) {
				mSimGuiControl.quit();
			}

			else if(cmd.equals("Options")) {
			   mSimGuiControl.options();
			}

			else if(cmd.equals("About")) {
				mSimGuiControl.about();;
			}

		}

	}

	private final static class WindowHandler extends WindowAdapter
	{
		private JFrame frame;
		public WindowHandler(JFrame f){ frame=f;}
		public void windowClosing(WindowEvent e){
			//     Simulator.getInstance().stopIt();
			//	mScheduler.discharge();
			frame.setVisible(false);
			frame.dispose();
			System.exit(0);
		}
	}
	private class StatusBar extends JPanel
	{
		private int current=0;
		private int total=0;
		private JLabel mText=null;
		private JProgressBar mProgressBar=null;

		int test=0;

		public StatusBar(String s){
			mText=new JLabel();
			mText.setText(s);
			setLayout(new BorderLayout());
			mText.setPreferredSize(new java.awt.Dimension(200, 120));
			add(mText, java.awt.BorderLayout.CENTER);

			mProgressBar=new JProgressBar();
			mProgressBar.setPreferredSize(new java.awt.Dimension(160, 20));
			mProgressBar.setBorderPainted(false);
			add(mProgressBar, java.awt.BorderLayout.EAST);

			//startProgress(200);

		}
		public void setMax(int m){
			mProgressBar.setVisible(true);

			if(total!=m){
				total=m;
				mProgressBar.setMaximum(m);
				setIndeterminate(false);
			}
		}
		public void setProgress(int c){
			current=c;
			mProgressBar.setValue(c);
		}
		public void setText(String t){
			mText.setText(t);
		}
		public void setIndeterminate(boolean b){
			mProgressBar.setIndeterminate(b);
			mProgressBar.setStringPainted(!b);

		}

		public void reset(){
			mProgressBar.setVisible(false);
		}
		public void startProgress(int m){

			int delay = 500; //500 milliseconds

			setMax(m);

			ActionListener taskPerformer = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					//setProgress(mScheduler.getCurrent());

					setProgress(test);
					test+=10;
				}
			};
			Timer mTimer=new Timer(delay, taskPerformer);
			mTimer.setRepeats(true);
			mTimer.start();
		}

	}



	private void setMenu(){
		mMBar=new JMenuBar();

		JMenu file=new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		JMenuItem file_openConfig=new JMenuItem("Load Config File", KeyEvent.VK_O);
		file_openConfig.addActionListener(mActionListener);
		file.add(file_openConfig);



		JMenuItem file_saveConfig=new JMenuItem("Save Config File", KeyEvent.VK_S);
		file_saveConfig.addActionListener(mActionListener);
		file.add(file_saveConfig);


		//	JMenuItem file_openGraph=new JMenuItem("Open Graph File", KeyEvent.VK_G);
		//	file_openGraph.addActionListener(mActionListener);
		//	file.add(file_openGraph);

		JMenuItem file_quit=new JMenuItem("Quit", KeyEvent.VK_Q);
		file_quit.addActionListener(mActionListener);
		file.add(file_quit);

		JMenu sim=new JMenu("Testing");
		sim.setMnemonic(KeyEvent.VK_T);

		JMenuItem sim_run=new JMenuItem("Run", KeyEvent.VK_R);
		sim_run.addActionListener(mActionListener);
		sim.add(sim_run);

		JMenuItem sim_pause=new JMenuItem("Pause", KeyEvent.VK_E);
		sim_pause.addActionListener(mActionListener);
		sim.add(sim_pause);


		JMenuItem sim_resume=new JMenuItem("Resume", KeyEvent.VK_U);
		sim_resume.addActionListener(mActionListener);
		sim.add(sim_resume);

		JMenuItem sim_stop=new JMenuItem("Stop", KeyEvent.VK_P);
		sim_stop.addActionListener(mActionListener);
		sim.add(sim_stop);


		//JMenu settings=new JMenu("Settings");
		//settings.setMnemonic(KeyEvent.VK_S);

		//JMenuItem settings_options=new JMenuItem("Options", KeyEvent.VK_O);
		//settings_options.addActionListener(mActionListener);
		//settings.add(settings_options);

		JMenu help=new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);

		JMenuItem help_about=new JMenuItem("About", KeyEvent.VK_A);
		help_about.addActionListener(mActionListener);
		help.add(help_about);

		mMBar.add(file);
		mMBar.add(sim);
		//mMBar.add(settings);
		mMBar.add(help);


	}


	public RINAConfig getConfig(){
		return this.config;
	}
}//end of class



