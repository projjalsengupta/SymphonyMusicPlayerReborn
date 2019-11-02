package music.symphony.com.materialmusicv2.objects;

import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;

public class AlbumSong {
    private String trackNumber;
    private String name;
    private String duration;
    private String durationString;

    public AlbumSong(String trackNumber, String name, String duration) {
        this.trackNumber = trackNumber;
        this.name = name;
        this.duration = duration;
        this.durationString = covertMilisToTimeString(Integer.parseInt(duration));
        fixTrackNumber();
    }

    private void fixTrackNumber() {
        int n = Integer.parseInt(trackNumber);
        n %= 1000;
        trackNumber = String.valueOf(n);
        if (trackNumber.equals("0")) {
            trackNumber = "-";
        }
    }
}
