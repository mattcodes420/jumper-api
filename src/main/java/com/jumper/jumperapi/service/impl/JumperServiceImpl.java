package com.jumper.jumperapi.service.impl;

import com.jumper.jumperapi.client.SportsClient;
import com.jumper.jumperapi.model.response.GameScheduleResponse;
import com.jumper.jumperapi.service.JumperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JumperServiceImpl implements JumperService {

    private final SportsClient sportsClient;

    @Autowired
    public JumperServiceImpl(SportsClient sportsClient) {this.sportsClient = sportsClient;}

    @Override
    public GameScheduleResponse getScheduleByDate(String date){
        GameScheduleResponse response = sportsClient.getScheduleByDate(date);
        //response + odds;
        return response;
    }
    //test

}
