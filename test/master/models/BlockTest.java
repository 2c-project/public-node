package master.models;

import master.resources.ConfigurationManager;
import master.storage.FilesystemStorage;
import org.junit.Test;

public class BlockTest {

    @Test
    public void getSignature() {
        ConfigurationManager.initConfigs("windows");
        Address address = new Address();
        FilesystemStorage.saveWallet(address);
//        Address to = new Address();
//        Transaction transaction = new Transaction(address.getPublicKey(), to.getPublicKey(), 20);
//        transaction.generateSignature(address);
//        Block block = new Block(List.of(transaction), "0");
//        assert !block.getHash().isEmpty();
    }
//
//    @Test
//    public void verifyBadBlock() {
//        Address address = new Address();
//        Address to = new Address();
//        Transaction transaction = new Transaction(address.getPublicKey(), to.getPublicKey(), 20);
//        transaction.generateSignature(to);
//        Block block = new Block(new ArrayList<>(){{add(transaction);}}, "0");
//        assert !block.verifyBlock();
//    }
}