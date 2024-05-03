package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("movies")
public class MovieResource {
    
    @Inject
    PgPool client;

    @GET
    public Multi<Movie> getAll() {
        return Movie.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> get(@PathParam("id") Long id) {
        return Movie.findById(client, id)
                .onItem()
                // if the movie is found, return it. Otherwise, return a 404
                .transform(movie -> movie != null ? Response.ok(movie) : Response.status(Response.Status.NOT_FOUND))
                .onItem()
                // builds the response
                .transform(Response.ResponseBuilder::build);
    }

    @PostConstruct
    void config() {
        initdb();
    }

      private void initdb() {
        client.query("DROP TABLE IF EXISTS movies").execute()
                .flatMap(m-> client.query("CREATE TABLE movies (id SERIAL PRIMARY KEY, " +
                        "title TEXT NOT NULL)").execute())
                .flatMap(m -> client.query("INSERT INTO movies (title) VALUES('The Lord of the Rings')").execute())
                .flatMap(m -> client.query("INSERT INTO movies (title) VALUES('Harry Potter')").execute())
                .await()
                .indefinitely();
    }
}
