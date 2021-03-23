package drawingbot.pfm.modules;


import drawingbot.api.IPixelData;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryCollection;

import java.util.List;

/**
 * Positional encoders generate geometric shapes in various positions to represent the image
 *
 * Designed following the principles outlined here
 * https://www.researchgate.net/publication/331407670_LinesLab_A_Flexible_Low-Cost_Approach_for_the_Generation_of_Physical_Monochrome_Art
 */
public abstract class PositionEncoder extends ModularEncoder {

    public int getIterations(){
        return 1;
    }

    public abstract void preProcess(IPixelData data);

    public abstract void doProcess(IPixelData data);

    public abstract List<Coordinate> getCoordinates();

    public abstract GeometryCollection getGeometries();
}