package rina.dap;

import java.util.LinkedHashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.IPCProcessRMT;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingDaemon;

public class Application  extends Thread {

	private String appName = null;



	private String IDDName = null;

	public RIBImpl rib = null;

	private RIBDaemonImpl RIBdaemon = null;

	private MessageQueue dtpMsgQueue = null;

	private MessageQueue cdapMsgQueue = null;

	private LinkedHashMap<String, IPCProcessImpl> underlyingIPCs = null;

	public IRM irm = null;

	private MessageQueue ribDaemonQueue = null;

	private IPCProcessRMT ipcProcessRMT = null;

	private FlowAllocatorImpl flowAllocator = null;

	private LinkedHashMap<String, MessageQueue> upperIPCsDTPMsgQueue = null ;

	private LinkedHashMap<String, MessageQueue> upperIPCsCDAPMsgQueue = null;


	public Application(String appName, String IDDName)
	{
		this.appName = appName;
		this.IDDName = IDDName;

		this.rib = new RIBImpl();

		this.ribDaemonQueue = new MessageQueue();
		this.rib.addAttribute("ribDaemonQueue", this.ribDaemonQueue);

		this.rib.addAttribute("ipcName", this.appName);
		this.rib.addAttribute("iddName", this.IDDName);

		this.dtpMsgQueue = new MessageQueue();

		this.cdapMsgQueue = new  MessageQueue();

		this.underlyingIPCs = new LinkedHashMap<String, IPCProcessImpl>();



		this.irm = new IRM(this.appName,this.rib, this.underlyingIPCs, this.dtpMsgQueue, this.cdapMsgQueue);

		this.flowAllocator = new FlowAllocatorImpl(this.rib, this.dtpMsgQueue, this.irm);

		this.ipcProcessRMT = new IPCProcessRMT(this.flowAllocator, this.rib, this.dtpMsgQueue, this.cdapMsgQueue
				, this.upperIPCsDTPMsgQueue, this.upperIPCsCDAPMsgQueue);

		this.RIBdaemon = new RIBDaemonImpl(this.rib, this.irm);

		this.rib.setRibDaemon(this.RIBdaemon);



	}


	public void run()
	{

		while(true)
		{

			byte[] msg = this.cdapMsgQueue.getReceive();
			handleCDAPMsg(msg);

		}
	}


	private void handleCDAPMsg(byte[] msg) {

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.rib.RIBlog.infoLog("Application Process(" + this.appName +"): cdapMessage received, " +
				"\n opcode is " + cdapMessage.getOpCode() + 
				"\n src is " + cdapMessage.getSrcApName()+ 
				"\n objclass is " + cdapMessage.getObjClass()+
				"\n objName is " + cdapMessage.getObjName());


		if(cdapMessage.getObjClass().equals("flow") || cdapMessage.getObjClass().equals("relayService")
				|| cdapMessage.getObjClass().equals("createNewIPCForApp"))//for IRM
		{
			this.irm.getIrmQueue().addReceive(msg);
			return;
		}else if(cdapMessage.getObjClass().equals("PubSub"))
		{
			this.ribDaemonQueue.addReceive(msg);
			return;
		}else {

			handleAppCDAPmessage(msg);
		}
	}

	/**
	 * overwrite this class and implement your the handler 
	 * for your own application
	 * @param msg
	 */
	public void handleAppCDAPmessage(byte[] msg) {

		//		CDAP.CDAPMessage cdapMessage = null;
		//		try {
		//			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		//		} catch (InvalidProtocolBufferException e) {
		//			e.printStackTrace();
		//		}
		//		
		//		this.rib.RIBlog.infoLog("00000000000000000000" + cdapMessage.getObjClass());
	}


	public void addUnderlyingIPC( IPCProcessImpl ipc)
	{ 

		this.underlyingIPCs.put(ipc.getIPCName(), ipc);
		ipc.addUpperIPC(this.appName, this.dtpMsgQueue, this.cdapMsgQueue);
		this.irm.allocateFlow(this.appName, this.IDDName);
	}


	/**
	 * @return the appName
	 */
	public synchronized String getAppName() {
		return appName;
	}


	/**
	 * @param appName the appName to set
	 */
	public synchronized void setAppName(String appName) {
		this.appName = appName;
	}



}
