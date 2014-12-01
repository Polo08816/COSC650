package launcher;


import java.io.*;

import server.Server;
import client.*;


public class main {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		selection:
		while(true){
			System.out.println("Welcome to the Project Launcher...\n");
		
			System.out.println("Please selection from the following options...\n");
		
			System.out.println("1.  Client - List of Websites");
			System.out.println("2.  Client - Local host - File transfer");
			System.out.println("3.  Server - Local host - File transfer\n");
		
		
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
	    			//listOfWebsites.debugIterateListWebsites();
	    	
	    			break selection;
	    		
	    		case "2":
	    		
	    			Client listOfLocalAddresses = new Client();
	    		
	    			//reads a list of websites from command line
	    			listOfLocalAddresses.readListLocalAddresses();
	    		
	    			break selection;
	    		
	    		case "3": //Server -  - Local host - File transfer	    		
	    		
	    			Server.serve(args);
	    			//listOfWebsites.debugIterateListWebsites();
	    			
	    			break selection;
	    		
	    		default:
	    		
	    			System.out.println("Invalid selection");
	    			break;
	    
	    	}
	    }
	    
	    return;
	}

}
