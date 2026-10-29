package LAB3;

import LAB2.AccountDAO;
import LAB2.AccountDAOImpl;
import LAB2.AuthService;
import LAB2.CustomerDAOImp;
import LAB2.ReportService;
import LAB2.Services;
import LAB2.TransactionDAOImp;
import LABB1.Account;
import LABB1.Customer;
import LABB1.Transaction;
import java.util.UUID;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAOImp customerDAO = new CustomerDAOImp();
    private static final AccountDAO accountDAO = new AccountDAOImpl();
    private static final TransactionDAOImp transactionDAO = new TransactionDAOImp();
    private static final Services wallet = new Services();
    private static final ReportService reports = new ReportService();
    private static final AuthService auth = new AuthService();

    // ID of the customer who is logged in (null = nobody)
    private static String loggedInId = null;

    // START MENU (before login)

    public static void main(String[] args) {
        while (true) {
            System.out.println();
            System.out.println("IgirePay ");
            System.out.println(" 1. Register customer");
            System.out.println(" 2. Login with PIN");
            System.out.println(" 3. Create PIN (customer without a PIN)");
            System.out.println(" 0. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> registerCustomer();
                    case "2" -> {
                        login();
                        sessionMenu();
                    }
                    case "3" -> createPin();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

    }

    // AUTHENTICATION

    private static void registerCustomer() throws SQLException {
        System.out.print("Customer ID: ");
        String id = scanner.nextLine().trim();
        if (customerDAO.findById(id) != null) {
            System.out.println("A customer with ID " + id + " already exists.");
            return;
        }
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone number: ");
        String phone = scanner.nextLine().trim();

        Customer customer = new Customer(id, name, email, phone);
        customerDAO.registerCustomer(customer);
        System.out.println("Customer registered: " + customer);

        // the customer needs a PIN to log in
        System.out.print("Create a 5-digit PIN: ");
        String pin = scanner.nextLine().trim();
        auth.createPin(id, pin);
        System.out.println("PIN created. You can now login.");
    }


    private static void createPin() throws SQLException {
        System.out.print("Customer ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Create a 4-digit PIN: ");
        String pin = scanner.nextLine().trim();
        auth.createPin(id, pin);
        System.out.println("PIN created. You can now login.");
    }

    private static void login() throws SQLException {
        System.out.print("Customer ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("PIN: ");
        String pin = scanner.nextLine().trim();
        auth.login(id, pin);
        loggedInId = id;
        System.out.println("Welcome, " + customerDAO.findById(id).getCustomerName() + "!");
    }

    private static void changePin() throws SQLException {
        System.out.print("Current PIN: ");
        String oldPin = scanner.nextLine().trim();
        System.out.print("New 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();
        auth.changePin(loggedInId, oldPin, newPin);
        System.out.println("PIN changed successfully.");
    }

    // MAIN MENU (after login)

    private static void sessionMenu() {
        while (loggedInId != null) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    // Customer management
                    case "1" -> updateCustomer();
                    case "2" -> viewCustomerAccounts();
                    case "3" -> changePin();
                    // Account management
                    case "4" -> createAccount("WALLET");
                    case "5" -> createAccount("SAVING");
                    case "6" -> viewBalance();
                    case "7" -> deleteInactiveAccounts();
                    // Transaction management
                    case "8" -> deposit();
                    case "9" -> withdraw();
                    case "10" -> transfer();
                    case "11" -> viewHistory();
                    // Reports
                    case "12" -> exportCsv();
                    case "13" -> dailySummary();
                    case "14" -> customerStatement();
                    case "0" -> {
                        loggedInId = null;
                        System.out.println("Logged out.");
                    }
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Error: use the date format yyyy-MM-dd, e.g. 2026-10-28");
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("IgirePay  (customer " + loggedInId + ")");
        System.out.println("-- Customer Management --");
        System.out.println(" 1. Update my information");
        System.out.println(" 2. View my accounts");
        System.out.println(" 3. Change PIN");
        System.out.println("-- Account Management --");
        System.out.println(" 4. Create wallet account");
        System.out.println(" 5. Create savings account");
        System.out.println(" 6. View account balance");
        System.out.println(" 7. Delete inactive accounts");
        System.out.println("-- Transaction Management --");
        System.out.println(" 8. Deposit money");
        System.out.println(" 9. Withdraw money");
        System.out.println("10. Transfer money");
        System.out.println("11. View transaction history");
        System.out.println("-- Reports --");
        System.out.println("12. Export transaction history to CSV");
        System.out.println("13. Daily transaction summary");
        System.out.println("14. My transaction statement");
        System.out.println(" 0. Logout");
    }

    // a customer can only use his own accounts
    private static void requireOwnAccount(String accountId) throws SQLException {
        Account account = accountDAO.findById(accountId);
        if (account == null || !account.getCustomerId().equals(loggedInId)) {
            throw new IllegalArgumentException("Account " + accountId + " was not found among your accounts.");
        }
    }

    // CUSTOMER MANAGEMENT

    private static void updateCustomer() throws SQLException {
        String id = loggedInId;
        Customer customer = customerDAO.findById(id);
        System.out.println("Current: " + customer);
        System.out.println("Press Enter to keep the current value.");

        System.out.print("New full name: ");
        String name = scanner.nextLine().trim();
        System.out.print("New email: ");
        String email = scanner.nextLine().trim();
        System.out.print("New phone number: ");
        String phone = scanner.nextLine().trim();

        // update only the columns the user typed
        if (!name.isEmpty())  customerDAO.updateName(id, name);
        if (!email.isEmpty()) customerDAO.updateEmail(id, email);
        if (!phone.isEmpty()) customerDAO.updatePhone(id, phone);

        // read it again from the database to show what is really saved
        System.out.println("Updated: " + customerDAO.findById(id));
    }

    private static void viewCustomerAccounts() throws SQLException {
        System.out.println(customerDAO.findById(loggedInId));
        List<Account> accounts = accountDAO.findByCustomer(loggedInId);
        if (accounts.isEmpty()) {
            System.out.println("   (no accounts)");
        }
        for (Account account : accounts) {
            System.out.println("   " + account);
        }
    }

    // ACCOUNT MANAGEMENT

    private static void createAccount(String type) throws SQLException {
        System.out.print("New account ID: ");
        String accountId = scanner.nextLine().trim();
        if (accountDAO.findById(accountId) != null) {
            System.out.println("An account with ID " + accountId + " already exists.");
            return;
        }
        accountDAO.registerAccount(accountId, loggedInId, type);
        System.out.println(type + " account " + accountId + " created with balance 0 RWF.");
    }

    private static void viewBalance() throws SQLException {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        requireOwnAccount(accountId);
        System.out.println(accountDAO.findById(accountId));
    }

    private static void deleteInactiveAccounts() throws SQLException {
        System.out.println("Deletes accounts with balance 0, no transactions, older than N days.");
        System.out.print("N (days): ");
        int days = Integer.parseInt(scanner.nextLine().trim());
        int deleted = accountDAO.deleteInactiveAccounts(days);
        System.out.println(deleted + " inactive account(s) deleted.");
    }

    //  TRANSACTION MANAGEMENT

    private static void deposit() throws SQLException {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        requireOwnAccount(accountId);
        System.out.print("Amount (RWF): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        String reference = generateReference("DEP");
        report(wallet.deposit(accountId, amount, reference), reference);
    }

    private static void withdraw() throws SQLException {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        requireOwnAccount(accountId);
        System.out.print("Amount (RWF): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        String reference = generateReference("WD");
        report(wallet.withdraw(accountId, amount, reference), reference);
    }

    private static void transfer() throws SQLException {
        System.out.print("From account ID (yours): ");
        String from = scanner.nextLine().trim();

        requireOwnAccount(from);

        System.out.print("To account ID: ");
        String to = scanner.nextLine().trim();

        System.out.print("Amount (RWF): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        // Generate the reference automatically
        String reference = generateReference("TRF");

        report(wallet.transfer(from, to, amount, reference), reference);
    }


    private static void viewHistory() throws SQLException {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        requireOwnAccount(accountId);
        Account account = accountDAO.findById(accountId);
        System.out.println(account);
        List<Transaction> history = transactionDAO.findAllByAccount(account.getAccountId());
        if (history.isEmpty()) {
            System.out.println("   (no transactions yet)");
        }
        for (Transaction t : history) {
            System.out.println("   " + t);
        }
    }

    // REPORTS

    private static void exportCsv() throws SQLException {
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();
        requireOwnAccount(accountId);
        System.out.println("CSV saved: " + reports.exportToCsv(accountId));
    }

    private static void dailySummary() throws SQLException {
        System.out.print("Date (yyyy-MM-dd, Enter = today): ");
        String text = scanner.nextLine().trim();
        LocalDate day = text.isEmpty() ? LocalDate.now() : LocalDate.parse(text);
        System.out.println(reports.dailySummary(day));
    }

    private static void customerStatement() throws SQLException {
        System.out.print("From (yyyy-MM-dd, Enter = first day of this month): ");
        String fromText = scanner.nextLine().trim();
        System.out.print("To (yyyy-MM-dd, Enter = today): ");
        String toText = scanner.nextLine().trim();
        LocalDate from = fromText.isEmpty() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(fromText);
        LocalDate to = toText.isEmpty() ? LocalDate.now() : LocalDate.parse(toText);
        System.out.println(reports.statement(loggedInId, from, to));
    }

    // Prevents duplicates: false means the reference id was already used
    private static void report(boolean processed, String reference) {
        if (processed) {
            System.out.println("Success. Reference: " + reference);
        } else {
            System.out.println("Duplicate request: reference " + reference
                    + " was already processed. Nothing was changed.");
        }

    }
    private static String generateReference(String prefix) {
        return prefix + "-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}
