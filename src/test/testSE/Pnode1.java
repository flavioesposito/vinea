/**
 * 
 */
package test.testSE;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;


import rina.cdap.impl.googleprotobuf.CDAP;
import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;
import vinea.slicespec.impl.googleprotobuf.SliceSpec;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;


public class Pnode1 extends Application{

	public Pnode1(String appName, String IDDName) {
		super(appName, IDDName);

	}


	public void handleAppCDAPmessage(byte[] msg) {

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}


		rib.RIBlog.infoLog("0000000 Pnode 1 0000000000 ObjClass" + cdapMessage.getObjClass());
		rib.RIBlog.infoLog("0000000 Pnode 1 0000000000 ObjName" + cdapMessage.getObjName());
		rib.RIBlog.infoLog("0000000 Pnode 1 0000000000 SrcAEName" + cdapMessage.getSrcAEName());


		Slice sliceRequested = null;

		if(cdapMessage.getObjClass() == "slice")
		{


			try {
				sliceRequested = SliceSpec.Slice.parseFrom(cdapMessage.getObjValue().getByteval());
			} catch (InvalidProtocolBufferException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	



			this.nodeBidding(sliceRequested);
		}

	}




	/**
	 * physical node bidding
	 * @param sliceRequested
	 */
	private void nodeBidding(Slice sliceRequested) {
		
		this.rib.RIBlog.infoLog("CADlog: NODE BIDDING PHASE started from pNode: "+this.getAppName()+
								" \n for sliceName: "+ sliceRequested.getName()+
								" \n slice topology: "+sliceRequested.getTopology()+
								" \n slice id: "+	   sliceRequested.getSliceID());
		

		List<vNode> vnodeList = sliceRequested.getVirtualnodeList();
		rib.RIBlog.infoLog("vnodeList: "+vnodeList);
		
		

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "pnode1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();


		Pnode1 pnode = new Pnode1("pnode1", IDDName);

		pnode.addUnderlyingIPC(pnodeIPC);

		pnode.start();



	}

}
