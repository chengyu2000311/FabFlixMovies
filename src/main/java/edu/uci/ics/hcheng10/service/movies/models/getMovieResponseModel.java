package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class getMovieResponseModel extends responseModel {

    @JsonProperty(value = "movie", required = true)
    private movieModelWithBudget movie;

    @JsonCreator
    public getMovieResponseModel (@JsonProperty(value = "resultCode", required = true) Integer resultCode,
                                @JsonProperty(value = "message", required = true) String message, @JsonProperty(value = "movie") movieModelWithBudget movie) {
        super(resultCode, message);
        this.movie = movie;
    }

    @JsonProperty("movie")
    public movieModelWithBudget getMovie() {return movie;}
}
