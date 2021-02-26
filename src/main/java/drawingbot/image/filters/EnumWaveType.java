package drawingbot.image.filters;

import com.jhlabs.image.RippleFilter;
import drawingbot.utils.Utils;

public enum EnumWaveType {

    SINE(RippleFilter.SINE),
    SAWTOOTH(RippleFilter.SAWTOOTH),
    TRIANGLE(RippleFilter.TRIANGLE),
    NOISE(RippleFilter.NOISE);

    private final int waveType;

    EnumWaveType(int waveType) {
        this.waveType = waveType;
    }

    public int getWaveType() {
        return waveType;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
