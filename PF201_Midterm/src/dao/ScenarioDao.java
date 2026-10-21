package dao;

import model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for tickets.
 * ALL SQL statements and JDBC operations of the application live here.
 */
public class ScenarioDao {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS tickets ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "attendee_name TEXT NOT NULL CHECK (length(trim(attendee_name)) > 0), "
            + "ticket_tier TEXT NOT NULL CHECK (ticket_tier IN ('REGULAR', 'VIP', 'VVIP')), "
            + "price REAL NOT NULL CHECK (price > 0), "
            + "payment TEXT NOT NULL CHECK (payment IN ('CASH', 'GCASH', 'CARD'))"
            + ")";

    private static final String INSERT_SQL =
            "INSERT INTO tickets (attendee_name, ticket_tier, price, payment) VALUES (?, ?, ?, ?)";

    private static final String SELECT_ALL_SQL =
            "SELECT id, attendee_name, ticket_tier, price, payment FROM tickets ORDER BY id ASC";

    /**
     * Creates the tickets table if it does not exist yet.
     * Safe to call every time the program starts: existing records are never touched.
     */
    public void initializeTable() throws DaoException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new DaoException("Unable to initialize the database: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts one ticket using a PreparedStatement.
     * The ID is generated automatically by SQLite.
     */
    public void addTicket(Ticket ticket) throws DaoException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, ticket.getAttendeeName());
            ps.setString(2, ticket.getTicketTier());
            ps.setDouble(3, ticket.getPrice());
            ps.setString(4, ticket.getPayment());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted != 1) {
                throw new DaoException("The ticket was not saved to the database.", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to save the ticket: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves every saved ticket, ordered by ID.
     * Returns an empty list when there are no records.
     */
    public List<Ticket> getAllTickets() throws DaoException {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tickets.add(new Ticket(
                        rs.getInt("id"),
                        rs.getString("attendee_name"),
                        rs.getString("ticket_tier"),
                        rs.getDouble("price"),
                        rs.getString("payment")));
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to retrieve tickets: " + e.getMessage(), e);
        }
        return tickets;
    }
}
