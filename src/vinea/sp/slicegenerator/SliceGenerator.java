/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.sp.slicegenerator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rina.rib.impl.RIBImpl;
import vinea.impl.googleprotobuf.CAD;
import vinea.slicespec.impl.googleprotobuf.SliceSpec;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.Builder;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;
import vinea.sp.util.SlicePartition;



/**
 * @author Flavio Esposito
 * @version 1.0
 * create a slice request object 
 * 
 */
public class SliceGenerator {

	/**
	 * RIB
	 */
	private RIBImpl rib = null;

	
	/**
	 * number of virtual nodes in the slice generated
	 */
	private int vnodes = -1;
	/**
	 * no. of v links in the slice generated
	 */
	private int vlinks = -1;
	/**
	 * slice topology type 
	 * @author Flavio Esposito
	 *
	 */
	public enum SliceType  {
		RANDOM, 
		FULL, 
		LINEAR, 
		STAR, 
		TREE
	}

	
	/**
	 * topology of the slice to be generated
	 */
	private SliceType slicetype = null; 
	/**
	 * random no. generator for sliceID
	 */
	private Random randomGenerator = new Random();

	/**
	 * slice adjacency matrix to check if the random generation is connected 
	 */
	private LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>> adjMatrix = null;

	
	
	/**
	 * dummy constructor
	 */
	public SliceGenerator() {
		 
		this.rib= new RIBImpl(); 

		
	}


	/**
	 * generate a slice parsing an entry from the configuration file config/events.txt
	 * @param eventFileEntry
	 */
	public SliceSpec.Slice.Builder generateSlice(String key, String eventFileEntry) {
		
		if (eventFileEntry ==null) {
			rib.RIBlog.errorLog("SliceGenerator: no slice to generate: events.txt entry is null");
			return null;
		}else {

			rib.RIBlog.infoLog("SliceGenerator: generating slice with "+eventFileEntry+ " event file entry");

			String[] eventLine = eventFileEntry.split("\\t");
			String sliceType =eventLine[0];
			this.vnodes = Integer.parseInt(eventLine[1]);
			Integer entryTime= Integer.parseInt(eventLine[2]);
			Integer exitTime= Integer.parseInt(eventLine[3]);

			SliceSpec.Slice.Builder slice = Slice.newBuilder();
			
			slice.setEntryTime(entryTime);
			slice.setExitTime(exitTime);
			slice.setName("sliceFromEventFile"+key);
			slice.setVersion(0);//there is only this version for now
			
			// TODO: we could add here support for policy specification from the events.txt file
			//slice.addAllPolicy(sliceToEmbed.getPolicyList());
			//residual_slice.setSliceID(sliceToEmbed.getSliceID());
			


			slice.setSliceID(randomGenerator.nextInt(9999999));
			slice.setTopology(sliceType);

			if(sliceType.toLowerCase().equals("linear")) {
				this.slicetype= SliceType.LINEAR; 
			}else if(sliceType.toLowerCase().equals("full")) {
				this.slicetype= SliceType.FULL;
			}else if(sliceType.toLowerCase().equals("random")) {
				this.slicetype= SliceType.RANDOM;
			}else if(sliceType.toLowerCase().equals("star")) {
				this.slicetype = SliceType.STAR;
			}else if(sliceType.toLowerCase().equals("tree")) {
				this.slicetype = SliceType.TREE;
			}
			else 	//default
				this.slicetype = SliceType.RANDOM;


			//this.adjMatrix = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>(); 
			
			return completeSliceGeneration(slice);
		}
	}

	/**
	 * set virtual nodes and links given the slice type
	 */
	public SliceSpec.Slice.Builder completeSliceGeneration(SliceSpec.Slice.Builder slice){
		
		switch (this.slicetype) {

		case FULL:
			slice = generateFull(vnodes,slice);
			slice.setTopology("FULL");
			rib.RIBlog.infoLog("SliceGenerator is generarting a slice with FULL topology of "+vnodes + " vnodes");
			break;


		case RANDOM:
			slice = generateRandom(vnodes,slice);
			slice.setTopology("RANDOM");
			rib.RIBlog.infoLog("SliceGenerator is generarting a slice with RANDOM topology of "+vnodes + " vnodes");
			break;

		case LINEAR:
			rib.RIBlog.infoLog("SliceGenerator is generarting a slice with LINEAR topology of "+vnodes + " vnodes");
			slice.setTopology("LINEAR");
			slice = generateLinear(vnodes,slice);
			slice.setName("sliceName_linearSlice");
			break;

		case STAR:
			rib.RIBlog.infoLog("SliceGenerator is generarting a slice with STAR (hub and spoke) topology of "+vnodes + " vnodes");
			slice = generateStar(vnodes,slice);
			slice.setTopology("STAR");
			break;



		default:
			rib.RIBlog.errorLog("slice type not handled yet. Not embedding any slice");
			break;
		}		
		return slice;
	}

	/**
	 * copy the oldSlice but only leaves the vnodesRequested and vLinksRequested  
	 * @param oldSlice
	 * @param vnodesRequested
	 * @param vLinksRequested
	 */
	public SliceSpec.Slice.Builder generateSlice(Slice.Builder oldSlice, List<vNode> vnodesRequested,List<vLink> vLinksRequested ) {
		
		 
		Slice.Builder slice = Slice.newBuilder();
		slice.setEntryTime(oldSlice.getEntryTime());
		slice.setExitTime(oldSlice.getExitTime());
		slice.setName(oldSlice.getName());

		slice.addAllPolicy(oldSlice.getPolicyList());
		slice.setSliceID(oldSlice.getSliceID());

		slice.setTopology(oldSlice.getTopology());
		slice.setVersion(oldSlice.getVersion());


		//add new vlinks
		//rib.RIBlog.debugLog
		System.out.println("SliceGenerator::generateSlice: vLinksRequested: "+vLinksRequested);

		for(int l = 0; l<vLinksRequested.size();l++) {
			slice.addVirtuallink(vLinksRequested.get(l));
		}


		//rib.RIBlog.debugLog
		System.out.println("SliceGenerator::generateSlice:: NEW this.slice.getVirtuallinkList(): "+slice.getVirtuallinkList());


		//add new vnodes
		//rib.RIBlog.debugLog
		System.out.println("SliceGenerator: vnodesRequested: "+vnodesRequested);

		for(int n=0; n<vnodesRequested.size();n++) {
			slice.addVirtualnode(vnodesRequested.get(n));
		}

		//rib.RIBlog.debugLog
		System.out.println("SliceGenerator: NEW this.slice.getVirtualnodeList() AFTER  elimination: "+slice.getVirtualnodeList());

		//rib.RIBlog.debugLog
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		//rib.RIBlog.debugLog
		System.out.println("&&&&&&&&&&&&&&& End of SliceGenerator &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		//rib.RIBlog.debugLog
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");


		return slice;
		//TODO: fix also the adj matrix, but for now nobody uses it.

	}


	/**
	 * generate the slice including the slice provider policies 
	 * @param vnodes
	 * @param slicetype
	 * @param spPolicies
	
	public SliceSpec.Slice.Builder generateSlice( int vnodes, SliceType slicetype, LinkedHashMap<String,String> spPolicies) {

		Slice.Builder slice = Slice.newBuilder();

		slice.setSliceID(randomGenerator.nextInt(9999999));
//fix this
		this.vnodes = vnodes;
		this.slicetype= slicetype;		
		this.adjMatrix = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>(); 

		if(spPolicies !=null) {
			Set<String> keySet = spPolicies.keySet();
			Iterator<String> keyIter = keySet.iterator();
			while(keyIter.hasNext()) {
				String spPolicyKey = keyIter.next();
				this.slice.addPolicy(spPolicyKey);
				String spPolicyValue = spPolicies.get(spPolicyKey);
				this.slice.addPolicy(spPolicyValue);
			}
		}

		completeSliceGeneration(slice);
		 

	}
	*/

	/**
	 * Generate Linear topology
	 * @param vnodes
	 */
	private SliceSpec.Slice.Builder generateLinear(int vnodes,SliceSpec.Slice.Builder slice) {

		for(int i=0; i<vnodes; i++){
			int j=i+1;

			
			
			vNode.Builder vnode = generateVnode(i);
			slice.addVirtualnode(i, vnode);
			this.addAdjMatrixElement(i,i,1);
			if(i<vnodes) { //if i is the last vnode we don't need more edges
				vLink.Builder vlink = generateVlink(i*10+j,i,j); //id,source, destination
				slice.addVirtuallink(i, vlink);
				
	//			addAdjMatrixElement(i,j,1);
				
			}
		}
		return slice;

	}

	/**
	 * 
	 * @param vnodes2
	 */
	private SliceSpec.Slice.Builder  generateStar(int vnodes,SliceSpec.Slice.Builder slice) {

		// node 0 is the hub
		for(int i=0; i<vnodes; i++){
			int j=i+1;

			vNode.Builder vnode = generateVnode(i);
			slice.addVirtualnode(i, vnode);
			addAdjMatrixElement(i,i,1);
			if(i<vnodes) { //if i is the last vnode we don't need more edges
				vLink.Builder vlink = generateVlink(i*10+j,0,j); //id,source, dest
				slice.addVirtuallink(i, vlink);
				//addAdjMatrixElement(0,j,1);

			}
		}

		return slice;
	}

	/**
	 * generate random slice
	 * @param vnodes
	 */
	private SliceSpec.Slice.Builder  generateRandom(int vnodes, SliceSpec.Slice.Builder slice ) {

		boolean isConnected = false;
		int counter = 0; 
		while(!isConnected) {
			counter++;
			//reset the slice if the previous was not connected
			if(counter>1) {
				slice = Slice.newBuilder();
				//this.adjMatrix = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>();

			}
			//otherwise we stay here forever
			if(counter>20) {
				SliceSpec.Slice.Builder full = generateFull(vnodes,slice); 
				return full;
			}
			for (int i=0; i<vnodes;i++) {
				for (int j=0; j<vnodes;j++) {
					if (i>j) continue;{
						if (i==j) {
							vNode.Builder vnode = generateVnode(i);
							slice.addVirtualnode(i, vnode);
							addAdjMatrixElement(i,i,1);
						}
						else {
							if(!randomGenerator.nextBoolean()) continue; //randomly assign an edge
							else {
								vLink.Builder vlink = generateVlink(i*10+j,i,j); //id,source, destionation
								slice.addVirtuallink(i, vlink);
								addAdjMatrixElement(i,j,1);
							}
						}
					}
				}
			} //end for
			isConnected = graphConnected(this.adjMatrix);
		}
		return slice;
	}

	/**
	 * generates a virtual node using SliceSpec GBP abstract syntax
	 * @param nodeid
	 * @param nodeCapacity
	 * @param nodeType
	 * @param nodeClass
	 * @param nodeName
	 * @return
	 */
	private vNode.Builder generateVnode(
			int nodeid, 		//required
			int nodeCapacity,   //optional
			int nodeType, 		//optional
			String nodeClass,   //optional
			String nodeName){   //optional
		vNode.Builder vnode = vNode.newBuilder();
		vnode.setVNodeId(nodeid);
		vnode.setVNodeCapacity(nodeCapacity);
		vnode.setVNodeType(nodeType); //not used for now example
		vnode.setVNodeClass(nodeClass);
		vnode.setVNodeName(nodeName);
		return vnode;
	}

	/**
	 * generateVnode
	 * @param nodeid
	 * @param vnode requested capacity
	 * @return
	 */
	private vNode.Builder generateVnode(int nodeid, int capacity){   //required
		vNode.Builder vnode = vNode.newBuilder();
		vnode.setVNodeId(nodeid);
		vnode.setVNodeCapacity(capacity);
		return vnode;
	}

	/**
	 * 
	 * @param nodeid
	 * @return
	 */
	private vNode.Builder generateVnode(int nodeid){   //required
		vNode.Builder vnode = vNode.newBuilder();
		vnode.setVNodeId(nodeid);
		vnode.setVNodeCapacity(10); //every vnode has 10 capacity requested as default
		//TODO: vnode.setVNodeLocation("any"); 
		return vnode;
	}

	/**
	 * generates a virtual link using SliceSpec GBP abstract syntax
	 * @param linkId
	 * @param SrcID
	 * @param DstID
	 * @param linkCapacity
	 * @param nodeType
	 * @return
	 */
	@SuppressWarnings("unused")
	private vLink.Builder generateVlink(
			int linkId, 		//required
			int SrcID,  	 	//required
			int DstID, 			//required
			int linkCapacity,   //optional
			int nodeType){   	//optional
		SliceSpec.Slice.vLink.Builder vlink = vLink.newBuilder();

		vlink.setVLinkId(linkId);
		vlink.setVSrcID(SrcID);
		vlink.setVDstID(DstID);
		vlink.setVLinkCapacity(linkCapacity);
		vlink.setVLinkType(nodeType);

		return vlink;
	}

	private vLink.Builder generateVlink(
			int linkId, 		//required
			int SrcID,  	 	//required
			int DstID, 			//required
			int linkCapacity){   	//optional
		SliceSpec.Slice.vLink.Builder vlink = vLink.newBuilder();

		vlink.setVLinkId(linkId);
		vlink.setVSrcID(SrcID);
		vlink.setVDstID(DstID);
		vlink.setVLinkCapacity(linkCapacity);

		return vlink;
	}


	/**
	 * 
	 * @param linkId
	 * @param SrcID
	 * @param DstID
	 * @return
	 */
	private vLink.Builder generateVlink(
			int linkId, 		//required
			int SrcID,  	 	//required
			int DstID){ 			//required

		SliceSpec.Slice.vLink.Builder vlink = vLink.newBuilder();

		vlink.setVLinkId(linkId);
		vlink.setVSrcID(SrcID);
		vlink.setVDstID(DstID);

		return vlink;
	}




	/**
	 * generate full virtual topology
	 * @param vnodes
	 * @param capacity
	 */
	private SliceSpec.Slice.Builder generateFull(int vnodes, int capacity, SliceSpec.Slice.Builder slice) {
		for (int i=0; i<=vnodes;i++) {
			for (int j=0; j<=vnodes;j++) {
				if (i>j) continue;{
					if (i==j) {
						vNode.Builder vnode = generateVnode(
								i, 		     	// nodeId mandatory
								capacity,         	// capacity optional
								3, 	         	// nodeType optional
								"nodeClass1",   // nodeClass optional
								"vnode1");   	// nodeName optional)
						slice.addVirtualnode(i, vnode);
						addAdjMatrixElement(i,i,vnode.getVNodeCapacity());
					}
					if(i<j) {
						vLink.Builder vlink = generateVlink(i*10+j,i,j, capacity); //id,source, destionation
						slice.addVirtuallink(i, vlink);
						addAdjMatrixElement(i,j,capacity);
					}
				}
			}
		}
		return slice;
	}

	/**
	 * generateFull topology with unitary capacity
	 * @param vnodes
	 */
	private SliceSpec.Slice.Builder  generateFull(int vnodes, SliceSpec.Slice.Builder slice) {
		for (int i=0; i<=vnodes;i++) {
			for (int j=0; j<=vnodes;j++) {
				if (i>j) continue;{
					if (i==j) {
						vNode.Builder vnode = generateVnode(i);
						slice.addVirtualnode(i, vnode);
						//set capacity for vNode(i,j)
						addAdjMatrixElement(i,i,1);
					}
					else {
						vLink.Builder vlink = generateVlink(i*10+j,i,j); //id,source, destionation
						slice.addVirtuallink(i, vlink);
						addAdjMatrixElement(i,j,1);
					}
				}
			}
		}
		return slice;
	}







	/**
	 * add a new element to adj matrix
	 * @param i
	 * @param j
	 * @param capacity
	 */
	private void addAdjMatrixElement(int i, int j, int capacity) {
		if(this.adjMatrix ==null)
			this.adjMatrix = new LinkedHashMap<Integer,LinkedHashMap<Integer,Integer>>();
		else if(!this.adjMatrix.containsKey(i)) {
			LinkedHashMap<Integer,Integer> rowI = new LinkedHashMap<Integer,Integer>();
			rowI.put(j, capacity);
			this.adjMatrix.put(i, rowI);
		}
		else {
			LinkedHashMap<Integer,Integer> rowI = this.adjMatrix.get(i);
			rowI.put(j, capacity);
			this.adjMatrix.put(i, rowI);
		}
	}


	/**
	 * check if adjMatrix is connected 
	 * @param adjMatrix
	 * @return
	 */
	private boolean graphConnected(LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> adjMatrix2) {


		//LinkedHashMap<Integer,Integer> sources= null;
		Set<Integer> allSources = this.adjMatrix.keySet();
		Iterator<Integer> KeyIter = allSources.iterator();


		//    choose a vertex x
		int x =  KeyIter.next();
		//    make a list L of vertices reachable from x,
		LinkedList<Integer> L = new LinkedList();
		//    and another list K of vertices to be explored.
		LinkedList<Integer> K = new LinkedList();
		//    initially, L = K = x.
		while(KeyIter.hasNext()) {
			L.add(KeyIter.next());
			K.add(KeyIter.next());
		}

		//   while K is nonempty
		while(!K.isEmpty()) {
			//    find and remove some vertex y in K
			int y = K.poll();

			//    for each edge (y, z)
			LinkedHashMap<Integer,Integer> allYedges = this.adjMatrix.get(y);
			Set<Integer> allYedgesSet= allYedges.keySet();
			Iterator<Integer> YKeyIter = allYedgesSet.iterator();
			while(YKeyIter.hasNext())
			{
				int z = YKeyIter.next();
				//    if (z is not in L)
				if(!L.contains(z)) {
					//    add z to both L and K
					L.add(z);
					K.add(z);
				}
			}
		}
		//		    if L has fewer than n items
		if(L.size()< K.size())
			return false;//disconnected
		else 
			return true; //connected

	}



	/**
	 * @return the vnodes
	 */
	public int getVnodes() {
		return vnodes;
	}

	/**
	 * @param vnodes the vnodes to set
	 */
	public void setVnodes(int vnodes) {
		this.vnodes = vnodes;
	}

	/**
	 * @return the vlinks
	 */
	public int getVlinks() {
		return vlinks;
	}

	/**
	 * @param vlinks the vlinks to set
	 */
	public void setVlinks(int vlinks) {
		this.vlinks = vlinks;
	}

	/**
	 * @return the slicetype
	 */
	public SliceType getSlicetype() {
		return slicetype;
	}

	/**
	 * @param slicetype the slicetype to set
	 */
	public void setSlicetype(SliceType slicetype) {
		this.slicetype = slicetype;
	}

	/**
	 * @return the adjMatrix
	 */
	public LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> getAdjMatrix() {
		return adjMatrix;
	}

	/**
	 * @param adjMatrix the adjMatrix to set
	 */
	public void setAdjMatrix(
			LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> adjMatrix) {
		this.adjMatrix = adjMatrix;
	}

	/**
	 * get the residual slice after removing vNodes and vlinks to embed:
	 * essential to keep track of the slice still to embed when partitioning
	 * @param sliceToEmbed
	 * @param vNodeCurrentList  vNodes to extract from sliceToEmbed
	 * @param vLinkCurrentList	vLinks to extract from sliceToEmbed
	 * @return residual slice removing vNodeCurrentList and vLinkCurrentList from sliceToEmbed  
	 */
	public SlicePartition generateResidualSlice(Builder sliceToEmbed,
			List<vNode> vNodeCurrentList, List<vLink> vLinkCurrentList) {

		assert sliceToEmbed != null: "ERROR! sliceToEmbed null";
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed: "+sliceToEmbed);

		
		SlicePartition partitionsObj = new SlicePartition();
		partitionsObj.setSliceID(sliceToEmbed.getSliceID());

		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed: "+sliceToEmbed);
		Slice.Builder residual_slice = Slice.newBuilder();
		Slice.Builder new_slice= Slice.newBuilder();
		
		if(sliceToEmbed.hasSliceID()) {
			residual_slice.setSliceID(sliceToEmbed.getSliceID());
			new_slice.setSliceID(sliceToEmbed.getSliceID());
			
			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getSliceID(): "+sliceToEmbed.getSliceID());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getSliceID(): "+sliceToEmbed.getSliceID());
		}
		if(sliceToEmbed.hasEntryTime()) {
			residual_slice.setEntryTime(sliceToEmbed.getEntryTime());
			new_slice.setEntryTime(sliceToEmbed.getEntryTime());
			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getEntryTime(): "+sliceToEmbed.getEntryTime());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getEntryTime(): "+sliceToEmbed.getEntryTime());
		}
		if(sliceToEmbed.hasExitTime()) {			
			residual_slice.setExitTime(sliceToEmbed.getExitTime());
			new_slice.setExitTime(sliceToEmbed.getExitTime());

			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getExitTime(): "+sliceToEmbed.getExitTime());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getExitTime(): "+sliceToEmbed.getExitTime());
		}
		if(sliceToEmbed.hasName()) {
			residual_slice.setName(sliceToEmbed.getName());
			new_slice.setName(sliceToEmbed.getName());

			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getName(): "+sliceToEmbed.getName());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getName(): "+sliceToEmbed.getName());
		}
		if(!sliceToEmbed.getPolicyList().isEmpty()) {
			residual_slice.addAllPolicy(sliceToEmbed.getPolicyList());
			new_slice.addAllPolicy(sliceToEmbed.getPolicyList());
			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getPolicyList(): "+sliceToEmbed.getPolicyList());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getPolicyList(): "+sliceToEmbed.getPolicyList());

		}
		if(sliceToEmbed.hasTopology()) {
			residual_slice.setTopology(sliceToEmbed.getTopology());
			new_slice.setTopology(sliceToEmbed.getTopology());

			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getTopology(): "+sliceToEmbed.getTopology());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getTopology(): "+sliceToEmbed.getTopology());

		}
		if(sliceToEmbed.hasVersion()) {
			residual_slice.setVersion(sliceToEmbed.getVersion());
			new_slice.setVersion(sliceToEmbed.getVersion());
			rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getVersion(): "+sliceToEmbed.getVersion());
		}else {
			rib.RIBlog.warnLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getVersion(): "+sliceToEmbed.getVersion());
		}
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getVirtuallinkList(): "+sliceToEmbed.getVirtuallinkList());
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: sliceToEmbed.getVirtuallinkList(): "+sliceToEmbed.getVirtualnodeList());


		//OBTAIN RESIDUAL VLINKS
		//for each vlink to remove
		Iterator<vLink> vLinkIterToDelete= vLinkCurrentList.iterator();
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vLinkIterToDelete: "+vLinkIterToDelete);

		//iterate over the list
		Iterator<vLink> vLinkIterResidual= sliceToEmbed.getVirtuallinkList().iterator();
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vLinkIterResidual: "+vLinkIterResidual);

		while(vLinkIterResidual.hasNext()) {
			vLink vlResidual = vLinkIterResidual.next();
			int vlResidualID = vlResidual.getVLinkId();
			boolean linkToInsert = true;

			while(vLinkIterToDelete.hasNext()) {
				vLink vlToDelete = vLinkIterToDelete.next();
				int vlinkIDToDelete = vlToDelete.getVLinkId();

				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vlToDelete+" with LinkID: "+vlinkIDToDelete+" to remove");
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vlResidual+" with LinkID: "+vlResidualID +" candidate to remove");

				if(vlinkIDToDelete == vlResidualID) {
					rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: DONT INSERT vlink with ID: "+vlinkIDToDelete);
					linkToInsert = false;
					break;
				}
			}
			if(linkToInsert) {
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: INSERT vlink: "+vlResidual);

				new_slice.addVirtuallink(vlResidual);
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vlResidual+" with LinkID: "+vlResidualID +" remains ");
			}else {
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vlResidual+" with LinkID: "+vlResidualID +" removed");
				//insert it into the residual
				residual_slice.addVirtuallink(vlResidual);
			}

		}

		//OBTAIN RESIDUAL VNODES
		//for each vlink to remove
		Iterator<vNode> vNodeIterToDelete= vNodeCurrentList.iterator();
		//iterate over the list
		Iterator<vNode> vNodeIterResidual= sliceToEmbed.getVirtualnodeList().iterator();


		while(vNodeIterResidual.hasNext()) {
			vNode vnResidual = vNodeIterResidual.next();
			int vnResidualID = vnResidual.getVNodeId();
			boolean nodeToInsert = true;

			while(vNodeIterToDelete.hasNext()) {
				vNode vnToDelete = vNodeIterToDelete.next();
				int vnodeIDToDelete = vnToDelete.getVNodeId();

				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vnode: "+vnToDelete+" with NodeID: "+vnodeIDToDelete+" to remove");
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vnode: "+vnResidual+" with NodeID: "+vnResidualID +" candidate to remove");

				if(vnResidualID == vnodeIDToDelete) {//if the candidate is equal to the one to remove stop it and continue without insertion
					nodeToInsert = false;
					break;
				}
			}
			if(nodeToInsert) {// if the candidate vnode was not found among the vnodes to remove insert it in the residual
				new_slice.addVirtualnode(vnResidual);
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vnResidual+" with LinkID: "+vnResidualID +" remains ");
			}else {
				residual_slice.addVirtualnode(vnResidual);
				rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: vlink: "+vnResidual+" with LinkID: "+vnResidualID +" removed");
				
			}

		}

		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: residual_slice.getVirtuallinkList(): "+residual_slice.getVirtuallinkList());
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: residual_slice.getVirtualnodeList(): "+residual_slice.getVirtualnodeList());

		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: new_slice.getVirtuallinkList(): "+new_slice.getVirtuallinkList());
		rib.RIBlog.debugLog("SliceGenerator::generateResidualSlice: new_slice.getVirtualnodeList(): "+new_slice.getVirtualnodeList());
		partitionsObj.setToEmbed(new_slice);
		partitionsObj.setResidual(residual_slice);
		
		
		
		
		return partitionsObj;
	}

	

}

