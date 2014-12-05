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
    public static void main (String args[]) throws IOException, ClassNotFoundException  {
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




