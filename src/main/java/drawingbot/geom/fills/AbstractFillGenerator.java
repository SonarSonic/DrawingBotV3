package drawingbot.geom.fills;

import drawingbot.api.IPlottingTools;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottingTools;
import drawingbot.utils.INamedSetting;

import java.util.List;

public abstract class AbstractFillGenerator implements INamedSetting {

    public abstract List<IGeometry> generateFilledShapes(PlottingTools tools, IGeometry geometry);

}
