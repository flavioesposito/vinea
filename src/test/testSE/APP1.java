package test.testSE;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;

public class APP1 extends Application {

	public APP1(String appName, String IDDName) {
		super(appName, IDDName);

	}


	//	public  void handleAppCDAPmessage(byte[] msg) {
	//		
	//
	//
	//		CDAP.CDAPMessage cdapMessage = null;
	//		try {
	//			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
	//		} catch (InvalidProtocolBufferException e) {
	//			e.printStackTrace();
	//		}
	//
	//		this.rib.RIBlog.infoLog(cdapMessage.getSrcApName());
	//
	//		
	//	}


	public static void main(String args[])
	{
		String file = "testSE.app1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc1 = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();


		APP1 test = new APP1("app1", IDDName);

		test.addUnderlyingIPC(ipc1);

		String dstName = "app2";

		int handle = test.irm.allocateFlow(test.getAppName(), dstName);



		CDAP.CDAPMessage M_READ = message.CDAPMessage.generateM_READ(
				"dummy",
				"dummy",
				dstName,//destAEInst
				dstName,//destAEName
				dstName,//destApInst
				dstName,//destApInst
				00001,  //invokeID, 
				test.getAppName(),//srcAEInst
				test.getAppName(),//srcAEName
				test.getAppName(),//srcApInst
				test.getAppName()//srcApName
		) ;	

		System.out.println("handle is " + handle);
		System.out.println("test.irm.getHandle(app2) " + test.irm.getHandle("app2"));


		try {
			test.irm.sendCDAP(test.irm.getHandle("app2"), M_READ.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//		IPCProcessImpl app1 = new IPCProcessImpl("app1", IDDName);
		//
		//		app1.addUnderlyingIPC(ipc1);
		//
		//		app1.start();
		//
		//		app1.getIrm().allocateFlow("app1", "app2");
		//
		//		String dstName = "app2";
		//		String srcName = "app1";
		//
		//		CDAP.CDAPMessage M_READ = message.CDAPMessage.generateM_READ(
		//				"dummy",
		//				"dummy",
		//				dstName,//destAEInst
		//				dstName,//destAEName
		//				dstName,//destApInst
		//				dstName,//destApInst
		//				00001,  //invokeID, 
		//				srcName,//srcAEInst
		//				srcName,//srcAEName
		//				srcName,//srcApInst
		//				srcName//srcApName
		//		) ;	
		//
		//
		//
		//		try {
		//			app1.getIrm().sendCDAP(app1.getIrm(). getHandle("app2"), M_READ.toByteArray());
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}

}
