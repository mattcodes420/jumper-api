package com.jumper.jumperapi.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jumper.jumperapi.model.response.BookmakerModels.Bookmaker;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookmakerResponse {
    private List<Bookmaker> bookmakers;

    // Getter and Setter
    public List<Bookmaker> getBookmakers() {
        return bookmakers;
    }

    public void setBookmakers(List<Bookmaker> bookmakers) {
        this.bookmakers = bookmakers;
    }
}

