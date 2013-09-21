package test.CPUUsage;

/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */

import java.util.LinkedHashMap;

public class cpuMonitor extends Thread {
	
    private String appName = null;
    private LinkedHashMap<String,Double> appStatus = null;
    private boolean stop = false;
    private int updateFrequency = 1; // unit second

    
	public cpuMonitor(String appName,  LinkedHashMap<String,Double> appStatus)
	{
		this.appName = appName;
		this.appStatus = appStatus;
	}
	
	public void run()
	{
		int threadID = getCPUInJava.getThreadID("java -jar");
		
		while(!stop)
		{
			double cpuUsage = getCPUInJava.getCPUsage(threadID);

			this.appStatus.put(this.appName, cpuUsage);
			
			
		
			try {
				Thread.sleep(updateFrequency * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println("this.appStatus is " + this.appStatus);
		}
	}

}
