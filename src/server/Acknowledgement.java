package server;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Acknowledgement implements Serializable {
    private long seqNum;
     
    public Acknowledgement (long sn) {
        this.seqNum = sn;
    }
     
    public long getSeqNum() {
        return seqNum;
    }
}
