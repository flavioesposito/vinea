/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import rina.config.RINAConfig;
import vinea.gui.util.ConfigFromGUI;
import vinea.gui.util.MyFileChooser;

/**
 * @author Flavio Esposito and Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0 
 *
 */
public class ControlPanel extends JPanel implements ActionListener {

	private JButton  mRun,  mStop, mShowIPCName, mOptions; //mGraph,  mMorph, mPause,
	private GuiControl mGuiControl;
	private MyFileChooser fileChooser = new MyFileChooser();
	private JToolBar mToolBar=null;
	private Box mMainPanel;
	private CADSysPanel mCADPanel;
	private Dimension size = new Dimension( 480, 500 );
	private JLabel logoLabel;

	private ConfigFromGUI mConfigFromGUI = new ConfigFromGUI();


	private RINAConfig config;
	/**
	 * 
	 * @param GuiControl associate with the panel
	 */
	public ControlPanel(GuiControl g, RINAConfig config){
		this.mGuiControl = g;
		this.config = config;
		this.initializeComponents();
	}

	private void initializeComponents() {


		makeToolBar();
		mMainPanel=Box.createVerticalBox();    	


		mCADPanel=new CADSysPanel(this, this.config);
		mCADPanel.setPreferredSize(new Dimension(320,450));
		mCADPanel.setMinimumSize(new Dimension(320,450));

		//here the initialization of mPanel depends on choices
		//switch(mCADPanel.getProtocolIndex()){}

		//      mPanel.setPreferredSize(new Dimension(320,450));


		mMainPanel.setPreferredSize(new Dimension(450,450));
		mMainPanel.setMinimumSize(new Dimension(450,450));
		mMainPanel.add(mCADPanel);


		add(mMainPanel);
		mCADPanel.retrieve(); 




	}
	/**
	 * generates objects for tool bar
	 */
	private void makeToolBar() {




		mToolBar = new JToolBar();
		mToolBar.getAlignmentX();

		mRun = new JButton("Run");
		mRun.setMnemonic(KeyEvent.VK_R);
		mRun.setActionCommand("Run");

		mStop = new JButton("Stop");
		mStop.setMnemonic(KeyEvent.VK_T);
		mStop.setActionCommand("Stop");
		mStop.setEnabled(false);



		/*
        String path="";
		try {
			path = new java.io.File(".").getCanonicalPath();
			//System.out.println("---path: "+path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */


		//     mPause = new JButton("Pause");
		//    mPause.setMnemonic(KeyEvent.VK_P);
		//     mPause.setActionCommand("Pause");
		//    mPause.setEnabled(false);

		//  	mGraph = new JButton("Graph");
		//     mGraph.setMnemonic(KeyEvent.VK_G);
		//     mGraph.setActionCommand("Graph");

		//   	mMorph = new JButton("Morph");
		//      mMorph.setMnemonic(KeyEvent.VK_M);
		//      mMorph.setActionCommand("Morph");
		//      mMorph.setEnabled(false);

	//	mShowIPCName = new JButton("IPC Name");
	//	mShowIPCName.setMnemonic(KeyEvent.VK_I);
	//	mShowIPCName.setActionCommand("IPC Name");
	//	mShowIPCName.setEnabled(true);

	//	mOptions = new JButton("Options");
	//	mOptions.setMnemonic(KeyEvent.VK_O);
	//	mOptions.setActionCommand("Options");
	//	mOptions.setEnabled(false);

		mRun.addActionListener(this);
		//      mPause.addActionListener(this);
		mStop.addActionListener(this);
		//      mGraph.addActionListener(this);
		//     mMorph.addActionListener(this);
	//	mShowIPCName.addActionListener(this);
	//	mOptions.addActionListener(this);

		mRun.setToolTipText("Click to run testing example.");
		//       mPause.setToolTipText("Click to pause testing example.");
		mStop.setToolTipText("Click to stop testing example.");
		//      mGraph.setToolTipText("Click to load graph file.");
		//     mMorph.setToolTipText("Click to make the graph look more clear.");
	//	mShowIPCName.setToolTipText("Click to show IPC name.");

		mToolBar.add(mRun);
		//       mToolBar.add(mPause);
		mToolBar.add(mStop);
		//       mToolBar.add(mGraph);
		//      mToolBar.add(mMorph);
	//	mToolBar.add(mShowIPCName);
	//	mToolBar.add(mOptions);

		add(mToolBar, BorderLayout.PAGE_START);


	}





	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Run".equals(e.getActionCommand())) {

			//			  JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
			//					  "Functionality not implemented yet",
			//				       "Run", JOptionPane.INFORMATION_MESSAGE);

			mRun.setEnabled(false);
			mStop.setEnabled(true);
			mShowIPCName.setEnabled(false);
			mOptions.setEnabled(false);
			//	            mControl.getSimGui().setStatusBar(" Running ... ");
			mCADPanel.start();

		} 
		else if ("Pause".equals(e.getActionCommand())) {

			JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
					"Functionality not implemented yet",
					"Pause", JOptionPane.INFORMATION_MESSAGE);	
		} 
		else if ("Stop".equals(e.getActionCommand())) {
		//	JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
		//			"Functionality not implemented yet",
		//			"Stop", JOptionPane.INFORMATION_MESSAGE);	
			
			mRun.setEnabled(true);
			mStop.setEnabled(false);
			mShowIPCName.setEnabled(false);
			mOptions.setEnabled(false);
			//	            mControl.getSimGui().setStatusBar(" Running ... ");
			mCADPanel.stop();
			
		}

		else if ("Graph".equals(e.getActionCommand())) {
			JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
					"Functionality not implemented yet",
					"Graph", JOptionPane.INFORMATION_MESSAGE);	
		}
		else if ("Morph".equals(e.getActionCommand())) {
			JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
					"Functionality not implemented yet",
					"Morph", JOptionPane.INFORMATION_MESSAGE);	
		}
		else if("IPC Name".equals(e.getActionCommand())) {
			JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
					"Functionality not implemented yet",
					"IPC Name", JOptionPane.INFORMATION_MESSAGE);
		}

		else if ("Options".equals(e.getActionCommand())){
			JOptionPane.showMessageDialog(mGuiControl.getGui().getFrame(), 
					"Functionality not implemented yet",
					"Options", JOptionPane.INFORMATION_MESSAGE);	
		}


		//following commands from menu
		else if ("Load".equals(e.getActionCommand()) || 
				"Load Config File".equals(e.getActionCommand())) {
			System.out.print("asadsssssss");

			mRun.setEnabled(false);
			//	   mPause.setEnabled(false);
			mStop.setEnabled(false);
			//	   mGraph.setEnabled(false);
			//	   mMorph.setEnabled(false);
			mShowIPCName.setEnabled(false);
			mOptions.setEnabled(false);
			fileChooser.setFileFilter(MyFileChooser.configFilter);


			int res = fileChooser.showOpenDialog(getParent());
			if (res == JFileChooser.CANCEL_OPTION)
				return;

			File file = fileChooser.getSelectedFile();

			System.out.print("getmTextConfigFile : "+mCADPanel.getmTextConfigFile().getText());
			mCADPanel.loadConfig(file);
			mCADPanel.getmTextConfigFile().setText(file.getName());
		} 

		// if we want to add later save and load from the main menu
		else if ("Save".equals(e.getActionCommand()) ||
				"Save Config File".equals(e.getActionCommand())) {
			fileChooser.setFileFilter(MyFileChooser.configFilter);


			int res = fileChooser.showOpenDialog(getParent());
			if (res == JFileChooser.CANCEL_OPTION)
				return;

			File file = fileChooser.getSelectedFile();
			mCADPanel.saveConfig(file);
		}
	}




	/**
	 * gets preferred size, inherited.
	 * @return preferred size which is preset in code
	 *
	 */
	public Dimension getPreferredSize() { return size; }

	/**
	 * gets minimum size, inherited.
	 * @return minimum size which is preset in code
	 *
	 */
	public Dimension getMinimumSize() { return size; }

	/**
	 * @return the mGuiControl
	 */
	public GuiControl getmGuiControl() {
		return mGuiControl;
	}

	/**
	 * @param mGuiControl the mGuiControl to set
	 */
	public void setmGuiControl(GuiControl mGuiControl) {
		this.mGuiControl = mGuiControl;
	}



}
