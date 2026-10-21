package dao;

/**
 * Exception thrown by the DAO layer when a database operation fails.
 * It carries a user-friendly message so Main.java never needs to
 * import or handle any JDBC (java.sql) classes.
 */
public class DaoException extends Exception {

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
