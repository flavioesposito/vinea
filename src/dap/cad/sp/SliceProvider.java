/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.sp;

import java.util.Iterator;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;
import dap.cad.config.CADConfig;
import dap.cad.impl.googleprotobuf.CAD;
import dap.cad.impl.googleprotobuf.CAD.CADMessage.assignment;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.Builder;
import dap.cad.sp.slicegenerator.SliceGenerator;
import dap.cad.sp.util.SlicePartition;
import dap.cad.message.impl.CADMessageImpl;
import test.Debug;
import dap.cad.sp.partitioning.impl.SlicePartitioningImpl;
import dap.cad.util.Timeout;
/**
 * @author Flavio Esposito
 *
 */
public class SliceProvider extends Application  {

	/**
	 * serviceProviderName
	 */
	private String _spName = null;
	/**
	 * physical node configuration file  
	 */
	private CADConfig _CADconfig= null;
	/**
	 * slice to embed		
	 */
	//	private CAD.CADMessage.Slice _sliceToEmbed = null;
	//private SliceSpec.Slice.Builder _sliceToEmbed = null;


	/**
	 * residual slice still to embed
	 */
	//private SliceSpec.Slice.Builder _residualSlice= null;

	/**
	 * auction policy, e.g., SAD or MAD : MAD by default
	 */
	private String _vnodeAuctionPolicy = "MAD";

	/**
	 * k = 1 by default
	 */
	private String _kforShortestPath= "1";

	/**
	 * map of service provider policies <key, value> 
	 */
	private LinkedHashMap<String,String> _policyMap= null;


	private int _slicesSuccessfullyAllocated = 0;
	/**
	 * timeout: seconds after which requests is considered not embeddable
	 * if -1 (no timeout) waits forever for pnode requests
	 */
	public int TIMEOUT = -1;//read it from config file: 15*1000 is 15 seconds
	/**
	 * when the sp has a lot of request, we wait an inter-time between sending the next request 
	 */
	public final int TIME_BETWEEN_REQUESTS = 2*1000;

	/**
	 * keeps track of final state of slice rejected and accepted for performance metric plots 
	 */
	public LinkedHashMap<Integer, Boolean> _acceptedVNMap = new LinkedHashMap<Integer, Boolean>(); 
	/**
	 * for storing timeout ExecutorService for each sliceID_partitionID
	 */
	public LinkedHashMap<Integer, ExecutorService> _executorsMap = new LinkedHashMap<Integer, ExecutorService>();

	/**
	 * received by one pnode this contains a list of winners pnodes to alert for the link embedding phase
	 */
	private List<assignment> _wList = null;
	/**
	 * CAD Message generator Implementation
	 */
	private CADMessageImpl _cadMsgImpl = new CADMessageImpl();

	/**
	 * mininet script generator: actual binding between physical and virutal resources
	 */
	private VNAllocationImpl _vnAllocation = null;

	/**
	 * if true, the SP allocates the entire VN
	 */
	private boolean _singleSlice = true;

	/**
	 * map of all the slices to embed
	 */
	private LinkedHashMap<Integer, SliceSpec.Slice.Builder> _slicesToEmbed = null;
	private SlicePartitioningImpl _slicePartitioning = null;
	/**
	 * if false, then waits forever for pnode responses  
	 */



	/**
	 * Gets a slice request from another file
	 * @param appName
	 * @param ISDName
	 * @param slice
	 */
	public SliceProvider(String appName, String ISDName, Slice slice) {
		super(appName, ISDName);
		this._spName = appName;
		this.init();

	}


	/**
	 * Constructor for single slice embedding 
	 * @param appName
	 * @param ISDName
	 * @deprecated we now use a config file to read the slices to embed
	 */
	public SliceProvider(String appName, String ISDName) {
		super(appName, ISDName);


		this._spName = appName;

		this.init();

		SliceGenerator sg = new SliceGenerator();

		//this._sliceToEmbed = sg.generateSlice("1", "linear");


		//this.partitionSliceRequest();

		//this.setTimeOut();

		// now that the timeout is set
		//this.sendSliceRequest("pnode1");

	}



	/**
	 * Constructor for many slices to be embedded
	 * @param appName
	 * @param ISDName
	 * @param slicesToEmbed to differentiate the constructor
	 */
	public SliceProvider(String appName, 
			String ISDName, 
			LinkedHashMap<Integer,SliceSpec.Slice.Builder> slicesToEmbed, 
			Boolean singleSliceFlag) {


		super(appName, ISDName);
		
		this._spName = appName;

		this.assertInputs(slicesToEmbed.size() );

		rib.RIBlog.infoLog("================== SLICE PROVIDER STARTED ========================");

		this._spName = appName;

		this.setSingleSlice(singleSliceFlag);
		rib.RIBlog.debugLog("SliceProvider::SliceProvider: singleSliceFlag: "+singleSliceFlag);

		rib.addAttribute("singleSliceFlag", singleSliceFlag);
		rib.RIBlog.debugLog("SliceProvider::SliceProvider: singleSliceFlag: "+singleSliceFlag);

		rib.addAttribute("slicesToEmbed", slicesToEmbed);

		rib.addAttribute("totalRequestedSlices", slicesToEmbed.size());


		this.init();//joins a (RINA DIF) private network with the pnodes and subscribes to updates  

		this._slicePartitioning = new SlicePartitioningImpl(this.rib);

		this.sendRequests();



	}




	/**
	 * send slice requests to pnodes
	 */
	private void sendRequests() {
		LinkedHashMap<Integer,SliceSpec.Slice.Builder> slicesToEmbed = 
				(LinkedHashMap<Integer, Builder>) rib.getAttribute("slicesToEmbed");

		//send all request one by one to a set of trusted pnodes
		Set<Integer> slicesIndeces =slicesToEmbed.keySet();
		Iterator<Integer> SliceIter = slicesIndeces.iterator();
		int slices = slicesToEmbed.size();


		while(SliceIter.hasNext()) {

			rib.RIBlog.debugLog("SliceProvider::sendRequests: still "+slices+" embeddings to attempt");
			slices--;//just to log
			int sliceKey = SliceIter.next();
			SliceSpec.Slice.Builder slice = slicesToEmbed.get(sliceKey);

			//test if this slice is right
			Debug.printSliceDetails(slice, rib);

			//add slice to the RIB for partitioning
			Integer sliceID = slice.getSliceID();
			String sliceAttribute = sliceID.toString();
			rib.addAttribute(sliceAttribute,slice);
			rib.RIBlog.debugLog("SliceProvider::sendRequests: "+sliceAttribute+" added to the RIB");


			//partition the slice 
			rib.RIBlog.debugLog("SliceProvider::sendRequests: partitioning slice: "+slice.getSliceID()+"...");
			String result = this._slicePartitioning.partitionSliceRequest(slice.getSliceID());

			//check if another partition needs to be sent
			int successfullyEmbedded = (Integer)rib.getAttribute("successfullyEmbedded"); 
			int totalRequestedSlices = (Integer)rib.getAttribute("totalRequestedSlices");				
			int failedEmbedding = (Integer)rib.getAttribute("failedEmbedding");	

			rib.RIBlog.debugLog("SliceProvider::sendRequests: successfullyEmbedded: "+successfullyEmbedded);
			rib.RIBlog.debugLog("SliceProvider::sendRequests: totalRequestedSlices : "+totalRequestedSlices );
			rib.RIBlog.debugLog("SliceProvider::sendRequests: failedEmbedding : "+failedEmbedding );

			//if there are no more pending slices will launch the link allocation
			if(failedEmbedding+successfullyEmbedded==totalRequestedSlices)	{ 
				this.linkAllocation(slice.getSliceID());
			}else { 

				this.sendNextPartition(slice.getSliceID());


			}


			// wait a bit to send the next request
			try { 
				Thread.sleep(TIME_BETWEEN_REQUESTS); //set to 3 seconds
			} catch (InterruptedException e) {
				//System.err.println(e);
				e.printStackTrace();
			} 
		}
	}



	/**
	 * release next slice partition
	 * @param sliceID
	 */
	private void sendNextPartition(Integer sliceID) {

		// read from the rib the partition obj
		SlicePartition partitionsObj =(SlicePartition) 
				rib.getAttribute("partition"+sliceID.toString());

		//TODO: check for errors on obj retrival
		rib.RIBlog.debugLog("slicePartition: "+partitionsObj);

		// get the partition from the rib
		SliceSpec.Slice.Builder slicePartition = partitionsObj.getToEmbed();

		// set the timeout and send the message
		this.rib.RIBlog.debugLog("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		this.rib.RIBlog.debugLog("SliceProvider::sendNextPartition: Sending slice partition to trusted nodes...");
		this.rib.RIBlog.debugLog("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		LinkedList<String> trustedPnodes = (LinkedList<String>)rib.getAttribute("trustedPnodes");

		//send request to the trusted pnode
		this.sendSliceRequest(slicePartition);

		rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: this._sliceToEmbed.getVirtualLINKCount(): "+slicePartition.getVirtuallinkCount());
		rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: this._sliceToEmbed.getVirtualNODECount(): "+slicePartition.getVirtualnodeCount());

	}


	/**
	 * initialize slice provider with as many ipc as needed
	 */
	private void init() {

		rib.RIBlog.debugLog("SliceProvider: init() started");

		String file = "sp.properties";

		this._CADconfig = new CADConfig(file);

		// load your policies here
		this._kforShortestPath = this._CADconfig.getProperty("cadsys.sp.vlink.kshortest.k");
		rib.RIBlog.debugLog("SliceProvider::init: kforShortestPath : "+this._kforShortestPath);



		//		this._vnodeAuctionPolicy= this._CADconfig.getProperty("cadsys.sp.vnode.auction");
		rib.addAttribute("vnodeAuctionPolicy", this._CADconfig.getProperty("cadsys.sp.vnode.auction"));
		this._vnodeAuctionPolicy = this._CADconfig.getProperty("cadsys.sp.vnode.auction");
		rib.RIBlog.debugLog("SliceProvider::init: vnodeAuctionPolicy: "+this._vnodeAuctionPolicy);

		this.TIMEOUT = Integer.parseInt(this._CADconfig.getProperty("cadsys.sp.timeout"));
		rib.addAttribute("TIMEOUT", this._CADconfig.getProperty("cadsys.sp.timeout"));
		rib.RIBlog.debugLog("SliceProvider::init: timeout: "+rib.getAttribute("TIMEOUT"));


		if(this.getSliceProviderPolicies() != null) {
			this.setPolicyMap(this.getSliceProviderPolicies());
		}else {
			LinkedHashMap<String, String> defaultPoliciesMap= new LinkedHashMap<String, String>();
			defaultPoliciesMap.put("default", "default");
			this.setPolicyMap(defaultPoliciesMap);
			rib.RIBlog.debugLog("SliceProvider::init: no loaded policies from sp config file: default ");
		}

		//load your truested pnodes
		LinkedList<String> trustedPnodes =  this.getTrustedPnodes();		
		rib.addAttribute("trustedPnodes",trustedPnodes);
		rib.RIBlog.debugLog("SliceProvider::init: trustedPnodes: "+trustedPnodes);


		//initialize VN discovery and communication support component
		RINAConfig rinaconfig = new RINAConfig(file);

		IPCProcessImpl ipc1 = new IPCProcessImpl(rinaconfig);

		this.addUnderlyingIPC(ipc1);

		//initialize vn allocation component
		this._vnAllocation = new VNAllocationImpl(this.rib);

		//initialize book-keeping
		rib.addAttribute("successfullyEmbeddedSlices",new LinkedHashMap<Integer,SliceSpec.Slice.Builder>() );
		rib.addAttribute("failedEmbeddedSlices",new LinkedHashMap<Integer,SliceSpec.Slice.Builder>() );

		rib.addAttribute("successfullyEmbedded", 0);
		rib.addAttribute("failedEmbedding", 0);


		this.start(); //start the slice provider app message listener 

	}

	/**
	 * get trusted pnodes from config file
	 * @return list of trusted pnode: slice is sent only to trusted pnodes
	 */
	public LinkedList<String> getTrustedPnodes() {

		LinkedList<String> trustedPnodes = new LinkedList<String>();

		int i =1;
		if(this._CADconfig.getProperty("cadsys.sp.trusted."+i) == null) {
			trustedPnodes.add("pnode1"); //default case 
		}else {

			boolean stop = true;
			String trustedPnode = null;
			while(stop) {
				try {
					trustedPnode = this._CADconfig.getProperty("cadsys.sp.trusted."+i);
				}catch(Exception e) {
					//rib.RIBlog.debugLog("No more catched as expected");
					trustedPnode =null;
				}
				if(trustedPnode == null )
				{
					stop = false;
				}else {
					trustedPnodes.add(trustedPnode);
					rib.RIBlog.infoLog("SliceProvider::getTrustedPnodes: trustedPnode "+trustedPnode+ " added");
					i++;
				}
			}
		}
		rib.RIBlog.infoLog("SliceProvider::getTrustedPnodes: trustedPnodes"+trustedPnodes);

		return trustedPnodes;
	}


	/**
	 * triggers the link allocation
	 */
	private void linkAllocation(int sliceID) {

		rib.RIBlog.infoLog("----------------------------------------------------------------");
		rib.RIBlog.infoLog("------------------TRIGGERING LINK ALLOCATION -------------------");
		rib.RIBlog.infoLog("----------------------------------------------------------------");

		SliceSpec.Slice.Builder sliceToEmbed = this.getSlice(sliceID);
		rib.RIBlog.infoLog("SliceProvider::linkAllocation: sliceToEmbed: "+sliceToEmbed);
		rib.RIBlog.infoLog("SliceProvider::linkAllocation: # vnodes: "+sliceToEmbed.getVirtualnodeCount());
		rib.RIBlog.infoLog("SliceProvider::linkAllocation: # links: "+sliceToEmbed.getVirtuallinkCount());

		if(this._singleSlice) {
			_vnAllocation.createVN(sliceToEmbed);
		}else {

			_vnAllocation.createMultiVNs();
			//this.sendLinkAllocationRequest(sliceToEmbed);
		}		
	}





	/**
	 * adds policies here and get them from the config file 
	 * @return map of all sp policies
	 */
	private LinkedHashMap<String, String> getSliceProviderPolicies() {

		LinkedHashMap<String,String> pMap = new LinkedHashMap<String,String>(); 

		rib.RIBlog.debugLog("SliceProvider::getSliceProviderPolicies: loading provider policies ");


		pMap.put("cadsys.sp.vnode.auction", this._vnodeAuctionPolicy);
		pMap.put("cadsys.sp.vlink.kshortest.k", this._kforShortestPath);

		rib.RIBlog.debugLog("SliceProvider::ProviderPolicies: "+pMap.entrySet() );

		return pMap;
	}


	void printStats() {
		//rib log
		rib.RIBlog.infoLog("stats printed here.");
	}



	/**
	 * @return the policyMap
	 */
	public LinkedHashMap<String, String> getPolicyMap() {
		return _policyMap;
	}

	/**
	 * @param policyMap the policyMap to set
	 */
	public void setPolicyMap(LinkedHashMap<String, String> policyMap) {
		this._policyMap = policyMap;
	}






	/**
	 * Handles Service Provider CDAP messages
	 */
	public void handleAppCDAPmessage(byte[] msg) {


		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

			rib.RIBlog.infoLog("======= SliceProvider::handleAppCDAPmessage "+this.getAppName()+" ObjClass: " + cdapMessage.getObjClass());
			rib.RIBlog.infoLog("======= SliceProvider::handleAppCDAPmessage "+this.getAppName()+" ObjName:  "+ cdapMessage.getObjName());
			rib.RIBlog.infoLog("======= SliceProvider::handleAppCDAPmessage "+this.getAppName()+" SrcAEName: " + cdapMessage.getSrcAEName());					

			rib.RIBlog.infoLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			rib.RIBlog.infoLog("======= SliceProvider::handleAppCDAPmessage: cdapMessage.getObjClass(): "+cdapMessage.getObjClass());

			if(cdapMessage.getObjClass().equals("response"))
			{
				handlePnodeResponse(cdapMessage);
			}
			else if(cdapMessage.getObjName().equals("timeout")) //never used till now but support exists
			{
				handleExternalRequestTimeOut(cdapMessage);	
			}
			else {
				rib.RIBlog.errorLog("======= SliceProvider: "+this.getAppName()+" ======= Object Class: "+cdapMessage.getObjClass()+" not handled yet");
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();

		}
	}


	/**
	 * in case the Slice Manager calls a timeout for a specific request
	 * we inserted support for this operation but we never used it so far 
	 * @param cdapMessage
	 */
	private void handleExternalRequestTimeOut(CDAPMessage cdapMessage) {

		CAD.CADMessage cadMessage  = null;
		try {
			cadMessage = CAD.CADMessage.parseFrom(cdapMessage.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("Pnode::handlePnodeResponse: Error Parsing CAD message from Slice Provider");
			e1.printStackTrace();
		}

		int sliceID = cadMessage.getSliceID();
		handleRequestTimeOut(sliceID);
	}


	/**
	 * handle the slice timeout event
	 * @param cdapMessage
	 */
	private void handleRequestTimeOut(int sliceID) {


		//log the slice rejected 
		rib.RIBlog.infoLog("-----------------------------------------------------------------");
		rib.RIBlog.infoLog("SliceProvider::handleEmbeddingTimeOut: sliceID: "+sliceID+" has timeout");
		rib.RIBlog.infoLog("-----------------------------------------------------------------");
		this._acceptedVNMap.put(sliceID, false);
		rib.RIBlog.infoLog("SliceProvider::handleEmbeddingTimeOut: sliceID: "+sliceID+" cannot be allocated");

		//TODO: make sure we ignore late messages of the rejected sliceID.
	}

	/**
	 * if positive response release next partition if any
	 * if negative response try again later or abort this slice and log
	 * PositiveOrNegative is encoded into the ObjName: "positive" if there was agreement "negative" if there was not
	 * @param cdapMessage
	 */
	private void handlePnodeResponse(CDAPMessage cdapMessage) {

		rib.RIBlog.debugLog("--------------------------------------------------------------------------");
		rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: received message with response payload");
		rib.RIBlog.debugLog("--------------------------------------------------------------------------");


		CAD.CADMessage cadMessage  = null;
		try {
			cadMessage = CAD.CADMessage.parseFrom(cdapMessage.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			rib.RIBlog.errorLog("SliceProvider::handlePnodeResponse: Error Parsing CAD message from Slice Provider");
			e1.printStackTrace();
		}

		//store winners in case in case this is the latest message for link embedding phase 
		this._wList = cadMessage.getAList();
		rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: received this.wList: "+this._wList );


		//abort the timeout for that specific VN request
		int sliceID = cadMessage.getSliceID();

		rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: executorsMap.containsKey(sliceID): "+_executorsMap.containsKey(sliceID));
		rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: executorsMap.entrySet(): "+_executorsMap.entrySet());


		// if there was a timeout, reset it
		if(this.TIMEOUT != -1) {

			if(_executorsMap.containsKey(sliceID)) {
				ExecutorService timeOutExecutor = _executorsMap.get(sliceID);
				_executorsMap.remove(sliceID);
				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: executorToKill: "+timeOutExecutor);
				//to avoid timing out 
				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: BEFORE timeOutExecutor.isShutdown(): "+timeOutExecutor.isShutdown());
				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: BEFORE timeOutExecutor.isTerminated(): "+timeOutExecutor.isTerminated());
				timeOutExecutor.shutdownNow();

				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: AFTER timeOutExecutor.isShutdown(): "+timeOutExecutor.isShutdown());
				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: AFTER timeOutExecutor.isTerminated(): "+timeOutExecutor.isTerminated());

			}else {
				rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: executor for sliceID "+sliceID+" does not exist");
			}
		}

		//if positive send other residual
		String positiveOrNegative = cdapMessage.getObjName();

		if(positiveOrNegative.equals("negative")) {
			//log
			rib.RIBlog.infoLog("----------------------------------------------------------------------");
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: CANNOT embed slice: "+sliceID);
			rib.RIBlog.infoLog("----------------------------------------------------------------------");

			// store failed slice for logging eventually
			LinkedList<Integer> failedEmbedding = (LinkedList<Integer>) rib.getAttribute("failedEmbedding");
			failedEmbedding.add(sliceID);
			//overwrite previous failedEmbedding attribute
			rib.addAttribute("failedEmbedding", failedEmbedding);

			//store failed slice and eventually try later 
			LinkedHashMap<Integer,SliceSpec.Slice.Builder> failedEmbeddedSlices =
					(LinkedHashMap<Integer,SliceSpec.Slice.Builder>) rib.getAttribute("failedEmbeddedSlices");

			if(failedEmbeddedSlices.containsKey(sliceID) ) {
				rib.RIBlog.warnLog("SliceProvider::handlePnodeResponse: knew already about this slice failure");
			}else {
				//log new failure
				failedEmbeddedSlices.put(sliceID, this.getSlice(sliceID));
				rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: failed embedding registered");
				//update failedEmbedding attribute
				rib.addAttribute("failedEmbeddedSlices", failedEmbeddedSlices);

			}


		}else if(positiveOrNegative.equals("positive")) {
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: got POSITIVE answer from : "+cdapMessage.getSrcAEName());
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: sending residual request for slice: "+sliceID);
			// remove Embedded Elements from residual slice


			rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: s.getVirtualnodeList(): "+this.getSlice(sliceID).getVirtualnodeList());
			rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: this._sliceToEmbed: "+this.getSlice(sliceID));
			rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: this._sliceToEmbed.getVirtualnodeList(): "+this.getSlice(sliceID).getVirtualnodeList());
			rib.RIBlog.debugLog("SliceProvider::handlePnodeResponse: this.residualSlice.getVirtualnodeList(): "+this.getSlice(sliceID).getVirtualnodeList());

			int residualVnodes = this.getSlice(sliceID).getVirtualnodeCount();
			SlicePartition slicePartitionsObj = (SlicePartition) rib.getAttribute("partition"+sliceID);
			rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: slicePartitionsObj: "+slicePartitionsObj);

			SliceSpec.Slice.Builder residual = slicePartitionsObj.getResidual();
			rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: residual: "+residual);

			if(residual!=null) {
				residualVnodes = residual.getVirtualnodeCount();
				rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: residualVnodes: "+residualVnodes);
			}


			if(this._vnodeAuctionPolicy.toLowerCase().equals("sad") && (residualVnodes > 0 ))
			{
				int res = this.sendResidualRequest(sliceID);
				if (res == 1) {//no more partitions
					this.handleSuccessfullNodeEmbedding(sliceID);
				}
			}else { //this is for handling policies with no partitioning required e.g., MAD
				this.handleSuccessfullNodeEmbedding(sliceID);
			}
		}else
			rib.RIBlog.errorLog("SliceProvider::handlePnodeResponse: got a message with cdapMessage.getObjName(): "+cdapMessage.getObjName()+" not recognized");

	}

	/**
	 * log successful node embedding and start link embedding if necessary 
	 * @param sliceID
	 */
	private void handleSuccessfullNodeEmbedding(int sliceID) {

		rib.RIBlog.infoLog("--------------------------------------------------------------------------");
		rib.RIBlog.infoLog("----SliceProvider::handlePnodeResponse: vnode mapping phase SUCCESSFULL---");
		rib.RIBlog.infoLog("--------------------------------------------------------------------------");

		//increase the # of successfully embedded slices
		int successfullyEmbedded = (Integer)rib.getAttribute("successfullyEmbedded")+1;
		rib.addAttribute("successfullyEmbedded", successfullyEmbedded);

		//store successful slice for late logging 
		LinkedHashMap<Integer,SliceSpec.Slice.Builder> successfullyEmbeddedSlices = 
				(LinkedHashMap<Integer,SliceSpec.Slice.Builder>) rib.getAttribute("successfullyEmbeddedSlices");

		if(successfullyEmbeddedSlices.containsKey(sliceID) ) { //it can happen that multiple messages arrive 
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: We knew already about this slice success");
		}else {
			//log new success
			successfullyEmbeddedSlices.put(sliceID, this.getSlice(sliceID));
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: successfully embedding registered");
			//update successfullyEmbeddedSlices rib attribute
			rib.addAttribute("successfullyEmbeddedSlices", successfullyEmbeddedSlices);
		}

		//if there are no more pending slices launch the link allocation   
		int totalRequestedSlices = (Integer)rib.getAttribute("totalRequestedSlices");				
		int failedEmbedding = (Integer)rib.getAttribute("failedEmbedding");

		rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: totalRequestedSlices: "+totalRequestedSlices);
		rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: failedEmbedding: "+failedEmbedding);
		rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: successfullyEmbedded: "+successfullyEmbedded);

		if(failedEmbedding+successfullyEmbedded>=totalRequestedSlices)	{
			rib.RIBlog.infoLog("SliceProvider::handlePnodeResponse: starting LINK ALLOCATION");
			this.linkAllocation(sliceID);
		}

	}


	/**
	 * send residual request to pnode
	 * @param sliceID
	 */
	private int sendResidualRequest(Integer sliceID) {

		//partition request further
		//SliceSpec.Slice.Builder slice = this.partitionSliceRequest();
		String result = this._slicePartitioning.partitionSliceRequest(sliceID);
		rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: partitioning result: "+result);
		if(result.equalsIgnoreCase("nomorepartitions")) {

			return 1;
		}

		//get partition object from rib
		SlicePartition slicePartitionsObj = (SlicePartition) rib.getAttribute("partition"+sliceID);
		rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: slicePartitionsObj: "+slicePartitionsObj);
		// do some checking, like check if partition it's empty 

		// get the slice from the partition object
		SliceSpec.Slice.Builder slicePartition = slicePartitionsObj.getToEmbed(); 
		rib.RIBlog.debugLog("SliceProvider::sendResidualRequest: slicePartition: "+slicePartition);


		this.sendSliceRequest(slicePartition);
		rib.RIBlog.infoLog("SliceProvider::sendResidualRequest: next slice partition sent");

		if(this.TIMEOUT != -1) {
			this.setTimeout(sliceID);
			rib.RIBlog.infoLog("SliceProvider::sendResidualRequest: timeout set");
		}
		return 0;
	}

	/**
	 * Sends request to a pnode
	 * 1. forks a new server to listen to InP flows
	 * 2. creates (asks to the underlying layer) a new flow to each InP 
	 * 3. InPsFlow.send(slice) over that flow
	 * @param dstName
	 */
	private void sendSliceRequest(SliceSpec.Slice.Builder slice ){

		LinkedList<String> trustedPnodes = (LinkedList<String>) rib.getAttribute("trustedPnodes");
		if (trustedPnodes ==null) {
			trustedPnodes = new LinkedList<String>();
			trustedPnodes.add("pnode1");//default
		}

		Iterator<String> trustedPnodesIterator =  trustedPnodes.iterator();
		while(trustedPnodesIterator.hasNext()) {
			String dstName = trustedPnodesIterator.next(); 
			rib.RIBlog.debugLog("SliceProvider::sendSliceRequest: allocating flow to destination pnode");

			int handle = this.irm.allocateFlow(this.getAppName(), dstName);
			rib.RIBlog.debugLog("SliceProvider::sendSliceRequest: handle is " + handle);


			//payload
			rib.RIBlog.debugLog("SliceProvider::sendSliceRequest: creating payload");
			slice.buildPartial();

			//generate CDAP message with payload
			CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();

			ByteString sliceByteString = ByteString.copyFrom(slice. build().toByteArray());
			ObjValue.setByteval(sliceByteString);
			CDAP.objVal_t objvalueSlice = ObjValue.buildPartial();


			CDAP.CDAPMessage M_CREATE = message.CDAPMessage.generateM_CREATE(
					"slice", //objclass
					"slice1",  // ObjName, //
					objvalueSlice, // objvalue
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

			rib.RIBlog.debugLog("SliceProvider::sendSliceRequest: sending M_CREATE (allocation request)...");


			//sendMsgWithTimeOut(M_CREATE.toByteArray(),this._sliceToEmbed.getSliceID() );
			int byteOverhead = M_CREATE.toByteArray().length;
			rib.RIBlog.debugLog("SliceProvider::sendSliceRequest: message overhead from: "+this._spName+" to: pnode1 is "+byteOverhead);
			
			// send msg here
			try {
				irm.sendCDAP(irm.getHandle("pnode1"), M_CREATE.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
			//start timeout
			//
			if(this.TIMEOUT != -1) {
				this.setTimeout(slice.getSliceID());
				rib.RIBlog.infoLog("SliceProvider::sendResidualRequest: timeout set");
			}

		}


	}


	/**
	 * send link embedding request to all pnode winners
	 * here I am assuming that all pnode know identity of all winners
	 * we could implement another version where SP tells who should connect to who 
	 */
	private void sendLinkAllocationRequest(SliceSpec.Slice.Builder slice) {


		rib.RIBlog.debugLog("SliceProvider: sendLinkAllocationRequest: wList: "+ this._wList);
		if(_wList==null) {
			rib.RIBlog.warnLog("SliceProvider: sendLinkAllocationRequest: wList empty");
			return;
		}

		//identify winner

		//<hosting vnodeId, pnode name>			
		LinkedHashMap<Integer,String> vnodeWinnerPnodeMap = new LinkedHashMap<Integer,String>();
		//<pnode name,hosting vnodeId>
		LinkedHashMap<String, Integer> winnerPnodeVnodeMap  = new LinkedHashMap<String,Integer>();		

		for(assignment wi : _wList) {
			int wonVnodeID = wi.getVNodeId();
			String winnerPNode = wi.getHostingPnodeName();
			vnodeWinnerPnodeMap.put(wonVnodeID, winnerPNode);
			winnerPnodeVnodeMap.put(winnerPNode, wonVnodeID);
		}


		if(winnerPnodeVnodeMap.isEmpty()) {
			rib.RIBlog.infoLog("SliceProvider: sendLinkAllocationRequest: no winners, no link allocation");
			return;
		}else { //send link request to winners

			//send it to all winners
			Iterator<String> winnersPnodesIter = winnerPnodeVnodeMap.keySet().iterator();

			//generate payload
			CAD.CADMessage cadMessage = _cadMsgImpl.generateLinkEmbeddingRequest(slice.getSliceID());

			while(winnersPnodesIter.hasNext()) {
				String dstName = winnersPnodesIter.next();


				//get destination name
				int handle = this.irm.allocateFlow(this.getAppName(), dstName);
				rib.RIBlog.debugLog("SliceProvider::sendLinkAllocationRequest: handle is " + handle);

				//generate CDAP message payload
				CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
				ByteString cadMessageByteString = ByteString.copyFrom(cadMessage.toByteArray());
				ObjValue.setByteval(cadMessageByteString);
				CDAP.objVal_t CADPayload  = ObjValue.buildPartial();

				//generate CDAP message 
				CDAP.CDAPMessage M_WRITE = message.CDAPMessage.generateM_WRITE(
						"linkEmbedding", //objclass
						"linkEmbedding",  // ObjName, 
						CADPayload, // objvalue
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



				//send link embedding request 
				rib.RIBlog.debugLog("SliceProvider::sendLinkAllocationRequest: link embedding request sent to" + dstName);
				int byteOverhead = M_WRITE.toByteArray().length;
				rib.RIBlog.debugLog("SliceProvider::sendLinkAllocationRequest: message overhead from: "+this._spName+" to: "+dstName+" is "+byteOverhead);
				
				
				try {
					irm.sendCDAP(irm.getHandle(dstName), M_WRITE.toByteArray());
				} catch (Exception e) {
					e.printStackTrace();
				}


			}

			//for each vitual link, get the winner, and the node it should connect to
			//send only one message saying: connect to the other end

			// send lnk allocation request to each of the winner

		}
	}

	/**
	 * set up an executor for a slice Request that on timeout will send a message
	 * to the SP itself to notify that the request could not be embedded 
	 * if instead we get an agreement message we will terminate the timeout with executor.shutdownNow();		
	 */
	public void setTimeout(int sliceID) {



		rib.RIBlog.debugLog("SliceProvider::setTimeOut: setting Timeout for slice: "+sliceID);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		_executorsMap.put(sliceID, executor);

		//Future<String> future = executor.submit(new CADMessageWithTimeout(this.irm, msg));
		Future<String> future = executor.submit(new Timeout());
		String result = null;
		try {
			try {

				result = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
				rib.RIBlog.infoLog("SliceProvider::setTimeOut: timeout started for slice: "+sliceID);
			} catch (InterruptedException e) {
				rib.RIBlog.errorLog("SliceProvider::setTimeOut for slice: InterruptedException "+e);

			} catch (ExecutionException e) {
				rib.RIBlog.errorLog("SliceProvider::setTimeOut ExecutionException "+e);
				rib.RIBlog.infoLog("SliceProvider::setTimeOut result: "+result);
			}
			rib.RIBlog.debugLog("SliceProvider::setTimeOut: slice partition successfully");
		} catch (TimeoutException e) {
			//log that there was a timeout
			rib.RIBlog.infoLog("SliceProvider::setTimeOut: "+result+" Slice (partition) could not be embedded within the timeout! "+e);

			//sending a message to itself to handle the timeout
			this.handleRequestTimeOut(sliceID);	 
		}

	}




	/**
	 * @return the singleSlice
	 */
	public synchronized boolean isSingleSlice() {
		return _singleSlice;
	}

	/**
	 * @return the _slicesSuccessfullyAllocated
	 */
	public synchronized int get_slicesSuccessfullyAllocated() {
		return _slicesSuccessfullyAllocated;
	}


	/**
	 * @param _slicesSuccessfullyAllocated the _slicesSuccessfullyAllocated to set
	 */
	public synchronized void set_slicesSuccessfullyAllocated(
			int _slicesSuccessfullyAllocated) {
		this._slicesSuccessfullyAllocated = _slicesSuccessfullyAllocated;
	}


	/**
	 * @param singleSlice the singleSlice to set
	 */
	public synchronized void setSingleSlice(boolean singleSlice) {
		this._singleSlice = singleSlice;
	}


	/**
	 * @return the _executorsMap
	 */
	public synchronized LinkedHashMap<Integer, ExecutorService> get_executorsMap() {
		return _executorsMap;
	}


	/**
	 * @param _executorsMap the _executorsMap to set
	 */
	public synchronized void set_executorsMap(
			LinkedHashMap<Integer, ExecutorService> _executorsMap) {
		this._executorsMap = _executorsMap;
	}


	/**
	 * @return the tIMEOUT
	 */
	public synchronized int getTIMEOUT() {
		return TIMEOUT;
	}


	/**
	 * @param tIMEOUT the tIMEOUT to set
	 */
	public synchronized void setTIMEOUT(int tIMEOUT) {
		TIMEOUT = tIMEOUT;
	}

	/**
	 * this method is in support of multiple simultanous embedding
	 * it access the rib to get the slice:
	 * @param sliceID
	 * @return sliceToEmbed
	 */
	private SliceSpec.Slice.Builder getSlice(int sliceID) {

		LinkedHashMap<Integer,SliceSpec.Slice.Builder> slicesToEmbed = 
				(LinkedHashMap<Integer,SliceSpec.Slice.Builder>)
				rib.getAttribute("slicesToEmbed");

		SliceSpec.Slice.Builder slice = slicesToEmbed.get(sliceID);

		return slice;
	}

	/**
	 * assert if there are requests to embed
	 * @param size
	 */
	private void assertInputs(int size) {

		if(size==0) {
			rib.RIBlog.warnLog("SliceProvider::SliceProvider: "+size+ " slice to be embedded");
			rib.RIBlog.warnLog("================== nothing to be done ========================");
			System.exit(0);
			//return;
		}else {
			rib.RIBlog.debugLog("SliceProvider::SliceProvider: "+size+ " slice to be embedded");
		}


	}

}//end of class



