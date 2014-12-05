package server;

/**
 * @author
 *
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Arrays;
import java.io.Serializable;

import client.Client;
import server.Acknowledgement;
import server.FileData;
import server.FileRequest;
import server.Server;
 
public class Server {
 
    static HashMap<String, byte[]> files = new HashMap<String, byte[]>();
 
    /**
     * Main method for the local file server. The server recieves a request from the local client to either upload or download a file.
     * It then transmits the file and sends ack packets
     * 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    
    class main {

    	public static void main(String[] args) throws Exception{
    		
    		selection:
    		while(true){
    			System.out.println("Welcome to the Project Launcher...\n");
    		
    			System.out.println("Please selection from the following options...\n");
    		
    			System.out.println("1.  Client - Local host - File transfer");
    			System.out.println("2.  Server - Local host - File transfer\n");
    		
    		
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
    	    	
    	    		case "1":
    	    		
    	    			Client listOfLocalAddresses = new Client();
    	    		
    	    			//reads a list of websites from command line
    	    			listOfLocalAddresses.readListLocalAddresses();
    	    		
    	    			break selection;
    	    		
    	    		case "2": //Server -  - Local host - File transfer	    		
    	    		
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
    
    public static void serve (String args[]) throws IOException, ClassNotFoundException  {
        int portNum = 12345; 
        int timeout = 2000; //in milliseconds
        DatagramSocket sock = new DatagramSocket(portNum);
        final int PACKET_SIZE = 1024;
        final int MAX_FILE_SIZE = 65536;
         
        System.out.println("FileServer Online");

        while(true) {

            byte[] rxBuff = new byte[PACKET_SIZE];
            byte[] txBuff = new byte[PACKET_SIZE];
     
            ByteArrayInputStream bais;
            ObjectInputStream ois;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(PACKET_SIZE); 
        	ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
        	oos.flush();
        	
            System.out.println("Waiting for Requests...");
             
            // Wait for requests
            DatagramPacket reqPacket = new DatagramPacket(rxBuff, rxBuff.length );
            sock.receive(reqPacket);    
            System.out.println("Request received... IP: " + reqPacket.getAddress());    
             
            bais = new ByteArrayInputStream(rxBuff);
            ois = new ObjectInputStream(bais);

            FileRequest fr = null;
            try {
            	fr = (FileRequest) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println(fr.filename + " " + fr.direction + "load requested..."); 
             
             
            // Send ack when request received
            Acknowledgement ack = new Acknowledgement(fr.getSeqNum());
            oos.writeObject(ack);
            oos.flush();
             
            txBuff = baos.toByteArray();
            DatagramPacket ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
            
            // Send Ack
            sock.setSoTimeout(timeout);
            boolean reqAcksent = false;
            while (reqAcksent == false) {
                try {
                    sock.send(ackPacket);
                    reqAcksent = true;
                    System.out.println(fr.getSeqNum() + " acknowledgement sent...");
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
            }
            sock.setSoTimeout(0);
            
            // If a file is downloaded
            if (fr.direction.equals("down")) {
                // Send data
                //wait for ack
            	Path path = Paths.get(fr.filename);
            	byte[] data = Files.readAllBytes(path);
            	int ln = data.length;
            	
            	System.out.println("File size = " + ln);
            	
            	// Throw exception if the file is too big
            	if (ln>MAX_FILE_SIZE){
            		throw new IOException("File size too large");
            	}
            	// Determine number of packets needed
            	int completePackets = ln/PACKET_SIZE;
            	
            	// Send data for each packet that is 1024
            	for (int i = 0; i < completePackets; i++ ){
            		int start = (PACKET_SIZE * i);
            		int end = 1024 * (i+1);
            		
            		byte[] xferData = Arrays.copyOfRange(data, start, end);
            				
	                FileData fdToClient = new FileData(PACKET_SIZE, xferData);
	                
	                //inserting and setting total file size into the FileData packet
	                fdToClient.setTotalFileSize(ln);
	                fdToClient.setPacketSeqNum(i);
	                fdToClient.setTotalPackets(completePackets);
	                fdToClient.setStart(start);
	                fdToClient.setEnd(end);
	                
	                //System.out.println("fdToClient:" + fdToClient.getData());
	                oos.writeObject(fdToClient);
	                oos.flush();
	                 
	                txBuff = baos.toByteArray();
	                DatagramPacket dataToClient = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
	                 
	                sock.send(dataToClient);
	                System.out.println(fr.filename + " sent...");
	                
	                // Make sure the ack is recieved
	                sock.setSoTimeout(timeout);
	                boolean ackRecieved = false;
	                int kill = 0;
	                while (ackRecieved == false){
		                try{
		                // Receive ack when data sent
		                ackPacket = new DatagramPacket(rxBuff, rxBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
		                sock.receive(ackPacket);
		                 
		                ack = (Acknowledgement) ois.readObject();
		                System.out.println(ack.getSeqNum() + " Ack received...");
		                ackRecieved = true;
		                }
		                catch (SocketTimeoutException e){
		                	if (kill == 5){
		                		throw new IOException("Transmission Failed");
		                	}else kill = kill + 1;
		                	System.out.println("Transmission Error, retrying packet");
		                	
		                	FileData retryfdToClient = new FileData(PACKET_SIZE, xferData);
			                //System.out.println("fdToClient:" + fdToClient.getData());
			                oos.writeObject(retryfdToClient);
			                oos.flush();
			                 
			                txBuff = baos.toByteArray();
			                DatagramPacket retryDataToClient = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
			                 
			                sock.send(retryDataToClient);
			                System.out.println(fr.filename + " sent...");
		                }
	                }
	                sock.setSoTimeout(0);
            	}
            	
            	// Send final packet
            	if (ln - (completePackets*PACKET_SIZE) > 0){
            		int start = completePackets*PACKET_SIZE;
            		int len = (ln - (completePackets*PACKET_SIZE));
            		int end = start + len;
            		
            		byte[] xferData = Arrays.copyOfRange(data, start, end);
            				
	                FileData fdToClient = new FileData(len, xferData);
	                //System.out.println("fdToClient:" + fdToClient.getData());
	                
	                //inserting and setting total file size, sequence number, and total number of packets into the FileData packet
	                fdToClient.setTotalFileSize(ln);
	                fdToClient.setPacketSeqNum(completePackets);
	                fdToClient.setTotalPackets(completePackets);
	                fdToClient.setStart(start);
	                
	                oos.writeObject(fdToClient);
	                oos.flush();
	                 
	                txBuff = baos.toByteArray();
	                DatagramPacket dataToClient = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
	                 
	                sock.send(dataToClient);
	                System.out.println(fr.filename + " sent...");
	 
	                // Receive ack when data sent
	                ackPacket = new DatagramPacket(rxBuff, rxBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
	                sock.receive(ackPacket);
	                 
	                ack = (Acknowledgement) ois.readObject();
	                System.out.println(ack.getSeqNum() + " Ack received...");
	               
	                // Make sure the ack is recieved
	                sock.setSoTimeout(timeout);
	                boolean ackRecieved = false;
	                int kill = 0;
	                while (ackRecieved == false){
		                try{
		                // Receive ack when data sent
		                ackPacket = new DatagramPacket(rxBuff, rxBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
		                sock.receive(ackPacket);
		                 
		                ack = (Acknowledgement) ois.readObject();
		                System.out.println(ack.getSeqNum() + " Ack received...");
		                break;
		                }
		                catch (SocketTimeoutException e){
		                	if (kill == 5){
		                		throw new IOException("Transmission Failed");
		                	}else kill = kill + 1;
		                	System.out.println("Transmission Error, retrying packet");
		                	
		                	FileData retryfdToClient = new FileData(len, xferData);
			                //System.out.println("fdToClient:" + fdToClient.getData());
			                oos.writeObject(retryfdToClient);
			                oos.flush();
			                 
			                txBuff = baos.toByteArray();
			                DatagramPacket retryDataToClient = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
			                 
			                sock.send(retryDataToClient);
			                System.out.println(fr.filename + " sent...");
		                }
	                }
	                sock.setSoTimeout(0);
            	}
            }
             
            System.out.println("Files on server: " + Server.files.toString());
             
        }
    }
}

@SuppressWarnings("serial")
class FileDataClass implements Serializable {
    private long seqNum;
    private long ackNum;
    private int chunkSize;
    private byte[] data;
    
    /*
     * Provides total size of file in all FileData packets.
     * Implementation is easier to adapt to what we currently have
     * and bandwidth waste is not a concern for local host at the
     * moment.
     */
    private int totalFileSize;
    private int packetSeqNum;
    private int totalPackets;
    private int byteStart;
    private int byteEnd;
     
    public void FileData (int chunk, byte[] data) {
        this.seqNum = System.currentTimeMillis();
        this.ackNum = 0; 
        this.chunkSize = chunk;
        this.data = data;
    }
     
    public long getSeqNum () {
        return seqNum;
    }
     
    public void setSeqNum (long sn) {
        this.seqNum = sn;
    }
     
    public long getAckNum () {
        return ackNum;
    }
     
    public void setAckNum (long an) {
        ackNum = an;
    }
     
    public int getChunkSize () {
        return chunkSize;
    }
     
    public void setChunkSize (int cs) {
        chunkSize = cs;
    }
     
    public byte[] getData () {
        return data;
    }
     
    public void setData (byte[] d) {
        this.data = d;
    }

	/**
	 * Returns the total size of the file that this packet is a part of.
	 * 
	 * @return
	 */
	public int getTotalFileSize() {
		return totalFileSize;
	}

	/**
	 * Allows the server to set the total size of the file that this packet is a part of.
	 * 
	 * @param totalFileSize
	 */
	public void setTotalFileSize(int totalFileSize) {
		this.totalFileSize = totalFileSize;
	}
	
	public int getPacketSeqNum() {
		return packetSeqNum;
	}
	
	public void setPacketSeqNum(int packetSeqNum) {
		this.packetSeqNum = packetSeqNum;
	}
	
	public int getTotalPackets() {
		return totalPackets;
	}
	
	public void setTotalPackets(int totalPackets) {
		this.totalPackets = totalPackets;
	}
	
	public int getStart() {
		return byteStart;
	}
	
	public void setStart(int byteStart) {
		this.byteStart = byteStart;
	}
	
	public int getEnd() {
		return byteEnd;
	}
	
	public void setEnd(int byteEnd) {
		this.byteEnd = byteEnd;
	}
}

class FileRequest implements Serializable {
	 
    /**
	 * 
	 */
	private long seqNum;
    private long ackNum;
    public String filename; //file being transferred
    public String direction; //"up" or "down" are valid values for this
 
     
    public FileRequest (String fn, String dir) {
        this.seqNum = System.currentTimeMillis();
        this.ackNum = 0;
        this.filename = fn;
        this.direction = dir;
    }
 
    public long getSeqNum () {
        return seqNum;
    }
     
    public void setSeqNum (long sn) {
        this.seqNum = sn;
    }
     
    public long getAckNum () {
        return ackNum;
    }
     
    public void setAckNum (long an) {
        ackNum = an;
    }
 
}

class Acknowledgement implements Serializable {
    private long seqNum;
     
    public Acknowledgement (long sn) {
        this.seqNum = sn;
    }
     
    public long getSeqNum() {
        return seqNum;
    }
}

class ClientLocalURLThread extends Thread{
	
	private String FilePath;
	private static String outputPath = "output/fromServer-";
	public static final String outputFolder = "output";
	private static int portNum = 12345; 
    private static DatagramSocket sock;
    private static InetAddress server;
    private static int timeout = 2000; //in milliseconds
    public final static int PACKET_SIZE = 65536;
     
    private static Acknowledgement ack;
     
    private static ByteArrayInputStream bais;
    private static ObjectInputStream ois;
    private static ByteArrayOutputStream baos;
    private static ObjectOutputStream oos;
     
    static boolean validArgs = false;
     
    /**
     * (One sentence summary describing receive).
     * 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void recieve(String args[]) throws IOException, ClassNotFoundException {
        
    	File f = new File(outputFolder);
    	
    	try {
			//Create directories
	    	if(f.exists() && !f.isDirectory()) {
	    		System.out.println("Output path exists");
	    	}else{
	    		System.out.println("Output path does not exist. Creating.");
	    		f.mkdirs();
	    	}
    	} catch (SecurityException e1){
	    	System.out.println("\nCould not create directory.\n");
			e1.printStackTrace();
	    }
    	
    	//String direction = up/down
        String direction = args[0];
        String filename = args[1];
        int blocksize = Integer.parseInt(args[2]); 
        
        // Ensure arguments are valid 
        if ( direction.equals("down") && blocksize > 0 ) {
             validArgs = true;          
        } else {
             validArgs = false; 
             System.out.println("Args Error: invalid direction or negative blocksize");
        }
         
        // If argurments are valid create connection settings
        if (validArgs) {
            server = InetAddress.getByName("localhost");
            sock = new DatagramSocket();
            sock.setSoTimeout(timeout);
            System.out.println("Connection to server established");
             
            byte[] rxBuff = new byte[PACKET_SIZE];  //holds datagram packets
            byte[] txBuff = new byte[PACKET_SIZE];
             
             
            /**Create File request, convert to byte[] and fill txBuff**/
            baos = new ByteArrayOutputStream(PACKET_SIZE);
            oos = new ObjectOutputStream(new BufferedOutputStream(baos));
            FileRequest fr = new FileRequest(filename, direction);
            oos.writeObject(fr);
            oos.flush();
            txBuff = baos.toByteArray();
             
            DatagramPacket reqPacket = new DatagramPacket(txBuff, txBuff.length, server, portNum ); //holds request packets
            DatagramPacket ackPacket = new DatagramPacket(rxBuff, rxBuff.length, server, portNum ); //holds acknowledgement packets
             
            /**Send request, if reply not received within 2 seconds, resend**/
            boolean requestSent = false;
            while (requestSent == false) {
                try {
                    sock.send(reqPacket);
                    System.out.println(fr.getSeqNum() + "PID: Request sent...");
                     
                    sock.receive(ackPacket);
                     
                    bais = new ByteArrayInputStream(rxBuff);
                    ois = new ObjectInputStream(bais);
                     
                    ack = (Acknowledgement) ois.readObject();
                    requestSent = true;
                    System.out.println(ack.getSeqNum() + "PID: Request Ack received...");
 
                } catch (SocketTimeoutException e) {
                    //e.printStackTrace();
                	System.out.println("Request timedout! " + timeout + "ms");
                }
            }
            
            // Download File
            if (direction.equals("down")) {
            	
            	int numPackets;
            	int totalSize;
            	int currentPacket;
            	int dataStart;
            	
            	// Create the file and place it in the directory listed
                File tmp = new File (filename);
                //File file = new File ("D:\\COSC650-" + tmp.getName());
                
                //create file in relative path - this file will be placed in the "output" folder
                File file = new File(outputPath +tmp.getName());
                  
                
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                 
                // Receive the initial packet and create an object with the information
                DatagramPacket initialDataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                sock.receive(initialDataFromClient);
                FileData fd = (FileData) ois.readObject();

                // Extract file data from packet
                totalSize = fd.getTotalFileSize(); // Total size of the file it is sending
                numPackets = fd.getTotalPackets(); // Total number of packets to be sent
                int[] recievedPackets= new int[numPackets+1]; // An array keeping track of what packets have arrived
                currentPacket = fd.getPacketSeqNum(); // A tracker for the current packet
                dataStart = fd.getStart(); // The starting byte number of the current packet
                byte[] fileData = new byte[totalSize]; // The new byte array that will accept the incoming packets
                System.arraycopy(fd.getData(), 0, fileData, dataStart, fd.getData().length);  // Copy the incoming packet into the byte array
                recievedPackets[currentPacket]=currentPacket; // Acknowledge that the packet was received.
                
                // Acknowledge that the packet was received
                System.out.println("Packet received.");
                
                //Create ack and write to buffer
                ack = new Acknowledgement(fd.getSeqNum());
                oos.writeObject(ack);
                oos.flush();
                 
                //make packet with buffer
                txBuff = baos.toByteArray();
                ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                 
                //send packet
                sock.send(ackPacket);
                System.out.println(fr.getSeqNum() + " acknowledgement sent..."); 
                
                // Loop through each additional packet
                for (int i = 1; i < numPackets+1;i++){
                	
                	// Receive the data and create an object with the information
                    DatagramPacket moreDataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                    sock.receive(moreDataFromClient);
                    FileData mfd = (FileData) ois.readObject();
                    
                    // Update the current Packet Number
                    currentPacket = mfd.getPacketSeqNum();
                    
                    // Check if the packet is where it should be. 
                    if (currentPacket != i){
                    	if (currentPacket < i){   // If the packet is 1 behind... 
                    		if (recievedPackets[currentPacket] != 0){        // Check to see if the value was already written. If it was ack it and notify the user.
                    			System.out.println("packet has already been recieved.");
                    			
                                /***Send ack when data received***/
                                //Create ack and write to buffer
                                ack = new Acknowledgement(fd.getSeqNum());
                                oos.writeObject(ack);
                                oos.flush();
                                 
                                //make packet with buffer
                                txBuff = baos.toByteArray();
                                ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                                 
                                //send packet
                                sock.send(ackPacket);
                                System.out.println(fr.getSeqNum() + " acknowledgement sent..."); 
                    			
                    		}
                    		else if (recievedPackets[currentPacket] == 0 && recievedPackets[currentPacket+1] == 0){   // Otherwise update the data and ack the server
                    			
                                dataStart = mfd.getStart();
                                System.arraycopy(mfd.getData(), 0, fileData, mfd.getStart(), mfd.getData().length);
                                
                                System.out.println("Packet received.");
                                
                                /***Send ack when data received***/
                                //Create ack and write to buffer
                                ack = new Acknowledgement(fd.getSeqNum());
                                oos.writeObject(ack);
                                oos.flush();
                                 
                                //make packet with buffer
                                txBuff = baos.toByteArray();
                                ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                                 
                                //send packet
                                sock.send(ackPacket);
                                System.out.println(fr.getSeqNum() + " acknowledgement sent..."); 
                                
                    		}
                    	}
                    	else;
                    }
                    else{
                    	// If the current packet already has a value. Notify the user at the console and ack the server.
                    	if (recievedPackets[currentPacket] != 0){  
                			System.out.println("packet has already been recieved.");
                			
                            /***Send ack when data received***/
                            //Create ack and write to buffer
                            ack = new Acknowledgement(fd.getSeqNum());
                            oos.writeObject(ack);
                            oos.flush();
                             
                            //make packet with buffer
                            txBuff = baos.toByteArray();
                            ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                             
                            //send packet
                            sock.send(ackPacket);
                            System.out.println(fr.getSeqNum() + " acknowledgement sent..."); 	
                		}
                    	else{
                    		// Otherwise accept the data
                    		dataStart = mfd.getStart();
                            System.arraycopy(mfd.getData(), 0, fileData, mfd.getStart(), mfd.getData().length);
                            
                            System.out.println("Packet received.");
                            
                            /***Send ack when data received***/
                            //Create ack and write to buffer
                            ack = new Acknowledgement(fd.getSeqNum());
                            oos.writeObject(ack);
                            oos.flush();
                             
                            //make packet with buffer
                            txBuff = baos.toByteArray();
                            ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                             
                            //send packet
                            sock.send(ackPacket);
                            System.out.println(fr.getSeqNum() + " acknowledgement sent..."); 
                    	}
                    }
                }
                
                
                // Check to see if the size of the created file matches the length sent by the server
                // If not throw an IO exception and notify the user.
                if (totalSize != fileData.length){
                	bos.close();
                	throw new IOException("Files do not match. Please retry.");
                }
                
                // Print the file length
                System.out.println("File size: " + fileData.length);
                
	            try {
	                bos.write(fileData,0,fileData.length);
	                bos.close();
	            } catch (IOException e) {
	                System.out.println(e.getMessage());
	                bos.close();
	                file.delete();
	            }
                
                // Print file data to console
                String str = new String(fileData, "UTF-8");
                System.out.println("file content:");
                System.out.println(str);                
                System.out.println(filename + " file received.");
                System.out.println("saved to " + file.getAbsolutePath());
                         
                /***Send ack when data received***/
                //Create ack and write to buffer
                ack = new Acknowledgement(fd.getSeqNum());
                oos.writeObject(ack);
                oos.flush();
                 
                //make packet with buffer
                txBuff = baos.toByteArray();
                ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                 
                //send packet
                sock.send(ackPacket);
                System.out.println(fr.getSeqNum() + " acknowledgement sent...");    
            }
        }   
        sock.close();
    }
	/**
	 * This is the constructor for this class that supports multithreaded File retrieval from a URL.
	 * 
	 * @param urlFromClient
	 */
	public ClientLocalURLThread(String FilePath){
		super("ClientLocalURLThread");
		
		String str[] = FilePath.split("\\s+");
		this.FilePath = str[0];
		if((str.length > 1)&&(str[1].length() > 0))
		{
			this.timeout =  Integer.valueOf(str[1]);
		}
		if(this.timeout <= 0)
			this.timeout = 2000;
	}
	

	public void run(){
		String args[] = {"down", FilePath, "65535"};
		try {
			recieve(args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}




