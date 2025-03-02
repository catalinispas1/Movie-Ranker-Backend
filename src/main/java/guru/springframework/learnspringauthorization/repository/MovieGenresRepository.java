package guru.springframework.learnspringauthorization.repository;

import guru.springframework.learnspringauthorization.model.MovieGenres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieGenresRepository extends JpaRepository<MovieGenres, Long> {
    Optional<MovieGenres> findByGenreId(int genreId);

    @Query(value = "SELECT COUNT(ufv.genre_id), ufv.genre_id AS 'ufv.genre_id', mg.genre_name  AS 'mg.genre_name' " +
            "FROM user_favorite_genres AS ufv " +
            "JOIN movie_genres AS mg ON mg.genre_id = ufv.genre_id " +
            "WHERE user_id = :userId " +
            "GROUP BY mg.genre_name, ufv.genre_id ", nativeQuery = true)
    List<Object[]> getFavoriteGenreCount(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_favorite_genres " +
            "WHERE user_id = :userId " +
            "AND genre_id = :genreId " +
            "LIMIT 1", nativeQuery = true)
    void deleteFavoriteGenres(@Param("userId") Long userId, @Param("genreId") int genreId);
}
