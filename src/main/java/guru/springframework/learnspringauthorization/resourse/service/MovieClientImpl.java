package guru.springframework.learnspringauthorization.resourse.service;

import guru.springframework.learnspringauthorization.model.*;
import guru.springframework.learnspringauthorization.repository.FavoriteMoviesRepository;
import guru.springframework.learnspringauthorization.repository.MovieCommentsRepository;
import guru.springframework.learnspringauthorization.repository.MovieRatesRepository;
import guru.springframework.learnspringauthorization.repository.MyUserRepository;
import guru.springframework.learnspringauthorization.resourse.movieModel.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.movieModel.MovieResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


// run project: ./gradlew bootRun
@Service
public class MovieClientImpl implements MovieClient{
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private MyUserRepository userRepository;
    @Autowired
    private FavoriteMoviesRepository favoriteMoviesRepository;
    @Autowired
    private MovieRatesRepository movieRatesRepository;
    @Autowired
    private MovieCommentsRepository movieCommentsRepository;
    private final String url = "https://api.themoviedb.org/3";
    private final String apiKey;

    @Autowired
    public MovieClientImpl(Environment environment) {
        this.apiKey = environment.getProperty("movie.api.key");
    }

    @Override
    public MovieResponse getMovies(int page) {
        String requestUrl = url + "/movie/popular" + "?page=" + page;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                MovieResponse.class
        );

        return response.getBody();
    }

    @Override
    public CurrentMovie getCurrentMovie(int id) {
        String requestUrl = url + "/movie/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<CurrentMovie> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                CurrentMovie.class
        );

        System.out.println("GETTING CURRENT MOVIE");

        return response.getBody();
    }

    @Override
    public MovieResponse getSearchedMovies(int page, String searchedMovie) {
        String requestUrl = url + "/search/movie" + "?page=" + page + "&query=" + searchedMovie;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                MovieResponse.class
        );
        System.out.println(response.getBody());

        return response.getBody();
    }

    @Override
    public MovieResponse getFilterMovies(int page, String query) {
        String requestUrl = url + "/discover/movie" + "?page=" + page + query;
        System.out.println(query);

        System.out.println("ASTA ESTE REQ URL: " + requestUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = restTemplateBuilder.build();

        ResponseEntity<MovieResponse> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                MovieResponse.class
        );

        return response.getBody();
    }

    @Override
    public void addFavoriteMovie(MyUser user, Long movieId, String posterPath, String title) {
        Optional<FavoriteMovies> existingMovie = favoriteMoviesRepository.findByMovieId(movieId);
        FavoriteMovies favoriteMovies;

        if (existingMovie.isPresent()) {
            favoriteMovies = existingMovie.get();
        } else {
            favoriteMovies = new FavoriteMovies();
            favoriteMovies.setMovieId(movieId);
            favoriteMovies.setBackdropPath(posterPath);
            favoriteMovies.setTitle(title);
            favoriteMoviesRepository.save(favoriteMovies);
        }

        user.getFavoriteMovies().add(favoriteMovies);
        favoriteMovies.getUsers().add(user);
        userRepository.save(user);
    }
    @Override
    public void removeFavoriteMovie(MyUser user, Long movieId) {
        user.getFavoriteMovies().removeIf(movie -> movie.getMovieId().equals(movieId));
        userRepository.save(user);
    }

    @Override
    public boolean isMovieFavorite(MyUser user, Long movieId) {
        return user.getFavoriteMovies().stream()
                .anyMatch(movie -> movie.getMovieId().equals(movieId) && movie.getUsers().contains(user));
    }

    @Override
    public void rateMovie(MyUser user, Long movieId, int rating) {
        Optional<MovieRates> existingRating = movieRatesRepository.findByUserReviewerAndMovieId(user, movieId);
        if (existingRating.isPresent()) {
            MovieRates updateRate = existingRating.get();
            updateRate.setRating(rating);
            movieRatesRepository.save(updateRate);
        } else {
            MovieRates movieRates = new MovieRates();
            movieRates.setRating(rating);
            movieRates.setUserReviewer(user);
            movieRates.setMovieId(movieId);
            movieRatesRepository.save(movieRates);
        }
    }

    @Override
    public int getMovieRating(MyUser user, Long movieId) {
        return movieRatesRepository.findByUserReviewerAndMovieId(user, movieId)
                .map(MovieRates::getRating).orElse(0);
    }

    @Override
    public Page<FavoriteMovies> getFavoriteMovies(MyUser user, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        return favoriteMoviesRepository.findByUsers(user, pageable);
    }

    @Override
    public void postMovieComment(MyUser user, Long movieId, String comment) {
        MovieComments movieComments = new MovieComments();
        movieComments.setComment(comment);
        movieComments.setCommentedMovieId(movieId);
        movieComments.setUserCommentator(user);
        movieComments.setCommentatorName(user.getUsername());
        movieCommentsRepository.save(movieComments);
    }

    @Override
    public Page<MovieComments> getMovieComments(Long movieId, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return movieCommentsRepository.findByCommentedMovieId(movieId, pageable);
    }

    @Override
    public Double getAverageRating(Long movieId) {
        return Optional.ofNullable(movieRatesRepository.findAverageRatingByMovieId(movieId)).orElse(0.0);
    }

    @Override
    public MyUser getCurrentUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
