package master.models;

import java.io.Serializable;

public class ShortTransaction implements Serializable {
    private String hash;
    private long time;

    public ShortTransaction(String hash, long time) {
        this.hash = hash;
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public long getTime() {
        return time;
    }
}
