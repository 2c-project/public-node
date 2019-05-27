package master.models;

import master.utils.Secp256k1;
import master.utils.Utils;

public class Address {
    private final byte[] secret;
    private final byte[] publicKey;

    public Address(byte[] publicKey, byte[] secret) {
        this.publicKey = publicKey;
        this.secret = secret;
    }

    public Address(byte[] secret) {
        this.secret = secret;
        this.publicKey = Secp256k1.getPublicKey(secret);
    }

    public Address() {
        try {
            this.secret = Secp256k1.generatePrivateKey();
            this.publicKey = Secp256k1.getPublicKey(this.secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAddress(byte[] address) {
        if (address == null) return "";
        return Utils.bytesToHexString(Utils.sha256hash160(address));
    }

    public String getAddress() {
        if (publicKey == null) return "";
        return Utils.bytesToHexString(Utils.sha256hash160(publicKey));
    }

    public byte[] getSecret() {
        return secret;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
