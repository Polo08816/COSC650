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
 
public class Server {
 
    static HashMap<String, byte[]> files = new HashMap<String, byte[]>();
 
    public static void serve (String args[]) throws IOException, ClassNotFoundException  {
        int portNum = 12345;
        int timeout = 2000; //in milliseconds
        DatagramSocket sock = new DatagramSocket(portNum);
        final int PACKET_SIZE = 65536;
         
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
             
             
            // Send ack when req received
            Acknowledgement ack = new Acknowledgement(fr.getSeqNum());
            oos.writeObject(ack);
            oos.flush();
             
            txBuff = baos.toByteArray();
            DatagramPacket ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
             
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
     
            if (fr.direction.equals("up")) {
                //Receive data
                DatagramPacket dataFromClient = new DatagramPacket(rxBuff, rxBuff.length );
                 
                sock.receive(dataFromClient);
                try {
                    FileData fd = (FileData) ois.readObject();
                    files.put(fr.filename, fd.getData());
                    System.out.println(fr.filename + " uploaded...");
                } catch (StreamCorruptedException e) {
                } finally {// Send ack when data received
                    ack = new Acknowledgement(1111);//fd.getSeqNum());
                    oos.writeObject(ack);
                    oos.flush();
                     
                    txBuff = baos.toByteArray();
                    ackPacket = new DatagramPacket(txBuff, txBuff.length, reqPacket.getAddress(), reqPacket.getPort() );
                     
                    sock.send(ackPacket);
                    System.out.println(/*fd.getSeqNum() +*/ " acknowledgement sent...");
                }
 
     
            }
            else if (fr.direction.equals("down")) {
                // Send data
                //wait for ack
            	Path path = Paths.get(fr.filename);
            	byte[] data = Files.readAllBytes(path);
            	
                FileData fdToClient = new FileData(PACKET_SIZE, data);
                //System.out.println("fdToClient:" + fdToClient.getData());
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
                 
            }
             
            System.out.println("Files on server: " + Server.files.toString());
             
        }
    }
}