package LABB1;


public class PinCredential {
    private String customerId;
    private String pinHash;
    private int failedAttempts;
    private boolean locked;

    public PinCredential() {
    }

    public PinCredential(String customerId, String pinHash, int failedAttempts, boolean locked) {
        this.customerId = customerId;
        this.pinHash = pinHash;
        this.failedAttempts = failedAttempts;
        this.locked = locked;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {   // never print the hash
        return "PinCredential{" +
                "customerId='" + customerId + '\'' +
                ", failedAttempts=" + failedAttempts +
                ", locked=" + locked +
                '}';
    }
}
