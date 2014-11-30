package server;

import java.io.Serializable;

//@SuppressWarnings("serial")
public class FileRequest implements Serializable {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = -2863969401328505967L;
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
