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

    private Odds getOdds(Game game) {
        Odds odds = new Odds();

        try {
            List<Bookmaker> bookmakers = sportsClient.getOddsByGameID(String.valueOf(game.getId()));

            // Check if bookmakers list is empty
            if (bookmakers == null || bookmakers.isEmpty()) {
                // Set default values or return a default Odds object
                odds.setMoneylineHome("N/A");
                odds.setMoneylineAway("N/A");
                odds.setSpreadHome("N/A");
                odds.setSpreadAway("N/A");
                return odds;
            }

            // Hardcoded to get one bookmaker every time
            Bookmaker bookmaker = bookmakers.get(0);

            // Find the 'Home/Away' bet (id = 2)
            bookmaker.getBets().stream()
                    .filter(bet -> bet.getId() == 2) // Home/Away bet
                    .findFirst()
                    .ifPresentOrElse(
                            homeAwayBet -> {
                                odds.setMoneylineHome(getOddForHomeAwayBet(homeAwayBet, "Home"));
                                odds.setMoneylineAway(getOddForHomeAwayBet(homeAwayBet, "Away"));
                            },
                            () -> {
                                odds.setMoneylineHome("N/A");
                                odds.setMoneylineAway("N/A");
                            }
                    );

            // Find the 'Asian Handicap' bet (id = 3)
            bookmaker.getBets().stream()
                    .filter(bet -> bet.getId() == 3) // Asian Handicap bet
                    .findFirst()
                    .ifPresentOrElse(
                            asianHandicapBet -> {
                                odds.setSpreadHome(getOddForAsianHandicapBet(asianHandicapBet, "Home"));
                                odds.setSpreadAway(getOddForAsianHandicapBet(asianHandicapBet, "Away"));
                            },
                            () -> {
                                odds.setSpreadHome("N/A");
                                odds.setSpreadAway("N/A");
                            }
                    );
        } catch (Exception e) {
            // Log the error
            System.err.println("Error getting odds for game " + game.getId() + ": " + e.getMessage());

            // Set default values
            odds.setMoneylineHome("N/A");
            odds.setMoneylineAway("N/A");
            odds.setSpreadHome("N/A");
            odds.setSpreadAway("N/A");
        }

        return odds;
    }

    private String getOddForHomeAwayBet(Bet bet, String value) {
        return bet.getValues().stream()
                .filter(valueOdd -> valueOdd.getValue().equals(value))
                .map(Value::getOdd) // Extract the odd
                .findFirst()
                .orElse("N/A");
    }

    private String getOddForAsianHandicapBet(Bet bet, String value) {
        List<Value> values = bet.getValues().stream()
                .filter(valueOdd -> valueOdd.getValue().contains(value))
                .toList();

        return values.stream()
                .min(Comparator.comparingDouble(v -> {
                    try {
                        return Math.abs(Double.parseDouble(v.getOdd()) - 1.0);
                    } catch (NumberFormatException e) {
                        return Double.MAX_VALUE;
                    }
                }))
                .map(Value::getOdd)
                .orElse("N/A");
    }

}
