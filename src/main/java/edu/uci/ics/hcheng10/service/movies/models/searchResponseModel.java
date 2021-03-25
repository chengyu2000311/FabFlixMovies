package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// Example response model
@JsonInclude(JsonInclude.Include.NON_NULL)
public class searchResponseModel{

    @JsonProperty(value = "resultCode", required = true)
    private int resultCode;

    @JsonProperty(value = "message", required = true)
    private String message;

    @JsonProperty(value = "movies", required = true)
    private movieModel [] movies;

    @JsonCreator
    public searchResponseModel(@JsonProperty(value = "resultCode", required = true) int resultCode,
                                @JsonProperty(value = "message", required = true) String message,
                                @JsonProperty(value = "movies", required = true) movieModel [] movies) {
        this.resultCode = resultCode;
        this.message = message;
        this.movies = movies;
    }

    @JsonProperty("resultCode")
    public int getResultCode(){
        return resultCode;
    }

    @JsonProperty("message")
    public String getMessage(){
        return message;
    }

    @JsonProperty("movies")
    public movieModel [] getMovies() {
        return movies;
    }
}