package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.print.attribute.IntegerSyntax;

// Example request model
public class phraseRequestModel {

    @JsonProperty(value = "phrase")
    private String phrase;
    @JsonProperty(value = "limit")
    private Integer limit;
    @JsonProperty(value = "offset")
    private Integer offset;
    @JsonProperty(value = "orderby")
    private String orderby;
    @JsonProperty(value = "direction")
    private String direction;

    @JsonCreator @Nullable
    public phraseRequestModel (@Nullable @JsonProperty(value = "phrase") String phrase,
                              @Nullable @JsonProperty(value = "limit") Integer limit, @Nullable @JsonProperty(value = "offset") Integer offset,
                              @Nullable @JsonProperty(value = "orderby") String orderby, @Nullable @JsonProperty(value = "direction") String direction) {
        this.phrase = phrase;
        this.limit = limit;
        this.offset = offset;
        this.orderby = orderby;
        this.direction = direction;
    }

    @JsonProperty("phrase")
    public String getPhrase() {return phrase;}


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