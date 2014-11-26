package launcher;


import java.net.*;
import java.io.*;

import client.*;


public class main {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		System.out.println("Welcome to the Project Launcher...\n");
		
		System.out.println("Please selection from the following options...\n");
		
		System.out.println("1.  Client - List of Websites");
		System.out.println("2.  Client - Local host - File transfer\n");
		
		//  prompt the user to enter their choice
	    System.out.print("Enter your option: ");
	 
	    //  open up standard input
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	 
	    String optionChoice = null;
	 
	    //  read the option
	    try {
	    	optionChoice = br.readLine();
	    } catch (IOException ioe) {
	         System.out.println("IO error trying to read your input!");
	         System.exit(1);
	    }
	    
	    //option handling
	    
	    	//initialize Client and Server
	   
	    switch (optionChoice){
	    	
	    	case "1": //Client - List of Websites
	    		
	    		
	    		
	    		Client listOfWebsites = new Client();
	    		
	    		//reads a list of websites from command line
	    		listOfWebsites.readListWebsites();
	    		listOfWebsites.debugIterateListWebsites();
	    	
	    		break;
	    		
	    	case "2":
	    		
	    		
	    		break;
	    		
	    	default:
	    		
	    		System.out.println("Invalid selection");
	    		break;
	    
	    }
	    
	    
	    
//		//TODO will move this to Client
//		URL oracle = null;
//		
//		try {
//		    oracle = new URL("http://oracle.com/");
//		    URLConnection myURLConnection = oracle.openConnection();
//		    myURLConnection.connect();
//		} 
//		catch (MalformedURLException e) { 
//		    // new URL() failed
//		    // ...
//		} 
//		catch (IOException e) {   
//		    // openConnection() failed
//		    // ...
//		}
//		
//        BufferedReader in = new BufferedReader(
//        new InputStreamReader(oracle.openStream()));
//
//        String inputLine;
//        while ((inputLine = in.readLine()) != null)
//            System.out.println(inputLine);
//        in.close();

	    return;
	}

}
