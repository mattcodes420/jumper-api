package com.jumper.jumperapi.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jumper.jumperapi.client.SportsClient;
import com.jumper.jumperapi.model.response.BookmakerModels.Bookmaker;
import com.jumper.jumperapi.model.response.BookmakerResponse;
import com.jumper.jumperapi.model.response.GameScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class SportsClientImpl implements SportsClient {

    private final HttpClient httpClient;
    private final String sportsUrl;

    @Value("${api.key}") // Use the key from your properties file
    private String apiKey;

    @Value("${api.host}") // Use the host from your properties file (e.g., api-basketball.p.rapidapi.com)
    private String apiHost;

    @Value("${sports.league}") // Add constant league value (e.g., 116)
    private String league;

    @Value("${sports.season}") // Add constant season value (e.g., 2024-2025)
    private String season;

    @Value("${sports.timezone}") // Add constant season value (e.g., 2024-2025)
    private String timezone;

    @Value("${sports.bookmaker}") // Add constant season value (e.g., 2024-2025)
    private String bookmaker;

    @Autowired
    public SportsClientImpl(HttpClient httpClient, @Value("${sports.url}") String sportsUrl) {
        this.httpClient = httpClient;
        this.sportsUrl = sportsUrl;
    }

    @Override
    public GameScheduleResponse getScheduleByDate(String date) {
        // Construct the URL with the league, season, and date
        String url = String.format("%s/games?league=%s&date=%s&season=%s&timezone=%s", sportsUrl, league, date, season, timezone);

        // Create the GET request with authentication headers
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-host", apiHost) // Add the host header
                .header("x-rapidapi-key", apiKey)  // Add the API key header
                .build();

        try {
            // Send the request and handle the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // If successful, parse the JSON response into GameSchedule
                return parseJsonToGameSchedule(response.body());
            } else {
                // Handle non-200 status codes
                throw new RuntimeException("Failed to fetch schedule: " + response.statusCode());
            }
        } catch (Exception e) {
            // Handle network or parsing errors
            throw new RuntimeException("Error while fetching schedule", e);
        }
    }

    @Override
    public List<Bookmaker> getOddsByGameID(String id) {
        // Construct the URL with the league, season, and date
        String url = String.format("%s/odds?league=%s&season=%s&bookmaker=%s&game=%s", sportsUrl, league, season, bookmaker, id);

        // Create the GET request with authentication headers
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-host", apiHost) // Add the host header
                .header("x-rapidapi-key", apiKey)  // Add the API key header
                .build();

        try {
            // Send the HTTP request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the response JSON to get the root object
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());

            // Extract the "response" array from the root object
            JsonNode responseArray = rootNode.get("response");

            // Now deserialize the response array into BookmakerResponse objects
            List<Bookmaker> bookmakers = new ArrayList<>();
            if (responseArray != null && responseArray.isArray() && responseArray.size() > 0) {
                // Get the first element in the response array
                BookmakerResponse bookmakerResponse = objectMapper.treeToValue(
                        responseArray.get(0), BookmakerResponse.class);

                // Extract the bookmakers list
                bookmakers = bookmakerResponse.getBookmakers();

                if (!bookmakers.isEmpty()) {
                    Bookmaker bookmaker = bookmakers.get(0);
                    System.out.println("Bookmaker Name: " + bookmaker.getName());
                } else {
                    System.out.println("No bookmakers found.");
                }
            }

            return bookmakers;
        } catch (Exception e) {
            // Handle network or parsing errors
            throw new RuntimeException("Error while fetching odds", e);
        }
    }



    private GameScheduleResponse parseJsonToGameSchedule(String jsonResponse) {
        try {
            // Use Jackson to parse the JSON response into GameSchedule object
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonResponse, GameScheduleResponse.class);
        } catch (Exception e) {
            // Handle JSON parsing errors
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }
}
