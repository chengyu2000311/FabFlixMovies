package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class thumbnailRequestModel {
    @JsonProperty(value = "movie_ids", required = true)
    private String [] movie_ids;

    @JsonCreator
    public thumbnailRequestModel (@JsonProperty(value = "movie_ids", required = true) String [] movie_ids) {
        this.movie_ids = movie_ids;
    }

    @JsonProperty("movie_ids")
    public String [] getMovie_ids() {return movie_ids;}
}
