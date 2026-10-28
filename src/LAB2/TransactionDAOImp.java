package LAB2;

import LABB1.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImp implements TransactionDAO {


    @Override
    public boolean CreateTable() throws SQLException {
        String createTransaction = """
            CREATE TABLE transactions (
                transaction_id   SERIAL PRIMARY KEY,
                account_id       VARCHAR(50)  NOT NULL,
                reference_id     VARCHAR(100) NOT NULL,
                transaction_type VARCHAR(50)  NOT NULL,
                amount           DECIMAL(20,4) NOT NULL,
                created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_id) REFERENCES accounts(account_id)
            )
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(createTransaction)) {
            statement.executeUpdate();
            return true;
        }
    }


    @Override
    public Transaction findByAccount(String id) throws SQLException {
        String findTransaction = " select * from transactions where account_id = ? ";
        List<Transaction> transaction = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(findTransaction)) {
            statement.setString(1, id);          // ← fills the ? BEFORE running


            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Transaction transaction1 = new Transaction();
                transaction1.setTransactionId(resultSet.getInt("transaction_id"));
                transaction1.setTransactionType(resultSet.getString("transaction_type"));
                transaction1.setReferenceId(resultSet.getString("reference_id"));
                transaction1.setAmount(resultSet.getDouble("amount"));
                transaction1.setAccountId(resultSet.getString("account_id"));
                transaction.add(transaction1);


                return transaction1;
            }
            return null;
        }
    }
@Override
    public List<Transaction> findAllByAccount(String id) throws SQLException {
        String sql = "select * from transactions where account_id = ? order by created_at desc";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(resultSet.getInt("transaction_id"));
                    t.setReferenceId(resultSet.getString("reference_id"));
                    t.setTransactionType(resultSet.getString("transaction_type"));
                    t.setAmount(resultSet.getDouble("amount"));
                    t.setAccountId(resultSet.getString("account_id"));
                    t.setTimestamp(resultSet.getObject("created_at", LocalDateTime.class));
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }


    @Override
    public List<Transaction> findByAccountAndDate(String accountId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "select * from transactions where account_id = ? and created_at >= ? and created_at < ? "
                   + "order by created_at";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);
            statement.setObject(2, from.atStartOfDay());
            statement.setObject(3, to.plusDays(1).atStartOfDay());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(resultSet.getInt("transaction_id"));
                    t.setReferenceId(resultSet.getString("reference_id"));
                    t.setTransactionType(resultSet.getString("transaction_type"));
                    t.setAmount(resultSet.getDouble("amount"));
                    t.setAccountId(resultSet.getString("account_id"));
                    t.setTimestamp(resultSet.getObject("created_at", LocalDateTime.class));
                    transactions.add(t);
                }
            }
        }
        return transactions;
    }

    @Override
    public List<String[]> dailySummary(LocalDate day) throws SQLException {
        String sql = "select transaction_type, count(*), sum(amount) from transactions "
                   + "where created_at >= ? and created_at < ? group by transaction_type order by transaction_type";
        List<String[]> rows = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, day.atStartOfDay());
            statement.setObject(2, day.plusDays(1).atStartOfDay());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new String[]{resultSet.getString(1),
                            String.valueOf(resultSet.getInt(2)),
                            String.valueOf(resultSet.getDouble(3))});
                }
            }
        }
        return rows;
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        return List.of();
    }


}

