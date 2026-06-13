import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailSpamClassifier {
    private static final int PORT = 8080;
    private static final Path PUBLIC_DIR = Paths.get("public");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/classify", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Only POST is allowed", "text/plain");
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> payload = parseJson(requestBody);
            String subject = payload.getOrDefault("subject", "");
            String body = payload.getOrDefault("body", "");

            Map<String, Object> result = classifyEmail(subject, body);
            String json = toJson(result);
            sendResponse(exchange, 200, json, "application/json");
        });

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                serveFile(exchange, PUBLIC_DIR.resolve("index.html"));
            } else {
                serveFile(exchange, PUBLIC_DIR.resolve(path.substring(1)));
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:" + PORT);
    }

    private static Map<String, Object> classifyEmail(String subject, String body) {
        String text = (subject + " " + body).toLowerCase();
        double score = 0;

        String[] spamTriggers = {
            "free", "win", "won", "prize", "urgent", "limited time", "act now",
            "click here", "buy now", "credit card", "bank account", "password", "subscribe",
            "lottery", "million", "offer", "congratulations", "dear friend", "risk free"
        };

        for (String trigger : spamTriggers) {
            if (text.contains(trigger)) {
                score += trigger.length() > 6 ? 1.4 : 1.0;
            }
        }

        if (text.matches(".*\\b(urgent|action required|click here|buy now|credit card|bank account|verify your|claim your|transfer|limited offer)\\b.*")) {
            score += 1.8;
        }

        if (body.length() < 40) {
            score += 0.8;
        }

        if (subject.length() < 10 && subject.length() > 0) {
            score += 0.5;
        }

        if (body.contains("unsubscribe") || body.contains("manage preferences")) {
            score += 1.2;
        }

        boolean isSpam = score >= 4.0;

        Map<String, Object> result = new HashMap<>();
        result.put("subject", subject);
        result.put("body", body);
        result.put("score", Math.round(score * 10.0) / 10.0);
        result.put("label", isSpam ? "Spam" : "Not Spam");
        result.put("explanation", isSpam
                ? "The message contains several spam indicators such as urgent offers, click requests, and money-related keywords."
                : "The message appears harmless based on common spam signals. Always verify unexpected emails manually." );
        return result;
    }

    private static void serveFile(HttpExchange exchange, Path filePath) throws IOException {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendResponse(exchange, 404, "Not found", "text/plain");
            return;
        }

        String contentType = guessContentType(filePath);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, int code, String body, String contentType) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType + "; charset=utf-8");
        byte[] bytes = body.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> data = new HashMap<>();
        Pattern pattern = Pattern.compile("\"(subject|body)\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\r", "");
            data.put(key, value);
        }
        return data;
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) builder.append(",");
            first = false;
            builder.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean) {
                builder.append(value);
            } else {
                builder.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String guessContentType(Path filePath) throws IOException {
        String contentType = Files.probeContentType(filePath);
        if (contentType != null) {
            return contentType;
        }
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html";
        if (fileName.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
