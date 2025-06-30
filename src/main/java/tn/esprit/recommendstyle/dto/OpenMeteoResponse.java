package tn.esprit.recommendstyle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.twilio.rest.api.v2010.account.usage.record.Daily;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Getter
@Setter
public class OpenMeteoResponse {
    private Current current;
    private Daily daily;
    @Data
    @Getter
    @Setter
    public static class Current {
        private String time;
        private double temperature_2m;
        private int relative_humidity_2m;
        private double apparent_temperature;
        private int weather_code;
        private double wind_speed_10m;
        private double pressure_msl;
        private double visibility;


    }
    @Data
    @Getter
    @Setter
    public static class Daily {
        private List<String> time;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
        private List<Integer> weather_code;
        private List<Double> precipitation_sum;
        private List<Double> wind_speed_10m_max;


        }




    }

