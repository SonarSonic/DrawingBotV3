package drawingbot.image.filters;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.RippleFilter;
import drawingbot.utils.Utils;

public enum EnumEdgeDetect {

    NONE(new float[9], new float[9]),
    ROBERTS(EdgeFilter.ROBERTS_H, EdgeFilter.ROBERTS_V),
    PREWITT(EdgeFilter.PREWITT_H, EdgeFilter.PREWITT_V),
    SOBEL(EdgeFilter.SOBEL_H, EdgeFilter.SOBEL_V),
    FREI_CHEN(EdgeFilter.FREI_CHEN_H, EdgeFilter.FREI_CHEN_V);

    private final float[] horizontal;
    private final float[] vertical;

    EnumEdgeDetect(float[] horizontal, float[] vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public float[] getHorizontalMatrix() {
        return horizontal;
    }

    public float[] getVerticalMatrix() {
        return vertical;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
