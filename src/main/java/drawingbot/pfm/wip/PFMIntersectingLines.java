package drawingbot.pfm.wip;

import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.image.ImageTools;
import drawingbot.pfm.AbstractDarkestPFM;

public class PFMIntersectingLines extends AbstractDarkestPFM {

    public int adjustbrightness;        // How fast it moves from dark to light, over-draw
    //public float desired_brightness;    // How long to process.  You can always stop early with "s" key

    public int tests;                   // Reasonable values:  13 for development, 720 for final

    protected int maxLines;

    protected float initialProgress;
    protected float progress;

    public PFMIntersectingLines() {
        super();
        tests = 360;
        adjustbrightness = 30;
        sampleHeight = 1;
        sampleWidth = 1;
        maxLines = 5000;
    }

    @Override
    public void init(IPlottingTask task) {
        super.init(task);
    }

    @Override
    public void doProcess(IPlottingTask task) {
        for(int i = 0; i < maxLines; i ++){
            findDarkestPixel(task.getPixelData());
            int startX = darkest_x;
            int startY = darkest_y;
            float darkestLineAvg = 0;
            int[] line = null;

            for (int d = 0; d < tests; d ++) {

                int[] testLine = getIntersectingLine(task.getPixelData(), startX, startY, randomSeed(0, 360));

                luminanceTestLine(task.getPixelData(), testLine[0], testLine[1], testLine[2], testLine[3]);

                float averageBrightness = (float) sum_luminance /(float) count_pixels;

                if(line == null || averageBrightness < darkestLineAvg){
                    darkestLineAvg = averageBrightness;
                    line = testLine;
                }
            }

            int rgba = ImageTools.getARGB(adjustbrightness, adjustbrightness, adjustbrightness, 50);
            task.addGeometry(new GLine(line[0], line[1], line[2], line[3]), null, rgba);

            adjustLuminanceLine(task, task.getPixelData(), line[0], line[1], line[2], line[3], adjustbrightness);
            progress = (float)i / maxLines;

            if(task.isFinished()){
                break;
            }
        }
        task.finishProcess();
    }

}
