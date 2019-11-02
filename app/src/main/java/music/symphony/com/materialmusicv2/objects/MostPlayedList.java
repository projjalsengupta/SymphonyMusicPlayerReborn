package music.symphony.com.materialmusicv2.objects;

import java.util.ArrayList;

public class MostPlayedList {
    public ArrayList<Song> songs;
    public ArrayList<Integer> playCountList;

    public MostPlayedList(ArrayList<Song> songs, ArrayList<Integer> playCountList) {
        this.songs = songs;
        this.playCountList = playCountList;
    }
}
