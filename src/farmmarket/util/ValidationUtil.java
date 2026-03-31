package farmmarket.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import farmmarket.exceptions.InvalidDataException;

public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private ValidationUtil() {
    }

    public static String requireNonEmpty(String value, String fieldName) throws InvalidDataException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidDataException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static double requireNonNegativeDouble(double value, String fieldName) throws InvalidDataException {
        if (value < 0) {
            throw new InvalidDataException(fieldName + " cannot be negative.");
        }
        return value;
    }

    public static int requireNonNegativeInt(int value, String fieldName) throws InvalidDataException {
        if (value < 0) {
            throw new InvalidDataException(fieldName + " cannot be negative.");
        }
        return value;
    }

    public static int requirePositiveInt(int value, String fieldName) throws InvalidDataException {
        if (value <= 0) {
            throw new InvalidDataException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    public static String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public static boolean matchesIgnoreCase(String left, String right) {
        return normalizeText(left).equals(normalizeText(right));
    }

    public static String requireEmail(String value, String fieldName) throws InvalidDataException {
        String email = requireNonEmpty(value, fieldName).toLowerCase(Locale.ENGLISH);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidDataException(fieldName + " is not valid.");
        }
        return email;
    }

    public static String requirePhone(String value, String fieldName) throws InvalidDataException {
        String phone = requireNonEmpty(value, fieldName);
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidDataException(fieldName + " is not valid.");
        }
        return phone;
    }

    public static String requireMinimumLength(String value, int minLength, String fieldName)
            throws InvalidDataException {
        String sanitizedValue = requireNonEmpty(value, fieldName);
        if (sanitizedValue.length() < minLength) {
            throw new InvalidDataException(fieldName + " must be at least " + minLength + " characters.");
        }
        return sanitizedValue;
    }

    public static double requirePositiveDouble(double value, String fieldName) throws InvalidDataException {
        if (value <= 0) {
            throw new InvalidDataException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    public static String requireAllowedValue(String value, String fieldName, String[] allowedValues)
            throws InvalidDataException {
        String sanitizedValue = requireNonEmpty(value, fieldName).toLowerCase(Locale.ENGLISH);
        boolean allowed = Arrays.stream(allowedValues)
                .anyMatch(candidate -> candidate.equalsIgnoreCase(sanitizedValue));
        if (!allowed) {
            throw new InvalidDataException(fieldName + " is not supported.");
        }
        return sanitizedValue;
    }

    public static String requireCropType(String value) throws InvalidDataException {
        String sanitizedValue = requireNonEmpty(value, "Crop type");
        boolean allowed = Arrays.stream(AppConstants.CROP_TYPES)
                .anyMatch(candidate -> candidate.equalsIgnoreCase(sanitizedValue));
        if (!allowed) {
            throw new InvalidDataException("Crop type is not supported.");
        }
        return sanitizedValue;
    }

    public static String requireRole(String value) throws InvalidDataException {
        return requireAllowedValue(value == null ? AppConstants.DEFAULT_USER_ROLE : value,
                "Role", AppConstants.USER_ROLES);
    }

    public static String requireUnit(String value) throws InvalidDataException {
        return requireAllowedValue(value == null ? AppConstants.DEFAULT_UNIT : value,
                "Unit", AppConstants.SUPPORTED_UNITS);
    }

    public static String requireStatus(String value) throws InvalidDataException {
        return requireAllowedValue(value == null ? AppConstants.DEFAULT_STATUS : value,
                "Status", AppConstants.LISTING_STATUSES);
    }

    public static String requireOptionalStatus(String value) throws InvalidDataException {
        if (value == null || value.isBlank()) {
            return "";
        }
        return requireStatus(value);
    }

    public static String optionalText(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitizedValue = value.trim();
        return sanitizedValue.length() > maxLength
                ? sanitizedValue.substring(0, maxLength)
                : sanitizedValue;
    }
}
