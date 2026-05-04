package taskmanager.impl;

import taskmanager.api.TaskManager;

/**
 * Concrete implementation of the {@link TaskManager.TaskManagerBuilder}.
 * Handles the construction and dependency injection of the TaskManager system.
 * 
 * Technical Pillar 1: Design Patterns (Builder).
 */
public class DefaultTaskManagerBuilder implements TaskManager.TaskManagerBuilder {
    private String apiKey;

    /**
     * Configures the weather API key required for the task manager.
     *
     * @param apiKey the external weather service API key
     * @return this builder instance for chaining
     */
    @Override
    public TaskManager.TaskManagerBuilder withWeatherApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Configures an optional storage path.
     *
     * @param path the local file system path for optional persistence
     * @return this builder instance for chaining
     */
    @Override
    public TaskManager.TaskManagerBuilder withStoragePath(String path) {
        return this; // Optional storage configuration
    }

    /**
     * Builds the {@link TaskManager} instance using the configured settings.
     *
     * @return the initialized task manager
     * @throws IllegalStateException if the weather API key is missing
     */
    @Override
    public TaskManager build() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API Key is mandatory for system initialization.");
        }
        
        // Injecting core services into the manager
        return new DefaultTaskManager(
            apiKey, 
            new DefaultTaskService(), 
            new DefaultSchedulePlanner()
        );
    }
}