package com.jumper.jumperapi.model.response.GameScheduleModels;

public class Parameters {
    private String league;
    private String date;
    private String season;

    // Getters and Setters

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
