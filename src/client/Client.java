package client;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;


public class Client {
	
	private ArrayList<URL> listOfWebsites;
	private ArrayList<String> listOfLocalAddresses;
	//private Thread currentThread;
	
	//relative path for output file of HTML since HTML has trouble fitting into the console window
	public static final String webhtmlOutput = "output/websitehtml.txt";
	public static final String webhtmlFolder = "output";
		
	/**
	 * Constructor for Client.
	 */
	public Client(){	
		
		
	}	
	

	/**
	 * Reads a list of URLs from the command line.
	 * URL will be delimited by '\n'.
	 * '.' wil indicate that the user has finished entering all URLs
	 *  
	 */
	public void readListWebsites(){
		
		/*
		 * Initializes ArrayList<URL> here instead of constructor.
		 * Originally, this was in the constructor.
		 * 
		 * TODO - determine if we are going to run only one instance of
		 * Client for both URLs and file handling with the Server class.
		 */
		listOfWebsites = new ArrayList<URL>();
		
		System.out.println("\nEnter a list of websites:\n");
		
		BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
		 
	    String enterWebsites = null;
	 
	    try {
	    	
	    	while ((enterWebsites = br2.readLine()) != null){
	    		

	    		/*
	    		 * The user input of a list of URLs where each individual URL will be delimited by \n.  
	    		 * The implementation of this should be handle directly by .readlLine().
	    		 * 
	    		 * Therefore, we only need to account for the "." which indicates that the user
	    		 * has completed entering a list of URLs.
	    		 * 
	    		 * There is a special case where the user may have already completed entering the
	    		 * list of URLs but forgot to put "." at the end of the last entry.  In which case
	    		 * the last line of entry may just be a string with just ".".  We will account for
	    		 * this case by checking if it startsWith and endsWith ".".
	    		 * 
	    		 * Sequences of condition checks:
	    		 * 1.  	Check if starts with "."
	    		 * 		*End of List determined
	    		 * 2.  	Check if starts with "http" (
	    		 * 
	    		 */
	    		
	    		
	    		if (enterWebsites.startsWith(".")){
    				
    				//Test case: where input is just "."
    				//System.out.println("\nOnly .\n");
	    			break;
    				
    			}
	    		
	    		/*
    			 * The URL constructor requires that the URL be created with a protocol.  In our
    			 * case this is the "http://".  The assignment requires that the user only enter
    			 * the URL without the protocol.
    			 * 
    			 * I included logic to account for the way the user is expected to input the
    			 * "partial" URLs from the command line.
    			 */
	    		
	    		if (enterWebsites.startsWith("http") == false){
    				enterWebsites = "http://" + enterWebsites;
    			}
	    			    		
	    		if (enterWebsites.endsWith(".")){
	    			
	    			//removes the period at the end of the string
	    			enterWebsites = enterWebsites.substring(0, enterWebsites.length() -1);
	    			listOfWebsites.add(new URL(enterWebsites));
	    			
	    			System.out.println("\n *** End of List ***\n");
	    			break;
	    		} else {
	    			
	    			listOfWebsites.add(new URL(enterWebsites));

	    		}
	    		
	    		//resets the String to empty string
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
	    
	    removeDuplicateURLs();
	    
	    iterateListWebsites();
	    
	    return;	    
		
	}
	
	
	/**
	 * A function that primarily serves as a debugging utility to iterate through an ArrayList of URLs.
	 * 
	 * @return Returns -1 if size of ArrayList<URL> listOfWebsites is 0.
	 * @return Returns 1 if size of ArrayList<URL> listOfWebsites greater than 0.
	 */
	public int debugIterateListWebsites(){
		
		if (listOfWebsites == null || listOfWebsites.size() == 0){
			System.out.println("\nArrayList of websites has not been initialized or populated");
			return -1;
			
		}
				
		List<URL> x = listOfWebsites;
		for (URL y : x){
			System.out.println("toExternalForm(): " + y.toExternalForm());
			System.out.println("toString(): " + y.toString());
			try {
				System.out.println("toURI(): " + y.toURI() +"\n");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return 1;
		
	}
	
	/**
	 * Eliminates duplicate URLs if provided by user.
	 * 
	 * Creates a HashSet and adds all elements of the listOfWebsites ArrayList to
	 * that HashSet.
	 */
	private void removeDuplicateURLs(){
		
		HashSet<URL> hs = new HashSet<URL>();
		hs.addAll(listOfWebsites);
		listOfWebsites.clear();
		listOfWebsites.addAll(hs);
		
	}
	
	/**
	 * Iterates through an ArrayList and spawns a new thread for each URL.
	 * 
	 * @return Returns -1 if size of ArrayList<URL> listOfWebsites is 0.
	 * @return Returns 1 if size of ArrayList<URL> listOfWebsites greater than 0.
	 */
	public int iterateListWebsites(){
		
		
		if (listOfWebsites == null || listOfWebsites.size() == 0){
			System.out.println("\nArrayList of websites has not been initialized or populated");
			return -1;
			
		}
		
		/*
		 * Write output of HTP GET request to file.
		 */
		File webTxt = null;
		
		try {
			// createNewFile() will not create directories
			webTxt = new File(webhtmlFolder);
			webTxt.mkdirs();
			webTxt = new File(webhtmlOutput);
			webTxt.createNewFile(); 
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("\nCould not create new file.\n");
			e1.printStackTrace();
		}
		
		FileChannel fileChannel = null;
		try {
			fileChannel = new FileOutputStream(webTxt,true).getChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Path of output file: " + webTxt.getAbsolutePath() + "\n");
				
		List<URL> x = listOfWebsites;
		
		for (URL y : x){
			
			System.out.println("toExternalForm(): " + y.toExternalForm() + "\n");
			
			//TODO need to lock this statement
			new ClientURLThread(y, fileChannel).start();
			

		}		
		
		return 1;
		
	}
	
	
public void readListLocalAddresses(){
		
		/*
		 * Initializes ArrayList<URL> here instead of constructor.
		 * Originally, this was in the constructor.
		 * 
		 * TODO - determine if we are going to run only one instance of
		 * Client for both URLs and file handling with the Server class.
		 */
		listOfLocalAddresses = new ArrayList<String>();
		
		System.out.println("\nEnter a local file address and timeout value (ie. C:\\log.txt 1000):\n");
		
		BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in));
		 
	    String enterWebsites = null;
	 
	    try {
	    	
	    	enterWebsites = br2.readLine();
	    		

    		/*
    		 * The user input of a list of URLs where each individual URL will be delimited by \n.  
    		 * The implementation of this should be handle directly by .readlLine().
    		 * 
    		 * Therefore, we only need to account for the "." which indicates that the user
    		 * has completed entering a list of URLs.
    		 * 
    		 * There is a special case where the user may have already completed entering the
    		 * list of URLs but forgot to put "." at the end of the last entry.  In which case
    		 * the last line of entry may just be a string with just ".".  We will account for
    		 * this case by checking if it startsWith and endsWith ".".
    		 * 
    		 * Sequences of condition checks:
    		 * 1.  	Check if starts with "."
    		 * 		*End of List determined
    		 * 2.  	Check if starts with "http" (
    		 * 3.   Otherwise it is a local address
    		 */
    		
    		
    		
    		/*
			 * The URL constructor requires that the URL be created with a protocol.  In our
			 * case this is the "http://".  The assignment requires that the user only enter
			 * the URL without the protocol.
			 * 
			 * I included logic to account for the way the user is expected to input the
			 * "partial" URLs from the command line.
			 */
    		
    			
			
			listOfLocalAddresses.add(new String(enterWebsites));
    			
	    		
	    	
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
	    
	    listOfLocalAddresses.trimToSize();
	    
	    iterateListLocalAddresses();
	    return;	    
		
	}	



public int iterateListLocalAddresses(){
	
	
	if (listOfLocalAddresses == null || listOfLocalAddresses.size() == 0){
		System.out.println("\nArrayList of local Addresses has not been initialized or populated");
		return -1;
		
	}
	
	ClientLocalURLThread CLT = new ClientLocalURLThread(listOfLocalAddresses.get(0));
	CLT.start();
	return 1;
	
}	

	
	

}
