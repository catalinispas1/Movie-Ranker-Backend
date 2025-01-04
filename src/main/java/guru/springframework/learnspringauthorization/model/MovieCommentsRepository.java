package guru.springframework.learnspringauthorization.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieCommentsRepository extends JpaRepository<MovieComments, Long> {
    Page<MovieComments> findByCommentedMovieId(Long movieId, Pageable pageable);
}
