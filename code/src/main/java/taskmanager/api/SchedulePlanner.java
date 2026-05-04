package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import java.util.List;

/**
 * Interface for planning and recommending schedules based on weather conditions.
 * 
 * Technical Pillar 2: Reactive Programming (Mono).
 * Technical Pillar 4: Weather API Integration.
 */
public interface SchedulePlanner {

    /**
     * Suggests a schedule by analyzing tasks against a provided weather forecast.
     * 
     * @param tasks    The list of tasks to evaluate.
     * @param forecast The current weather data to check against.
     * @return A Mono containing a list of recommendations for each task.
     */
    Mono<List<ScheduleRecommendation>> suggestSchedule(
            List<Task> tasks,
            WeatherForecast forecast);

    /**
     * Retrieves weather for a specific location and suggests a schedule.
     * 
     * @param tasks    The list of tasks to evaluate.
     * @param location The city or region to fetch weather for.
     * @return A Mono emitting the recommendations based on local weather.
     */
    Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(
            List<Task> tasks,
            String location);
}