/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.gui.panel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rina.config.RINAConfig;
import rina.dns.DNSProcess;
import rina.idd.IDDProcess;
import rina.ipcProcess.impl.IPCProcessImpl;
import vinea.gui.util.ConfigFromGUI;
import vinea.gui.util.MyFileChooser;


/**
 * @author Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 *
 */
public class CADSysPanel extends JPanel implements ActionListener {
	/**
	 * rina config file 
	 */
	private RINAConfig config =null;
	private Properties rinaProperties =null;
	private ControlPanel mControlPanel =null;
	private JComboBox mChoices, NMSSupportingDIFPolicy = null;
	private JPanel consolPanel, mChoicePanel, ioPanel,IDDPanel,NMSPanel,mPanel3, mPanel0 = null;
	private JButton  mLoadConfig,mSaveConfig,mApply;
	private JTextArea consolPanelText;
	private Date date = new Date();
	/**
	 * labels for input/output panel
	 */
	private JLabel mLabelDocumentsFile, mLabelEventsFile, mLabelOutputDir, mLabelNewKey,mLabelNewValue;
	private JTextField mTextConfigFile, mTextFieldEventsFile, mTextFieldOutputDir,mTextFieldNewKeyField,mTextFieldNewValueField;
	private JCheckBox mCheckBoxTraceLog,mCheckBoxEventLog,mCheckBoxDebugLog;
	private JLabel logoLabel;

	/**
	 * labels for IDD panel
	 */
	private JLabel mLabelIDDName,mLabelIDDPort,mLabelIDDServer;
	private JTextField mTextIDDName,mTextFieldIDDPort,mTextFieldIDDServer;

	/**
	 * labels for NMS panel
	 */
	private JLabel mLabelSuppDIF,mLabelNMSName,mLabelNMSPort,mLabelNMSServer, mLabelNMSUserName,mLabelNMSPWD;
	private JTextField mTextNMSName,mTextFieldNMSPort,mTextFieldNMSServer,mTextFieldNMSUserName,mTextFieldNMSPWD;

	private JCheckBox mCheckBoxAuthentication;

	private int current=0;
	private int iNMScurrentPolicy=0;
	private ConfigFromGUI configFromGUI;

	private MyFileChooser fileChooser = new MyFileChooser();

	private DNSProcess dns =null;
	private IDDProcess idd=null;

	/**
	 * Constructor
	 * @param p
	 * @param properties
	 */
	public CADSysPanel(ControlPanel mControlPanel, RINAConfig config) {

		this.configFromGUI = new ConfigFromGUI();
		this.config= config;
		this.rinaProperties=config.getRinaProperties();
		this.mControlPanel=mControlPanel;


		//Create and initialize the buttons.
		final JButton saveButton = new JButton("Save");
		saveButton.setActionCommand("Save");
		saveButton.addActionListener(this);
		//getRootPane().setDefaultButton(saveButton);

		final JButton applyButton = new JButton("Apply");
		applyButton.setActionCommand("Apply");
		applyButton.addActionListener(this);


		setBorder(new javax.swing.border.TitledBorder("RINA Component Properties:"));




		String path="";
		try {
			path = new java.io.File(".").getCanonicalPath();
			System.out.println("---path: "+path);
		} catch (IOException e) {
			e.printStackTrace();
		}


		ImageIcon icon = createImageIcon("/images/RINAlogo2.jpg");

		logoLabel = new JLabel(icon);

		//        logoLabel.setBounds(10, 0, 160, 20);
		//        int h = logoLabel.getHeight();
		//       System.out.println("h: "+h);
		//      int w = logoLabel.getWidth();
		//       System.out.println("w: "+w);
		//Set the position of the text, relative to the icon:
		//       logoLabel.setVerticalTextPosition(JLabel.BOTTOM);
		//      logoLabel.setHorizontalTextPosition(JLabel.CENTER);
		//        logoLabel.setSize(200, 200);
		logoLabel.setOpaque(true);

		this.add(logoLabel);





		//String[] testingScenario = { "Enrollment", "Recursive IPC", "Legacy Client", "Your DEMO here" };
		String[] testingScenario = { "DNS", "IDD", "NMS1", "NMS2", "IPC1-level0", "IPC2-level0",
				"IPCA-level1", "IPCB-level1", "IPCC-level1", "IPCD-level1", "Your IPC" };

		mChoices = new JComboBox(testingScenario);
		mChoices.setSelectedIndex(0);
		mChoices.setSize(40, 20);
		mChoices.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {


				if ("comboBoxChanged".equals(evt.getActionCommand())) {
					//Update the icon to display the new phase
					if(current==mChoices.getSelectedIndex())
						return;

					if(0==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						//	Enrollment enrollment = new Enrollment();
						System.out.println("DNS selected from GUI");
					}
					else if(1==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IDD selected from GUI");
					}
					else if(2==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("NMS1 selected from GUI");
					}
					else if(3==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("NMS2 selected from GUI");
					}
					else if(4==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPC1-level0 selected from GUI");
					}
					else if(5==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPC2-level0 selected from GUI");
					}
					else if(6==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPCA-level1 selected from GUI");
					}
					else if(7==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPCB-level1 selected from GUI");
					}
					else if(8==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPCC-level1 selected from GUI");
					}
					else if(9==mChoices.getSelectedIndex()){
						current=mChoices.getSelectedIndex();
						System.out.println("IPCD-level1 selected from GUI");
					}
					else{
						JOptionPane.showMessageDialog(getmControlPanel().getmGuiControl()
								.getGui().getFrame(),"Please implement here your IPC first!", "Not Implemented!", 
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}


			}
		});



		mChoicePanel=new JPanel();
		mChoicePanel.setLayout(null);
		//mChoicePanel.setLayout(new BoxLayout(mChoicePanel, BoxLayout.LINE_AXIS));
		//mChoicePanel.add(Box.createHorizontalGlue());
		JLabel l=new JLabel("Select RINA Component:");
		l.setBounds(0, 10, 180, 20); 
		mChoices.setBounds(180, 10, 110, 18);

		mChoicePanel.add(l);
		mChoicePanel.add(mChoices);
		mChoicePanel.setPreferredSize(new Dimension(295,32));
		mChoicePanel.setMinimumSize(new Dimension(295,22));

		add(mChoicePanel);

		//Create a container so that we can add a title around
		//the scroll pane.  Can't add a title directly to the
		//scroll pane because its background would be white.
		//Lay out the label and scroll pane from top to bottom.


		JPanel optionPane = new JPanel();
		//optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.PAGE_AXIS));



		JTabbedPane tabbedPane = new JTabbedPane();

		JComponent panel1 = makeIOPanel();
		panel1.setPreferredSize(new Dimension(395, 150));
		tabbedPane.addTab("Input/Output", panel1);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_I);

		JComponent panel2 = makeIDDPanel();
		panel2.setPreferredSize(new Dimension(195, 180));
		tabbedPane.addTab("IDD Configuration", panel2);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_D);

		JComponent panel3 = makeNMSPanel();
		panel3.setPreferredSize(new Dimension(295, 200));
		tabbedPane.addTab("NMS Configuration", panel3);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_N);
		//Uncomment the following line to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);


		//Add the tabbed pane to this panel.
		optionPane.add(tabbedPane);
		add(optionPane);

		// add button 
		mPanel0 = new JPanel();
		mPanel0.setMinimumSize(new Dimension(520, 265));
		mPanel0.setPreferredSize(new Dimension(320, 65));
		mPanel0 = makeButtonPanel();
		//add(mPanel0);


		//make console
		//	consolPanel =new JPanel();
		//	consolPanelText =new JTextArea();
		//	consolPanelText = MakeConsolPanel();
		//	add(consolPanelText);
		//add(consolPanel);


	}
	/**
	 * generate components of NMS panel tab
	 * @return NMS Panel
	 */
	protected JComponent makeNMSPanel() {

		NMSPanel=new JPanel();
		//ioPanel.setBorder(new javax.swing.border.TitledBorder("Input/Output"));
		NMSPanel.setLayout(null);

		mLabelSuppDIF = new JLabel("Supporting DIF Policy:");
		mLabelSuppDIF.setBounds(10, 10, 180, 20);

		String[] supportingDIFPolicy = { "firstAvailable", "LeastCost" };

		NMSSupportingDIFPolicy = new JComboBox(supportingDIFPolicy);
		NMSSupportingDIFPolicy.setSelectedIndex(0);
		//NMSSupportingDIFPolicy.setSize(120, 20);
		NMSSupportingDIFPolicy.setBounds(170, 10, 125, 20);

		NMSSupportingDIFPolicy.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {

				if ("comboBoxChanged".equals(evt.getActionCommand())) {
					//Update the icon to display the new phase
					if(iNMScurrentPolicy==NMSSupportingDIFPolicy.getSelectedIndex())
						return;

					if(0==mChoices.getSelectedIndex()){
						iNMScurrentPolicy=NMSSupportingDIFPolicy.getSelectedIndex();
						updateNMScurrentPolicy();
					}
					else{
						System.out.println("mChoices.getSelectedIndex(): "+mChoices.getSelectedIndex());
						JOptionPane.showMessageDialog(getmControlPanel().getmGuiControl()
								.getGui().getFrame(),"Not implemented yet!", "Error!", 
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

			}

			private void updateNMScurrentPolicy() {
				config.setProperty("rina.dif1.supportingDIFPolicy", "firstAvailable");//fix it for every dif

			}
		});


		mLabelNMSName = new JLabel("NMS Name or IP:");
		mLabelNMSName.setBounds(10, 35, 120, 20);
		mTextNMSName = new JTextField();
		mTextNMSName.setBounds(140, 35, 140, 20);
		mTextNMSName.setText("localhost");

		mLabelNMSPort = new JLabel("local port:");
		mLabelNMSPort.setBounds(10, 60, 110, 20);
		mTextFieldNMSPort = new JTextField();
		mTextFieldNMSPort.setBounds(140, 60, 140, 20);
		mTextFieldNMSPort.setText("1111");

		mLabelNMSServer= new JLabel("Server Listening:");
		mLabelNMSServer.setBounds(10, 85, 140, 20);        
		mTextFieldNMSServer = new JTextField();
		mTextFieldNMSServer.setBounds(140, 85, 100, 20);
		mTextFieldNMSServer.setText("true");

		mLabelNMSUserName= new JLabel("User Name:");
		mLabelNMSUserName.setBounds(10, 110, 110, 20);        
		mTextFieldNMSUserName = new JTextField();
		mTextFieldNMSUserName.setBounds(120, 110, 100, 20);
		mTextFieldNMSUserName.setText("BU");


		mLabelNMSPWD= new JLabel("Password:");
		mLabelNMSPWD.setBounds(10, 130, 110, 20);        
		mTextFieldNMSPWD = new JTextField();
		mTextFieldNMSPWD.setBounds(120, 130, 100, 20);
		mTextFieldNMSPWD.setText("BU");

		//	mLabelBoxAuthentication= new JLabel("Authentication:");
		//	mLabelBoxAuthentication.setBounds(10, 150, 110, 20); 

		mCheckBoxAuthentication = new JCheckBox("Require Authentication", true);
		mCheckBoxAuthentication.setMnemonic(KeyEvent.VK_R);
		mCheckBoxAuthentication.setBounds(10, 165, 200, 20);





		NMSPanel.add(mLabelSuppDIF);
		NMSPanel.add(NMSSupportingDIFPolicy);

		NMSPanel.add(mLabelNMSName);
		NMSPanel.add(mTextNMSName);

		NMSPanel.add(mLabelNMSPort);
		NMSPanel.add(mTextFieldNMSPort);

		NMSPanel.add(mLabelNMSServer);
		NMSPanel.add(mTextFieldNMSServer);

		NMSPanel.add(mLabelNMSUserName);
		NMSPanel.add(mTextFieldNMSUserName);

		NMSPanel.add(mLabelNMSPWD);
		NMSPanel.add(mTextFieldNMSPWD);

		//	NMSPanel.add(mLabelBoxAuthentication);
		NMSPanel.add(mCheckBoxAuthentication);




		return NMSPanel;


	}

	/**
	 * 
	 * @return IDD panel
	 */
	protected JComponent makeIDDPanel() {

		IDDPanel=new JPanel();
		//ioPanel.setBorder(new javax.swing.border.TitledBorder("Input/Output"));
		IDDPanel.setLayout(null);

		mLabelIDDName = new JLabel("IDD Name or IP:");
		mLabelIDDName.setBounds(10, 20, 110, 20);
		mTextIDDName = new JTextField();
		mTextIDDName.setBounds(120, 20, 140, 20);
		mTextIDDName.setText("localhost");

		mLabelIDDPort = new JLabel("local port:");
		mLabelIDDPort.setBounds(10, 45, 110, 20);
		mTextFieldIDDPort = new JTextField();
		mTextFieldIDDPort.setBounds(120, 45, 140, 20);
		mTextFieldIDDPort.setText("2222");

		mLabelIDDServer= new JLabel("Server Listening:");
		mLabelIDDServer.setBounds(10, 70, 110, 20);        
		mTextFieldIDDServer = new JTextField();
		mTextFieldIDDServer.setBounds(120, 70, 140, 20);
		mTextFieldIDDServer.setText("true");

		IDDPanel.add(mLabelIDDName);
		IDDPanel.add(mTextIDDName);
		IDDPanel.add(mLabelIDDPort);
		IDDPanel.add(mTextFieldIDDPort);
		IDDPanel.add(mLabelIDDServer);
		IDDPanel.add(mTextFieldIDDServer);

		return IDDPanel;
	}

	protected JPanel makeButtonPanel(){

		mPanel0 = new JPanel();
		mPanel0.setLayout(null);

		mPanel3 = new JPanel();
		mPanel3.setLayout(new BoxLayout(mPanel3, BoxLayout.LINE_AXIS));

		mLoadConfig = new JButton("Load");
		mLoadConfig.setEnabled(true);
		mLoadConfig.addActionListener(this);
		mLoadConfig.setMnemonic(KeyEvent.VK_A);		



		mSaveConfig = new JButton("Save");
		mSaveConfig.setEnabled(true);
		mSaveConfig.addActionListener(this);
		mLoadConfig.setMnemonic(KeyEvent.VK_V);		


		mApply = new JButton("Apply");
		mApply.setEnabled(true);
		mApply.addActionListener(this);
		mLoadConfig.setMnemonic(KeyEvent.VK_Y);		


		mPanel3.add(mLoadConfig);
		mPanel3.add(mSaveConfig);
		mPanel3.add(mApply);

		mPanel3.setMinimumSize(new Dimension(220, 20));
		mPanel3.setPreferredSize(new Dimension(220, 20));

		add(mPanel0);
		add(mPanel3);
		return mPanel3;
	}
	/**
	 * 
	 * @return ioPanel
	 */
	protected JComponent makeIOPanel() {

		ioPanel=new JPanel();
		//ioPanel.setBorder(new javax.swing.border.TitledBorder("Input/Output"));
		ioPanel.setLayout(null);

		mLabelDocumentsFile = new JLabel("Configuration File:");
		mLabelDocumentsFile.setBounds(10, 20, 140, 20);
		mTextConfigFile = new JTextField();
		mTextConfigFile.setBounds    (150, 20, 140, 20);
		mTextConfigFile.setText("rina.properties"); 
		mTextConfigFile.setToolTipText("Write path to your config file.");


		mLabelEventsFile = new JLabel("Events File:");
		mLabelEventsFile.setBounds     (10, 45, 110, 20);
		mTextFieldEventsFile = new JTextField();
		mTextFieldEventsFile.setBounds(150, 45, 140, 20);
		mTextFieldEventsFile.setText("events.txt");
		mTextFieldEventsFile.setToolTipText("Write path to your event file.");

		mLabelOutputDir = new JLabel("Output Dir:");
		mLabelOutputDir.setBounds    (10, 70, 110, 20);        
		mTextFieldOutputDir = new JTextField();
		mTextFieldOutputDir.setBounds(150, 70, 140, 20);
		mTextFieldOutputDir.setText("output");
		mTextFieldOutputDir.setToolTipText("Write path to your output directory.");

		// insert new (key,value )from gui
		mLabelNewKey = new JLabel("New Key:"); 
		mLabelNewKey.setBounds          (10, 105, 110, 20);        
		mTextFieldNewKeyField = new JTextField();
		mTextFieldNewKeyField.setBounds(120, 105, 140, 20);
		mTextFieldNewKeyField.setText("rina.sKey");
		mTextFieldNewKeyField.setToolTipText("Write new key (config file field must be set).");


		mLabelNewValue = new JLabel("New Value:"); 
		mLabelNewValue.setBounds         (10, 130, 110, 20);        
		mTextFieldNewValueField = new JTextField();
		mTextFieldNewValueField.setBounds(120, 130, 140, 20);
		mTextFieldNewValueField.setText("sValue");
		mTextFieldNewValueField.setToolTipText("Write new value (config file field must be set).");



		mCheckBoxTraceLog = new JCheckBox("Trace Log", true);
		mCheckBoxTraceLog.setMnemonic(KeyEvent.VK_T);
		mCheckBoxTraceLog.setBounds(10, 160, 85, 20);

		mCheckBoxEventLog = new JCheckBox("Event Log", true);
		mCheckBoxEventLog.setMnemonic(KeyEvent.VK_E);
		mCheckBoxEventLog.setBounds(100, 160, 85, 20);

		mCheckBoxDebugLog = new JCheckBox("Debug Log", true);
		mCheckBoxDebugLog.setMnemonic(KeyEvent.VK_D);
		//	mCheckBoxDebugLog.setBounds(190, 95, 85, 20);
		mCheckBoxDebugLog.setBounds(190, 160, 85, 20);



		ioPanel.add(mLabelDocumentsFile);
		ioPanel.add(mTextConfigFile);
		ioPanel.add(mLabelEventsFile);
		ioPanel.add(mTextFieldEventsFile);
		ioPanel.add(mLabelOutputDir);
		ioPanel.add(mTextFieldOutputDir);

		ioPanel.add(mLabelNewKey);
		ioPanel.add(mTextFieldNewKeyField);

		ioPanel.add(mLabelNewValue);
		ioPanel.add(mTextFieldNewValueField);


		ioPanel.add(mCheckBoxTraceLog);
		ioPanel.add(mCheckBoxEventLog);
		ioPanel.add(mCheckBoxDebugLog);


		return ioPanel;

	}

	//not used for now
	protected JTextArea MakeConsolPanel(){
		//Create a text area.
		JTextArea textArea = new JTextArea(
				"This is an editable JTextArea. " +
				"A text area is a \"plain\" text component, " +
				"which means that although it can display text " +
				"in any font, all of the text is in the same font."
		);
		textArea.setFont(new Font("Serif", Font.ITALIC, 16));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder("Plain Text"),
								BorderFactory.createEmptyBorder(5,5,5,5)),
								areaScrollPane.getBorder()));

		return textArea;
	}




	/**
	 * Handle clicks on the Set and Cancel buttons.
	 * @param e action event
	 */
	public void actionPerformed(ActionEvent e) { 
		if ("Load".equals(e.getActionCommand())) {
			fileChooser.setFileFilter(MyFileChooser.configFilter);
			int res = fileChooser.showOpenDialog(getParent());
			if (res == JFileChooser.CANCEL_OPTION)
				return;
			File file = fileChooser.getSelectedFile();
			loadConfig(file);

			this.mTextConfigFile.setText(file.getName());

		} else if ("Save".equals(e.getActionCommand())) {
			fileChooser.setFileFilter(MyFileChooser.configFilter);
			int res = fileChooser.showSaveDialog(getParent());
			if (res == JFileChooser.CANCEL_OPTION)
				return;
			File file = fileChooser.getSelectedFile();
			saveConfig(file);
		}

		else if ("Apply".equals(e.getActionCommand())) {
			apply();
		}
	}



	/**
	 * save Config file
	 * @param file
	 */
	public void saveConfig(File file) {
		this.configFromGUI.saveConfToFileGUI(file, rinaProperties, getmControlPanel().getmGuiControl().getGui().getFrame());
	}
	/**
	 * Load config file
	 * @param file
	 */
	public void loadConfig(File file) {
		this.configFromGUI.loadConfFromFileGUI(file, rinaProperties, getmControlPanel().getmGuiControl().getGui().getFrame());
	}

	/**
	 * retrieve from config file default values for the gui
	 */
	public void retrieve(){

		mTextConfigFile.setText(rinaProperties.getProperty("rina.config"));
		mTextFieldEventsFile.setText(rinaProperties.getProperty("rina.EventsFile"));
		mTextFieldOutputDir.setText(rinaProperties.getProperty("rina.OutputDirectory"));
		mTextFieldOutputDir.setText(rinaProperties.getProperty("rina.OutputDir"));


		if(rinaProperties.contains("rina.TraceLog")){
			if(rinaProperties.getProperty("rina.TraceLog").compareToIgnoreCase("True")==0)
				mCheckBoxTraceLog.setSelected(true);
			else 
				mCheckBoxTraceLog.setSelected(false);
		}
		if(rinaProperties.contains("rina.EventLog")){
			if(rinaProperties.getProperty("rina.EventLog").compareToIgnoreCase("True")==0)
				mCheckBoxEventLog.setSelected(true);
			else 
				mCheckBoxEventLog.setSelected(false);
		}
		if(rinaProperties.contains("rina.EventLog")){
			if(rinaProperties.getProperty("rina.DebugLog").compareToIgnoreCase("True")==0)
				mCheckBoxDebugLog.setSelected(true);
			else 
				mCheckBoxDebugLog.setSelected(false);
		}

		if(rinaProperties.contains("rina.NMS_NAME")){		
			mTextNMSName.setText(rinaProperties.getProperty("rina.NMS_NAME"));
		}
		if(rinaProperties.contains("rina.NMSlocalPort")){
			mTextFieldNMSPort.setText(rinaProperties.getProperty("rina.NMSlocalPort"));
		}
		if(rinaProperties.contains("rina.NMS_LISTENING")){
			mTextFieldNMSServer.setText(rinaProperties.getProperty("rina.NMS_LISTENING"));
		}
		if(rinaProperties.contains("rina.user")){
			mTextFieldNMSUserName.setText(rinaProperties.getProperty("rina.user"));
		}
		if(rinaProperties.contains("rina.pwd")){
			mTextFieldNMSPWD.setText(rinaProperties.getProperty("rina.pwd"));
		}


		if(NMSSupportingDIFPolicy.getSelectedIndex()==0){
			rinaProperties.setProperty("rina.supportingDIF", "firstAvailable");
		}


		if(rinaProperties.contains("rina.auth")){

			if(rinaProperties.getProperty("rina.auth").compareToIgnoreCase("True")==0)
				mCheckBoxAuthentication.setSelected(true);
			else 
				mCheckBoxAuthentication.setSelected(false);
		}

	}

	/**
	 * apply configuration file loaded
	 */
	private void apply() {

		rinaProperties.setProperty("Configuration File:", mTextConfigFile.getText());
		rinaProperties.setProperty("rina.EventsFile", mTextFieldEventsFile.getText());		
		rinaProperties.setProperty("rina.OutputDirectory", mTextFieldOutputDir.getText());
		rinaProperties.setProperty("rina.OutputDir", mTextFieldOutputDir.getText());



		rinaProperties.setProperty("rina.TraceLog", Boolean.toString(mCheckBoxTraceLog.isSelected()));
		rinaProperties.setProperty("rina.EventLog", Boolean.toString(mCheckBoxEventLog.isSelected()));
		rinaProperties.setProperty("rina.DebugLog", Boolean.toString(mCheckBoxDebugLog.isSelected()));



		rinaProperties.setProperty("rina.NMS_NAME", mTextNMSName.getText());
		if(NMSSupportingDIFPolicy.getSelectedIndex()==0){
			rinaProperties.setProperty("rina.supportingDIF", "firstAvailable");
		}
		rinaProperties.setProperty("rina.NMSlocalPort", mTextFieldNMSPort.getText());
		rinaProperties.setProperty("rina.NMS_LISTENING", mTextFieldNMSServer.getText());


		rinaProperties.setProperty("rina.user", mTextFieldNMSUserName.getText());
		rinaProperties.setProperty("rina.pwd", mTextFieldNMSPWD.getText());


		rinaProperties.setProperty("rina.auth", Boolean.toString(mCheckBoxAuthentication.isSelected()));

		writeonConfigFile(mTextFieldNewKeyField.getText(),mTextFieldNewValueField.getText());



	}

	/**
	 * write new keys and value on Configuration File
	 * @param new key to be written on the config file
	 * @param new value to be written on the config file
	 */
	private void writeonConfigFile(String key, String value) {


		try{
			// load file from gui
			FileWriter configfile = new FileWriter(mTextConfigFile.getText(),true);
			BufferedWriter out = new BufferedWriter(configfile);
			//write on file new keys and value
			out.write("# Property add from GUI on: "+date.toString()+" \n");
			out.write(key+"="+value+"\n\n");
			System.out.println("Entry: "+key+", "+value+" written on "+mTextConfigFile.getText());
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}


	}
	/**
	 * @return the rinaProperties
	 */
	public Properties getRinaProperties() {
		return rinaProperties;
	}

	/**
	 * @param rinaProperties the rinaProperties to set
	 */
	public void setRinaProperties(Properties rinaProperties) {
		this.rinaProperties = rinaProperties;
	}

	/**
	 * @return the mControlPanel
	 */
	public ControlPanel getmControlPanel() {
		return mControlPanel;
	}

	/**
	 * @param mControlPanel the mControlPanel to set
	 */
	public void setmControlPanel(ControlPanel mControlPanel) {
		this.mControlPanel = mControlPanel;
	}
	/**
	 * @return the mTextConfigFile
	 */
	public synchronized JTextField getmTextConfigFile() {
		return mTextConfigFile;


	}
	/**
	 * @param mTextConfigFile the mTextConfigFile to set
	 */
	public synchronized void setmTextConfigFile(JTextField mTextConfigFile) {
		this.mTextConfigFile = mTextConfigFile;
	}

	/** 
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 */
	protected ImageIcon createImageIcon(String path){
		//String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	/**
	 * Button handler - RUN
	 */
	public void start() {
		//get the action id and perform the action
		if(0==mChoices.getSelectedIndex()){

			dns = new DNSProcess(config);
			dns.start();
			System.out.println("DNS Running...");
		}
		else if(1==mChoices.getSelectedIndex()){


			String file = "idd.properties";
			RINAConfig config = new RINAConfig(file);
			idd = new IDDProcess(config);

			System.out.println("IDD Running...");
		}
		else if(2==mChoices.getSelectedIndex()){

			String file = "nms1.properties";
			RINAConfig config = new RINAConfig(file);
			//NMSProcess0 process = new NMSProcess0(config);

			System.out.println("NMS1 Running...");
		}
		else if(3==mChoices.getSelectedIndex()){

			String file = "nms2.properties";
			RINAConfig config = new RINAConfig(file);;
			//NMSProcess0 process = new NMSProcess0(config);

			System.out.println("NMS2 Running...");
		}
		else if(4==mChoices.getSelectedIndex()){

			//RINAConfig config = new RINAConfig("rina.properties", "dif2");
			//IPCProcess0Impl process = new IPCProcess0Impl(config, "oruno.bu.edu", "dif2", "BU", "BU"); //fixme
			//process.start();

			System.out.println("IPC1-level0 Running...");
		}
		else if(5==mChoices.getSelectedIndex()){
			System.out.println("IPC2-level0 sRunning...");

			//RINAConfig config = new RINAConfig("rina.properties", "dif2");
			//IPCProcess0Impl process = new IPCProcess0Impl(config, "iberate.bu.edu", "dif2", "BU", "BU");	     
			//process.start();

		}
		else if(6==mChoices.getSelectedIndex()){
			System.out.println("IPCA-level1 Running...");

			//RINAConfig config = new RINAConfig("ipcA.properties", "dif1");
			//IPCProcess0Impl l0Process1 = new IPCProcess0Impl(config, "ipc1", "dif1", "BU", "BU");
			//l0Process1.start();

			//IPCProcessImpl l1ProcesssA= new IPCProcessImpl(config,"l1ProcesssA");
			//l1ProcesssA.addUnderlyingIPC(l0Process1);
			//l1ProcesssA.start();

			for(int i = 0; i < 5;i++)
			{
				//l1ProcesssA.getNeighbours();
				//l1ProcesssA.broadcastFowardingTable();
				//ForkIPCA.sleep(2);
			}
			//System.out.println(" forwarding table is  " +  l1ProcesssA.RIBdaemon.localRIB.getForwardingTable());
			//ForkIPCA.sleep(10);
			//l1ProcesssA.enrollment("BUCS", "BUCS", "BUCSDIF");


		}
		else if(7==mChoices.getSelectedIndex()){
			System.out.println("IPCB-level1 Running...");

			//RINAConfig config1 = new RINAConfig("ipcB.properties", "dif1");
			//IPCProcess0Impl l0Process1 = new IPCProcess0Impl(config1, "ipc2", "dif1", "BU", "BU");
			//l0Process1.start();

			//RINAConfig config2 = new RINAConfig("ipcB.properties", "dif2");
			//IPCProcess0Impl l0Process2 = new IPCProcess0Impl(config2, "ipc3", "dif2", "BU", "BU");
			//l0Process2.start();
			//IPCProcessImpl l1ProcesssB= new IPCProcessImpl(config1,"l1ProcesssB");

			//l1ProcesssB.addUnderlyingIPC(l0Process1);
			//l1ProcesssB.addUnderlyingIPC(l0Process2);

			//  l1ProcesssB.initialize();
			//l1ProcesssB.start();

			for(int i = 0; i < 5;i++)
			{
			//	l1ProcesssB.getNeighbours();
			//	l1ProcesssB.broadcastFowardingTable();
			//	ForkIPCB.sleep(2);
			}



		}
		else if(8==mChoices.getSelectedIndex()){
			//	System.out.println("IPCC-level1 Running...");
			System.out.println("not implemented yet...");
		}
		else if(9==mChoices.getSelectedIndex()){
			System.out.println("IPCD-level1 Running...");


		/*	RINAConfig config = new RINAConfig("ipcD.properties", "dif2");
			IPCProcess0Impl l0Process1 = new IPCProcess0Impl(config, "ipc4", "dif2", "BU", "BU");
			l0Process1.start();
			IPCProcessImpl l1ProcesssD= new IPCProcessImpl(config,"l1ProcesssD");
			l1ProcesssD.addUnderlyingIPC(l0Process1);

			l1ProcesssD.initNMS("BUCS", "BUCS", "BUCSDIF");
			l1ProcesssD.start();*/

			for(int i = 0; i < 5;i++)
			{
			//	l1ProcesssD.getNeighbours();
			//	l1ProcesssD.broadcastFowardingTable();
			//	ForkIPCD.sleep(2);
			}



		}

	}
	/**
	 * Button handler - STOP
	 */
	public void stop() {

		if(0==mChoices.getSelectedIndex()){
			
			dns.stopDNS();
			dns = null;
			//System.out.println("DNS stopped.");
	
		}
		else if(1==mChoices.getSelectedIndex()){
			
		}

	}


}
