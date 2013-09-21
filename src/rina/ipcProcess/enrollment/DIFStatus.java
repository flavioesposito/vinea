/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
 

package rina.ipcProcess.enrollment;



import java.util.LinkedHashMap;
import rina.rib.impl.RIBDaemonImpl;
/**
 * check the status of each member in this DIF
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 *
 */
public class DIFStatus extends Thread{


	private RIBDaemonImpl RIBdaemon = null;

	private LinkedHashMap<String, Boolean> ipcStatus = null;
	private LinkedHashMap<String, Boolean> ipcProbeStatus = null;

	private int period;

	private boolean STOP = false;

	public DIFStatus(int period, RIBDaemonImpl RIBdaemon,LinkedHashMap<String, Boolean> ipcStatus,  LinkedHashMap<String, Boolean> ipcProbeStatus )
	{
		this.period = period;
		this.RIBdaemon = RIBdaemon;
		this.ipcStatus = ipcStatus;
		this.ipcProbeStatus = ipcProbeStatus;

	}


	public void run()
	{



		while(!STOP)
		{
			try {
				Thread.sleep(this.period*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			String[] ipcName = LinkedHashMapKeyToArray(this.ipcProbeStatus);

			for(int i = 0; i< ipcName.length; i++ )
			{
				if(this.ipcProbeStatus.get(ipcName[i]).equals(Boolean.FALSE))
				{
               
					//this member is down
					if(this.RIBdaemon.localRIB.hasMember(ipcName[i]))
					{
					this.ipcStatus.put(ipcName[i], Boolean.FALSE);
					this.RIBdaemon.localRIB.removeMemberListElement(ipcName[i]);
					this.RIBdaemon.localRIB.RIBlog.infoLog("NMS Process: DIF Status : " + ipcName[i] + " was down");
					}
					
					
				}else
				{
					if( this.ipcStatus.get(ipcName[i]).equals(Boolean.FALSE))
					{
						this.ipcStatus.put(ipcName[i], Boolean.TRUE);
						//this member is back 
						this.RIBdaemon.localRIB.writeMemberListElement(ipcName[i]);
						
						this.RIBdaemon.localRIB.RIBlog.infoLog("NMS Process: DIF Status : " + ipcName[i] + " is back");
					}

					//reset it to false
					this.ipcProbeStatus.put(ipcName[i],Boolean.FALSE);

				}

			}


		

		}
	}
	/**
	 * convert LinkedHashMap Key To Array
	 * @param map
	 * @return array
	 */
	public  static String[] LinkedHashMapKeyToArray( LinkedHashMap hp)
	{
		int num = hp.size();

		String[] keyArray= new String[num];

		Object [] array ;
		array = hp.keySet().toArray();


		for(int j =0;j< num;j++)
		{
			keyArray[j] =  array[j].toString();
		} 
		return keyArray;


	}
}
