package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HashUtil - Utility class for SHA-256 hash computation.
 *
 * Used for:
 *  - Computing SHA-256 hash of an evidence file when it is registered.
 *  - Recomputing the hash during verification and comparing to stored hash.
 *
 * SHA-256 is a one-way cryptographic function.
 * If the hash matches -> file content is unchanged.
 * If the hash differs -> file has been modified.
 */
public final class HashUtil {

    // final class - cannot be subclassed
    // private constructor - cannot be instantiated
    private HashUtil() {}

    /**
     * Computes the SHA-256 hash of a file.
     *
     * @param filePath  Absolute path to the file
     * @return          64-character hexadecimal SHA-256 hash string
     * @throws IOException if file cannot be read
     * @throws NoSuchAlgorithmException if SHA-256 is not available (won't happen on standard JVM)
     */
    public static String computeHash(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];  // Read in 8KB chunks for memory efficiency
        int bytesRead;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        // Convert the byte array to a hex string
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    /**
     * Computes SHA-256 hash of a String (used for password hashing).
     *
     * @param input  The string to hash
     * @return       Hexadecimal SHA-256 hash
     */
    public static String computeHash(String input, boolean isString) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verifies whether a file's current hash matches an expected hash.
     *
     * @param filePath     Path to the file
     * @param expectedHash The original stored hash
     * @return             true if hashes match, false if file has changed
     */
    public static boolean verifyHash(String filePath, String expectedHash) {
        try {
            String currentHash = computeHash(filePath);
            return currentHash.equalsIgnoreCase(expectedHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("[HashUtil] Error computing hash: " + e.getMessage());
            return false;
        }
    }
}
