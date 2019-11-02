package music.symphony.com.materialmusicv2.objects.events.controllerevents;

public class EqualizerBassboostVirtualizerPresetReverb {

    public EqualizerBassboostVirtualizerPresetReverb(boolean enabled, int numberOfBands, short[] bandLevelRange, int[] centerFrequency, short[] bandLevel, String[] presets, int currentPreset, int bassBoostLevel, int virtualizerLevel, String[] reverbs, int currentReverb) {
        this.enabled = enabled;
        this.numberOfBands = numberOfBands;
        this.bandLevelRange = bandLevelRange;
        this.centerFrequency = centerFrequency;
        this.bandLevel = bandLevel;
        this.presets = presets;
        this.currentPreset = currentPreset;
        this.bassBoostLevel = bassBoostLevel;
        this.virtualizerLevel = virtualizerLevel;
        this.reverbs = reverbs;
        this.currentReverb = currentReverb;
    }

    public boolean enabled;

    public int numberOfBands;
    public short[] bandLevelRange;
    public int[] centerFrequency;
    public short[] bandLevel;

    public String[] presets;
    public int currentPreset;

    public int bassBoostLevel;

    public int virtualizerLevel;

    public String[] reverbs;
    public int currentReverb;
}
