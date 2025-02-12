package com.jumper.jumperapi.model;

import com.jumper.jumperapi.model.response.GameScheduleModels.Game;

public class GameResponse {
    private Odds odds;
    private Game game;

    public Odds getOdds() {
        return odds;
    }

    public void setOdds(Odds odds) {
        this.odds = odds;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
