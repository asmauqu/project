package taskmanager.exception;

/**
 * Custom exception thrown when there is an error connecting to or retrieving data 
 * from the external weather service API.
 * 
 * Technical Pillar 1: Exceptions and Specifications.
 * Technical Pillar 5: Documentation and Annotations.
 */
public class WeatherAPIException extends Exception {

    /**
     * Constructs a new WeatherAPIException with a message and the original cause.
     * 
     * @param message A descriptive message about the weather service error.
     * @param cause   The underlying cause of the exception (e.g., IOException, ConnectException).
     */
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}