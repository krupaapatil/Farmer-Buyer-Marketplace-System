package farmmarket.util;

public final class AppConstants {
    public static final String[] CROP_TYPES = {
        "Wheat", "Rice", "Cotton", "Sugarcane", "Maize", "Tomato", "Potato", "Onion"
    };
    public static final String[] USER_ROLES = {
        "farmer", "buyer", "both"
    };
    public static final String[] LISTING_STATUSES = {
        "active", "fulfilled", "expired"
    };
    public static final String[] SUPPORTED_UNITS = {
        "kg", "quintal", "ton"
    };

    public static final String DATA_DIRECTORY = "data";
    public static final String DATABASE_FILE = "marketplace.db";
    public static final String FARMERS_FILE = "farmers.csv";
    public static final String BUYERS_FILE = "buyers.csv";
    public static final String MATCHES_FILE = "matches.csv";
    public static final String BACKUP_FILE = "marketplace-backup.dat";
    public static final String SESSION_COOKIE_NAME = "farmmarket_session";
    public static final int SESSION_DURATION_DAYS = 14;
    public static final String DEFAULT_USER_ROLE = "both";
    public static final String DEFAULT_UNIT = "kg";
    public static final String DEFAULT_STATUS = "active";

    private AppConstants() {
    }
}
