package drawingbot.image.filters;

import com.jhlabs.image.ShapeFilter;
import drawingbot.utils.Utils;

public enum EnumShapeFilter {

    LINEAR(ShapeFilter.LINEAR),
    CIRCLE_UP(ShapeFilter.CIRCLE_UP),
    CIRCLE_DOWN(ShapeFilter.CIRCLE_DOWN),
    SMOOTH(ShapeFilter.SMOOTH);

    private final int shapeType;

    EnumShapeFilter(int shapeType) {
        this.shapeType = shapeType;
    }

    public int getShapeType() {
        return shapeType;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
