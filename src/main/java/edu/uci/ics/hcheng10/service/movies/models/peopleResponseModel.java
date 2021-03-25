package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;

public class peopleResponseModel extends responseModel {
    @JsonProperty(value = "people", required = true)
    private peopleModel [] people;



    @JsonCreator
    public peopleResponseModel (@JsonProperty(value = "resultCode", required = true) int resultCode, @JsonProperty(value = "message",required = true) String message,
                                @JsonProperty(value = "people", required = true) peopleModel[] people) {
        super(resultCode, message);
        this.people = people;
    }


    @JsonProperty("people")
    public peopleModel [] getPeople() {return people;}

    public static class peopleModel {
        @JsonProperty(value = "person_id", required = true)
        private int person_id;

        @JsonProperty(value = "name", required = true)
        private String name;

        @JsonProperty(value = "birthday")
        private String birthday;

        @JsonProperty(value = "popularity")
        private Float  popularity;

        @JsonProperty(value = "profile_path")
        private String profile_path;

        @JsonCreator
        public peopleModel(@JsonProperty(value = "person_id", required = true) int person_id, @JsonProperty(value = "name", required = true) String name,
                           @JsonProperty(value = "birthday") String birthday, @JsonProperty(value = "popularity") Float popularity,
                           @JsonProperty(value = "profile_path") String profile_path) {
            this.person_id = person_id;
            this.name = name;
            this.birthday = birthday;
            this.popularity = popularity;
            this.profile_path = profile_path;
        }

        @JsonProperty("person_id")
        private int getPerson_id() {return person_id;}

        @JsonProperty("name")
        private String getName() {return name;}

        @JsonProperty("birthday")
        private String getBirthday() {return birthday;}

        @JsonProperty("popularity")
        private Float  getPopularity() {return popularity;}

        @JsonProperty("profile_path")
        private String getProfile_path() {return profile_path;}
    }

    public static class peopleModelWithBio extends peopleModel{
        @JsonProperty(value = "biography")
        private String biography;

        @JsonProperty(value = "birthplace")
        private String birthplace;

        @JsonProperty(value = "gender")
        private String gender;

        @JsonCreator
        public peopleModelWithBio (@JsonProperty(value = "person_id", required = true) int person_id, @JsonProperty(value = "name", required = true) String name,
                                   @JsonProperty(value ="gender") String gender, @JsonProperty(value = "birthday") String birthday, @JsonProperty(value = "biography") String biography,
                                   @JsonProperty(value = "birthplace") String birthplace, @JsonProperty(value = "popularity") Float popularity,
                                   @JsonProperty(value = "profile_path") String profile_path) {
            super(person_id,name,birthday,popularity,profile_path);
            this.gender = gender;
            this.birthplace = birthplace;
            this.biography = biography;
        }

        @JsonProperty("biography")
        public String getBiography() {return biography;}

        @JsonProperty("birthplace")
        public String getBirthplace() {return birthplace;}

        @JsonProperty("gender")
        public String getGender() {return gender;}
    }
}
