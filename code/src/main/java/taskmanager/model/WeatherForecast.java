package taskmanager.model;

import java.time.LocalDateTime;

/**
 * A record representing the weather conditions for a specific location and time.
 * This data is used by the SchedulePlanner to evaluate task suitability.
 * 
 * Technical Pillar 4: Weather API Integration.
 * Technical Pillar 5: Documentation and Annotations.
 * 
 * @param location                 The name of the city or region.
 * @param time                     The timestamp for when this forecast is valid.
 * @param temperatureCelsius       The ambient temperature in Celsius.
 * @param condition                A summary of weather (e.g., "Sunny", "Rainy").
 * @param precipitationProbability The likelihood of rain or snow (0.0 to 1.0).
 */
public record WeatherForecast(
    String location,
    LocalDateTime time,
    double temperatureCelsius,
    String condition,
    double precipitationProbability
) {
    /**
     * Determines if the weather is considered extreme or dangerous.
     * 
     * @return true if there is a high probability of rain or extreme heat.
     */
    public boolean isExtreme() {
        return precipitationProbability > 0.7 || temperatureCelsius > 40;
    }
}