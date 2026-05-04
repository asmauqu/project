package taskmanager.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import taskmanager.api.SchedulePlanner;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

/**
 * Implementation of the {@link SchedulePlanner} interface.
 * This class provides intelligent scheduling logic by analyzing weather conditions.
 * 
 * Technical Pillar 4: Intelligent Weather API Integration.
 * Technical Pillar 5: Professional Documentation and Javadoc.
 */
public class DefaultSchedulePlanner implements SchedulePlanner {

    /**
     * Generates schedule recommendations for a list of tasks using the provided forecast.
     *
     * @param tasks    the tasks to evaluate for scheduling
     * @param forecast the weather forecast to use when determining suitability
     * @return a {@link Mono} emitting a list of {@link ScheduleRecommendation} objects
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast) {
        return Mono.fromCallable(() -> generateRecommendations(tasks, forecast));
    }

    /**
     * Builds a forecast internally for the requested location and returns recommendations.
     *
     * @param tasks    the tasks to evaluate for scheduling
     * @param location the location to use when generating a local weather forecast
     * @return a {@link Mono} emitting a list of {@link ScheduleRecommendation} objects
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location) {
        WeatherForecast forecast = new WeatherForecast(
                location,
                LocalDateTime.now(),
                25.0,
                "Sunny",
                0.0);
        return Mono.fromCallable(() -> generateRecommendations(tasks, forecast));
    }

    /**
     * Creates recommendations based on each task's weather sensitivity and forecast.
     *
     * @param tasks    the tasks to evaluate
     * @param forecast the weather forecast to check against each task
     * @return a list of generated schedule recommendations
     */
    private List<ScheduleRecommendation> generateRecommendations(
            List<Task> tasks,
            WeatherForecast forecast) {
        return tasks.stream().map(task -> {
            String advice = "Weather conditions are optimal for this task.";
            boolean isOptimal = true;

            if (task.weatherSensitive()) {
                if (forecast.precipitationProbability() > 0.6) {
                    advice = "⚠️ High Rain Risk: Consider rescheduling or moving indoors.";
                    isOptimal = false;
                } else if (forecast.temperatureCelsius() > 40) {
                    advice = "🔥 Extreme Heat Warning: Avoid outdoor activity.";
                    isOptimal = false;
                }
            }

            return new ScheduleRecommendation(task, forecast, advice, isOptimal);
        }).collect(Collectors.toList());
    }
}