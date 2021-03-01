package drawingbot.image.filters;

import com.jhlabs.image.TransformFilter;
import drawingbot.utils.Utils;

public enum EnumBumpSource {

    TRANSPARENT(TransformFilter.ZERO),
    CLAMP(TransformFilter.CLAMP),
    WRAP(TransformFilter.WRAP);

    private final int edgeAction;

    EnumBumpSource(int edgeAction) {
        this.edgeAction = edgeAction;
    }

    public int getEdgeAction() {
        return edgeAction;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
