package test.testHttp;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class client
{

	public static void main(String[] args)  

	{    
	
		 HttpClient httpclient = new DefaultHttpClient();

		 // Prepare a request object
		 HttpGet httpget = new HttpGet("http://localhost:55566/");

		 // Execute the request
		 try {
			HttpResponse response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		httpclient.getConnectionManager().shutdown();
	}    

}    



