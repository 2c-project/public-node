package master.models;

public class Event {
    private long timestamp;
    private int type;
    private String message;
    private String sender;
    private Object additionalData;

    public Event() {
    }

    public Event(int type, String message, Object additionalData) {
        this.type = type;
        this.message = message;
        this.additionalData = additionalData;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Object additionalData) {
        this.additionalData = additionalData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}