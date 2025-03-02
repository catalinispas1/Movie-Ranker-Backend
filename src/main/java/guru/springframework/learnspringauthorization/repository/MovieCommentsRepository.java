package guru.springframework.learnspringauthorization.repository;

import guru.springframework.learnspringauthorization.model.MovieComments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieCommentsRepository extends JpaRepository<MovieComments, Long> {
    Page<MovieComments> findByCommentedMovieId(Long movieId, Pageable pageable);

    @Query("SELECT COUNT(userCommentator) FROM MovieComments c WHERE userCommentator.id = :userId")
    int getTotalCommentsPostedByUser(@Param("userId") Long userId);
}
