package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// Example response model
@JsonInclude(JsonInclude.Include.NON_NULL)
public class movieModel {

    @JsonProperty(value = "movie_id", required = true)
    private String movie_id;

    @JsonProperty(value = "title", required = true)
    private String title;

    @JsonProperty(value = "year", required = true)
    private Integer year;

    @JsonProperty(value = "director", required = true)
    private String director;

    @JsonProperty(value = "rating", required = true)
    private Float rating;

    @JsonProperty(value = "backdrop_path")
    private String backdrop_path;

    @JsonProperty(value = "poster_path")
    private String poster_path;

    @JsonProperty(value = "hidden")
    private Boolean hidden;

    @JsonCreator
    public movieModel (@JsonProperty(value = "movie_id", required = true) String movie_id,
                       @JsonProperty(value = "title", required = true) String title, @JsonProperty(value = "year", required = true) Integer year,
                       @JsonProperty(value = "director", required = true) String director, @JsonProperty(value = "rating", required = true) Float rating,
                       @JsonProperty(value = "backdrop_path") String backdrop_path, @JsonProperty(value = "poster_path") String poster_path,
                       @JsonProperty(value = "hidden") Boolean hidden) {
        this.movie_id = movie_id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.rating = rating;
        this.backdrop_path = backdrop_path;
        this.poster_path = poster_path;
        this.hidden = hidden;
    }

    @JsonProperty("movie_id")
    public String getMovie_id(){
        return movie_id;
    }

    @JsonProperty("title")
    public String getTitle(){
        return title;
    }

    @JsonProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonProperty("director")
    public String getDirector(){
        return director;
    }

    @JsonProperty("rating")
    public Float getRating(){
        return rating;
    }

    @JsonProperty("backdrop_path")
    public String getBackdrop_path() {
        return backdrop_path;
    }

    @JsonProperty("poster_path")
    public String getPoster_path() {
        return poster_path;
    }

    @JsonProperty("hidden")
    public Boolean getHidden() {
        return hidden;
    }
}