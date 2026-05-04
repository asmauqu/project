package taskmanager.exception;

/**
 * Custom exception thrown when task validation fails (e.g., missing title or invalid date).
 * 
 * Technical Pillar 1: Exceptions and Specifications.
 * Requirement: Detailed Javadoc explaining the purpose.
 */
public class InvalidTaskException extends RuntimeException {
    
    /**
     * Constructs a new InvalidTaskException with a specific error message.
     * @param message The detailed reason why the task is invalid.
     */
    public InvalidTaskException(String message) {
        super(message);
    }
}