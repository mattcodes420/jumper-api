package com.jumper.jumperapi.service.impl;

import com.jumper.jumperapi.client.SportsClient;
import com.jumper.jumperapi.model.GameResponse;
import com.jumper.jumperapi.model.Odds;
import com.jumper.jumperapi.model.response.BookmakerModels.Bet;
import com.jumper.jumperapi.model.response.BookmakerModels.Bookmaker;
import com.jumper.jumperapi.model.response.BookmakerModels.Value;
import com.jumper.jumperapi.model.response.BookmakerResponse;
import com.jumper.jumperapi.model.response.GameScheduleModels.Game;
import com.jumper.jumperapi.model.response.GameScheduleResponse;
import com.jumper.jumperapi.service.JumperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JumperServiceImpl implements JumperService {

    private final SportsClient sportsClient;

    @Autowired
    public JumperServiceImpl(SportsClient sportsClient) {this.sportsClient = sportsClient;}

    @Override
    public List<GameResponse> getScheduleByDate(String date){
        GameScheduleResponse response = sportsClient.getScheduleByDate(date);
        List<GameResponse> gameResponses = new ArrayList<>();

        // Iterate over each game in the response, map it to a GameResponse, and collect them in the list
        response.getResponse()
                .forEach(game -> {
                    // Create a new GameResponse object for each game
                    GameResponse gameResponse = new GameResponse();

                    // Set the game details into the GameResponse object
                    gameResponse.setGame(game); // Assuming GameResponse has a setGame() method

                    // Fetch the odds for the game
                    Odds odds = getOdds(game); // Assuming getOdds is a method that fetches the odds for the game

                    // Set the odds in the GameResponse object
                    gameResponse.setOdds(odds); // Assuming GameResponse has a setOdds() method

                    // Add the GameResponse to the list
                    gameResponses.add(gameResponse);
                });

        return gameResponses;
    }

    private Odds getOdds(Game game){
        List<Bookmaker> bookmakers = sportsClient.getOddsByGameID(String.valueOf(game.getId()));
        // Hardcoded to get one bookmaker every time
        Bookmaker bookmaker = bookmakers.get(0);  // You might need to select which bookmaker you are using

        // Find the 'Home/Away' bet (id = 2)
        Bet homeAwayBet = bookmaker.getBets().stream()
                .filter(bet -> bet.getId() == 2) // Home/Away bet
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Home/Away bet not found"));

        // Find the 'Asian Handicap' bet (id = 3)
        Bet asianHandicapBet = bookmaker.getBets().stream()
                .filter(bet -> bet.getId() == 3) // Asian Handicap bet
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asian Handicap bet not found"));


        Odds odds = new Odds();

        // Handling "Home/Away" bet
        odds.setMoneylineHome(getOddForHomeAwayBet(homeAwayBet, "Home"));
        odds.setMoneylineAway(getOddForHomeAwayBet(homeAwayBet, "Away"));

        // Handling "Asian Handicap" bet
        // Find the "Home" and "Away" odds that are the closest
        odds.setSpreadHome(getOddForAsianHandicapBet(asianHandicapBet, "Home"));
        odds.setSpreadAway(getOddForAsianHandicapBet(asianHandicapBet, "Away"));

        return odds;
    }

    private String getOddForHomeAwayBet(Bet bet, String value) {
        return bet.getValues().stream()
                .filter(valueOdd -> valueOdd.getValue().equals(value))
                .map(Value::getOdd) // Extract the odd
                .findFirst()
                .orElseThrow(() -> new RuntimeException(value + " odd not found"));
    }

    // Helper method to find the most balanced odd for "Asian Handicap"
    private String getOddForAsianHandicapBet(Bet bet, String value) {
        // Find the pair of odds with the smallest difference for the given "Home" or "Away"
        List<Value> values = bet.getValues().stream()
                .filter(valueOdd -> valueOdd.getValue().contains(value)) // Filter Home or Away
                .toList();

        // If there are multiple "Home" or "Away" odds, we need to pick the most even one
        return values.stream()
                .min(Comparator.comparingDouble(v -> Math.abs(Double.parseDouble(v.getOdd()) - 1.0))) // Find the most even odds
                .map(Value::getOdd) // Get the odd of the most even pair
                .orElseThrow(() -> new RuntimeException("No valid odds found for " + value));
    }

}
