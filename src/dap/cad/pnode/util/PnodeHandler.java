/**
 * this class is deprecated
 * @author flavioesposito
 */
package dap.cad.pnode.util;

import java.util.LinkedHashMap;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.ipcProcess.util.MessageQueue;
import rina.rib.impl.RIBDaemonImpl;

/**
 * @author flavio
 *
 */
public class PnodeHandler extends Thread{
	/**
	 * new peer 
	 */
	private String newPeer;
	
	
	/**
	 * message queue
	 */
	private  MessageQueue messageQueue = null;
	/**
	 * terminate this node
	 */
	private boolean PNODE_IS_ACTIVE = true;

	/**
	 * logger
	 */
	public RIBDaemonImpl ribDaemon= null;
	/**
	 * used to handle Pnode messages
	 * @param ribDaemon
	 * @param messageQueue
	 */
	public PnodeHandler(RIBDaemonImpl ribDaemon, MessageQueue messageQueue){

		this.messageQueue = messageQueue;
		this.ribDaemon = ribDaemon;
	
	}





	public void run(){ 

		
		while(PNODE_IS_ACTIVE)
		{

			byte[] msg = this.messageQueue.getReceive();
			handleReceivedMessage(msg);

		}

	}



	private void handleReceivedMessage(byte[] msg) {



		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.ribDaemon.localRIB.RIBlog.infoLog("PnodeHandler: message received opcode is  " + cdapMessage.getOpCode());


		switch(cdapMessage.getOpCode()){


		case M_READ:
			handle_M_READ(cdapMessage);
			break;		

		case M_READ_R:
			handle_M_READ_R(cdapMessage);
			break;
			
		case M_CREATE:

			handle_M_CREATE(cdapMessage);	
			break;

		
		case M_CREATE_R:

			handle_M_CREATE_R(cdapMessage);	
			break;


		case M_WRITE:
			handle_M_WRITE(cdapMessage);
			break;	

		case M_WRITE_R:
			handle_M_WRITE_R(cdapMessage);
			break;	

		case M_STOP:
			handle_M_STOP(cdapMessage);
			break;

		default:
			System.out.print("Something is wrong!!! opcpde not handled");
			break;
		}

	
	}



	private void handle_M_READ(CDAPMessage cdapMessage) {
	// TODO Auto-generated method stub
		
	}



	private void handle_M_READ_R(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub
		
	}



	private void handle_M_CREATE(CDAPMessage cdapMessage) {
		// node sends a request for a slice embedding
		
		
		
	}



	private void handle_M_CREATE_R(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub
		
	}



	private void handle_M_WRITE(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub
		
	}



	private void handle_M_WRITE_R(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub
		
	}



	private void handle_M_STOP(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub
		
	}

}
