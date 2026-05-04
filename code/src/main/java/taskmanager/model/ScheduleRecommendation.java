package taskmanager.model;

/**
 * A record that represents a scheduling suggestion for a specific task.
 * This model follows the provided skeleton contracts by using only two parameters.
 * 
 * @param task            The specific task being evaluated.
 * @param recommendation  A human-readable message suggesting to proceed or postpone.
 */
public record ScheduleRecommendation(
    Task task, 
    String recommendation
) {
    /**
     * Checks if the recommendation suggests postponing the task.
     * This logic helps identify tasks that need a warning in the UI without 
     * changing the required record structure.
     * 
     * @return true if the recommendation contains the word "postpone".
     */
    public boolean isPostponed() {
        return recommendation != null && recommendation.toLowerCase().contains("postpone");
    }
}