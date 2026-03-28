package farmmarket.util;

public final class AppConstants {
    public static final String[] CROP_TYPES = {
        "Wheat", "Rice", "Cotton", "Sugarcane", "Maize", "Tomato", "Potato", "Onion"
    };

    public static final String DATA_DIRECTORY = "data";
    public static final String FARMERS_FILE = "farmers.csv";
    public static final String BUYERS_FILE = "buyers.csv";
    public static final String MATCHES_FILE = "matches.csv";
    public static final String BACKUP_FILE = "marketplace-backup.dat";

    private AppConstants() {
    }
}
