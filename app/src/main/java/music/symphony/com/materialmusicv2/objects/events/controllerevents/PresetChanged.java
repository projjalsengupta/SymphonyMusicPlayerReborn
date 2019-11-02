package music.symphony.com.materialmusicv2.objects.events.controllerevents;

public class PresetChanged {

    public short[] bandLevel;
    public short[] bandLevelRange;

    public PresetChanged(short[] bandLevel, short[] bandLevelRange) {
        this.bandLevel = bandLevel;
        this.bandLevelRange = bandLevelRange;
    }
}
