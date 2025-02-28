package com.jumper.jumperapi.model;

public class Odds {
    private String moneylineHome;
    private String spreadHome;
    private String spreadHomeOdds;
    private String moneylineAway;
    private String spreadAway;
    private String spreadAwayOdds;

    public String getMoneylineHome() {
        return moneylineHome;
    }

    public void setMoneylineHome(String moneylineHome) {
        this.moneylineHome = moneylineHome;
    }

    public String getSpreadHome() {
        return spreadHome;
    }

    public void setSpreadHome(String spreadHome) {
        this.spreadHome = spreadHome;
    }

    public String getMoneylineAway() {
        return moneylineAway;
    }

    public void setMoneylineAway(String moneylineAway) {
        this.moneylineAway = moneylineAway;
    }

    public String getSpreadAway() {
        return spreadAway;
    }

    public void setSpreadAway(String spreadAway) {
        this.spreadAway = spreadAway;
    }

    public String getSpreadHomeOdds() {
        return spreadHomeOdds;
    }

    public void setSpreadHomeOdds(String spreadHomeOdds) {
        this.spreadHomeOdds = spreadHomeOdds;
    }

    public String getSpreadAwayOdds() {
        return spreadAwayOdds;
    }

    public void setSpreadAwayOdds(String spreadAwayOdds) {
        this.spreadAwayOdds = spreadAwayOdds;
    }
}
