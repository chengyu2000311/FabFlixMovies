package edu.uci.ics.hcheng10.service.movies.resources;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.hcheng10.service.movies.logger.ServiceLogger;
import edu.uci.ics.hcheng10.service.movies.models.*;
import org.apache.commons.codec.binary.Hex;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import edu.uci.ics.hcheng10.service.movies.core.retrieveMovie;

@Path ("/")
public class moviePage {
    @Path("search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Search(@Context HttpHeaders headers, @QueryParam("title") String title,
                             @QueryParam("year") int year, @QueryParam("director") String director,
                             @QueryParam("genre") String genre, @QueryParam("hidden") Boolean hidden,
                             @QueryParam("limit") int limit, @QueryParam("offset") int offset,
                             @QueryParam("orderby") String orderby, @QueryParam("direction") String direction) {
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");
        Boolean hidden1 = retrieveMovie.getHidden(email, session_id, transaction_id);
        if (hidden1 && hidden) hidden = true;
        else hidden = false;
        searchRequestModel requestModelForSearch = new searchRequestModel(title, year, director, genre, hidden, limit, offset, orderby, direction);
        searchResponseModel responseModel = null;
        ServiceLogger.LOGGER.info("breakpoint here");
        try {
            if (title == null && year == 0 && director == null && genre == null) {
                movieModel [] movies = new movieModel[0];
                responseModel = new searchResponseModel(211, "No movies found with search parameters.", movies);
                ServiceLogger.LOGGER.info("Search criteria is empty.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                        header("session_id", session_id).header("transaction_id", transaction_id).build();
            } else {
                movieModel [] movies = retrieveMovie.getMovies(title, year, director, genre, hidden, limit, offset,orderby, direction);
                ServiceLogger.LOGGER.info("Retrieve Movies Successful.");
                if (movies.length == 0) {
                    ServiceLogger.LOGGER.info("211 No movies found with search parameters.");
                    responseModel = new searchResponseModel(211, "No movies found with search parameters.", null);
                    return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                            header("session_id", session_id).header("transaction_id", transaction_id).build();
                }
                else responseModel = new searchResponseModel(210, "Found movie(s) with search parameters.", movies);
                ServiceLogger.LOGGER.info("210 Found movie(s) with search parameters.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                        header("session_id", session_id).header("transaction_id", transaction_id).build();
            }
        } catch(Exception e) {
            responseModel = new searchResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }

    @Path("browse/{phrase}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Browse(@Context HttpHeaders headers, @PathParam("phrase") String phrase,
                           @QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset,
                           @QueryParam("orderby") String orderby, @QueryParam("direction") String direction) {

        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");


        phraseRequestModel requestModelForSearch = new phraseRequestModel(phrase, limit, offset, orderby, direction);
        searchResponseModel responseModel = null;

        try {
                movieModel [] movies = retrieveMovie.getMoviesByPhrase(phrase, limit, offset, orderby, direction);
                ServiceLogger.LOGGER.info("Retrieve Movies Successful.");
                if (movies.length == 0) {
                    ServiceLogger.LOGGER.info("211 No movies found with search parameters.");
                    responseModel = new searchResponseModel(211, "No movies found with search parameters.", null);
                    return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                            header("session_id", session_id).header("transaction_id", transaction_id).build();
                }
                else responseModel = new searchResponseModel(210, "Found movie(s) with search parameters.", movies);
                ServiceLogger.LOGGER.info("210 Found movie(s) with search parameters.");
                return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                        header("session_id", session_id).header("transaction_id", transaction_id).build();

        } catch(Exception e) {
            responseModel = new searchResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error. --");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }
}