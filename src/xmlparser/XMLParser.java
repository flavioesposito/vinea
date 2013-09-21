package xmlparser;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import com.sun.org.apache.xerces.internal.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.xml.internal.bind.v2.model.core.Element;


import org.w3c.dom.*;


/**
 * parse XML files
 * @author flavioesposito
 *
 */
public class XMLParser {

	private Document dom = null;
	
	private LinkedList<Employee> myEmpls = new LinkedList<Employee>();

	
	public XMLParser(String fileToParse) {
		
		parseXmlFile(fileToParse);
		parseDocument();
		printData();
	}
	
	/**
	 * parseXmlFile
	 */
	private void parseXmlFile(String XMLfile){
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(XMLfile);
			org.w3c.dom.Element e = dom.getElementById("b");
			
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private void parseDocument(){
		//get the root element
		org.w3c.dom.Element docEle = dom.getDocumentElement();
		

		//get a nodelist of  elements
		NodeList nl = docEle.getElementsByTagName("Employee");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {

				//get the employee element
				Element el = (Element) nl.item(i);

				//get the Employee object
				Employee e = getEmployee(el);

				//add it to list
				myEmpls .add(e);
			}
		}
	}
	
	
	/**
	 * I take an employee element and read the values in, create
	 * an Employee object and return it
	 */
	private Employee getEmployee(Element empEl) {

		//for each <employee> element get text or int values of
		//name ,id, age and name
		String name = getTextValue(empEl,"Name");
		int id = getIntValue(empEl,"Id");
		int age = getIntValue(empEl,"Age");

		String type = (String) ((DocumentBuilderFactory) empEl).getAttribute("type");

		//Create a new Employee with the value read from the xml nodes
		Employee e = new Employee(name,id,age,type);

		return e;
	}


	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 */
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ((org.w3c.dom.Element) ele).getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = ((Node) el).getFirstChild().getNodeValue();
		}

		return textVal;
	}


	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}
	
	/**
	 * debug print Data
	 */
	private void printData(){

		System.out.println("No of Employees '" + myEmpls.size() + "'.");

		Iterator it = myEmpls.iterator();
		while(it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}
}//end of class
