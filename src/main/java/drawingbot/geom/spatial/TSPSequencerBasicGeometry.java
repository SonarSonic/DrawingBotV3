package drawingbot.geom.spatial;

import drawingbot.geom.basic.IGeometry;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

@Deprecated
public class TSPSequencerBasicGeometry extends TSPSequencerBasic<IGeometry> {

    public TSPSequencerBasicGeometry(List<IGeometry> lineStrings, double allowableDistance) {
        super(lineStrings, allowableDistance);
    }

    @Override
    protected Coordinate getCoordinateFromCity(IGeometry geometry) {
        return geometry.getOriginCoordinate();
    }
}
