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

@Path("/")
public class moviePage2 {
    @Path("get/{movie_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpHeaders headers, @PathParam("movie_id") String movie_id) {
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");
        Boolean hidden = retrieveMovie.getHidden(email, session_id, transaction_id);
        getMovieResponseModel responseModel = null;
        try {
            movieModelWithBudget movies = retrieveMovie.getMovieWithBudget(movie_id, hidden);
            if (movies == null) {
                responseModel = new getMovieResponseModel(211, "No movies found with search parameters.", movies);
                ServiceLogger.LOGGER.info("No movies found with search parameters.");
            }

            else {
                responseModel = new getMovieResponseModel(210, "Found movies with search parameters.", movies);
                ServiceLogger.LOGGER.info("Found movie(s) with search parameters.");
            }

            return Response.status(Response.Status.OK).entity(responseModel).header("email", email).
                    header("session_id", session_id).header("transaction_id", transaction_id).build();
        } catch(Exception e) {
            responseModel = new getMovieResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }

    }

    @Path("thumbnail")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response thumbnail(@Context HttpHeaders headers, String jsonText) {
        thumbnailRequestModel requestModel;
        thumbnailResponseModel responseModel;
        ObjectMapper mapper = new ObjectMapper();
        try {
            requestModel = mapper.readValue(jsonText, thumbnailRequestModel.class);
        } catch (IOException e) {
            int resultCode;
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                resultCode = -3;
                responseModel = new thumbnailResponseModel(resultCode, "JSON Parse Exception",null);
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (e instanceof JsonMappingException) {
                resultCode = -2;
                responseModel = new thumbnailResponseModel(resultCode, "JSON Mapping Exception",null);
                ServiceLogger.LOGGER.warning("Unable to map JSON to POJO");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else {
                resultCode = -1;
                responseModel = new thumbnailResponseModel(resultCode, "Internal Server Error",null);
                ServiceLogger.LOGGER.severe("Internal Server Error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
            }
        }

        try {
            String [] movie_ids = requestModel.getMovie_ids();
            thumbnailList []  thumbnails = retrieveMovie.getThumbnails(movie_ids);
            if (thumbnails.length == 0) {
                responseModel = new thumbnailResponseModel(211, "No movies found with search parameters.", thumbnails);
                ServiceLogger.LOGGER.info("No movies found with search parameters for thumbnail.");
            } else {
                responseModel = new thumbnailResponseModel(210, "Found movies with search parameters.", thumbnails);
                ServiceLogger.LOGGER.info("Found movies with search parameters for thumbnail.");
            }
            return Response.status(Response.Status.OK).entity(responseModel).build();
        } catch(Exception e) {
            responseModel = null;
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }

    @Path("people")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response people(@Context HttpHeaders headers, @QueryParam("name") String name,
                           @QueryParam("limit") int limit, @QueryParam("offset") int offset,
                           @QueryParam("orderby") String orderby, @QueryParam("direction") String direction) {
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");
        Boolean hidden = retrieveMovie.getHidden(email, session_id, transaction_id);

        searchResponseModel responseModel;

        try {
            ServiceLogger.LOGGER.info("Trying to get the movies.");
            movieModel [] movies = retrieveMovie.getMovieByName(name, direction, orderby, limit, offset, hidden);
            ServiceLogger.LOGGER.info("Sucessful get the movie lists.");
            if (movies.length == 0) {
                responseModel = new searchResponseModel(211, " No movies found with search parameters.", null);
                ServiceLogger.LOGGER.info("There is no movies found for people");
            } else {
                responseModel = new searchResponseModel(210, " Found movie(s) with search parameters.", movies);
                ServiceLogger.LOGGER.info("There are " + movies.length +" movies found for people");
            }
            return Response.status(Response.Status.OK).header("email", email).header("session_id", session_id).header("transaction_id", transaction_id).entity(responseModel).build();
        } catch(Exception e) {
            responseModel = new searchResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }

    }

    @Path("people/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response peopleSearch(@Context HttpHeaders headers, @QueryParam("name") String name,
                                 @QueryParam("birthday") String birthday, @QueryParam("movie_title") String movie_title,
                                 @QueryParam("limit") int limit, @QueryParam("offset") int offset,
                                 @QueryParam("orderby") String orderby, @QueryParam("direction") String direction) {
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");

        peopleResponseModel responseModel;
        try {
            responseModel = retrieveMovie.peopleSearch(name, birthday, movie_title, limit, offset, orderby, direction);
            return Response.status(Response.Status.OK).entity(responseModel).header("email", email)
                    .header("session_id", session_id).header("transaction_id", transaction_id).build();
        } catch(Exception e) {
            responseModel = new peopleResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }

    @Path("people/get/{person_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response peopleGet(@Context HttpHeaders headers, @PathParam("person_id") Integer person_id) {
        String email = headers.getHeaderString("email");
        String session_id = headers.getHeaderString("session_id");
        String transaction_id = headers.getHeaderString("transaction_id");
        personResponseModel responseModel = null;
        try {
            responseModel = retrieveMovie.getPersonByID(person_id);
            ServiceLogger.LOGGER.info("Retrive person with id: " + person_id + ", " + responseModel.getMessage());
            return Response.status(Response.Status.OK).header("email", email)
                    .header("session_id", session_id).header("transaction_id", transaction_id).entity(responseModel).build();
        } catch(Exception e) {
            responseModel = new personResponseModel(-1, "500 Internal Server Error.", null);
            ServiceLogger.LOGGER.info("500 Internal Server Error.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseModel).build();
        }
    }

}
