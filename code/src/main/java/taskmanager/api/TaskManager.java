package taskmanager.api;

import java.util.List;

import reactor.core.publisher.Mono;
import taskmanager.impl.DefaultTaskManagerBuilder;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

/**
 * Main facade for the Smart Task Manager system.
 * This interface provides a simplified entry point to manage tasks, weather integration, 
 * and scheduling logic, hiding the underlying complexity.
 * 
 * Technical Pillar 1: Design Patterns (Facade & Builder).
 * Technical Pillar 5: Professional Documentation.
 */
public interface TaskManager {

    /**
     * Adds a new task to the system's registry.
     * @param task The task record to be managed.
     */
    void addTask(Task task);

    /**
     * Removes an existing task based on its unique identifier.
     * @param taskId The ID of the task to remove.
     */
    void removeTask(String taskId);

    /**
     * Retrieves an unmodifiable list of all current tasks.
     * @return A list containing all Task records.
     */
    List<Task> getTasks();

    /**
     * Fetches real-time weather data for a specific location.
     * Technical Pillar 2: Reactive Programming (Non-blocking I/O).
     * 
     * @param location The city or region name.
     * @return A Mono emitting the WeatherForecast once retrieved.
     */
    Mono<WeatherForecast> fetchWeather(String location);

    /**
     * Evaluates a task against the current weather forecast.
     * This returns a reactive stream that must be subscribed to in order to execute.
     * 
     * @param task The task to evaluate for weather alignment.
     * @param location The location to use for the weather request.
     * @return A Mono emitting the weather forecast used for evaluation.
     */
    Mono<WeatherForecast> evaluateTasks(Task task, String location);

    /**
     * Accesses the scheduling engine for weather-aware recommendations.
     * @return The SchedulePlanner instance.
     */
    SchedulePlanner getPlanner();

    /**
     * Static factory method to initiate the TaskManager construction process.
     * @return A new instance of TaskManagerBuilder.
     */
    static TaskManagerBuilder builder() {
        return new DefaultTaskManagerBuilder();
    }

    /**
     * A nested interface defining the construction steps for the TaskManager.
     * Technical Pillar 1: Builder Design Pattern for flexible initialization.
     */
    interface TaskManagerBuilder {
        /**
         * Configures the API key required for weather service integration.
         * @param apiKey The external service authentication key.
         * @return The builder instance for chaining.
         */
        TaskManagerBuilder withWeatherApiKey(String apiKey);

        /**
         * Sets an optional path for data persistence.
         * @param path The local file system path.
         * @return The builder instance for chaining.
         */
        TaskManagerBuilder withStoragePath(String path);

        /**
         * Finalizes the configuration and creates the TaskManager instance.
         * @return The fully initialized TaskManager object.
         */
        TaskManager build();
    }
}