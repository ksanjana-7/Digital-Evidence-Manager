package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Utility class for managing MySQL JDBC connections.
 *
 * Demonstrates JDBC concepts:
 *   - DriverManager.getConnection()
 *   - Connection management
 *   - PreparedStatement usage (used in DAO classes)
 *
 * Uses the Singleton pattern so we reuse one connection across the app.
 * For a production system, a connection pool would be used instead.
 */
public class DBConnection {

    // ==============================================================
    // Database Configuration - Change these to match your MySQL setup
    // ==============================================================
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/digital_evidence_manager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";   // <-- Change this

    // The single shared connection instance
    private static Connection connection = null;

    // Private constructor - prevents instantiation
    private DBConnection() {}

    /**
     * Returns the single shared Connection.
     * Creates it if not already connected.
     *
     * @return Connection to MySQL
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // If no connection exists or the connection is closed, create a new one
            if (connection == null || connection.isClosed()) {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("[DBConnection] Connected to database successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to classpath.", e);
        }
        return connection;
    }

    /**
     * Closes the connection safely.
     * Call this when the application shuts down.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DBConnection] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Tests the database connection.
     * Returns true if successful, false otherwise.
     */
    public static boolean testConnection() {
        try {
            Connection c = getConnection();
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
