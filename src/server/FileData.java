package server;

import java.io.Serializable;


@SuppressWarnings("serial")
public class FileData implements Serializable {
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
