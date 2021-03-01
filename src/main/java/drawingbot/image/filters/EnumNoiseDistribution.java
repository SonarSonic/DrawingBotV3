package drawingbot.image.filters;

import com.jhlabs.image.NoiseFilter;
import drawingbot.utils.Utils;

public enum EnumNoiseDistribution {
    GAUSSIAN(NoiseFilter.GAUSSIAN),
    UNIFORM(NoiseFilter.UNIFORM);

    private final int distribution;

    EnumNoiseDistribution(int edgeAction) {
        this.distribution = edgeAction;
    }

    public int getDistribution() {
        return distribution;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
