package farmmarket.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.model.AccountUser;
import farmmarket.model.ActivityEntry;
import farmmarket.model.CropPost;
import farmmarket.model.DashboardSnapshot;
import farmmarket.model.MatchRecord;
import farmmarket.model.PurchaseRequest;
import farmmarket.service.MarketplaceDatabase;
import farmmarket.util.AppConstants;

public class ApiHandler implements HttpHandler {
    private final MarketplaceDatabase database;

    public ApiHandler(MarketplaceDatabase database) {
        this.database = database;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("/api/health".equals(path)) {
                HttpUtil.sendJson(exchange, 200, Map.of("status", "ok"));
                return;
            }
            if ("/api/auth/signup".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleSignup(exchange);
                return;
            }
            if ("/api/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
                return;
            }
            if ("/api/auth/logout".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleLogout(exchange);
                return;
            }
            if ("/api/auth/session".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleSession(exchange);
                return;
            }

            AccountUser currentUser = requireAuthenticated(exchange);
            if (currentUser == null) {
                return;
            }

            if ("/api/dashboard".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleDashboard(exchange, currentUser);
                return;
            }
            if ("/api/profile".equals(path) && "GET".equalsIgnoreCase(method)) {
                HttpUtil.sendJson(exchange, 200, Map.of("user", userToMap(currentUser)));
                return;
            }
            if ("/api/profile".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleProfileUpdate(exchange, currentUser);
                return;
            }
            if ("/api/crops".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetCrops(exchange, currentUser);
                return;
            }
            if ("/api/crops".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreateCrop(exchange, currentUser);
                return;
            }
            if ("/api/purchases".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleGetPurchases(exchange, currentUser);
                return;
            }
            if ("/api/purchases".equals(path) && "POST".equalsIgnoreCase(method)) {
                handleCreatePurchase(exchange, currentUser);
                return;
            }

            HttpUtil.sendJson(exchange, 404, Map.of("error", "Endpoint not found."));
        } catch (InvalidDataException exception) {
            HttpUtil.sendJson(exchange, 400, Map.of("error", exception.getMessage()));
        } catch (SQLException exception) {
            HttpUtil.sendJson(exchange, 500, Map.of("error", "Database error: " + exception.getMessage()));
        } catch (Exception exception) {
            HttpUtil.sendJson(exchange, 500, Map.of("error", exception.getMessage()));
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser user = database.createUser(
                values.get("fullName"),
                values.get("email"),
                values.get("city"),
                values.get("phone"),
                values.get("password"));
        String sessionId = database.createSession(user.getUserId());
        attachSessionCookie(exchange, sessionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Account created successfully.");
        response.put("user", userToMap(user));
        response.put("redirectTo", "/dashboard");
        HttpUtil.sendJson(exchange, 201, response);
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser user = database.authenticate(values.get("email"), values.get("password"));
        String sessionId = database.createSession(user.getUserId());
        attachSessionCookie(exchange, sessionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Logged in successfully.");
        response.put("user", userToMap(user));
        response.put("redirectTo", "/dashboard");
        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleLogout(HttpExchange exchange) throws IOException, SQLException {
        database.deleteSession(HttpUtil.getCookie(exchange, AppConstants.SESSION_COOKIE_NAME));
        clearSessionCookie(exchange);
        HttpUtil.sendJson(exchange, 200, Map.of("message", "Logged out successfully."));
    }

    private void handleSession(HttpExchange exchange) throws IOException, SQLException {
        AccountUser user = resolveAuthenticatedUser(exchange);
        if (user == null) {
            HttpUtil.sendJson(exchange, 401, Map.of("authenticated", false));
            return;
        }
        HttpUtil.sendJson(exchange, 200, Map.of(
                "authenticated", true,
                "user", userToMap(user)));
    }

    private void handleDashboard(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        DashboardSnapshot snapshot = database.buildDashboard(currentUser.getUserId());
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("cropPosts", snapshot.getCropPostsCount());
        stats.put("purchaseRequests", snapshot.getPurchaseRequestsCount());
        stats.put("marketplaceCrops", snapshot.getMarketplaceCropCount());
        stats.put("marketplaceDemand", snapshot.getMarketplaceDemandCount());
        stats.put("availableMatches", snapshot.getAvailableMatchesCount());

        List<Map<String, Object>> recentActivity = new ArrayList<>();
        for (ActivityEntry activityEntry : snapshot.getRecentActivity()) {
            recentActivity.add(activityToMap(activityEntry));
        }

        List<Map<String, Object>> topMatches = new ArrayList<>();
        for (MatchRecord matchRecord : snapshot.getTopMatches()) {
            topMatches.add(matchToMap(matchRecord));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", userToMap(snapshot.getUser()));
        response.put("stats", stats);
        response.put("recentActivity", recentActivity);
        response.put("topMatches", topMatches);
        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleProfileUpdate(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser updatedUser = database.updateUser(
                currentUser.getUserId(),
                values.get("fullName"),
                values.get("city"),
                values.get("phone"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Profile updated successfully.");
        response.put("user", userToMap(updatedUser));
        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleGetCrops(HttpExchange exchange, AccountUser currentUser) throws IOException, SQLException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        boolean ownOnly = "mine".equalsIgnoreCase(query.getOrDefault("scope", "all"));
        List<CropPost> cropPosts = database.getCropPosts(
                ownOnly ? currentUser.getUserId() : null,
                query.getOrDefault("crop", ""),
                query.getOrDefault("city", ""));

        List<Map<String, Object>> items = new ArrayList<>();
        for (CropPost cropPost : cropPosts) {
            items.add(cropToMap(cropPost));
        }

        HttpUtil.sendJson(exchange, 200, Map.of(
                "scope", ownOnly ? "mine" : "all",
                "crops", items));
    }

    private void handleCreateCrop(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        CropPost cropPost = database.createCropPost(
                currentUser.getUserId(),
                values.get("cropType"),
                parseRequiredInt(values.get("quantityKg"), "Quantity"),
                parseRequiredDouble(values.get("pricePerKg"), "Price per kg"),
                values.get("notes"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Crop listing created successfully.");
        response.put("crop", cropToMap(cropPost));
        HttpUtil.sendJson(exchange, 201, response);
    }

    private void handleGetPurchases(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        boolean ownOnly = "mine".equalsIgnoreCase(query.getOrDefault("scope", "all"));
        List<PurchaseRequest> purchaseRequests = database.getPurchaseRequests(
                ownOnly ? currentUser.getUserId() : null,
                query.getOrDefault("crop", ""),
                query.getOrDefault("city", ""));

        List<Map<String, Object>> items = new ArrayList<>();
        for (PurchaseRequest purchaseRequest : purchaseRequests) {
            items.add(purchaseToMap(purchaseRequest));
        }

        HttpUtil.sendJson(exchange, 200, Map.of(
                "scope", ownOnly ? "mine" : "all",
                "purchases", items));
    }

    private void handleCreatePurchase(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        PurchaseRequest purchaseRequest = database.createPurchaseRequest(
                currentUser.getUserId(),
                values.get("cropType"),
                parseRequiredInt(values.get("quantityKg"), "Required quantity"),
                parseRequiredDouble(values.get("maxBudget"), "Budget per kg"),
                values.get("notes"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Purchase request created successfully.");
        response.put("purchase", purchaseToMap(purchaseRequest));
        HttpUtil.sendJson(exchange, 201, response);
    }

    private AccountUser requireAuthenticated(HttpExchange exchange) throws IOException, SQLException {
        AccountUser currentUser = resolveAuthenticatedUser(exchange);
        if (currentUser == null) {
            clearSessionCookie(exchange);
            HttpUtil.sendJson(exchange, 401, Map.of("error", "Please log in to continue."));
        }
        return currentUser;
    }

    private AccountUser resolveAuthenticatedUser(HttpExchange exchange) throws SQLException {
        return database.findUserBySession(HttpUtil.getCookie(exchange, AppConstants.SESSION_COOKIE_NAME));
    }

    private void attachSessionCookie(HttpExchange exchange, String sessionId) {
        int maxAgeSeconds = AppConstants.SESSION_DURATION_DAYS * 24 * 60 * 60;
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                AppConstants.SESSION_COOKIE_NAME + "=" + sessionId
                        + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + maxAgeSeconds);
    }

    private void clearSessionCookie(HttpExchange exchange) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                AppConstants.SESSION_COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
    }

    private int parseRequiredInt(String rawValue, String fieldName) throws InvalidDataException {
        try {
            return Integer.parseInt(rawValue);
        } catch (Exception exception) {
            throw new InvalidDataException(fieldName + " must be a valid number.");
        }
    }

    private double parseRequiredDouble(String rawValue, String fieldName) throws InvalidDataException {
        try {
            return Double.parseDouble(rawValue);
        } catch (Exception exception) {
            throw new InvalidDataException(fieldName + " must be a valid number.");
        }
    }

    private Map<String, Object> userToMap(AccountUser user) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", user.getUserId());
        values.put("fullName", user.getFullName());
        values.put("email", user.getEmail());
        values.put("city", user.getCity());
        values.put("phone", user.getPhone());
        values.put("createdAt", user.getCreatedAt());
        values.put("updatedAt", user.getUpdatedAt());
        return values;
    }

    private Map<String, Object> cropToMap(CropPost cropPost) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("cropPostId", cropPost.getCropPostId());
        values.put("ownerUserId", cropPost.getOwnerUserId());
        values.put("sellerName", cropPost.getSellerName());
        values.put("city", cropPost.getCity());
        values.put("phone", cropPost.getPhone());
        values.put("cropType", cropPost.getCropType());
        values.put("quantityKg", cropPost.getQuantityKg());
        values.put("pricePerKg", cropPost.getPricePerKg());
        values.put("notes", cropPost.getNotes());
        values.put("createdAt", cropPost.getCreatedAt());
        return values;
    }

    private Map<String, Object> purchaseToMap(PurchaseRequest purchaseRequest) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("purchaseRequestId", purchaseRequest.getPurchaseRequestId());
        values.put("ownerUserId", purchaseRequest.getOwnerUserId());
        values.put("buyerName", purchaseRequest.getBuyerName());
        values.put("city", purchaseRequest.getCity());
        values.put("phone", purchaseRequest.getPhone());
        values.put("cropType", purchaseRequest.getCropType());
        values.put("quantityKg", purchaseRequest.getQuantityKg());
        values.put("maxBudget", purchaseRequest.getMaxBudget());
        values.put("notes", purchaseRequest.getNotes());
        values.put("createdAt", purchaseRequest.getCreatedAt());
        return values;
    }

    private Map<String, Object> activityToMap(ActivityEntry activityEntry) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", activityEntry.getType());
        values.put("recordId", activityEntry.getRecordId());
        values.put("title", activityEntry.getTitle());
        values.put("subtitle", activityEntry.getSubtitle());
        values.put("createdAt", activityEntry.getCreatedAt());
        values.put("route", activityEntry.getRoute());
        return values;
    }

    private Map<String, Object> matchToMap(MatchRecord matchRecord) {
        String[] row = matchRecord.toTableRow();
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("buyerId", row[0]);
        values.put("buyerName", row[1]);
        values.put("farmerId", row[2]);
        values.put("farmerName", row[3]);
        values.put("cropType", row[4]);
        values.put("city", row[5]);
        values.put("availableQuantity", Double.parseDouble(row[6]));
        values.put("pricePerUnit", Double.parseDouble(row[7]));
        values.put("score", Double.parseDouble(row[8]));
        values.put("status", row[9]);
        return values;
    }
}
