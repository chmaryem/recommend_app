package tn.esprit.recommendstyle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
@Setter
public class WeatherResponse {
    private String location;
    private int temperature;
    private String description;
    private int humidity;
    private double windSpeed;
    private String icon;
    private int feelsLike;
    private String error;
    private double pressure;
    private double visibility;
    private String timestamp;
    private List<DailyForecast> dailyForecasts;

}
