package master.models;

import master.configs.BlockchainConstants;
import master.configs.LifecycleProperties;
import master.exceptions.InsufficientFundsException;
import master.exceptions.InvalidTransactionException;
import master.resources.Configurations;
import master.utils.CryptoUtils;
import master.utils.LotteryUtils;
import master.utils.Secp256k1;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Transaction {
    private final long height;
    private final String hash;
    private final String from;
    private final String to;
    private final long amount;
    private final long timestamp;
    private final List<String> inputs;
    private final List<TransactionOutput> outputs;
    private String signature;

    public Transaction(String from, String to, long amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        height = LifecycleProperties.getBlockchainHeight();
        createInputs();
    }

    public Transaction(String from, String to, long amount, long timestamp) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.timestamp = timestamp;
        this.hash = calculateHash();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        createInputs();
        height = LifecycleProperties.getBlockchainHeight();
    }

    public Transaction(String hash, String from, String to, long amount, List<String> inputs, List<TransactionOutput> outputs, String signature, long timestamp, long height) {
        this.hash = hash;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.inputs = inputs;
        this.signature = signature;
        this.timestamp = timestamp;
        this.outputs = outputs;
        this.height = height;
    }

    private void createInputs() {
        long neededAmount = amount + (from == null ? 0 : BlockchainConstants.commission);
        synchronized (LifecycleProperties.UTXOLock) {
            if (this.from != null) {
                List<TransactionOutput> globalOutputs = LifecycleProperties.getUTXOs(this.from);
                for (TransactionOutput output : globalOutputs) {
                    neededAmount -= output.getValue();
                    inputs.add(output.getId());
                }
                if (neededAmount > 0) {
                    inputs.clear();
                    throw new InsufficientFundsException();
                }
                globalOutputs.clear();
                if (neededAmount < 0) {
                    TransactionOutput fromOutput = new TransactionOutput(this.from, Math.abs(neededAmount), this.hash);
                    outputs.add(fromOutput);
                    globalOutputs.add(fromOutput);
                }
            }
            TransactionOutput toOutput = new TransactionOutput(to, amount, this.hash);
            outputs.add(toOutput);
            LifecycleProperties.getUTXOs(to).add(toOutput);
        }
        if (from != null) outputs.add(new TransactionOutput(null, BlockchainConstants.commission, this.hash));
    }

    public void processInputs(long previousBlockTimestamp) throws InvalidTransactionException {
        synchronized (LifecycleProperties.UTXOLock) {
            if (from != null) {
                List<TransactionOutput> globalOutputs = LifecycleProperties.getUTXOs(this.from);
                all:
                for (String input : inputs) {
                    Iterator<TransactionOutput> availableOutputs = globalOutputs.iterator();


                    while (availableOutputs.hasNext()) {
                        TransactionOutput availableOutput = availableOutputs.next();
                        if (availableOutput.getId().equals(input)) {
                            availableOutputs.remove();
                            continue all;
                        }
                    }
                    throw new InvalidTransactionException(String.format("Can't find input with id [%s], available outputs: %s", input, globalOutputs.toString()));
                }
            }
            if (from == null) {
                TransactionOutput output = outputs.get(0);
                if (outputs.size() > 1 || output == null)
                    throw new InvalidTransactionException(String.format("Transaction %s neither has `from` field no winner output", this.getHash()));
                if (previousBlockTimestamp < 0 || !LotteryUtils.isWinner(output.getRecipient(), this.timestamp, previousBlockTimestamp)) {
                    throw new InvalidTransactionException(String.format("Transaction %s is invalid - supposed to be winner [%s, %s, %s] but wasn't winner", this.getHash(), output.getRecipient(), this.timestamp, previousBlockTimestamp));
                }
            }

            for (TransactionOutput output : outputs) {
                LifecycleProperties.getUTXOs(output.getRecipient()).add(output);
            }
        }
    }

    public long getCommission() {
        for (TransactionOutput output : outputs) {
            if (output.getRecipient() == null) return output.getValue();
        }
        return 0;
    }

    private String calculateHash() {
        return CryptoUtils.applySha256(
                from + to +
                        amount + timestamp
        );
    }

    public void generateSignature(Address address) {
        if (address == null) return;
        byte[] data = CryptoUtils.applyRawSha256(from + to + amount + timestamp);
        signature = CryptoUtils.signatureToString(Secp256k1.signTransaction(data, address.getSecret()));
    }

    public boolean verifySignature() {
        try {
            byte[] data = CryptoUtils.applyRawSha256(from + to + amount + timestamp);
            byte[][] rawSignature = CryptoUtils.stringToSignature(signature);
            return Address.getAddress(Secp256k1.recoverPublicKey(rawSignature[0], rawSignature[1], rawSignature[2], data)).equals(from);
        } catch (Exception e) {
            throw new RuntimeException("Can't verify transaction " + hash, e);
        }
    }

    public String getHash() {
        return hash;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getAmount() {
        return amount;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public byte[][] getRawSignature() {
        if (signature == null) return new byte[0][0];
        return CryptoUtils.stringToSignature(signature);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public long getHeight() {
        return height;
    }

    public Path getStoragePath() {
        return Path.of(Configurations.getStoragePrefix() + "/transactions/" + hash);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "height=" + height +
                ", hash='" + hash + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
