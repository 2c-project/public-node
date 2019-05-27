package master.storage;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemStorageTest {

//    @Test
//    public void saveWallet() {
//        Address address = new Address();
//        FilesystemStorage.saveWallet(address);
//    }
//
//    @Test
//    public void saveTransaction() {
//        Address from = new Address(), to = new Address();
//        Transaction transaction = new Transaction(from.getPublicKey(), to.getPublicKey(), 10);
//        transaction.generateSignature(from);
//        FilesystemStorage.saveTransaction(transaction);
//    }
//
//    @Test
//    public void saveBlock() {
//        Address from = new Address(), to = new Address();
//        Transaction transaction = new Transaction(from.getPublicKey(), to.getPublicKey(), 10);
//        Transaction transaction2 = new Transaction(to.getPublicKey(), from.getPublicKey(), 10);
//        transaction.generateSignature(from); transaction2.generateSignature(to);
//        Block block = new Block(List.of(transaction, transaction2), "0");
//        FilesystemStorage.saveBlock(block);
//    }
//
//    @Test
//    public void getZipped() {
//        ConfigurationManager.initConfigs("windows");
//        LifecycleProperties.blockchainHeight=10;
//        System.out.println(FilesystemStorage.getZipped(0, 0));
//    }

    @Test
    public void loadTransaction() throws IOException {
        Path file = Path.of("/Work/NodeStorage/transactions/fdc4fcbe5b1b4415ca3ad8468ee6b9587ecb92a66dfe4e819a3b0971862f3425");
        String oneString = Files.readString(file);
        byte[] read = Files.readAllBytes(file);
        String another = new String(read);

        System.out.println();
    }
}