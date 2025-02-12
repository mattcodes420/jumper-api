package com.jumper.jumperapi.client;

import com.jumper.jumperapi.model.response.GameScheduleResponse;

public interface SportsClient {

    GameScheduleResponse getScheduleByDate(String date);


}
