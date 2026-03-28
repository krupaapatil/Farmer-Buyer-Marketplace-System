package farmmarket.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.service.FileManager;
import farmmarket.service.MarketplaceManager;

public final class MarketplaceWebServer {
    private MarketplaceWebServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        MarketplaceManager manager = new MarketplaceManager();
        FileManager fileManager = new FileManager();

        try {
            manager.loadAll(fileManager);
        } catch (InvalidDataException | DuplicateIdException e) {
            System.err.println("Could not load startup data: " + e.getMessage());
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api", new ApiHandler(manager, fileManager));
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Farmer-Buyer web app running on http://0.0.0.0:" + port);
        System.out.println("Health check: http://0.0.0.0:" + port + "/api/health");
    }

    private static int resolvePort(String[] args) {
        if (args != null && args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String portFromEnv = System.getenv("PORT");
        if (portFromEnv != null && !portFromEnv.isBlank()) {
            return Integer.parseInt(portFromEnv);
        }
        return 8080;
    }
}
