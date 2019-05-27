package master.utils;

import master.models.Address;
import org.junit.Test;

import java.util.Arrays;

public class CryptoUtilsTest {

    @Test
    public void applySha256() {
        System.out.println(CryptoUtils.applySha256("string"));
    }

    @Test
    public void testCreateAddress() {
        Address address = new Address();
        System.out.println(Arrays.toString(address.getPublicKey()));
    }

    @Test
    public void signatureToString() {
        for (int i = 0; i < 1000; i++) {
            byte[] message = ("super secret data" + i).getBytes();
            byte[] privateKey = Secp256k1.generatePrivateKey();
            byte[][] rawSignature = Secp256k1.signTransaction(message, privateKey);
            String signature = CryptoUtils.signatureToString(rawSignature);
            byte[][] decodedSignature = CryptoUtils.stringToSignature(signature);
            try {
                if (!Arrays.equals(Secp256k1.recoverPublicKey(decodedSignature[0], decodedSignature[1], decodedSignature[2], message), Secp256k1.getPublicKey(privateKey))) {
                    System.out.println("Shouldn't happen");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void stringToSignature() {
    }
}