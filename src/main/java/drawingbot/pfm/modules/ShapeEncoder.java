package drawingbot.pfm.modules;

import drawingbot.api.IPixelData;

/**
 * Shape encoder can turn the output of Positional Encoders into geometries with rotation / size in relation to density of the image (typically measured by luminance)
 *
 * Designed following the principles outlined here
 * https://www.researchgate.net/publication/331407670_LinesLab_A_Flexible_Low-Cost_Approach_for_the_Generation_of_Physical_Monochrome_Art
 */
public abstract class ShapeEncoder extends ModularEncoder {

    public abstract void doProcess(IPixelData data, PositionEncoder positionEncoder);

}
