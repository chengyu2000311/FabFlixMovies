package edu.uci.ics.hcheng10.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class movieModelWithBudget extends movieModel {


    @JsonProperty(value = "num_votes", required = true)
    private Integer num_votes;

    @JsonProperty(value = "budget")
    private String budget;

    @JsonProperty(value = "revenue")
    private String revenue;

    @JsonProperty(value = "overview")
    private String overview;

    @JsonProperty(value = "genres", required = true)
    private genreIDMap[] genres;

    @JsonProperty(value = "people", required = true)
    private personIDMap[] people;

    @JsonCreator
    public movieModelWithBudget (@JsonProperty(value = "movie_id", required = true) String movie_id,
                                 @JsonProperty(value = "title", required = true) String title, @JsonProperty(value = "year", required = true) Integer year,
                                 @JsonProperty(value = "director", required = true) String director, @JsonProperty(value = "rating", required = true) Float rating,
                                 @JsonProperty(value = "num_votes", required = true) Integer num_votes, @JsonProperty(value = "budget") String budget,
                                 @JsonProperty(value = "revenue") String revenue, @JsonProperty(value = "overview") String overview,
                                 @JsonProperty(value = "backdrop_path") String backdrop_path, @JsonProperty(value = "poster_path") String poster_path,
                                 @JsonProperty(value = "hidden") Boolean hidden, @JsonProperty(value = "genres", required = true) genreIDMap[] genres, @JsonProperty(value = "people", required = true) personIDMap[] people) {
        super(movie_id, title, year, director, rating, backdrop_path, poster_path, hidden);
        this.num_votes = num_votes;
        this.budget = budget;
        this.revenue = revenue;
        this.overview = overview;
        this.genres = genres;
        this.people = people;
    }

    @JsonProperty("num_votes")
    public Integer getNum_votes() {return num_votes;}

    @JsonProperty("budget")
    private String getBudget() {return budget;}

    @JsonProperty("revenue")
    private String getRevenue() {return revenue;}

    @JsonProperty("overview")
    private String getOverview() {return overview;}

    @JsonProperty("genres")
    private genreIDMap[] getGenres() {return genres;}

    @JsonProperty("people")
    private personIDMap[] getPeople() {return people;}

    public static class genreIDMap {
        @JsonProperty(value = "genre_id", required = true)
        private Integer genre_id;

        @JsonProperty(value = "name", required = true)
        private String name;

        @JsonCreator
        public genreIDMap(@JsonProperty(value = "genre_id", required = true) Integer genre_id, @JsonProperty(value = "name", required = true) String name) {
            this.genre_id = genre_id;
            this.name = name;
        }

        @JsonProperty("genre_id")
        public Integer getGenre_id() {return genre_id;}

        @JsonProperty("name")
        public String getName() {return name;}
    }

    public static class personIDMap {
        @JsonProperty(value = "person_id", required = true)
        private Integer person_id;

        @JsonProperty(value = "name", required = true)
        private String name;

        @JsonCreator
        public personIDMap(@JsonProperty(value = "person_id", required = true) Integer person_id, @JsonProperty(value = "name", required = true) String name) {
            this.person_id = person_id;
            this.name = name;
        }

        @JsonProperty("person_id")
        public Integer getPerson_id() {return person_id;}

        @JsonProperty("name")
        public String getName() {return name;}
    }
}