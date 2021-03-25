package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.print.attribute.IntegerSyntax;

// Example request model
public class searchRequestModel{

    @JsonProperty(value = "title")
    private String title;
    @JsonProperty(value = "year")
    private Integer year;
    @JsonProperty(value = "director")
    private String director;
    @JsonProperty(value = "genre")
    private String genre;
    @JsonProperty(value = "hidden")
    private Boolean hidden;
    @JsonProperty(value = "limit")
    private Integer limit;
    @JsonProperty(value = "offset")
    private Integer offset;
    @JsonProperty(value = "orderby")
    private String orderby;
    @JsonProperty(value = "direction")
    private String direction;

    @JsonCreator @Nullable
    public searchRequestModel(@Nullable @JsonProperty(value = "title") String title,
                              @Nullable @JsonProperty(value = "year") Integer year, @Nullable @JsonProperty(value = "director") String director,
                              @Nullable @JsonProperty(value = "genre") String genre, @Nullable @JsonProperty(value = "hidden") Boolean hidden,
                              @Nullable @JsonProperty(value = "limit") Integer limit, @Nullable @JsonProperty(value = "offset") Integer offset,
                              @Nullable @JsonProperty(value = "orderby") String orderby, @Nullable @JsonProperty(value = "direction") String direction) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.genre = genre;
        this.hidden = hidden;
        this.limit = limit;
        this.offset = offset;
        this.orderby = orderby;
        this.direction = direction;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonProperty("director")
    public String getDirector() {
        return director;
    }

    @JsonProperty("genre")
    public String getGenre() {
        return genre;
    }

    @JsonProperty("hidden")
    public Boolean getHidden() {
        return hidden;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("offset")
    public Integer getOffset() {
        return offset;
    }

    @JsonProperty("orderby")
    public String getOrderby() {
        return orderby;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

}