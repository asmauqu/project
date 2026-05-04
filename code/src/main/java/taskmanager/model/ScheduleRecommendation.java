package taskmanager.model;

/**
 * A record that combines a task with a weather forecast to provide intelligent scheduling advice.
 * This record serves as the bridge between the task management logic and weather awareness.
 * 
 * Technical Pillar 4: Weather API Integration.
 * Technical Pillar 5: Documentation and Annotations.
 * 
 * @param task              The specific task being evaluated for scheduling.
 * @param forecast          The weather forecast data associated with the task's time.
 * @param recommendation    A human-readable message suggesting to proceed, postpone, or take precautions.
 * @param warningTriggered  A flag indicating if the weather conditions conflict with the task requirements.
 */
public record ScheduleRecommendation(
    Task task,
    WeatherForecast forecast,
    String recommendation,
    boolean warningTriggered
) {
    /**
     * Checks if the recommendation contains a critical alert.
     * @return true if a warning is triggered, false otherwise.
     */
    public boolean hasAlert() {
        return warningTriggered;
    }
}