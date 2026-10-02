package com.museum.server;

import com.museum.model.MuseumPass;
import com.museum.builder.MuseumPassBuilder;
import com.museum.factory.PassCreator;
import com.museum.factory.StandardPassCreator;
import com.museum.factory.StudentPassCreator;
import com.museum.prototype.PresetRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** HTTP integration of Factory Method products, Prototype presets, and Builder composition. */
public class MuseumServer {

    private static final int PORT = 8080;
    private static final Path STATIC_DIR =
            Path.of("src/main/resources/static");

    /** Starts the HTTP endpoints that expose the creational patterns and pass UI. */
    public void start() throws IOException {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(PORT),
                        0
                );

        server.createContext(
                "/api/pass",
                this::handlePass
        );

        server.createContext("/api/presets", this::handlePresets);

        server.createContext(
                "/",
                this::handleStatic
        );

        server.setExecutor(null);
        server.start();

        System.out.println(
                "Museum Experience Pass running at " +
                "http://localhost:" + PORT
        );
    }

    private void handlePass(HttpExchange exchange)
            throws IOException {

        if (!"GET".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJson(
                    exchange,
                    405,
                    "{\"error\":\"Method not allowed\"}"
            );

            return;
        }

        try {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String type = params.getOrDefault("type", "standard");
            PassCreator creator = switch (type) {
                case "standard" -> new StandardPassCreator();
                case "student" -> new StudentPassCreator();
                default -> throw new IllegalArgumentException("Unknown pass type: " + type);
            };
            List<String> options = params.containsKey("preset")
                    ? PresetRegistry.getClone(params.get("preset")).getOptions()
                    : Arrays.stream(params.getOrDefault("options", "").split(","))
                            .map(String::trim)
                            .filter(value -> !value.isBlank())
                            .collect(Collectors.toList());
            MuseumPassBuilder builder = new MuseumPassBuilder(creator.createBasePass());
            for (String option : options) {
                builder.withOption(option);
            }
            MuseumPass pass = builder.build();

            String servicesJson =
                    pass.getServices()
                            .stream()
                            .map(this::quote)
                            .collect(
                                    Collectors.joining(",")
                            );

            String json =
                    "{"
                    + "\"type\":" + quote(type) + ","
                    + "\"options\":["
                    + options.stream().distinct().map(this::quote).collect(Collectors.joining(","))
                    + "],"
                    + "\"description\":"
                    + quote(pass.getDescription())
                    + ","
                    + "\"price\":"
                    + String.format(
                            java.util.Locale.US,
                            "%.2f",
                            pass.getPrice()
                    )
                    + ","
                    + "\"services\":["
                    + servicesJson
                    + "],"
                    + "\"activation\":"
                    + quote(pass.activate())
                    + "}";

            sendJson(exchange, 200, json);
        } catch (IllegalArgumentException error) {
            sendJson(exchange, 400, "{\"error\":" + quote(error.getMessage()) + "}");
        }
    }

    private void handlePresets(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        String names = PresetRegistry.getPresetNames().stream()
                .map(this::quote).collect(Collectors.joining(","));
        sendJson(exchange, 200, "[" + names + "]");
    }

    private void handleStatic(HttpExchange exchange)
            throws IOException {

        if (!"GET".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendText(
                    exchange,
                    405,
                    "Method not allowed",
                    "text/plain"
            );

            return;
        }

        String requested =
                exchange.getRequestURI().getPath();

        if (requested.equals("/")) {
            requested = "/index.html";
        }

        Path file =
                STATIC_DIR
                        .resolve(
                                requested.substring(1)
                        )
                        .normalize();

        if (!file.startsWith(STATIC_DIR)
                || !Files.exists(file)
                || Files.isDirectory(file)) {

            sendText(
                    exchange,
                    404,
                    "Not found",
                    "text/plain"
            );

            return;
        }

        byte[] data =
                Files.readAllBytes(file);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType(file)
                );

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(data);
        }
    }

    private Map<String, String> parseQuery(URI uri) {

        if (uri.getRawQuery() == null) {
            return Map.of();
        }

        return Arrays.stream(
                uri.getRawQuery().split("&")
        )
        .map(part ->
                part.split("=", 2)
        )
        .filter(parts ->
                parts.length == 2
        )
        .collect(
                Collectors.toMap(
                        parts ->
                                URLDecoder.decode(
                                        parts[0],
                                        StandardCharsets.UTF_8
                                ),
                        parts ->
                                URLDecoder.decode(
                                        parts[1],
                                        StandardCharsets.UTF_8
                                ),
                        (first, second) -> second
                )
        );
    }

    private String contentType(Path file) {

        String name =
                file.getFileName().toString();

        if (name.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (name.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (name.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "application/octet-stream";
    }

    private String quote(String value) {
        StringBuilder json = new StringBuilder("\"");
        for (char character : value.toCharArray()) {
            if (character == '"' || character == '\\') {
                json.append('\\').append(character);
            } else if (character < 0x20) {
                json.append(String.format("\\u%04x", (int) character));
            } else {
                json.append(character);
            }
        }
        return json.append('"').toString();
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String body
    ) throws IOException {

        sendText(
                exchange,
                status,
                body,
                "application/json; charset=UTF-8"
        );
    }

    private void sendText(
            HttpExchange exchange,
            int status,
            String body,
            String contentType
    ) throws IOException {

        byte[] data =
                body.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType
                );

        exchange.sendResponseHeaders(
                status,
                data.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(data);
        }
    }
}
