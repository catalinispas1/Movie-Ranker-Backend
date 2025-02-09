package guru.springframework.learnspringauthorization.resourse.movieModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Genre {
    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;
}

