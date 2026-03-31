package farmmarket.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.model.AccountUser;
import farmmarket.model.ActivityEntry;
import farmmarket.model.Buyer;
import farmmarket.model.CropPost;
import farmmarket.model.DashboardSnapshot;
import farmmarket.model.Farmer;
import farmmarket.model.MatchRecord;
import farmmarket.model.PurchaseRequest;
import farmmarket.util.AppConstants;
import farmmarket.util.SecurityUtil;
import farmmarket.util.ValidationUtil;

public class MarketplaceDatabase {
    private final Path databasePath;
    private final FileManager fileManager;
    private final String databaseUrl;

    public MarketplaceDatabase(FileManager fileManager) {
        this(Paths.get(AppConstants.DATA_DIRECTORY).resolve(AppConstants.DATABASE_FILE), fileManager);
    }

    public MarketplaceDatabase(Path databasePath, FileManager fileManager) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
        this.fileManager = fileManager;
        this.databaseUrl = "jdbc:sqlite:" + this.databasePath;
    }

    public void initialize() throws IOException, SQLException {
        loadDriver();
        Files.createDirectories(databasePath.getParent());
        try (Connection connection = openConnection()) {
            createTables(connection);
            cleanupExpiredSessions(connection);
        }
        seedLegacyMarketplaceDataIfEmpty();
    }

    public AccountUser createUser(String fullName, String email, String city, String phone, String password)
            throws SQLException, InvalidDataException {
        return createUser(fullName, email, city, phone, password, AppConstants.DEFAULT_USER_ROLE);
    }

    public AccountUser createUser(String fullName, String email, String city, String phone, String password,
            String role) throws SQLException, InvalidDataException {
        String sanitizedName = ValidationUtil.requireNonEmpty(fullName, "Full name");
        String sanitizedEmail = ValidationUtil.requireEmail(email, "Email");
        String sanitizedCity = ValidationUtil.requireNonEmpty(city, "Location");
        String sanitizedPhone = ValidationUtil.requirePhone(phone, "Phone");
        String sanitizedRole = ValidationUtil.requireRole(role);
        SecurityUtil.PasswordHash passwordHash = SecurityUtil.hashPassword(password);
        String userId = generateUniqueId("USR", "users", "user_id");
        String timestamp = Instant.now().toString();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        INSERT INTO users
                        (user_id, full_name, email, city, phone, role, password_hash, password_salt, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, userId);
            statement.setString(2, sanitizedName);
            statement.setString(3, sanitizedEmail);
            statement.setString(4, sanitizedCity);
            statement.setString(5, sanitizedPhone);
            statement.setString(6, sanitizedRole);
            statement.setString(7, passwordHash.hash());
            statement.setString(8, passwordHash.salt());
            statement.setString(9, timestamp);
            statement.setString(10, timestamp);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (isUniqueConstraintViolation(exception)) {
                throw new InvalidDataException("An account with this email already exists.");
            }
            throw exception;
        }

        return getUserById(userId);
    }

    public AccountUser authenticate(String email, String password) throws SQLException, InvalidDataException {
        String sanitizedEmail = ValidationUtil.requireEmail(email, "Email");
        ValidationUtil.requireMinimumLength(password, 8, "Password");

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT user_id, full_name, email, city, phone, role, password_hash, password_salt, created_at, updated_at
                        FROM users
                        WHERE email = ?
                        """)) {
            statement.setString(1, sanitizedEmail);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new InvalidDataException("Incorrect email or password.");
                }

                if (!SecurityUtil.verifyPassword(password,
                        resultSet.getString("password_hash"),
                        resultSet.getString("password_salt"))) {
                    throw new InvalidDataException("Incorrect email or password.");
                }

                return mapUser(resultSet);
            }
        }
    }

    public String createSession(String userId) throws SQLException {
        String sessionId = SecurityUtil.generateSessionToken();
        String createdAt = Instant.now().toString();
        String expiresAt = Instant.now().plus(AppConstants.SESSION_DURATION_DAYS, ChronoUnit.DAYS).toString();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO sessions (session_id, user_id, created_at, expires_at) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, sessionId);
            statement.setString(2, userId);
            statement.setString(3, createdAt);
            statement.setString(4, expiresAt);
            statement.executeUpdate();
        }

        return sessionId;
    }

    public AccountUser findUserBySession(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        try (Connection connection = openConnection()) {
            cleanupExpiredSessions(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    """
                    SELECT users.user_id, users.full_name, users.email, users.city, users.phone, users.role,
                           users.created_at, users.updated_at
                    FROM sessions
                    JOIN users ON users.user_id = sessions.user_id
                    WHERE sessions.session_id = ?
                    """)) {
                statement.setString(1, sessionId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return mapUser(resultSet);
                }
            }
        }
    }

    public void deleteSession(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM sessions WHERE session_id = ?")) {
            statement.setString(1, sessionId);
            statement.executeUpdate();
        }
    }

    public AccountUser getUserById(String userId) throws SQLException {
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT user_id, full_name, email, city, phone, role, created_at, updated_at
                        FROM users
                        WHERE user_id = ?
                        """)) {
            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapUser(resultSet);
            }
        }
    }

    public AccountUser updateUser(String userId, String fullName, String city, String phone)
            throws SQLException, InvalidDataException {
        AccountUser currentUser = requireUser(userId);
        return updateUser(userId, fullName, city, phone, currentUser.getRole());
    }

    public AccountUser updateUser(String userId, String fullName, String city, String phone, String role)
            throws SQLException, InvalidDataException {
        String sanitizedName = ValidationUtil.requireNonEmpty(fullName, "Full name");
        String sanitizedCity = ValidationUtil.requireNonEmpty(city, "Location");
        String sanitizedPhone = ValidationUtil.requirePhone(phone, "Phone");
        String sanitizedRole = ValidationUtil.requireRole(role);
        String timestamp = Instant.now().toString();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        UPDATE users
                        SET full_name = ?, city = ?, phone = ?, role = ?, updated_at = ?
                        WHERE user_id = ?
                        """)) {
            statement.setString(1, sanitizedName);
            statement.setString(2, sanitizedCity);
            statement.setString(3, sanitizedPhone);
            statement.setString(4, sanitizedRole);
            statement.setString(5, timestamp);
            statement.setString(6, userId);
            statement.executeUpdate();
        }

        return getUserById(userId);
    }

    public CropPost createCropPost(String userId, String cropType, int quantityKg, double pricePerKg, String notes)
            throws SQLException, InvalidDataException {
        return createCropPost(userId, cropType, quantityKg, AppConstants.DEFAULT_UNIT, pricePerKg, null, notes,
                AppConstants.DEFAULT_STATUS);
    }

    public CropPost createCropPost(String userId, String cropType, int quantityKg, String unit, double pricePerKg,
            String location, String notes, String status) throws SQLException, InvalidDataException {
        AccountUser user = requireUser(userId);
        requireRoleAccess(user, "buyer", "Buyer-only accounts cannot create crop listings.");
        String sanitizedCropType = ValidationUtil.requireCropType(cropType);
        int sanitizedQuantity = ValidationUtil.requirePositiveInt(quantityKg, "Quantity");
        String sanitizedUnit = ValidationUtil.requireUnit(unit);
        double sanitizedPrice = ValidationUtil.requirePositiveDouble(pricePerKg, "Expected price");
        String sanitizedLocation = ValidationUtil.requireNonEmpty(
                location == null || location.isBlank() ? user.getLocation() : location,
                "Location");
        String sanitizedStatus = ValidationUtil.requireStatus(status);
        String cropPostId = generateUniqueId("CRP", "crop_posts", "crop_post_id");
        String timestamp = Instant.now().toString();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        INSERT INTO crop_posts
                        (crop_post_id, owner_user_id, seller_name, city, phone, crop_type, quantity_kg, unit,
                         price_per_kg, notes, status, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, cropPostId);
            statement.setString(2, user.getUserId());
            statement.setString(3, user.getFullName());
            statement.setString(4, sanitizedLocation);
            statement.setString(5, user.getPhone());
            statement.setString(6, sanitizedCropType);
            statement.setInt(7, sanitizedQuantity);
            statement.setString(8, sanitizedUnit);
            statement.setDouble(9, sanitizedPrice);
            statement.setString(10, ValidationUtil.optionalText(notes, 500));
            statement.setString(11, sanitizedStatus);
            statement.setString(12, timestamp);
            statement.executeUpdate();
        }

        return getCropPostById(cropPostId);
    }

    public PurchaseRequest createPurchaseRequest(String userId, String cropType, int quantityKg, double maxBudget,
            String notes) throws SQLException, InvalidDataException {
        return createPurchaseRequest(userId, cropType, quantityKg, AppConstants.DEFAULT_UNIT, maxBudget, null, notes,
                AppConstants.DEFAULT_STATUS);
    }

    public PurchaseRequest createPurchaseRequest(String userId, String cropType, int quantityKg, String unit,
            double maxBudget, String location, String notes, String status)
            throws SQLException, InvalidDataException {
        AccountUser user = requireUser(userId);
        requireRoleAccess(user, "farmer", "Farmer-only accounts cannot create purchase requests.");
        String sanitizedCropType = ValidationUtil.requireCropType(cropType);
        int sanitizedQuantity = ValidationUtil.requirePositiveInt(quantityKg, "Required quantity");
        String sanitizedUnit = ValidationUtil.requireUnit(unit);
        double sanitizedBudget = ValidationUtil.requirePositiveDouble(maxBudget, "Budget");
        String sanitizedLocation = ValidationUtil.requireNonEmpty(
                location == null || location.isBlank() ? user.getLocation() : location,
                "Location");
        String sanitizedStatus = ValidationUtil.requireStatus(status);
        String requestId = generateUniqueId("REQ", "purchase_requests", "purchase_request_id");
        String timestamp = Instant.now().toString();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        INSERT INTO purchase_requests
                        (purchase_request_id, owner_user_id, buyer_name, city, phone, crop_type, quantity_kg, unit,
                         max_budget, notes, status, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, requestId);
            statement.setString(2, user.getUserId());
            statement.setString(3, user.getFullName());
            statement.setString(4, sanitizedLocation);
            statement.setString(5, user.getPhone());
            statement.setString(6, sanitizedCropType);
            statement.setInt(7, sanitizedQuantity);
            statement.setString(8, sanitizedUnit);
            statement.setDouble(9, sanitizedBudget);
            statement.setString(10, ValidationUtil.optionalText(notes, 500));
            statement.setString(11, sanitizedStatus);
            statement.setString(12, timestamp);
            statement.executeUpdate();
        }

        return getPurchaseRequestById(requestId);
    }

    public List<CropPost> getCropPosts(String ownerUserId, String cropType, String city) throws SQLException {
        return getCropPosts(ownerUserId, cropType, city, null, null, "");
    }

    public List<CropPost> getCropPosts(String ownerUserId, String cropType, String city, Integer minQuantity,
            Double maxPrice, String status) throws SQLException {
        List<CropPost> cropPosts = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                """
                SELECT crop_post_id, owner_user_id, seller_name, city, phone, crop_type, quantity_kg, unit,
                       price_per_kg, notes, status, created_at
                FROM crop_posts
                WHERE 1 = 1
                """);

        if (ownerUserId != null && !ownerUserId.isBlank()) {
            query.append(" AND owner_user_id = ?");
            parameters.add(ownerUserId);
        }
        if (cropType != null && !cropType.isBlank()) {
            query.append(" AND LOWER(crop_type) = ?");
            parameters.add(cropType.trim().toLowerCase(Locale.ENGLISH));
        }
        if (city != null && !city.isBlank()) {
            query.append(" AND LOWER(city) = ?");
            parameters.add(city.trim().toLowerCase(Locale.ENGLISH));
        }
        if (minQuantity != null) {
            query.append(" AND quantity_kg >= ?");
            parameters.add(minQuantity);
        }
        if (maxPrice != null) {
            query.append(" AND price_per_kg <= ?");
            parameters.add(maxPrice);
        }
        if (status != null && !status.isBlank()) {
            query.append(" AND LOWER(status) = ?");
            parameters.add(status.trim().toLowerCase(Locale.ENGLISH));
        }
        query.append(" ORDER BY created_at DESC");

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cropPosts.add(mapCropPost(resultSet));
                }
            }
        }

        return cropPosts;
    }

    public List<PurchaseRequest> getPurchaseRequests(String ownerUserId, String cropType, String city)
            throws SQLException {
        return getPurchaseRequests(ownerUserId, cropType, city, null, null, "");
    }

    public List<PurchaseRequest> getPurchaseRequests(String ownerUserId, String cropType, String city,
            Integer minQuantity, Double maxBudget, String status) throws SQLException {
        List<PurchaseRequest> purchaseRequests = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                """
                SELECT purchase_request_id, owner_user_id, buyer_name, city, phone, crop_type, quantity_kg, unit,
                       max_budget, notes, status, created_at
                FROM purchase_requests
                WHERE 1 = 1
                """);

        if (ownerUserId != null && !ownerUserId.isBlank()) {
            query.append(" AND owner_user_id = ?");
            parameters.add(ownerUserId);
        }
        if (cropType != null && !cropType.isBlank()) {
            query.append(" AND LOWER(crop_type) = ?");
            parameters.add(cropType.trim().toLowerCase(Locale.ENGLISH));
        }
        if (city != null && !city.isBlank()) {
            query.append(" AND LOWER(city) = ?");
            parameters.add(city.trim().toLowerCase(Locale.ENGLISH));
        }
        if (minQuantity != null) {
            query.append(" AND quantity_kg >= ?");
            parameters.add(minQuantity);
        }
        if (maxBudget != null) {
            query.append(" AND max_budget <= ?");
            parameters.add(maxBudget);
        }
        if (status != null && !status.isBlank()) {
            query.append(" AND LOWER(status) = ?");
            parameters.add(status.trim().toLowerCase(Locale.ENGLISH));
        }
        query.append(" ORDER BY created_at DESC");

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(query.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    purchaseRequests.add(mapPurchaseRequest(resultSet));
                }
            }
        }

        return purchaseRequests;
    }

    public DashboardSnapshot buildDashboard(String userId) throws SQLException, InvalidDataException {
        AccountUser user = requireUser(userId);
        List<CropPost> ownCropPosts = getCropPosts(userId, "", "", null, null, AppConstants.DEFAULT_STATUS);
        List<PurchaseRequest> ownPurchaseRequests = getPurchaseRequests(userId, "", "", null, null,
                AppConstants.DEFAULT_STATUS);
        List<CropPost> allCropPosts = getCropPosts(null, "", "", null, null, AppConstants.DEFAULT_STATUS);
        List<PurchaseRequest> allPurchaseRequests = getPurchaseRequests(null, "", "", null, null,
                AppConstants.DEFAULT_STATUS);
        List<ActivityEntry> recentActivity = getRecentActivity(userId, 6);
        List<MatchRecord> topMatches = getUserMatches(userId, 5);

        return new DashboardSnapshot(
                user,
                ownCropPosts.size(),
                ownPurchaseRequests.size(),
                allCropPosts.size(),
                allPurchaseRequests.size(),
                topMatches.size(),
                recentActivity,
                topMatches);
    }

    public List<ActivityEntry> getRecentActivity(String userId, int limit) throws SQLException, InvalidDataException {
        requireUser(userId);
        List<CropPost> ownCropPosts = getCropPosts(userId, "", "", null, null, "");
        List<PurchaseRequest> ownPurchaseRequests = getPurchaseRequests(userId, "", "", null, null, "");
        return buildRecentActivity(ownCropPosts, ownPurchaseRequests, limit);
    }

    public List<MatchRecord> getUserMatches(String userId, int limit) throws SQLException, InvalidDataException {
        requireUser(userId);
        List<CropPost> allCropPosts = getCropPosts(null, "", "", null, null, AppConstants.DEFAULT_STATUS);
        List<PurchaseRequest> allPurchaseRequests = getPurchaseRequests(null, "", "", null, null,
                AppConstants.DEFAULT_STATUS);
        return buildUserMatches(userId, allCropPosts, allPurchaseRequests, limit);
    }

    private AccountUser requireUser(String userId) throws SQLException, InvalidDataException {
        AccountUser user = getUserById(userId);
        if (user == null) {
            throw new InvalidDataException("User account could not be found.");
        }
        return user;
    }

    private void requireRoleAccess(AccountUser user, String blockedRole, String message) throws InvalidDataException {
        if (user.getRole() != null && user.getRole().equalsIgnoreCase(blockedRole)) {
            throw new InvalidDataException(message);
        }
    }

    private CropPost getCropPostById(String cropPostId) throws SQLException {
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT crop_post_id, owner_user_id, seller_name, city, phone, crop_type, quantity_kg, unit,
                               price_per_kg, notes, status, created_at
                        FROM crop_posts
                        WHERE crop_post_id = ?
                        """)) {
            statement.setString(1, cropPostId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapCropPost(resultSet);
            }
        }
    }

    private PurchaseRequest getPurchaseRequestById(String purchaseRequestId) throws SQLException {
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT purchase_request_id, owner_user_id, buyer_name, city, phone, crop_type, quantity_kg, unit,
                               max_budget, notes, status, created_at
                        FROM purchase_requests
                        WHERE purchase_request_id = ?
                        """)) {
            statement.setString(1, purchaseRequestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapPurchaseRequest(resultSet);
            }
        }
    }

    private List<ActivityEntry> buildRecentActivity(List<CropPost> ownCropPosts,
            List<PurchaseRequest> ownPurchaseRequests, int limit) {
        List<ActivityEntry> activityEntries = new ArrayList<>();

        for (CropPost cropPost : ownCropPosts) {
            activityEntries.add(new ActivityEntry(
                    "crop-post",
                    cropPost.getCropPostId(),
                    "Added " + cropPost.getCropType() + " listing",
                    cropPost.getQuantityKg() + " " + cropPost.getUnit() + " at Rs. "
                            + String.format(Locale.ENGLISH, "%.2f", cropPost.getPricePerKg()) + " per "
                            + cropPost.getUnit(),
                    cropPost.getStatus(),
                    "View listing",
                    cropPost.getCreatedAt(),
                    "/add-crops"));
        }

        for (PurchaseRequest purchaseRequest : ownPurchaseRequests) {
            activityEntries.add(new ActivityEntry(
                    "purchase-request",
                    purchaseRequest.getPurchaseRequestId(),
                    "Requested " + purchaseRequest.getCropType(),
                    purchaseRequest.getQuantityKg() + " " + purchaseRequest.getUnit() + " up to Rs. "
                            + String.format(Locale.ENGLISH, "%.2f", purchaseRequest.getMaxBudget()) + " per "
                            + purchaseRequest.getUnit(),
                    purchaseRequest.getStatus(),
                    "Review request",
                    purchaseRequest.getCreatedAt(),
                    "/buy-crops"));
        }

        activityEntries.sort(Comparator.comparing(ActivityEntry::getCreatedAt).reversed());
        int safeLimit = Math.max(1, limit);
        if (activityEntries.size() <= safeLimit) {
            return activityEntries;
        }
        return new ArrayList<>(activityEntries.subList(0, safeLimit));
    }

    private List<MatchRecord> buildUserMatches(String userId, List<CropPost> cropPosts,
            List<PurchaseRequest> purchaseRequests, int limit) throws InvalidDataException {
        List<MatchRecord> matches = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (PurchaseRequest purchaseRequest : purchaseRequests) {
            if (!AppConstants.DEFAULT_STATUS.equalsIgnoreCase(purchaseRequest.getStatus())) {
                continue;
            }
            for (CropPost cropPost : cropPosts) {
                if (!AppConstants.DEFAULT_STATUS.equalsIgnoreCase(cropPost.getStatus())) {
                    continue;
                }
                if (purchaseRequest.getOwnerUserId() == null && cropPost.getOwnerUserId() == null) {
                    continue;
                }

                boolean userInvolved = userId.equals(purchaseRequest.getOwnerUserId())
                        || userId.equals(cropPost.getOwnerUserId());
                if (!userInvolved) {
                    continue;
                }

                if (userId.equals(purchaseRequest.getOwnerUserId())
                        && userId.equals(cropPost.getOwnerUserId())) {
                    continue;
                }

                Farmer farmer = toFarmer(cropPost);
                Buyer buyer = toBuyer(purchaseRequest);
                if (!farmer.isCompatibleWith(buyer)) {
                    continue;
                }

                String pairKey = buyer.getId() + "|" + farmer.getId();
                if (!seenPairs.add(pairKey)) {
                    continue;
                }

                double score = calculateMatchScore(farmer, buyer);
                String status = score >= 92 ? "high confidence" : "good fit";
                matches.add(new MatchRecord(buyer, farmer, score, status));
            }
        }

        matches.sort(Comparator.naturalOrder());
        int safeLimit = Math.max(1, limit);
        if (matches.size() <= safeLimit) {
            return matches;
        }
        return new ArrayList<>(matches.subList(0, safeLimit));
    }

    private double calculateMatchScore(Farmer farmer, Buyer buyer) {
        double score = farmer.calculateMatchScore(buyer);
        double budgetGap = Math.max(0, buyer.getMaxBudget() - farmer.getPricePerUnit());
        if (buyer.getMaxBudget() > 0) {
            double priceAlignment = Math.min(10, (budgetGap / buyer.getMaxBudget()) * 10);
            score += priceAlignment;
        }
        return Math.round(Math.min(99.5, score) * 100.0) / 100.0;
    }

    private Farmer toFarmer(CropPost cropPost) throws InvalidDataException {
        return new Farmer(
                cropPost.getCropPostId(),
                cropPost.getSellerName(),
                cropPost.getCity(),
                cropPost.getPhone(),
                cropPost.getCropType(),
                cropPost.getQuantityKg(),
                cropPost.getPricePerKg());
    }

    private Buyer toBuyer(PurchaseRequest purchaseRequest) throws InvalidDataException {
        return new Buyer(
                purchaseRequest.getPurchaseRequestId(),
                purchaseRequest.getBuyerName(),
                purchaseRequest.getCity(),
                purchaseRequest.getPhone(),
                purchaseRequest.getCropType(),
                purchaseRequest.getQuantityKg(),
                purchaseRequest.getMaxBudget());
    }

    private void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("SQLite JDBC driver is missing from the classpath.", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS users (
                        user_id TEXT PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        city TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'both',
                        password_hash TEXT NOT NULL,
                        password_salt TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS sessions (
                        session_id TEXT PRIMARY KEY,
                        user_id TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        expires_at TEXT NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS crop_posts (
                        crop_post_id TEXT PRIMARY KEY,
                        owner_user_id TEXT,
                        seller_name TEXT NOT NULL,
                        city TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        crop_type TEXT NOT NULL,
                        quantity_kg INTEGER NOT NULL,
                        unit TEXT NOT NULL DEFAULT 'kg',
                        price_per_kg REAL NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'active',
                        created_at TEXT NOT NULL,
                        FOREIGN KEY(owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL
                    )
                    """);
            statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS purchase_requests (
                        purchase_request_id TEXT PRIMARY KEY,
                        owner_user_id TEXT,
                        buyer_name TEXT NOT NULL,
                        city TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        crop_type TEXT NOT NULL,
                        quantity_kg INTEGER NOT NULL,
                        unit TEXT NOT NULL DEFAULT 'kg',
                        max_budget REAL NOT NULL,
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'active',
                        created_at TEXT NOT NULL,
                        FOREIGN KEY(owner_user_id) REFERENCES users(user_id) ON DELETE SET NULL
                    )
                    """);
        }

        ensureColumn(connection, "users", "role", "TEXT NOT NULL DEFAULT 'both'");
        ensureColumn(connection, "crop_posts", "unit", "TEXT NOT NULL DEFAULT 'kg'");
        ensureColumn(connection, "crop_posts", "status", "TEXT NOT NULL DEFAULT 'active'");
        ensureColumn(connection, "purchase_requests", "unit", "TEXT NOT NULL DEFAULT 'kg'");
        ensureColumn(connection, "purchase_requests", "status", "TEXT NOT NULL DEFAULT 'active'");

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_crop_posts_search "
                            + "ON crop_posts (crop_type, city, status, quantity_kg, price_per_kg)");
            statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_purchase_requests_search "
                            + "ON purchase_requests (crop_type, city, status, quantity_kg, max_budget)");
        }
    }

    private void ensureColumn(Connection connection, String tableName, String columnName, String definition)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(" + tableName + ")");
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement alterStatement = connection.createStatement()) {
            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void cleanupExpiredSessions(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM sessions WHERE expires_at <= ?")) {
            statement.setString(1, Instant.now().toString());
            statement.executeUpdate();
        }
    }

    private void seedLegacyMarketplaceDataIfEmpty() {
        try (Connection connection = openConnection()) {
            if (tableHasRows(connection, "crop_posts") || tableHasRows(connection, "purchase_requests")) {
                return;
            }

            List<farmmarket.model.Farmer> farmers = fileManager.loadFarmers();
            List<farmmarket.model.Buyer> buyers = fileManager.loadBuyers();
            connection.setAutoCommit(false);
            try (PreparedStatement cropStatement = connection.prepareStatement(
                    """
                    INSERT INTO crop_posts
                    (crop_post_id, owner_user_id, seller_name, city, phone, crop_type, quantity_kg, unit,
                     price_per_kg, notes, status, created_at)
                    VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);
                    PreparedStatement purchaseStatement = connection.prepareStatement(
                            """
                            INSERT INTO purchase_requests
                            (purchase_request_id, owner_user_id, buyer_name, city, phone, crop_type, quantity_kg, unit,
                             max_budget, notes, status, created_at)
                            VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """)) {
                String timestamp = Instant.now().toString();
                for (farmmarket.model.Farmer farmer : farmers) {
                    cropStatement.setString(1, farmer.getId());
                    cropStatement.setString(2, farmer.getName());
                    cropStatement.setString(3, farmer.getCity());
                    cropStatement.setString(4, farmer.getPhone());
                    cropStatement.setString(5, farmer.getCropType());
                    cropStatement.setInt(6, farmer.getQuantityAvailable());
                    cropStatement.setString(7, AppConstants.DEFAULT_UNIT);
                    cropStatement.setDouble(8, farmer.getPricePerUnit());
                    cropStatement.setString(9, "Imported marketplace seed data");
                    cropStatement.setString(10, AppConstants.DEFAULT_STATUS);
                    cropStatement.setString(11, timestamp);
                    cropStatement.addBatch();
                }
                for (farmmarket.model.Buyer buyer : buyers) {
                    purchaseStatement.setString(1, buyer.getId());
                    purchaseStatement.setString(2, buyer.getName());
                    purchaseStatement.setString(3, buyer.getCity());
                    purchaseStatement.setString(4, buyer.getPhone());
                    purchaseStatement.setString(5, buyer.getRequiredCrop());
                    purchaseStatement.setInt(6, buyer.getRequiredQuantity());
                    purchaseStatement.setString(7, AppConstants.DEFAULT_UNIT);
                    purchaseStatement.setDouble(8, buyer.getMaxBudget());
                    purchaseStatement.setString(9, "Imported marketplace seed data");
                    purchaseStatement.setString(10, AppConstants.DEFAULT_STATUS);
                    purchaseStatement.setString(11, timestamp);
                    purchaseStatement.addBatch();
                }
                cropStatement.executeBatch();
                purchaseStatement.executeBatch();
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception exception) {
            System.err.println("Database seed skipped: " + exception.getMessage());
        }
    }

    private boolean tableHasRows(Connection connection, String tableName) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            int position = index + 1;
            if (parameter instanceof Integer integer) {
                statement.setInt(position, integer);
            } else if (parameter instanceof Double number) {
                statement.setDouble(position, number);
            } else {
                statement.setString(position, String.valueOf(parameter));
            }
        }
    }

    private String generateUniqueId(String prefix, String tableName, String columnName) throws SQLException {
        while (true) {
            String candidate = SecurityUtil.generateId(prefix);
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "SELECT 1 FROM " + tableName + " WHERE " + columnName + " = ? LIMIT 1")) {
                statement.setString(1, candidate);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return candidate;
                    }
                }
            }
        }
    }

    private boolean isUniqueConstraintViolation(SQLException exception) {
        return exception.getMessage() != null
                && exception.getMessage().toLowerCase(Locale.ENGLISH).contains("unique");
    }

    private AccountUser mapUser(ResultSet resultSet) throws SQLException {
        return new AccountUser(
                resultSet.getString("user_id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("city"),
                resultSet.getString("phone"),
                resultSet.getString("role"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at"));
    }

    private CropPost mapCropPost(ResultSet resultSet) throws SQLException {
        return new CropPost(
                resultSet.getString("crop_post_id"),
                resultSet.getString("owner_user_id"),
                resultSet.getString("seller_name"),
                resultSet.getString("city"),
                resultSet.getString("phone"),
                resultSet.getString("crop_type"),
                resultSet.getInt("quantity_kg"),
                resultSet.getString("unit"),
                resultSet.getDouble("price_per_kg"),
                resultSet.getString("notes"),
                resultSet.getString("status"),
                resultSet.getString("created_at"));
    }

    private PurchaseRequest mapPurchaseRequest(ResultSet resultSet) throws SQLException {
        return new PurchaseRequest(
                resultSet.getString("purchase_request_id"),
                resultSet.getString("owner_user_id"),
                resultSet.getString("buyer_name"),
                resultSet.getString("city"),
                resultSet.getString("phone"),
                resultSet.getString("crop_type"),
                resultSet.getInt("quantity_kg"),
                resultSet.getString("unit"),
                resultSet.getDouble("max_budget"),
                resultSet.getString("notes"),
                resultSet.getString("status"),
                resultSet.getString("created_at"));
    }
}
