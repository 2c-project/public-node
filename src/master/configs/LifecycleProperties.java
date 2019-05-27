package master.configs;

import master.exceptions.InvalidTransactionException;
import master.models.Block;
import master.models.ShortTransaction;
import master.models.Transaction;
import master.models.TransactionOutput;
import master.resources.Configurations;
import master.storage.FilesystemStorage;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

public class LifecycleProperties {
    public static final Object UTXOLock = new Object(), pendingTransactionsLock = new Object(), nodesLock = new Object(), usedAddressesLock = new Object();
    private final static Map<String, List<TransactionOutput>> globalUTXOs = new HashMap<>();
    public static Map<String, Transaction> pendingTransactions = new LinkedHashMap<>();
    public static List<ShortTransaction> lastTransactions = new LinkedList<>();
    private static List<String> nodes = new ArrayList<>();
    private static String lastBlockHash;
    private static long previousBlockTime;
    private static long lastBlockTime;
    private static long blockchainHeight;
    private static List<String> usedAddresses = new ArrayList<>(30);

    public static void receivePendingTransaction(Transaction transaction) throws InvalidTransactionException {
        if (!transaction.verifySignature()) throw new InvalidTransactionException("Signature is invalid");
        transaction.processInputs(LifecycleProperties.getLastBlockTime());
        synchronized (LifecycleProperties.pendingTransactionsLock) {
            pendingTransactions.putIfAbsent(transaction.getHash(), transaction);
        }
        FilesystemStorage.saveTransaction(transaction);
        LifecycleProperties.newTransactionHook(transaction);
        System.out.printf("Received transaction %s", transaction.toString());
    }

    public static void receiveBlock(Block block, Transaction winnerTransaction) throws InvalidTransactionException {
        handleRecievedBlock(block, winnerTransaction);
    }

    public static void registerNewNode(String url) {
        synchronized (nodesLock) {
            if (nodes.contains(url)) return;
            nodes.add(url);
        }
    }

    public static List<TransactionOutput> getUTXOs(String address) {
        return globalUTXOs.computeIfAbsent(address, s -> new ArrayList<>());
    }

    public static long getBalance(String address) {
        synchronized (LifecycleProperties.UTXOLock) {
            List<TransactionOutput> UTXOs = LifecycleProperties.getUTXOs(address);
            return UTXOs.stream().mapToLong(TransactionOutput::getValue).sum();
        }
    }

    public static void updateLast(String lastHash, long lastTimestamp, long lastHeight) {
        LifecycleProperties.previousBlockTime = LifecycleProperties.lastBlockTime;
        LifecycleProperties.lastBlockTime = lastTimestamp;
        LifecycleProperties.lastBlockHash = lastHash;
        LifecycleProperties.blockchainHeight = lastHeight;
        FilesystemStorage.saveConfigs();
    }

    public static void newTransactionHook(Transaction transaction) {
        lastTransactions.add(0, new ShortTransaction(transaction.getHash(), transaction.getTimestamp()));
        if (lastTransactions.size() > 10) lastTransactions.remove(10);
        synchronized (usedAddressesLock) {
            usedAddresses.add(transaction.getFrom());
        }
    }

    public static boolean isAddressUsed(String address) {
        synchronized (usedAddressesLock) {
            return usedAddresses.contains(address);
        }
    }

    public static void removeAddressFromUsed(String address) {
        synchronized (usedAddressesLock) {
            usedAddresses.remove(address);
        }
    }

    public static void addNeighbour(String... addresses) {
        synchronized (nodesLock) {
            for (String address : addresses) {
                if (nodes.contains(address)) return;
                nodes.add(address);
            }
            FilesystemStorage.saveNeighbours();
        }
    }

    public static void removeNeighbour(List<String> addresses) {
        synchronized (nodesLock) {
            for (String address : addresses) {
                if (!nodes.contains(address)) return;
                nodes.remove(address);
            }
            FilesystemStorage.saveNeighbours();
        }
    }

    public static void removeNeighbour(String... addresses) {
        synchronized (nodesLock) {
            for (String address : addresses) {
                if (!nodes.contains(address)) return;
                nodes.remove(address);
            }
            FilesystemStorage.saveNeighbours();
        }
    }

    public static List<String> getNeighbours() {
        synchronized (nodesLock) {
            return nodes;
        }
    }

    static void loadParams() {
        nodes = FilesystemStorage.loadNeighbours();

        try {
            FileInputStream fileOut =
                    new FileInputStream(Configurations.getStoragePrefix() + "/lastTransactions.ser");
            ObjectInputStream in = new ObjectInputStream(fileOut);
            Object object = in.readObject();
            if (!(object instanceof List)) throw new RuntimeException("Read object isn't a list instance");
            LifecycleProperties.lastTransactions = (List<ShortTransaction>) object;
        } catch (Exception e) {
            System.err.println("Can't load last transactions: [" + e.getClass() + "] " + e.getMessage());
        }
    }

    public static String getLastBlockHash() {
        return lastBlockHash;
    }

    public static long getLastBlockTime() {
        return lastBlockTime;
    }

    public static long getBlockchainHeight() {
        return blockchainHeight;
    }
}
