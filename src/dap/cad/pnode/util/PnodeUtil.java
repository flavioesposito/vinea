/**
 * 
 */
package dap.cad.pnode.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rina.rib.impl.RIBImpl;

import dap.cad.impl.googleprotobuf.CAD;
import dap.cad.pnode.Pnode;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;

/**
 * @author flavio
 * physical node utilities: write utility function here
 */
public class PnodeUtil {

	/**
	 * utility type
	 */
	private String utility = null;

	/**
	 * RIB
	 */
	public RIBImpl rib = null;

	/**
	 * constructor
	 * @param utility
	 */
	public PnodeUtil(String utility, RIBImpl rib){
		this.utility = utility;
		this.rib = rib;

	}

	
	/**
	 * compute utility
	 * @param vnodeCapacity
	 * @param targetCapacity_T
	 * @param currentCapacity_S
	 * @return utility(pnode,vnode)
	 */
	public double computeUtility(double vnodeCapacity,  double targetCapacity_T, double currentCapacity_S){

		double result = -1;
		rib.RIBlog.debugLog("PnodeUtil::computeUtility: this.utility: "+this.utility);
		if (this.utility.equals("residual_node_capacity")){
			result = computeUtility1(vnodeCapacity, targetCapacity_T, currentCapacity_S);
		}else{
			rib.RIBlog.errorLog("PnodeUtil::computeUtility: this.utility: "+this.utility+" not handled");
			return result;
		}

		return result;

	}


	/**
	 * 
	 * @param vnodeCapacity
	 * @param bundle vector m
	 * @param sliceRequested
	 * @param currentStress
	 * @param targetStress
	 * @return utility
	 */
	public double computeUtility(double vnodeCapacity, LinkedList<Integer> m, Slice sliceRequested, double targetCapacity_T, double currentCapacity_S){

		double result = 0.0;

		if (this.utility.equals("residual_node_capacity")){
			result = computeUtility1(vnodeCapacity, m, sliceRequested,   targetCapacity_T,  currentCapacity_S);
		}

		return result;

	}

	/**
	 * compute utility of adding a node of vnodeCapacity with bundle m
	 * in this utility the stress = nodeStress
	 * @param vnodeCapacity
	 * @param m
	 * @param sliceRequested
	 * @param currentStress
	 * @param targetStress
	 * @return
	 */
	private double computeUtility1(double vnodeCapacity, LinkedList<Integer> m,
			Slice sliceRequested,  double targetCapacity_T, double currentCapacity_S) {

		double utility = -1;

		List<vNode> requested_vNodeList = sliceRequested.getVirtualnodeList();

		if(m.isEmpty()) {//bundle is empty
			rib.RIBlog.debugLog("Pnode::computeUtility1 with m: m is empty");
			rib.RIBlog.debugLog("Pnode::computeUtility1 targetCapacity_T  = "+targetCapacity_T );
			double T = vnodeCapacity + currentCapacity_S;
			utility = (targetCapacity_T - T)/targetCapacity_T;
			rib.RIBlog.debugLog("Pnode::computeUtility1 with m: utility = "+utility);

			return utility ;	
		}
		else {
			rib.RIBlog.debugLog("Pnode::computeUtility1 with m: m = "+m);
			double currentCapacity = 0;

			Iterator<Integer> mIter = m.iterator();
			while(mIter.hasNext()) {
				int vnodeInBundle = mIter.next(); 
//			for(int index = 0; index< m.size(); index++)
     	  //{
//				vnodeInBundle= m.get(index);
				int tempCapacity = requested_vNodeList.get(vnodeInBundle).getVNodeCapacity();
				rib.RIBlog.debugLog("Pnode::computeUtility1 targetCapacity_T  = "+targetCapacity_T );
				rib.RIBlog.debugLog("Pnode::computeUtility1 vnodeInBundle  = "+vnodeInBundle );
				rib.RIBlog.debugLog("Pnode::computeUtility1 tempCapacity  = "+tempCapacity );

				currentCapacity = currentCapacity + tempCapacity;
				rib.RIBlog.debugLog("Pnode::computeUtility1 currentCapacity  = "+currentCapacity );
			}

			double T = vnodeCapacity + currentCapacity+ currentCapacity_S;
			utility = (targetCapacity_T - T)/targetCapacity_T;
			rib.RIBlog.debugLog("Pnode::computeUtility1 with m: utility = "+utility);


			return utility;
		}

	}

	/**
	 * to compute h_ij
	 * @param vnodeCapacity
	 * @param sliceRequested
	 * @param targetCapacity_T
	 * @param currentCapacity_S
	 * @return utiliy
	 */
	public double computeUtility1(double vnodeCapacity, double targetCapacity_T, double currentCapacity_S) {

		rib.RIBlog.debugLog("Pnode::computeUtility1: currentCapacity_S :"+currentCapacity_S);
		rib.RIBlog.debugLog("Pnode::computeUtility1: vnodeCapacity :"+vnodeCapacity);

		double T = vnodeCapacity + currentCapacity_S;
		double utility = (T - currentCapacity_S)/T;
		rib.RIBlog.debugLog("Pnode::computeUtility1: utility = (T - currentCapacity_S)/T  = "+utility);

		return utility ;



	}
	
	
	/**
	 * reconstruct SliceSpec.Slice from CAD.CADMessage.Slice type
	 * @param piggyBackedSlice decoded from "first bid" message
	 * @return SliceSpec.Slice sliceToEmbed
	 */
	public Slice reconstructSlice(CAD.CADMessage.Slice sliceRequest) {

		SliceSpec.Slice.Builder slice = Slice.newBuilder();
		
		slice.setSliceID(sliceRequest.getSliceID());
		
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

		List<CAD.CADMessage.Slice.vNode> requested_vNodeList = sliceRequest.getVirtualnodeList();
		for (int j=0; j <  requested_vNodeList.size(); j++) {

			CAD.CADMessage.Slice.vNode v = requested_vNodeList.get(j);
			SliceSpec.Slice.vNode.Builder newVNode = SliceSpec.Slice.vNode.newBuilder();
			
			
			int vnodeid = v.getVNodeId();
			newVNode.setVNodeId(vnodeid);

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
			slice.addVirtualnode(newVNode.build());
			
			
		}


		List<CAD.CADMessage.Slice.vLink> requested_vLinkList = sliceRequest.getVirtuallinkList();

		for (int l=0; l <  requested_vLinkList.size(); l++) {

			CAD.CADMessage.Slice.vLink vlink = requested_vLinkList.get(l);
			SliceSpec.Slice.vLink.Builder newVLink = SliceSpec.Slice.vLink.newBuilder();
			
			
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

			slice.addVirtuallink(newVLink.build());

		}


		
		return slice.build();
	}

	
	


}
