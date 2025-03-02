package guru.springframework.learnspringauthorization.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "my_user", indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true)
})
public class MyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(min = 2)
    private String username;

    @Size(min = 4)
    private String password;

    private String role; // daca sunt 2 punem asa: ADMIN,USER

    @ManyToMany
    @JoinTable(
            name = "user_favorite_movies",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    private Set<FavoriteMovies> favoriteMovies = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_favorite_genres",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<MovieGenres> favoriteGenres = new ArrayList<>();

    public List<MovieGenres> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<MovieGenres> favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }

    public Set<FavoriteMovies> getFavoriteMovies() {
        return favoriteMovies;
    }

    public void setFavoriteMovies(Set<FavoriteMovies> favoriteMovies) {
        this.favoriteMovies = favoriteMovies;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
