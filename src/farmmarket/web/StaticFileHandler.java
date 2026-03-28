package farmmarket.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticFileHandler implements HttpHandler {
    private final Path webRoot;

    public StaticFileHandler() {
        this(Paths.get("web"));
    }

    public StaticFileHandler(Path webRoot) {
        this.webRoot = webRoot.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if ("/".equals(requestPath) || requestPath.isBlank()) {
            requestPath = "/index.html";
        }

        Path requestedFile = webRoot.resolve(requestPath.substring(1)).normalize();
        if (!requestedFile.startsWith(webRoot) || !Files.exists(requestedFile) || Files.isDirectory(requestedFile)) {
            HttpUtil.sendText(exchange, 404, "File not found.");
            return;
        }

        String contentType = detectContentType(requestedFile);
        byte[] bytes = Files.readAllBytes(requestedFile);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private String detectContentType(Path file) throws IOException {
        String contentType = Files.probeContentType(file);
        if (contentType != null) {
            return contentType;
        }

        String fileName = file.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (fileName.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        return "text/plain; charset=utf-8";
    }
}
