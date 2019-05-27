package master.models;

public class Ticket {
    private final double stacke;
    private final String address;

    public Ticket(double stacke, String address) {
        this.stacke = stacke;
        this.address = address;
    }

    public double getStacke() {
        return stacke;
    }

    public String getAddress() {
        return address;
    }
}
