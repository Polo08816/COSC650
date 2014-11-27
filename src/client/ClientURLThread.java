package client;

import java.io.*;
import java.net.*;

public class ClientURLThread extends Thread{
	
	private URL link = null;
	
	/**
	 * This is the constructor for this class that supports multithreaded HTML retrieval from a URL.
	 * 
	 * @param urlFromClient
	 */
	public ClientURLThread(URL urlFromClient){
		super("ClientURLThread");
		this.link = urlFromClient;
		
	}
	

	public void run(){
		
		try {

			URLConnection myURLConnection = link.openConnection();
			myURLConnection.connect();
		} catch (MalformedURLException e) { 
			// new URL() failed
			// ...
		} catch (IOException e) {   
	    // openConnection() failed
	    // ...
		}
	

		
		BufferedReader in;
		String inputLine;
		try {
			in = new BufferedReader(
			new InputStreamReader(link.openStream()));
			
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
