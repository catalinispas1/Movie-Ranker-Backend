package guru.springframework.learnspringauthorization.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;


@Entity
@Table(name = "movie_comments", indexes = {
        @Index(name = "idx_movieId", columnList = "commentedMovieId")
})
public class MovieComments {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_commentator", referencedColumnName = "id")
    private MyUser userCommentator;
    private Long commentedMovieId;
    @Column(nullable = false)
    private String comment;
    private String commentatorName;
    @CreationTimestamp
    private Timestamp timePosted;

    public String getCommentatorName() {
        return commentatorName;
    }

    public void setCommentatorName(String commentatorName) {
        this.commentatorName = commentatorName;
    }

    public Timestamp getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(Timestamp timePosted) {
        this.timePosted = timePosted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyUser getUserCommentator() {
        return userCommentator;
    }

    public void setUserCommentator(MyUser userCommentator) {
        this.userCommentator = userCommentator;
    }

    public Long getCommentedMovieId() {
        return commentedMovieId;
    }

    public void setCommentedMovieId(Long commentedMovieId) {
        this.commentedMovieId = commentedMovieId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
