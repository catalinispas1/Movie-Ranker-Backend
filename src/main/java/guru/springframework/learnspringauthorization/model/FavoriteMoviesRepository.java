package guru.springframework.learnspringauthorization.model;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteMoviesRepository extends JpaRepository<FavoriteMovies, Long> {
    Optional<FavoriteMovies> findByMovieId(Long movieId);
    Page<FavoriteMovies> findByUsers(MyUser user, Pageable pageable);
}
