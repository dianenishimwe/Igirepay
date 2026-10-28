package LAB2;

import LABB1.PinCredential;

import java.sql.*;

public class AuthDAOImpl implements AuthDAO {

    @Override
    public boolean CreateTable() throws SQLException {
        String createSql = """
            create table if not exists customer_pin(
                customer_id varchar(50) primary key references customer(customer_id) on delete cascade,
                pin_hash varchar(200) not null,
                failed_attempts int not null default 0,
                locked boolean not null default false)
            """;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createSql);
            return true;
        }
    }

    @Override
    public PinCredential findByCustomer(String customerId) throws SQLException {
        String findSql = "select * from customer_pin where customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(findSql)) {

            statement.setString(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    PinCredential credential = new PinCredential();
                    credential.setCustomerId(resultSet.getString("customer_id"));
                    credential.setPinHash(resultSet.getString("pin_hash"));
                    credential.setFailedAttempts(resultSet.getInt("failed_attempts"));
                    credential.setLocked(resultSet.getBoolean("locked"));
                    return credential;
                }
            }
        }
        return null;
    }


    @Override
    public boolean savePin(String customerId, String pinHash) throws SQLException {
        String saveSql = """
            insert into customer_pin(customer_id, pin_hash) values(?, ?)
            on conflict (customer_id) do update set pin_hash = excluded.pin_hash
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(saveSql)) {

            statement.setString(1, customerId);   // 1st ?
            statement.setString(2, pinHash);      // 2nd ?

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateAttempts(String customerId, int failedAttempts, boolean locked) throws SQLException {
        String updateSql = "update customer_pin set failed_attempts = ?, locked = ? where customer_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSql)) {

            statement.setInt(1, failedAttempts);
            statement.setBoolean(2, locked);
            statement.setString(3, customerId);

            return statement.executeUpdate() > 0;
        }
    }


    }
