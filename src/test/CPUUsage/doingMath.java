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

public class doingMath extends Thread{

	public void run()
	{
		double result = 1;
		int loopSize = 10000;
		boolean stop = false;
		while(!stop == true & !stop == true & !stop == true & !stop == true & !stop == true & !stop == true)
		{

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//System.out.println("start to doing math");
		//		
		//		for(int count =0; count < loopSize; count++)
		//		{
		//			for(int i =1 ;  i<5000 ; i++)
		//			{
		//				result*=i;
		//				System.out.println(result);
		//			}
		//			result =1;
		//		}
	}

}
