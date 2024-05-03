package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

public class Movie {
    
    public static Multi<Movie> findAll(PgPool client) {
        return client
                // query to get all movies ordered by title
                .query("SELECT id, title FROM movies ORDER BY title DESC")
                .execute()
                // transforms the set of rows into a Multi
                .onItem()
                .transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem()
                // transforms each row into a Movie instance
                .transform(Movie::from);
    }

    public static Uni<Movie>findById(PgPool client, Long id) {
        return client
                // prepared query to avoid SQL injection
                .preparedQuery("SELECT id, title FROM movies WHERE id = $1")
                .execute(Tuple.of(id))
                // executes on the first item emitted by the upstream
                .onItem()
                // transforms the set of rows into a Uni
                .transform(m -> m.iterator().hasNext() ? from(m.iterator().next()) : null);
    }
    private static Movie from(Row row) {
        return new Movie(row.getLong("id"), row.getString("title"));
    }
    
    private Long id;

    private String title;

    public Movie(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId() {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle() {
        this.title = title;
    }
}
