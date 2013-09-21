/**
 * 
 */
package dap.cad.util;

import com.google.protobuf.ByteString;

import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;

import rina.cdap.impl.googleprotobuf.CDAP;

/**
 * @author Flavio Esposito
 *
 */
public class TimeoutHandler {

	private SliceSpec.Slice.Builder _sliceToEmbed = null;
	private String _appName = null;


	/**
	 * 
	 */
	public TimeoutHandler(SliceSpec.Slice.Builder slice, String appName) {
		this._sliceToEmbed =slice;
		this._appName = appName;
	}

	

	/**
	 * no need to do this and congest the queue
	 * send a message to itself to trigger timeout
	 * @param dstName
	 */
	
	private void sendTimeoutAutoMessage(String dstName) {

		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();

		ByteString sliceByteString = ByteString.copyFrom(this._sliceToEmbed. build().toByteArray());
		ObjValue.setByteval(sliceByteString);
		CDAP.objVal_t objvalueSlice = ObjValue.buildPartial();


		//String dstName = "pnode1";

	//	int handle = this.irm.allocateFlow(this.getAppName(), dstName);

		//allocate a flow or get the handle of the previously allocated flow (RINA API)
		//we must have a flow to IDD, we are putting the messages to itself on this queue flow, to avoid wasting other resources 


		CDAP.CDAPMessage M_WRITE= message.CDAPMessage.generateM_WRITE(
				"timeout", //objclass
				"timeout",  // ObjName, //
				objvalueSlice, // objvalue
				dstName,//destAEInst,
				dstName,//destAEName, 
				dstName,//destApInst, 
				dstName,//destApName, 
				00001, //invokeID, 
				this._appName,//srcAEInst
				this._appName,//srcAEName
				this._appName,//srcApInst
				this._appName//srcApName
				);



		


		try {//send the message to itself to handle the timeout
			//irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
			//no need to encode for internal use
			//this.handleRequestTimeOut(M_WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
}
