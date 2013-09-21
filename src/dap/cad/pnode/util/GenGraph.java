/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode.util;

import test.VN;
import java.util.Random;
/**
 * @author Flavio Esposito
 *
 */
public class GenGraph {

	private	int n;
	private	int m;

	public void GenGraph(){
		//		this.n=n;
		//		this.m=m;
	}
	
	public void GenGraph(int n, int m){
				this.n=n;
				this.m=m;
	}

	/**
	 * @return the n
	 */
	public int getN() {
		return n;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(int n) {
		this.n = n;
	}

	/**
	 * @return the m
	 */
	public int getM() {
		return m;
	}

	/**
	 * @param m the m to set
	 */
	public void setM(int m) {
		this.m = m;
	}

	public VN genRandomGraph(int n){

		VN randomVN= new VN(n);

		Random generator = new Random();

		while(isConnected(randomVN)){
			for (int i=0; i<n;i++)
			{
				for (int j=0; j<n;j++)
				{
					double x = generator.nextDouble();
					if (x>=0.5){
						randomVN.setElement(i, j, 1);
					}else{
						randomVN.setElement(i, j, 0);
					}
				}

			}
		}
		return randomVN;
	}


	public VN genClickGraph(int n){

		VN ClickGraph= new VN(n);


		for (int i=0; i<n;i++)
		{
			for (int j=0; j<n;j++)
			{	
				ClickGraph.setElement(i, j, 1);
			}
		}
		return ClickGraph;
	}


	public boolean isConnected(VN vn){

		int n = vn.getRows();
		int[][] prod = new int[n][n]; 

		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n-1; j++) {
				for(int k = 0; k < n; k++){
					if (i==j)
						continue;
					prod[i][j] += vn.getH()[i][k]*vn.getH()[k][j];
					if (prod[i][j] ==0) //there is no path bw i and j if the product of the adj returns zero
						return false;
				}
			}  
		}


		return true;

	}



}
