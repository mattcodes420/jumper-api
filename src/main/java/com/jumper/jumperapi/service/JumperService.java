package com.jumper.jumperapi.service;

import com.jumper.jumperapi.model.GameResponse;
import com.jumper.jumperapi.model.response.GameScheduleResponse;

import java.util.List;

public interface JumperService {

    List<GameResponse> getScheduleByDate(String date);

}
