package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * FileUtil - Utility class for file operations on evidence files.
 *
 * Demonstrates Java File I/O:
 *   - File selection (via Swing JFileChooser in GUI)
 *   - Copying files to the evidence store directory
 *   - Reading file metadata (name, size, type)
 */
public final class FileUtil {

    // Base directory where all evidence files are stored
    public static final String EVIDENCE_BASE_DIR = "Evidence";

    private FileUtil() {}

    /**
     * Returns the file extension (type) of a given file.
     * Example: "document.pdf" -> "PDF"
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            return name.substring(lastDot + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * Formats a file size in bytes to a human-readable string.
     * Examples: 1024 -> "1.00 KB", 1048576 -> "1.00 MB"
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024)                  return bytes + " B";
        if (bytes < 1024 * 1024)           return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)   return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return                               String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Copies a source evidence file into the evidence store directory.
     * Creates directory structure: Evidence/CASE_NUMBER/EVIDENCE_NUMBER/filename
     *
     * @param sourceFile     The file selected by the user
     * @param caseNumber     The case this evidence belongs to
     * @param evidenceNumber The evidence identifier
     * @return               The relative path where the file was copied
     * @throws IOException if copy fails
     */
    public static String copyEvidenceFile(File sourceFile, String caseNumber, String evidenceNumber) throws IOException {
        // Build target directory path
        String dirPath = EVIDENCE_BASE_DIR + File.separator + caseNumber + File.separator + evidenceNumber;
        Path targetDir = Paths.get(dirPath);

        // Create directories if they don't exist
        Files.createDirectories(targetDir);

        // Target file path
        Path targetFile = targetDir.resolve(sourceFile.getName());

        // Copy file, replacing if it already exists
        Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("[FileUtil] Evidence file copied to: " + targetFile.toString());
        return targetFile.toString();
    }

    /**
     * Checks whether a file at the given path exists.
     */
    public static boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && f.isFile();
    }

    /**
     * Returns a File object for the given path, or null if not found.
     */
    public static File getFile(String filePath) {
        File f = new File(filePath);
        return f.exists() ? f : null;
    }

    /**
     * Ensures the base evidence directory exists.
     * Called at application startup.
     */
    public static void ensureEvidenceDirectoryExists() {
        File dir = new File(EVIDENCE_BASE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("[FileUtil] Created evidence directory: " + dir.getAbsolutePath());
            }
        }
    }
}
