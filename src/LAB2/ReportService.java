package LAB2;

import LABB1.Account;
import LABB1.Customer;
import LABB1.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


public class ReportService {

    private final CustomerDAOImp customerDAO = new CustomerDAOImp();
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final TransactionDAOImp transactionDAO = new TransactionDAOImp();

    // 1. Export transaction history of one account to a CSV file
    public String exportToCsv(String accountId) throws SQLException {
        if (accountDAO.findById(accountId) == null) {
            throw new IllegalArgumentException("Account " + accountId + " does not exist.");
        }
        List<Transaction> history = transactionDAO.findAllByAccount(accountId);

        File folder = new File("reports");
        folder.mkdirs();
        File file = new File(folder, "account_" + accountId + ".csv");

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("transaction_id,reference_id,type,amount,date");
            for (Transaction t : history) {
                out.println(t.getTransactionId() + "," + t.getReferenceId() + "," + t.getTransactionType()
                        + "," + t.getAmount() + "," + t.getTimestamp().toString().replace('T', ' '));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not write the CSV file: " + e.getMessage());
        }
        return file.getAbsolutePath() + "  (" + history.size() + " transactions)";
    }


    public String dailySummary(LocalDate day) throws SQLException {
        List<String[]> rows = transactionDAO.dailySummary(day);

        StringBuilder text = new StringBuilder("===== DAILY SUMMARY " + day + " =====\n");
        if (rows.isEmpty()) {
            text.append("(no transactions on this day)\n");
        }
        for (String[] row : rows) {
            text.append(String.format("%-12s %3s transaction(s)   total %s%n", row[0], row[1], row[2]));
        }
        return text.toString();
    }


    public String statement(String customerId, LocalDate from, LocalDate to) throws SQLException {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer " + customerId + " does not exist.");
        }
        List<Account> accounts = accountDAO.findByCustomer(customerId);

        StringBuilder text = new StringBuilder();
        text.append("===== STATEMENT: ").append(customer.getCustomerName())
            .append(" (").append(customerId).append(") =====\n");
        text.append("Period: ").append(from).append(" to ").append(to).append("\n");
        if (accounts.isEmpty()) {
            text.append("(this customer has no accounts)\n");
        }

        for (Account account : accounts) {
            double in = 0;
            double out = 0;
            text.append("\nAccount ").append(account.getAccountId())
                .append(" (").append(account.getAccountType()).append(")\n");

            List<Transaction> list = transactionDAO.findByAccountAndDate(account.getAccountId(), from, to);
            if (list.isEmpty()) {
                text.append("   (no transactions in this period)\n");
            }
            for (Transaction t : list) {
                boolean moneyIn = "DEPOSIT".equals(t.getTransactionType());
                if (moneyIn) in += t.getAmount(); else out += t.getAmount();
                text.append(String.format("   %-16s %-11s %s%10.1f   %s%n", t.getReferenceId(), t.getTransactionType(),
                        moneyIn ? "+" : "-", t.getAmount(), t.getTimestamp().toString().replace('T', ' ').substring(0, 16)));
            }
            text.append(String.format("   Total in: %.1f   Total out: %.1f   Current balance: %.1f%n",
                    in, out, account.getBalance()));
        }
        return text.toString();
    }
}
