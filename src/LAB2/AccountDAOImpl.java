package LAB2;

import LABB1.Account;
import LABB1.Customer;

import java.util.UUID;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public boolean CreateTable() throws SQLException {

        String createSql = """
            CREATE TABLE accounts (
                account_id VARCHAR(50) PRIMARY KEY,
                account_number VARCHAR(30) NOT NULL,
                customer_id VARCHAR(5) NOT NULL,
                account_type VARCHAR(255) NOT NULL,
                balance DECIMAL(20,5) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (customer_id)
                    REFERENCES customer(customer_id)
            )
            """;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createSql);
            return true;
        }
    }

    @Override
    public Account registerAccount(Account account) throws SQLException {

        String checkSql = """
            SELECT 1
            FROM customer
            WHERE customer_id = ?
            """;

        String insertSql = """
            INSERT INTO accounts
                (account_id, account_number, account_type,
                 balance, created_at, customer_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;


        try (Connection connection = DBConnection.getConnection()) {

            // Check whether the customer exists
            try (PreparedStatement check =
                         connection.prepareStatement(checkSql)) {

                check.setString(1, account.getCustomerId());

                try (ResultSet result = check.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException(
                                "Customer ID " + account.getCustomerId()
                                        + " does not exist in the customer table."
                        );
                    }
                }
            }

            // Insert the account
            try (PreparedStatement statement =
                         connection.prepareStatement(insertSql)) {

                statement.setString(1, account.getAccountId());
                statement.setString(2, account.getAccountNumber());
                statement.setString(3, account.getAccountType());
                statement.setDouble(4, account.getBalance());
                statement.setObject(5, account.getCreatedAt());
                statement.setString(6, account.getCustomerId());

                statement.executeUpdate();
            }
        }

        return account;
    }

    @Override
    public Account findById(String id) throws SQLException {

        String findAccountSql =
                "SELECT * FROM accounts WHERE account_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(findAccountSql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {

                    Account account = new Account();

                    account.setAccountId(
                            resultSet.getString("account_id"));

                    account.setAccountNumber(
                            resultSet.getString("account_number"));

                    account.setAccountType(
                            resultSet.getString("account_type"));

                    account.setBalance(
                            resultSet.getDouble("balance"));

                    account.setCreatedAt(
                            resultSet.getObject(
                                    "created_at", LocalDateTime.class));

                    account.setCustomerId(
                            resultSet.getString("customer_id"));

                    return account;
                }
            }
        }

        return null;
    }

    @Override
    public List<Account> findAll() throws SQLException {

        String display = "SELECT * FROM accounts";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(display);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Account account = new Account();

                account.setAccountId(
                        resultSet.getString("account_id"));

                account.setAccountNumber(
                        resultSet.getString("account_number"));

                account.setAccountType(
                        resultSet.getString("account_type"));

                account.setBalance(
                        resultSet.getDouble("balance"));

                account.setCreatedAt(
                        resultSet.getObject(
                                "created_at", LocalDateTime.class));

                account.setCustomerId(
                        resultSet.getString("customer_id"));

                accounts.add(account);
            }
        }

        return accounts;
    }

    @Override
    public boolean updateAccount(Account account) throws SQLException {

        String updateSql = """
            UPDATE accounts
            SET account_id = ?,
                account_type = ?,
                balance = ?,
                created_at = ?
            WHERE customer_id = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(updateSql)) {

            statement.setString(1, account.getAccountId());
            statement.setString(2, account.getAccountType());
            statement.setDouble(3, account.getBalance());
            statement.setObject(4, account.getCreatedAt());
            statement.setString(5, account.getCustomerId());

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                System.out.println(
                        "Account " + account.getAccountId() + " is not found");
                return false;
            }

            System.out.println(
                    "Account " + account.getAccountId() + " updated");
        }

        return true;
    }

    @Override
    public boolean deleteAccount(String id) throws SQLException {

        String deleteSql =
                "DELETE FROM accounts WHERE account_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(deleteSql)) {

            statement.setString(1, id);

            int rowsDeleted = statement.executeUpdate();

            return rowsDeleted > 0;
        }
    }

    // Used by Main.createAccount: new account starts with balance 0
    @Override
    public void registerAccount(
            String accountId, String customerId, String type) {

        try {
            registerAccount(
                    new Account(accountId, accountId, type, customerId));
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findByCustomer(String customerId)
            throws SQLException {

        String findSql =
                "SELECT * FROM accounts WHERE customer_id = ?";

        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(findSql)) {

            statement.setString(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Account account = new Account();

                    account.setAccountId(
                            resultSet.getString("account_id"));

                    account.setAccountNumber(
                            resultSet.getString("account_number"));

                    account.setAccountType(
                            resultSet.getString("account_type"));

                    account.setBalance(
                            resultSet.getDouble("balance"));

                    account.setCreatedAt(
                            resultSet.getObject(
                                    "created_at", LocalDateTime.class));

                    account.setCustomerId(
                            resultSet.getString("customer_id"));

                    accounts.add(account);
                }
            }
        }

        return accounts;
    }

    // Inactive = balance 0, no transactions, older than N days
    @Override
    public int deleteInactiveAccounts(int days) throws SQLException {

        String deleteSql = """
            DELETE FROM accounts
            WHERE balance = 0
              AND created_at < ?
              AND NOT EXISTS (
                  SELECT 1
                  FROM transactions t
                  WHERE t.account_id = accounts.account_id
              )
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(deleteSql)) {

            statement.setObject(
                    1, LocalDateTime.now().minusDays(days));

            return statement.executeUpdate();
        }
    }





    }
