package master.services;

import master.models.Block;
import master.models.Transaction;
import master.resources.Configurations;
import master.utils.RequestHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class WebhookService {

    static void sendBlockNotification(Block block, List<Transaction> transactions) {
        String url = Configurations.getStatsWebhook();
        if (url == null) return;
        try {
            JSONObject payload = new JSONObject();
            payload.put("timestamp", block.getTimestamp());
            payload.put("height", block.getHeight());
            payload.put("blockHash", block.getHash());
            payload.put("transactions", block.getTransactions());
            JSONArray outputs = new JSONArray();
            for (Transaction transaction : transactions) {
                JSONObject part = new JSONObject();
                part.put("to", transaction.getTo());
                part.put("amount", transaction.getAmount());
                outputs.put(part);
            }
            payload.put("outputs", outputs);
            RequestHelper.sendPostRequest(url, payload.toString());
        } catch (Exception e) {
            System.err.println("Can't send block notification to url " + url + ": [" + e.getClass() + "] " + e.getMessage());
        }
    }
}
