package guru.springframework.learnspringauthorization.resourse.service;

import guru.springframework.learnspringauthorization.resourse.model.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.model.Movie;
import guru.springframework.learnspringauthorization.resourse.model.MovieResponse;

public interface MovieClient {
    MovieResponse getMovies(int page);
    CurrentMovie getCurrentMovie(int id);
    MovieResponse getSearchedMovies(int page, String query);
    MovieResponse getFilterMovies(int page, String query);
}
