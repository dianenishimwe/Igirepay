package LAB2;

import LABB1.PinCredential;

import java.sql.SQLException;

public interface AuthDAO {

    boolean CreateTable() throws SQLException;
    PinCredential findByCustomer(String customerId) throws SQLException;
    boolean savePin(String customerId, String pinHash) throws SQLException;          // create or change PIN
    boolean updateAttempts(String customerId, int failedAttempts, boolean locked) throws SQLException;
}
