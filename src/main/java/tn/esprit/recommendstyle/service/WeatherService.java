package tn.esprit.recommendstyle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import tn.esprit.recommendstyle.dto.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private final RestTemplate restTemplate;
    private final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";

    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }

    public WeatherResponse getCurrentWeather(double latitude, double longitude) {
        try {
            String weatherUrl = String.format(
                    "%s?latitude=%f&longitude=%f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,apparent_temperature,pressure_msl,visibility&timezone=auto",
                    OPEN_METEO_URL, latitude, longitude
            );

            OpenMeteoResponse weatherResponse = restTemplate.getForObject(weatherUrl, OpenMeteoResponse.class);
            String locationName = getLocationName(latitude, longitude);

            return mapToWeatherResponse(weatherResponse, locationName);
        } catch (RestClientException e) {
            throw new RuntimeException("Erreur de connexion à l'API météo", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement des données météo", e);
        }
    }

    public WeatherResponse getForecast(double latitude, double longitude, int days) {
        try {
            String forecastUrl = String.format(
                    "%s?latitude=%f&longitude=%f&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_sum,wind_speed_10m_max&timezone=auto&forecast_days=%d",
                    OPEN_METEO_URL, latitude, longitude, days
            );

            OpenMeteoResponse forecastResponse = restTemplate.getForObject(forecastUrl, OpenMeteoResponse.class);
            String locationName = getLocationName(latitude, longitude);

            return mapToForecastResponse(forecastResponse, locationName);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des prévisions", e);
        }
    }

    public List<LocationResult> searchLocations(String query) {
        try {
            String searchUrl = String.format("%s?name=%s&count=10&language=fr&format=json",
                    GEOCODING_URL, query);

            GeocodingResponse response = restTemplate.getForObject(searchUrl, GeocodingResponse.class);

            List<LocationResult> results = new ArrayList<>();
            if (response != null && response.getResults() != null) {
                for (GeocodingResponse.Location location : response.getResults()) {
                    LocationResult result = new LocationResult();
                    result.setName(location.getName());
                    result.setCountry(location.getCountry());
                    result.setLatitude(location.getLatitude());
                    result.setLongitude(location.getLongitude());
                    if (location.getAdmin1() != null) {
                        result.setRegion(location.getAdmin1());
                    }
                    results.add(result);
                }
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de locations", e);
        }
    }

    private String getLocationName(double latitude, double longitude) {
        try {
            String geoUrl = String.format(
                    "%s?latitude=%f&longitude=%f&count=1&language=fr",
                    GEOCODING_URL, latitude, longitude
            );

            GeocodingResponse geoResponse = restTemplate.getForObject(geoUrl, GeocodingResponse.class);

            if (geoResponse != null && geoResponse.getResults() != null && !geoResponse.getResults().isEmpty()) {
                GeocodingResponse.Location location = geoResponse.getResults().get(0);
                String name = location.getName();
                String country = location.getCountry();
                String admin1 = location.getAdmin1();

                if (admin1 != null && !admin1.equals(name)) {
                    return name + ", " + admin1 + ", " + country;
                }
                return name + ", " + country;
            }

            return String.format("%.2f, %.2f", latitude, longitude);
        } catch (Exception e) {
            return String.format("%.2f, %.2f", latitude, longitude);
        }
    }

    private WeatherResponse mapToWeatherResponse(OpenMeteoResponse apiResponse, String locationName) {
        WeatherResponse response = new WeatherResponse();

        if (apiResponse != null && apiResponse.getCurrent() != null) {
            OpenMeteoResponse.Current current = apiResponse.getCurrent();

            response.setLocation(locationName);
            response.setTemperature((int) Math.round(current.getTemperature_2m()));
            response.setDescription(getWeatherDescription(current.getWeather_code()));
            response.setHumidity(current.getRelative_humidity_2m());
            response.setWindSpeed(Math.round(current.getWind_speed_10m() * 10.0) / 10.0);
            response.setFeelsLike((int) Math.round(current.getApparent_temperature()));
            response.setIcon(getWeatherIcon(current.getWeather_code()));
            response.setPressure(current.getPressure_msl());
            response.setVisibility(current.getVisibility());
            response.setTimestamp(current.getTime());
        }

        return response;
    }

    private WeatherResponse mapToForecastResponse(OpenMeteoResponse apiResponse, String locationName) {
        WeatherResponse response = new WeatherResponse();
        response.setLocation(locationName);

        if (apiResponse != null && apiResponse.getDaily() != null) {
            OpenMeteoResponse.Daily daily = apiResponse.getDaily();
            List<DailyForecast> forecasts = new ArrayList<>();

            for (int i = 0; i < daily.getTime().size(); i++) {
                DailyForecast forecast = new DailyForecast();
                forecast.setDate(daily.getTime().get(i));
                forecast.setMaxTemp((int) Math.round(daily.getTemperature_2m_max().get(i)));
                forecast.setMinTemp((int) Math.round(daily.getTemperature_2m_min().get(i)));
                forecast.setWeatherCode(daily.getWeather_code().get(i));
                forecast.setDescription(getWeatherDescription(daily.getWeather_code().get(i)));
                forecast.setIcon(getWeatherIcon(daily.getWeather_code().get(i)));
                forecast.setPrecipitation(daily.getPrecipitation_sum().get(i));
                forecast.setWindSpeed(daily.getWind_speed_10m_max().get(i));
                forecasts.add(forecast);
            }

            response.setDailyForecasts(forecasts);
        }

        return response;
    }

    private String getWeatherDescription(int weatherCode) {
        switch (weatherCode) {
            case 0: return "Ciel dégagé";
            case 1: return "Principalement dégagé";
            case 2: return "Partiellement nuageux";
            case 3: return "Couvert";
            case 45: case 48: return "Brouillard";
            case 51: case 53: case 55: return "Bruine";
            case 56: case 57: return "Bruine verglaçante";
            case 61: case 63: case 65: return "Pluie";
            case 66: case 67: return "Pluie verglaçante";
            case 71: case 73: case 75: return "Neige";
            case 77: return "Grains de neige";
            case 80: case 81: case 82: return "Averses de pluie";
            case 85: case 86: return "Averses de neige";
            case 95: return "Orage";
            case 96: case 99: return "Orage avec grêle";
            default: return "Conditions météo inconnues";
        }
    }

    private String getWeatherIcon(int weatherCode) {
        switch (weatherCode) {
            case 0: return "☀️";
            case 1: return "🌤️";
            case 2: return "⛅";
            case 3: return "☁️";
            case 45: case 48: return "🌫️";
            case 51: case 53: case 55: case 56: case 57: return "🌦️";
            case 61: case 63: case 65: case 66: case 67: return "🌧️";
            case 71: case 73: case 75: case 77: return "🌨️";
            case 80: case 81: case 82: return "🌦️";
            case 85: case 86: return "🌨️";
            case 95: case 96: case 99: return "⛈️";
            default: return "🌡️";
        }
    }
}
