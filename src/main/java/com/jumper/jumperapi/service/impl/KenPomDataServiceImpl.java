package com.jumper.jumperapi.service.impl;

import com.jumper.jumperapi.model.KenPomGame;
import com.jumper.jumperapi.service.KenPomDataService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KenPomDataServiceImpl implements KenPomDataService {

    // Only for local testing
    private static final String DATA_DIR = "./data";  // Point to local folder
    //private static final String DATA_DIR = "/app/data";

    /**
     * Gets KenPom game data for a specific date
     *
     * @param dateStr Date in format YYYY-MM-DD
     * @return List of KenPomGame objects
     */
    public List<KenPomGame> getKenPomGamesByDate(String dateStr) {
        // Construct the CSV filename based on date
        String filename = String.format("kenpom_fanmatch_%s.csv", dateStr);
        Path filePath = Paths.get(DATA_DIR, filename);

        // Check if file exists
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(filePath.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            List<KenPomGame> games = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                KenPomGame game = new KenPomGame();

                // Map CSV fields to object properties
                game.setGame(record.get("Game"));
                game.setLocation(record.get("Location"));
                game.setThrillScore(parseDoubleOrNull(record.get("ThrillScore")));
                game.setComeback(parseDoubleOrNull(record.get("Comeback")));
                game.setExcitement(parseDoubleOrNull(record.get("Excitement")));
                game.setThrillScoreRank(parseIntOrNull(record.get("ThrillScoreRank")));
                game.setMvp(record.get("MVP"));
                game.setTournament(record.get("Tournament"));
                game.setPossessions(parseDoubleOrNull(record.get("Possessions")));
                game.setPredictedWinner(record.get("PredictedWinner"));
                game.setPredictedScore(record.get("PredictedScore"));
                game.setWinProbability(record.get("WinProbability"));
                game.setPredictedPossessions(parseDoubleOrNull(record.get("PredictedPossessions")));
                game.setPredictedMOV(parseDoubleOrNull(record.get("PredictedMOV")));
                game.setPredictedLoser(record.get("PredictedLoser"));
                game.setOt(record.get("OT"));
                game.setLoser(record.get("Loser"));
                game.setLoserRank(parseIntOrNull(record.get("LoserRank")));
                game.setLoserScore(parseIntOrNull(record.get("LoserScore")));
                game.setWinner(record.get("Winner"));
                game.setWinnerRank(parseIntOrNull(record.get("WinnerRank")));
                game.setWinnerScore(parseIntOrNull(record.get("WinnerScore")));
                game.setActualMOV(parseDoubleOrNull(record.get("ActualMOV")));

                // Process data for API response format
                processGameData(game);

                games.add(game);
            }

            return games;

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Process raw game data to extract fields needed for API response
     */
    private void processGameData(KenPomGame game) {
        // Parse the game string to extract home and away teams
        String gameStr = game.getGame();
        if (gameStr != null && !gameStr.isEmpty()) {
            if (gameStr.contains("vs.")) {
                // Format: "38 Oklahoma vs. 33 Georgia"
                String[] parts = gameStr.split("vs\\.");
                if (parts.length == 2) {
                    // Extract and clean team names
                    String homeTeam = extractTeamName(parts[0]);
                    String awayTeam = extractTeamName(parts[1]);

                    game.setTeamHome(homeTeam);
                    game.setTeamAway(awayTeam);
                }
            } else if (gameStr.contains(",")) {
                // Format: "46 Texas 79, 45 Vanderbilt 72"
                String[] parts = gameStr.split(",");
                if (parts.length == 2) {
                    // Extract team names from score format
                    String[] homeParts = parts[0].trim().split(" ");
                    String[] awayParts = parts[1].trim().split(" ");

                    if (homeParts.length >= 3 && awayParts.length >= 3) {
                        // Remove rank and score
                        String homeTeam = extractTeamNameFromScore(homeParts);
                        String awayTeam = extractTeamNameFromScore(awayParts);

                        game.setTeamHome(homeTeam);
                        game.setTeamAway(awayTeam);
                    }
                }
            }
        }

        // Set the location and determine if it's neutral
        if (game.getLocation() != null && !game.getLocation().isEmpty()) {
            game.setNeutral(true); // Most tournament games are at neutral sites
        }

        // Set spread and moneyline info from predicted data
        if (game.getPredictedScore() != null && !game.getPredictedScore().isEmpty()) {
            String[] scores = game.getPredictedScore().split("-");
            if (scores.length == 2) {
                try {
                    int homeScore = Integer.parseInt(scores[0].trim());
                    int awayScore = Integer.parseInt(scores[1].trim());

                    double spread = homeScore - awayScore;

                    game.setHomeSpread(String.format("%+.1f", spread));
                    game.setAwaySpread(String.format("%+.1f", -spread));
                } catch (NumberFormatException e) {
                    game.setHomeSpread("N/A");
                    game.setAwaySpread("N/A");
                }
            }
        }

        // Extract moneyline odds from win probability
        if (game.getWinProbability() != null && !game.getWinProbability().isEmpty()) {
            String winProb = game.getWinProbability().replace("%", "");
            try {
                double probability = Double.parseDouble(winProb) / 100.0;

                // Convert probability to American odds
                if (probability > 0.5) { // Favorite
                    int odds = (int) (-100 * (probability / (1 - probability)));
                    game.setHomeML(String.valueOf(odds));
                    game.setAwayML(String.valueOf((int) (100 * ((1 - probability) / probability))));
                } else { // Underdog
                    int odds = (int) (100 * (probability / (1 - probability)));
                    game.setHomeML(String.valueOf(odds));
                    game.setAwayML(String.valueOf((int) (-100 * ((1 - probability) / probability))));
                }
            } catch (NumberFormatException e) {
                game.setHomeML("N/A");
                game.setAwayML("N/A");
            }
        }

        // Set date/time (this would need to be derived from context or other fields)
        game.setDateTime(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Extract team name from team with rank format (e.g., "38 Oklahoma")
     */
    private String extractTeamName(String rawTeam) {
        String[] parts = rawTeam.trim().split(" ", 2);
        return parts.length > 1 ? parts[1].trim() : rawTeam.trim();
    }

    /**
     * Extract team name from score format (e.g., "46 Texas 79")
     */
    private String extractTeamNameFromScore(String[] parts) {
        // Skip first part (rank) and last part (score)
        StringBuilder teamName = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            if (!teamName.isEmpty()) {
                teamName.append(" ");
            }
            teamName.append(parts[i]);
        }
        return teamName.toString();
    }

    /**
     * Parse string to Double, return null if not a number
     */
    private Double parseDoubleOrNull(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("nan")) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse string to Integer, return null if not a number
     */
    private Integer parseIntOrNull(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("nan")) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Find all available KenPom data files
     *
     * @return List of available dates
     */
    public List<String> getAvailableDates() {
        File dir = new File(DATA_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }

        String[] files = dir.list((d, name) -> name.startsWith("kenpom_fanmatch_") && name.endsWith(".csv"));
        if (files == null) {
            return new ArrayList<>();
        }

        return Stream.of(files)
                .map(filename -> filename.replace("kenpom_fanmatch_", "").replace(".csv", ""))
                .collect(Collectors.toList());
    }
}