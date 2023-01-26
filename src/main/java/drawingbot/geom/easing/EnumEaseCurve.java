package drawingbot.geom.easing;

import drawingbot.utils.Utils;

public enum EnumEaseCurve {
    LINEAR,
    SINE,
    QUAD,
    CUBIC,
    QUART,
    QUINT,
    EXPO,
    CIRC,
    BACK,
    ELASTIC,
    BOUNCE;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
