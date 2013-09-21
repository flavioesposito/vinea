/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.message.api;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import dap.cad.impl.googleprotobuf.CAD;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;

/**
 * @author flavio esposito
 *
 */
public interface CADMessageAPI {
	

	public CAD.CADMessage generateFirstBidMessage(Slice sliceRequest,
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime,//bidtime here
			LinkedList<Integer> mVector	
			);
	
	
	public  CAD.CADMessage generateCADMessage(
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime,//bidtime here
			LinkedList<Integer> mVector	
			);
	
	public CAD.CADMessage generateSpResponse(
			int sliceID,
			String allocationPolicy,
			LinkedHashMap<Integer, String> allocationVector,
			LinkedList<Double> bidVector,
			LinkedHashMap<Integer,Long> biddingTime//bidtime here
			);
	
	public CAD.CADMessage generateLinkEmbeddingRequest(int sliceID);
}
