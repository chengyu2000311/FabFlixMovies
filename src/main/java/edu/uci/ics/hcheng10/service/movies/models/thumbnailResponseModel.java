package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class thumbnailResponseModel extends responseModel {
    @JsonProperty(value = "thumbnails", required = true)
    private thumbnailList [] thumbnails;

    @JsonCreator
    public thumbnailResponseModel(@JsonProperty(value = "resultCode", required = true) int resultCode, @JsonProperty(value = "message",required = true) String message,
                                  @JsonProperty(value = "thumbnails", required = true) thumbnailList [] thumbnails) {
        super(resultCode, message);
        this.thumbnails = thumbnails;
    }

    @JsonProperty("thumbnails")
    public thumbnailList [] getThumbnails() {return thumbnails;}
}

