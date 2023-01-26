package drawingbot.geom.easing;

import drawingbot.utils.Utils;

public enum EnumEaseTarget {
    IN,
    OUT,
    BOTH;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
