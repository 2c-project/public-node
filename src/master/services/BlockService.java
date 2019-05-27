package master.services;

import master.configs.LifecycleProperties;
import master.models.Block;
import master.models.Transaction;
import master.models.TransactionOutput;
import master.storage.FilesystemStorage;

import java.util.List;

public class BlockService {

    public static Block createBlock(Transaction winnerTransaction, List<Transaction> transactions, long time) {
        transactions.add(winnerTransaction);
        Block block = new Block(transactions, time);
        BroadcastService.broadcastBlock(block, winnerTransaction);
        transactions.stream().map(Transaction::getFrom).forEach(LifecycleProperties::removeAddressFromUsed);
        FilesystemStorage.saveBlock(block);
        WebhookService.sendBlockNotification(block, transactions);
        LifecycleProperties.updateLast(block.getHash(), block.getTimestamp(), block.getHeight());
        return block;
    }

    public static Block createGenesis() {
        Transaction emissionTransaction = new Transaction(emissionHash, null, systemAddress, startEmission, List.of(), List.of(new TransactionOutput(systemAddress, startEmission, emissionHash)), null, 0, 0);
        FilesystemStorage.saveTransaction(emissionTransaction);
        Block block = new Block(List.of(emissionTransaction.getHash()), null, "0", 0, 0);
        FilesystemStorage.saveGenesisBlock(block);
        return block;
    }

    public static boolean checkAndApplyGenesis(Block genesis) {
        if (!genesis.getHash().equals("0") || genesis.getTimestamp() != 0 || genesis.getHeight() != 0) return false;
        synchronized (LifecycleProperties.UTXOLock) {
            List<TransactionOutput> systemUTXOs = LifecycleProperties.getUTXOs(systemAddress);
            for (String hash : genesis.getTransactions()) {
                Transaction transaction = FilesystemStorage.getTransaction(hash);
                if (transaction == null || !transaction.getTo().equals(systemAddress)) return false;
                systemUTXOs.addAll(transaction.getOutputs());
            }
        }
        return true;
    }
}
