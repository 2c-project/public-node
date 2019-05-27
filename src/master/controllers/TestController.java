package master.controllers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class TestController extends AbstractController {

    public static void sendMoney(HttpExchange exchange) throws IOException {

    }

    public static void createWallet(HttpExchange exchange) throws IOException {
        var answer = createWallet();
        sendResponse(exchange, 200, answer);
    }

    public static void getTransaction(HttpExchange exchange) throws IOException {
        try {
            var answer = getTransaction();
            sendResponse(exchange, 200, answer);
        } catch (Exception e) {
            sendError(e, exchange);
        }
    }

    public static void getBlock(HttpExchange exchange) throws IOException {
        var answer = getBlock();
        sendResponse(exchange, 200, answer);
    }

    public static void getLastBlocks(HttpExchange exchange) throws IOException {
        var answer = getLastBlocks();
        sendResponse(exchange, 200, answer);
    }

    public static void getAddressHistory(HttpExchange exchange) throws IOException {
        var answer = getAddressHistory();
        sendResponse(exchange, 200, answer);
    }

    public static void getBalance(HttpExchange exchange) throws IOException {
        var answer = getBalance();
        sendResponse(exchange, 200, answer);
    }

    public static void getNeighboursAmount(HttpExchange exchange) throws IOException {
        var answer = getNeighboursAmount();
        sendResponse(exchange, 200, answer));
    }

    public static void lastTransactions(HttpExchange exchange) throws IOException {
        var answer = getLastTransactions();
        sendResponse(exchange, 200, answer);
    }

    public static void isValidAddress(HttpExchange exchange) throws IOException {
        var answer = isValidAddress();
        sendResponse(exchange, 200, answer);
    }

}
