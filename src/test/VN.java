/**
 * 
 */
package test;

/**
 * @author Flavio Esposito
 * testing class to acquire a VN from adj matrix
 *
 */
public class VN {
	/**
	 * adj matrix
	 */
	private int[][] H = null;
	private int rows = -1;

	public VN(int n){//adj matrices are always square
		this.H = new int[n][n];
		this.rows = n;
	}

	

	/**
	 * 
	 * @param row
	 * @param column
	 */
	public void setElement(int row, int column, int value){
		this.H[row][column] = value;
	}
	/**
	 * 
	 * @param row
	 * @param column
	 * @return element
	 */
	public int getElement(int row, int column){
		return this.H[row][column];
	}



	/**
	 * @return the h
	 */
	public int[][] getH() {
		return H;
	}



	/**
	 * @param h the h to set
	 */
	public void setH(int[][] h) {
		H = h;
	}



	/**
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}



	/**
	 * @param rows the rows to set
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}


	

	
	
	
}
