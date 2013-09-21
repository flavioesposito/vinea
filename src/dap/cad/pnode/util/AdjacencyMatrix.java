/**
 * 
 */
package dap.cad.pnode.util;

import java.util.LinkedHashMap;

/**
 * @author flavioesposito
 *
 */
public class AdjacencyMatrix {

	private LinkedHashMap<Integer, Integer> adjMatrix = null;
	
	
	public AdjacencyMatrix() {}
	/**
	 * 
	 * @param adjMatrix
	 */
	public AdjacencyMatrix(LinkedHashMap<Integer, Integer> adjMatrix) {
		this.adjMatrix = adjMatrix;
	}
}
