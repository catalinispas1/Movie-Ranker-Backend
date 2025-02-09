package guru.springframework.learnspringauthorization;

import guru.springframework.learnspringauthorization.model.*;
import guru.springframework.learnspringauthorization.repository.FavoriteMoviesRepository;
import guru.springframework.learnspringauthorization.repository.MovieCommentsRepository;
import guru.springframework.learnspringauthorization.repository.MovieRatesRepository;
import guru.springframework.learnspringauthorization.repository.MyUserRepository;
import guru.springframework.learnspringauthorization.resourse.movieModel.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.movieModel.MovieResponse;
import guru.springframework.learnspringauthorization.resourse.service.MovieClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class MovieClientImplTest {

    @InjectMocks
    private MovieClientImpl movieClient;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Environment environment;

    @Mock
    private MyUserRepository userRepository;

    @Mock
    private FavoriteMoviesRepository favoriteMoviesRepository;

    @Mock
    private MovieRatesRepository movieRatesRepository;

    @Mock
    private MovieCommentsRepository movieCommentsRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(environment.getProperty("movie.api.key")).thenReturn("test-api-key");
    }

    @Test
    void getMovies_ShouldReturnMovieResponse() {
        String url = "https://api.themoviedb.org/3/movie/popular?page=1";
        MovieResponse mockResponse = new MovieResponse();

        when(restTemplate.exchange(eq(url), any(), any(), eq(MovieResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        MovieResponse response = movieClient.getMovies(1);

        assertNotNull(response);
        verify(restTemplate, times(1))
                .exchange(eq(url), any(), any(), eq(MovieResponse.class));
    }

    @Test
    void getCurrentMovie_ShouldReturnCurrentMovie() {
        String url = "https://api.themoviedb.org/3/movie/1";
        CurrentMovie mockMovie = new CurrentMovie();

        when(restTemplate.exchange(eq(url), any(), any(), eq(CurrentMovie.class)))
                .thenReturn(ResponseEntity.ok(mockMovie));

        CurrentMovie response = movieClient.getCurrentMovie(1);

        assertNotNull(response);
        verify(restTemplate, times(1))
                .exchange(eq(url), any(), any(), eq(CurrentMovie.class));
    }

    @Test
    void getSearchedMovies_ShouldReturnMovieResponse() {
        String url = "https://api.themoviedb.org/3/search/movie?page=1&query=test";
        MovieResponse mockResponse = new MovieResponse();

        when(restTemplate.exchange(eq(url), any(), any(), eq(MovieResponse.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        MovieResponse response = movieClient.getSearchedMovies(1, "test");

        assertNotNull(response);
        verify(restTemplate, times(1))
                .exchange(eq(url), any(), any(), eq(MovieResponse.class));
    }

    @Test
    void addFavoriteMovie_ShouldSaveFavoriteMovie() {
        MyUser user = new MyUser();
        user.setUsername("testUser");
        Long movieId = 1L;

        when(favoriteMoviesRepository.findByMovieId(movieId)).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        movieClient.addFavoriteMovie(user, movieId, "posterPath", "title");

        verify(favoriteMoviesRepository, times(1)).save(any(FavoriteMovies.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void removeFavoriteMovie_ShouldRemoveMovieFromFavorites() {
        MyUser user = new MyUser();
        FavoriteMovies favoriteMovie = new FavoriteMovies();
        favoriteMovie.setMovieId(1L);
        user.getFavoriteMovies().add(favoriteMovie);

        movieClient.removeFavoriteMovie(user, 1L);

        assertTrue(user.getFavoriteMovies().isEmpty());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void isMovieFavorite_ShouldReturnTrueIfFavorite() {
        // Setup
        MyUser user = new MyUser();
        user.setFavoriteMovies(new HashSet<>()); // Inițializează lista de filme favorite

        FavoriteMovies favoriteMovie = new FavoriteMovies();
        favoriteMovie.setMovieId(1L);
        favoriteMovie.setUsers(new HashSet<>()); // Inițializează lista de utilizatori ca un HashSet
        favoriteMovie.getUsers().add(user);      // Adaugă utilizatorul în lista de utilizatori

        user.getFavoriteMovies().add(favoriteMovie); // Adaugă filmul în lista de filme favorite ale utilizatorului

        // Test
        boolean result = movieClient.isMovieFavorite(user, 1L);

        // Assertion
        assertTrue(result, "Expected the movie to be marked as favorite.");
    }




    @Test
    void rateMovie_ShouldAddNewRating() {
        MyUser user = new MyUser();
        user.setUsername("testUser");
        Long movieId = 1L;

        when(movieRatesRepository.findByUserReviewerAndMovieId(user, movieId))
                .thenReturn(Optional.empty());

        movieClient.rateMovie(user, movieId, 5);

        ArgumentCaptor<MovieRates> captor = ArgumentCaptor.forClass(MovieRates.class);
        verify(movieRatesRepository, times(1)).save(captor.capture());

        MovieRates savedRate = captor.getValue();
        assertEquals(5, savedRate.getRating());
        assertEquals(user, savedRate.getUserReviewer());
        assertEquals(movieId, savedRate.getMovieId());
    }

    @Test
    void getMovieRating_ShouldReturnExistingRating() {
        MyUser user = new MyUser();
        Long movieId = 1L;
        MovieRates movieRates = new MovieRates();
        movieRates.setRating(4);

        when(movieRatesRepository.findByUserReviewerAndMovieId(user, movieId))
                .thenReturn(Optional.of(movieRates));

        int rating = movieClient.getMovieRating(user, movieId);

        assertEquals(4, rating);
    }

    @Test
    void postMovieComment_ShouldSaveComment() {
        MyUser user = new MyUser();
        user.setUsername("testUser");
        Long movieId = 1L;
        String comment = "Great movie!";

        movieClient.postMovieComment(user, movieId, comment);

        ArgumentCaptor<MovieComments> captor = ArgumentCaptor.forClass(MovieComments.class);
        verify(movieCommentsRepository, times(1)).save(captor.capture());

        MovieComments savedComment = captor.getValue();
        assertEquals(comment, savedComment.getComment());
        assertEquals(movieId, savedComment.getCommentedMovieId());
        assertEquals(user, savedComment.getUserCommentator());
    }

    @Test
    void getMovieComments_ShouldReturnPagedComments() {
        Long movieId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<MovieComments> comments = new ArrayList<>();
        comments.add(new MovieComments());
        Page<MovieComments> commentPage = new PageImpl<>(comments);

        when(movieCommentsRepository.findByCommentedMovieId(movieId, pageable))
                .thenReturn(commentPage);

        Page<MovieComments> result = movieClient.getMovieComments(movieId, 0);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAverageRating_ShouldReturnCorrectAverage() {
        Long movieId = 1L;
        when(movieRatesRepository.findAverageRatingByMovieId(movieId))
                .thenReturn(4.5);

        Double averageRating = movieClient.getAverageRating(movieId);

        assertEquals(4.5, averageRating);
    }

    @Test
    void getCurrentUser_ShouldReturnUser() {
        String username = "testUser";
        MyUser user = new MyUser();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        MyUser result = movieClient.getCurrentUser(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }
}

