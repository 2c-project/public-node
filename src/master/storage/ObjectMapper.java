package master.storage;

import master.models.Address;
import master.models.Block;
import master.models.Transaction;
import master.models.TransactionOutput;
import master.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectMapper {
    public static String blockToString(Block block) {
        return block.getPreviousHash() + "," + block.getTimestamp() + '\n' + String.join(",", block.getTransactions());
    }

    public static String transactionToString(Transaction transaction) {
        return String.valueOf(transaction.getAmount()) + ',' + transaction.getFrom() + ',' + transaction.getTo() + ',' + transaction.getTimestamp() + "," + transaction.getHeight() + '\n' + String.join(",", transaction.getInputs()) + '\n' + transaction.getOutputs().stream().map(to -> (to.getRecipient() != null ? to.getRecipient() : "") + "," + to.getValue()).collect(Collectors.joining(";")) + "\n" + transaction.getSignature();
    }

    public static Transaction stringToTransaction(String serializedTransaction, String hash) {
        String[] lines = serializedTransaction.split("\n");
        String[] firstLine = lines[0].split(","), inputs = (lines.length > 1 && !lines[1].isEmpty()) ? lines[1].split(",") : null, outputStrs = lines.length > 2 ? lines[2].split(";") : null;
        String from = firstLine[1].equals("null") ? null : firstLine[1], to = firstLine[2].equals("null") ? null : firstLine[2];
        long amount = Long.parseLong(firstLine[0]), timestamp = Long.parseLong(firstLine[3]), height = Long.parseLong(firstLine[4]);
        List<TransactionOutput> outputs = new ArrayList<>();
        if (outputStrs != null)
            for (String outputStr : outputStrs) {
                String[] parts = outputStr.split(",");
                outputs.add(new TransactionOutput(parts[0].isEmpty() ? null : parts[0], Long.parseLong(parts[1]), hash));
            }
        StringBuilder signature = new StringBuilder();
        for (int i = lines.length - 1; i >= 3; i--) {
            signature.insert(0, lines[i]);
        }
        return new Transaction(hash, from, to, amount, inputs == null ? new ArrayList<>() : List.of(inputs), outputs, signature.length() == 0 ? null : signature.toString(), timestamp, height);

    }

    public static Address stringToAddress(String serializedAddress) {
        String[] lines = serializedAddress.split("\n");
        return new Address(Utils.hexStringToByteArray(lines[0]), Utils.hexStringToByteArray(lines[1]));
    }

    public static Block stringToBlock(String serializedBlock, String hash, long height) {
        String[] lines = serializedBlock.split("\n");
        String[] firstLine = lines[0].split(",");
        if (lines.length == 1)
            return new Block(new ArrayList<>(), firstLine[0], hash, height, Long.parseLong(firstLine[1]));
        String[] transactions = lines[1].split(",");
        return new Block(Arrays.asList(transactions), firstLine[0], hash, height, Long.parseLong(firstLine[1]));
    }
}
