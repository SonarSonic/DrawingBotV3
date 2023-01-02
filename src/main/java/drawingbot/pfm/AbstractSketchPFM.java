package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.PixelTargetCache;
import drawingbot.image.PixelTargetDarkestArea;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottingTools;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    //user settings
    public int squiggleMinLength;
    public int squiggleMaxLength;
    public float squiggleMaxDeviation;

    public int adjustbrightness;
    public float lineDensity;

    public int lineTests;
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;
    public boolean shouldDrawMoves;

    //process specific
    public double initialLuminance;

    public final float desiredLuminance = 253.5F;

    //latest progress
    public double lineProgress = 0;
    public double lumProgress = 0;
    public double actualProgress = 0;

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public PixelTargetCache targetCache;

    @Override
    public void setup() {
        super.setup();
        if(maxLineLength < minLineLength){
            int value = minLineLength;
            minLineLength = maxLineLength;
            maxLineLength = value;
        }

        if(squiggleMaxLength < squiggleMinLength){
            int value = squiggleMinLength;
            squiggleMinLength = squiggleMaxLength;
            squiggleMaxLength = value;
        }
        initialLuminance = this.tools.getPixelData().getAverageLuminance();
        targetCache = new PixelTargetDarkestArea(tools.getPixelData());
        findDarkestMethod = (pixelData, dst) -> targetCache.updateNextDarkestPixel(dst);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public BiConsumer<IPixelData, int[]> findDarkestMethod = AbstractDarkestPFM::findDarkestArea;
    public int[] last = new int[]{-1, -1};
    public int[] current = new int[]{-1, -1};
    public int[] darkest = new int[]{-1, -1};

    @Override
    public void run() {
        while(!tools.isFinished()){
            if(shouldLiftPen || current[0] == -1){
                findDarkestMethod.accept(tools.getPixelData(), darkest);
                if(shouldDrawMoves){
                    addSegments(tools.getPixelData(), current[0], current[1], darkest[0], darkest[1], adjustbrightness, null);
                }
            }else{
                findDarkestNeighbour(tools.getPixelData(), last, current, darkest);
                addSegments(tools.getPixelData(), current[0], current[1], darkest[0], darkest[1], adjustbrightness, null);
                tools.getPixelData().adjustLuminance(current[0], current[1], 1);

                last[0] = current[0];
                last[1] = current[1];

                current[0] = darkest[0];
                current[1] = darkest[1];
            }

            float initialDarkness = tools.getPixelData().getLuminance(darkest[0], darkest[1]);
            float allowableDarkness = initialDarkness + Math.max(1, 255 * squiggleMaxDeviation);

            last[0] = current[0];
            last[1] = current[1];

            current[0] = darkest[0];
            current[1] = darkest[1];

            beginSquiggle();

            for (int s = 0; s < squiggleMaxLength; s++) {

                float next = findDarkestNeighbour(tools.getPixelData(), last, current, darkest);

                //if the first line has been drawn and the next neighbour isn't dark enough to add to the squiggle, end it prematurely
                if(s > 3 && (s >= squiggleMinLength - 1) && (next == -1 || next > allowableDarkness || next > tools.getPixelData().getAverageLuminance())){
                    endSquiggle();

                    //there are no valid lines from this point, brighten it slightly so we don't test it again immediately.
                    tools.getPixelData().adjustLuminance(current[0], current[1], 5);
                    break;
                }

                if(next != -1){
                    addSegments(tools.getPixelData(), current[0], current[1], darkest[0], darkest[1], adjustbrightness, null); //TODO

                    last[0] = current[0];
                    last[1] = current[1];

                    current[0] = darkest[0];
                    current[1] = darkest[1];
                }else{
                    //there are no valid lines from this point, brighten it slightly so we don't test it again immediately.
                    tools.getPixelData().adjustLuminance(current[0], current[1], 5);
                    break;
                }

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

    public void addSegments(IPixelData pixelData, int x1, int y1, int x2, int y2, int adjust, Consumer<IGeometry> segments){
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
    protected abstract float findDarkestNeighbour(IPixelData pixels, int[] lastPoint, int[] currentPoint, int[] darkestDst);

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public int maxLineLength() {
        return maxLineLength;
    }
}
