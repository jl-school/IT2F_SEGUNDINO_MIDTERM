package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralizes the SQLite connection configuration.
 * The database file is stored at <project folder>/data/tickets.db,
 * so the same file is reused every time the program runs.
 */
public class DatabaseConnection {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE = DB_FOLDER + File.separator + "tickets.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseConnection() {
        // Utility class: no instances needed.
    }

    /**
     * Opens a new connection to the SQLite database file.
     * The caller must close it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        File folder = new File(DB_FOLDER);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new SQLException("Could not create the database folder: " + DB_FOLDER);
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Add the sqlite-jdbc library to the project.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }
}
