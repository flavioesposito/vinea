/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode.api;

import dap.cad.pnode.util.BidStructure;
import dap.cad.pnode.util.BiddingData;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;

/**
 * @author flavioesposito
 *
 */
public interface BiddingAPI {

	/**
	 * 
	 * @param sliceRequested
	 * @param biddingData
	 * @return updated Bidding Data structure
	 */
	BiddingData nodeBidding(Slice sliceRequested, BiddingData biddingData);
	/**
	 * 
	 * @param sliceRequested
	 * @return true if bidding is required
	 */
	boolean biddingIsNeeded(Slice sliceRequested);
	/**
	 * 
	 * @param sliceRequested
	 * @return bid structure
	 */
	BidStructure computeEta(Slice sliceRequested);
	/**
	 * 
	 * @param sliceRequested
	 * @return true if there was a bidding 
	 */
	boolean overbidAttempt(Slice sliceRequested);
}
