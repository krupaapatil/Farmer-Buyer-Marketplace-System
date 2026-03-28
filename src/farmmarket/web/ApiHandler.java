package farmmarket.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.exceptions.NoMatchFoundException;
import farmmarket.model.Buyer;
import farmmarket.model.Farmer;
import farmmarket.model.MatchRecord;
import farmmarket.service.FileManager;
import farmmarket.service.MarketplaceManager;

public class ApiHandler implements HttpHandler {
    private final MarketplaceManager manager;
    private final FileManager fileManager;

    public ApiHandler(MarketplaceManager manager, FileManager fileManager) {
        this.manager = manager;
        this.fileManager = fileManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/health".equals(path)) {
                HttpUtil.sendJson(exchange, 200, "{\"status\":\"ok\"}");
                return;
            }
            if ("/api/farmers".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetFarmers(exchange);
                return;
            }
            if ("/api/farmers".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreateFarmer(exchange);
                return;
            }
            if ("/api/buyers".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetBuyers(exchange);
                return;
            }
            if ("/api/buyers".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreateBuyer(exchange);
                return;
            }
            if ("/api/matches".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleMatches(exchange);
                return;
            }
            if ("/api/reports/summary".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleSummary(exchange);
                return;
            }
            if ("/api/files/save".equals(path) && "POST".equalsIgnoreCase(method)) {
                manager.saveAll(fileManager);
                HttpUtil.sendJson(exchange, 200, "{\"message\":\"Data saved successfully.\"}");
                return;
            }
            if ("/api/files/load".equals(path) && "POST".equalsIgnoreCase(method)) {
                manager.loadAll(fileManager);
                HttpUtil.sendJson(exchange, 200, "{\"message\":\"Data loaded successfully.\"}");
                return;
            }

            HttpUtil.sendJson(exchange, 404, "{\"error\":\"Endpoint not found.\"}");
        } catch (InvalidDataException | DuplicateIdException | NoMatchFoundException e) {
            HttpUtil.sendJson(exchange, 400, "{\"error\":\"" + HttpUtil.jsonEscape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 500, "{\"error\":\"" + HttpUtil.jsonEscape(e.getMessage()) + "\"}");
        }
    }

    private void handleGetFarmers(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String crop = query.getOrDefault("crop", "");
        String city = query.getOrDefault("city", "");
        List<Farmer> farmers = manager.searchFarmers(crop, city);

        StringBuilder builder = new StringBuilder();
        builder.append("{\"farmers\":[");
        for (int index = 0; index < farmers.size(); index++) {
            Farmer farmer = farmers.get(index);
            if (index > 0) {
                builder.append(",");
            }
            builder.append("{")
                    .append("\"id\":\"").append(HttpUtil.jsonEscape(farmer.getId())).append("\",")
                    .append("\"name\":\"").append(HttpUtil.jsonEscape(farmer.getName())).append("\",")
                    .append("\"city\":\"").append(HttpUtil.jsonEscape(farmer.getCity())).append("\",")
                    .append("\"phone\":\"").append(HttpUtil.jsonEscape(farmer.getPhone())).append("\",")
                    .append("\"cropType\":\"").append(HttpUtil.jsonEscape(farmer.getCropType())).append("\",")
                    .append("\"quantityAvailable\":").append(farmer.getQuantityAvailable()).append(",")
                    .append("\"pricePerUnit\":").append(farmer.getPricePerUnit())
                    .append("}");
        }
        builder.append("]}");
        HttpUtil.sendJson(exchange, 200, builder.toString());
    }

    private void handleCreateFarmer(HttpExchange exchange)
            throws IOException, InvalidDataException, DuplicateIdException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        Farmer farmer = new Farmer(
                values.get("id"),
                values.get("name"),
                values.get("city"),
                values.get("phone"),
                values.get("cropType"),
                Integer.parseInt(values.getOrDefault("quantityAvailable", "0")),
                Double.parseDouble(values.getOrDefault("pricePerUnit", "0")));
        manager.addFarmer(farmer);
        HttpUtil.sendJson(exchange, 201, "{\"message\":\"Farmer added successfully.\"}");
    }

    private void handleGetBuyers(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String crop = query.getOrDefault("crop", "");
        String city = query.getOrDefault("city", "");
        List<Buyer> buyers = manager.searchBuyers(crop, city);

        StringBuilder builder = new StringBuilder();
        builder.append("{\"buyers\":[");
        for (int index = 0; index < buyers.size(); index++) {
            Buyer buyer = buyers.get(index);
            if (index > 0) {
                builder.append(",");
            }
            builder.append("{")
                    .append("\"id\":\"").append(HttpUtil.jsonEscape(buyer.getId())).append("\",")
                    .append("\"name\":\"").append(HttpUtil.jsonEscape(buyer.getName())).append("\",")
                    .append("\"city\":\"").append(HttpUtil.jsonEscape(buyer.getCity())).append("\",")
                    .append("\"phone\":\"").append(HttpUtil.jsonEscape(buyer.getPhone())).append("\",")
                    .append("\"requiredCrop\":\"").append(HttpUtil.jsonEscape(buyer.getRequiredCrop())).append("\",")
                    .append("\"requiredQuantity\":").append(buyer.getRequiredQuantity()).append(",")
                    .append("\"maxBudget\":").append(buyer.getMaxBudget())
                    .append("}");
        }
        builder.append("]}");
        HttpUtil.sendJson(exchange, 200, builder.toString());
    }

    private void handleCreateBuyer(HttpExchange exchange)
            throws IOException, InvalidDataException, DuplicateIdException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        Buyer buyer = new Buyer(
                values.get("id"),
                values.get("name"),
                values.get("city"),
                values.get("phone"),
                values.get("requiredCrop"),
                Integer.parseInt(values.getOrDefault("requiredQuantity", "0")),
                Double.parseDouble(values.getOrDefault("maxBudget", "0")));
        manager.addBuyer(buyer);
        HttpUtil.sendJson(exchange, 201, "{\"message\":\"Buyer added successfully.\"}");
    }

    private void handleMatches(HttpExchange exchange) throws IOException, NoMatchFoundException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String buyerId = query.getOrDefault("buyerId", "");
        List<MatchRecord> matches = buyerId.isBlank()
                ? manager.generateAllMatches()
                : manager.generateMatchesForBuyer(buyerId);

        StringBuilder builder = new StringBuilder();
        builder.append("{\"matches\":[");
        for (int index = 0; index < matches.size(); index++) {
            MatchRecord match = matches.get(index);
            String[] row = match.toTableRow();
            if (index > 0) {
                builder.append(",");
            }
            builder.append("{")
                    .append("\"buyerId\":\"").append(HttpUtil.jsonEscape(row[0])).append("\",")
                    .append("\"buyerName\":\"").append(HttpUtil.jsonEscape(row[1])).append("\",")
                    .append("\"farmerId\":\"").append(HttpUtil.jsonEscape(row[2])).append("\",")
                    .append("\"farmerName\":\"").append(HttpUtil.jsonEscape(row[3])).append("\",")
                    .append("\"cropType\":\"").append(HttpUtil.jsonEscape(row[4])).append("\",")
                    .append("\"city\":\"").append(HttpUtil.jsonEscape(row[5])).append("\",")
                    .append("\"availableQuantity\":").append(row[6]).append(",")
                    .append("\"pricePerUnit\":").append(row[7]).append(",")
                    .append("\"score\":").append(row[8]).append(",")
                    .append("\"status\":\"").append(HttpUtil.jsonEscape(row[9])).append("\"")
                    .append("}");
        }
        builder.append("],")
                .append("\"report\":\"")
                .append(HttpUtil.jsonEscape(manager.buildMatchReport()))
                .append("\"}");
        HttpUtil.sendJson(exchange, 200, builder.toString());
    }

    private void handleSummary(HttpExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"farmers\":").append(manager.getFarmers().size()).append(",")
                .append("\"buyers\":").append(manager.getBuyers().size()).append(",")
                .append("\"lastMatches\":").append(manager.getLastGeneratedMatches().size()).append(",")
                .append("\"summary\":\"").append(HttpUtil.jsonEscape(manager.buildSummaryReport())).append("\"")
                .append("}");
        HttpUtil.sendJson(exchange, 200, builder.toString());
    }
}
