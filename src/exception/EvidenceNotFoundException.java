package exception;

/**
 * EvidenceNotFoundException - Thrown when an evidence record is not found.
 *
 * Custom checked exception extending Exception.
 */
public class EvidenceNotFoundException extends Exception {

    private final String evidenceNumber;

    public EvidenceNotFoundException(String message) {
        super(message);
        this.evidenceNumber = null;
    }

    public EvidenceNotFoundException(String evidenceNumber, String message) {
        super(message);
        this.evidenceNumber = evidenceNumber;
    }

    public EvidenceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.evidenceNumber = null;
    }

    public String getEvidenceNumber() {
        return evidenceNumber;
    }
}
