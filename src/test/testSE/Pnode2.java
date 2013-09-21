/**
 * 
 */
package test.testSE;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;


public class Pnode2 extends Application{

	public Pnode2(String appName, String IDDName) {
		super(appName, IDDName);
		
	}
	
	
	public void handleAppCDAPmessage(byte[] msg) {
		
		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		this.rib.RIBlog.infoLog("Pnode2: objectClass " + cdapMessage.getObjClass());
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "pnode2.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);
		
     	String IDDName = config.getIDDName();

     	
    	PnodeTest pnode2 = new PnodeTest("pnode", IDDName);
		
     	pnode2.addUnderlyingIPC(pnodeIPC);
		
     	pnode2.start();
     


		
	}

}
