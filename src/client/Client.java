/**
 * 
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

import launcher.*;

/**
 * @author 
 *
 */

/**
 * @author J14688
 *
 */
/**
 * @author J14688
 *
 */
public class Client {
	
	//contains list of websites - can this just be a List?
	private ArrayList<URL> listOfWebsites;
	
	
	/**
	 * Constructor for Client.
	 */
	public Client(){
		
		//initializes list to null
		listOfWebsites = new ArrayList();
		
	}	
	

	/**
	 * Reads a list of URLs from the command line.
	 * URL will be delimited by '\n'.
	 * '.' wil indicate that the user has finished entering all URLs
	 *  
	 */
	public void readListWebsites(){
		
		//read from command line
		
		System.out.println("\nEnter a list of websites:\n");
		
		BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
		 
	    String enterWebsites = null;
	 
	    try {
	    	while ((enterWebsites = br2.readLine()) != null){
	    		
	    		//delim by \n aka [enter] and end by '.'
	    			    		
	    		if (enterWebsites.endsWith(".")){
	    			enterWebsites = enterWebsites.substring(0, enterWebsites.length() -1);
	    			System.out.println(" *** End of List ***\n");
	    			break; //does it break out of the while loop?
	    		} else {
	    			if (enterWebsites.startsWith("http")){
	    				listOfWebsites.add(new URL(enterWebsites));
	    			} else {
	    				enterWebsites = "http://" + enterWebsites;
	    				listOfWebsites.add(new URL(enterWebsites));
	    			}
	    		}
	    		
	    		enterWebsites = "";
	    	}
	    	
	    } catch (MalformedURLException e){
	    	System.out.println("Malformed URL");
	    	System.exit(1);
	    } catch (IOException ioe) {
	         System.out.println("Error entering webistes.");
	         System.exit(1);
	    } finally {
	    	try {
				br2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    listOfWebsites.trimToSize();
	    
	    return;	    
		
	}
	
	
	/**
	 * A function that primarily serves as a debugging utility to iterate through an ArrayList of websites.
	 */
	public void debugIterateListWebsites(){
		
		if (listOfWebsites == null || listOfWebsites.size() == 0){
			System.out.println("\nArrayList of websites has not been initialized or populated");
			return;
			
		}
		
		Iterator<URL> x = listOfWebsites.iterator();
		
		while (x.hasNext()){
			System.out.println(x.toString());
		}
		
	}
	
	public void iterateListWebsites(){
		
		//iterates through ArrayList
			//spawns thread to do HTTP GET for each website
				//is the threading part going to be its own seaparate class? - https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServerThread.java
		
		
		/*
		 * 
		 * 
		 */
	}
	
	
	

}
