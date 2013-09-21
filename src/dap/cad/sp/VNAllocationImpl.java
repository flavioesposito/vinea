/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */
package dap.cad.sp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import rina.rib.impl.RIBImpl;

import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.Builder;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vLink;
import dap.cad.slicespec.impl.googleprotobuf.SliceSpec.Slice.vNode;
import dap.util.Unix;

/**
 * @author Flavio Esposito
 *
 */
public class VNAllocationImpl extends Thread{

	/**
	 * Resource Information Base (generalization of Network Information Base of the RINA network architecture)
	 */
	public RIBImpl rib = null;
	/**
	 * java library to run unix command
	 */
	public Unix unix = null;

	/**
	 * to write on file
	 */
	private PrintWriter writer;

	/**
	 * constructor 
	 * @param rib of the service provider
	 */
	public VNAllocationImpl(RIBImpl rib) {

		this.rib = rib;
		this.unix = new Unix(rib);
		this.cleanUp();
	}

	/**
	 * create a "mininet convergence layer: a middleware between cadsys and mininet
	 * i.e. a python script to run inside VM with a given topology of virtual switches 
	 * 
	 * For example:
	 * 
	 * h1 <-> s1 <-> s2 .. sN-1
	 *  |       |    |
	 *  h2      h3   hN
	 *  
	 *  WARNING: by default, the reference controller only supports 16
	 *  switches, so this test WILL NOT WORK unless you have recompiled
	 *  your controller to support 100 switches (or more.)
	 *  
	 * @param slice (virtual network spec. object) 
	 */
	public void createVN(SliceSpec.Slice.Builder slice) {

		if(slice==null) {
			rib.RIBlog.errorLog("VNAllocationImpl::createVN: slice null");
		}



		//retrieve slice info to create mininet convergence layer
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



		//create mininet convergence layer (middleware between cadsys and mininet)
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: creating python mininet convergence layer...");

		try {
			writer = new PrintWriter("createVN.py", "UTF-8");
			writer.println("#!/usr/bin/python");

			writer.println("from mininet.net import Mininet");
			writer.println("from mininet.node import Controller");
			writer.println("from mininet.log import setLogLevel, info");
			writer.println("from mininet.util import ensureRoot");
			writer.println("from mininet.topo import Topo");
			writer.println("");
			writer.println("");
			writer.println("ensureRoot()");

			writer.println("");
			writer.println("");
			writer.println("def create():");
			writer.println("    net = Mininet( controller=Controller )");
			////we don't use the controller so far but it's a nice to have
			writer.println("    info( '*** Adding controller' )"); 
			writer.println("    net.addController( 'c0' )");

			// dummy way of defining virtual node CPU usage 
			Integer totalCapacity = 0;
			for (vNode vn : vnodeList)
			{
				if(vn.hasVNodeCapacity())
					totalCapacity+=vn.getVNodeCapacity();	
			}
			String totCPU =totalCapacity.toString();

			writer.println("    info( '*** Adding virtual nodes (at least one host and one switch)' )");

			for (vNode vn : vnodeList)
			{
				Integer hostname = vn.getVNodeId();
				Integer hostnameIP = hostname+1;//otherwise we get 10.0.0.0 
				//String h = hostname.toString();
				String hIP = hostnameIP.toString();
				if(hostname+1>255){
					rib.RIBlog.errorLog("LinkEmbeddingImpl::createVN: we don't support more than 255 virtual nodes");
					return;
				}
				
				rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: h: "+hIP);
				rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: hIP: 10.0.0."+hIP);
				writer.println("    s"+hIP+" = net.addSwitch( 's"+hIP+"' )");

				if(vn.hasVNodeCapacity()) {
					// Each host gets 50%/n of system CPU
					//host = self.addHost('h%s' % (h + 1), cpu=.5/n)
					Integer vnCap = vn.getVNodeCapacity();
					String vnCPU =vnCap.toString();

					writer.println("    h"+hIP+" = net.addHost( 'h"+hIP+"', inNamespace=False, ip='10.0.0."+hIP+"', cpu=."+vnCPU+"/"+totCPU +" )");
				}
				else {

					writer.println("    h"+hIP+" = net.addHost( '"+hIP+"', inNamespace=False, ip='10.0.0."+hIP+"' )");
				}

			}


			//  h1.linkTo( root )
			//   h2.linkTo( root )
			//    h3.linkTo( root )
			Integer rootAddress = vnodeList.size()+1;
			String  rootAdd = rootAddress.toString();    
			writer.println("    root = net.addHost( 'root', ip='10.0.0."+rootAdd+"', inNamespace=False )");		    

			for (vNode vn : vnodeList)
			{
				Integer hostname = vn.getVNodeId();
				Integer hostnameIP = hostname+1;//otherwise we get 10.0.0.0 
				String hIP = hostnameIP.toString();
				rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: connecting host: "+hIP+" to switch s"+hIP);

				writer.println("    info( '*** Connecting virtual node' )");
				writer.println("    h"+hIP+".linkTo( s"+hIP+" )");
				writer.println("    h"+hIP+".linkTo( root )");
				//add sshd support
				rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: adding support for ssh...");
				writer.println("    print \"*** Creating banner file\"");
				writer.println("    f = open( '/tmp/%s.banner' % h"+hIP+".name, 'w' )");
				writer.println("    f.write( 'Welcome to %s at %s\\n' % ( h"+hIP+".name, h"+hIP+".IP() ) )");
				writer.println("    f.close()");
				writer.println("");
				writer.println("    h"+hIP+".cmd( '/usr/sbin/sshd -o \"Banner /tmp/%s.banner\"' % h"+hIP+".name ) ");
				writer.println("    print \"*** you may now ssh into h"+hIP+"\"");
				writer.println("");
			}


			// self.addLink(host, switch, bw=10, delay='5ms', loss=10, max_queue_size=1000, use_htb=True)
			for (vLink vl : vlinkList)
			{
				Integer src = vl.getVSrcID()+1;
				String h_src = src.toString();


				Integer dst = vl.getVDstID()+1;
				String h_dst = dst.toString();
				rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: adding link between s"+h_src+" and s"+h_dst);


				if(src==dst) continue;//avoid setting up links with itself

				if(vl.hasVLinkCapacity()) {
					Integer vlinkCapacity = vl.getVLinkCapacity();
					String vlBW =  vlinkCapacity.toString();
					writer.println("    net.addLink( s"+h_src+", s"+h_dst+", bw="+vlBW+" )");
				}else {
					writer.println("    net.addLink( s"+h_src+", s"+h_dst+")");
				}

			}

			writer.println("    ");//add testing and log
			// vn is ready to be started
			writer.println("    info( '*** Starting virtual network')");
			writer.println("    net.start()");
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

		this.start();

	}



	/**
	 * creates and runs the mininet convergence layer for VNs allocation 
	 */
	public void createMultiVNs() {

		createMininetScript();
		this.start();
	}

	/**
	 * create mininet script from all slices that were successful in the embedding process
	 */
	private void createMininetScript() {

		initializeScript();

		
		LinkedHashMap<Integer,SliceSpec.Slice.Builder> successfullyEmbeddedSlices = 
				(LinkedHashMap<Integer,SliceSpec.Slice.Builder>) rib.getAttribute("successfullyEmbeddedSlices");
		
	

		//for each VN
		Set<Integer> keySet = successfullyEmbeddedSlices.keySet();
		Iterator<Integer> sliceIter = keySet.iterator();
		while(sliceIter.hasNext()) { 
			int sliceID = sliceIter.next();
			SliceSpec.Slice.Builder sliceToEmbed =  successfullyEmbeddedSlices.get(sliceID);
			// 		write sub-script for this particular VN
			writeSliceOnScript(sliceToEmbed);
		}
		//close script
		closeScript();

	}

	/**
	 * augment the python script parsing the builderFile and using the mininet API  
	 * @param sliceToEmbed
	 */
	private void writeSliceOnScript(Builder slice) {

		//test the perf.py and modify that... 
		//forget about ssh if it does not work with tc (it works!)
		
		if(slice==null) {
			rib.RIBlog.errorLog("VNAllocationImpl::writeSliceOnScript: slice null");
		}


		//retrieve slice info to create mininet convergence layer
		LinkedHashMap<String,String> argsMap = new LinkedHashMap<String,String>(); 
		argsMap.put("topology", slice.getTopology());
		
		Integer vnodes = slice.getVirtualnodeCount();
		Integer vlinks = slice.getVirtuallinkCount();
		argsMap.put("vnodesCount", vnodes.toString());
		argsMap.put("vlinksCount", vlinks.toString());

		List<vLink> vlinkList = slice.getVirtuallinkList();
		List<vNode> vnodeList = slice.getVirtualnodeList();
		rib.RIBlog.debugLog("LinkEmbeddingImpl::writeSliceOnScript: vnodeList: "+vnodeList);
		rib.RIBlog.debugLog("LinkEmbeddingImpl::writeSliceOnScript: vlinkList: "+vlinkList);

		
		
		// dummy way of defining virtual node CPU usage 
		Integer totalCapacity = 0;
		for (vNode vn : vnodeList)
		{
			if(vn.hasVNodeCapacity())
				totalCapacity+=vn.getVNodeCapacity();	
		}
		String totCPU =totalCapacity.toString();

		writer.println("    info( '*** Adding virtual nodes (at least one host and one switch)' )");
		
		int assignedAddresses = 0;
		if(rib.getAttribute("assignedAddresses")== null) {
			rib.addAttribute("assignedAddresses", 0);
		}else {
			assignedAddresses = (Integer) rib.getAttribute("assignedAddresses");
			rib.RIBlog.debugLog("LinkEmbeddingImpl::writeSliceOnScript:assignedAddresses is: "+assignedAddresses);
		}
		

		for (vNode vn : vnodeList)
		{
			Integer hostname = vn.getVNodeId();
			Integer hostnameIP = assignedAddresses+hostname+1;//otherwise we get 10.0.0.0
			if(assignedAddresses+hostname+1>255){
				rib.RIBlog.errorLog("LinkEmbeddingImpl::writeSliceOnScript:we don't support more than 255 virtual nodes");
				return;
			}
			//String h = hostname.toString();
			String hIP = hostnameIP.toString();

			rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: h: "+hIP);
			rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: hIP: 10.0.0."+hIP);
			writer.println("    s"+hIP+" = net.addSwitch( 's"+hIP+"' )");

			if(vn.hasVNodeCapacity()) {
				// Each host gets 50%/n of system CPU
				//host = self.addHost('h%s' % (h + 1), cpu=.5/n)
				Integer vnCap = vn.getVNodeCapacity();
				String vnCPU =vnCap.toString();

				writer.println("    h"+hIP+" = net.addHost( 'h"+hIP+"', inNamespace=False, ip='10.0.0."+hIP+"', cpu=."+vnCPU+"/"+totCPU +" )");
			}
			else {

				writer.println("    h"+hIP+" = net.addHost( '"+hIP+"', inNamespace=False, ip='10.0.0."+hIP+"' )");
			}

		}

		//update addresses already assigned for future requests
		rib.addAttribute("assignedAddresses", assignedAddresses +=vnodeList.size());
		

		//  h1.linkTo( root )
		//   h2.linkTo( root )
		//    h3.linkTo( root )
		Integer rootAddress = vnodeList.size()+1;
		String  rootAdd = rootAddress.toString();    
		writer.println("    root = net.addHost( 'root', ip='10.0.0."+rootAdd+"', inNamespace=False )");		    

		for (vNode vn : vnodeList)
		{
			Integer hostname = vn.getVNodeId();
			Integer hostnameIP = hostname+1;//otherwise we get 10.0.0.0 
			String hIP = hostnameIP.toString();
			rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: connecting host: "+hIP+" to switch s"+hIP);

			writer.println("    info( '*** Connecting virtual node' )");
			writer.println("    h"+hIP+".linkTo( s"+hIP+" )");
			writer.println("    h"+hIP+".linkTo( root )");
			//add sshd support
			rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: adding support for ssh...");
			writer.println("    print \"*** Creating banner file\"");
			writer.println("    f = open( '/tmp/%s.banner' % h"+hIP+".name, 'w' )");
			writer.println("    f.write( 'Welcome to %s at %s\\n' % ( h"+hIP+".name, h"+hIP+".IP() ) )");
			writer.println("    f.close()");
			writer.println("");
			writer.println("    h"+hIP+".cmd( '/usr/sbin/sshd -o \"Banner /tmp/%s.banner\"' % h"+hIP+".name ) ");
			writer.println("    print \"*** you may now ssh into h"+hIP+"\"");
			writer.println("");
		}


		// self.addLink(host, switch, bw=10, delay='5ms', loss=10, max_queue_size=1000, use_htb=True)
		for (vLink vl : vlinkList)
		{
			Integer src = vl.getVSrcID()+1;
			String h_src = src.toString();


			Integer dst = vl.getVDstID()+1;
			String h_dst = dst.toString();
			rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: adding link between s"+h_src+" and s"+h_dst);


			if(src==dst) continue;//avoid setting up links with itself

			if(vl.hasVLinkCapacity()) {
				Integer vlinkCapacity = vl.getVLinkCapacity();
				String vlBW =  vlinkCapacity.toString();
				writer.println("    net.addLink( s"+h_src+", s"+h_dst+", bw="+vlBW+" )");
			}else {
				writer.println("    net.addLink( s"+h_src+", s"+h_dst+")");
			}

		}
		
	}

	/**
	 * write the common part to every allocation using the mininet api
	 * no matter how many slices are allocated 
	 */
	private void initializeScript() {

		//to keep track of virtual IP addresses already assigned
		rib.addAttribute("assignedAddresses", 0);
		
		try {
			writer = new PrintWriter("createMultiVNs.py", "UTF-8");
			writer.println("#!/usr/bin/python");

			writer.println("from mininet.net import Mininet");
			writer.println("from mininet.node import Controller");
			writer.println("from mininet.log import setLogLevel, info");
			writer.println("from mininet.util import ensureRoot");
			writer.println("from mininet.topo import Topo");
			writer.println("");
			writer.println("");
			writer.println("ensureRoot()"); //to be able to ssh into the nodes for demo purposes

			writer.println("");
			writer.println("");
			writer.println("def create():");
			writer.println("    net = Mininet( controller=Controller )");
			////we don't use the controller so far
			writer.println("    info( '*** Adding controller' )"); 
			writer.println("    net.addController( 'c0' )");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




	}

	/**
	 * Makes sure the other mininet virtual networks are gone when starting the experiment  
	 */
	private void cleanUp() {
		
		unix.execute("sudo killall ovs-controller");
		unix.execute("sudo mn -c");		
	}

	/**
	 * write the main and close the file 
	 */
	private void closeScript() {



		writer.println("    ");//add testing and log
		// vn is ready to be started
		writer.println("    info( '*** Starting virtual network')");
		writer.println("    net.start()");
		writer.println("");
		writer.println("if __name__ == '__main__':");
		writer.println("    setLogLevel( 'info' )");
		writer.println("    create()");
		writer.close();



	}


	/**
	 * starts the VN executing the created python script that uses mininet 
	 * 
	 */
	public void run() {
		rib.RIBlog.debugLog("============================================");
		rib.RIBlog.debugLog("LinkEmbeddingImpl::run: STARTING THE VN...");
		rib.RIBlog.debugLog("============================================");
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: changing file permissions...");
		unix.execute("chmod 755 *");

		String folder = System.getProperty("user.dir");
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: folder is: "+folder);


		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: running python file...");
		String command = "sudo python "+folder+"/createVN.py";
		rib.RIBlog.debugLog("LinkEmbeddingImpl::createVN: command is: "+command );

		unix.execute(command);
	}


}
