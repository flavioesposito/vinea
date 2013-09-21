/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.rib.impl.RIBImpl;


import dap.cad.config.CADConfig;
import dap.cad.impl.googleprotobuf.CAD;


import dap.cad.message.impl.CADMessageImpl;
import dap.cad.pnode.agreement.impl.NodeAgreementImpl;
import dap.cad.pnode.bidding.impl.NodeBiddingImpl;
import dap.cad.pnode.linkEmbedding.impl.LinkEmbeddingImpl;
import dap.cad.pnode.util.BiddingData;
import dap.cad.pnode.util.PnodeUtil;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import dap.cad.sp.slicegenerator.SliceGenerator;


//TODO: physical nodes needs to be aware of the physical topology or we can't bootstrap
/**
 * core class of CADSys: physical nodes, gets authenticated and run the CAD protocol
 * 
 * @author Flavio Esposito
 * @version 1.0
 */
public class Pnode extends Application {

	/**
	 * adjacent Links id
	 */
	private LinkedHashMap<Integer, LinkedList<Integer>> _adjacentLinksMap = null;

	/**
	 * <sliceID, <adjecent link, bandwidth>>
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer, Double> > _adjacentBandwidthMap= null;

	/**
	 * allocation policy default MAD (SAD or MAD or write your own)
	 */
	private String _allocationPolicy= "MAD";

	/**
	 * list of pnodes reachable from this
	 */
	private LinkedList<String> _appsReachable = null;


	/**
	 * allocation vector for each slice hosted or in hosting attempt <sliceID, <vnode, pnodeNameOrAddress>>
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer,String>> _allocationVectorMap = null;

	/**
	 * bid vector for each slice hosted or in hosting attempt
	 */
	private LinkedHashMap<Integer, LinkedList<Double>> _bidVectorMap = null;

	/**
	 * <sliceID, <vNodeID, biddingTime>>
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer,Long>> _biddingTimeMap = null;

	/**
	 * pnode configuration file  
	 */
	private CADConfig _CADconfig= null;


	/**
	 * node identifier: for now this is redundant with applicationName
	 */
	private int _id = -1;

	/**
	 * current adjacent incoming link capacity
	 */
	private double _incomingLinkCapacity = 0.0;

	/**
	 * current adjacent incoming link capacity
	 */
	private double _outgoingLinkCapacity = 0.0;

	/**
	 * bundle vector for each slice hosted or in hosting attempt
	 */
	private LinkedHashMap<Integer, LinkedList<Integer>> _mMap = null;


	/**
	 * service providers can send requests for embedding
	 */
	private String _mySP = null;
	/**
	 * InP manager (don't send bid message to him) 
	 */
	private String _myManager = null;

	/**
	 * <sliceID,<pnode id, vnode id>> known so far
	 */
	private LinkedHashMap<Integer,LinkedHashMap<Integer, Integer>> _nodeMappingMap  = null;

	/**
	 * to reset to this value when the bundle is reset
	 */
	private double _nodeStressBeforeThisSliceRequest = 0.01;


	/**
	 * global node capacity
	 */
	private double _nodeStress = 0.01;

	/**
	 * pnode utility function
	 */
	private PnodeUtil _NodeUtil= null;

	/**
	 * ISP owner of this physical node 
	 */
	private String _owner = null;

	/**
	 * application name
	 */
	private String _pNodeName = null; 
	/**
	 * initial stress on physical node
	 */
	private double _stress = 0.01;

	/**
	 * target node capacity
	 */
	private double _targetNodeCapacity = 100.0;


	/**
	 * target adjacent link capacity
	 */
	private double _targetLinkCapacity = 500.0;

	/**
	 * target stress
	 */
	private double _targetStress = 1.0;

	/**
	 * <sliceID, iteration_t >
	 */
	private LinkedHashMap<Integer, Integer> _iterationMap  =null;


	/**
	 * node bidding utility function
	 */
	private String nodeUtility = null;//cad.utility = utility1

	/**
	 * assignment Vector: least or most informative (x or a)
	 */
	private String assignmentVectorPolicy = null; //# least or most cad.assignmentVector = least
	/**
	 * bidVectorLength
	 */
	private int bidVectorLengthPolicy = 0;//	cad.bidVectorLength = 1 

	/**
	 * owner ISP
	 */
	private String ownerISP = null;
	/**
	 * in case a pnode is an ISP 
	 */
	private LinkedHashMap<Integer, Pnode> pnodeChildren = null;
	/**
	 * <dest pnode ID, next hop pnode ID>
	 */
	private LinkedHashMap<Integer,Integer> fwTable = null;

	/**
	 * kill this node's communication
	 */
	private boolean stop = false;

	/**
	 * node bidding implementation
	 */
	private NodeBiddingImpl _biddingImpl = null;
	/**
	 * node agreement implementation
	 */
	private NodeAgreementImpl _agreementImpl = null;

	/**
	 * partial replica of RIB for bidding and agreement 
	 */
	private BiddingData _currentBiddingData = null;

	/**
	 * list of embedded sliceIDs seen so far by this pnode
	 */
	private LinkedList<Integer> _embeddedSlices = null;

	/**
	 * instance to call message implementation
	 */
	private CADMessageImpl cadMsgImpl = new CADMessageImpl();

	/**
	 * ongoing slice to be embedded
	 */
	private LinkedHashMap<Integer,Slice> _onGoingEmbedding = new LinkedHashMap<Integer,Slice>();
	
	/**
	 * Link embedding implementation
	 */
	private LinkEmbeddingImpl _linkEmbeddingImpl = null;
	
	
	/**
	 * this constructor should be used if the policies are given 
	 * to the node by the slice manager (SM) 
	 * @param appName
	 */
	public Pnode(String pNodeName, String IDDName) {


		super(pNodeName, IDDName);
		
		

		//initialize the pnode
		this.set_pNodeName(pNodeName);
		this.initPnode_noConfigFile();
		this._biddingImpl = new NodeBiddingImpl(this.rib,pNodeName);
		this._agreementImpl = new NodeAgreementImpl(this.rib,pNodeName);
		this._currentBiddingData = new BiddingData();
		
		this._linkEmbeddingImpl = new LinkEmbeddingImpl(super.rib);

		//TODO: load private policies from config file (those that cannot be given by InPManager)


	}



	/**
	 * this constructor should be used if the policies are given 
	 * to the node by the CAD configuration file --- fully distributed architecture 
	 * without slice manager (controller)
	 * @param appName
	 * @param IDDName
	 */
	public Pnode(String pNodeName, String IDDName, String CADconfigFile) {



		super(pNodeName, IDDName);



		//load CAD config file
		this._CADconfig = new CADConfig(CADconfigFile);

		//initialize the pnode
		this.set_pNodeName(pNodeName);
		this.initPnode();
		this._biddingImpl = new NodeBiddingImpl(this.rib, pNodeName);
		this._agreementImpl = new NodeAgreementImpl(this.rib,pNodeName);
		this._currentBiddingData = new BiddingData();
		
		this._embeddedSlices = new LinkedList<Integer>();
		this._onGoingEmbedding = new LinkedHashMap<Integer, SliceSpec.Slice>();

		//TODO: discover the applications reachable
		//		int subID = this.rib.getRibDaemon().createPub(3, "appsReachableApp");
		//		this._appsReachable = (LinkedList<String>) this.rib.getRibDaemon().readSub(subID);
		//		this.rib.getRibDaemon().createSub(frequency, subName, publisher)		

		// join the DIF of a InPManager using the RIBDaemon 
		// subscribe to neighbor compute physical topology for routing

		// establish a flow with all your direct neighbors  
		//		String dstName = "pnode2";
		//		int handlePnode2 = this.irm.allocateFlow(this.getAppName(), dstName);

		//wait for slice request

	}


	/**
	 * Handle pnode CDAP messages
	 */
	public void handleAppCDAPmessage(byte[] msg) {

		//	this.rib.RIBlog.debugLog("Pnode::handleAppCDAPmessage: this.irm.getUnderlyingIPCs().get(pnode1).getRib().getMemberList(): "+this.irm.getUnderlyingIPCs().get("pnode1").getRib().getMemberList());

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

			rib.RIBlog.infoLog("======= Pnode::handleAppCDAPmessage "+this.getAppName()+" ObjClass: " + cdapMessage.getObjClass());
			rib.RIBlog.infoLog("======= Pnode::handleAppCDAPmessage "+this.getAppName()+" ObjName:  "+ cdapMessage.getObjName());
			rib.RIBlog.infoLog("======= Pnode::handleAppCDAPmessage "+this.getAppName()+" SrcAEName: " + cdapMessage.getSrcAEName());					


			if(cdapMessage.getObjClass().toLowerCase().equals("slice"))
			{
				handleSliceRequest(cdapMessage);		
			}
			else if(cdapMessage.getObjName().toLowerCase().equals("first bid"))
			{
				handleFirstBidMessage(cdapMessage);	
			}
			else if(cdapMessage.getObjName().toLowerCase().equals("bid"))
			{
				handleBidMessage(cdapMessage);		
			}
			else if(cdapMessage.getObjClass().toLowerCase().equals("linkembedding")) {
				handleLinkEmbedding(cdapMessage);
			}
			else {
				rib.RIBlog.errorLog("======= Pnode: "+this.getAppName()+" ======= Object Class: "+cdapMessage.getObjClass()+" not handled yet");
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();

		}
	}









	/**
	 * bids and send the bids over to all neighbors
	 * @param cdapMessage
	 */
	protected void handleSliceRequest(CDAPMessage cdapMessage) {

		Slice sliceRequested = null;
		try {
			sliceRequested = SliceSpec.Slice.parseFrom(cdapMessage.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("Error Parsing Slice from Slice Provider");
			e1.printStackTrace();
		}	



		//get sliceID
		int sliceID = sliceRequested.getSliceID();

		//log new slice to be embedded
		if(this._embeddedSlices.contains(sliceID)) {
			rib.RIBlog.warnLog("Pnode::handleSliceRequest: slice: "+sliceID+ "already embedded");
			
		}else if(!this._onGoingEmbedding.containsKey(sliceID)) {
			this._onGoingEmbedding.put(sliceID, sliceRequested);
			this.rib.addAttribute("onGoingEmbedding", _onGoingEmbedding);
			rib.RIBlog.infoLog("Pnode::handleSliceRequest: embedding slice: "+sliceID);
		}
		//initialize data structures for this slice
		initializeCADStructures(sliceRequested);

		// prepare data for bidding
		_currentBiddingData = generateBiddingData();

		//bid
		BiddingData biddingData = _biddingImpl.nodeBidding(sliceRequested, _currentBiddingData);

//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: BEFORE updateBiddingData _currentBiddingData  "+_currentBiddingData );
//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: BEFORE updateBiddingData _currentBiddingData.get_allocationVectorMap() "+_currentBiddingData.get_allocationVectorMap() );
//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: BEFORE updateBiddingData _currentBiddingData.get_allocationVector(sliceID)  "+_currentBiddingData.get_allocationVector(sliceRequested.getSliceID()) );

		//update local data structures
		_currentBiddingData = updateBiddingData(biddingData);

//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: AFTER updateBiddingData _currentBiddingData  "+_currentBiddingData );
//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: AFTER updateBiddingData _currentBiddingData.get_allocationVectorMap() "+_currentBiddingData.get_allocationVectorMap() );
//		rib.RIBlog.debugLog("Pnode::handleSliceRequest: AFTER updateBiddingData _currentBiddingData.get_allocationVector(sliceID)  "+_currentBiddingData.get_allocationVector(sliceRequested.getSliceID()) );

		// prepare CAD payload

		CAD.CADMessage cadMessage = cadMsgImpl.generateFirstBidMessage(
				sliceRequested,								// slice to piggyback 
				sliceID,									// mandatory slice ID
				get_allocationPolicy(),						// SAD or MAD
				this._allocationVectorMap.get(sliceID),		// allocation vector a
				this._bidVectorMap.get(sliceID),			// bid Vector b,
				this._biddingTimeMap.get(sliceID),			// bidding time vector 
				this._mMap.get(sliceID)); 					// bundle m
		//   null); 					// bundle m

		rib.RIBlog.debugLog("================================================");
		rib.RIBlog.debugLog("================================================");

		//TODO: fixme extending the RIBdaemonImpl at any application
		//   this is wrong because we need to send the messages to the appName not to the underlying IPCs: 
		//	 for now it's ok if they have the same name, but we need to create the subscription to gather
		//	 all the appReachable that are one hop away only

		rib.RIBlog.debugLog("Pnode::handleSliceRequest: this.getAppName()"+this.getAppName());
		LinkedList<String> neighborList = this.irm.getUnderlyingIPCs().get(this.getAppName()).getRib().getMemberList();
		rib.RIBlog.debugLog("Pnode::handleSliceRequest: neighborList BEFORE removing itself: "+neighborList);
		
		
		LinkedHashMap<String, IPCProcessImpl> myUnderlyingIPCs = this.irm.getUnderlyingIPCs();

		Set<String> SetCurrentMaps = myUnderlyingIPCs.keySet();
		Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();
		while(KeyIterMaps.hasNext()){//remove itself from the neighbors
			String underlyingIPCName = KeyIterMaps.next();
			if(neighborList.contains(underlyingIPCName))
				neighborList.remove(underlyingIPCName);
		}
		rib.RIBlog.debugLog("Pnode::handleSliceRequest: neighborList AFTER removing itself: "+neighborList);

		//it has the slice info in it as well
		rib.RIBlog.debugLog("Pnode::handleSliceRequest: sending first BID to neighborList: "+neighborList);

		sendFirstBIDMessage(cadMessage, neighborList ); //to neighbor

	}




	/**
	 * handle message with slice info and bid 
	 * @param cdapMessage
	 */
	protected void handleFirstBidMessage(CDAPMessage cdapMessage) {

		//start a timeout after which send

		//this.sendResponseToSP(0, cadMessage.getSliceID());



		CAD.CADMessage cadMessage  = null;
		try {
			cadMessage = CAD.CADMessage.parseFrom(cdapMessage.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("Pnode::handleBidMessage: Error Parsing CAD message from Slice Provider");
			e1.printStackTrace();
		}

		rib.RIBlog.infoLog("Pnode::handleBidMessage: first Bid Message received by: "+cdapMessage.getSrcAEName());


		// get slice from the message

		CAD.CADMessage.Slice piggyBackedSlice = null;
		if(cadMessage.hasSliceRequest()){
			piggyBackedSlice = cadMessage.getSliceRequest();
		}else{
			rib.RIBlog.errorLog("Pnode::handleFirstBidMessage: there is no slice piggybacked ");
		}

		//reconstruct the SliceSpec.Slice slice
		SliceSpec.Slice sliceToEmbed =  _NodeUtil.reconstructSlice(piggyBackedSlice); 

		//////////////////////////////////////////BIDDING //////////////////////////////////////////

		//initialize data structures for this slice
		initializeCADStructures(sliceToEmbed);

		// prepare data for bidding
		_currentBiddingData = generateBiddingData();

		//bid first then check for agreement
		BiddingData biddingData = _biddingImpl.nodeBidding(sliceToEmbed, this._currentBiddingData);

		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: BEFORE updateBiddingData _currentBiddingData  "+_currentBiddingData );
		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: BEFORE updateBiddingData _currentBiddingData.get_allocationVectorMap() "+_currentBiddingData.get_allocationVectorMap() );
		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: BEFORE updateBiddingData _currentBiddingData.get_allocationVector(sliceID)  "+_currentBiddingData.get_allocationVector(sliceToEmbed.getSliceID()) );

		//update local data structures
		_currentBiddingData = updateBiddingData(biddingData);

		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: AFTER updateBiddingData _currentBiddingData  "+_currentBiddingData );
		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: AFTER updateBiddingData _currentBiddingData.get_allocationVectorMap() "+_currentBiddingData.get_allocationVectorMap() );
		rib.RIBlog.debugLog("Pnode::handleFirstBidMessage: AFTER updateBiddingData _currentBiddingData.get_allocationVector(sliceID)  "+_currentBiddingData.get_allocationVector(sliceToEmbed.getSliceID()) );


		//////////////////////////////////////////// AGREEMENT //////////////////////////////////////////

		// now that the node has bid, it checks the incoming bid for agreement

		//bid
		biddingData = _agreementImpl.nodeAgreement(cadMessage, this._currentBiddingData, cdapMessage.getSrcAEName(), cdapMessage.getDestAEName());


		//update local data structures
		updateDataAfterAgreement(biddingData);

		rib.RIBlog.infoLog("================================================");
		rib.RIBlog.infoLog("================================================");
		rib.RIBlog.infoLog("======= FIRST AGREEMENT PHASE COMPLETE =========");
		rib.RIBlog.infoLog("================================================");
		rib.RIBlog.infoLog("================================================");

		rib.RIBlog.infoLog("Pnode::handleFirstBidMessage:: rebroadcast message?: "+biddingData.get_rebroadcast());

		if(biddingData.get_rebroadcast()){
			//rebroadcast
			int sliceID = cadMessage.getSliceID();
			CAD.CADMessage cadMessageToBroadcast = cadMsgImpl.generateCADMessage(
					sliceID,									// mandatory slice ID
					get_allocationPolicy(),						// SAD or MAD
					this._allocationVectorMap.get(sliceID),		// allocation vector a
					this._bidVectorMap.get(sliceID),			// bid Vector b,
					this._biddingTimeMap.get(sliceID),			// time stamp of bids
					this._mMap.get(sliceID)); 					// bundle m
			//null); 					// bundle m



			//TODO: extend the RIBdaemonImpl at any application
			//   this is not clean as we need to send the messages to the appName not to the underlying IPCs: 
			//	 for now it's ok if they have the same name, but we need to create the subscription to gather
			//	 all the appReachable that are one hop away only


			LinkedList<String> neighborList = this.irm.getUnderlyingIPCs().get(this._pNodeName).getRib().getMemberList();
			rib.RIBlog.infoLog("Pnode::handleFirstBidMessage: neighborList BEFORE REMOVING: "+neighborList);

			
			LinkedHashMap<String, IPCProcessImpl> myUnderlyingIPCs = this.irm.getUnderlyingIPCs();//
			Set<String> SetCurrentMaps = myUnderlyingIPCs.keySet();
			Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();
			while(KeyIterMaps.hasNext()){
				String underlyingIPCName = KeyIterMaps.next();
				if(neighborList.contains(underlyingIPCName) || neighborList.contains(this.get_owner()))
					neighborList.remove(underlyingIPCName);
			}

			rib.RIBlog.infoLog("Pnode::handleFirstBidMessage: neighborList AFTER REMOVING: "+neighborList);
			
			sendBIDMessage(cadMessageToBroadcast, neighborList); //to neighbor
		}else{
			rib.RIBlog.infoLog("Pnode::handleFirstBidMessage: AGREEMENT: No need to send anything back ");
			rib.RIBlog.infoLog("Pnode::handleFirstBidMessage: sending Response To SP...");
			this.sendResponseToSP(1, cadMessage.getSliceID());
		}



	}






	/**
	 * 
	 * @param cdapMessage
	 */
	protected void handleBidMessage(CDAPMessage cdapMessage) {

		CAD.CADMessage cadMessage  = null;
		try {
			cadMessage = CAD.CADMessage.parseFrom(cdapMessage.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("Pnode::handleBidMessage: Error Parsing BID message");
			e1.printStackTrace();
		}	

		// prepare data for agreement

		//in case the pnode has never heard about this slice, 
		if(!this._embeddedSlices.contains(cadMessage.getSliceID())){
			//this node should:

			// ask the slice provider for the slice details 
			// bid first on the new slice without sending out the bid message,
			sendSliceRequestMessage(cadMessage.getSliceID());
			//	then come back here ,check for consensus and generate the response
		}


		//_currentBiddingData = generateBiddingData();

		// prepare data for bidding
		rib.RIBlog.debugLog("Pnode::handleBidMessage: cdapMessage.getSrcAEName() = k: "+cdapMessage.getSrcAEName() );
		rib.RIBlog.debugLog("Pnode::handleBidMessage: cdapMessage.getDestAEName() = i: "+cdapMessage.getDestAEName() );

		rib.RIBlog.debugLog("Pnode::handleBidMessage:BEFORE AGREEMENT _currentBiddingData  "+_currentBiddingData );
		rib.RIBlog.debugLog("Pnode::handleBidMessage:BEFORE AGREEMENT _currentBiddingData.get_allocationVectorMap() "+_currentBiddingData.get_allocationVectorMap() );
		//rib.RIBlog.debugLog("Pnode::handleBidMessage:BEFORE AGREEMENT _currentBiddingData.get_allocationVector(sliceID)  "+_currentBiddingData.get_allocationVector(cadMessage.getSliceID()) );


		//bid
		BiddingData biddingData = _agreementImpl.nodeAgreement(cadMessage, _currentBiddingData, cdapMessage.getSrcAEName(), cdapMessage.getDestAEName());

		//update local data structures
		updateDataAfterAgreement(biddingData);

		//if a_i != a_k send otherwise don't send anything and log node agreement reached
		LinkedHashMap<Integer,String> a_i = _currentBiddingData.getA_i();
		rib.RIBlog.debugLog("Pnode::handleBidMessage: a_i: "+a_i );
		LinkedHashMap<Integer,String> a_k = _currentBiddingData.getA_k();
		rib.RIBlog.debugLog("Pnode::handleBidMessage: a_k: "+a_k );
		boolean agreementReached = true;

		Set<Integer> a_iKeys = a_i.keySet();
		Iterator<Integer> aIter = a_iKeys.iterator();
		while(aIter.hasNext()){
			int vnodeID = aIter.next();
			rib.RIBlog.debugLog("Pnode::handleBidMessage: a_k for vnodeID: "+vnodeID+" is "+a_k.get(vnodeID) );
			rib.RIBlog.debugLog("Pnode::handleBidMessage: a_i for vnodeID: "+vnodeID+" is "+a_i.get(vnodeID) );

			if(!a_k.get(vnodeID).equals(a_i.get(vnodeID))){
				agreementReached = false;
			}
			rib.RIBlog.debugLog("Pnode::handleBidMessage: agreementReached "+agreementReached);

		}




		// even if the agreement is reached pnode may still have to rebroacast the current message

		if(biddingData.get_rebroadcast()){
			//rebroadcast
			int sliceID = cadMessage.getSliceID();
			CAD.CADMessage cadMessageToBroadcast = cadMsgImpl.generateCADMessage(
					sliceID,									// mandatory slice ID
					get_allocationPolicy(),						// SAD or MAD
					this._allocationVectorMap.get(sliceID),		// allocation vector a
					this._bidVectorMap.get(sliceID),			// bid Vector b,
					this._biddingTimeMap.get(sliceID),			// time stamp of bids
					this._mMap.get(sliceID)); 					// bundle m
			//null); 					// bundle m

			rib.RIBlog.debugLog("================================================");
			rib.RIBlog.debugLog("================================================");

			//TODO: fixme extending the RIBdaemonImpl at any application
			//   this is wrong because we need to send the messages to the appName not to the underlying IPCs: 
			//	 for now it's ok if they have the same name, but we need to create the subscription to gather
			//	 all the appReachable that are one hop away only

			LinkedList<String> neighborList = this.irm.getUnderlyingIPCs().get(this._pNodeName).getRib().getMemberList();
			LinkedHashMap<String, IPCProcessImpl> myUnderlyingIPCs = this.irm.getUnderlyingIPCs();//
			Set<String> SetCurrentMaps = myUnderlyingIPCs.keySet();
			Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();
			while(KeyIterMaps.hasNext()){
				String underlyingIPCName = KeyIterMaps.next();
				if(neighborList.contains(underlyingIPCName))
					neighborList.remove(underlyingIPCName);
			}


			this.sendBIDMessage(cadMessageToBroadcast, neighborList); //to neighbor

		} // end rebroadcast 

		// maybe some other messsage is coming! how can we check that too? 
		// we should set the bound based on the diameter, 
		// or just check if all the pnodeID have responded and set a timeout id they haven't, 
		// or we just create proactively respond as soon as we see agreement and in case we change it later updating the alloc

		if(agreementReached) { 
			rib.RIBlog.infoLog("============================================================================================");
			rib.RIBlog.infoLog("============================================================================================");
			rib.RIBlog.infoLog("CADsys: Pnode: "+this.get_pNodeName()+" has REACHED AGREEMENT SO FAR ON VIRTUAL NETWORK ID: "+cadMessage.getSliceID());
			rib.RIBlog.infoLog("============================================================================================");
			rib.RIBlog.infoLog("============================================================================================");

			//1 allocated, 0 not allocated after timeout
			//reset timeout
			rib.RIBlog.debugLog("Pnode::handleBidMessage: sending response to SP");
			this.sendResponseToSP(1, cadMessage.getSliceID());

		}

		//		assignment.Builder  a_ij = assignment.newBuilder();
		//		a_ij.setVNodeId(vnodeId);
		//		a_ij.setHostingPnodeName(hostingPnodeName);
		//		cadMessage.addA(a_ij);


	}


	/**
	 * start virtual links and update capacities
	 * @param cdapMessage
	 */
	protected void handleLinkEmbedding(CDAPMessage cdapMessage) {
		if(cdapMessage==null)
			rib.RIBlog.errorLog("Pnode::handleLinkEmbedding: cdapMessage is null");

		Slice slice =null;
		int sliceID =-1;
		try {
			slice = SliceSpec.Slice.parseFrom(cdapMessage.getObjValue().getByteval());
			sliceID = slice.getSliceID();
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("Error Parsing Slice from Slice Provider");
			e1.printStackTrace();
		}
		if(sliceID ==-1) return;
		else {
			rib.RIBlog.infoLog("===================================================================");
			rib.RIBlog.infoLog("Pnode::handleLinkEmbedding: ------check if there are resources-----");
			rib.RIBlog.infoLog("Pnode::handleLinkEmbedding: ------create vlink here ---------------");
			rib.RIBlog.infoLog("===================================================================");

			
			//TODO: log only if link embedding is successful 
			// this is only for stats and should be moved after the link allocation is really done  
			if(this._onGoingEmbedding.containsKey(sliceID)) {
				this._onGoingEmbedding.remove(sliceID);
				this._embeddedSlices.add(sliceID);
				//refresh
				if(this.rib.hasMember("onGoingEmbedding")) {
					this.rib.removeAttribute("onGoingEmbedding");
					this.rib.addAttribute("_onGoingEmbedding", _onGoingEmbedding);
					rib.RIBlog.debugLog("Pnode::handleLinkEmbedding: _onGoingEmbedding updated");

				}
			}
			

			if(slice != null) {
				
				//linkEmbeddingImpl.createTopology(slice);
				_linkEmbeddingImpl.createVNmultiInP(slice);
			}else {
				rib.RIBlog.warnLog("Pnode::handleLinkEmbedding: Slice empty: nothing to create!!!!!!!!!!!!");
				return;
			}
			
		}

		//		this.rib.addAttribute("onGoingEmbedding", _onGoingEmbedding);



	}



	/**
	 * send to Service provider a message to communicate that an agreement has been reached
	 * @param type 0 no agreement 1 agreement
	 * @param sliceID
	 */
	private void sendResponseToSP(int type, int sliceID) {

		rib.RIBlog.debugLog("Pnode::sendResponseToSP: type: "+type);
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: sliceID: "+sliceID);
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: get_allocationPolicy(): "+get_allocationPolicy());
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: _allocationVectorMap.get(sliceID): "+this._allocationVectorMap.get(sliceID));
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: this._bidVectorMap.get(sliceID): "+this._bidVectorMap.get(sliceID));


		CAD.CADMessage cadMessageResponseToSP = cadMsgImpl.generateSpResponse(
				sliceID,									// mandatory slice ID
				get_allocationPolicy(),						// SAD or MAD
				this._allocationVectorMap.get(sliceID),		// allocation vector a
				this._bidVectorMap.get(sliceID),			// bid Vector b,
				this._biddingTimeMap.get(sliceID)			// time stamp of bids
				);					 			 



		// generate new message in response
		//create the "object value" for the CDAP message from the cadMessage payload
		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString CADByteString = ByteString.copyFrom(cadMessageResponseToSP.toByteArray());
		ObjValue.setByteval(CADByteString);
		//payload of the CDAP message 
		CDAP.objVal_t objvalueCAD = ObjValue.buildPartial();
		// send it to SP

		String dstName = this.getMySP();
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: preparing message for SP: "+dstName);


		//allocate a flow or get the handle of the previously allocated flow (RINA API)
		int handle = -1;
		handle = this.irm.getHandle(dstName);
		
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: handle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));
		if (handle == -1){
			this.irm.allocateFlow(this.getAppName(), dstName);
		}
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: getHandle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: handle for flow with "+dstName+" is :"+handle);


		String PositiveOrNegative = "positive";
		if (type ==0)
		{
			PositiveOrNegative = "negative";
		}


		CDAP.CDAPMessage M_WRITE = message.CDAPMessage.generateM_WRITE(
				"response", //ObjClass
				PositiveOrNegative, // ObjName,
				objvalueCAD, // objvalue
				dstName,//destAEInst,
				dstName,//destAEName, 
				dstName,//destApInst, 
				dstName,//destApName, 
				00001, //invokeID, 
				this.getAppName(),//srcAEInst
				this.getAppName(),//srcAEName
				this.getAppName(),//srcApInst
				this.getAppName()//srcApName
				);


		rib.RIBlog.debugLog("Pnode::sendResponseToSP: sent to: "+M_WRITE.getDestApName());
		int byteOverhead = M_WRITE.toByteArray().length;
		rib.RIBlog.debugLog("Pnode::sendResponseToSP: message overhead from: "+this._pNodeName+" to: "+dstName+" is "+byteOverhead);
		
		
		
		try {
			irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
			rib.RIBlog.debugLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			rib.RIBlog.debugLog("Pnode::sendResponseToSP: CDAP message with POSITIVE RESPONSE sent to "+dstName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}



	private void sendBIDMessage(
			dap.cad.impl.googleprotobuf.CAD.CADMessage cadMessageToBroadcast,
			LinkedList<String> neighborList) {

		rib.RIBlog.debugLog("==================== inside sendBIDMessage: cadMessageToBroadcast =============================");

		rib.RIBlog.debugLog("Pnode::sendBIDMessage: neighborList: "+neighborList);


		//create the "object value" for the CDAP message from the cadMessage payload
		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString CADByteString = ByteString.copyFrom(cadMessageToBroadcast.toByteArray());
		ObjValue.setByteval(CADByteString);
		//payload of the CDAP message 
		CDAP.objVal_t objvalueCAD = ObjValue.buildPartial();

		Iterator<String> neighborsIter = neighborList.iterator();

		while (neighborsIter.hasNext()){

			String dstName = neighborsIter.next();
			rib.RIBlog.debugLog("Pnode::sendBIDMessage: dstName = "+dstName);

			if(dstName.equals(getMySP()) || dstName.equals("sliceManagerIPC") || dstName.equals("InPManagerIPC")
					|| dstName.equals(this.get_owner()) ){ //this should be either "sliceManagerIPC" or "InPManagerIPC" but we never know  
				rib.RIBlog.debugLog("Pnode::sendBIDMessage: not sending anything to: "+dstName);
				continue;
			}
			//int handle = this.irm.allocateFlow(this.getAppName(), dstName);
			//allocate a flow or get the handle of the previously allocated flow (RINA API)
			int handle = -1;
			handle = this.irm.getHandle(dstName);
			rib.RIBlog.debugLog("Pnode::sendCADMessage: handle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));
			if (handle == -1){
				this.irm.allocateFlow(this.getAppName(), dstName);
			}
			rib.RIBlog.debugLog("Pnode::sendCADMessage: getHandle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));
			rib.RIBlog.debugLog("Pnode::sendBIDMessage: handle for flow with "+dstName+" is :"+handle);


			CDAP.CDAPMessage M_WRITE = message.CDAPMessage.generateM_WRITE(
					"bid", //objclass
					"bid", // ObjName, //
					objvalueCAD, // objvalue
					dstName,//destAEInst,
					dstName,//destAEName, 
					dstName,//destApInst, 
					dstName,//destApName, 
					00001, //invokeID, 
					this.getAppName(),//srcAEInst
					this.getAppName(),//srcAEName
					this.getAppName(),//srcApInst
					this.getAppName()//srcApName
					);


			int byteOverhead = M_WRITE.toByteArray().length;
			rib.RIBlog.debugLog("Pnode::sendBIDMessage: message overhead from: "+this._pNodeName+" to: "+dstName+" is "+byteOverhead);

			try {
				irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
				rib.RIBlog.debugLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				rib.RIBlog.debugLog("Pnode::sendBIDMessage: CDAP message with BID payload sent to "+dstName);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		//rib.RIBlog.debugLog("==================== nothing to do yet =============================");
		rib.RIBlog.debugLog("==================== bid message rebroadcasted =============================");


	}



	/**
	 * update local data structure of pnode after the agreement phase
	 * @param biddingData
	 */
	private void updateDataAfterAgreement(BiddingData biddingData) {

		this._allocationVectorMap = biddingData.get_allocationVectorMap();
		this._bidVectorMap = biddingData.get_bidVectorMap();
		this._biddingTimeMap = biddingData.get_biddingTimeMap();
		this._mMap = biddingData.get_mMap();

	}



	/**
	 * create structure for nodeBidding
	 * @return BiddingData
	 */
	private BiddingData updateBiddingData(BiddingData biddingData) {

		biddingData.set_pNodeName(_pNodeName);
		biddingData.set_allocationPolicy(_allocationPolicy);
		biddingData.set_allocationVectorMap(_allocationVectorMap);
		biddingData.set_bidVectorMap(_bidVectorMap);

		biddingData.set_biddingTimeMap(_biddingTimeMap);

		biddingData.set_iterationMap(_iterationMap);
		biddingData.set_mMap(_mMap);
		biddingData.set_mySP(_mySP);
		biddingData.set_nodeMappingMap(_nodeMappingMap);
		biddingData.set_nodeStress(_nodeStress);
		biddingData.set_nodeStressBeforeThisSliceRequest(_nodeStressBeforeThisSliceRequest);
		biddingData.set_NodeUtil(_NodeUtil);
		biddingData.set_stress(_stress);
		biddingData.set_targetLinkCapacity(_targetLinkCapacity);
		biddingData.set_targetNodeCapacity(_targetNodeCapacity);
		biddingData.set_targetStress(_targetStress);



		return biddingData;
	}



	/**
	 * create structure for nodeBidding
	 * @return BiddingData
	 */
	private BiddingData generateBiddingData() {

		BiddingData biddingData = new BiddingData();

		biddingData.set_pNodeName(_pNodeName);
		biddingData.set_allocationPolicy(_allocationPolicy);

		biddingData.set_allocationVectorMap(_allocationVectorMap);

		biddingData.set_bidVectorMap(_bidVectorMap);

		biddingData.set_biddingTimeMap(_biddingTimeMap);

		biddingData.set_iterationMap(_iterationMap);
		biddingData.set_mMap(_mMap);
		biddingData.set_mySP(_mySP);
		biddingData.set_nodeMappingMap(_nodeMappingMap);
		biddingData.set_nodeStress(_nodeStress);
		biddingData.set_nodeStressBeforeThisSliceRequest(_nodeStressBeforeThisSliceRequest);
		biddingData.set_NodeUtil(_NodeUtil);
		biddingData.set_stress(_stress);
		biddingData.set_targetLinkCapacity(_targetLinkCapacity);
		biddingData.set_targetNodeCapacity(_targetNodeCapacity);
		biddingData.set_targetStress(_targetStress);

		return biddingData;
	}




	/**
	 * send bids message to neighbors 
	 * @param cadMessage already built() with Google buffer protocol
	 * @param neighborList
	 */
	private void sendFirstBIDMessage(CAD.CADMessage cadMessage, LinkedList<String> neighborList) {

		if(neighborList == null)
		{
			rib.RIBlog.warnLog("                                                             ");
			rib.RIBlog.warnLog("*************************************************************");
			rib.RIBlog.warnLog("Pnode::sendFirstBIDMessage: neighborList is empty: no message sent");
			rib.RIBlog.warnLog("*************************************************************");
			rib.RIBlog.warnLog("                                                             ");
			return;
		}
		rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: neighborList BEFORE: "+neighborList);


		rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: getMySP() BEFORE: "+getMySP());
		//create the "object value" for the CDAP message from the cadMessage payload
		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString CADByteString = ByteString.copyFrom(cadMessage.toByteArray());
		ObjValue.setByteval(CADByteString);
		//payload of the CDAP message 
		CDAP.objVal_t objvalueCAD = ObjValue.buildPartial();

		Iterator<String> neighborsIter = neighborList.iterator();

		while (neighborsIter.hasNext()){

			String dstName = neighborsIter.next();
			rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: dstName = "+dstName);

			if(dstName.equals(getMySP()) || dstName.equals("sliceManagerIPC") || dstName.equals("InPManagerIPC")){
				rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: not sending anything to: "+dstName);
				continue;
			}
			int handle = this.irm.allocateFlow(this.getAppName(), dstName);
			rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: handle for flow with "+dstName+" is :"+handle);


			CDAP.CDAPMessage M_WRITE = message.CDAPMessage.generateM_WRITE(
					"bid", //objclass
					"first bid", // ObjName, //
					objvalueCAD, // objvalue
					dstName,//destAEInst,
					dstName,//destAEName, 
					dstName,//destApInst, 
					dstName,//destApName, 
					00001, //invokeID, 
					this.getAppName(),//srcAEInst
					this.getAppName(),//srcAEName
					this.getAppName(),//srcApInst
					this.getAppName()//srcApName
					);



			rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: handle for flow to destination "+dstName+" is :"+this.irm.getHandle(dstName));
			int byteOverhead = M_WRITE.toByteArray().length;
			rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: message overhead from: "+this._pNodeName+" to: "+dstName+" is "+byteOverhead);
			

			try {
				irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
				rib.RIBlog.debugLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				rib.RIBlog.debugLog("Pnode::sendFirstBIDMessage: CDAP message with BID and Slice payload sent to "+dstName);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}



	/**
	 * TODO: finish support for this (pulling requests in a p2p manner)
	 * ask for this slice to any neighbor or to slice provider
	 * @param sliceID
	 */
	private void sendSliceRequestMessage(int sliceID) {

		//TODO: replace with a subscription event to slice provider or to any pnode
		//create the "object value" for the CDAP message from the cadMessage payload


		//instantiate a generator
		//SliceGenerator sliceGenerator = new SliceGenerator(sliceID);

		//create and empty slice with the given id (payload of "slice request" message)
	//	SliceSpec.Slice.Builder sliceToRequest = sliceGenerator.getSlice(); 
	//	sliceToRequest.buildPartial(); 


		//generate CDAP object serializing payload  of "slice request" message
	//	CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();

//		ByteString sliceByteString = ByteString.copyFrom(sliceToRequest.build().toByteArray());
//		ObjValue.setByteval(sliceByteString);
//		CDAP.objVal_t objvalueSlice = ObjValue.buildPartial();


		//set destination
		String dstName = this.getMySP();
		//allocate a flow to the slice provider or get the handle of the previosuly allocated flow (RINA API)
		int handle = -1;
		handle = this.irm.getHandle(dstName);
		rib.RIBlog.debugLog("Pnode::sendCADMessage: handle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));
		if (handle == -1){
			this.irm.allocateFlow(this.getAppName(), dstName);
		}
		rib.RIBlog.debugLog("Pnode::sendCADMessage: handle for flow with "+dstName+" is :"+this.irm.getHandle(dstName));

		//rib.RIBlog.debugLog("Pnode::sendCADMessage: handle for flow with "+this.getMySP()+" is :"+handle);

		//build message
		CDAP.CDAPMessage M_READ = message.CDAPMessage.generateM_READ(
				"slice request", //objclass
				"slice request ", // ObjName, //
				null, // objvalue
				dstName,//destAEInst,
				dstName,//destAEName, 
				dstName,//destApInst, 
				dstName,//destApName, 
				00001, //invokeID, 
				this.getAppName(),//srcAEInst
				this.getAppName(),//srcAEName
				this.getAppName(),//srcApInst
				this.getAppName()//srcApName
				);


		try {
			irm.sendCDAP(irm.getHandle(dstName), M_READ.toByteArray());

			rib.RIBlog.debugLog("Pnode::sendCADMessage: CDAP message with SLICE REQUEST payload sent to "+dstName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}





	/**
	 * all config is given by the sliceManager (controller)	
	 */
	private void initPnode_noConfigFile() {

		this._adjacentBandwidthMap = new LinkedHashMap<Integer, LinkedHashMap<Integer,Double>>();
		this._adjacentLinksMap = new LinkedHashMap<Integer,LinkedList<Integer>>();   
		this._nodeMappingMap = new LinkedHashMap<Integer,LinkedHashMap<Integer, Integer>>();
		this._allocationVectorMap = new LinkedHashMap<Integer, LinkedHashMap<Integer,String>>();
		this._bidVectorMap = new LinkedHashMap<Integer, LinkedList<Double>>();
		this._mMap = new LinkedHashMap<Integer, LinkedList<Integer>>();
		this._iterationMap = new LinkedHashMap<Integer, Integer>();
		this._NodeUtil = new PnodeUtil("residual_node_capacity", this.rib);

		this._embeddedSlices = new LinkedList<Integer>();

	}


	/**
	 * Initialize physical node structures including parsing from
	 * configuration file information on policies
	 */
	private void initPnode() {

		this._adjacentBandwidthMap = new LinkedHashMap<Integer, LinkedHashMap<Integer,Double>>();
		this._adjacentLinksMap = new LinkedHashMap<Integer,LinkedList<Integer>>();   
		this._nodeMappingMap = new LinkedHashMap<Integer,LinkedHashMap<Integer, Integer>>();
		this._allocationVectorMap = new LinkedHashMap<Integer, LinkedHashMap<Integer,String>>();
		this._bidVectorMap = new LinkedHashMap<Integer, LinkedList<Double>>();
		this._mMap = new LinkedHashMap<Integer, LinkedList<Integer>>();
		this._iterationMap = new LinkedHashMap<Integer, Integer>();
		this._embeddedSlices = new LinkedList<Integer>();

		this._biddingTimeMap = new LinkedHashMap<Integer, LinkedHashMap<Integer,Long>>();
		//		this.pnodeChildren= new LinkedHashMap<Integer,Pnode>(); //unused for now
		//		this.fwTable = new LinkedHashMap<Integer,Integer>();    //unused for now

		// Support for a fully Distributed Slice Architecture
		//get policies from config file
		this._id = Integer.parseInt(this._CADconfig.getProperty("cad.id"));
		this.assignmentVectorPolicy = this._CADconfig.getProperty("cad.assignmentVectorPolicy");
		this.bidVectorLengthPolicy = Integer.parseInt(this._CADconfig.getProperty("cad.bidVectorLength"));
		this.nodeUtility = this._CADconfig.getProperty("cad.nodeUtility");
		this.ownerISP = this._CADconfig.getProperty("cad.owner");

		this._incomingLinkCapacity= Integer.parseInt(this._CADconfig.getProperty("cad.incomingLinkCapacity"));
		this._outgoingLinkCapacity= Integer.parseInt(this._CADconfig.getProperty("cad.outgoingLinkCapacity"));

		this._mySP = this._CADconfig.getProperty("cad.mySP");
		
		this._myManager = this._CADconfig.getProperty("cad.owner");

		this._allocationPolicy = this._CADconfig.getProperty("cad.allocationPolicy");

		this._NodeUtil = new PnodeUtil(this._CADconfig.getProperty("cad.nodeUtility"), this.rib);
	}


	/**
	 * initialize bidVector, allocation vector, bundle vector, and node mapping data structures 
	 * @param sliceRequested
	 */
	private void initializeCADStructures(Slice sliceRequested) {


		int sliceID= sliceRequested.getSliceID();

		//register the slice as seen (add it to the known slices)
		if(!this._embeddedSlices.contains(sliceID))
			this._embeddedSlices.add(sliceID);

		int sliceSize = sliceRequested.getVirtualnodeCount();



		// initialize bid vector for all nodes to 0
		LinkedList<Double> b = new LinkedList<Double>(); 
		for(int i=1; i<=sliceSize; i++ )
		{
			b.add(0.0); 
		}
		this._bidVectorMap.put(sliceID, b);

		// initialize a 
		LinkedHashMap<Integer,String> a = new LinkedHashMap<Integer,String>();
		this._allocationVectorMap.put(sliceID, a);

		// initialize m
		LinkedList<Integer> m = new LinkedList<Integer>(); 
		this._mMap.put(sliceID, m);

		// inizialize bidding time
		LinkedHashMap<Integer,Long> biddingTime = new LinkedHashMap<Integer,Long>();
		this._biddingTimeMap.put(sliceID, biddingTime);


		// initialize nodeMapping
		LinkedHashMap<Integer,Integer> nodeMapping = new LinkedHashMap<Integer,Integer>(); 
		this._nodeMappingMap.put(sliceID, nodeMapping);

		// store residual node capacity at this point
		this.set_nodeStressBeforeThisSliceRequest(this.get_nodeCapacity());


	}









	/**
	 * @return the _id
	 */
	public int get_pid() {
		return _id;
	}


	public NodeBiddingImpl getNodeBidding() {
		return this._biddingImpl;
	}



	public void setNodeBidding(NodeBiddingImpl biddingImpl) {
		this._biddingImpl = biddingImpl;
	}



	public NodeAgreementImpl getNodeAgreement() {
		return this._agreementImpl;
	}



	public void setNodeAgreement(NodeAgreementImpl nodeAgreement) {
		this._agreementImpl = nodeAgreement;
	}



	/**
	 * @param _id the _id to set
	 */
	public void set_pid(int id) {
		this._id = id;
	}





	/**
	 * @return the _targetNodeCapacity
	 */
	public double get_targetNodeCapacity() {
		return _targetNodeCapacity;
	}

	public double get_nodeStressBeforeThisSliceRequest() {
		return _nodeStressBeforeThisSliceRequest;
	}

	public void set_nodeStressBeforeThisSliceRequest(
			double _nodeStressBeforeThisSliceRequest) {
		this._nodeStressBeforeThisSliceRequest = _nodeStressBeforeThisSliceRequest;
	}

	/**
	 * @param _targetNodeCapacity the _targetNodeCapacity to set
	 */
	public void set_targetNodeCapacity(double _targetNodeCapacity) {
		this._targetNodeCapacity = _targetNodeCapacity;
	}

	/**
	 * @return the _targetLinkCapacity
	 */
	public double get_targetLinkCapacity() {
		return _targetLinkCapacity;
	}

	/**
	 * @param _targetLinkCapacity the _targetLinkCapacity to set
	 */
	public void set_targetLinkCapacity(double _targetLinkCapacity) {
		this._targetLinkCapacity = _targetLinkCapacity;
	}

	/**
	 * @return the _stress
	 */
	public double get_stress() {
		return _stress;
	}


	/**
	 * @param _stress the _stress to set
	 */
	public void set_stress(double _stress) {
		this._stress = _stress;
	}


	/**
	 * @return the _targetStrss
	 */
	public double get_targetStrss() {
		return _targetStress;
	}


	/**
	 * @param _targetStrss the _targetStrss to set
	 */
	public void set_targetStrss(double _targetStress) {
		this._targetStress = _targetStress;
	}





	/**
	 * @return the _allocationPolicy
	 */
	public String get_allocationPolicy() {
		return _allocationPolicy;
	}

	/**
	 * @param _allocationPolicy the _allocationPolicy to set
	 */
	public void set_allocationPolicy(String _allocationPolicy) {
		this._allocationPolicy = _allocationPolicy;
	}

	/**
	 * @return the _owner
	 */
	public String get_owner() {
		return _owner;
	}



	/**
	 * @param _owner the _owner to set
	 */
	public void set_owner(String _owner) {
		this._owner = _owner;
	}


	/**
	 * @return the mySP
	 */
	public String getMySP() {
		return this._mySP;
	}


	/**
	 * @param mySP the mySP to set
	 */
	public void setMySP(String mySP) {
		this._mySP = mySP;
	}



	/**
	 * @return the _nodeCapacity
	 */
	public double get_nodeCapacity() {
		return _nodeStress;
	}


	/**
	 * @param _nodeCapacity the _nodeCapacity to set before sending the message out
	 */
	public void set_nodeCapacity(double _nodeStress) {
		this._nodeStress = _nodeStress;
	}

	public LinkedList<String> getAppsReachable() {
		return _appsReachable;
	}



	public void setAppsReachable(LinkedList<String> appsReachable) {
		this._appsReachable = appsReachable;
	}



	/**
	 * @return the _pNodeName
	 */
	public String get_pNodeName() {
		return _pNodeName;
	}



	/**
	 * @param _pNodeName the _pNodeName to set
	 */
	public void set_pNodeName(String _pNodeName) {
		this._pNodeName = _pNodeName;
	}



	/**
	 * @return the _incomingLinkCapacity
	 */
	public double get_incomingLinkCapacity() {
		return _incomingLinkCapacity;
	}



	/**
	 * @param _incomingLinkCapacity the _incomingLinkCapacity to set
	 */
	public void set_incomingLinkCapacity(double _incomingLinkCapacity) {
		this._incomingLinkCapacity = _incomingLinkCapacity;
	}



	/**
	 * @return the _outgoingLinkCapacity
	 */
	public double get_outgoingLinkCapacity() {
		return _outgoingLinkCapacity;
	}



	/**
	 * @param _outgoingLinkCapacity the _outgoingLinkCapacity to set
	 */
	public void set_outgoingLinkCapacity(double _outgoingLinkCapacity) {
		this._outgoingLinkCapacity = _outgoingLinkCapacity;
	}



	/**
	 * @return the _myManager
	 */
	public synchronized String get_myManager() {
		return _myManager;
	}



	/**
	 * @param _myManager the _myManager to set
	 */
	public synchronized void set_myManager(String _myManager) {
		this._myManager = _myManager;
	}



	public void printNodeStats(){

		this.rib.RIBlog.infoLog("Pnode stats: ID: "+this._id);
		this.rib.RIBlog.infoLog("Pnode stats: owner ISP: "+this._owner);

		this.rib.RIBlog.infoLog("Pnode stats: Current stress: "+this._stress);
		this.rib.RIBlog.infoLog("Pnode stats: Target stress: "+this._targetStress);
		this.rib.RIBlog.infoLog("Pnode stats: Assignment Vector Policy: "+this.assignmentVectorPolicy);
		this.rib.RIBlog.infoLog("Pnode stats: Node Utility: "+this.nodeUtility);
		this.rib.RIBlog.infoLog("Pnode stats: Bid_Vector_Length Policy: "+this.bidVectorLengthPolicy);




	}



}