package music.symphony.com.materialmusicv2.objects;

public class Playlist {

    private long id;
    private String name;

    public Playlist(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist() {
        this.id = -1;
        this.name = "";
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Playlist && this.getId() == ((Playlist) other).getId();
    }
}
