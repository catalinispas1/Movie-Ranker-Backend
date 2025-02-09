package guru.springframework.learnspringauthorization.resourse.movieModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductionCountry {
    @JsonProperty("iso_3166_1")
    private String iso31661;

    @JsonProperty("name")
    private String name;
}
