package taskmanager.exception;

/**
 * Custom exception thrown when a specific task ID does not exist in the system.
 * 
 * Technical Pillar 1: Exceptions and Specifications.
 * Requirement: Javadoc for class purpose and parameters.
 */
public class TaskNotFoundException extends Exception {

    /**
     * Constructs the exception with a message identifying the missing task.
     * @param taskId The unique ID of the task that was not found.
     */
    public TaskNotFoundException(String taskId) {
        super("Task not found with ID: " + taskId);
    }
}