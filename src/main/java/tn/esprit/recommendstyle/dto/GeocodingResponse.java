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
public class GeocodingResponse {
    private List<Location> results;


    @Data
    @Getter
    @Setter
    public static class Location {
        private String name;
        private String country;
        private String admin1;
        private double latitude;
        private double longitude;


    }
}
