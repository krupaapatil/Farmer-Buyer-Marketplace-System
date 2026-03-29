package farmmarket.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.net.httpserver.HttpExchange;

public final class HttpUtil {
    private HttpUtil() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        sendResponse(exchange, statusCode, "application/json; charset=utf-8", body);
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        sendJson(exchange, statusCode, toJson(body));
    }

    public static void sendText(HttpExchange exchange, int statusCode, String body) throws IOException {
        sendResponse(exchange, statusCode, "text/plain; charset=utf-8", body);
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseFormData(String rawBody) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawBody == null || rawBody.isBlank()) {
            return values;
        }

        String[] pairs = rawBody.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    public static Map<String, String> parseQuery(String rawQuery) {
        return parseFormData(rawQuery);
    }

    public static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(current);
                    break;
            }
        }
        return builder.toString();
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return "\"" + jsonEscape(text) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    continue;
                }
                if (!first) {
                    builder.append(",");
                }
                builder.append(toJson(key)).append(":").append(toJson(entry.getValue()));
                first = false;
            }
            builder.append("}");
            return builder.toString();
        }
        if (value instanceof Collection<?> collection) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    builder.append(",");
                }
                builder.append(toJson(item));
                first = false;
            }
            builder.append("]");
            return builder.toString();
        }
        if (value instanceof Object[] array) {
            StringBuilder builder = new StringBuilder("[");
            for (int index = 0; index < array.length; index++) {
                if (index > 0) {
                    builder.append(",");
                }
                builder.append(toJson(array[index]));
            }
            builder.append("]");
            return builder.toString();
        }
        return toJson(String.valueOf(value));
    }

    public static Map<String, String> parseCookies(String rawCookieHeader) {
        Map<String, String> cookies = new LinkedHashMap<>();
        if (rawCookieHeader == null || rawCookieHeader.isBlank()) {
            return cookies;
        }

        String[] segments = rawCookieHeader.split(";");
        for (String segment : segments) {
            String[] parts = segment.trim().split("=", 2);
            if (parts.length == 2) {
                cookies.put(parts[0].trim(), parts[1].trim());
            }
        }
        return cookies;
    }

    public static String getCookie(HttpExchange exchange, String name) {
        return parseCookies(exchange.getRequestHeaders().getFirst("Cookie")).get(name);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
