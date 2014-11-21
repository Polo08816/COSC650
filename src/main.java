import java.net.*;
import java.io.*;


public class main {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		URL oracle = null;
		
		try {
		    oracle = new URL("http://oracle.com/");
		    URLConnection myURLConnection = oracle.openConnection();
		    myURLConnection.connect();
		} 
		catch (MalformedURLException e) { 
		    // new URL() failed
		    // ...
		} 
		catch (IOException e) {   
		    // openConnection() failed
		    // ...
		}
		
        BufferedReader in = new BufferedReader(
        new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();

	}

}
