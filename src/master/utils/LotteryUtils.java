package master.utils;

import java.util.Random;

public class LotteryUtils {
    public static boolean isWinner(String address, long timestamp, long previousTimestamp) {
        long delay = timestamp - previousTimestamp;
        assert delay > 0;
        double value = new Random(address.hashCode() * timestamp).nextDouble();
        return value > (1 - (delay / ((double) 3 * 60 * 1000)));
    }
}
