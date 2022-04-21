package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.*;
import drawingbot.geom.GeometryClipping;
import drawingbot.geom.shapes.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.AbstractDarkestPFM;
import drawingbot.pfm.helpers.BresenhamHelper;
import drawingbot.pfm.helpers.ColourSampleTest;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.UnitsLength;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class PlottingTools implements IPlottingTools {

    public PFMTask pfmTask; //only to used by PFMs, unavailable to ad-hoc PlottingTools
    public PlottedDrawing drawing;
    public PlottedGroup currentGroup;
    public PathBuilder pathBuilder;
    public BresenhamHelper bresenham;
    public ColourSampleTest defaultColourTest;
    public AffineTransformStack transform;
    public int currentPen = 0;
    public int currentFillType = -1;

    public int randomSeed = 0;
    public Random random;
    public AffineTransform plottingTransform;

    // CLIPPING \\
    public Shape clippingShape = null;

    public PlottingTools(PlottedDrawing drawing) {
        this(drawing, drawing.getPlottedGroup(0));
    }

    public PlottingTools(PlottedDrawing drawing, PlottedGroup currentGroup) {
        this.drawing = drawing;
        this.currentGroup = currentGroup;
        this.pathBuilder = new PathBuilder(this);
        this.bresenham = new BresenhamHelper();
        this.defaultColourTest = new ColourSampleTest();
        this.transform = new AffineTransformStack(32);
        this.random = new Random(randomSeed);
    }

    ////////////////////////////////////////////////////////

    //// INTERNAL USE ONLY \\\\

    public PlottedDrawing getPlottedDrawing(){
        return drawing;
    }

    public ICanvas getCanvas(){
        return drawing.getCanvas();
    }

    public PathBuilder getPathBuilder(){
        return pathBuilder;
    }

    public BresenhamHelper getBresenhamHelper(){
        return bresenham;
    }

    public Shape getClippingShape() {
        return clippingShape;
    }

    public void setClippingShape(Shape clippingShape) {
        this.clippingShape = clippingShape;
    }

    ////////////////////////////////////////////////////////

    @Override
    public boolean isFinished() {
        if(pfmTask == null){
            return false;
        }
        return pfmTask.isFinished();
    }

    @Override
    public void updateProgress(double progress, double max) {
        if(pfmTask == null){
            return;
        }
        pfmTask.updatePlottingProgress(progress, max);
    }

    @Override
    public void updateMessage(String message) {
        if(pfmTask == null){
            return;
        }
        pfmTask.updateMessage(message);
    }

    @Override
    public void updateTitle(String title) {
        if(pfmTask == null){
            return;
        }
        pfmTask.updateTitle(title);
    }

    @Override
    public int getPlottingWidth() {
        return (int)drawing.getCanvas().getDrawingWidth(UnitsLength.PIXELS);
    }

    @Override
    public int getPlottingHeight() {
        return (int)drawing.getCanvas().getDrawingHeight(UnitsLength.PIXELS);
    }

    @Override
    public IPixelData getPixelData() {
        assert pfmTask instanceof PFMTaskImage;
        return ((PFMTaskImage) pfmTask).getPixelData();
    }

    @Override
    public IPixelData getReferencePixelData() {
        assert pfmTask instanceof PFMTaskImage;
        return ((PFMTaskImage) pfmTask).getReferencePixelData();
    }

    @Override
    public BufferedImage getOriginalImage() {
        return drawing.getOriginalImage();
    }

    @Override
    public BufferedImage getReferenceImage() {
        return drawing.getReferenceImage();
    }

    @Override
    public BufferedImage getPlottingImage() {
        return drawing.getPlottingImage();
    }

    @Override
    public File getImageFile() {
        return drawing.getOriginalFile();
    }

    @Override
    public GLine createLine(float x1, float y1, float x2, float y2) {
        return new GLine(x1, y1, x2, y2);
    }

    @Override
    public GRectangle createRectangle(float x, float y, float width, float height) {
        return new GRectangle(x, y, width, height);
    }

    @Override
    public GEllipse createEllipse(float x, float y, float width, float height) {
        return new GEllipse(x, y, width, height);
    }

    @Override
    public GQuadCurve createQuadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
        return new GQuadCurve(x1, y1, ctrlx, ctrly, x2, y2);
    }

    @Override
    public GCubicCurve createCubicCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2) {
        return new GCubicCurve(x1, y1, ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, x2, y2);
    }

    public GCubicCurve createCatmullRomCurve(float x1, float y1, float ctrl1X, float ctrl1Y, float ctrl2X, float ctrl2Y, float x2, float y2, float tension) {
        float[][] bezier = PathBuilder.catmullToBezier(new float[][]{new float[]{x1, y1}, new float[]{ctrl1X, ctrl1Y}, new float[]{ctrl2X, ctrl2Y}, new float[]{x2, y2}}, new float[4][2], tension);
        return new GCubicCurve(bezier[0][0], bezier[0][1], bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
    }

    @Override
    public GShape createShape(Shape shape) {
        return new GShape(shape);
    }

    @Override
    public void addGeometry(IGeometry geometry, int penIndex, int rgba, int fillType) {
        if(geometry.getFillType() == -1 && fillType != -1){
            geometry.setFillType(fillType);
        }
        addGeometry(geometry, penIndex, rgba);
    }

    @Override
    public void addGeometry(IGeometry geometry, int penIndex, int rgba){
        if(geometry.getSampledRGBA() == -1 && rgba != -1){
            geometry.setSampledRGBA(rgba);
        }
        addGeometry(geometry, penIndex);
    }

    @Override
    public void addGeometry(IGeometry geometry, int penIndex) {
        if(geometry.getPenIndex() == -1 && penIndex != -1){
            geometry.setPenIndex(penIndex);
        }
        addGeometry(geometry);
    }

    @Override
    public void addGeometry(IGeometry geometry) {
        geometry = geometry.transformGeometry(transform);

        if(geometry.getPenIndex() == -1){
            geometry.setPenIndex(currentPen);
        }

        if(geometry.getFillType() == -1){
            geometry.setFillType(currentFillType);
        }

        geometry.setPFMPenIndex(geometry.getPenIndex()); //store the pfm pen index for later reference
        geometry.setGroupID(currentGroup.getGroupID());

        //transform geometry back to the images size
        if(plottingTransform != null){
            geometry = geometry.transformGeometry(plottingTransform);
        }


        if(clippingShape != null && GeometryClipping.shouldClip(clippingShape, geometry)){
            List<IGeometry> geometries = GeometryClipping.clip(clippingShape, geometry);
            geometries.forEach(g -> getPlottedDrawing().addGeometry(g));
        }else{
            getPlottedDrawing().addGeometry(geometry);
        }

    }

    @Override
    public IGeometry getLastGeometry() {
        return getPlottedDrawing().geometries.isEmpty() ? null : getPlottedDrawing().geometries.get(getPlottedDrawing().geometries.size()-1);
    }

    @Override
    public void clearAllGeometries() {
        getPlottedDrawing().clearGeometries();
        if(pfmTask != null){
            pfmTask.drawingManager.clearDrawingRender();
        }
    }

    ////////////////////////////////////////////////////////

    //// PIXEL DATA TOOLS \\\\

    @Override
    public void addGeometryWithColourSamples(IPixelData pixelData, IGeometry geometry, int adjust){
        int colourSamples = adjustGeometryLuminance(pixelData, geometry, adjust);
        addGeometry(geometry, -1, colourSamples);
    }

    @Override
    public int adjustGeometryLuminance(IPixelData pixelData, IGeometry geometry, int adjust){
        defaultColourTest.resetColourSamples(adjust);
        geometry.renderBresenham(bresenham, (x,y) -> defaultColourTest.addSample(pixelData, x, y));
        return defaultColourTest.getCurrentAverage();
    }

    @Override
    public void findDarkestArea(IPixelData pixels, int[] dest) {
        AbstractDarkestPFM.findDarkestArea(pixels, dest);
    }

    @Override
    public List<int[]> findDarkestPixels(IPixelData pixels) {
        return AbstractDarkestPFM.findDarkestPixels(pixels);
    }

    @Override
    public float findDarkestLine(IPixelData pixels, int startX, int startY, int minLength, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, int[] darkestDst) {
        return AbstractDarkestPFM.findDarkestLine(bresenham, pixels, startX, startY, minLength, maxLength, maxTests, startAngle, drawingDeltaAngle, shading, darkestDst);
    }

    @Override
    public void runDarkestTest(IPixelData pixels, int startX, int startY, int maxLength, int maxTests, float startAngle, float drawingDeltaAngle, boolean shading, boolean safe, BiConsumer<Integer, Integer> consumer) {
        AbstractDarkestPFM.runDarkestTest(bresenham, pixels, startX, startY, maxLength, maxTests, startAngle, drawingDeltaAngle, shading, safe, consumer);
    }

    ////////////////////////////////////////////////////////

    //// PATH BUILDING TOOLS \\\\

    @Override
    public void startPath(){
        pathBuilder.startPath();
    }

    @Override
    public void endPath(){
        pathBuilder.endPath();
    }

    @Override
    public void moveTo(float x, float y) {
        pathBuilder.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        pathBuilder.lineTo(x, y);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2){
        pathBuilder.quadTo(x1, y1, x2, y2);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3){
        pathBuilder.curveTo(x1, y1, x2, y2, x3, y3);
    }

    @Override
    public void closePath(){
        pathBuilder.closePath();
    }

    ////////////////////////////////////////////////////////

    //// CURVE BUILDING TOOLS \\\\

    @Override
    public void startCurve(){
        pathBuilder.startCatmullCurve();
    }

    @Override
    public void endCurve(){
        pathBuilder.endCatmullCurve();
    }

    @Override
    public void addCurveVertex(float x, float y){
        pathBuilder.addCatmullCurveVertex(x, y);
    }

    @Override
    public void setCurveTension(float tension) {
        pathBuilder.setCatmullCurveTension(tension);
    }

    @Override
    public float getCurveTension() {
        return pathBuilder.getCatmullTension();
    }


    ////////////////////////////////////////////////////////

    //// GEOMETRY ATTRIBUTES \\\\
    public List<GeometryAttributes> attribList = new ArrayList<>();

    public static class GeometryAttributes {
        public Integer penIndex = null;
        public Integer groupID = null;
        public Integer fillType = null;
        public Float curveTension = null;

        public void onPush(PlottingTools tools){
            penIndex = tools.getCurrentPen();
            groupID = tools.currentGroup.getGroupID();
            fillType = tools.getCurrentFillType();
            curveTension = tools.getCurveTension();
        }

        public void onPop(PlottingTools tools){
            if(penIndex != null) tools.setCurrentPen(penIndex);
            if(groupID != null)  tools.currentGroup = tools.drawing.getPlottedGroup(groupID);
            if(fillType != null)  tools.setCurrentFillType(fillType);
            if(curveTension != null)  tools.setCurveTension(curveTension);
        }
    }

    public void pushAttrib(){
        GeometryAttributes attrib = new GeometryAttributes();
        attrib.onPush(this);
        attribList.add(attrib);
    }

    public void popAttrib(){
        if(attribList.isEmpty()){
            DrawingBotV3.logger.warning("Unable to Pop Geometry Attributes");
        }else{
            GeometryAttributes attrib = attribList.remove(attribList.size()-1);
            attrib.onPop(this);
        }
    }

    ////////////////////////////////////////////////////////

    //// PEN TOOLS \\\\

    @Override
    public ObservableDrawingSet getCurrentDrawingSet() {
        return currentGroup.drawingSet;
    }

    public int getCurrentPen(){
        return currentPen;
    }

    @Override
    public void setCurrentPen(IDrawingPen drawingPen){
        if(drawingPen instanceof ObservableDrawingPen){
            ObservableDrawingPen observableDrawingPen = (ObservableDrawingPen) drawingPen;
            setCurrentPen(observableDrawingPen.penNumber.get());
        }
    }

    @Override
    public void setCurrentPen(int penNumber){
        currentPen = penNumber;
    }

    ////////////////////////////////////////////////////////

    //// GROUP TOOLS \\\\

    @Override
    public void setGroupType(PlottedGroup.GroupDistributionType groupType){
        currentGroup.groupType = groupType;
    }

    @Override
    public void setGroupDistributionType(EnumDistributionType distributionType){
        currentGroup.overrideDistributionType = distributionType;
    }

    @Override
    public void setGroupNeedsDistribution(boolean needsDistribution){
        currentGroup.needsDistribution = needsDistribution;
    }

    ////////////////////////////////////////////////////////

    //// FILL TOOLS \\\\

    @Override
    public int getCurrentFillType() {
        return currentFillType;
    }

    @Override
    public void setCurrentFillType(int fillType) {
        currentFillType = fillType;
    }

    ////////////////////////////////////////////////////////

    //// RANDOM SEED TOOLS \\\\

    @Override
    public Random getRandom(){
        return random;
    }

    @Override
    public int getRandomSeed(){
        return randomSeed;
    }

    ////////////////////////////////////////////////////////

    //// TRANSFORM TOOLS \\\\

    @Override
    public AffineTransform getTransform(){
        return transform;
    }

    @Override
    public void pushMatrix() {
        transform.pushMatrix();
    }

    @Override
    public void popMatrix() {
        transform.popMatrix();
    }
}