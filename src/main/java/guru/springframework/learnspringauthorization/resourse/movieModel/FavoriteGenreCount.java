package guru.springframework.learnspringauthorization.resourse.movieModel;

public class FavoriteGenreCount {
    private int genre_id;

    private String genreName;

    private int favorite_count;

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public int getGenre_id() {
        return genre_id;
    }

    public void setGenre_id(int genre_id) {
        this.genre_id = genre_id;
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
    }

    public FavoriteGenreCount(int genre_id, String genreName, int favorite_count) {
        this.genre_id = genre_id;
        this.genreName = genreName;
        this.favorite_count = favorite_count;
    }
}
