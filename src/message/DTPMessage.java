/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import rina.cdap.impl.googleprotobuf.*;
import rina.cdap.impl.googleprotobuf.CDAP.objVal_t;
import rina.dtp.impl.googleprotobuf.*;

/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */

public class DTPMessage {
	/**
	 * version of the abstract syntax
	 */
	private static final int ABSTRACT_SYNTAX_VERSION = 0x0073;

	/**
	 * version of the DTP protocol
	 */
	private static final int DTP_VERSION = 0x0001;


	/**
	 * 
	 * @param destApName
	 * @param destApPortID
	 * @param srcApName
	 * @param srcApPortID
	 * @param payload
	 * @return message
	 */
	public static DTP.DTPMessage generateM_CDAP(

			String destApName, 
			int destApPortID,
			String srcApName,
			int srcApPortID,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_CDAP);

		dtpMessage.setDestIPCName(destApName);
		dtpMessage.setDestPortID(destApPortID);

		dtpMessage.setSrcIPCName(srcApName);
		dtpMessage.setSrcPortID(srcApPortID);

		dtpMessage.setPayload( ByteString.copyFrom (payload) );

		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	public static DTP.DTPMessage generateM_CDAP(

			String destApName, 
			String srcApName,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_CDAP);

		dtpMessage.setDestIPCName(destApName);


		dtpMessage.setSrcIPCName(srcApName);


		dtpMessage.setPayload( ByteString.copyFrom (payload) );

		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	public static DTP.DTPMessage generateM_DTP(

			String destApName, 
			String srcApName,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);

		dtpMessage.setDestIPCName(destApName);


		dtpMessage.setSrcIPCName(srcApName);


		dtpMessage.setPayload( ByteString.copyFrom (payload) );

		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	/**
	 * 
	 * @param destApPortID
	 * @param srcApPortID
	 * @param payload
	 * @return
	 */
	public static DTP.DTPMessage generateM_CDAP(

			int destApPortID,
			int srcApPortID,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_CDAP);
		dtpMessage.setDestPortID(destApPortID);
		dtpMessage.setSrcPortID(srcApPortID);
		dtpMessage.setPayload( ByteString.copyFrom (payload) );
		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}

	
	
	/**
	 * 
	 * @param destApName
	 * @param destApPortID
	 * @param srcApName
	 * @param srcApPortID
	 * @param payload
	 * @return message
	 */
	public static DTP.DTPMessage generateM_DTP(

			String destApName, 
			int destApPortID,
			String srcApName,
			int srcApPortID,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);

		dtpMessage.setDestIPCName(destApName);
		dtpMessage.setDestPortID(destApPortID);

		dtpMessage.setSrcIPCName(srcApName);
		dtpMessage.setSrcPortID(srcApPortID);

		dtpMessage.setPayload( ByteString.copyFrom (payload) );

		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}

	
	//////////////////////////////////////////////////////////////////////////////
	
	//this two methods are to wrap up the payload message of DTP message
	//basically from the payload, it can know whether it is a CDAP or DTP message
	//this is useful when multiplexing data to upper IPC(application)
	
	public static DTP.DTPMessage generatePayloadM_DTP( byte[] payload)
	{
		DTP.DTPMessage.Builder  dtpMessagePayload = DTP.DTPMessage.newBuilder();

		dtpMessagePayload.setOpCode(DTP.opCode_t.M_DTP);

		dtpMessagePayload.setPayload( ByteString.copyFrom (payload) );

		return  dtpMessagePayload.build();
	}
	
	public static DTP.DTPMessage generatePayloadM_CDAP( byte[] payload)
	{
		DTP.DTPMessage.Builder  cdapMessagePayload = DTP.DTPMessage.newBuilder();

		cdapMessagePayload.setOpCode(DTP.opCode_t.M_CDAP);

		cdapMessagePayload.setPayload( ByteString.copyFrom (payload) );

		return  cdapMessagePayload.build();
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param destApPortID
	 * @param srcApPortID
	 * @param payload
	 * @return
	 */
	public static DTP.DTPMessage generateM_DTP(
			int destApPortID,
			int srcApPortID,
			byte[] payload

	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);
		dtpMessage.setDestPortID(destApPortID);
		dtpMessage.setSrcPortID(srcApPortID);
		dtpMessage.setPayload( ByteString.copyFrom (payload) );
		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}

	
	public static DTP.DTPMessage generateM_DTP(String srcIPCName, int srcApPortID)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);
		dtpMessage.setSrcIPCName(srcIPCName);
		dtpMessage.setSrcPortID(srcApPortID);
		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	public static DTP.DTPMessage generateM_DTP(String srcIPCName)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);
		dtpMessage.setSrcIPCName(srcIPCName);
		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	public static DTP.DTPMessage generateM_DTP(

			String destApName, 
			String srcApName
	)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);

		dtpMessage.setDestIPCName(destApName);

		dtpMessage.setSrcIPCName(srcApName);

		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	
	public static DTP.DTPMessage generateM_DTP( int srcApPortID)
	{
		DTP.DTPMessage.Builder  dtpMessage = DTP.DTPMessage.newBuilder();

		dtpMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		dtpMessage.setOpCode(DTP.opCode_t.M_DTP);
		dtpMessage.setSrcPortID(srcApPortID);
		dtpMessage.setVersion(DTP_VERSION);

		return  dtpMessage.build();
	}
	


}
