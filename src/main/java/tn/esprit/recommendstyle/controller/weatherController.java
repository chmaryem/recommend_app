package tn.esprit.recommendstyle.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tn.esprit.recommendstyle.dto.WeatherResponse;
import tn.esprit.recommendstyle.service.WeatherService;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class weatherController {


    @GetMapping("/weather/by-coords")
    public ResponseEntity<?> getWeatherByCoords(@RequestParam double lat, @RequestParam double lon) {
        String apiKey = "a31b37f448517c64a3dfa01b0de3ee6a";
        String weatherUrl = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, apiKey
        );

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> weatherResponse = restTemplate.getForEntity(weatherUrl, Map.class);
        return ResponseEntity.ok(weatherResponse.getBody());
    }
}
