package drawingbot.mosaic.modules;

import drawingbot.utils.Utils;

public class CrissCrossMosaicModule extends AbstractMosaicModule {

    @Override
    public void doProcess() {
        double brightness = Utils.mapDouble(task.getPixelData().getAverageBrightness(),  0,255, 0, 1 );

    }

}
