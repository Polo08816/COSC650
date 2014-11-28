package client;

import java.io.*;
import java.net.*;

public class ClientURLThread extends Thread{
	
	private URL link = null;
	private FileWriter fileWriter= null;
	
	/**
	 * This is the constructor for this class that supports multithreaded HTML retrieval from a URL.
	 * 
	 * @param urlFromClient
	 */
	public ClientURLThread(URL urlFromClient, FileWriter fw){
		super("ClientURLThread");
		this.link = urlFromClient;
		this.fileWriter = fw;
		
	}
	

	public void run(){
		
		try {

			URLConnection myURLConnection = link.openConnection();
			//myURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
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
			in = new BufferedReader(new InputStreamReader(link.openStream()));
			
			System.out.println("\nSTART: "+ link.toExternalForm() + "\n");
			fileWriter.write("\nSTART: "+ link.toExternalForm() + "\n");
			while ((inputLine = in.readLine()) != null){
				System.out.println(inputLine);
				fileWriter.write("\n" + inputLine + "\n");
			}	
			System.out.println("\nEND: "+ link.toExternalForm() + "\n");
			fileWriter.write("\\nEND: "+ link.toExternalForm() + "\n");
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
