package guru.springframework.learnspringauthorization.resourse.controller;

import guru.springframework.learnspringauthorization.model.*;
import guru.springframework.learnspringauthorization.resourse.movieModel.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.movieModel.FavoriteGenreCount;
import guru.springframework.learnspringauthorization.resourse.movieModel.Genre;
import guru.springframework.learnspringauthorization.resourse.movieModel.MovieResponse;
import guru.springframework.learnspringauthorization.resourse.service.MovieClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ./gradlew bootRun
@RestController
public class MovieController {
    @Autowired
    private MovieClientImpl movieClient;

    @GetMapping("/popular")
    public MovieResponse getPopularMovies(@RequestParam(defaultValue = "1") int page) {
        return movieClient.getMovies(page);
    }

    @GetMapping("/movie/details")
    public CurrentMovie getMovieDetails(@RequestParam int id) {
        return movieClient.getCurrentMovie(id);
    }

    @GetMapping("/search/movie")
    public MovieResponse getSearchMovies(@RequestParam String query, @RequestParam(defaultValue = "1") int page) {
        return movieClient.getSearchedMovies(page, query);
    }

    @GetMapping("/filter/movie")
    public MovieResponse getFilterMovies(
            @RequestParam(defaultValue = "popularity.desc") String sort_by,
            @RequestParam(required = false) String primary_release_year,
            @RequestParam(required = false) String with_genres,
            @RequestParam(defaultValue = "false") boolean include_adult,
            @RequestParam(defaultValue = "1") int page
    ) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("&sort_by=").append(sort_by);

        if (primary_release_year != null && !primary_release_year.isEmpty()) {
            queryBuilder.append("&primary_release_year=").append(primary_release_year);
        }

        if (with_genres != null && !with_genres.isEmpty()) {
            queryBuilder.append("&with_genres=").append(with_genres);
        }

        queryBuilder.append("&include_adult=").append(include_adult);

        String query = queryBuilder.toString();
        return movieClient.getFilterMovies(page, query);
    }

    @PostMapping("/add/fav/movie")
    public void addFavoriteMovie(@RequestParam Long id, @RequestParam String posterPath, @RequestParam String title, @RequestBody List<Genre> genres) {
        movieClient.addFavoriteMovie(getCurrentUser(), id, posterPath, title, genres);
    }

    @PostMapping("/remove/fav/movie")
    public void removeFavoriteMovie(@RequestParam Long id, @RequestBody List<Genre> genres) {
        movieClient.removeFavoriteMovie(getCurrentUser(), id, genres);
    }

    @GetMapping("/is/movie/fav")
    public boolean isThisMovieFav(@RequestParam Long id) {
        return movieClient.isMovieFavorite(getCurrentUser(), id);
    }

    @PostMapping("/rate/movie")
    public void rateMovie(@RequestParam Long id, @RequestParam int rating) {
        movieClient.rateMovie(getCurrentUser(), id, rating);
    }

    @GetMapping("/get/movie/rating")
    public int getMovieRating(@RequestParam Long id) {
        return movieClient.getMovieRating(getCurrentUser(), id);
    }

    @GetMapping("/user/fav/movies")
    public Page<FavoriteMovies> getFavoriteMovies(@RequestParam(defaultValue = "0") int page) {
        return movieClient.getFavoriteMovies(getCurrentUser(), page);
    }

    @PostMapping("/post/movie/comment")
    public void postMovieComment(@RequestParam Long id, @RequestParam String comment) {
        movieClient.postMovieComment(getCurrentUser(), id, comment);
    }

    @GetMapping("/get/movie/comments")
    public Page<MovieComments> getMovieComments(@RequestParam Long id, @RequestParam(defaultValue = "0") int page) {
        return movieClient.getMovieComments(id, page);
    }

    @GetMapping("/get/avg/rating")
    public Double getAverageRating(@RequestParam Long id) {
        return movieClient.getAverageRating(id);
    }

    @GetMapping("/get/fav/count")
    public int getUserFavoriteMoviesCount() {
        return movieClient.getUserFavoriteMoviesCount(getCurrentUser());
    }

    @GetMapping("/get/average/user-movie-rates")
    public Double getUserRatingPattern() {
        return movieClient.getAverageRatingPattern(getCurrentUser());
    }

    @GetMapping("/get/user-comments-count")
    public int getUserTotalComments() {
        return movieClient.getTotalCommentsPosted(getCurrentUser());
    }

    @GetMapping("/get/user-genre-fav-count")
    public List<FavoriteGenreCount> getUserGenreFavoriteCount() {
        return movieClient.getFavoriteGenreCount(getCurrentUser());
    }

    private MyUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not found");
        }
        return movieClient.getCurrentUser(authentication.getName());
    }
}
