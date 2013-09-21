/**
 * 
 */
package test.testSE;

import vinea.sp.SliceProvider;

/**
 * @author flavio
 *
 */
public class testSliceProvider {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SliceProvider sp = new SliceProvider("sp", "idd");
		//start it to listen for response
		sp.start();

	}

}
