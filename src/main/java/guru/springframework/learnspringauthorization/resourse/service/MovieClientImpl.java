package guru.springframework.learnspringauthorization.resourse.service;

import guru.springframework.learnspringauthorization.model.*;
import guru.springframework.learnspringauthorization.repository.*;
import guru.springframework.learnspringauthorization.resourse.movieModel.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.movieModel.FavoriteGenreCount;
import guru.springframework.learnspringauthorization.resourse.movieModel.Genre;
import guru.springframework.learnspringauthorization.resourse.movieModel.MovieResponse;
import jakarta.transaction.Transactional;
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

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private MovieGenresRepository movieGenresRepository;
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

        return response.getBody();
    }

    @Override
    public MovieResponse getFilterMovies(int page, String query) {
        String requestUrl = url + "/discover/movie" + "?page=" + page + query;

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

    @Transactional
    @Override
    public void addFavoriteMovie(MyUser user, Long movieId, String posterPath, String title, List<Genre> genres) {
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

        for (Genre genre: genres) {
            Optional<MovieGenres> movieGenres = movieGenresRepository.findByGenreId(genre.getId());

            // they are all present in the DB, this is just for safety measures
            movieGenres.ifPresent(value -> user.getFavoriteGenres().add(value));
        }

        user.getFavoriteMovies().add(favoriteMovies);
        favoriteMovies.getUsers().add(user);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void removeFavoriteMovie(MyUser user, Long movieId, List<Genre> genres) {
        user.getFavoriteMovies().removeIf(movie -> movie.getMovieId().equals(movieId));
        for (Genre genre: genres) {
            movieGenresRepository.deleteFavoriteGenres(user.getId(), genre.getId());
        }
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

    @Override
    public int getUserFavoriteMoviesCount(MyUser user) {
        return user.getFavoriteMovies().size();
    }

    @Override
    public Double getAverageRatingPattern(MyUser user) {
        return movieRatesRepository.findAverageRatingByUserId(user.getId());
    }

    @Override
    public int getTotalCommentsPosted(MyUser user) {
        return movieCommentsRepository.getTotalCommentsPostedByUser(user.getId());
    }

    @Override
    public List<FavoriteGenreCount> getFavoriteGenreCount(MyUser user) {
        List<FavoriteGenreCount> favoriteGenreCountList = new ArrayList<>();
        List<Object[]> rawResults = movieGenresRepository.getFavoriteGenreCount(user.getId());

        for (Object[] row: rawResults) {
            int genreCount = ((Number) row[0]).intValue();
            int genreId = ((Number) row[1]).intValue();
            String genreName = (String) row[2];

            favoriteGenreCountList.add(new FavoriteGenreCount(genreId, genreName, genreCount));
        }

        return favoriteGenreCountList;
    }
}
