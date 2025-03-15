package com.jumper.jumperapi.service;

import com.jumper.jumperapi.model.KenPomGame;

import java.util.List;

public interface KenPomDataService {
    public List<KenPomGame> getKenPomGamesByDate(String dateStr);
    public List<String> getAvailableDates();
}
