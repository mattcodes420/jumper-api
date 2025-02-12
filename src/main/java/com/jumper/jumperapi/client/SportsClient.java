package com.jumper.jumperapi.client;

import com.jumper.jumperapi.model.response.BookmakerModels.Bookmaker;
import com.jumper.jumperapi.model.response.GameScheduleResponse;

import java.util.List;

public interface SportsClient {

    GameScheduleResponse getScheduleByDate(String date);

    List<Bookmaker> getOddsByGameID(String id);


}
