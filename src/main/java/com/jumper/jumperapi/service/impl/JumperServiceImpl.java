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
                                getOddForAsianHandicapBet(asianHandicapBet, odds);
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

    private Odds getOddForAsianHandicapBet(Bet bet, Odds odds) {
        List<Value> values = bet.getValues(); // Get the values for the bet

        Value selectedHomeSpread = null;
        Value selectedAwaySpread = null;
        double smallestDifference = Double.MAX_VALUE; // Initialize with a large value

        // Iterate over the values to find the most even spread odds
        for (Value value : values) {
            String betValue = value.getValue();
            String betOdd = value.getOdd();

            if (betOdd == null || betOdd.isEmpty()) {
                continue; // Skip invalid odds
            }

            double odd = Double.parseDouble(betOdd); // Convert the odd to a double

            // Look for the spread bet for Home (e.g., "Home 1", "Home 2")
            if (betValue.startsWith("Home") && betValue.contains(" ")) {
                // Extract the spread value (e.g., "1", "2" from "Home 1", "Home 2")
                String spread = betValue.split(" ")[1]; // This will give us "1" or "2" etc.

                // Find the corresponding Away bet for the same spread value (without the '+')
                Value awayValue = findAwayBetForSpread(values, spread);
                if (awayValue != null) {
                    double awayOdd = Double.parseDouble(awayValue.getOdd());

                    // Calculate the difference from 2.00 for both Home and Away odds
                    double homeDifference = Math.abs(odd - 2.00);
                    double awayDifference = Math.abs(awayOdd - 2.00);

                    // If the combined difference is smaller than the smallest difference found so far, store it
                    double totalDifference = homeDifference + awayDifference;
                    if (totalDifference < smallestDifference) {
                        smallestDifference = totalDifference;
                        selectedHomeSpread = value;
                        selectedAwaySpread = awayValue;
                    }
                } else {
                    // Debugging: Log a message if no corresponding Away spread was found
                    System.out.println("No matching Away spread found for: " + betValue);
                }
            }
        }

        // If we found a spread with the most even odds, set them to the Odds object
        if (selectedHomeSpread != null && selectedAwaySpread != null) {
            odds.setSpreadHome(selectedHomeSpread.getValue());
            odds.setSpreadHomeOdds(selectedHomeSpread.getOdd());
            odds.setSpreadAway(selectedAwaySpread.getValue());
            odds.setSpreadAwayOdds(selectedAwaySpread.getOdd());
        }

        return odds;
    }

    private Value findAwayBetForSpread(List<Value> values, String spread) {
        // Look for the Away bet that corresponds to the spread value (without the '+' sign)
        for (Value value : values) {
            String valueStr = value.getValue();
            if (valueStr.startsWith("Away") && valueStr.contains(" " + spread)) {
                return value;
            }
        }
        return null; // Return null if no matching Away bet is found
    }


}
