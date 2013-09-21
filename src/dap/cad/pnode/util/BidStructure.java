/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.pnode.util;

/**
 * bid data structure
 * @author flavioesposito
 * @version 1.0
 */
public class BidStructure {
	
	private double Uij = Double.MIN_VALUE;
	private int eta = -1;
	
	public BidStructure() {}
	
	public BidStructure(double Uij, int eta) {
		this.Uij = Uij;
		this.eta = eta;
	}

	/**
	 * @return the uij
	 */
	public double getUij() {
		return Uij;
	}

	/**
	 * @param uij the uij to set
	 */
	public void setUij(double uij) {
		Uij = uij;
	}

	/**
	 * @return the eta
	 */
	public int getEta() {
		return eta;
	}

	/**
	 * @param eta the eta to set
	 */
	public void setEta(int eta) {
		this.eta = eta;
	}
	
}
