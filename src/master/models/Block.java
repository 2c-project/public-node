package master.models;

import master.configs.LifecycleProperties;
import master.exceptions.InvalidBlockchainException;
import master.exceptions.InvalidTransactionException;
import master.services.TransactionService;
import master.storage.FilesystemStorage;
import master.utils.CryptoUtils;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private final List<String> transactions;
    private final String previousHash;
    private final String hash;
    private final long height;
    private final long timestamp;


    public Block(List<String> transactions, String previousHash, String hash, long height, long timestamp) {
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.hash = hash;
        this.height = height;
        this.timestamp = timestamp;
    }

    public Block(List<Transaction> transactions, long time) {
        this.transactions = new ArrayList<>(transactions.size());
        this.previousHash = LifecycleProperties.getLastBlockHash();
        this.height = LifecycleProperties.getBlockchainHeight() + 1;
        this.timestamp = time;
        this.hash = createHash(transactions);
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getHeight() {
        return height;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private String createHash(List<Transaction> transactions) {
        StringBuilder data = new StringBuilder(previousHash.length() * 2 + transactions.size() * 64 + 20);
        data.append(previousHash);
        synchronized (LifecycleProperties.pendingTransactionsLock) {
            for (Transaction transaction : transactions) {
                data.append(transaction.getHash());
                this.transactions.add(transaction.getHash());
                LifecycleProperties.pendingTransactions.remove(transaction.getHash());
            }
        }
        data.append(height);
        data.append(previousHash);
        return CryptoUtils.applySha256(data.toString());
    }

    private String calculateHash(List<Transaction> transactions) {
        StringBuilder data = new StringBuilder(previousHash.length() * 2 + transactions.size() * 64 + 20);
        data.append(previousHash);
        for (Transaction transaction : transactions) {
            data.append(transaction.getHash());
        }
        data.append(height);
        data.append(previousHash);
        return CryptoUtils.applySha256(data.toString());
    }

    public boolean verifyBlock() {
        List<Transaction> transactions = new ArrayList<>(this.transactions.size());
        for (String hash : this.transactions) {
            Transaction transaction;
            try {
                transaction = loadAndCheck(hash);
            } catch (InvalidTransactionException e) {
                return false;
            }
            transactions.add(transaction);
        }
        return calculateHash(transactions).equals(hash);
    }

    public boolean verifyBlockAndApplyOutputs(Block previous) {
        if (hash.equals("0")) return true;
        for (String hash : this.getTransactions()) {
            try {
                Transaction transaction = loadAndCheck(hash);

                transaction.processInputs(previous.getTimestamp());
                FilesystemStorage.addTransactionToAddress(transaction);
            } catch (InvalidTransactionException e) {
                throw new InvalidBlockchainException("Invalid transaction: " + hash + " in block " + this.getHeight(), e);
            }
        }
        return this.verifyBlock();
    }

    private Transaction loadAndCheck(String hash) throws InvalidTransactionException {
        Transaction transactionObject = TransactionService.getByHash(hash);
        if (transactionObject == null || !transactionObject.verifySignature())
            throw new InvalidTransactionException(hash);
        return transactionObject;
    }
}
