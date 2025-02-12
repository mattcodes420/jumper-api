package com.jumper.jumperapi.model.response.GameScheduleModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Status {

    @JsonProperty("long")
    private String longStatus;
    @JsonProperty("short")
    private String shortStatus;
    private String timer;

    // Getters and Setters

    public String getLongStatus() {
        return longStatus;
    }

    public void setLongStatus(String longStatus) {
        this.longStatus = longStatus;
    }

    public String getShortStatus() {
        return shortStatus;
    }

    public void setShortStatus(String shortStatus) {
        this.shortStatus = shortStatus;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }
}
