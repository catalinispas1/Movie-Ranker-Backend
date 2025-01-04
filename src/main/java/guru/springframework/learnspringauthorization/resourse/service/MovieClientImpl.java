package guru.springframework.learnspringauthorization.resourse.service;

import guru.springframework.learnspringauthorization.resourse.model.CurrentMovie;
import guru.springframework.learnspringauthorization.resourse.model.Movie;
import guru.springframework.learnspringauthorization.resourse.model.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
// run project: ./gradlew bootRun
@Service
public class MovieClientImpl implements MovieClient{
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
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
}
