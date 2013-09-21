package test;
import java.io.*;  

import rina.rib.impl.RIBImpl;

public class TestExec {  
	
	RIBImpl rib = new RIBImpl();
	
	public TestExec() {
		
		
    	mininetCommand("sudo killall ovs-controller");
		mininetCommand("sudo mn -c");
		//mininetCommand("sudo ./simple.py 4");
		//String command = "script -c \"sudo ./simple.py 2\" mininetLog.txt";
		String command = "sudo ./simple.py 2";
//		System.out.println(command);
		mininetCommand(command);
		
	}
	
    public static void main(String[] args) {  
    	
    	TestExec te = new TestExec();
		
    }
    
    public void mininetCommand(String command) {
        try {  
            Process p = Runtime.getRuntime().exec(command);  
            BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                this.rib.RIBlog.infoLog(line);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
} 