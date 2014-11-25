/**
 * 
 */
package client;

import java.net.*;
import java.util.*;

import launcher.*;

/**
 * @author 
 *
 */

public class Client {
	
	//contains list of websites - can this just be a List?
	private ArrayList<URL> listOfWebsites;
	
	public Client(){
		
		//initializes list to null
		listOfWebsites = null;
		
	}
	
	
	/*
	 * Reads a list of website URLs from user
	 * 
	 * Adds 
	 */
	public void readListWebsites(){
		
		//read from command line
			//delim by \n aka [enter] and end by '.'
			//add each website to ArrayList
		
		//calls function to iterate over ArrayList
		
	}
	
	public void iterateListWebsites(){
		
		//iterates through ArrayList
			//spawns thread to do HTTP GET for each website
				//is the threading part going to be its own seaparate class? - https://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServerThread.java
	}
	
	
	

}
