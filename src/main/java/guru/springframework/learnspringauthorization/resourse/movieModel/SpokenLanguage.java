package guru.springframework.learnspringauthorization.resourse.movieModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SpokenLanguage {
    @JsonProperty("english_name")
    private String englishName;

    @JsonProperty("iso_639_1")
    private String iso6391;

    @JsonProperty("name")
    private String name;
}
