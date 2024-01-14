package drawingbot.pfm;

import drawingbot.api.IPixelData;
import drawingbot.geom.easing.EasingUtils;
import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.*;
import drawingbot.pfm.helpers.PFMRenderPipe;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PlottingTools;
import drawingbot.utils.Utils;

import java.util.function.BiConsumer;

public abstract class AbstractSketchPFM extends AbstractDarkestPFM {

    //user settings
    public int squiggleMinLength;
    public int squiggleMaxLength;
    public float squiggleMaxDeviation;

    public float lineDensity;

    public int lineTests;
    public int minLineLength;
    public int maxLineLength;
    public int maxLines;

    public boolean shouldLiftPen;

    //process specific
    public double initialLuminance = 0;

    public final float desiredLuminance = 253.5F;

    //latest progress
    public double lineProgress = 0;
    public double lumProgress = 0;
    public double actualProgress = 0;

    // ERASING \\
    public float radiusMin = 1F, radiusMax = 4F;
    public float eraseMin = 1F, eraseMax = 255F;
    public float tone = 0.5F;

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

        if(radiusMax < radiusMin){
            float value = radiusMin;
            radiusMin = radiusMax;
            radiusMax = value;
        }

        if(eraseMin < eraseMin){
            float value = eraseMin;
            eraseMin = eraseMax;
            eraseMax = value;
        }

        initialLuminance = this.tools.getPixelData().getAverageLuminance();
        targetCache = new PixelTargetDarkestArea(tools, tools.getPixelData());
        findDarkestPixelMethod = (pixelData, dst) -> targetCache.updateNextDarkestPixel(dst);
        renderPipe.setRescaleMode(tools.getCanvas().getRescaleMode());
    }

    @Override
    public IPixelData createPixelData(int width, int height) {
        if(radiusMin != radiusMax || tools.getCanvas().getRenderedPenWidth() != 1F){
            return new PixelDataAdditiveComposite(width, height);
        }
        return new PixelDataARGBY(width, height);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public BiConsumer<IPixelData, int[]> findDarkestPixelMethod = AbstractDarkestPFM::findDarkestArea;
    public final PathFindingContext context = new PathFindingContext();

    private int fails = 0;
    private static final int MAX_FAILS = 1000;

    @Override
    public void run() {
        //Check we actually need to run, images passed via Color Separation may not have any drawable lines.
        if(updateProgress(tools)){
            return;
        }

        while(!tools.isFinished()){

            // Find the darkest pixel in the image, using the current method, use this as the current position
            context.last = context.current;
            context.current = new int[2];
            findDarkestPixelMethod.accept(tools.getPixelData(), context.current);

            // Create a linking geometry, to draw this forced pen move
            if(!shouldLiftPen && context.hasResult()){
                addLinkingGeometry(tools.getPixelData(), context.last, context.current);
            }

            beginSquiggle();

            // Keep track of the squiggle's deviation
            float initialDarkness = tools.getPixelData().getLuminance(context.getX(), context.getY());
            float allowableDarkness = !shouldLiftPen ? Float.MAX_VALUE : initialDarkness + Math.max(1, 255 * squiggleMaxDeviation);
            boolean failed = false;

            // Run the loop until it is stopped in the case of should lift pen, or run it until the maximum squiggle length if we should lift the pen
            for (int s = 0; !shouldLiftPen || s < squiggleMaxLength; s++) {

                // Remove the previous result
                context.clearResult();

                // Find the next geometry / result
                nextPathFindingResult(context, tools.getPixelData());

                // If no result has been found we end this squiggle early
                if(!context.hasResult()){
                    failed = true;
                    break;
                }

                // If the squiggle has passed the minimum length now we check the squiggles darkness
                if(shouldLiftPen && s >= squiggleMinLength){

                    // Check the last generated geometry doesn't exceed the allowable darkness, if it does end the squiggle early
                    if(context.getAvgLuminance() > allowableDarkness || context.getAvgLuminance()  > tools.getPixelData().getAverageLuminance()){
                        break;
                    }

                }

                // The generated geometry has passed all our tests, add it too the drawing & update the context
                addPathFindingResult(context, tools.getPixelData());
                context.last = context.getPosition();
                context.current = context.getDstPosition();

                // Check the squiggle shouldn't be ended early
                if(updateProgress(tools) || tools.isFinished()){
                    endSquiggle();
                    return;
                }
            }

            endSquiggle();

            if(failed){
                // If there were no path finding results from the current point, it must be isolated, so erase it.
                tools.getPixelData().setLuminance(context.getX(), context.getY(), 255);
                fails++;
            }else{
                fails = 0;
            }

            // If we still can't find a valid shape stop the process.
            if(fails >= MAX_FAILS){
                return;
            }

            // Colour Match will may change pen after each squiggle, so prevent the default while loop.
            if (tools.pfmTask.isColourMatchTask()){
                return;
            }
        }
    }

    public void beginSquiggle(){

    }

    public void endSquiggle(){

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

    public abstract void nextPathFindingResult(PathFindingContext context, IPixelData pixels);

    public void addPathFindingResult(PathFindingContext context, IPixelData pixels){
        eraseAddGeometry(pixels, new GLine(context.getX(), context.getY(), context.getDstX(), context.getDstY()));
    }

    public void addLinkingGeometry(IPixelData pixels, int[] src, int[] dst){
        eraseAddGeometry(pixels, new GLine(src[0], src[1], dst[0], dst[1]));
    }

    @Override
    public int minLineLength() {
        return minLineLength;
    }

    @Override
    public int maxLineLength() {
        return maxLineLength;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public PFMRenderPipe renderPipe = new PFMRenderPipe();

    /**
     * Convenience Method: Erases the geometry on the current Pixel Data and adds it too the current drawing.
     */
    public void eraseAddGeometry(IPixelData pixelData, IGeometry geometry){
        int colourSamples = eraseGeometry(pixelData, geometry);
        tools.addGeometry(geometry, -1, colourSamples);
    }

    /**
     * Erases the Geometry on the provided pixel data.
     */
    public int eraseGeometry(IPixelData pixelData, IGeometry geometry){
        return eraseGeometry(pixelData, geometry, radiusMin, radiusMax, eraseMin, eraseMax);
    }

    /**
     * Erases the Geometry on the provided pixel data, allows you to provide custom erasing values
     */
    public int eraseGeometry(IPixelData pixelData, IGeometry geometry, float radiusMin, float radiusMax, float eraseMin, float eraseMax){
        int luminance = !pixelData.withinXY(context.getX(), context.getY()) ? 255 : pixelData.getLuminance(context.getX(), context.getY());
        double xProgress = luminance/255D;
        double yProgress = (EasingUtils.easeInCubic(xProgress)*(tone)) + (xProgress*(1-tone));

        float strokeWidth = (float) (radiusMin + yProgress * (radiusMax - radiusMin));
        int erase = (int) (eraseMin + yProgress * (eraseMax-eraseMin));

        return renderPipe.eraseGeometry(pixelData, tools.getReferencePixelData(), geometry, erase, tools.getCanvas().getRenderedPenWidth() * strokeWidth);
    }

    @Override
    public void onStopped() {
        super.onStopped();
        if(targetCache != null){
            targetCache.destroy();
        }
    }

    public static class PathFindingContext {

        //// Path Finding Current Positions \\\\
        public int[] current = new int[2];
        public int[] last = new int[2];

        //// Path Finding Result \\\\
        public float[] data = null;
        private float avgLuminance = 0F;
        private int[] dst = new int[2];
        private boolean hasResult;
        public Object resultData = null;

        public PathFindingContext(){}

        public PathFindingContext(int[] dst, float luminance, float[] data) {
            setResult(dst, avgLuminance, data);
        }

        public void clearResult(){
            this.data = null;
            this.avgLuminance = 0F;
            this.dst = new int[2];
            this.hasResult = false;
        }

        public void setResult(int[] dst, float luminance) {
            setResult(dst, luminance, null, null);
        }

        public void setResult(int[] dst, float luminance, float[] testData) {
            setResult(dst, luminance, testData, null);
        }

        public void setResult(int[] dst, float luminance, float[] testData, Object resultData) {
            this.data = testData;
            this.avgLuminance = luminance;
            this.dst = dst;
            this.resultData = resultData;

            // Check the result is actually valid
            this.hasResult = luminance != -1;
        }

        public boolean hasResult(){
            return hasResult;
        }

        public float getAvgLuminance() {
            return avgLuminance;
        }

        public int[] getDstPosition() {
            return dst;
        }

        public int getDstX(){
            return dst[0];
        }

        public int getDstY(){
            return dst[1];
        }

        public int getX(){
            return current[0];
        }

        public int getY(){
            return current[1];
        }

        public int[] getPosition(){
            return current;
        }

        public int getLastX(){
            return last[0];
        }

        public int getLastY(){
            return last[1];
        }

        public int[] getLastPosition(){
            return last;
        }

    }
}
