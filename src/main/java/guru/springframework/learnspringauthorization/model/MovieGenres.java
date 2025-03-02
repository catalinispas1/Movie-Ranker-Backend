package guru.springframework.learnspringauthorization.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_genres", indexes = {
        @Index(name = "idx_movie_id", columnList = "genre_id")
})
public class MovieGenres {

    @Id
    private int genreId;

    private String genreName;

    @ManyToMany(mappedBy = "favoriteGenres")
    private Set<MyUser> users = new HashSet<>();


    public Set<MyUser> getUsers() {
        return users;
    }

    public void setUsers(Set<MyUser> users) {
        this.users = users;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }
}
