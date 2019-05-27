package master.configs;

import com.sun.net.httpserver.HttpServer;
import master.resources.ConfigurationManager;

import java.net.InetSocketAddress;

class WebServer {
    static void startServer() {
        try {
            int port = (int) ConfigurationManager.getConfigAsLong("port");
            System.out.println("Starting web server on port: " + port);
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            initAPI();
            httpServer.start();
        } catch (Exception ignore) {

        }
    }
}