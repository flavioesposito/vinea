/**
 * 
 */
package test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author flavioesposito
 *
 */
public class TestCreateVN {

	/**
	 * 
	 */
	public TestCreateVN() {


		PrintWriter writer;
		try {
			writer = new PrintWriter("createVN.py", "UTF-8");
			writer.println("#!/usr/bin/python");

			writer.println("from mininet.net import Mininet");
			writer.println("from mininet.node import Controller");
			writer.println("from mininet.log import setLogLevel, info");
			writer.println("");
			writer.println("");
			writer.println("def create():");
			writer.println("    print culo");
			writer.println("");
			writer.println("if __name__ == '__main__':");
			writer.println("    setLogLevel( 'info' )");
			writer.println("    create()");



			writer.close();


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public static void main(String[] args) throws Exception {
		TestCreateVN tc = new TestCreateVN();


	}
}
