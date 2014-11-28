package client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ClientURLThread extends Thread{
	
	private URL link = null;
	private FileChannel fileChannel = null;
	
	/**
	 * This is the constructor for this class that supports multithreaded HTML retrieval from a URL.
	 * 
	 * @param urlFromClient
	 */
	public ClientURLThread(URL urlFromClient, FileChannel fc){
		super("ClientURLThread");
		this.link = urlFromClient;
		this.fileChannel = fc;
		
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
			
			FileLock fileLock = fileChannel.lock();
			if(fileLock != null)
			{
				String outString = "\nSTART: "+ link.toExternalForm() + "\n";
				ByteBuffer byteBuffer = ByteBuffer.wrap(outString.getBytes());
				System.out.println(outString);
				fileChannel.write(byteBuffer);
			
				while ((inputLine = in.readLine()) != null){
				
					System.out.println(inputLine);
					outString = "\n" + inputLine + "\n";
					byteBuffer = ByteBuffer.wrap(outString.getBytes());
					fileChannel.write(byteBuffer);
				
				}	
			
				outString = "\nEND: "+ link.toExternalForm() + "\n";
				System.out.println(outString);
				byteBuffer = ByteBuffer.wrap(outString.getBytes());
				fileChannel.write(byteBuffer);
			}			
			fileLock.release();
			in.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
