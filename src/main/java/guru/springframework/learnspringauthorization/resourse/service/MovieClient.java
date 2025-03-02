package guru.springframework.learnspringauthorization.resourse.service;

import guru.springframework.learnspringauthorization.model.FavoriteMovies;
import guru.springframework.learnspringauthorization.model.MovieComments;
import guru.springframework.learnspringauthorization.model.MyUser;
import guru.springframework.learnspringauthorization.resourse.movieModel.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.movieModel.FavoriteGenreCount;
import guru.springframework.learnspringauthorization.resourse.movieModel.Genre;
import guru.springframework.learnspringauthorization.resourse.movieModel.MovieResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieClient {
    MovieResponse getMovies(int page);
    CurrentMovie getCurrentMovie(int id);
    MovieResponse getSearchedMovies(int page, String query);
    MovieResponse getFilterMovies(int page, String query);
    void addFavoriteMovie(MyUser user, Long movieId, String posterPath, String title, List<Genre> genres);
    void removeFavoriteMovie(MyUser user, Long movieId, List<Genre> genres);
    boolean isMovieFavorite(MyUser user, Long movieId);
    void rateMovie(MyUser user, Long movieId, int rating);
    int getMovieRating(MyUser user, Long movieId);
    Page<FavoriteMovies> getFavoriteMovies(MyUser user, int page);
    void postMovieComment(MyUser user, Long movieId, String comment);
    Page<MovieComments> getMovieComments(Long movieId, int page);
    Double getAverageRating(Long movieId);
    MyUser getCurrentUser(String username);
    int getUserFavoriteMoviesCount(MyUser user);
    Double getAverageRatingPattern(MyUser user);
    int getTotalCommentsPosted(MyUser user);
    List<FavoriteGenreCount> getFavoriteGenreCount(MyUser user);
}
