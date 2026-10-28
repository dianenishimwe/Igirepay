package LAB2;

import LABB1.PinCredential;

import java.sql.SQLException;


public class AuthService {

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private final CustomerDAOImp customerDAO = new CustomerDAOImp();
    private final AuthDAO authDAO = new AuthDAOImpl();

    public AuthService() {
        try {
            authDAO.CreateTable();   // creates the customer_pin table only if it does not exist yet
        } catch (SQLException e) {
            throw new IllegalStateException("Could not prepare the PIN table: " + e.getMessage());
        }
    }


    public void createPin(String customerId, String pin) throws SQLException {
        if (customerDAO.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer " + customerId + " does not exist.");
        }
        if (authDAO.findByCustomer(customerId) != null) {
            throw new IllegalStateException("This customer already has a PIN. Log in and use Change PIN.");
        }
        PinUtil.validateFormat(pin);
        authDAO.savePin(customerId, PinUtil.hash(pin));
    }


    public void login(String customerId, String pin) throws SQLException {
        PinCredential credential = authDAO.findByCustomer(customerId);

        if (credential == null) {
            if (customerDAO.findById(customerId) == null) {
                throw new IllegalArgumentException("Wrong customer ID or PIN.");
            }
            throw new IllegalStateException("No PIN yet for this customer. Choose 'Create PIN' first.");
        }
        checkPin(credential, pin);
    }


    public void changePin(String customerId, String oldPin, String newPin) throws SQLException {
        PinCredential credential = authDAO.findByCustomer(customerId);
        if (credential == null) {
            throw new IllegalStateException("No PIN to change. Create a PIN first.");
        }
        checkPin(credential, oldPin);

        PinUtil.validateFormat(newPin);
        if (oldPin.equals(newPin)) {
            throw new IllegalArgumentException("The new PIN must be different from the old one.");
        }
        authDAO.savePin(customerId, PinUtil.hash(newPin));
    }


    private void checkPin(PinCredential credential, String pin) throws SQLException {
        String customerId = credential.getCustomerId();

        if (credential.isLocked()) {
            throw new IllegalStateException("This account is locked after too many wrong PINs. Contact the administrator.");
        }
        if (pin == null || !PinUtil.matches(pin, credential.getPinHash())) {
            int attempts = credential.getFailedAttempts() + 1;
            boolean lock = attempts >= MAX_FAILED_ATTEMPTS;
            authDAO.updateAttempts(customerId, attempts, lock);

            if (lock) {
                throw new IllegalStateException("Wrong PIN. Too many attempts: the account is now locked.");
            }
            throw new IllegalArgumentException("Wrong customer ID or PIN. Attempts left: "
                    + (MAX_FAILED_ATTEMPTS - attempts));
        }
        if (credential.getFailedAttempts() > 0) {
            authDAO.updateAttempts(customerId, 0, false);   // correct PIN: reset the counter
        }
    }
}
