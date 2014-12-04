package server;

import java.io.Serializable;


@SuppressWarnings("serial")
public class FileData implements Serializable {
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
}
