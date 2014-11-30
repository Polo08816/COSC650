package client;

import java.io.*;
import java.net.*;

public class ClientLocalURLThread extends Thread{
	
	
	public static class Acknowledgement implements Serializable {
	    private long seqNum;
	     
	    public Acknowledgement (long sn) {
	        this.seqNum = sn;
	    }
	     
	    public long getSeqNum() {
	        return seqNum;
	    }
	}
	
	public static class FileData implements Serializable {
	    private long seqNum;
	    private long ackNum;
	    private int chunkSize;
	    private byte[] data;
	     
	    public FileData (int chunk, byte[] data) {
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
	}
	
	public static class FileRequest implements Serializable {
		 
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
	
	
	
	private static int portNum = 12345; 
    private static DatagramSocket sock;
    private static InetAddress server;
    static int timeout = 2000; //in milliseconds
    public final static int PACKET_SIZE = 1024;
     
    private static Acknowledgement ack;
     
    private static ByteArrayInputStream bais;
    private static ObjectInputStream ois;
    private static ByteArrayOutputStream baos;
    private static ObjectOutputStream oos;
     
    static boolean validArgs = false;
     
    public static void main(String args[]) throws IOException, ClassNotFoundException {
         
        //String direction = "up";
        //String direction = "down";
        //String filename = "test1.txt";
        //int blocksize = 8;

        String direction = args[0];
        String filename = args[1];
        int blocksize = Integer.parseInt(args[2]); 
        
        /******************
        String direction = args[0];
        String filename = args[1];
        int blocksize = Integer.parseInt(args[2]); 
        *******************/
         
        if ( direction.equals("up") && (blocksize > 0) ) {
            File file = new File (filename);
            if (!file.exists()) {
                System.out.println("Args Error: file does not exist");
            }
            else if (blocksize <= file.length()) {
                 validArgs = true;
            } else {
                 System.out.println("Args Error: blocksize (" + blocksize + ") greater than filesize (" + file.length() + ")");             
            }
        } else if ( direction.equals("down") && blocksize > 0 ) {
             validArgs = true;          
        } else {
             validArgs = false; 
             System.out.println("Args Error: invalid direction or negative blocksize");
        }
         
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
                    e.printStackTrace();
                }
            }
 
            if (direction.equals("up")) {
                byte[] fileByteArray = new byte[blocksize];
                try {
                    FileInputStream fis = new FileInputStream(filename);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(fileByteArray,0,fileByteArray.length);
                } catch (FileNotFoundException e) {
                    System.out.println("Client Error: File not found");
                } catch (IOException e) {
                    System.out.println(e.getMessage());         
                }
                 
                FileData fd = new FileData(blocksize, fileByteArray);
                oos.writeObject(fd);
                oos.flush();
                txBuff = baos.toByteArray();
 
                /**Send data, if reply not received within 2 seconds, resend**/
                boolean dataSent = false;
                while (dataSent == false) {
                    try {
                        sock.send(fdPacket);
                        System.out.println(fd.getSeqNum() + "PID: " + filename + " sent...");
                         
                        sock.receive(ackPacket);
         
                        bais = new ByteArrayInputStream(rxBuff);
                        ois = new ObjectInputStream(bais);                      
                        ack = (Acknowledgement) ois.readObject();
                        dataSent = true;
                        System.out.println(ack.getSeqNum() + "PID: Data Ack received...");
 
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (direction.equals("down")) {
 
                File file = new File (filename);    
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                 
                /***Receive data***/
                DatagramPacket dataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                 
                sock.receive(dataFromClient);
                FileData fd = (FileData) ois.readObject();
                 
                try {
                    bos.write(fd.getData(),0,fd.getData().length);
                    bos.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    bos.close();
                    file.delete();
                }
 
                System.out.println(filename + " downloaded...");
                         
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
	public ClientLocalURLThread(URL urlFromClient, FileWriter fw){
		super("ClientLocalURLThread");
		//this.link = urlFromClient;
		//this.fileWriter = fw;
		
	}
	

	public void run(){
		String args[] = {"down", "received.txt", "65535"};
		try {
			main(args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
