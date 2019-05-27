package master.controllers;

import com.sun.net.httpserver.HttpExchange;

abstract class AbstractController {
    protected static void sendResponse(HttpExchange exchange, int status, String message) {

    }

    protected static void sendError(Throwable e, HttpExchange httpExchange) {

    }
}
