package com.jumper.jumperapi.model;

public class Odds {
    private String moneylineHome;
    private String spreadHome;
    private String moneylineAway;
    private String spreadAway;

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
}
