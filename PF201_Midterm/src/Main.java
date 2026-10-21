import dao.DaoException;
import dao.ScenarioDao;
import model.Ticket;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Presentation layer: menu, input validation, and console output.
 * This class contains NO SQL and NO JDBC code. It talks to the
 * database only through ScenarioDao.
 */
public class Main {

    private static final String[] VALID_TIERS = {"REGULAR", "VIP", "VVIP"};
    private static final String[] VALID_PAYMENTS = {"CASH", "GCASH", "CARD"};
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MENU_WIDTH = 40;

    // One Scanner, and nextLine() is used for EVERY input, so no leftover
    // newline can ever be consumed by a later text prompt.
    private static final Scanner scanner = new Scanner(System.in);
    private static final ScenarioDao dao = new ScenarioDao();

    public static void main(String[] args) {
        try {
            dao.initializeTable();
        } catch (DaoException e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = readMenuChoice();
                switch (choice) {
                    case 1:
                        addTicket();
                        break;
                    case 2:
                        viewTickets();
                        break;
                    case 3:
                        System.out.println();
                        System.out.println("Exiting system... Thank you!");
                        running = false;
                        break;
                    default:
                        // Invalid choice: message already shown, show the menu again.
                        break;
                }
            }
        } catch (IllegalStateException e) {
            // Input stream was closed (for example Ctrl+D): leave gracefully.
            System.out.println();
            System.out.println("Input closed. Exiting system... Thank you!");
        } finally {
            scanner.close();
        }
    }

    // ---------------------------------------------------------------- menu

    private static void printMenu() {
        String line = repeat('=', MENU_WIDTH);
        System.out.println();
        System.out.println(line);
        System.out.println(center("EVENT TICKET SALES SYSTEM", MENU_WIDTH));
        System.out.println(line);
        System.out.println("[1] Add New Ticket");
        System.out.println("[2] View Sold Tickets");
        System.out.println("[3] Exit System");
        System.out.println(line);
    }

    /** Returns 1, 2, or 3, or -1 (after printing an error) for anything else. */
    private static int readMenuChoice() {
        String input = readLine("Enter choice: ");
        if (input.matches("[123]")) {
            return Integer.parseInt(input);
        }
        System.out.println("ERROR: Invalid choice. Please enter 1, 2, or 3.");
        return -1;
    }

    // ------------------------------------------------------------ add ticket

    private static void addTicket() {
        System.out.println();
        System.out.println("--- ISSUE EVENT TICKET ---");

        String name = promptName();
        String tier = promptOption("Enter Ticket Tier (REGULAR / VIP / VVIP): ", VALID_TIERS, "ticket tier");
        double price = promptPrice();
        String payment = promptOption("Enter Payment Method (CASH / GCASH / CARD): ", VALID_PAYMENTS, "payment method");

        Ticket ticket = new Ticket(name, tier, price, payment);
        try {
            dao.addTicket(ticket);
            System.out.println();
            System.out.println("SUCCESS: Ticket issued to attendee!");
        } catch (DaoException e) {
            System.out.println();
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("The ticket was NOT issued.");
        }
    }

    private static String promptName() {
        while (true) {
            // Collapse repeated spaces/tabs so names are stored cleanly.
            String name = readLine("Enter Attendee Name: ").replaceAll("\\s+", " ");
            if (name.isEmpty()) {
                System.out.println("ERROR: Attendee name cannot be empty. Please try again.");
            } else if (name.length() > MAX_NAME_LENGTH) {
                System.out.println("ERROR: Attendee name must be at most " + MAX_NAME_LENGTH + " characters.");
            } else {
                return name;
            }
        }
    }

    /** Case-insensitive validation; returns the value normalized to uppercase. */
    private static String promptOption(String prompt, String[] validValues, String label) {
        while (true) {
            String input = readLine(prompt).toUpperCase(Locale.ROOT);
            for (String valid : validValues) {
                if (valid.equals(input)) {
                    return valid;
                }
            }
            System.out.println("ERROR: Invalid " + label + ". Allowed values: " + String.join(", ", validValues) + ".");
        }
    }

    private static double promptPrice() {
        while (true) {
            String input = readLine("Enter Price: ");
            // Digits with an optional decimal part (max 2 places). This rejects
            // letters, negatives, "NaN", "Infinity", and exponent formats.
            if (!input.matches("\\d+(\\.\\d{1,2})?")) {
                System.out.println("ERROR: Invalid price. Enter a positive number such as 1500 or 1500.00.");
                continue;
            }
            double price = Double.parseDouble(input);
            if (Double.isInfinite(price) || price <= 0) {
                System.out.println("ERROR: Price must be greater than zero.");
                continue;
            }
            return price;
        }
    }

    // ---------------------------------------------------------- view tickets

    private static void viewTickets() {
        List<Ticket> tickets;
        try {
            tickets = dao.getAllTickets();
        } catch (DaoException e) {
            System.out.println();
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        if (tickets.isEmpty()) {
            System.out.println();
            System.out.println("No tickets have been sold yet.");
            return;
        }

        String[] headers = {"ID", "ATTENDEE NAME", "TICKET TIER", "PRICE", "PAYMENT"};
        int[] widths = {2, 20, 11, 8, 7};
        String[][] rows = new String[tickets.size()][5];

        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            rows[i][0] = String.valueOf(t.getId());
            rows[i][1] = t.getAttendeeName();
            rows[i][2] = t.getTicketTier();
            rows[i][3] = String.format(Locale.US, "%.2f", t.getPrice());
            rows[i][4] = t.getPayment();
        }
        // Widen each column to fit the longest value (long names are never cut off).
        for (int c = 0; c < headers.length; c++) {
            widths[c] = Math.max(widths[c], headers[c].length());
            for (String[] row : rows) {
                widths[c] = Math.max(widths[c], row[c].length());
            }
        }

        String border = buildBorder(widths);
        int bannerWidth = Math.max(border.length(), MENU_WIDTH);
        String banner = repeat('=', bannerWidth);

        System.out.println();
        System.out.println(banner);
        System.out.println(center("EVENT TICKET PURCHASES", bannerWidth));
        System.out.println(banner);
        System.out.println();
        System.out.println(border);
        printRow(headers, widths);
        System.out.println(border);
        for (String[] row : rows) {
            printRow(row, widths);
        }
        System.out.println(border);
        System.out.println();
        System.out.println("Total Records Displayed: " + tickets.size());
    }

    private static String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append(repeat('-', w + 2)).append('+');
        }
        return sb.toString();
    }

    private static void printRow(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.length; i++) {
            sb.append(' ').append(String.format("%-" + widths[i] + "s", cells[i])).append(" |");
        }
        System.out.println(sb);
    }

    // --------------------------------------------------------------- helpers

    /** Prints the prompt and reads one full line (always nextLine). */
    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Input closed");
        }
        return scanner.nextLine().trim();
    }

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private static String center(String text, int width) {
        int padding = Math.max(0, (width - text.length()) / 2);
        return repeat(' ', padding) + text;
    }
}
