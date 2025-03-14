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
    private Integer excitementRank;
    private Integer comebackRank;
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
    private String dateTime; // You might need to extract this from other fields
    private String homeSpread;
    private String awaySpread;
    private String homeML;
    private String awayML;
    private boolean neutral;
}