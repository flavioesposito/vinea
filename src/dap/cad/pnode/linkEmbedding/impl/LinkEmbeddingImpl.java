/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode.linkEmbedding.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

import rina.rib.impl.RIBImpl;

import dap.cad.pnode.Pnode;
import dap.cad.pnode.linkEmbedding.api.LinkEmbeddingAPI;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;
import dap.util.Unix;

/**
 * @author flavioesposito
 * link embedding implementation
 */
public class LinkEmbeddingImpl extends Thread implements LinkEmbeddingAPI {

	/**
	 * Pnode who started this link embedding process
	 */
	Pnode ownerPnode = null;
	/**
	 * source of the virtual link 
	 */
	String srcName = null;

	/**
	 * source of the virtual link 
	 */
	String dstName = null;

	/**
	 * Resource Information Baes
	 */
	public RIBImpl rib = null;
	
	/**
	 * utility class to easily run Unix commands (e.g. mininet CLI)
	 */
	public Unix unix = null;

	/**
	 * dummy constructor
	 */
	public LinkEmbeddingImpl(RIBImpl rib) {
		this.rib = rib;
	}

	/**
	 * constructor 
	 * @param ownerPnode object
	 * @param destName: string to translate into address of virtual interface
	 */
	public LinkEmbeddingImpl(Pnode ownerPnode, String destName) {
		this.ownerPnode = ownerPnode;
		this.srcName  = ownerPnode.getAppName();
		this.dstName = destName;
		this.createLink(srcName,dstName);
		this.rib = new RIBImpl();
		this.unix = new Unix(this.rib);

		this.start();
	}
	/**
	 * implement one link at the time
	 * @param srcName
	 * @param dstName
	 */
	public void createLink(String srcName, String dstName) {

		this.ownerPnode.rib.RIBlog.infoLog("=================================================================================");
		this.ownerPnode.rib.RIBlog.infoLog("============Embed Link here between "+srcName+" and "+dstName+"==========");
		this.ownerPnode.rib.RIBlog.infoLog("=================================================================================");


	}
	/**
	 * 
	 * @param slice
	 */
	public void createTopology(SliceSpec.Slice slice) {


		this.ownerPnode.rib.RIBlog.infoLog("================================================");
		this.ownerPnode.rib.RIBlog.infoLog("============Embed entire vn topology here=======");
		this.ownerPnode.rib.RIBlog.infoLog("================================================");


		//make sure the other networks are gone
		unix.execute("sudo killall ovs-controller");
		unix.execute("sudo mn -c");

		

		String topology = slice.getTopology();
		Integer vnodes = slice.getVirtualnodeCount();
		Integer vlinks = slice.getVirtuallinkCount();


		String command = "sudo ./createTopology.py "+topology+ " "+vnodes+" "+vlinks;
		rib.RIBlog.debugLog(command);
		unix.execute(command);


	}
	/**
	 * create a python script to run inside VM with a given vSwitch topology: for example
	 * h1 <-> s1 <-> s2 .. sN-1
	 *  |       |    |
	 *  h2      h3   hN
	 *  
	 *  WARNING: by default, the reference controller only supports 16 switches
	 *  
	 * @param virtual network spec object 
	 */
	public void createVN(SliceSpec.Slice slice) {


		LinkedHashMap<String,String> argsMap = new LinkedHashMap<String,String>(); 
		argsMap.put("topology", slice.getTopology());
		Integer vnodes = slice.getVirtualnodeCount();
		Integer vlinks = slice.getVirtuallinkCount();

		argsMap.put("vnodesCount", vnodes.toString());
		argsMap.put("vlinksCount", vlinks.toString());


		List<vLink> vlinkList = slice.getVirtuallinkList();
		List<vNode> vnodeList = slice.getVirtualnodeList();
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: vnodeList: "+vnodeList);
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: vlinkList: "+vlinkList);




		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: creating python mininet convergence layer...");
		PrintWriter writer;
		try {
			writer = new PrintWriter("createVN.py", "UTF-8");
			writer.println("#!/usr/bin/python");

			writer.println("from mininet.net import Mininet");
			writer.println("from mininet.node import Controller");
			writer.println("from mininet.log import setLogLevel, info");
			writer.println("");
			writer.println("");
			
			writer.println("    ");//add sshd support
			writer.println("    ");//add our of VN traffic support 
			writer.println("    ");//add testing and log
			
			
			writer.println("def create():");
			writer.println("    net = Mininet( controller=Controller )");
			writer.println("    info( '*** Adding controller\n' )");
			writer.println("    net.addController( 'c0' )");

			Integer totalCapacity = 0;
			for (vNode vn : vnodeList)
			{
				if(vn.hasVNodeCapacity())
					totalCapacity+=vn.getVNodeCapacity();	
			}
			String totCPU =totalCapacity.toString();

			writer.println("    info( '*** Adding virtual nodes (at least one host and one switch) \n' )");
			for (vNode vn : vnodeList)
			{
				Integer hostname = vn.getVNodeId();
				String h = hostname.toString();
				int vnID = vn.getVNodeId();
				writer.println("    s"+vnID+" = net.addSwitch( 's"+vnID+"' )");

				if(vn.hasVNodeCapacity()) {
					// Each host gets 50%/n of system CPU
					//host = self.addHost('h%s' % (h + 1), cpu=.5/n)
					Integer vnCap = vn.getVNodeCapacity();
					String vnCPU =vnCap.toString();

					writer.println("    h"+vnID+" = net.addHost( '"+h+"', ip='10.0.0."+vnID+"', cpu=."+vnCPU+"/"+totCPU +" )");
				}
				else {

					writer.println("    h"+vnID+" = net.addHost( '"+h+"', ip='10.0.0."+vnID+"' )");
				}

				writer.println("    info( '*** Connecting virtual node \n' )");
				writer.println("    h"+vnID+".linkTo( s"+vnID+" )");
			}

			// 10 Mbps, 5ms delay, 10% loss, 1000 packet queue
			// self.addLink(host, switch, bw=10, delay='5ms', loss=10, max_queue_size=1000, use_htb=True)
			for (vLink vl : vlinkList)
			{
				Integer src = vl.getVSrcID();
				String h_src = "s"+src.toString();

				Integer dst = vl.getVDstID();
				String h_dst = "s"+dst.toString();
				if(vl.hasVLinkCapacity()) {
					Integer vlinkCapacity = vl.getVLinkCapacity();
					String vlBW =  vlinkCapacity.toString();
					writer.println("    self.addLink( s"+h_src+", s"+h_dst+", bw="+vlBW+" )");
				}else {
					writer.println("    self.addLink( s"+h_src+", s"+h_dst+")");
				}

			}



			writer.println("    info( '*** Starting virtual network\n')");
			writer.println("    net.start()");
			writer.println("    ");//add sshd support
			writer.println("    ");//add testing and log
			writer.println("");
			writer.println("if __name__ == '__main__':");
			writer.println("    setLogLevel( 'info' )");
			writer.println("    create()");

			writer.close();



		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: changing file permissions...");
		unix.execute("chmod 755 *");

		String folder = System.getProperty("user.dir");
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: folder is: "+folder);


		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: changing file permissions...");
		String command = "sudo python "+folder+"/createVN.py";
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: command is: "+command );

		unix.execute(command);

	}

	
	/**
	 * monitor link
	 */
	public void run(){
		this.monitorLink(srcName,dstName);
	}

	private void monitorLink(String srcName2, String dstName2) {

		//TODO: create and handle monitoring pub/sub events here
		// we only focus on embedding in the thesis

	}

	/**
	 * create subVN only of the nodes that are mine and enable Internet connection (out of VN traffic)
	 * @param slice
	 */
	public void createVNmultiInP(Slice slice) {
		// TODO Auto-generated method stub
		
	}

}
