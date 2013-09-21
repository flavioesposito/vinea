/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package message;

import com.google.protobuf.InvalidProtocolBufferException;
import rina.cdap.impl.googleprotobuf.*;
import rina.cdap.impl.googleprotobuf.CDAP.objVal_t;


/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */

public class CDAPMessage {

	/**
	 * version of the abstract syntax
	 */
	private static final int ABSTRACT_SYNTAX_VERSION = 0x0073;

	/**
	 * version of the CDAP protocol
	 */

	private static final int CDAP_VERSION = 0x0001;
	/**
	 * dummy Constructor
	 */
	public CDAPMessage(){}

	/**
	 * generate AuthValue
	 * @param username
	 * @param password
	 * @return authValue
	 */
	public static CDAP.authValue_t generateAuthValue(String username, String password)
	{
		CDAP.authValue_t.Builder authValue = CDAP.authValue_t.newBuilder();
		authValue.setAuthName(username);
		authValue.setAuthPassword(password);
		return authValue.buildPartial();

	}

	/**
	 * generate M_CONNECT
	 * @param authMech
	 * @param authValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CONNECT(
			CDAP.authTypes_t authMech,
			CDAP.authValue_t authValue, 
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName
	)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setAuthMech(authMech);
		cdapMessage.setAuthValue(authValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * 
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CONNECTToEduard(

			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 

			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName

	)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(115);
		cdapMessage.setAuthMech(CDAP.authTypes_t.AUTH_NONE);
		cdapMessage.setObjInst(0);
		//	cdapMessage.setAuthValue(authValue);

		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);

		cdapMessage.setInvokeID(1);

		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT);

		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);

		cdapMessage.setScope(0);

		cdapMessage.setVersion(1);
		cdapMessage.setResult(0);


		return  cdapMessage.buildPartial();
	}


	/**
	 * generate M_CONNECT_R
	 * @param resultValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CONNECT_R(
			int resultValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName, 
			int invokeID, 
			String srcAEInst, 
			String srcAEName, 
			String srcApInst,
			String srcApName
	)
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setResult(resultValue);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CONNECT_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.build();
	}


	/**
	 * generate M_CREATE
	 * @param ObjClass
	 * @param ObjName
	 * @param objValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}

	/**
	 * 
	 * @param ObjClass
	 * @param ObjName
	 * @param objValue
	 * @param destApInst
	 * @param destApName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			String destApInst,
			String destApName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);

		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}

	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			String destApInst,
			String destApName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);

		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}
	/**
	 * 
	 * @param ObjClass
	 * @param ObjName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}

	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			String ObjName,
			String destAEName, 
			String destApName,
			String srcAEName,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}

	public static CDAP.CDAPMessage generateM_CREATE(
			String ObjClass,
			int resultValue,
			String destAEName, 
			String destApName,
			String srcAEName,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setResult(resultValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}


	public static CDAP.CDAPMessage generateM_CREATE_R(
			String ObjClass,
			String ObjName,
			String destAEName, 
			String destApName,
			String srcAEName,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}


	/**
	 * generate M_CREATE_R
	 * @param result
	 * @param ObjClass
	 * @param ObjName
	 * @param objValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			String destApInst,
			String destApName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * 
	 * @param result
	 * @param ObjClass
	 * @param ObjName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String ObjClass,
			String ObjName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}
	/**
	 * generate M_CREATE_R
	 * @param result
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_CREATE_R(
			int result,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CREATE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}


	/**
	 * generate M_READ
	 * @param ObjClass
	 * @param objName
	 * @param ObjValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_READ(
			String ObjClass,
			String objName,
			CDAP.objVal_t ObjValue,
			String destAEInst,
			String destAEName,
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(objName);
		cdapMessage.setObjValue(ObjValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();
	}

	public static CDAP.CDAPMessage generateM_READ(
			String ObjClass,
			String objName,
			String destAEInst,
			String destAEName,
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(objName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();
	}

	public static CDAP.CDAPMessage generateM_READ(
			String ObjClass,
			String objName,
			String destApInst,
			String destApName,
			String srcApInst,
			String srcApName)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(objName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();
	}



	/**
	 * generate M_READ_R
	 * @param ObjClass
	 * @param ObjName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_READ_R(
			int result, 
			String ObjClass,
			String ObjName,
			CDAP.objVal_t ObjValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result); //Mandatory
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);

		cdapMessage.setObjValue(ObjValue);

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ_R); //Mandatory
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	public static CDAP.CDAPMessage generateM_READ_R(
			String ObjClass,
			String ObjName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ_R); //Mandatory
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * 
	 * @param result
	 * @param ObjClass
	 * @param ObjName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_READ_R(
			int result, 
			String ObjClass,
			String ObjName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result); //Mandatory
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);


		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_READ_R); //Mandatory
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * generate M_WRITE
	 * @param ObjClass
	 * @param ObjName
	 * @param objValue
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_WRITE(
			String ObjClass,
			String ObjName,
			CDAP.objVal_t objValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_WRITE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * 
	 * @param ObjClass
	 * @param ObjName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_WRITE(
			String ObjClass,
			String ObjName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setObjClass(ObjClass);
		cdapMessage.setObjName(ObjName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_WRITE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}


	/**
	 * 
	 * @param result
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_WRITE_R(
			int result,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjName(objName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_WRITE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}



	/**
	 * 
	 * @param result
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return
	 */
	public static CDAP.CDAPMessage generateM_WRITE_R(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName
	)  {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();
		cdapMessage.setResult(result);
		cdapMessage.setObjClass(objClass);
		cdapMessage.setObjName(objName);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_WRITE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}
	/**
	 * generate M_START
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_START(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setResult(result);
		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}
	
	public static CDAP.CDAPMessage generateM_START(
			String objClass,
			String objName,
			CDAP.objVal_t objValue,
			CDAP.authTypes_t authMech,
			CDAP.authValue_t authValue, 
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setObjValue(objValue);
		cdapMessage.setAuthMech(authMech);
		cdapMessage.setAuthValue(authValue);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}
	/**
	 * generate M_START_R
	 * @param result
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_START_R(
			int result,
			String objClass,
			String objName,
			CDAP.objVal_t objValue,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 
		cdapMessage.setObjValue(objValue);
		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();

	}
	
	
	public static CDAP.CDAPMessage generateM_START_R(
			int result,
			String objClass,
			String objName,
			
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();

	}
	
	
	public static CDAP.CDAPMessage generateM_START_R(
			int result,

			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();


		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_START_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();

	}
	/**
	 * generate M_STOP
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return message
	 */
	public static CDAP.CDAPMessage generateM_STOP(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName		
	)    {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_STOP);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();

	}
	/**
	 * generate M_STOP_R
	 * @return CDAPMessage
	 */
	public static CDAP.CDAPMessage generateM_STOP_R(    
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_STOP_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();


	}

	/**
	 * generate M_RELEASE
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return message 
	 */
	public static CDAP.CDAPMessage generateM_RELEASE(    
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_RELEASE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial();
	}
	/**
	 * generate M_RELEASE_R
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return message
	 */
	public static CDAP.CDAPMessage generateM_RELEASE_R(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_RELEASE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial(); 
	}

	/**
	 * generate M_CANCELEREAD
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return cdapMessage
	 */
	public static CDAP.CDAPMessage generateM_CANCELEREAD(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)    
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CANCELREAD);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial(); 
	}
	/**
	 * generate M_CANCELREAD_R
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return cdapMessage
	 */
	public static CDAP.CDAPMessage generateM_CANCELREAD_R(	int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName)     
	{
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_CANCELREAD_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);


		return  cdapMessage.buildPartial(); 
	}

	/**
	 * generate M_DELETE
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return cdapMessage
	 */
	public static CDAP.CDAPMessage generateM_DELETE(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_DELETE);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();

	}

	/**
	 * generate M_DELETE_R
	 * @param result
	 * @param objClass
	 * @param objName
	 * @param destAEInst
	 * @param destAEName
	 * @param destApInst
	 * @param destApName
	 * @param invokeID
	 * @param srcAEInst
	 * @param srcAEName
	 * @param srcApInst
	 * @param srcApName
	 * @return CDAPMessage 
	 */
	public static CDAP.CDAPMessage generateM_DELETE_R(
			int result,
			String objClass,
			String objName,
			String destAEInst, 
			String destAEName, 
			String destApInst,
			String destApName,
			int invokeID,
			String srcAEInst,
			String srcAEName,
			String srcApInst,
			String srcApName) {
		CDAP.CDAPMessage.Builder  cdapMessage = CDAP.CDAPMessage.newBuilder();

		cdapMessage.setObjClass(objClass);//required from CDAP specs 0.7.2
		cdapMessage.setObjName(objName); //required from CDAP specs 0.7.2 

		cdapMessage.setResult(result);
		cdapMessage.setAbsSyntax(ABSTRACT_SYNTAX_VERSION);
		cdapMessage.setDestAEInst(destAEInst);
		cdapMessage.setDestAEName(destAEName);
		cdapMessage.setDestApInst(destApInst);
		cdapMessage.setDestApName(destApName);
		cdapMessage.setInvokeID(invokeID);
		cdapMessage.setOpCode(CDAP.opCode_t.M_DELETE_R);
		cdapMessage.setSrcAEInst(srcAEInst);
		cdapMessage.setSrcAEName(srcAEName);
		cdapMessage.setSrcApInst(srcApInst);
		cdapMessage.setSrcApName(srcApName);
		cdapMessage.setVersion(CDAP_VERSION);

		return  cdapMessage.buildPartial();
	}

	/**
	 * encode CDAPMessage to byte array
	 * @param msg
	 * @return data
	 */
	public static byte[] encodeCDAPMessage(CDAP.CDAPMessage msg)
	{
		byte[] data = msg.toByteArray();
		return data;
	}

}