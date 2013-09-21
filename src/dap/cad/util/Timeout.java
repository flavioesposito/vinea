/**
 * 
 */
package dap.cad.util;

import java.util.concurrent.Callable;

import rina.irm.IRM;



/**
 * test TimeOut class
 */
public class Timeout implements Callable<String> {
	private static final long TIMEOUT = 15*1000; //wait 15 seconds

	@Override
	public String call() throws Exception {
		Thread.sleep(TIMEOUT); // Just to demo a long running task of 15 seconds.

		return "Timeout!";
	}
}


/**
 * CADMessageWithTimeout 
 * @author Flavio Esposito
 * @deprecated 
 */
class CADMessageWithTimeout implements Callable<String> {

	private static final long TIMEOUT = 15*1000; //wait 5 seconds

	private IRM irm = null;
	private byte[] msg = null;
	public CADMessageWithTimeout(IRM irm, byte[] msg ) {
		this.irm = irm;
		this.msg = msg;
	}

	@Override
	public String call() throws Exception {

		// send msg here
		try {
			irm.sendCDAP(irm.getHandle("pnode1"), msg);
			Thread.sleep(TIMEOUT);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Message Timeout!";
	}
}