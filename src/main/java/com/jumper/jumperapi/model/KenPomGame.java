package com.jumper.jumperapi.model;

import lombok.Data;

@Data
public class KenPomGame {
    private String game;
    private String location;
    private Double thrillScore;
    private Double comeback;
    private Double excitement;
    private Integer thrillScoreRank;
    private String mvp;
    private String tournament;
    private Double possessions;
    private String predictedWinner;
    private String predictedScore;
    private String winProbability;
    private Double predictedPossessions;
    private Double predictedMOV;
    private String predictedLoser;
    private String ot;
    private String loser;
    private Integer loserRank;
    private Integer loserScore;
    private String winner;
    private Integer winnerRank;
    private Integer winnerScore;
    private Double actualMOV;

    // Derived fields for API responses
    private String teamHome;
    private String teamAway;
    private String dateTime;
    private String homeSpread;
    private String awaySpread;
    private String homeML;
    private String awayML;
    private boolean neutral;

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getThrillScore() {
        return thrillScore;
    }

    public void setThrillScore(Double thrillScore) {
        this.thrillScore = thrillScore;
    }

    public Double getComeback() {
        return comeback;
    }

    public void setComeback(Double comeback) {
        this.comeback = comeback;
    }

    public Double getExcitement() {
        return excitement;
    }

    public void setExcitement(Double excitement) {
        this.excitement = excitement;
    }

    public Integer getThrillScoreRank() {
        return thrillScoreRank;
    }

    public void setThrillScoreRank(Integer thrillScoreRank) {
        this.thrillScoreRank = thrillScoreRank;
    }

    public String getMvp() {
        return mvp;
    }

    public void setMvp(String mvp) {
        this.mvp = mvp;
    }

    public String getTournament() {
        return tournament;
    }

    public void setTournament(String tournament) {
        this.tournament = tournament;
    }

    public Double getPossessions() {
        return possessions;
    }

    public void setPossessions(Double possessions) {
        this.possessions = possessions;
    }

    public String getPredictedWinner() {
        return predictedWinner;
    }

    public void setPredictedWinner(String predictedWinner) {
        this.predictedWinner = predictedWinner;
    }

    public String getPredictedScore() {
        return predictedScore;
    }

    public void setPredictedScore(String predictedScore) {
        this.predictedScore = predictedScore;
    }

    public String getWinProbability() {
        return winProbability;
    }

    public void setWinProbability(String winProbability) {
        this.winProbability = winProbability;
    }

    public Double getPredictedPossessions() {
        return predictedPossessions;
    }

    public void setPredictedPossessions(Double predictedPossessions) {
        this.predictedPossessions = predictedPossessions;
    }

    public Double getPredictedMOV() {
        return predictedMOV;
    }

    public void setPredictedMOV(Double predictedMOV) {
        this.predictedMOV = predictedMOV;
    }

    public String getPredictedLoser() {
        return predictedLoser;
    }

    public void setPredictedLoser(String predictedLoser) {
        this.predictedLoser = predictedLoser;
    }

    public String getOt() {
        return ot;
    }

    public void setOt(String ot) {
        this.ot = ot;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }

    public Integer getLoserRank() {
        return loserRank;
    }

    public void setLoserRank(Integer loserRank) {
        this.loserRank = loserRank;
    }

    public Integer getLoserScore() {
        return loserScore;
    }

    public void setLoserScore(Integer loserScore) {
        this.loserScore = loserScore;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Integer getWinnerRank() {
        return winnerRank;
    }

    public void setWinnerRank(Integer winnerRank) {
        this.winnerRank = winnerRank;
    }

    public Integer getWinnerScore() {
        return winnerScore;
    }

    public void setWinnerScore(Integer winnerScore) {
        this.winnerScore = winnerScore;
    }

    public Double getActualMOV() {
        return actualMOV;
    }

    public void setActualMOV(Double actualMOV) {
        this.actualMOV = actualMOV;
    }

    public String getTeamHome() {
        return teamHome;
    }

    public void setTeamHome(String teamHome) {
        this.teamHome = teamHome;
    }

    public String getTeamAway() {
        return teamAway;
    }

    public void setTeamAway(String teamAway) {
        this.teamAway = teamAway;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getHomeSpread() {
        return homeSpread;
    }

    public void setHomeSpread(String homeSpread) {
        this.homeSpread = homeSpread;
    }

    public String getAwaySpread() {
        return awaySpread;
    }

    public void setAwaySpread(String awaySpread) {
        this.awaySpread = awaySpread;
    }

    public String getHomeML() {
        return homeML;
    }

    public void setHomeML(String homeML) {
        this.homeML = homeML;
    }

    public String getAwayML() {
        return awayML;
    }

    public void setAwayML(String awayML) {
        this.awayML = awayML;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
    }
}