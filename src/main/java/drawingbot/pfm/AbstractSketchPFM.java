package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.geom.shapes.GLine;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottingTools;

import java.util.function.BiConsumer;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    //user settings
    public int squiggleLength;
    public float squiggleDeviation;

    public int adjustbrightness;
    public float lineDensity;

    public int lineTests;
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;

    //process specific
    public double initialLuminance;

    public final float desiredLuminance = 253.5F;

    //latest progress
    public double lineProgress = 0;
    public double lumProgress = 0;
    public double actualProgress = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void setup() {
        super.setup();
        if(maxLineLength < minLineLength){
            int value = minLineLength;
            minLineLength = maxLineLength;
            maxLineLength = value;
        }
        initialLuminance = this.tools.getPixelData().getAverageLuminance();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public BiConsumer<IPixelData, int[]> findDarkestMethod = AbstractDarkestPFM::findDarkestArea;
    public int[] current = new int[]{-1, -1};
    public int[] darkest = new int[]{-1, -1};

    @Override
    public void run() {
        while(!tools.isFinished()){
            findDarkestMethod.accept(tools.getPixelData(), darkest);

            if(!shouldLiftPen && current[0] != -1){
                addGeometry(tools.getPixelData(), current[0], current[1], darkest[0], darkest[1], adjustbrightness);
            }

            float initialDarkness = tools.getPixelData().getLuminance(darkest[0], darkest[1]);
            float allowableDarkness = initialDarkness + Math.max(1, 255*squiggleDeviation);

            current[0] = darkest[0];
            current[1] = darkest[1];

            beginSquiggle();

            for (int s = 0; s < squiggleLength; s++) {

                float next = findDarkestNeighbour(tools.getPixelData(), current, darkest);

                //if the first line has been drawn and the next neighbour isn't dark enough to add to the squiggle, end it prematurely
                if(s > 3 && (next == -1 || next > allowableDarkness || next > tools.getPixelData().getAverageLuminance())){
                    endSquiggle();

                    //there are no valid lines from this point, brighten it slightly so we don't test it again immediately.
                    tools.getPixelData().adjustLuminance(current[0], current[1], 1);
                    break;
                }

                addGeometry(tools.getPixelData(), current[0], current[1], darkest[0], darkest[1], adjustbrightness);

                current[0] = darkest[0];
                current[1] = darkest[1];

                if(updateProgress(tools) || tools.isFinished()){
                    endSquiggle();
                    return;
                }
            }

            endSquiggle();
            if (tools.pfmTask.isColourMatchTask()){
                return;
            }
        }
    }

    public void beginSquiggle(){
        //used by Curve PFMs
    }

    public void endSquiggle(){
        //used by Curve PFMs
    }

    public void addGeometry(IPixelData pixelData, int x1, int y1, int x2, int y2, int adjust){
        addGeometryWithColourSamples(pixelData, new GLine(x1, y1, x2, y2), adjust);
    }

    protected boolean updateProgress(PlottingTools tools){
        PFMTask task = tools.pfmTask;
        if(!task.applySketchPFMProgressCallback(this)){
            double avgLuminance = tools.getPixelData().getAverageLuminance();
            lineProgress = maxLines == -1 ? 0 : (double)tools.drawing.geometries.size() / maxLines;
            lumProgress = avgLuminance >= desiredLuminance ? 1 : (avgLuminance - initialLuminance) / ((desiredLuminance - initialLuminance)*lineDensity);
            actualProgress = Math.max(lineProgress, lumProgress);
        }
        tools.updateProgress(actualProgress, 1D);
        return actualProgress >= 1;
    }

    /**
     * @return returns the darkness of this new neighbour
     */
    protected abstract float findDarkestNeighbour(IPixelData pixels, int[] point, int[] darkestDst);

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public int maxLineLength() {
        return maxLineLength;
    }
}
