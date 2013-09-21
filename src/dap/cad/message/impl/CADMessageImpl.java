/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.message.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dap.cad.impl.googleprotobuf.CAD;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.assignment;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.bid;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.bidTime;
import dap.cad.message.api.CADMessageAPI;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;


/**
 * @author flavio
 *
 */
public class CADMessageImpl implements CADMessageAPI{


	/**
	 * version of the CDAP protocol
	 */

	private static final int CAD_VERSION = 0x0001;

	/**
	 * dummy Constructor
	 */
	public CADMessageImpl(){}


	/**
	 * first bid message (with slice info piggybacked in it)
	 * @param sliceRequested
	 * @param sliceID
	 * @param allocationPolicy
	 * @param allocationVector
	 * @param bidVector
	 * @param biddingTime
	 * @param mVector
	 * @return CAD message
	 */
	public  CAD.CADMessage generateFirstBidMessage(Slice sliceRequest,
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime,//bidtime here
			LinkedList<Integer> mVector	
			)
	{

		CAD.CADMessage.Builder  cadMessage = CAD.CADMessage.newBuilder();

		///////////////////////////////////////////// SLICE to piggyback  ///////////////////////////////////

		CAD.CADMessage.Slice.Builder slice = CAD.CADMessage.Slice.newBuilder();

		//required fields don't have hasfield check

		if(sliceRequest.hasEntryTime()){
			slice.setEntryTime(sliceRequest.getEntryTime());
		}
		if (sliceRequest.hasExitTime()){
			slice.setExitTime(sliceRequest.getExitTime());
		}
		if (sliceRequest.hasTopology()){
			slice.setTopology(sliceRequest.getTopology());
		}
		if (sliceRequest.hasName()){
			slice.setName(sliceRequest.getName());
		}
		if(sliceRequest.hasVersion()){
			slice.setVersion(sliceRequest.getVersion());
		}
		slice.setSliceID(sliceRequest.getSliceID());

		List<SliceSpec.Slice.vNode> requested_vNodeList = sliceRequest.getVirtualnodeList();
		//	System.out.println("CADMessage::generateFirstBidMessage::nodeAgreement: requested_vNodeList: "+requested_vNodeList);
		//	System.out.println("CADMessage::generateFirstBidMessage::nodeAgreement: requested_vNodeList.size(): "+requested_vNodeList.size());

		for (int j=0; j <  requested_vNodeList.size(); j++) {

			//		System.out.println("CADMessage::generateFirstBidMessage::nodeAgreement: j: "+j);

			SliceSpec.Slice.vNode v = requested_vNodeList.get(j);
			CAD.CADMessage.Slice.vNode.Builder newVNode = CAD.CADMessage.Slice.vNode.newBuilder();

			int vnodeid = v.getVNodeId();
			newVNode.setVNodeId(vnodeid);
			//		System.out.println("CADMessage::generateFirstBidMessage::nodeAgreement: vnodeid(): "+vnodeid);


			if(v.hasVNodeCapacity()){
				int vnodeCapacity = v.getVNodeCapacity();
				newVNode.setVNodeCapacity(vnodeCapacity);
			}
			if (v.hasVNodeType()){
				int vNodeType = v.getVNodeType();
				newVNode.setVNodeType(vNodeType);
			}
			if (v.hasVNodeClass()){
				String vNodeClass = v.getVNodeClass();
				newVNode.setVNodeClass(vNodeClass);
			}
			if (v.hasVNodeName()){
				String vNodeName = v.getVNodeName();
				newVNode.setVNodeClass(vNodeName);
			}//implement the extension here
			//		System.out.println("CADMessage::generateFirstBidMessage::nodeAgreement: newVNode: "+newVNode);
			//	slice.setVirtualnode(j, newVNode);
			slice.addVirtualnode(newVNode);
		}


		List<SliceSpec.Slice.vLink> requested_vLinkList = sliceRequest.getVirtuallinkList();

		for (int l=0; l <  requested_vLinkList.size(); l++) {

			SliceSpec.Slice.vLink vlink = requested_vLinkList.get(l);
			CAD.CADMessage.Slice.vLink.Builder newVLink = CAD.CADMessage.Slice.vLink.newBuilder();

			int vlinkID = vlink.getVLinkId();
			newVLink.setVLinkId(vlinkID);

			int vlinkSrc = vlink.getVSrcID();
			newVLink.setVSrcID(vlinkSrc);

			int vlinkDst = vlink.getVDstID();
			newVLink.setVDstID(vlinkDst);

			if (vlink.hasVLinkCapacity()){
				int vlinkCap = vlink.getVLinkCapacity();
				newVLink.setVLinkCapacity(vlinkCap);
			}

			if (vlink.hasVLinkType()){
				int vlinkT = vlink.getVLinkType();
				newVLink.setVLinkType(vlinkT);
			}

			//			slice.setVirtuallink(l, newVLink);
			slice.addVirtuallink(newVLink);


		}


		cadMessage.setSliceRequest(slice);

		/////////////////////////////////////////////// BID ///////////////////////////////////
		cadMessage.setVersion(CAD_VERSION);
		cadMessage.setSliceID(sliceID);
		cadMessage.setAllocationPolicy(allocationPolicy);

		//allocation vector
		if(allocationPolicy.equals("SAD")){

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setAssigned(true);
				cadMessage.addA(a_ij);
			}

		}else{ //default MAD

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				String hostingPnodeName= allocationVector.get(vnodeId);

				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setHostingPnodeName(hostingPnodeName);
				cadMessage.addA(a_ij);
			}


		}

		//bid vector

		for(int j=0;j<bidVector.size();j++)
		{
			double bidValue = bidVector.get(j);
			bid.Builder b_ij = bid.newBuilder();
			b_ij.setBidValue(bidValue);
			b_ij.setVNodeId(j);
			cadMessage.addB(b_ij);
		}

		//bundle m
		//TODO: m is private and it should only be sent to the slice manager when the agreement is reached to form the slice.
		if(mVector!= null){

			Iterator<Integer> mIter = mVector.iterator();
			while (mIter.hasNext())
			{
				int mValue = mIter.next();
				cadMessage.addM(mValue);
			}
		}

		//bidding time
		Set<Integer> tKeyset = biddingTime.keySet();
		Iterator<Integer> tIter = tKeyset.iterator();
		while(tIter.hasNext()){

			int vnodeId = tIter.next();
			Long timeofBidding= biddingTime.get(vnodeId);
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setVNodeId(vnodeId);
			t_ij.setTime(timeofBidding);
			cadMessage.addTimeStamp(t_ij);
		}

		Long currentBidTime = new Long(new Timestamp(new Date().getTime()).getTime());


		Iterator<Integer> mIter2 = mVector.iterator();
		while (mIter2.hasNext()) //the time only changes for vnodes in the bundle
		{
			int j = mIter2.next(); 
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setTime(currentBidTime);
			t_ij.setVNodeId(j);
			cadMessage.addTimeStamp(t_ij);
		}



		return  cadMessage.buildPartial();

	}


	/**
	 * bid message 
	 * @param sliceID
	 * @param allocationPolicy
	 * @param allocationVector
	 * @param bidVector
	 * @param biddingTime
	 * @param mVector
	 * @return CAD message
	 */
	public CAD.CADMessage generateCADMessage(
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime,//bidtime here
			LinkedList<Integer> mVector	
			)
	{
		CAD.CADMessage.Builder  cadMessage = CAD.CADMessage.newBuilder();

		cadMessage.setVersion(CAD_VERSION);
		cadMessage.setSliceID(sliceID);
		cadMessage.setAllocationPolicy(allocationPolicy);

		//allocation vector
		if(allocationPolicy.equals("SAD")){

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setAssigned(true);
				cadMessage.addA(a_ij);
			}

		}else{ //default MAD

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				String hostingPnodeName= allocationVector.get(vnodeId);

				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setHostingPnodeName(hostingPnodeName);
				cadMessage.addA(a_ij);
			}


		}

		//bid vector

		for(int j=0;j<bidVector.size();j++)
		{
			double bidValue = bidVector.get(j);
			bid.Builder b_ij = bid.newBuilder();
			b_ij.setBidValue(bidValue);
			b_ij.setVNodeId(j);
			cadMessage.addB(b_ij);
		}

		//bundle m
		//TODO: m is private and it should only be sent to the slice manager when the agreement is reached to form the slice.
		if(mVector!= null){

			Iterator<Integer> mIter = mVector.iterator();
			while (mIter.hasNext())
			{
				int mValue = mIter.next();
				cadMessage.addM(mValue);
			}
		}

		//bidding time
		Set<Integer> tKeyset = biddingTime.keySet();
		Iterator<Integer> tIter = tKeyset.iterator();
		while(tIter.hasNext()){

			int vnodeId = tIter.next();
			Long timeofBidding= biddingTime.get(vnodeId);
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setVNodeId(vnodeId);
			t_ij.setTime(timeofBidding);
			cadMessage.addTimeStamp(t_ij);
		}

		Long currentBidTime = new Long(new Timestamp(new Date().getTime()).getTime());


		Iterator<Integer> mIter2 = mVector.iterator();
		while (mIter2.hasNext()) //the time only changes for vnodes in the bundle
		{
			int j = mIter2.next(); 
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setTime(currentBidTime);
			t_ij.setVNodeId(j);
			cadMessage.addTimeStamp(t_ij);
		}



		return  cadMessage.buildPartial();
	}



	/**
	 * send response to SP
	 * @param sliceID
	 * @param allocationPolicy
	 * @param allocationVector
	 * @param bidVector
	 * @param biddingTime
	 * @param mVector
	 * @return CAD message
	 */
	public  CAD.CADMessage generateSpResponse(
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime//bidtime here
			)
	{
		CAD.CADMessage.Builder  cadMessage = CAD.CADMessage.newBuilder();

		cadMessage.setVersion(CAD_VERSION);
		cadMessage.setSliceID(sliceID);
		cadMessage.setAllocationPolicy(allocationPolicy);

		//allocation vector
		if(allocationPolicy.equals("SAD")){

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setAssigned(true);
				cadMessage.addA(a_ij);
			}

		}else{ //default MAD

			Set<Integer> aKeyset = allocationVector.keySet();
			Iterator<Integer> aIter = aKeyset.iterator();
			while(aIter.hasNext()){

				int vnodeId = aIter.next();
				String hostingPnodeName= allocationVector.get(vnodeId);

				assignment.Builder  a_ij = assignment.newBuilder();
				a_ij.setVNodeId(vnodeId);
				a_ij.setHostingPnodeName(hostingPnodeName);
				cadMessage.addA(a_ij);
			}

		}

		//bid vector

		for(int j=0;j<bidVector.size();j++)
		{
			double bidValue = bidVector.get(j);
			bid.Builder b_ij = bid.newBuilder();
			b_ij.setBidValue(bidValue);
			b_ij.setVNodeId(j);
			cadMessage.addB(b_ij);
		}

		

		//bidding time
		Set<Integer> tKeyset = biddingTime.keySet();
		Iterator<Integer> tIter = tKeyset.iterator();
		while(tIter.hasNext()){

			int vnodeId = tIter.next();
			Long timeofBidding= biddingTime.get(vnodeId);
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setVNodeId(vnodeId);
			t_ij.setTime(timeofBidding);
			cadMessage.addTimeStamp(t_ij);
		}

		Long currentBidTime = new Long(new Timestamp(new Date().getTime()).getTime());


		Set<Integer> tKeyset2 = biddingTime.keySet();
		Iterator<Integer> tIter2 = tKeyset2.iterator();
		while (tIter2.hasNext()) //the time only changes for vnodes in the bundle
		{
			int j = tIter2.next(); 
			bidTime.Builder t_ij = bidTime.newBuilder();
			t_ij.setTime(currentBidTime);
			t_ij.setVNodeId(j);
			cadMessage.addTimeStamp(t_ij);
		}



		return  cadMessage.buildPartial();
	}

	
	
	public CAD.CADMessage generateLinkEmbeddingRequest(int sliceID){
		
		CAD.CADMessage.Builder  cadMessage = CAD.CADMessage.newBuilder();

		cadMessage.setVersion(CAD_VERSION);
		cadMessage.setSliceID(sliceID);
		
		
		return cadMessage.buildPartial();
	}

}//end of class
