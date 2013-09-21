/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode.agreement.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import rina.rib.impl.RIBImpl;
import dap.cad.impl.googleprotobuf.CAD;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.assignment;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.bid;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.bidTime;
import dap.cad.pnode.api.AgreementAPI;
import dap.cad.pnode.util.BiddingData;

/**
 * @author Flavio Esposito
 * The Node agreement is run from any physical node 
 * to reach consensus on the winner for each virtual resources
 *
 */
public class NodeAgreementImpl implements AgreementAPI {
	/**
	 * resource information base
	 */
	private	RIBImpl rib = null;

	private String pNodeBidder = null;

	private boolean _rebroadcastFlag = false;

	private BiddingData _newBiddingData = null;
	/**
	 * receiver allocation vector
	 */
	private LinkedHashMap<Integer,String> a_i = null;
	/**
	 * sender allocation vector
	 */
	private LinkedHashMap<Integer,String> a_k = null;

	/**
	 * receiver bid vector
	 */
	private LinkedList<Double> b_i = null;

	/**
	 * sender bid vector 
	 */
	private LinkedList<Double> b_k = null;

	/**
	 * receiver bundle vector 
	 * of course the sender is private as it could be used to reverse engineer 
	 * the utility function and it is not broadcasted
	 */
	private LinkedList<Integer> m_i = null;

	/**
	 * receiver time stamp vector
	 */
	private LinkedHashMap<Integer, Long> t_i = null;

	/**
	 * sender time stamp vector
	 */
	private LinkedHashMap<Integer, Long> t_k = null;

	/**
	 * constructor
	 * @param rib
	 * @param pNodeBidder
	 */
	public NodeAgreementImpl(RIBImpl rib, String pNodeBidder){
		this.rib = rib;
		this.pNodeBidder = pNodeBidder;
	}


	/**
	 * handle the  CAD conflict resolution phase
	 * conflict resolution rule table inspired by: http://lics.kaist.ac.kr/files/JohnsonPondaChoiHow_Info11.pdf
	 * @param cadMessage
	 * @param _currentBiddingData
	 * @return the updated biddintdata
	 */
	public BiddingData nodeAgreement(CAD.CADMessage cadMessage, BiddingData _currentBiddingData, 
			String k,   //sender pnode 
			String i) { //receiver pnode

		
		_newBiddingData = _currentBiddingData;

		// assume there is no need to rebroadcast
		_newBiddingData.set_rebroadcast(false);

		int received_sliceID = cadMessage.getSliceID(); 
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_sliceID: "+received_sliceID);

		String received_allocationPolicy = cadMessage.getAllocationPolicy();
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_allocationPolicy: "+received_allocationPolicy);

		List<bidTime> received_bidTime = cadMessage.getTimeStampList();
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_bidTime: "+received_bidTime);


		List<assignment> received_assignment = cadMessage.getAList();

		//a_kj winning agent list of size V_N where each element indicates who pnode sender k believes is winning vnode j
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_assignment: "+received_assignment);
		a_k = extract_ak(received_assignment); //received allocation vector
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: a_k : "+a_k);

		//a_ij winning agent list of size V_N where each element indicates who pnode receiver i believes is winning vnode j
		
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: _currentBiddingData : "+_currentBiddingData);

		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: get_allocationVectorMap() : "+_currentBiddingData.get_allocationVectorMap());

		if(_currentBiddingData.get_allocationVector(received_sliceID) == null){
			a_i = new LinkedHashMap<Integer,String>();
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: a_i empty so far (no bid has been or could have been made yet)");
		}
		else{
			a_i = _currentBiddingData.get_allocationVector(received_sliceID);
		}
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: a_i : "+a_i);


		List<bid> received_bids = cadMessage.getBList();
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement:: received_bids: "+received_bids);

		//b_kj winning bid list of size V_N where each element indicates what is the bid of pnode sender k for vnode j
		b_k =  extract_bk(received_bids);
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: receiver b_k : "+b_k );

		//b_ij winning bid list of size V_N where each element indicates what is the bid of pnode receiver i for vnode j
		if(_currentBiddingData.get_bidVector(received_sliceID) == null){
			b_i = new LinkedList<Double>();
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_i empty so far (no bid has been or could have been made yet) ");

		}
		else{
			b_i = _currentBiddingData.get_bidVector(received_sliceID);
		}
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement:: receiver b_i : "+b_i );



		//t_kj time-stamp list of size V_N where each element indicates what is the time at which the bid was made from pnode sender k for vnode j
		t_k = extract_tk(received_bidTime);
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement:: sender t_k : "+t_k );

		//t_ij time-stamp list of size V_N where each element indicates what is the time at which the bid was made from pnode receiver ifor vnode j
		if(_currentBiddingData.get_biddingTimeVector(received_sliceID) ==null )
		{
			t_i = new LinkedHashMap<Integer, Long>();
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: t_i empty so far (no bid has been or could have been made yet) ");

		}
		t_i = _currentBiddingData.get_biddingTimeVector(received_sliceID);
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement:: sender t_i : "+t_i );

		//TODO: m should only be sent around for debugging reasons and it is only needed at the NMS when the agreement is reached to form the slice. 
		List<Integer> received_m = (List<Integer>) cadMessage.getMList();
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_m: "+received_m);

		if( _currentBiddingData.get_mMap().get(received_sliceID) ==null )
		{
			m_i = new LinkedList<Integer>();
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: m_i empty so far (no bid has been or could have been made yet) ");
		}
		m_i = _currentBiddingData.get_mMap().get(received_sliceID); 

		//initialize this to true every time receive e message
		_rebroadcastFlag = false; 
		rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_k.size(): "+b_k.size());

		for(int j = 0; j< b_k.size(); j++)
		{
			
			//TODO: Improvement: as soon as we realize we need to release a node in the bundle:
					// interrupt the agreement phase (saving the breakpoint)
					// redo the bidding phase taking into account the lost bid (otherwise bids are too conservative)
					// come back to the breakpoint and continue the agreement phase with the new bids 


			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: for loop: j: "+j);

			String a_kj = null;
			String a_ij = null;
			if(a_k.containsKey(j)) {
				a_kj = a_k.get(j);
			}
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: a_kj : "+a_kj);
			if(a_i.containsKey(j)) {
				a_ij = a_i.get(j);
			}
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: a_ij : "+a_ij);

			double b_kj = 0.0;//Double.MIN_VALUE; //initialize to the minimum possible value

			if(!b_k.isEmpty())  
				b_kj = b_k.get(j);
				
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_kj : "+b_kj);
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_k : "+b_k);

			double b_ij = 0.0;// Double.MIN_VALUE; //initialize to the minimum possible value
			if(!b_i.isEmpty()) 
				b_ij = b_i.get(j);
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_ij : "+b_ij);
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: b_i : "+b_i);
			



			long t_kj = Long.MIN_VALUE;
			if(t_k.containsKey(j)) 
				t_kj = t_k.get(j);
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: t_kj  : "+t_kj );
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: t_k  : "+t_k );

			
			long t_ij = Long.MIN_VALUE; 
			if(t_i.containsKey(j))
				t_ij = t_i.get(j);
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: t_ij : "+t_ij );
			rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: t_i : "+t_i );

			//conflict resolution rules:
			if(a_kj.equals(k) && a_ij.equals(i)) {

				//1. [sender k thinks a_kj is k] [receiver i thinks a_ij is i] => if b_kj > b_ij --> update & rebroadcast
				if(b_kj > b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 1");
					updateAndRebroadcast(j , k); //update winner of vnode j with pnode k 
				}
				//2. [sender k thinks a_kj is k] [receiver i thinks a_ij is i] => if b_kj = b_ij and a_kj < a_ij -->  update & rebroadcast
				else if(b_kj == b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 2");
					updateAndRebroadcast(j , k); //break ties so that sender always win (not random as in ACBBA, so we minimize the overhead of time and protocol messages)
					//updateTimeAndRebroadcast(j);  //is it this one instead? check
				}
				//3. [sender k thinks a_kj is k] [receiver i thinks a_ij is i] => if b_kj < b_ij --> update time & rebroadcast
				else if(b_kj < b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 3");
					updateTimeAndRebroadcast(j);
				}
			}

			//4. [sender k thinks a_kj is k] [receiver i thinks a_ij is k] => if t_kj > t_ij --> update & rebroadcast
			else if(a_kj.equals(k) && a_ij.equals(k)) {
				if(t_kj > t_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 4");
					updateAndRebroadcast(j , k); //update winner of vnode j with pnode k
				}
				//5. [sender k thinks a_kj is k] [receiver i thinks a_ij is k] => |t_kj - t_ij | < \epsilon --> leave & no-broadcast
				else if(Math.abs(t_kj - t_ij) < Double.MIN_VALUE) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 5");
					leaveAndNoBroadcast();
				}
				//6. [sender k thinks a_kj is k] [receiver i thinks a_ij is k] => if t_kj < t_ij --> leave & no-rebroadcast
				else if(t_kj < t_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 6");
					leaveAndNoBroadcast();
				}
			}


			//7. [sender k thinks a_kj is k] [receiver i thinks a_ij is m \neq{i,k} ] => if b_kj > b_ij and t_kj >= t_ij --> update & rebroadcast
			else if(a_kj.equals(k) && !a_ij.equals(k) && !a_ij.equals(i) ) {
				if( b_kj > b_ij && t_kj >= t_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 7");
					updateAndRebroadcast(j , k); // sender wins with updated info
				}
				//8. [sender k thinks a_kj is k] [receiver i thinks a_ij is m \neq{i,k} ] => if b_kj < b_ij and t_kj >= t_ij --> leave & rebroadcast
				else if((b_kj < b_ij) && (t_kj >= t_ij) ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 8");
					leaveAndRebroadcast();    	// receiver wins and sender needs to know
				}
				//9. [sender k thinks a_kj is k] [receiver i thinks a_ij is m \neq{i,k} ] => if b_kj = b_ij ! leave & rebroadcast
				else if(b_kj == b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 9");
					leaveAndRebroadcast();
				}
				//10.[sender k thinks a_kj is k] [receiver i thinks a_ij is m \neq{i,k} ] =>  if b_kj < b_ij and t_kj > t_ij --> update & rebroadcast
				else if((b_kj < b_ij) && (t_kj < t_ij) ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 10");
					updateAndRebroadcast(j , k); //sender has higher bid but out of date
				}
				//11.[sender k thinks a_kj is k] [receiver i thinks a_ij is m \neq{i,k} ] =>  if b_kj > b_ij and t_kj < t_ij --> update & rebroadcast
				else if((b_kj > b_ij) && (t_kj < t_ij)) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 11");
					updateAndRebroadcast(j , a_ij); //sender has higher bid but updated info
				}
			}
			//12.[sender k thinks a_kj is k] [receiver i thinks a_ij is none ] =>   update & rebroadcast
			else if(a_kj.equals(k) && (!a_i.containsKey(j))) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 12");
				updateAndRebroadcast(j , k); //sender has no clue who is winning on j 
			}

			//13.[sender k thinks a_kj is i] [receiver i thinks a_ij is i] => if |t_kj - t_ij | < \epsilon --> leave & no-rebroadcast
			else if(a_kj.equals(i) && a_ij.equals(i)) {
				if(Math.abs(t_kj - t_ij) < Double.MIN_VALUE) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 13");
					leaveAndNoBroadcast();  // sender and receiver agree on j 
				}
			}
			//14.[sender k thinks a_kj is i] [receiver i thinks a_ij is k] => reset & rebroadcast*
			else if(a_kj.equals(i) && a_ij.equals(k)) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 14");
				resetAndRebroadcastStar(j); 			// rebroadcast* --> empty bid with current time
			}
			//15.[sender k thinks a_kj is i] [receiver i thinks a_ij is m \neq{i,k}] => leave & rebroadcast
			else if(a_kj.equals(i) && !a_ij.equals(k) && !a_ij.equals(i) ) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 15");
				leaveAndRebroadcast();  // sender needs to be updaated 
			}
			//16.[sender k thinks a_kj is i] [receiver i thinks a_ij is none] => leave & rebroadcast*
			else if(a_kj.equals(i) && (!a_i.containsKey(j))) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 16");
				leaveAndRebroadcast();		//--> empty bid with current time (sender has wrong info so it needs to be reset)
			}

			//17.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is i] => if b_kj > b_ij --> update & rebroadcast
			else if(!a_kj.equals(i) && (!a_kj.equals(k)) && a_ij.equals(i)) {
				if(b_kj > b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 17");
					updateAndRebroadcast(j , i); //sender has updated info 
				}
				//18.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is i] => if b_kj = b_ij and a_kj < a_ij --> update & rebroadcast
				else if(b_kj == b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 18");
					updateAndRebroadcast(j, k); //break ties so that receiver always win (random)
				}
				//19.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is i] => if b_kj < b_ij --> update time & rebroadcast
				else if(b_kj < b_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 19");
					updateTimeAndRebroadcast(j);
				}
			}

			//20.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is k] => update & rebroadcast (sender info)
			else if(!a_kj.equals(i) && (!a_kj.equals(k)) && a_ij.equals(k)) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 20");
				updateAndRebroadcast(j , k); //broadcast sender information 
			}
			//21.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is m] =>	if t_kj > t_ij --> update & rebroadcast
			else if(!a_kj.equals(i) && (!a_kj.equals(k)) && a_ij.equals(a_kj)) {
				if(t_kj > t_ij ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 21");
					updateAndRebroadcast(j , a_kj); //broadcast sender information (its the same in this case anyway)
				}
				//22.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is m] => if |t_kj - t_ij | < epsilon --> leave & no-rebroadcast
				else if(Math.abs(t_kj - t_ij) < Double.MIN_VALUE ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 22");
					leaveAndNoBroadcast();  // sender and receiver agree on j 
				}
				//23.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is m] => if t_kj < t_ij --> leave & rebroadcast
				else if( t_kj < t_ij ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 23");
					leaveAndRebroadcast();		// sender has old time stamp that need to be refreshed
				}
			}

			//24.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is n \neq{i,k,m}] => if b_kj > b_ij and t_kj >= t_ij --> update & rebroadcast
			else if(!a_kj.equals(i) && (!a_kj.equals(k)) && (!a_ij.equals(a_kj))) {
				if((b_kj > b_ij) && (t_kj >= t_ij) ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 24");
					updateAndRebroadcast(j , a_kj);  //broadcast sender information as it has more fresh info 
				}
				//25.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is n \neq{i,k,m}] => if b_kj < b_ij and t_kj <= t_ij --> leave & rebroadcast
				else if((b_kj < b_ij) && (t_kj <= t_ij) ) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 25");
					leaveAndRebroadcast();		// sender has old time stamp that need to be refreshed
				}
				//26.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is n \neq{i,k,m}] => if b_kj < b_ij and t_kj > t_ij --> update & rebroadcast
				else if((b_kj < b_ij) && (t_kj > t_ij)) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 26");
					updateAndRebroadcast(j , a_kj);  //broadcast sender information as it has more fresh info 
				}
				//27.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is n \neq{i,k,m}] => if b_kj > b_ij and t_kj < t_ij --> leave & rebroadcast
				else if((b_kj > b_ij) && (t_kj < t_ij )) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 27");
					leaveAndRebroadcast();		// sender has old time stamp that need to be refreshed
				}
			}

			//28.[sender k thinks a_kj is m \neq{i,k}] [receiver i thinks a_ij is none] --> update & rebroadcast
			else if(!a_kj.equals(i) && !a_kj.equals(k) && !a_i.containsKey(j)) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 28");
				updateAndRebroadcast(j , a_kj);  //broadcast sender information as it has more fresh info 
			}

			//29.[sender k thinks a_kj is none] [receiver i thinks a_ij is i] --> leave & rebroadcast
			else if(!a_k.containsKey(j) && a_ij.equals(i) ) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 29");
				leaveAndRebroadcast();		// sender has need to be updated has it has no idea who is the winner but there is one.
			}

			//30.[sender k thinks a_kj is none] [receiver i thinks a_ij is k] --> update & rebroadcast
			else if(!a_k.containsKey(j) && (a_ij.equals(k))) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 30");
				updateAndRebroadcast(j , a_kj);  //broadcast sender information as it has more fresh info 
			}

			//31.[sender k thinks a_kj is none] [receiver i thinks a_ij is m \neq{i,k}] => if t_kj > t_ij --> update & rebroadcast
			else if(!a_k.containsKey(j) && (!a_ij.equals(i)) && (!a_ij.equals(k))) {
				if( t_kj > t_ij) {
					rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 31");
					updateAndRebroadcast(j , a_kj);  //broadcast sender information as it has more fresh info 
				}
			}

			//32.[sender k thinks a_kj is none] [receiver i thinks a_ij is none] --> leave & no-rebroadcast
			else if(!a_k.containsKey(j) && !a_i.containsKey(j)) {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: conflict resolution rule 32");
				leaveAndNoBroadcast();  // sender and receiver agree on j 
			}

			else {
				rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: default conflict resolution rule");
				leaveAndRebroadcast();		// default option
			}
			//note: 
			// rebroadast with leave --> broadcast own information 
			// rebroadast with update --> broadcast sender's information	
			// rebroadast with reset--> broadcast sender's information
			// rebroadast with update time--> broadcast own information

			// rebroadcast* --> empty bid with current time	



		}//end of the for cicle on the vNodes


		updateVectors(_currentBiddingData, cadMessage.getSliceID());

		
		return _newBiddingData;
	}

	/**
	 * update current vecors
	 * @param _currentBiddingData
	 */
	public void updateVectors(BiddingData _currentBiddingData, int sliceID) {


		_newBiddingData.set_biddingTimeVector(t_i, sliceID);

		LinkedHashMap<Integer, LinkedHashMap<Integer,Long>> _biddingTimeMap = _currentBiddingData.get_biddingTimeMap();
		_biddingTimeMap.remove(sliceID);
		_biddingTimeMap.put(sliceID, t_i);
		_newBiddingData.set_biddingTimeMap(_biddingTimeMap);

		_newBiddingData.set_allocationVector(sliceID, a_i);

		LinkedHashMap<Integer, LinkedHashMap<Integer,String>> _allocationVectorMap = _currentBiddingData.get_allocationVectorMap();
		_allocationVectorMap.remove(sliceID);
		_allocationVectorMap.put(sliceID, a_i);		 
		_newBiddingData.set_allocationVectorMap(_allocationVectorMap);

		_newBiddingData.set_bidVector(sliceID, b_i);

		LinkedHashMap<Integer, LinkedList<Double>> _bidVectorMap = _currentBiddingData.get_bidVectorMap();
		_bidVectorMap.remove(sliceID);
		_bidVectorMap.put(sliceID, b_i);
		_newBiddingData.set_bidVectorMap(_bidVectorMap);


		_newBiddingData.set_m(sliceID, m_i);

		LinkedHashMap<Integer, LinkedList<Integer>> _mMap = _currentBiddingData.get_mMap();
		_mMap.remove(sliceID);
		_mMap.put(sliceID, m_i);
		_newBiddingData.set_mMap(_mMap);

		_newBiddingData.setA_i(a_i);
		_newBiddingData.setA_k(a_k);
		

		if(_rebroadcastFlag == true)
		{
			rebroadcast();
		}

	}


	/**
	 * This case happens when the receiver is the vNode winner and observes a possibly confusing message. 
	 * The receiver updates the time-stamp on his bid to reflect the current time, 
	 * confirming that the bid is still active at the current time.
	 */
	public void updateTimeAndRebroadcast(int j) {


		rib.RIBlog.debugLog("NodeAgreement::updateTimeAndRebroadcast: BEFORE updating t_i : "+t_i );
		t_i.remove(j);
		Long currentBidTime = new Long(new Timestamp(new Date().getTime()).getTime());
		t_i.put(j, currentBidTime);
		rib.RIBlog.debugLog("NodeAgreement::updateTimeAndRebroadcast: AFTER updating t_i : "+t_i );


		_rebroadcastFlag = true;
	}


	/**
	 * // rebroadcast* --> empty bid with current time
	 * The receiver resets its information state: a_ij = null and b_ij = 0, and
	 * rebroadcasts the original received message so that the confusion can be resolved by other agents.
	 */
	public void resetAndRebroadcastStar(int j) {
		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: for vnode "+j );


		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: BEFORE updating a_i : "+a_i );
		a_i.remove(j);
		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: AFTER updating a_i : "+a_i );

		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: BEFORE updating b_i : "+b_i );
		b_i.remove(j);
		b_i.add(j, 0.0);
		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: AFTER updating b_i : "+b_i );

		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: BEFORE updating t_i : "+t_i );
		t_i.remove(j);
		rib.RIBlog.debugLog("NodeAgreement::resetAndRebroadcastStar: AFTER updating t_i : "+t_i );


		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: BEFORE updating m_i : "+m_i );
		releaseSubsequentVnodes(j);
		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: AFTER updating m_i : "+m_i );


		_rebroadcastFlag =  true;
	}

	/**
	 * The receiver does not change its information state, but rebroadcasts its local
	 * copy of the winning agent's information because either it believes its information is more correct than
	 * the sender's, or the agent is unsure and it's looking for confirmation from another agent
	 */
	public void leaveAndRebroadcast() {

		rib.RIBlog.debugLog("NodeAgreement::leaveAndRebroadcast: DO NOTHING for this vnode but rebroadcast" );

		_rebroadcastFlag = true;
	}

	/**
	 * The receiver neither changes its information state nor rebroadcasts it.
	 * This action is applied when the information is either not new or is outdated and should have been
	 * corrected already
	 */
	public void leaveAndNoBroadcast() {

		rib.RIBlog.debugLog("NodeAgreement::leaveAndNoBroadcast: Agreement reached!!! DO NOTHING ELSE for this vnode" );

	}


	/**
	 * The receiver i updates its winning agent a_ij , winning bid b_ij , and winning
	 * time t_ij with the received information from the sender k. It then propagates this new information.
	 * @param j vNodeId
	 * @param k winner
	 */
	public void updateAndRebroadcast(int j, String winner) {
		_rebroadcastFlag = true;

		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: for vnode "+j );

		//TODO: consider implementing also this function: if its SAD only update 1 or 0 and not the identity of the winner
		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: BEFORE updating a_i : "+a_i );
		a_i.remove(j);
		a_i.put(j, winner);
		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: AFTER updating a_i : "+a_i );

		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: BEFORE updating b_i : "+b_i );
		b_i.remove(j);
		b_i.add(j, b_k.get(j));
		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: AFTER updating b_i : "+b_i );

		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: BEFORE updating t_i : "+t_i );
		t_i.remove(j);
		t_i.put(j, t_k.get(j));
		rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: AFTER updating t_i : "+t_i );

		
		if(!winner.equals(this.pNodeBidder)){
			rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: BEFORE updating m_i : "+m_i );
			releaseSubsequentVnodes(j);
			rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: AFTER updating m_i : "+m_i );
		}else
			rib.RIBlog.debugLog("NodeAgreement::updateAndRebroadcast: no need to update bundle m_i: "+m_i );

		
		// log what's just happened
		//rib.RIBlog.debugLog("NodeAgreement::nodeAgreement: received_m: "+received_m);

		
	}





	/**
	 * set the reboracasting flag to true
	 * rebroadcast
	 */
	public void rebroadcast() {

		this._newBiddingData.set_rebroadcast(true);
	}

	/**
	 * release subsequent nodes after the outbid vnode j;
	 * if the subsequent tasks are not released, then the current best scores
	 * computed for those tasks would be overly conservative, possibly leading to a degradation in performance
	 * @param j
	 */
	public void releaseSubsequentVnodes(int j) {
		//if the node was in the bundle you need to release the subsequent and rebid forbidding the node just lost
		rib.RIBlog.debugLog("NodeAgreement::releaseSubsequentVnodes: old bundle m_i : "+m_i );
		LinkedList<Integer> newBundle_m = new LinkedList<Integer>();
		if(m_i.contains(j)){
			for(int l=0; l<j;l++){
				newBundle_m.add(m_i.get(l));
				rib.RIBlog.debugLog("NodeAgreement::releaseSubsequentVnodes: newBundle_m : "+newBundle_m );
			}
			m_i = newBundle_m;
		}
		rib.RIBlog.debugLog("NodeAgreement::releaseSubsequentVnodes: newBundle_m : "+newBundle_m );
		//TODO: bid again after you release a node

	}

	/**
	 * extract sender k time vector t_k from received serialized bid message
	 * @param received_bids
	 * @return LinkedHashMap<int vNodeID, long timeStamp>
	 */
	private LinkedHashMap<Integer, Long> extract_tk(List<bidTime> received_timeStamps) {

		rib.RIBlog.debugLog("NodeAgreement::extract_tk: received_timeStamps : "+received_timeStamps );

		LinkedHashMap<Integer, Long> t_k = new LinkedHashMap<Integer, Long>();

		Iterator<bidTime> tIter = received_timeStamps.iterator(); 

		while(tIter.hasNext()) {
			bidTime time_kj = tIter.next();
			int j = time_kj .getVNodeId();
			long t_kj = time_kj.getTime();
			t_k.put(j, t_kj);
		}

		rib.RIBlog.debugLog("NodeAgreement::extract_tk: t_k : "+t_k );

		return t_k;
	}


	/**
	 * extract sender k allocation vector a_k from received serialized bid message
	 * @param assignmentsList
	 * @return
	 */
	private LinkedHashMap<Integer, String> extract_ak(List<assignment> assignmentsList) {

		LinkedHashMap<Integer, String> a_k = new LinkedHashMap<Integer, String>();


		rib.RIBlog.debugLog("NodeAgreement::extract_ak: assignmentsList : "+assignmentsList );

		Iterator<assignment> aIter = assignmentsList.iterator();
		rib.RIBlog.debugLog("NodeAgreement::extract_ak: aIter : "+aIter );

		while(aIter.hasNext())
		{
			assignment assignment_kj = aIter.next();
			int j= assignment_kj .getVNodeId();
			String a_kj =  assignment_kj .getHostingPnodeName();
			a_k.put(j,a_kj);
		}

		rib.RIBlog.debugLog("NodeAgreement::extract_ak: a_k : "+a_k );

		return a_k;
	}

	/**
	 * extract sender k bid vector b_k from received serialized bid message
	 * @param received_bids
	 * @return
	 */
	private LinkedList<Double> extract_bk(List<bid> received_bids) {

		rib.RIBlog.debugLog("NodeAgreement::extract_bk: received_bids : "+received_bids );

		LinkedList<Double> b_k = new LinkedList<Double>();
		Iterator<bid> bIter = received_bids.iterator();

		while(bIter.hasNext()) {
			bid b_Serialized = bIter.next();
			double b_kj = b_Serialized.getBidValue();
			b_k.add(b_kj);
		}

		rib.RIBlog.debugLog("NodeAgreement::extract_bk: b_k : "+b_k );

		return b_k;
	}


}
