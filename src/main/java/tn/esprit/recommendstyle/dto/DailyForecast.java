package tn.esprit.recommendstyle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
@Setter
public class DailyForecast {
    private String date;
    private int maxTemp;
    private int minTemp;
    private int weatherCode;
    private String description;
    private String icon;
    private double precipitation;
    private double windSpeed;
}
