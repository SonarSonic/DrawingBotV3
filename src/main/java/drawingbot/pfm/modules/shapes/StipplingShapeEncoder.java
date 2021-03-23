package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import drawingbot.pfm.modules.position.WeightedVoronoiPositionEncoder;
import drawingbot.utils.Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public class StipplingShapeEncoder extends ShapeEncoder {

    public double stippleSize = 0.8D;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        pfmModular.task.updateMessage("Building Stipples");

        //point size
        int pixelsPerStipple = (data.getWidth() * data.getHeight()) / positionEncoder.getCoordinates().size();

        int minDotSize = 1;
        int maxDotSize = (int)(Math.sqrt(pixelsPerStipple)* stippleSize);

        Envelope envelope = new Envelope(0, data.getWidth()-1, 0, data.getHeight()-1);
        for (int i = 0; i < positionEncoder.getCoordinates().size(); i ++) {
            Coordinate coord = positionEncoder.getCoordinates().get(i);
            if(envelope.contains(coord)){
                int lum = ((WeightedVoronoiPositionEncoder)positionEncoder).luminance.get(i);
                int dotSize = Utils.mapInt(lum, 0, 255, minDotSize, maxDotSize);
                pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - dotSize/2F, (float)coord.y - dotSize/2F, dotSize, dotSize));
                pfmModular.updateShapeEncoderProgess(i, positionEncoder.getCoordinates().size()-1);
            }
        }
    }
}
