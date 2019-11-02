package music.symphony.com.materialmusicv2.objects;

public class Genre {

    private long id;
    private String name;

    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Genre && this.getId() == ((Genre) other).getId();
    }
}
