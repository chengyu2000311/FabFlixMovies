package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class personResponseModel extends responseModel {
    @JsonProperty(value = "person", required = true)
    private peopleResponseModel.peopleModelWithBio person;

    @JsonCreator
    public personResponseModel(@JsonProperty(value = "resultCode", required = true) int resultCode, @JsonProperty(value = "message", required = true) String message,
                               @JsonProperty(value = "person", required = true) peopleResponseModel.peopleModelWithBio person) {
        super(resultCode, message);
        this.person = person;
    }

    @JsonProperty("person")
    private peopleResponseModel.peopleModelWithBio getPerson() {
        return person;
    }
}
