package edu.uci.ics.hcheng10.service.movies.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.hcheng10.service.movies.MoviesService;
import edu.uci.ics.hcheng10.service.movies.configs.IdmConfigs;
import edu.uci.ics.hcheng10.service.movies.logger.ServiceLogger;
import edu.uci.ics.hcheng10.service.movies.models.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.jackson.JacksonFeature;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class retrieveMovie {

    private static IdmConfigs idm = MoviesService.getIdmConfigs();
    private static String servicePath = idm.getScheme()+idm.getHostName()+":"+idm.getPort()+idm.getPath();

    private static String endpointPath = idm.getPrivilegePath();

    public static Boolean getHidden(String email, String session_id, String transaction_id) {

        plevelRequestModel requestModel = new plevelRequestModel(email, 4);
        responseModel responseM = null;
        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);

        ServiceLogger.LOGGER.info("Building WebTarget...");
        WebTarget webTarget = client.target(servicePath).path(endpointPath);

        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON)
                .header("email", email)
                .header("session_id", session_id)
                .header("transaction_id", transaction_id);

        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");

        ServiceLogger.LOGGER.info("Received status " + response.getStatus());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonText = response.readEntity(String.class);
            responseM = mapper.readValue(jsonText, responseModel.class);
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");
        } catch (IOException e) {
            ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
        }

        ServiceLogger.LOGGER.info("The resultCode is " + responseM.getResultCode());
        return responseM.getResultCode() == 140;

    }

    private static String getOrderForPeopleSearch(String order, String item, Integer offset, Integer limit) {
        String result;
        ServiceLogger.LOGGER.info("Putting order...");
        if (offset == 0) offset = 0;
        if (order == null) order = "ASC";
        if (item == null) item = "title";
        if (limit == 0) limit = 10;

        if (limit != 0 && offset % limit != 0) offset = 0;
        if (item.equals("title")) result = "ORDER BY min(m.title) " + order +", p.popularity DESC ";
        else if (item.equals("birthday")) result = "ORDER BY p.birthday " + order +", p.popularity DESC ";
        else if (item.equals("popularity")) result = "ORDER BY p.popularity " + order +", min(m.title) ASC ";
        else result = "";
        return result;
    }

    private static String getOrder(String order, String item, Integer offset, Integer limit) {
        String result;
        ServiceLogger.LOGGER.info("Putting order...");
        if (offset == 0) offset = 0;
        if (order == null) order = "ASC";
        if (item == null) item = "title";
        if (limit == 0 || (limit != 10 && limit != 25 && limit != 50 && limit != 100)) limit = 10;
        if (limit != 0 && offset % limit != 0) offset = 0;
        if (item.equals("title")) result = "ORDER BY m.title " + order +", m.rating DESC LIMIT ?  OFFSET " + offset;
        else if (item.equals("rating")) result = "ORDER BY m.rating " + order +", m.title ASC LIMIT ? OFFSET " + offset;
        else if (item.equals("year")) result = "ORDER BY m.year " + order +", m.rating DESC LIMIT ? OFFSET " + offset;
        else result = "";
        return result;
    }

    private static ResultSet getMoviesSetByPhrase(@Nullable String phrase, @Nullable Integer limit,
                                                @Nullable Integer offset, @Nullable String orderby, @Nullable String direction) {

        if (limit == null) limit = 10;
        if (offset == null) offset = 0;

        if (orderby == null) orderby = "title";
        else if (orderby.equals("rating")) orderby = "rating";
        else if (orderby.equals("year")) orderby = "year";
        else orderby = "title";

        if (direction == null) direction = "asc";
        else if (direction.equals("desc")) direction = "desc";
        else direction = "asc";

        try {
            String query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                    "from movie m INNER JOIN keyword_in_movie kim on m.movie_id = kim.movie_id INNER JOIN keyword k on kim.keyword_id = k.keyword_id INNER JOIN person p on m.director_id = p.person_id " +
                    "WHERE ";
            String [] phraseList = phrase.split(",");
            ServiceLogger.LOGGER.info("Get phrase: " + phrase);
            for (int index=0; index<phraseList.length; ++index) {
                if (index==0 && index==phraseList.length-1) query += "k.name = ?  ";
                else if (index==0) query += "k.name = ? AND m.movie_id IN ( ";
                else if (index==phraseList.length-1) query += "SELECT DISTINCT m.movie_id " +
                        "FROM movie m INNER JOIN keyword_in_movie kim on m.movie_id = kim.movie_id INNER JOIN keyword k on kim.keyword_id = k.keyword_id INNER JOIN person p on m.director_id = p.person_id " +
                        "WHERE k.name = ?) ";
                else query += "SELECT DISTINCT m.movie_id " +
                            "FROM movie m INNER JOIN keyword_in_movie kim on m.movie_id = kim.movie_id INNER JOIN keyword k on kim.keyword_id = k.keyword_id INNER JOIN person p on m.director_id = p.person_id " +
                            "WHERE k.name = ? AND m.movie_id IN ( ";
            }
            for (int i=0; i<phraseList.length-2; ++i) query += ')';
            //ServiceLogger.LOGGER.info("direction is: "+ direction + " -- " + orderby);
            query += getOrder(direction, orderby, offset, limit);
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            int i;
            for (i=1; i<=phraseList.length; ++i) {
                ps.setString(i, phraseList[i-1]);
            }
            ps.setInt(i, limit);
            ServiceLogger.LOGGER.info("Execute Query: "+ ps.toString());
            ResultSet rs = ps.executeQuery();
            return rs;

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve student records.");
            e.printStackTrace();
        }
        return null;

    }

    public static movieModel [] getMoviesByPhrase(@Nullable String phrase, @Nullable Integer limit,
                                                 @Nullable Integer offset, @Nullable String orderby, @Nullable String direction) {
        try {
            ResultSet rs = getMoviesSetByPhrase(phrase, limit, offset, orderby, direction);
            ResultSet rs1 = getMoviesSetByPhrase(phrase, limit, offset, orderby, direction);
            int numberOfRecords;
            for (numberOfRecords=0; rs1.next(); ++numberOfRecords);
            movieModel [] movies = new movieModel[numberOfRecords];
            ServiceLogger.LOGGER.info("There are " + numberOfRecords + " movies.");
            for (int i = 0; rs.next(); ++i) {
                String movie_id = rs.getString(1);
                String the_title = rs.getString(2);
                Integer the_year = rs.getInt(3);
                String the_director = rs.getString(4);
                Float rating = rs.getFloat(5);
                String backdrop_path = rs.getString(6);
                String poster_path = rs.getString(7);
                Boolean the_hidden = rs.getBoolean(8);
                movieModel newMovie = new movieModel(movie_id, the_title, the_year, the_director, rating, backdrop_path, poster_path, the_hidden);
                movies[i] = newMovie;
            }
            return movies;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve student records.");
            e.printStackTrace();
        }
        return null;


    }

    private static ResultSet getMoviesSet(@Nullable String title, @Nullable Integer year, @Nullable String director,
                                          @Nullable String genre, @Nullable Boolean hidden, @Nullable Integer limit,
                                          @Nullable Integer offset, @Nullable String orderby, @Nullable String direction) {

        String query;
        if (hidden == null) hidden = false;
        if (limit == 0) limit = 10;
        if (offset == 0) offset = 0;

        if (orderby == null) orderby = "title";
        else if (orderby.equals("rating")) orderby = "rating";
        else if (orderby.equals("year")) orderby = "year";
        else orderby = "title";

        if (direction == null) direction = "asc";
        else if (direction.equals("desc")) direction = "desc";
        else direction = "asc";
        ResultSet rs;
        try {
            if (title == null && year == 0 && director == null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + genre+ "%");
                ps.setBoolean(2, hidden);
                ps.setInt(3, limit);
                //ps.setInt(4, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year == 0 && director != null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%"+director+"%");
                ps.setBoolean(2, hidden);
                ps.setInt(3, limit);
                //ps.setInt(4, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year != 0 && director == null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setInt(1, year);
                ps.setBoolean(2, hidden);
                ps.setInt(3, limit);
                //ps.setInt(4, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year == 0 && director == null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + title + "%");
                ps.setBoolean(2, hidden);
                ps.setInt(3, limit);
                //ps.setInt(4, offset);
                ServiceLogger.LOGGER.info("Executing Query: " + ps.toString());
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year != 0 && director == null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + title + "%");
                ps.setInt(2, year);
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                //ps.setInt(5, offset);
                ServiceLogger.LOGGER.info("Executing Query: " + ps.toString());
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year == 0 && director != null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND p.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND p.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + title + "%");
                ps.setString(2, "%"+director+"%");
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                //ps.setInt(5, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year == 0 && director == null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND genre.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.title LIKE ? AND genre.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + title + "%");
                ps.setString(2, genre);
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                ServiceLogger.LOGGER.info("Genre is " + genre);
                ServiceLogger.LOGGER.info("Execute Query: " + ps.toString());
                //ps.setInt(5, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year != 0 && director != null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND p.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND p.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setInt(1, year);
                ps.setString(2, "%"+director+"%");
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                //ps.setInt(5, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year != 0 && director == null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND genre.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where m.year = ? AND genre.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setInt(1, year);
                ps.setString(2, "%" + genre+ "%");
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                //ps.setInt(5, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year == 0 && director != null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND genre.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND genre.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%"+director+"%");
                ps.setString(2, "%" + genre+ "%");
                ps.setBoolean(3, hidden);
                ps.setInt(4, limit);
                //ps.setInt(5, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year != 0 && director != null && genre == null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where p.name LIKE ? AND m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%"+director+"%");
                ps.setString(2, "%" + title + "%");
                ps.setInt(3, year);
                ps.setBoolean(4, hidden);
                ps.setInt(5, limit);
                //ps.setInt(6, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year != 0 && director == null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.title LIKE ? AND m.year = ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + genre+ "%");
                ps.setString(2, "%" + title + "%");
                ps.setInt(3, year);
                ps.setBoolean(4, hidden);
                ps.setInt(5, limit);
                //ps.setInt(6, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title != null && year == 0 && director != null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.title LIKE ? AND p.name LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND m.title LIKE ? AND p.name LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + genre+ "%");
                ps.setString(2, "%" + title + "%");
                ps.setString(3, "%"+director+"%");
                ps.setBoolean(4, hidden);
                ps.setInt(5, limit);
                //ps.setInt(6, offset);
                rs = ps.executeQuery();
                return rs;
            } else if (title == null && year != 0 && director != null && genre != null) {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND p.name LIKE ? AND m.year = ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND p.name LIKE ? AND m.year = ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + genre+ "%");
                ps.setString(2, "%"+director+"%");
                ps.setInt(3, year);
                ps.setBoolean(4, hidden);
                ps.setInt(5, limit);
                //ps.setInt(6, offset);
                rs = ps.executeQuery();
                return rs;
            } else {
                if (direction == "asc") {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND p.name LIKE ? AND m.year = ? AND m.title LIKE ? AND m.hidden <= ? ";
                } else {
                    query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden " +
                            "FROM genre INNER JOIN genre_in_movie gim on genre.genre_id = gim.genre_id INNER JOIN movie m ON gim.movie_id = m.movie_id INNER JOIN person p ON m.director_id = p.person_id " +
                            "where genre.name LIKE ? AND p.name LIKE ? AND m.year = ? AND m.title LIKE ? AND m.hidden <= ? ";
                }
                query += getOrder(direction, orderby, offset, limit);
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, "%" + genre+ "%");
                ps.setString(2, "%"+director+"%");
                ps.setInt(3, year);
                ps.setString(4, "%" + title + "%");
                ps.setBoolean(5, hidden);
                ps.setInt(6, limit);
                //ps.setInt(7, offset);
                rs = ps.executeQuery();
                return rs;
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve student records.");
            e.printStackTrace();
        }
        return null;
    }

    public static movieModel [] getMovies(@Nullable String title, @Nullable Integer year, @Nullable String director,
                                 @Nullable String genre, @Nullable Boolean hidden, @Nullable Integer limit,
                                 @Nullable Integer offset, @Nullable String orderby, @Nullable String direction) {
        try {
            ResultSet rs = getMoviesSet(title, year, director, genre, hidden, limit, offset, orderby, direction);
            ResultSet rs1 = getMoviesSet(title, year, director, genre, hidden, limit, offset, orderby, direction);
            int numberOfRecords;
            for (numberOfRecords=0; rs1.next(); ++numberOfRecords);
            movieModel [] movies = new movieModel[numberOfRecords];
            ServiceLogger.LOGGER.info("There are " + numberOfRecords + " movies.");
            Boolean the_hidden;
            for (int i = 0; rs.next(); ++i) {
                String movie_id = rs.getString(1);
                String the_title = rs.getString(2);
                Integer the_year = rs.getInt(3);
                String the_director = rs.getString(4);
                Float rating = rs.getFloat(5);
                String backdrop_path = rs.getString(6);
                String poster_path = rs.getString(7);
                if (!hidden) the_hidden = null;
                else the_hidden = rs.getBoolean(8);
                movieModel newMovie = new movieModel(movie_id, the_title, the_year, the_director, rating, backdrop_path, poster_path, the_hidden);
                movies[i] = newMovie;
            }
            return movies;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve student records.");
            e.printStackTrace();
        }
        return null;
    }

    public static movieModelWithBudget getMovieWithBudget(String movie_id, Boolean hidden) {
        try {
            int len;
            String query0 = "SELECT DISTINCT p.person_id, p.name\n" +
                    "FROM movie m INNER JOIN genre_in_movie gim on m.movie_id = gim.movie_id INNER JOIN genre g on gim.genre_id = g.genre_id INNER JOIN person_in_movie pim on m.movie_id = pim.movie_id INNER JOIN person p on pim.person_id = p.person_id\n" +
                    "WHERE m.movie_id = ? AND m.hidden <= 1";
            PreparedStatement ps0 = MoviesService.getCon().prepareStatement(query0);
            PreparedStatement ps01 = MoviesService.getCon().prepareStatement(query0);
            ps0.setString(1, movie_id);
            ps01.setString(1, movie_id);
            ResultSet rs0 = ps0.executeQuery();
            ResultSet rs01 = ps01.executeQuery();
            for (len = 0; rs0.next(); ++len);
            movieModelWithBudget.personIDMap[] people = new movieModelWithBudget.personIDMap[len];
            for (int i = 0; rs01.next(); ++i) {
                int id = rs01.getInt(1);
                String name = rs01.getString(2);
                people[i] = new movieModelWithBudget.personIDMap(id, name);
            }

            String query1 = "SELECT DISTINCT g.genre_id, g.name\n" +
                    "FROM movie m INNER JOIN genre_in_movie gim on m.movie_id = gim.movie_id INNER JOIN genre g on gim.genre_id = g.genre_id INNER JOIN person_in_movie pim on m.movie_id = pim.movie_id INNER JOIN person p on pim.person_id = p.person_id\n" +
                    "WHERE m.movie_id = ? AND m.hidden <= 1";
            PreparedStatement ps00 = MoviesService.getCon().prepareStatement(query1);
            PreparedStatement ps001 = MoviesService.getCon().prepareStatement(query1);
            ps00.setString(1, movie_id);
            ps001.setString(1, movie_id);
            ResultSet rs00 = ps00.executeQuery();
            ResultSet rs001 = ps001.executeQuery();
            for (len = 0; rs00.next(); ++len);
            movieModelWithBudget.genreIDMap[] genres = new movieModelWithBudget.genreIDMap[len];
            for (int i = 0; rs001.next(); ++i) {
                int id = rs001.getInt(1);
                String name = rs001.getString(2);
                genres[i] = new movieModelWithBudget.genreIDMap(id, name);
            }

            String query = "SELECT DISTINCT m.movie_id, m.title, m.year, p.name, m.rating, m.num_votes, CONVERT(m.budget, CHAR), CONVERT(m.revenue, CHAR), m.overview, m.backdrop_path, m.poster_path, m.hidden\n" +
                    "FROM movie m INNER JOIN person p on m.director_id = p.person_id\n" +
                    "WHERE m.movie_id = ? AND m.hidden <= 1";
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            ps.setString(1, movie_id);
            ResultSet rs = ps.executeQuery();

            movieModelWithBudget movies = null;
            Boolean the_hidden;
            if (rs.next()) {
                String the_movie_id = rs.getString(1);
                String the_title = rs.getString(2);
                Integer the_year = rs.getInt(3);
                String the_director = rs.getString(4);
                Float rating = rs.getFloat(5);
                Integer the_num_vote = rs.getInt(6);
                String budget = rs.getString(7);
                String revenue = rs.getString(8);
                String overview = rs.getString(9);
                String backdrop_path = rs.getString(10);
                String poster_path = rs.getString(11);
                if (!hidden) the_hidden = null;
                else the_hidden = rs.getBoolean(12);
                movies = new movieModelWithBudget(the_movie_id, the_title, the_year, the_director, rating, the_num_vote, budget, revenue, overview, backdrop_path, poster_path, the_hidden, genres, people);
            }
            return movies;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movie(s) records.");
            e.printStackTrace();
        }
        return null;
    }

    public static thumbnailList [] getThumbnails(String [] movie_ids) {
        thumbnailList [] result = new thumbnailList [movie_ids.length];
        try {

            String query = "SELECT DISTINCT m.movie_id, m.title, m.backdrop_path, m.poster_path\n" +
                    "FROM movie m\n" +
                    "WHERE m.movie_id = ?";
            for (int i=0; i< movie_ids.length; ++i) {
                PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
                ps.setString(1, movie_ids[i]);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String the_movie_id = rs.getString(1);
                    String the_title = rs.getString(2);
                    String backdrop_path = rs.getString(3);
                    String poster_path = rs.getString(4);
                    result [i] = new thumbnailList(the_movie_id, the_title, backdrop_path, poster_path);
                }
            }
            int realLen = 0;
            for (int i=0; i<result.length; ++i) if (result[i] != null) realLen++;
            thumbnailList [] resulta = new thumbnailList[realLen];
            int index = 0;
            for (int i=0; i<result.length; ++i) if (result[i] != null) resulta[index++] = result[i];

            return resulta;

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movies records.");
            e.printStackTrace();
        }
        return result;
    }

    public static movieModel [] getMovieByName(String name, String direction, String orderby, int limit, int offset, Boolean hidden) {
        try {
            if (limit == 0) limit = 10;
            String query = "WITH MTemp AS (\n" +
                    "    SELECT DISTINCT m.movie_id, m.director_id, m.title, m.year, pim.person_id, m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                    "    FROM movie m\n" +
                    "             INNER JOIN person_in_movie pim on m.movie_id = pim.movie_id\n" +
                    "    WHERE pim.person_id = (SELECT DISTINCT pim2.person_id FROM person INNER JOIN person_in_movie pim2 on person.person_id = pim2.person_id WHERE person.name = ?)\n" +
                    ")\n" +
                    "SELECT m.movie_id, m.title, m.year, p.name, m.rating, m.backdrop_path, m.poster_path, m.hidden\n" +
                    "FROM MTemp m INNER JOIN person p ON p.person_id = m.director_id\n";
            query += getOrder(direction, orderby, offset, limit);
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            PreparedStatement ps1 = MoviesService.getCon().prepareStatement(query);
            ps.setString(1, name);
            ps1.setString(1, name);
            ps.setInt(2, limit);
            ps1.setInt(2, limit);
            ServiceLogger.LOGGER.info("Doing query: " + ps1.toString());
            ResultSet rs = ps.executeQuery();
            ResultSet rs1 = ps1.executeQuery();
            int len;
            for (len=0; rs1.next(); ++len);
            movieModel [] result = new movieModel [len];
            Boolean the_hidden;
            for (int i=0; rs.next(); ++i) {
                String the_movie_id = rs.getString(1);
                String the_title = rs.getString(2);
                Integer the_year = rs.getInt(3);
                String the_name = rs.getString(4);
                Float the_rating = rs.getFloat(5);
                String backdrop_path = rs.getString(6);
                String poster_path = rs.getString(7);
                if (!hidden) the_hidden = null;
                else the_hidden = rs.getBoolean(8);
                result[i] = new movieModel(the_movie_id, the_title, the_year, the_name, the_rating, backdrop_path, poster_path, the_hidden);
            }
            return result;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movies records.");
            e.printStackTrace();
        }
        return null;
    }

    public static peopleResponseModel peopleSearch(@Nullable String name, @Nullable String birthday, @Nullable String movie_title,
                                                   int limit, int offset, @Nullable String orderby, @Nullable String direction) {
        if (limit == 0) limit = 10;
        int [] allowed_limit = {10,25,50,100};
        Boolean in_allowed = false;
        for (int i: allowed_limit){
            if (i == limit) {
                in_allowed = true;
                break;
            }
        }
        if (!in_allowed) limit = 10;
        try {
            peopleResponseModel responseModel;
            String query = "SELECT p.person_id, p.name, p.birthday, p.popularity, p.profile_path#, m.title\n" +
                    "    FROM person p INNER JOIN person_in_movie pim on p.person_id = pim.person_id INNER JOIN movie m on pim.movie_id = m.movie_id\n";

            if (name == null && birthday == null && movie_title == null && orderby == null && direction == null) query += "";
            else {
                query += "WHERE ";
                if (name != null) query += "p.name LIKE ? ";
                if (birthday != null && name !=null) query += " AND p.birthday = ? ";
                else if (birthday != null && name ==null) query += " p.birthday = ? ";
                if (movie_title != null && (name != null || birthday != null)) query += "AND m.title LIKE ? ";
                else if (movie_title != null && name == null && birthday == null) query += " m.title LIKE ? ";
            }
            query += "GROUP BY p.person_id ";
            query += getOrderForPeopleSearch(direction, orderby, offset, limit);
            if (offset == 0) offset = 0;
            if (limit == 0) limit = 10;
            if (limit != 0 && offset % limit != 0) offset = 0;
            query += "LIMIT ? " + "OFFSET " + offset;
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            PreparedStatement ps1 = MoviesService.getCon().prepareStatement(query);
            int index = 1;
            if (name != null) {
                ps.setString(index, "%"+name+"%");
                ps1.setString(index, "%"+name+"%");
                index++;
            }
            if (birthday != null) {
                ps.setString(index, birthday);
                ps1.setString(index, birthday);
                index++;
            }
            if (movie_title != null) {
                ps.setString(index, "%"+movie_title+"%");
                ps1.setString(index, "%"+movie_title+"%");
                index++;
            }
            ServiceLogger.LOGGER.info("The query before limit applied: " + ps.toString());
            ps.setInt(index, limit);
            ps1.setInt(index, limit);
            ServiceLogger.LOGGER.info("Doing Query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ResultSet rs1 = ps1.executeQuery();
            int len;
            for (len=0; rs1.next(); ++len);
            peopleResponseModel.peopleModel [] people = new peopleResponseModel.peopleModel[len];
            for (int i=0; rs.next(); ++i) {
                Integer pid = rs.getInt(1);
                String the_name = rs.getString(2);
                String birth = rs.getString(3);
                Float pop = rs.getFloat(4);
                String proPath = rs.getString(5);
                people[i] = new peopleResponseModel.peopleModel(pid, the_name, birth, pop, proPath);
            }
            if (people.length != 0) responseModel = new peopleResponseModel(212, " Found people with search parameters.", people);
            else responseModel = new peopleResponseModel(213, "No people found with search parameters.", null);
            return responseModel;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movies records.");
            e.printStackTrace();
        }
        return null;
    }

    public static personResponseModel getPersonByID(Integer person_id) {
        personResponseModel result = null;
        try {
            String query = "SELECT DISTINCT p.person_id, p.name, g.gender_name, p.birthday, p.biography, p.birthplace, p.popularity, p.profile_path\n" +
                    "FROM person p INNER JOIN gender g ON p.gender_id = g.gender_id\n" +
                    "WHERE p.person_id = ?";
            PreparedStatement ps = MoviesService.getCon().prepareStatement(query);
            PreparedStatement ps1 = MoviesService.getCon().prepareStatement(query);
            ps.setInt(1, person_id);
            ps1.setInt(1, person_id);
            ResultSet rs = ps.executeQuery();
            ResultSet rs1 = ps1.executeQuery();
            int len;
            for (len=0; rs1.next(); ++len);
            if (rs.next()) {
                Integer pid = rs.getInt(1);
                String pname = rs.getString(2);
                String pgname = rs.getString(3);
                String pbir = rs.getString(4);
                String pbio = rs.getString(5);
                String pbplace = rs.getString(6);
                Float ppop = rs.getFloat(7);
                String ppro = rs.getString(8);
                peopleResponseModel.peopleModelWithBio person = new peopleResponseModel.peopleModelWithBio(pid, pname, pgname, pbir, pbio, pbplace, ppop, ppro);
                result = new personResponseModel(212, "Found people with search parameters.", person);
            } else {
                result = new personResponseModel(213, "No people found with search parameters.", null);
            }
            return result;
        }catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve movies records.");
            e.printStackTrace();
        }
        return result;
    }
}
