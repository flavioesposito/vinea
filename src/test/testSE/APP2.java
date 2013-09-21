package test.testSE;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class APP2 extends Application {
	
	public APP2(String appName, String IDDName) {
		super(appName, IDDName);
		
	}
	

	
	public void handleAppCDAPmessage(byte[] msg) {
		
		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		this.rib.RIBlog.infoLog("00000000000000000000" + cdapMessage.getObjClass());
	}


	
	public static void main(String args[])
	{
		String file = "testSE.app2.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc2 = new IPCProcessImpl(config);
		
     	String IDDName = config.getIDDName();

     	
		APP2 test = new APP2("app2", IDDName);
		
		test.addUnderlyingIPC(ipc2);
		
		test.start();
     

//		IPCProcessImpl app2 = new IPCProcessImpl("app2", IDDName);
//
//		app2.addUnderlyingIPC(ipc2);
//		
//		app2.start();

		
	}

}
