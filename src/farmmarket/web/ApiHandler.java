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
            if (("/api/crops".equals(path) || "/api/listings".equals(path)) && "GET".equalsIgnoreCase(method)) {
                handleGetCrops(exchange, currentUser);
                return;
            }
            if (("/api/crops".equals(path) || "/api/listings".equals(path)) && "POST".equalsIgnoreCase(method)) {
                handleCreateCrop(exchange, currentUser);
                return;
            }
            if (("/api/purchases".equals(path) || "/api/requests".equals(path)) && "GET".equalsIgnoreCase(method)) {
                handleGetPurchases(exchange, currentUser);
                return;
            }
            if (("/api/purchases".equals(path) || "/api/requests".equals(path)) && "POST".equalsIgnoreCase(method)) {
                handleCreatePurchase(exchange, currentUser);
                return;
            }
            if ("/api/matches".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleMatches(exchange, currentUser);
                return;
            }
            if ("/api/activities".equals(path) && "GET".equalsIgnoreCase(method)) {
                handleActivities(exchange, currentUser);
                return;
            }

            sendError(exchange, 404, "NOT_FOUND", "Endpoint not found.");
        } catch (InvalidDataException exception) {
            sendError(exchange, 400, "VALIDATION_ERROR", exception.getMessage());
        } catch (SQLException exception) {
            sendError(exchange, 500, "DATABASE_ERROR", "Database error: " + exception.getMessage());
        } catch (Exception exception) {
            sendError(exchange, 500, "INTERNAL_ERROR", exception.getMessage());
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser user = database.createUser(
                values.get("fullName"),
                values.get("email"),
                values.get("city"),
                values.get("phone"),
                values.get("password"),
                values.get("role"));
        String sessionId = database.createSession(user.getUserId());
        attachSessionCookie(exchange, sessionId);

        HttpUtil.sendJson(exchange, 201, Map.of(
                "message", "Account created successfully.",
                "user", userToMap(user),
                "redirectTo", "/dashboard"));
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser user = database.authenticate(values.get("email"), values.get("password"));
        String sessionId = database.createSession(user.getUserId());
        attachSessionCookie(exchange, sessionId);

        HttpUtil.sendJson(exchange, 200, Map.of(
                "message", "Logged in successfully.",
                "user", userToMap(user),
                "redirectTo", "/dashboard"));
    }

    private void handleLogout(HttpExchange exchange) throws IOException, SQLException {
        database.deleteSession(HttpUtil.getCookie(exchange, AppConstants.SESSION_COOKIE_NAME));
        clearSessionCookie(exchange);
        HttpUtil.sendJson(exchange, 200, Map.of("message", "Logged out successfully."));
    }

    private void handleSession(HttpExchange exchange) throws IOException, SQLException {
        AccountUser user = resolveAuthenticatedUser(exchange);
        if (user == null) {
            sendError(exchange, 401, "UNAUTHENTICATED", "Please log in to continue.");
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
        response.put("catalog", Map.of(
                "cropTypes", AppConstants.CROP_TYPES,
                "units", AppConstants.SUPPORTED_UNITS,
                "statuses", AppConstants.LISTING_STATUSES));
        HttpUtil.sendJson(exchange, 200, response);
    }

    private void handleMatches(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        int limit = parseOptionalInt(query.get("limit"), 8);
        List<Map<String, Object>> matches = new ArrayList<>();
        for (MatchRecord matchRecord : database.getUserMatches(currentUser.getUserId(), limit)) {
            matches.add(matchToMap(matchRecord));
        }

        HttpUtil.sendJson(exchange, 200, Map.of("matches", matches));
    }

    private void handleActivities(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        int limit = parseOptionalInt(query.get("limit"), 12);
        List<Map<String, Object>> activity = new ArrayList<>();
        for (ActivityEntry activityEntry : database.getRecentActivity(currentUser.getUserId(), limit)) {
            activity.add(activityToMap(activityEntry));
        }

        HttpUtil.sendJson(exchange, 200, Map.of("activities", activity));
    }

    private void handleProfileUpdate(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException, InvalidDataException {
        Map<String, String> values = HttpUtil.parseFormData(HttpUtil.readBody(exchange));
        AccountUser updatedUser = database.updateUser(
                currentUser.getUserId(),
                values.get("fullName"),
                values.get("city"),
                values.get("phone"),
                values.get("role"));

        HttpUtil.sendJson(exchange, 200, Map.of(
                "message", "Profile updated successfully.",
                "user", userToMap(updatedUser)));
    }

    private void handleGetCrops(HttpExchange exchange, AccountUser currentUser) throws IOException, SQLException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        boolean ownOnly = "mine".equalsIgnoreCase(query.getOrDefault("scope", "all"));
        List<CropPost> cropPosts = database.getCropPosts(
                ownOnly ? currentUser.getUserId() : null,
                query.getOrDefault("crop", ""),
                query.getOrDefault("city", query.getOrDefault("location", "")),
                parseOptionalInteger(query.get("minQuantity")),
                parseOptionalDouble(query.get("maxPrice")),
                query.getOrDefault("status", ""));

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
                values.get("unit"),
                parseRequiredDouble(values.get("pricePerKg"), "Expected price"),
                values.get("location"),
                values.get("notes"),
                values.get("status"));

        HttpUtil.sendJson(exchange, 201, Map.of(
                "message", "Crop listing created successfully.",
                "crop", cropToMap(cropPost)));
    }

    private void handleGetPurchases(HttpExchange exchange, AccountUser currentUser)
            throws IOException, SQLException {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        boolean ownOnly = "mine".equalsIgnoreCase(query.getOrDefault("scope", "all"));
        List<PurchaseRequest> purchaseRequests = database.getPurchaseRequests(
                ownOnly ? currentUser.getUserId() : null,
                query.getOrDefault("crop", ""),
                query.getOrDefault("city", query.getOrDefault("location", "")),
                parseOptionalInteger(query.get("minQuantity")),
                parseOptionalDouble(query.get("maxBudget")),
                query.getOrDefault("status", ""));

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
                values.get("unit"),
                parseRequiredDouble(values.get("maxBudget"), "Budget"),
                values.get("location"),
                values.get("notes"),
                values.get("status"));

        HttpUtil.sendJson(exchange, 201, Map.of(
                "message", "Purchase request created successfully.",
                "purchase", purchaseToMap(purchaseRequest)));
    }

    private AccountUser requireAuthenticated(HttpExchange exchange) throws IOException, SQLException {
        AccountUser currentUser = resolveAuthenticatedUser(exchange);
        if (currentUser == null) {
            clearSessionCookie(exchange);
            sendError(exchange, 401, "UNAUTHENTICATED", "Please log in to continue.");
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

    private int parseOptionalInt(String rawValue, int fallback) {
        try {
            return rawValue == null || rawValue.isBlank() ? fallback : Integer.parseInt(rawValue);
        } catch (Exception exception) {
            return fallback;
        }
    }

    private Integer parseOptionalInteger(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : Integer.valueOf(rawValue);
        } catch (Exception exception) {
            return null;
        }
    }

    private double parseRequiredDouble(String rawValue, String fieldName) throws InvalidDataException {
        try {
            return Double.parseDouble(rawValue);
        } catch (Exception exception) {
            throw new InvalidDataException(fieldName + " must be a valid number.");
        }
    }

    private Double parseOptionalDouble(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : Double.valueOf(rawValue);
        } catch (Exception exception) {
            return null;
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String code, String message) throws IOException {
        HttpUtil.sendJson(exchange, statusCode, Map.of(
                "error", Map.of(
                        "code", code,
                        "message", message)));
    }

    private Map<String, Object> userToMap(AccountUser user) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("userId", user.getUserId());
        values.put("fullName", user.getFullName());
        values.put("email", user.getEmail());
        values.put("city", user.getCity());
        values.put("location", user.getLocation());
        values.put("phone", user.getPhone());
        values.put("role", user.getRole());
        values.put("initials", buildInitials(user.getFullName()));
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
        values.put("location", cropPost.getLocation());
        values.put("phone", cropPost.getPhone());
        values.put("cropType", cropPost.getCropType());
        values.put("quantityKg", cropPost.getQuantityKg());
        values.put("unit", cropPost.getUnit());
        values.put("pricePerKg", cropPost.getPricePerKg());
        values.put("notes", cropPost.getNotes());
        values.put("status", cropPost.getStatus());
        values.put("createdAt", cropPost.getCreatedAt());
        return values;
    }

    private Map<String, Object> purchaseToMap(PurchaseRequest purchaseRequest) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("purchaseRequestId", purchaseRequest.getPurchaseRequestId());
        values.put("ownerUserId", purchaseRequest.getOwnerUserId());
        values.put("buyerName", purchaseRequest.getBuyerName());
        values.put("city", purchaseRequest.getCity());
        values.put("location", purchaseRequest.getLocation());
        values.put("phone", purchaseRequest.getPhone());
        values.put("cropType", purchaseRequest.getCropType());
        values.put("quantityKg", purchaseRequest.getQuantityKg());
        values.put("unit", purchaseRequest.getUnit());
        values.put("maxBudget", purchaseRequest.getMaxBudget());
        values.put("notes", purchaseRequest.getNotes());
        values.put("status", purchaseRequest.getStatus());
        values.put("createdAt", purchaseRequest.getCreatedAt());
        return values;
    }

    private Map<String, Object> activityToMap(ActivityEntry activityEntry) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("type", activityEntry.getType());
        values.put("recordId", activityEntry.getRecordId());
        values.put("title", activityEntry.getTitle());
        values.put("subtitle", activityEntry.getSubtitle());
        values.put("status", activityEntry.getStatus());
        values.put("actionLabel", activityEntry.getActionLabel());
        values.put("createdAt", activityEntry.getCreatedAt());
        values.put("route", activityEntry.getRoute());
        return values;
    }

    private Map<String, Object> matchToMap(MatchRecord matchRecord) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("buyerId", matchRecord.getBuyerId());
        values.put("buyerName", matchRecord.getBuyerName());
        values.put("farmerId", matchRecord.getFarmerId());
        values.put("farmerName", matchRecord.getFarmerName());
        values.put("cropType", matchRecord.getCropType());
        values.put("location", matchRecord.getCity());
        values.put("availableQuantity", matchRecord.getAvailableQuantity());
        values.put("requestedQuantity", matchRecord.getRequestedQuantity());
        values.put("matchedQuantity", matchRecord.getMatchedQuantity());
        values.put("pricePerUnit", matchRecord.getPricePerUnit());
        values.put("buyerBudget", matchRecord.getBuyerBudget());
        values.put("score", matchRecord.getScore());
        values.put("status", matchRecord.getStatus());
        values.put("ctaLabel", "View Match");
        values.put("route", "/dashboard");
        return values;
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
            if (builder.length() == 2) {
                break;
            }
        }
        return builder.isEmpty() ? "U" : builder.toString();
    }
}
