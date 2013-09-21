/**
 * 
 */
package test;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.google.protobuf.ByteString;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dap.Application;
import vinea.impl.googleprotobuf.CAD;
import vinea.message.impl.CADMessageImpl;

/**
 * @author Flavio Esposito
 *
 */
public class TestApp extends Application {

	private CADMessageImpl cadMsgImpl = new CADMessageImpl();

	/**
	 * @param appName
	 * @param IDDName
	 */
	public TestApp(String appName, String IDDName, String dstName) {
		super(appName, IDDName);
		
		int sliceID = 1;
		
		
		CAD.CADMessage CADmessage = cadMsgImpl.generateCADMessage(
				sliceID,									// mandatory slice ID
				"test",						// SAD or MAD
				new LinkedHashMap<Integer,String>(),//(),
				new LinkedList<Double>(),// bidVector(),
				new LinkedHashMap<Integer,Long>(),//bidtime here
				new LinkedList<Integer>()// mVector	
				); 	
			
		
		
		
		
		
		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString CADByteString = ByteString.copyFrom(CADmessage.toByteArray());
		ObjValue.setByteval(CADByteString);
		//payload of the CDAP message 
		CDAP.objVal_t objvalueCAD = ObjValue.buildPartial();	
				
				
		

		CDAP.CDAPMessage M_WRITE = message.CDAPMessage.generateM_WRITE(
				"test ObjClass", //ObjClass
				"test ObjName", // ObjName,
				objvalueCAD, // objvalue
				dstName,//destAEInst,
				dstName,//destAEName, 
				dstName,//destApInst, 
				dstName,//destApName, 
				00001, //invokeID, 
				this.getAppName(),//srcAEInst
				this.getAppName(),//srcAEName
				this.getAppName(),//srcApInst
				this.getAppName()//srcApName
				);
		
		try {
			irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
			rib.RIBlog.debugLog("TestApp::TestApp: CDAP message with test payload sent to "+dstName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//send a dummy message to sp
		TestApp testa = new TestApp ("testapp","idd", "sp");
		
	}

}
