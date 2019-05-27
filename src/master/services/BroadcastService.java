package master.services;

import master.configs.LifecycleProperties;
import master.exceptions.FailedRequestException;
import master.models.Block;
import master.models.Transaction;
import master.resources.enums.EventTypes;
import master.storage.ObjectMapper;
import master.utils.RequestHelper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BroadcastService {
    public static void broadcastTransaction(Transaction transaction) {
        JSONObject payload = new JSONObject();
        payload.put("type", EventTypes.Transaction);
        payload.put("hash", transaction.getHash());
        payload.put("transaction", ObjectMapper.transactionToString(transaction));
        new Thread(() -> broadcastToAll(payload.toString())).start();
    }

    public static void broadcastBlock(Block block, Transaction winnerTransaction) {
        JSONObject payload = new JSONObject();
        payload.put("type", EventTypes.Block);
        payload.put("block", ObjectMapper.blockToString(block));
        payload.put("hash", block.getHash());
        payload.put("height", block.getHeight());
        payload.put("winnerTransaction", ObjectMapper.transactionToString(winnerTransaction));
        payload.put("winnerTransactionHash", winnerTransaction.getHash());
        new Thread(() -> broadcastToAll(payload.toString())).start();
    }

    public static void broadcastNewNode(String nodeAddress) {
        JSONObject payload = new JSONObject();
        payload.put("type", EventTypes.Node);
        payload.put("address", nodeAddress);
        new Thread(() -> broadcastToAll(payload.toString()));
    }

    private static void broadcastToAll(String message) {
        List<String> deletedNodes = new ArrayList<>(LifecycleProperties.getNeighbours().size() / 3);
        for (String node : LifecycleProperties.getNeighbours()) {
            try {
                RequestHelper.sendPostRequest(node + "/nodes/receive", message);
            } catch (FailedRequestException e) {
                System.err.printf("Can't send request to %s, got exception: [%s] %s\n", node, e.getClass(), e.getMessage());
                deletedNodes.add(node);
            }
        }
        if (deletedNodes.isEmpty()) return;
        System.out.printf("Failed broadcast to %s, deleting\n", deletedNodes);
        LifecycleProperties.removeNeighbour(deletedNodes);
    }
}
