package farmmarket.util;

import farmmarket.exceptions.InvalidDataException;

public final class ValidationUtil {
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
}
