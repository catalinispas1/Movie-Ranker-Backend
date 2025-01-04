package guru.springframework.learnspringauthorization.resourse.controller;

import guru.springframework.learnspringauthorization.model.*;
import guru.springframework.learnspringauthorization.resourse.model.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.model.MovieResponse;
import guru.springframework.learnspringauthorization.resourse.service.MovieClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

// ./gradlew bootRun
@RestController
public class MovieController {
    @Autowired
    private MovieClientImpl movieClient;
    @Autowired
    private MyUserRepository userRepository;
    @Autowired
    private MovieRatesRepository movieRatesRepository;
    @Autowired
    private FavoriteMoviesRepository favoriteMoviesRepository;
    @Autowired
    private MovieCommentsRepository movieCommentsRepository;

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
    public void addFavoriteMovie(@RequestParam(name = "id") Long movieId, @RequestParam String posterPath, @RequestParam String title) {
        MyUser user = getCurrentUser();

        Optional<FavoriteMovies> existingMovie  = favoriteMoviesRepository.findByMovieId(movieId);
        FavoriteMovies favoriteMovies;

        if (existingMovie.isPresent()) {
            favoriteMovies = existingMovie.get();
        } else {
            favoriteMovies = new FavoriteMovies();
            favoriteMovies.setMovieId(movieId);
            favoriteMovies.getUsers().add(user);
            favoriteMovies.setBackdropPath(posterPath);
            favoriteMovies.setTitle(title);
            favoriteMoviesRepository.save(favoriteMovies);
        }

        user.getFavoriteMovies().add(favoriteMovies);
        favoriteMovies.getUsers().add(user);
        userRepository.save(user);
    }

    @PostMapping("/remove/fav/movie")
    public void removeFavoriteMovie(@RequestParam(name = "id") Long movieId) {
        MyUser user = getCurrentUser();

        user.getFavoriteMovies().removeIf(movie -> movie.getMovieId().equals(movieId));
        userRepository.save(user);
    }

    @GetMapping("/is/movie/fav")
    public boolean isThisMovieFav(@RequestParam(name = "id") Long movieId) {
        MyUser user = getCurrentUser();
        return user.getFavoriteMovies().stream()
                .anyMatch(movie -> movie.getMovieId().equals(movieId) && movie.getUsers().contains(user));
    }

    @PostMapping("rate/movie")
    public void rateMovie(@RequestParam(name = "id") Long movieId, @RequestParam int rating) {
        MyUser user = getCurrentUser();

        Optional<MovieRates> getUserRating = movieRatesRepository.findByUserReviewerAndMovieId(user, movieId);
        if (getUserRating.isPresent()) {
            MovieRates updateRate = getUserRating.get();
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

    @GetMapping("get/movie/rating")
    public int getMovieRating(@RequestParam(name = "id") long movieId) {
        MyUser user = getCurrentUser();

        Optional<MovieRates> currentRating = movieRatesRepository.findByUserReviewerAndMovieId(user, movieId);
        return currentRating.map(MovieRates::getRating).orElse(0);
    }

    @GetMapping("user/fav/movies")
    public Page<FavoriteMovies> getFavoriteMovies(@RequestParam(required = false, defaultValue = "0") int page) {
        MyUser user = getCurrentUser();

        Pageable pageable = PageRequest.of(page, 20);
        return favoriteMoviesRepository.findByUsers(user, pageable);
    }

    @PostMapping("post/movie/comment")
    public void postMovieComment(@RequestParam(name = "id") Long movieId, @RequestParam String comment) {
        MyUser user = getCurrentUser();
        MovieComments movieComments = new MovieComments();

        movieComments.setComment(comment);
        movieComments.setCommentedMovieId(movieId);
        movieComments.setUserCommentator(user);
        movieComments.setCommentatorName(user.getUsername());
        movieCommentsRepository.save(movieComments);
    }

    @GetMapping("get/movie/comments")
    public Page<MovieComments> getMovieComments(@RequestParam(name = "id") Long id, @RequestParam(required = false, defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "timePosted"));
        return movieCommentsRepository.findByCommentedMovieId(id, pageable);
    }

    @GetMapping("get/avg/rating")
    public Double getAverageRating(@RequestParam(name = "id") Long movieId) {
        Double avgRating = movieRatesRepository.findAverageRatingByMovieId(movieId);
        if (avgRating == null) {
            return 0.0;
        }
        return avgRating;
    }

    private MyUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
