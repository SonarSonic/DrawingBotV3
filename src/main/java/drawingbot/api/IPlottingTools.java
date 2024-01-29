package drawingbot.api;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumRendererType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Random;

public interface IPlottingTools {

    /**
     * The {@link IPFM} should check this value during {@link IPFM#run()}.
     * If the plotting task is finished the {@link IPFM} should end the process prematurely.
     *<p>A typical usage might look something like this
     * <pre><code>
     * while(!tools.isFinished()){
     *      //part of a long running process
     * }
     * </code></pre>
     * @return if the task has been finished by the user.
     */
    boolean isFinished();

    /**
     * When the PFM has been cancelled, it should stop as soon as possible
     * When TRUE, {@link #isFinished()} will also be true.
     * @return if the task has been cancelled by the task manager.
     */
    boolean isCancelled();


    /**
     * Updates the progress bar in the user interface
     * @param progress the current progress
     * @param max the max progress
     */
    void updateProgress(double progress, double max);

    void updateMessage(String message);

    void updateTitle(String title);

    int getPlottingWidth();

    int getPlottingHeight();

    /**
     * The pixel data the {@link IPFM} can alter while processing
     * This will be set to a copy of {@link #getReferencePixelData()} before the process begins
     * @return the pixels of the image , in ARGB format
     */
    IPixelData getPixelData();

    /**
     * The pixel data of the original image the {@link IPFM} should draw
     * @return the reference pixels, in ARGB format
     */
    IPixelData getReferencePixelData();

    BufferedImage getOriginalImage();

    BufferedImage getReferenceImage();

    BufferedImage getPlottingImage();

    File getImageFile();

    boolean isToneMapping();

    ////////////////////////////////////////////////////////

    //// GEOMETRY TOOLS \\\\

    IGeometry createLine(float x1, float y1, float x2, float y2);

    IGeometry createRectangle(float x, float y, float width, float height);

    IGeometry createEllipse(float x, float y, float width, float height);

    IGeometry createQuadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2);

    IGeometry createCubicCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2);

    IGeometry createCatmullRomCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2, float tension);

    IGeometry createShape(Shape shape);

    default void line(float x1, float y1, float x2, float y2){
        addGeometry(createLine(x1, y1, x2, y2));
    }

    default void rect(float x, float y, float width, float height){
        addGeometry(createRectangle(x, y, width, height));
    }

    default void ellipse(float x, float y, float width, float height){
        addGeometry(createEllipse(x, y, width, height));
    }

    default void quadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2){
        addGeometry(createQuadCurve(x1, y1, ctrlx, ctrly, x2, y2));
    }

    default void cubicCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2){
        addGeometry(createCubicCurve(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2));
    }

    default void catmullRomCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2, float tension){
        addGeometry(createCatmullRomCurve(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2, tension));
    }

    default void shape(Shape shape){
        addGeometry(createShape(shape));
    }

    void addGeometry(IGeometry geometry, int penIndex, int rgba, int fillType);

    void addGeometry(IGeometry geometry, int penIndex, int rgba);

    void addGeometry(IGeometry geometry, int penIndex);

    void addGeometry(IGeometry geometry);

    default void addGeometries(List<IGeometry> geometries){
        for(IGeometry geometry : geometries){
            addGeometry(geometry);
        }
    }

    IGeometry getLastGeometry();

    void clearDrawing();

    default void reRender(){
        reRender(EnumRendererType.ANY);
    }

    void reRender(EnumRendererType rendererType);

    ////////////////////////////////////////////////////////

    //// PIXEL DATA TOOLS \\\\

    /**
     * Fast implementation: Accurate too a pixel only.
     */
    boolean withinPlottableArea(int x, int y);

    /**
     * Slow implementation: Accurate to sub pixel positions.
     */
    boolean withinPlottableAreaPrecise(double x, double y);

    @Deprecated
    void addGeometryWithColourSamples(IPixelData pixelData, IGeometry geometry, int adjust);

    @Deprecated
    int adjustGeometryLuminance(IPixelData pixelData, IGeometry geometry, int adjust);

    void findDarkestArea(IPixelData pixels, int[] dest);

    List<int[]> findDarkestPixels(IPixelData pixels);

    float findDarkestLine(IPixelData pixels, int startX, int startY, int minLength, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, int[] darkestDst);

    void forAvailableEndPoints(IPixelData pixels, int startX, int startY, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, boolean safe, BresenhamHelper.IPixelSetter consumer);

    ////////////////////////////////////////////////////////

    //// PATH BUILDING TOOLS \\\\

    void startPath();

    void endPath();

    void moveTo(float x, float y);

    void lineTo(float x, float y);

    void quadTo(float x1, float y1, float x2, float y2);

    void curveTo(float x1, float y1, float x2, float y2, float x3, float y3);

    void closePath();

    ////////////////////////////////////////////////////////

    //// CURVE BUILDING TOOLS \\\\

    void startCurve();

    void endCurve();

    void addCurveVertex(float x, float y);

    void setCurveTension(float tension);

    float getCurveTension();

    ////////////////////////////////////////////////////////

    //// GEOMETRY ATTRIBUTES \\\\

    void pushAttrib();

    void popAttrib();

    ////////////////////////////////////////////////////////

    //// PEN TOOLS \\\\

    IDrawingSet<?> getCurrentDrawingSet();

    int getCurrentPen();

    void setCurrentPen(IDrawingPen drawingPen);

    void setCurrentPen(int penNumber);

    int getBestPen(int x, int y);

    int getBestMatchedPen(int argb);

    int getColourSampling();

    void setColourSampling(int argb);

    void setColourSampling(IPixelData data, int x, int y);

    void resetColourSampling();

    ////////////////////////////////////////////////////////

    //// GROUP TOOLS \\\\

    void setGroupType(PlottedGroup.GroupDistributionType groupType);

    void setGroupDistributionType(EnumDistributionType distributionType);

    void setGroupNeedsDistribution(boolean needsDistribution);

    ////////////////////////////////////////////////////////

    //// FILL TOOLS \\\\

    int getCurrentFillType();

    void setCurrentFillType(int fillType);

    ////////////////////////////////////////////////////////

    //// RANDOM SEED TOOLS \\\\

    Random getRandom();

    int getRandomSeed();

    void setRandomSeed(int seed);

    default boolean randomBoolean(){
        return getRandom().nextBoolean();
    }

    default int randomInt(){
        return getRandom().nextInt();
    }

    default int randomInt(int bound){
        return getRandom().nextInt(bound);
    }

    default int randomInt(int origin, int bound){
        if(origin == bound){
            return origin;
        }
        if (origin >= bound) {
            return getRandom().nextInt(bound, origin);
        }
        return getRandom().nextInt(origin, bound);
    }

    default long randomLong(){
        return getRandom().nextLong();
    }

    default long randomLong(long bound){
        return getRandom().nextLong(bound);
    }

    default long randomLong(long origin, long bound){
        if(origin == bound){
            return origin;
        }
        if (origin >= bound) {
            return getRandom().nextLong(bound, origin);
        }
        return getRandom().nextLong(origin, bound);
    }

    default float randomFloat(){
        return getRandom().nextFloat();
    }

    default float randomFloat(float bound){
        return getRandom().nextFloat(bound);
    }

    default float randomFloat(float origin, float bound){
        if(origin == bound){
            return origin;
        }
        if (origin >= bound) {
            return getRandom().nextFloat(bound, origin);
        }
        return getRandom().nextFloat(origin, bound);
    }

    default double randomDouble(){
        return getRandom().nextDouble();
    }

    default double randomDouble(double bound){
        return getRandom().nextDouble(bound);
    }

    default double randomDouble(double origin, double bound){
        if(origin == bound){
            return origin;
        }
        if (origin >= bound) {
            return getRandom().nextDouble(bound, origin);
        }
        return getRandom().nextDouble(origin, bound);
    }

    ////////////////////////////////////////////////////////

    //// TRANSFORM TOOLS \\\\

    AffineTransform getTransform();

    void pushMatrix();

    void popMatrix();

    default double getScaleX() {
        return getTransform().getScaleX();
    }

    default double getScaleY() {
        return getTransform().getScaleY();
    }

    default double getShearX() {
        return getTransform().getShearX();
    }

    default double getShearY() {
        return getTransform().getShearY();
    }

    default double getTranslateX() {
        return getTransform().getTranslateX();
    }

    default double getTranslateY() {
        return getTransform().getTranslateY();
    }

    default void translate(double tx, double ty) {
        getTransform().translate(tx, ty);
    }

    default void rotate(double theta) {
        getTransform().rotate(theta);
    }

    default void rotate(double theta, double anchorx, double anchory) {
        getTransform().rotate(theta, anchorx, anchory);
    }

    default void rotate(double vecx, double vecy) {
        getTransform().rotate(vecx, vecy);
    }

    default void rotate(double vecx, double vecy, double anchorx, double anchory) {
        getTransform().rotate(vecx, vecy, anchorx, anchory);
    }

    default void scale(double sx, double sy) {
        getTransform().scale(sx, sy);
    }

    default void shear(double shx, double shy) {
        getTransform().shear(shx, shy);
    }

    ////////////////////////////////////////////////////////
}
