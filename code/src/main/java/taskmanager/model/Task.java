package taskmanager.model;

import java.time.LocalDateTime;
import taskmanager.exception.InvalidTaskException;

/**
 * A record representing a task entity within the system.
 * This record is immutable and serves as a data carrier for task-related information.
 * 
 * Technical Pillar 5: Documentation and Annotations.
 * Technical Pillar 1: Exceptions and Specifications.
 * 
 * @param id                The unique identifier for the task.
 * @param title             The short summary of the task.
 * @param description       Detailed notes about the task.
 * @param dueDateTime       The scheduled deadline using Java Time API.
 * @param weatherSensitive  Indicates if the task should be checked against weather conditions.
 */
public record Task(
    String id,
    String title,
    String description,
    LocalDateTime dueDateTime,
    boolean weatherSensitive
) { 

    /**
     * Compact constructor to ensure data integrity and validate task specifications.
     * 
     * @throws InvalidTaskException if the pre-conditions (id or title not null/empty) are not met.
     * 
     * Technical Pillar 1: Specifications (Pre-condition checks).
     * Requirement: Use custom exceptions as specified in the project manual.
     */
    public Task {
        // Pre-condition: Task ID and Title must be provided and not blank
        if (id == null || id.isBlank() || title == null || title.isBlank()) {
            throw new InvalidTaskException("Task validation failed: ID and Title are mandatory fields.");
        }
        
        // Ensure dueDateTime is provided
        if (dueDateTime == null) {
            throw new InvalidTaskException("Task validation failed: Due date and time must be specified.");
        }
    }

    /**
     * Helper method to determine if the task has passed its scheduled deadline.
     * 
     * @return true if the current system time is after the dueDateTime, false otherwise.
     */
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDateTime);
    }
}