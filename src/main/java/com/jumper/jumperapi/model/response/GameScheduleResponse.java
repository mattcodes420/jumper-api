package com.jumper.jumperapi.model.response;
import com.jumper.jumperapi.model.response.GameScheduleModels.Game;
import com.jumper.jumperapi.model.response.GameScheduleModels.Parameters;

import java.util.List;

public class GameScheduleResponse {
    private String get;
    private Parameters parameters;
    private List<Object> errors;
    private int results;
    private List<Game> response;

    // Getters and Setters

    public String getGet() {
        return get;
    }

    public void setGet(String get) {
        this.get = get;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public List<Game> getResponse() {
        return response;
    }

    public void setResponse(List<Game> response) {
        this.response = response;
    }
}
