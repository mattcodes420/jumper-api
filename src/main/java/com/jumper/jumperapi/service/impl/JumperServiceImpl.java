package com.jumper.jumperapi.service.impl;

import com.jumper.jumperapi.client.SportsClient;
import com.jumper.jumperapi.model.GameResponse;
import com.jumper.jumperapi.model.KenPomGame;
import com.jumper.jumperapi.model.Odds;
import com.jumper.jumperapi.model.Predictions;
import com.jumper.jumperapi.model.response.BookmakerModels.Bet;
import com.jumper.jumperapi.model.response.BookmakerModels.Bookmaker;
import com.jumper.jumperapi.model.response.BookmakerModels.Value;
import com.jumper.jumperapi.model.response.BookmakerResponse;
import com.jumper.jumperapi.model.response.GameScheduleModels.Game;
import com.jumper.jumperapi.model.response.GameScheduleResponse;
import com.jumper.jumperapi.service.JumperService;
import com.jumper.jumperapi.service.KenPomDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JumperServiceImpl implements JumperService {

    private final SportsClient sportsClient;
    private final KenPomDataService kenPomDataService;
    private String NO_ODDS_AVAILABLE = "No odds currently available for this game";
    private String NO_PREDICTIONS_AVAILABLE = "No predictions available for this game";

    @Autowired
    public JumperServiceImpl(SportsClient sportsClient, KenPomDataService kenPomDataService) {
        this.sportsClient = sportsClient;
        this.kenPomDataService = kenPomDataService;
    }

    @Override
    public List<GameResponse> getScheduleByDate(String date) {
        GameScheduleResponse response = sportsClient.getScheduleByDate(date);
        List<GameResponse> gameResponses = new ArrayList<>();

        // Get KenPom data for this date
        List<KenPomGame> kenPomGames = kenPomDataService.getKenPomGamesByDate(date);

        // Iterate over each game in the response
        response.getResponse()
                .forEach(game -> {
                    // Create a new GameResponse object for each game
                    GameResponse gameResponse = new GameResponse();

                    // Set the game details into the GameResponse object
                    gameResponse.setGame(game);

                    // Fetch the odds for the game
                    Odds odds = getOdds(game);
                    gameResponse.setOdds(odds);

                    // Find and set matching KenPom data if available
                    KenPomGame matchingKenPomGame = findMatchingKenPomGame(game, kenPomGames);
                    if (matchingKenPomGame != null) {
                        gameResponse.setKenPomGame(matchingKenPomGame);

                        // Get the home and away teams from the game
                        String homeTeam = game.getTeams().getHome().getName();
                        String awayTeam = game.getTeams().getAway().getName();
                        // If we have KenPom data, we can also set predictions
                        if (!Objects.equals(odds.getMoneylineAway(), NO_ODDS_AVAILABLE))
                        {
                            Predictions predictions = createPredictionsFromKenPom(matchingKenPomGame, odds, homeTeam, awayTeam);
                            gameResponse.setPredictions(predictions);
                        }
                        else {
                            // Set default predictions if no odds are available
                            Predictions predictions = new Predictions();
                            setDefaultPredictions(matchingKenPomGame, predictions);
                            gameResponse.setPredictions(predictions);
                        }
                    }

                    // Add the GameResponse to the list
                    gameResponses.add(gameResponse);
                });

        return gameResponses;
    }

    /**
     * Find the matching KenPom game data for a given API game
     */
    private KenPomGame findMatchingKenPomGame(Game game, List<KenPomGame> kenPomGames) {
        if (kenPomGames == null || kenPomGames.isEmpty()) {
            return null;
        }

        // Get team names from the API Game object
        String homeTeamName = game.getTeams().getHome().getName();
        String awayTeamName = game.getTeams().getAway().getName();

        // Try to find the best matching KenPom game
        return kenPomGames.stream()
                .filter(kenPomGame -> {
                    String kenPomHome = kenPomGame.getTeamHome();
                    String kenPomAway = kenPomGame.getTeamAway();

                    if (kenPomHome == null || kenPomAway == null) {
                        return false;
                    }

                    // Normalize team names for better matching
                    String normalizedHomeTeam = normalizeTeamName(homeTeamName);
                    String normalizedAwayTeam = normalizeTeamName(awayTeamName);
                    String normalizedKenPomHome = normalizeTeamName(kenPomHome);
                    String normalizedKenPomAway = normalizeTeamName(kenPomAway);

                    // Check regular and flipped team configurations
                    return (matchTeams(normalizedKenPomHome, normalizedHomeTeam) &&
                            matchTeams(normalizedKenPomAway, normalizedAwayTeam)) ||
                            (matchTeams(normalizedKenPomHome, normalizedAwayTeam) &&
                                    matchTeams(normalizedKenPomAway, normalizedHomeTeam));
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Normalize team name by removing common suffixes and conference indicators.
     */
    private String normalizeTeamName(String teamName) {

        if (teamName.toLowerCase().contains("john's")){
            teamName = "St. John's (N.Y.)";
        }
        if (teamName.toLowerCase().contains("vcu")){
            teamName = "VCU Rams";
        }
        if (teamName.toLowerCase().contains("byu")){
            teamName = "Brigham Young";
        }
        if (teamName.toLowerCase().contains("siu")){
            teamName = "Siu Edwardsville";
        }
        if (teamName.toLowerCase().contains("nebraska")){
            teamName = "Nebraska O.";
        }
        if (Objects.equals(teamName.toLowerCase(), "mississippi")){
            teamName = "Ole Miss";
        }
        if (teamName == null) return "";

        // Convert to lowercase and trim
        String normalized = teamName.toLowerCase().trim();

        // Remove common suffixes like "Rams", "Bulldogs", etc.
        int spaceIdx = normalized.lastIndexOf(' ');
        if (spaceIdx > 0 && spaceIdx < normalized.length() - 1) {
            String lastWord = normalized.substring(spaceIdx + 1);
            // If last word appears to be a mascot/suffix, remove it
            if (!lastWord.contains(".") && lastWord.length() > 2 && lastWord.contains("(")) {
                normalized = normalized.substring(0, spaceIdx);
            }
        }

        // Remove conference designations like "B10-T", "ACC-T", etc.
        normalized = normalized.replaceAll("\\s+[a-z0-9]+-t$", "");

        // Remove apostrophes
        normalized = normalized.replace("'", "");

        return normalized;
    }

    /**
     * Check if two team names match.
     */
    private boolean matchTeams(String team1, String team2) {
        // Direct match
        if (team1.equals(team2)) {
            return true;
        }

        // Handle St. vs Saint variations
        String processed1 = team1.replace("st.", "saint").replace("saint", "st");
        String processed2 = team2.replace("st.", "saint").replace("saint", "st");
        processed1 = team1.replace("state", "st");
        processed2 = team2.replace("state", "st");

        if (processed1.equals(processed2)) {
            return true;
        }

        // Check if one name contains the core part of the other
        String[] words1 = processed1.split("\\s+");
        String[] words2 = processed2.split("\\s+");

        // Get the most significant word (usually the first one, except for cases like "North Carolina")
        String core1 = words1.length > 0 ? words1[words1.length > 1 ? 1 : 0] : "";
        String core2 = words2.length > 0 ? words2[words2.length > 1 ? 1 : 0] : "";

        // If core words are substantial (to avoid generic matches), check if they match
        if (core1.length() > 3 && core2.length() > 3) {
            return core1.equals(core2);
        }

        // One name substantially contains the other
        return (processed1.length() > 4 && processed2.contains(processed1)) ||
                (processed2.length() > 4 && processed1.contains(processed2));
    }

    /**
     * Create predictions based on KenPom data
     */
    private Predictions createPredictionsFromKenPom(KenPomGame kenPomGame, Odds odds, String homeTeam, String awayTeam) {
        Predictions predictions = new Predictions();

        try {
            // Check if vales are valid before parsing
            String spreadHome = odds.getSpreadHome();
            String spreadAway = odds.getSpreadAway();
            String moneylineHome = odds.getMoneylineHome();
            String moneylineAway = odds.getMoneylineAway();
            String winProbability = kenPomGame.getWinProbability();

            // Validate if any required value is missing
            if (spreadHome == null || spreadHome.isEmpty() || spreadHome.equals(NO_ODDS_AVAILABLE) ||
                    spreadAway == null || spreadAway.isEmpty() || spreadAway.equals(NO_ODDS_AVAILABLE) ||
                    moneylineHome == null || moneylineHome.isEmpty() || moneylineHome.equals(NO_ODDS_AVAILABLE) ||
                    moneylineAway == null || moneylineAway.isEmpty() || moneylineAway.equals(NO_ODDS_AVAILABLE) ||
                    winProbability == null || winProbability.isEmpty()) {

                // Set default predictions if any data is missing
                setDefaultPredictions(kenPomGame, predictions);
                return predictions;
            }

            Double getPredictedMOV = kenPomGame.getPredictedMOV();
            Double getHomeSpread = Double.valueOf(odds.getSpreadHome());
            Double getAwaySpread = Double.parseDouble(odds.getSpreadAway());
            Double getMoneylineHome = Math.round(setImpliedOdds(Double.parseDouble(odds.getMoneylineHome())) * 10.0) / 10.0;
            Double getMoneylineAway = Math.round(setImpliedOdds(Double.parseDouble(odds.getMoneylineAway())) * 10.0) / 10.0;
            Double getPredictedWinProb = Double.valueOf(kenPomGame.getWinProbability().split("%")[0]);

            String normalizedHomeTeam = normalizeTeamName(homeTeam);
            String normalizedAwayTeam = normalizeTeamName(awayTeam);
            String normalizedPredictedWinner = normalizeTeamName(kenPomGame.getPredictedWinner());

            if (matchTeams(normalizedHomeTeam, normalizedPredictedWinner))
            {
                if (getMoneylineHome < getPredictedWinProb)
                {
                    predictions.setMoneylinePrediction(homeTeam + " has best value at " + getPredictedWinProb + "% vs the moneyline implied odds of " + getMoneylineHome + "%");
                }
                else if (getMoneylineAway > (100 - getPredictedWinProb)){
                    predictions.setMoneylinePrediction("Both moneyline bets are overvalued");
                }
                else
                {
                    if (((100 - getPredictedWinProb) - getMoneylineAway) > 5)
                    {
                        predictions.setMoneylinePrediction(awayTeam + " has best value at " + (100 - getPredictedWinProb) + "% vs the moneyline implied odds of " + getMoneylineAway + "%");
                    }
                    else
                    {
                        predictions.setMoneylinePrediction("Not enough value to bet on " + awayTeam);
                    }
                }

                getPredictedMOV = -getPredictedMOV;
                if (getPredictedMOV > getHomeSpread)
                {
                    predictions.setSpreadPrediction(awayTeam + " has best value at " + getAwaySpread + " vs the spread of " + -getPredictedMOV);
                }
                else if (getPredictedMOV < getHomeSpread)
                {
                    predictions.setSpreadPrediction(homeTeam + " has best value at " + getHomeSpread + " vs the spread of " + getPredictedMOV);
                }
                else
                {
                    predictions.setSpreadPrediction("No spread value available");
                }
            }
            else if (matchTeams(normalizedAwayTeam, normalizedPredictedWinner))
            {
                if (getMoneylineAway < getPredictedWinProb)
                {
                    predictions.setMoneylinePrediction(awayTeam + " has best value at " + getPredictedWinProb + "% vs the moneyline implied odds of " + getMoneylineAway + "%");
                }
                else if (getMoneylineHome > (100 - getPredictedWinProb)){
                    predictions.setMoneylinePrediction("Both moneyline bets are overvalued");
                }
                else
                {
                    if (((100 - getPredictedWinProb) - getMoneylineHome) > 5)
                    {
                        predictions.setMoneylinePrediction(homeTeam + " has best value at " + (100 - getPredictedWinProb) + "% vs the moneyline implied odds of " + getMoneylineHome + "%");
                    }
                    else
                    {
                        predictions.setMoneylinePrediction("Not enough value to bet on " + homeTeam);
                    }
                }

                getPredictedMOV = -getPredictedMOV;
                if (getPredictedMOV > getAwaySpread)
                {
                    predictions.setSpreadPrediction(homeTeam + " has best value at " + getHomeSpread + " vs the spread of " + -getPredictedMOV);
                }
                else if (getPredictedMOV < getAwaySpread)
                {
                    predictions.setSpreadPrediction(awayTeam + " has best value at " + getAwaySpread + " vs the spread of " + getPredictedMOV);
                }
                else
                {
                    predictions.setSpreadPrediction("No spread value available");
                }
            }
            else
            {
                predictions.setSpreadPrediction("No spread value available");
            }
        } catch (Exception e) {
            // Fallback if any exception occurs while parsing KenPom data
            predictions.setMoneylinePrediction("Unable to calculate predictions due to data error");
            predictions.setSpreadPrediction("Unable to calculate predictions due to data error");
        }

        return predictions;
    }

    private void setDefaultPredictions(KenPomGame kenPomGame, Predictions predictions) {
        String winProbability;
        String getPredictedWinner = (kenPomGame.getPredictedWinner() == null || kenPomGame.getPredictedWinner().isEmpty())
                ? NO_PREDICTIONS_AVAILABLE
                : kenPomGame.getPredictedWinner();
        winProbability = (kenPomGame.getWinProbability() == null || kenPomGame.getWinProbability().isEmpty())
                ? NO_PREDICTIONS_AVAILABLE
                : kenPomGame.getWinProbability();
        String getPredictedMOV = (kenPomGame.getPredictedMOV() == null)
                ? NO_PREDICTIONS_AVAILABLE
                : String.valueOf(kenPomGame.getPredictedMOV());

        if (Objects.equals(getPredictedWinner, NO_PREDICTIONS_AVAILABLE)){
            predictions.setMoneylinePrediction(NO_PREDICTIONS_AVAILABLE);
            predictions.setSpreadPrediction(NO_PREDICTIONS_AVAILABLE);
        }
        else {
            if (Objects.equals(winProbability, NO_PREDICTIONS_AVAILABLE)) {
                predictions.setMoneylinePrediction(getPredictedWinner + " is the predicted winner. No win probability available.");
            }
            else {
                predictions.setMoneylinePrediction(getPredictedWinner + " has a predicted win probability of " + winProbability);
            }
            if (Objects.equals(getPredictedMOV, NO_PREDICTIONS_AVAILABLE)) {
                predictions.setSpreadPrediction(getPredictedWinner + " is the predicted winner. No margin of victory available.");
            }
            else {
                predictions.setSpreadPrediction(getPredictedWinner + " has a predicted margin of victory of " + getPredictedMOV);
            }
        }
    }

    private Double setImpliedOdds(double odds) {
        return 1 / odds * 100;
    }

    private Odds getOdds(Game game) {
        Odds odds = new Odds();

        try {
            List<Bookmaker> bookmakers = sportsClient.getOddsByGameID(String.valueOf(game.getId()));

            // Check if bookmakers list is empty
            if (bookmakers == null || bookmakers.isEmpty()) {
                // Set default values or return a default Odds object
                odds.setMoneylineHome(NO_ODDS_AVAILABLE);
                odds.setMoneylineAway(NO_ODDS_AVAILABLE);
                odds.setSpreadHome(NO_ODDS_AVAILABLE);
                odds.setSpreadAway(NO_ODDS_AVAILABLE);
                odds.setSpreadAwayOdds(NO_ODDS_AVAILABLE);
                odds.setSpreadHomeOdds(NO_ODDS_AVAILABLE);
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
                                odds.setMoneylineHome(NO_ODDS_AVAILABLE);
                                odds.setMoneylineAway(NO_ODDS_AVAILABLE);
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
                                odds.setSpreadHome(NO_ODDS_AVAILABLE);
                                odds.setSpreadAway(NO_ODDS_AVAILABLE);
                            }
                    );
        } catch (Exception e) {
            // Log the error
            System.err.println("Error getting odds for game " + game.getId() + ": " + e.getMessage());

            // Set default values
            odds.setMoneylineHome(NO_ODDS_AVAILABLE);
            odds.setMoneylineAway(NO_ODDS_AVAILABLE);
            odds.setSpreadHome(NO_ODDS_AVAILABLE);
            odds.setSpreadAway(NO_ODDS_AVAILABLE);
        }

        return odds;
    }

    private String getOddForHomeAwayBet(Bet bet, String value) {
        return bet.getValues().stream()
                .filter(valueOdd -> valueOdd.getValue().equals(value))
                .map(Value::getOdd) // Extract the odd
                .findFirst()
                .orElse(NO_ODDS_AVAILABLE);
    }

    private Odds getOddForAsianHandicapBet(Bet bet, Odds odds) {
        List<Value> values = bet.getValues(); // Get the values for the bet
        Double getMoneylineHome = setImpliedOdds(Double.parseDouble(odds.getMoneylineHome()));
        Double getMoneylineAway = setImpliedOdds(Double.parseDouble(odds.getMoneylineAway()));

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
            double spreadNumber = Double.parseDouble(selectedHomeSpread.getValue().split(" ")[1]);
            if (getMoneylineHome >= getMoneylineAway) {
                selectedHomeSpread.setValue(String.valueOf(+spreadNumber));
                selectedAwaySpread.setValue(String.valueOf(-spreadNumber));
            }
            else {
                selectedHomeSpread.setValue(String.valueOf(-spreadNumber));
                selectedAwaySpread.setValue(String.valueOf(+spreadNumber));
            }
            odds.setSpreadHome(selectedHomeSpread.getValue());
            odds.setSpreadHomeOdds(selectedHomeSpread.getOdd());
            odds.setSpreadAway(selectedAwaySpread.getValue());
            odds.setSpreadAwayOdds(selectedAwaySpread.getOdd());
        }
        else {
            odds.setSpreadHomeOdds(NO_ODDS_AVAILABLE);
            odds.setSpreadAway(NO_ODDS_AVAILABLE);
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
