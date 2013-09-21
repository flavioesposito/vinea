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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


public class getCPUInJava {

	public static void main(String args[])
	{
		for(int i =0;i<50;i++)
		{
			new doingMath().start();
		}

		int threadId = getThreadID("java -jar");

		while(true)
		{
			getCPUsage(threadId);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static int getThreadID(String threadPattern)
	{
		int id = -1;


		try {  
			Process p = Runtime.getRuntime().exec("ps aux");  

			BufferedReader in = new BufferedReader(  
					new InputStreamReader(p.getInputStream()));  
			String line = null;  


			String[] result = new String[30];

			while ((line = in.readLine()) != null )
			{  
				if(line.contains(threadPattern))
				{
					//			System.out.println(line);  

					StringTokenizer st = new StringTokenizer(line);

					int count = 0;
					while (st.hasMoreTokens()) {
						result[count] = st.nextToken();
						//System.out.println( count + "  " + result[count]);
						count++;
					}
					break;
				}

			}  

			id = Integer.parseInt(result[1]);


		} catch (Exception e) {  
			e.printStackTrace();  
			System.exit(0);
		}  

		System.out.println("thread id of  " + threadPattern + " is "  + id);
		return id;
	}

	public static double getCPUsage(int threadID)
	{
		double cpuUsage = 0;

		try {  

			String cmd = "top -b -n 100 -p " + threadID;

			Process p = Runtime.getRuntime().exec(cmd);  

			BufferedReader in = new BufferedReader(  
					new InputStreamReader(p.getInputStream()));  
			String line = null;  

			String[] result = new String[12];

			while ((line = in.readLine()) != null )
			{  
				if(line.contains(String.valueOf(threadID)))
				{
					//System.out.println(line);  

					StringTokenizer st = new StringTokenizer(line);

					int count = 0;
					while (st.hasMoreTokens()) {
						result[count] = st.nextToken();
						//	System.out.println( count + "  " + result[count]);
						count++;
					}
					break;
				}

			}  

			cpuUsage = Double.parseDouble(result[8]);

//			System.out.println("cpu usage is " + cpuUsage);
		} catch (Exception e) {  
			e.printStackTrace();  
			System.exit(0);
		}  

		return cpuUsage;
	}



}
