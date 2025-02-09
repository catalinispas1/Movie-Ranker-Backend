package guru.springframework.learnspringauthorization.repository;

import guru.springframework.learnspringauthorization.model.MovieRates;
import guru.springframework.learnspringauthorization.model.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieRatesRepository extends JpaRepository<MovieRates, Long> {
    Optional<MovieRates> findByUserReviewerAndMovieId(MyUser userReviewer, Long movieId);

    @Query("SELECT AVG(m.rating) FROM MovieRates m WHERE m.movieId = :movieId")
    Double findAverageRatingByMovieId(@Param("movieId") Long movieId);
}
