/**
 * 
 */
package test;

import rina.rib.impl.RIBImpl;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.Builder;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import vinea.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;

/**
 * @author Flavio Esposito
 *
 */
public class Debug {


	public Debug(){}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * print slice features
	 * @param slice
	 */
	public static void printSliceDetails(Builder slice,RIBImpl rib) {

		
		rib.RIBlog.debugLog("sliceID: "+slice.getSliceID());
		rib.RIBlog.debugLog("EntryTime: "+slice.getEntryTime());
		rib.RIBlog.debugLog("ExitTime: "+slice.getExitTime());
		rib.RIBlog.debugLog("Topology: "+slice.getTopology());

		rib.RIBlog.debugLog("Virtual node Count: "+slice.getVirtualnodeCount());
		rib.RIBlog.debugLog("Virtual link Count: "+slice.getVirtuallinkCount());

		rib.RIBlog.debugLog("Virtual node List: "+slice.getVirtualnodeList());
		rib.RIBlog.debugLog("Virtual link List: "+slice.getVirtuallinkList());

		for (vNode vn : slice.getVirtualnodeList()) {
			rib.RIBlog.debugLog("--------- begin vnode ------------");
			rib.RIBlog.debugLog("Virtual node ID: "+vn.getVNodeId());
			rib.RIBlog.debugLog("Virtual node capacity: "+vn.getVNodeCapacity());
			rib.RIBlog.debugLog("Virtual node class: "+vn.getVNodeClass());
			rib.RIBlog.debugLog("Virtual node name: "+vn.getVNodeName());
			rib.RIBlog.debugLog("Virtual node type: "+vn.getVNodeType());
			rib.RIBlog.debugLog("--------- end vnode ------------");		
		}

		for (vLink vl : slice.getVirtuallinkList()) {
			rib.RIBlog.debugLog("--------- begin link ------------");
			rib.RIBlog.debugLog("Virtual link type: "+vl.getVLinkId());
			rib.RIBlog.debugLog("Virtual link capacity: "+vl.getVLinkCapacity());
			rib.RIBlog.debugLog("Virtual link destID: "+vl.getVDstID());
			rib.RIBlog.debugLog("Virtual link srcID: "+vl.getVSrcID());
			rib.RIBlog.debugLog("Virtual link type: "+vl.getVLinkType());
			rib.RIBlog.debugLog("--------- end link ------------");
		}
		rib.RIBlog.debugLog("version: --------- "+slice.getVersion());	
	}
	
}

