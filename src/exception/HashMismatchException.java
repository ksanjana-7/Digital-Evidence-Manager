package exception;

/**
 * HashMismatchException - Thrown when evidence hash verification fails.
 * This indicates possible tampering with the evidence file.
 */
public class HashMismatchException extends Exception {

    private final String originalHash;
    private final String currentHash;
    private final String evidenceNumber;

    public HashMismatchException(String message) {
        super(message);
        this.originalHash  = null;
        this.currentHash   = null;
        this.evidenceNumber = null;
    }

    public HashMismatchException(String evidenceNumber, String originalHash,
                                 String currentHash, String message) {
        super(message);
        this.evidenceNumber = evidenceNumber;
        this.originalHash  = originalHash;
        this.currentHash   = currentHash;
    }

    public String getOriginalHash()  { return originalHash;  }
    public String getCurrentHash()   { return currentHash;   }
    public String getEvidenceNumber(){ return evidenceNumber; }
}
