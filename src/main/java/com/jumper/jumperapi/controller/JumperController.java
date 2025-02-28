package com.jumper.jumperapi.controller;

import com.jumper.jumperapi.model.GameResponse;
import com.jumper.jumperapi.model.response.GameScheduleResponse;
import com.jumper.jumperapi.service.JumperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController()
@CrossOrigin(origins = "*")
@RequestMapping("/jumper")
public class JumperController {

    private final JumperService jumperService;

    @Autowired
    public JumperController(JumperService jumperService) {
        this.jumperService = jumperService;
    }

    @GetMapping(value = "/schedule")
    public List<GameResponse> getScheduleByDate(@RequestParam(value = "date") String date){
        return jumperService.getScheduleByDate(date);
    }

}
