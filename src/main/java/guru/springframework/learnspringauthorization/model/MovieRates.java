package guru.springframework.learnspringauthorization.model;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_rates", indexes = {
        @Index(name = "idx_userReviewer_movieId", columnList = "user_reviewer, movieId", unique = true)
})
public class MovieRates {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_reviewer", referencedColumnName = "id")
    private MyUser userReviewer;
    private Long movieId;
    private int rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyUser getUserReviewer() {
        return userReviewer;
    }

    public void setUserReviewer(MyUser userReviewer) {
        this.userReviewer = userReviewer;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
