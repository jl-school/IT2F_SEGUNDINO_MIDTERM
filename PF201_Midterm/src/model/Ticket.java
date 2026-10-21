package model;

/**
 * Model class representing one event ticket record.
 * Holds exactly the five required attributes:
 * ID, Attendee Name, Ticket Tier, Price, and Payment.
 */
public class Ticket {

    private int id;
    private String attendeeName;
    private String ticketTier;
    private double price;
    private String payment;

    /** Used when creating a NEW ticket (the ID is generated later by SQLite). */
    public Ticket(String attendeeName, String ticketTier, double price, String payment) {
        this(0, attendeeName, ticketTier, price, payment);
    }

    /** Used when rebuilding a ticket that was loaded from the database. */
    public Ticket(int id, String attendeeName, String ticketTier, double price, String payment) {
        this.id = id;
        this.attendeeName = attendeeName;
        this.ticketTier = ticketTier;
        this.price = price;
        this.payment = payment;
    }

    public int getId() {
        return id;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public String getTicketTier() {
        return ticketTier;
    }

    public double getPrice() {
        return price;
    }

    public String getPayment() {
        return payment;
    }
}
