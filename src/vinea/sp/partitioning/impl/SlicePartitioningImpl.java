/**
 * 
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 */
package vinea.sp.partitioning.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import rina.rib.impl.RIBImpl;
import test.Debug;
import vinea.slicespec.impl.googleprotobuf.SliceSpec;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;
import vinea.sp.partitioning.api.SlicePartitioningAPI;
import vinea.sp.slicegenerator.SliceGenerator;
import vinea.sp.util.SlicePartition;

/**
 * @author Flavio Esposito
 * slice partitioning implementation
 */
public class SlicePartitioningImpl implements SlicePartitioningAPI {

	/**
	 * for SAD policy: send subnetwork of size partitionSize 
	 */
	private final int _partitionSize= 1;//one vlink at the time

	/**
	 * resource information base
	 */
	RIBImpl rib = null;

	/**
	 * node auction policies 
	 */
	private String _vnodeAuctionPolicy; 

	/**
	 * dummy constructor
	 */
	public SlicePartitioningImpl() {}

	/**
	 *  constructor
	 */
	public SlicePartitioningImpl(RIBImpl rib) {

		this.rib = rib;

	}

	/**
	 * construct the partition and keeps track of what should still be embed 
	 * according to specified policies
	 * 
	 * rebuild the slice with the same procedure of when it was generated using the same data
	 * only consider the first partitionSize vNodes and all its links
	 * get the list of nodes and links to subtract from residual slice
	 * generate a partition object and stores it in the rib
	 * 
	 * @return partition result (or errorcode if the slice was  notpartitioned successfully) 
	 * result: "OK" : slice successfully partitioned
	 * result: "EmptySlice" or "mad" : no partitioning needed 
	 * result: "NoMorePartitions" ready to start link embedding phase
	 * result: "unsupportedPolicy" error in the config file
	 */
	public String partitionSliceRequest(Integer sliceID) {

		// get the slice
		SliceSpec.Slice.Builder fullSlice = (SliceSpec.Slice.Builder)
				rib.getAttribute(sliceID.toString());

		//check if slice is null and return error code
		if(fullSlice==null) { 
			rib.RIBlog.errorLog("SlicePartitioningImpl::partitionSliceRequest: Slice is Empty");
			return "EmptySlice"; 
		}	

		//create empty partition object
		SlicePartition partitionsObj = new SlicePartition();
		partitionsObj.setSliceID(sliceID);

		this._vnodeAuctionPolicy = (String)rib.getAttribute("vnodeAuctionPolicy");

		if(this._vnodeAuctionPolicy==null ||
				this._vnodeAuctionPolicy.toLowerCase().equals("mad")) {
			rib.RIBlog.warnLog("-------------------------------------------------------------------------- ");
			rib.RIBlog.warnLog("----- MAD policy or NO auction policy set in config file (default MAD)----- ");
			rib.RIBlog.warnLog("-------------------------------------------------------------------------- ");

			// set the attributes of the SlicePartitions object for MAD
			partitionsObj.setAlreadyEmbedded(null);
			partitionsObj.setToEmbed(fullSlice);

			// write on the rib the partition obj
			rib.addAttribute("partition"+sliceID.toString(), partitionsObj);
			rib.RIBlog.debugLog("SlicePartitioningImpl::partitionSliceRequest: partition obj "+partitionsObj+" with sliceID: "+partitionsObj.getSliceID()+"written on the rib");

			return "MAD"; // no partitioning for MAD

		} else if(this._vnodeAuctionPolicy.toLowerCase().equals("sad")) {
			rib.RIBlog.infoLog("SlicePartitioningImpl::partitionSliceRequest: partitioning the slice according to SAD policy");

			List<vNode> vNodeResidualList = null;
			List<vLink> vLinkResidualList = null;

			//if there exist a partition obj already associated with that sliceID take it
			if(rib.getAttribute("partition"+sliceID.toString()) != null) {
				partitionsObj = (SlicePartition) rib.getAttribute("partition"+sliceID.toString());
				rib.RIBlog.debugLog("SlicePartitioningImpl::partitionSliceRequest: partition obj "+partitionsObj+" for slice "+partitionsObj.getSliceID()+" existed already on the rib");

				vNodeResidualList = partitionsObj.getToEmbed().getVirtualnodeList();
				vLinkResidualList = partitionsObj.getToEmbed().getVirtuallinkList();

			} else {// use the full slice
				vNodeResidualList = fullSlice.getVirtualnodeList();
				vLinkResidualList = fullSlice.getVirtuallinkList();
			}
			rib.RIBlog.debugLog("SlicePartitioningImpl::partitionSliceRequest:: vNodeResidualList: "+vNodeResidualList);
			rib.RIBlog.debugLog("SlicePartitioningImpl::partitionSliceRequest:: vLinkResidualList: "+vLinkResidualList);

			List<vNode> vNodeCurrentList = new LinkedList<vNode>();
			List<vLink> vLinkCurrentList = new LinkedList<vLink>();


			for(int i=0;i<_partitionSize;i++) {

				//add residual vlinks
				if(vNodeResidualList.size()==0 || vNodeResidualList==null) {
					rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: empty vNodeResidualList: "+vNodeResidualList);
				}else {

					rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: adding vLinkResidualList.get("+i+"): "+vLinkResidualList.get(i));		
					
					//if a resource (node or link) is in vNodeResidualList or vLinkResidualList it has been not already embedded				
					vLinkCurrentList.add(vLinkResidualList.get(i));
					rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: adding vLink: "+vLinkResidualList.get(i));

					int srcID = vLinkResidualList.get(i).getVSrcID();
					int destID = vLinkResidualList.get(i).getVDstID();		

					//add residual vnodes
					Iterator<vNode> vnodeIter = vNodeResidualList.iterator();
					//rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: vnodeIter: "+vnodeIter);

					while(vnodeIter.hasNext()) {
						vNode vCandidate = vnodeIter.next();
						if(vCandidate.getVNodeId()==srcID || vCandidate.getVNodeId()==destID) {
							vNodeCurrentList.add(vCandidate);
							rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: adding vNode "+vCandidate);
						}
					}

				}
			}

			rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: vNodeCurrentList to be embedded: "+vNodeCurrentList);
			rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: vLinkCurrentList to be embedded: "+vLinkCurrentList);


			//don't do anything if there are no more nodes 
			if(vNodeCurrentList.isEmpty() ) {
				//handle no more partition in the sliceProvider
				rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: NO MORE PARTITIONS");
				//logSuccess(sliceID);
				return "NoMorePartitions";

			}else { //create the slice partition and store it into the rib

				SliceGenerator sg = new SliceGenerator();
				
				// create the residual_slice: the original - slice to embed in this sad round
				// the residual_slice would be replaced to the _sliceToEmbed in case of success
				SliceSpec.Slice.Builder sliceToPartition = null;//fullSlice;
				if(partitionsObj.getToEmbed()==null) {
					sliceToPartition = fullSlice;
				}else {
					sliceToPartition =partitionsObj.getToEmbed(); 
				}
				
				SlicePartition newPartitionsObj = sg.generateResidualSlice(sliceToPartition, //original slice
						vNodeCurrentList, //new vnodes to extract from this._sliceToEmbed
						vLinkCurrentList ); //new vlinks to extract from this._sliceToEmbed

				rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: partitionsObj.getToEmbed(): ");
				Debug.printSliceDetails(newPartitionsObj.getToEmbed(), rib);
				rib.RIBlog.debugLog("SliceProvider::partitionSliceRequest: residualSlice: ");
				Debug.printSliceDetails(newPartitionsObj.getResidual(), rib);

				rib.addAttribute("partition"+sliceID.toString(), newPartitionsObj);
				return "OK";
			}

		}else {
			rib.RIBlog.errorLog("SliceProvider: partitionSliceRequest :policy "+ this._vnodeAuctionPolicy+"not supported");
			return "unsupportedPolicy";
		}


	}//end of partitionSliceRequest

	


	/**
	 * log new slice embedding success
	 */
	private void logSuccess(int sliceID) {

		//log that the slice can be allocated
		LinkedHashMap<Integer,SliceSpec.Slice.Builder> successfullyEmbeddedSlices = (LinkedHashMap<Integer,SliceSpec.Slice.Builder>)
				rib.getAttribute("successfullyEmbeddedSlices");

		//get current state
		int successfullyEmbedded = (Integer)rib.getAttribute("successfullyEmbedded"); 
		int totalRequestedSlices = (Integer)rib.getAttribute("totalRequestedSlices");				
		int failedEmbedding = (Integer)rib.getAttribute("failedEmbedding");

		rib.RIBlog.debugLog("failedEmbedding so far: "+failedEmbedding);
		rib.RIBlog.debugLog("successfullyEmbedded so far: "+successfullyEmbedded);
		rib.RIBlog.debugLog("totalRequestedSlices: "+totalRequestedSlices);


		if(successfullyEmbeddedSlices.containsKey(sliceID)) {
			rib.RIBlog.warnLog("SliceProvider::partitionSliceRequest: we already know about this slice");
			//	return;//stop this embedding process
		}else {
			//log new success
			LinkedHashMap<Integer,SliceSpec.Slice.Builder> slicesToEmbed = 
					(LinkedHashMap<Integer,SliceSpec.Slice.Builder>)
					rib.getAttribute("slicesToEmbed");
				
			SliceSpec.Slice.Builder fullSlice = slicesToEmbed.get(sliceID);
			
			successfullyEmbeddedSlices.put(sliceID, fullSlice);
			rib.RIBlog.infoLog("SliceProvider::partitionSliceRequest: successfully embedding registered");
			rib.addAttribute("successfullyEmbeddedSlices", successfullyEmbeddedSlices);

			//increase the # of successfully embedded slices
			successfullyEmbedded = (Integer)rib.getAttribute("successfullyEmbedded")+1;
			rib.addAttribute("successfullyEmbedded", successfullyEmbedded);

			rib.RIBlog.debugLog("failedEmbedding after adding new: "+failedEmbedding);
			rib.RIBlog.debugLog("successfullyEmbedded after adding new: "+successfullyEmbedded);
			rib.RIBlog.debugLog("totalRequestedSlices after adding new: "+totalRequestedSlices);

		}
		
	}



}//end of class
