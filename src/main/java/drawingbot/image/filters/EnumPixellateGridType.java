package drawingbot.image.filters;

import com.jhlabs.image.CrystallizeFilter;
import drawingbot.utils.Utils;

public enum EnumPixellateGridType {

    RANDOM(CrystallizeFilter.RANDOM),
    SQUARE(CrystallizeFilter.SQUARE),
    HEXAGONAL(CrystallizeFilter.HEXAGONAL),
    OCTAGONAL(CrystallizeFilter.OCTAGONAL),
    TRIANGULAR(CrystallizeFilter.TRIANGULAR);

    private final int gridType;

    EnumPixellateGridType(int gridType) {
        this.gridType = gridType;
    }

    public int getGridType() {
        return gridType;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
