package master.utils;

import org.junit.Test;

public class LotteryUtilsTest {

    @Test
    public void isWinner() {
        System.out.println(LotteryUtils.isWinner("E7683B58A3439073196DD01EB03122C3630EAA82", 1554206929761L, 1554206927371L));
    }
}