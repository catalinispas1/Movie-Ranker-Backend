package guru.springframework.learnspringauthorization.resourse.movieModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BelongsToCollection {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;
}
