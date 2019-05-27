package master.services;

import master.configs.BlockchainConstants;
import master.configs.LifecycleProperties;
import master.exceptions.FailedRequestException;
import master.exceptions.TransactionNotExistsException;
import master.models.Address;
import master.models.Transaction;
import master.resources.Configurations;
import master.storage.FilesystemStorage;
import master.storage.ObjectMapper;
import master.utils.RequestHelper;

public class TransactionService {
    public static Transaction sendMoney(String fromAddr, String toAddr, long amount) {
        Address from = FilesystemStorage.getWallet(fromAddr);
        Transaction transaction = new Transaction(fromAddr, toAddr, amount);
        transaction.generateSignature(from);
        if (!transaction.verifySignature()) throw new RuntimeException("Never happens but who knows");
        synchronized (LifecycleProperties.pendingTransactionsLock) {
            LifecycleProperties.pendingTransactions.put(transaction.getHash(), transaction);
        }
        FilesystemStorage.saveTransaction(transaction);
        BroadcastService.broadcastTransaction(transaction);
        LifecycleProperties.newTransactionHook(transaction);
        return transaction;
    }

    public static Transaction getByHash(String hash) {
        Transaction toRet = FilesystemStorage.getTransaction(hash);
        synchronized (LifecycleProperties.pendingTransactionsLock) {
            if (toRet == null) toRet = LifecycleProperties.pendingTransactions.get(hash);
        }
        if (toRet == null && Configurations.getParentNode() != null) {
            try {
                toRet = ObjectMapper.stringToTransaction(RequestHelper.sendGetRequestAsString(Configurations.getParentNode() + "/node/getRawTransaction?hash=" + hash), hash);
            } catch (FailedRequestException e) {
                throw new TransactionNotExistsException(hash);
            }
        }
        return toRet;
    }

    public static Transaction createWinnerTransaction(long timestamp, long commissions, String winner) {
        long minerReward = commissions;
        if (LifecycleProperties.getBlockchainHeight() * BlockchainConstants.mineReward < BlockchainConstants.maxToMine)
            minerReward += BlockchainConstants.mineReward;
        return new Transaction(null, winner, minerReward, timestamp);
    }
}
