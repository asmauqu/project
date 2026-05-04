package taskmanager.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.api.TaskService;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

/**
 * Technical Pillar 1: Facade Design Pattern.
 */
public class DefaultTaskManager implements TaskManager {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.create();

    private final TaskService taskService;
    private final SchedulePlanner planner;
    private final String apiKey;

    /**
     * Creates a new DefaultTaskManager with the required service dependencies.
     *
     * @param apiKey   the weather API key used by the manager
     * @param service  the task service implementation for persistence operations
     * @param planner  the schedule planner used for weather-based recommendations
     */
    DefaultTaskManager(String apiKey, TaskService service, SchedulePlanner planner) {
        this.apiKey = apiKey;
        this.taskService = service;
        this.planner = planner;
    }

    /**
     * Adds a task to the system by delegating to the reactive task service.
     *
     * @param task the task to add
     */
    @Override
    public void addTask(Task task) {
        taskService.addTask(task).block();
    }

    /**
     * Removes a task from the system using its identifier.
     *
     * @param taskId the ID of the task to remove
     */
    @Override
    public void removeTask(String taskId) {
        try {
            taskService.removeTask(taskId).block();
        } catch (Exception e) {
            // Handle appropriately in UI
        }
    }

    @Override
    public List<Task> getTasks() {
        return taskService.findAllTasksAsList().block();
    }

    /**
     * Fetches a weather forecast for the specified location.
     *
     * @param location the location for which to retrieve weather data
     * @return a {@link Mono} emitting a {@link WeatherForecast}
     */
    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {
        if (apiKey == null || apiKey.isBlank()) {
            return Mono.error(new IllegalStateException("OpenWeatherMap API key is not configured."));
        }

        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String endpoint = "/data/2.5/weather?q=" + encodedLocation + "&appid=" + apiKey + "&units=metric";

        return HTTP_CLIENT.get()
                .uri("https://api.openweathermap.org" + endpoint)
                .responseSingle((response, bytes) -> {
                    if (response.status().code() != 200) {
                        return bytes.asString()
                                .flatMap(body -> {
                                    try {
                                        JsonNode errorJson = MAPPER.readTree(body);
                                        String message = errorJson.path("message").asText("Unknown OpenWeatherMap error");
                                        return Mono.error(new IllegalStateException("OpenWeatherMap error: " + message));
                                    } catch (Exception parseError) {
                                        return Mono.error(new IllegalStateException("OpenWeatherMap error code: " + response.status().code()));
                                    }
                                });
                    }
                    return bytes.asString();
                })
                .flatMap(body -> {
                    try {
                        JsonNode json = MAPPER.readTree(body);
                        double temp = json.path("main").path("temp").asDouble(Double.NaN);
                        JsonNode weatherArray = json.path("weather");
                        String condition = "Unknown";
                        if (weatherArray.isArray() && weatherArray.size() > 0) {
                            condition = weatherArray.get(0).path("description").asText("Unknown");
                        }

                        double precipitationProbability = 0.0;
                        if (json.has("rain")) {
                            precipitationProbability = json.path("rain").path("1h").asDouble(0.0);
                        }
                        if (precipitationProbability == 0.0 && json.has("snow")) {
                            precipitationProbability = json.path("snow").path("1h").asDouble(0.0);
                        }
                        if (precipitationProbability > 0.0) {
                            precipitationProbability = 0.85;
                        }

                        WeatherForecast forecast = new WeatherForecast(
                                location,
                                LocalDateTime.now(),
                                temp,
                                condition,
                                precipitationProbability);
                        return Mono.just(forecast);
                    } catch (Exception jsonError) {
                        return Mono.error(new IllegalStateException("Failed to parse weather response", jsonError));
                    }
                })
                .onErrorMap(ex -> new IllegalStateException("Weather API request failed: " + ex.getMessage(), ex));
    }

    /**
     * Evaluates a task against the latest weather forecast.
     * The result is returned reactively and must be subscribed to by the caller.
     *
     * @param task The task being evaluated.
     * @param location The location to use for weather lookup.
     * @return A {@link Mono} emitting the forecast used for evaluation.
     */
    @Override
    public Mono<WeatherForecast> evaluateTasks(Task task, String location) {
        return fetchWeather(location)
                .onErrorMap(ex -> new IllegalStateException("Task evaluation failed for " + task.id() + ": " + ex.getMessage(), ex));
    }

    /**
     * Returns the planner used by this manager for generating recommendations.
     *
     * @return the configured schedule planner
     */
    @Override
    public SchedulePlanner getPlanner() {
        return this.planner;
    }
}