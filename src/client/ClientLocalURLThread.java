package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import server.*;

public class ClientLocalURLThread extends Thread{
	
	private String FilePath;
	private static String outputPath = "output/fromServer-";
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
            DatagramPacket fdPacket = new DatagramPacket(txBuff, txBuff.length, server, portNum );  //holds file data packets
             
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
            	int dataEnd;
            	
            	
            	// Create the file and place it in the directory listed
                File tmp = new File (filename);
                //File file = new File ("D:\\COSC650-" + tmp.getName());
                
                //create file in relative path - this file will be placed in the "output" folder
                File file = new File(outputPath +tmp.getName());
                  
                
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                 
                /***Receive data***/
                DatagramPacket initialDataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                
                // Recieve the data and create an object with the information
                sock.receive(initialDataFromClient);
                
                FileData fd = (FileData) ois.readObject();
                
                totalSize = fd.getTotalFileSize();
                numPackets = fd.getTotalPackets();
                int[] recievedPackets= new int[numPackets];
                currentPacket = fd.getPacketSeqNum();
                dataStart = fd.getStart();
                dataEnd = fd.getEnd();
                byte[] fileData = new byte[totalSize];
                System.arraycopy(fd.getData(), 0, fileData, dataStart, fd.getData().length);
                recievedPackets[currentPacket-1]=currentPacket;
                
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
                
                for (int i = 2; i <= numPackets;i++){
                	
                	/***Receive data***/
                    DatagramPacket moreDataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                    
                    // Recieve the data and create an object with the information
                    sock.receive(moreDataFromClient);
                    
                    FileData mfd = (FileData) ois.readObject();
                    
                    currentPacket = mfd.getPacketSeqNum();
                    
                    if (currentPacket != i){
                    	if (currentPacket < i){
                    		if (recievedPackets[currentPacket-1] != 0){
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
                    		else if (recievedPackets[currentPacket-1] == 0 && recievedPackets[currentPacket+1] == 0){
                    			
                                dataStart = mfd.getStart();
                                dataEnd = mfd.getEnd();
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
                    	if (recievedPackets[currentPacket-1] != 0){
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
                    		dataStart = mfd.getStart();
                            dataEnd = mfd.getEnd();
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
                
                
                // Print file data to console
                if (totalSize != fileData.length){
                	bos.close();
                	throw new IOException("Files do not match. Please retry.");
                }
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
