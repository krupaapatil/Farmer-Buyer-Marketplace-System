package farmmarket.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import farmmarket.exceptions.InvalidDataException;

public final class SecurityUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private SecurityUtil() {
    }

    public static PasswordHash hashPassword(String password) throws InvalidDataException {
        String sanitizedPassword = ValidationUtil.requireMinimumLength(password, 8, "Password");
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(sanitizedPassword.toCharArray(), salt);
        return new PasswordHash(
                Base64.getEncoder().encodeToString(hash),
                Base64.getEncoder().encodeToString(salt));
    }

    public static boolean verifyPassword(String password, String expectedHash, String salt) throws InvalidDataException {
        String sanitizedPassword = ValidationUtil.requireMinimumLength(password, 8, "Password");
        byte[] computedHash = pbkdf2(
                sanitizedPassword.toCharArray(),
                Base64.getDecoder().decode(salt.getBytes(StandardCharsets.UTF_8)));
        byte[] storedHash = Base64.getDecoder().decode(expectedHash.getBytes(StandardCharsets.UTF_8));
        return MessageDigest.isEqual(computedHash, storedHash);
    }

    public static String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ENGLISH);
    }

    public static String generateSessionToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) throws InvalidDataException {
        try {
            KeySpec keySpec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(keySpec).getEncoded();
        } catch (Exception exception) {
            throw new InvalidDataException("Password hashing could not be completed.");
        }
    }

    public record PasswordHash(String hash, String salt) {
    }
}
